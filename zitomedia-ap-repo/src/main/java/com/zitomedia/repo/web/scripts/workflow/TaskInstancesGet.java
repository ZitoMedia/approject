package com.zitomedia.repo.web.scripts.workflow;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.zitomedia.repo.model.ZitoMediaWorkflowModel;
import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery.OrderBy;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ModelUtil;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript impelementation to return workflow task instances.
 *
 * @author Nick Smith
 * @author Gavin Cornwell
 * @since 3.4
 */
public class TaskInstancesGet extends AbstractWorkflowWebscript {
    public static final String PARAM_AUTHORITY = "authority";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PRIORITY = "priority";
    public static final String PARAM_DUE_BEFORE = "dueBefore";
    public static final String PARAM_DUE_AFTER = "dueAfter";
    public static final String PARAM_PROPERTIES = "properties";
    public static final String PARAM_POOLED_TASKS = "pooledTasks";
    public static final String VAR_WORKFLOW_INSTANCE_ID = "workflow_instance_id";

    public static final String PARAM_CUSTOMER_CATEGORY = "customerCategory";
    public static final String PARAM_WF_NAME = "wfName";
    public static final String PARAM_CUSTOM_EXCLUDE = "customExclude";

    public static final String PARAM_SORT_BY = "sortBy";
    public static final String PARAM_DIRECTION = "direction";

    private static final Logger logger = Logger.getLogger(TaskInstancesGet.class);

    private WorkflowTaskDueAscComparator taskComparator = new WorkflowTaskDueAscComparator();

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache) {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        Map<String, Object> filters = new HashMap<String, Object>(4);

        // authority is not included into filters list as it will be taken into account before filtering
        String authority = getAuthority(req);

        if (authority == null) {
            // ALF-11036 fix, if authority argument is omitted the tasks for the current user should be returned.
            authority = authenticationService.getCurrentUserName();
        }

        // state is also not included into filters list, for the same reason
        WorkflowTaskState state = getState(req);

        // look for a workflow instance id
        String workflowInstanceId = params.get(VAR_WORKFLOW_INSTANCE_ID);

        // determine if pooledTasks should be included, when appropriate i.e. when an authority is supplied
        Boolean pooledTasksOnly = getPooledTasks(req);

        // get list of properties to include in the response
        List<String> properties = getProperties(req);

        // get filter param values
        filters.put(PARAM_PRIORITY, req.getParameter(PARAM_PRIORITY));
        processDateFilter(req, PARAM_DUE_BEFORE, filters);
        processDateFilter(req, PARAM_DUE_AFTER, filters);

        String excludeParam = req.getParameter(PARAM_EXCLUDE);
        if (excludeParam != null && excludeParam.length() > 0) {
            filters.put(PARAM_EXCLUDE, new ExcludeFilter(excludeParam));
        }

        String taskName = req.getParameter(PARAM_NAME);
        if (taskName != null && !taskName.equals("")) {
            filters.put(PARAM_NAME, taskName);
        }

        String customerCategory = req.getParameter(PARAM_CUSTOMER_CATEGORY);
        if (customerCategory != null && !customerCategory.equals("")) {
            filters.put(PARAM_CUSTOMER_CATEGORY, customerCategory);
        }

        String wfName = req.getParameter(PARAM_WF_NAME);
        if (wfName != null && !wfName.equals("")) {
            filters.put(PARAM_WF_NAME, wfName);
        }

        String customExcludeParam = req.getParameter(PARAM_CUSTOM_EXCLUDE);
        if (customExcludeParam != null && customExcludeParam.length() > 0) {
            filters.put(PARAM_CUSTOM_EXCLUDE, new ExcludeFilter(customExcludeParam));
        }

        List<WorkflowTask> allTasks;

        if (workflowInstanceId != null) {
            // a workflow instance id was provided so query for tasks
            WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
            taskQuery.setActive(null);
            taskQuery.setProcessId(workflowInstanceId);
            taskQuery.setTaskState(state);
            taskQuery.setOrderBy(new OrderBy[]{OrderBy.TaskDue_Asc});

            if (authority != null) {
                taskQuery.setActorId(authority);
            }

            allTasks = workflowService.queryTasks(taskQuery);
        } else {
            // default task state to IN_PROGRESS if not supplied
            if (state == null) {
                state = WorkflowTaskState.IN_PROGRESS;
            }

            // no workflow instance id is present so get all tasks
            if (authority != null) {
                List<WorkflowTask> tasks = workflowService.getAssignedTasks(authority, state, true);
                List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(authority, true);
                if (pooledTasksOnly != null) {
                    if (pooledTasksOnly.booleanValue()) {
                        // only return pooled tasks the user can claim
                        allTasks = new ArrayList<WorkflowTask>(pooledTasks.size());
                        allTasks.addAll(pooledTasks);
                    } else {
                        // only return tasks assigned to the user
                        allTasks = new ArrayList<WorkflowTask>(tasks.size());
                        allTasks.addAll(tasks);
                    }
                } else {
                    // include both assigned and unassigned tasks
                    allTasks = new ArrayList<WorkflowTask>(tasks.size() + pooledTasks.size());
                    allTasks.addAll(tasks);
                    allTasks.addAll(pooledTasks);
                }

                // sort tasks by due date
                DirectionType directionType = DirectionType.fromString(req.getParameter(PARAM_DIRECTION));
                SortType sortBy = SortType.fromString(req.getParameter(PARAM_SORT_BY));

                switch (sortBy) {
                    case DUE:
                        switch(directionType) {
                            case DES:
                                Collections.sort(allTasks, new WorkflowTaskDueDesComparator());
                                break;
                            case ASC:
                                Collections.sort(allTasks, new WorkflowTaskDueAscComparator());
                                break;
                            default:
                                Collections.sort(allTasks, new WorkflowTaskDueDesComparator());
                                break;
                        }
                        break;
                    case PRIORITY:
                        switch(directionType) {
                            case DES:
                                Collections.sort(allTasks, new WorkflowTaskPriorityDesComparator());
                                break;
                            case ASC:
                                Collections.sort(allTasks, new WorkflowTaskPriorityAscComparator());
                                break;
                            default:
                                Collections.sort(allTasks, new WorkflowTaskPriorityDesComparator());
                                break;
                        }
                        break;
                    case NAME:
                        switch(directionType) {
                            case DES:
                                Collections.sort(allTasks, new WorkflowTaskNameDesComparator());
                                break;
                            case ASC:
                                Collections.sort(allTasks, new WorkflowTaskNameAscComparator());
                                break;
                            default:
                                Collections.sort(allTasks, new WorkflowTaskNameDesComparator());
                                break;
                        }
                        break;
                    default:
                        switch(directionType) {
                            case DES:
                                Collections.sort(allTasks, new WorkflowTaskDueDesComparator());
                                break;
                            case ASC:
                                Collections.sort(allTasks, new WorkflowTaskDueAscComparator());
                                break;
                            default:
                                Collections.sort(allTasks, new WorkflowTaskDueDesComparator());
                                break;
                        }
                        break;
                }
                //Collections.sort(allTasks, taskComparator);
            } else {
                // authority was not provided -> return all active tasks in the system
                WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
                taskQuery.setTaskState(state);
                taskQuery.setActive(null);
                taskQuery.setOrderBy(new OrderBy[]{OrderBy.TaskDue_Asc});
                allTasks = workflowService.queryTasks(taskQuery);
            }
        }

        int maxItems = getIntParameter(req, PARAM_MAX_ITEMS, DEFAULT_MAX_ITEMS);
        int skipCount = getIntParameter(req, PARAM_SKIP_COUNT, DEFAULT_SKIP_COUNT);
        int totalCount = 0;
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

        // Filter results
        WorkflowTask task = null;
        for (int i = 0; i < allTasks.size(); i++) {
            task = allTasks.get(i);
            if (matches(task, filters)) {
                // Total-count needs to be based on matching tasks only, so we can't just use allTasks.size() for this
                totalCount++;
                if (totalCount > skipCount && (maxItems < 0 || maxItems > results.size())) {
                    // Only build the actual detail if it's in the range of items we need. This will
                    // drastically improve performance over paging after building the model
                    results.add(modelBuilder.buildSimple(task, properties));
                }
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("taskInstances", results);

        if (maxItems != DEFAULT_MAX_ITEMS || skipCount != DEFAULT_SKIP_COUNT) {
            // maxItems or skipCount parameter was provided so we need to include paging into response
            model.put("paging", ModelUtil.buildPaging(totalCount, maxItems == DEFAULT_MAX_ITEMS ? totalCount : maxItems, skipCount));
        }

        // create and return results, paginated if necessary
        return model;
    }

    /**
     * Retrieves the list of property names to include in the response.
     *
     * @param req The WebScript request
     * @return List of property names
     */
    private List<String> getProperties(WebScriptRequest req) {
        String propertiesStr = req.getParameter(PARAM_PROPERTIES);
        if (propertiesStr != null) {
            return Arrays.asList(propertiesStr.split(","));
        }
        return null;
    }

    /**
     * Retrieves the pooledTasks parameter.
     *
     * @param req The WebScript request
     * @return null if not present, Boolean object otherwise
     */
    private Boolean getPooledTasks(WebScriptRequest req) {
        Boolean result = null;
        String includePooledTasks = req.getParameter(PARAM_POOLED_TASKS);

        if (includePooledTasks != null) {
            result = Boolean.valueOf(includePooledTasks);
        }

        return result;
    }

    /**
     * Gets the specified {@link WorkflowTaskState}, null if not requested
     *
     * @param req
     * @return
     */
    private WorkflowTaskState getState(WebScriptRequest req) {
        String stateName = req.getParameter(PARAM_STATE);
        if (stateName != null) {
            try {
                return WorkflowTaskState.valueOf(stateName.toUpperCase());
            } catch (IllegalArgumentException e) {
                String msg = "Unrecognised State parameter: " + stateName;
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
            }
        }

        return null;
    }

    /**
     * Returns the specified authority. If no authority is specified then returns the current Fully Authenticated user.
     *
     * @param req
     * @return
     */
    private String getAuthority(WebScriptRequest req) {
        String authority = req.getParameter(PARAM_AUTHORITY);
        if (authority == null || authority.length() == 0) {
            authority = null;
        }
        return authority;
    }

    /**
     * Determine if the given task should be included in the response.
     *
     * @param task    The task to check
     * @param filters The list of filters the task must match to be included
     * @return true if the task matches and should therefore be returned
     */
    private boolean matches(WorkflowTask task, Map<String, Object> filters) {
        // by default we assume that workflow task should be included
        boolean result = true;

        for (String key : filters.keySet()) {
            Object filterValue = filters.get(key);

            // skip null filters (null value means that filter was not specified)
            if (filterValue != null) {
                if (key.equals(PARAM_EXCLUDE)) {
                    ExcludeFilter excludeFilter = (ExcludeFilter) filterValue;
                    String type = task.getDefinition().getMetadata().getName().toPrefixString(this.namespaceService);
                    if (excludeFilter.isMatch(type)) {
                        result = false;
                        break;
                    }
                } else if (key.equals(PARAM_DUE_BEFORE)) {
                    Date dueDate = (Date) task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

                    if (!isDateMatchForFilter(dueDate, filterValue, true)) {
                        result = false;
                        break;
                    }
                } else if (key.equals(PARAM_DUE_AFTER)) {
                    Date dueDate = (Date) task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

                    if (!isDateMatchForFilter(dueDate, filterValue, false)) {
                        result = false;
                        break;
                    }
                } else if (key.equals(PARAM_PRIORITY)) {
                    if (!filterValue.equals(task.getProperties().get(WorkflowModel.PROP_PRIORITY).toString())) {
                        result = false;
                        break;
                    }
                } else if (key.equals(PARAM_NAME)) {
                    if (!filterValue.equals(task.getName())) {
                        result = false;
                        break;
                    }
                } else if (key.equals(PARAM_CUSTOMER_CATEGORY)) {
                    Map<QName, Serializable> taskProperties = task.getProperties();
                    if (!taskProperties.containsKey(ZitoMediaWorkflowModel.PROP_CUSTOMER_CATEGORY)
                            || taskProperties.get(ZitoMediaWorkflowModel.PROP_CUSTOMER_CATEGORY) == null
                            || !filterValue.equals(taskProperties.get(ZitoMediaWorkflowModel.PROP_CUSTOMER_CATEGORY).toString())) {
                        result = false;
                        break;
                    }
                } else if (key.equals(PARAM_WF_NAME)) {
                    String wfName = task.getPath().getInstance().getDefinition().getName();
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Workflow name is %s", wfName));
                    }
                    if (!filterValue.equals(wfName)) {
                        result = false;
                        break;
                    }
                } else if (key.equals(PARAM_CUSTOM_EXCLUDE)) {
                    ExcludeFilter excludeFilter = (ExcludeFilter) filterValue;
                    String type = task.getDefinition().getMetadata().getName().toPrefixString(this.namespaceService);
                    if (excludeFilter.isMatch(type)) {
                        result = false;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Comparator to sort workflow tasks by due date in ascending order.
     */
    class WorkflowTaskDueAscComparator implements Comparator<WorkflowTask> {
        @Override
        public int compare(WorkflowTask o1, WorkflowTask o2) {
            Date date1 = (Date) o1.getProperties().get(WorkflowModel.PROP_DUE_DATE);
            Date date2 = (Date) o2.getProperties().get(WorkflowModel.PROP_DUE_DATE);

            long time1 = date1 == null ? Long.MAX_VALUE : date1.getTime();
            long time2 = date2 == null ? Long.MAX_VALUE : date2.getTime();

            long result = time1 - time2;

            return (result > 0) ? 1 : (result < 0 ? -1 : 0);
        }

    }

    /**
     * Comparator to sort workflow tasks by due date in descending order.
     */
    class WorkflowTaskDueDesComparator implements Comparator<WorkflowTask> {
        @Override
        public int compare(WorkflowTask o1, WorkflowTask o2) {
            Date date1 = (Date) o1.getProperties().get(WorkflowModel.PROP_DUE_DATE);
            Date date2 = (Date) o2.getProperties().get(WorkflowModel.PROP_DUE_DATE);

            long time1 = date1 == null ? Long.MAX_VALUE : date1.getTime();
            long time2 = date2 == null ? Long.MAX_VALUE : date2.getTime();

            long result = time2 - time1;

            return (result > 0) ? 1 : (result < 0 ? -1 : 0);
        }

    }

    /**
     * Comparator to sort workflow tasks by priority in descending order.
     */
    class WorkflowTaskPriorityDesComparator implements Comparator<WorkflowTask> {
        @Override
        public int compare(WorkflowTask o1, WorkflowTask o2) {
            Integer priority1 = (Integer) o1.getProperties().get(WorkflowModel.PROP_PRIORITY);
            Integer priority2 = (Integer) o2.getProperties().get(WorkflowModel.PROP_PRIORITY);

            int result = priority1 - priority2;

            return (result > 0) ? 1 : (result < 0 ? -1 : 0);
        }

    }

    /**
     * Comparator to sort workflow tasks by priority in ascending order.
     */
    class WorkflowTaskPriorityAscComparator implements Comparator<WorkflowTask> {
        @Override
        public int compare(WorkflowTask o1, WorkflowTask o2) {
            Integer priority1 = (Integer) o1.getProperties().get(WorkflowModel.PROP_PRIORITY);
            Integer priority2 = (Integer) o2.getProperties().get(WorkflowModel.PROP_PRIORITY);

            int result = priority2 - priority1;

            return (result > 0) ? 1 : (result < 0 ? -1 : 0);
        }

    }

    /**
     * Comparator to sort workflow tasks by name in descending order.
     */
    class WorkflowTaskNameDesComparator implements Comparator<WorkflowTask> {
        @Override
        public int compare(WorkflowTask o1, WorkflowTask o2) {
            String name1 = o1.getName();
            String name2 = o2.getName();

            int result = name1.compareTo(name2);

            return (result > 0) ? 1 : (result < 0 ? -1 : 0);
        }

    }

    /**
     * Comparator to sort workflow tasks by name in ascending order.
     */
    class WorkflowTaskNameAscComparator implements Comparator<WorkflowTask> {
        @Override
        public int compare(WorkflowTask o1, WorkflowTask o2) {
            String name1 = o1.getName();
            String name2 = o2.getName();

            int result = name2.compareTo(name1);

            return (result > 0) ? 1 : (result < 0 ? -1 : 0);
        }

    }
}
