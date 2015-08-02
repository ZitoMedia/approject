package com.zitomedia.repo.action.executer;

import com.zitomedia.repo.model.ZitoMediaModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by drq on 7/26/15.
 */
public class ZitoMediaNotifierActionExecuter extends ActionExecuterAbstractBase {
    private static final Log logger = LogFactory.getLog(ZitoMediaNotifierActionExecuter.class);

    public static final String NAME = "zitomedia-notifier";
    public static final String PARAM_WORKFLOW_NAME = "workflow-name";
    public static final String PARAM_GROUP_NAME = "group-name";

    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected void executeImpl(Action action, NodeRef nodeRef) {
        if (logger.isDebugEnabled()) {
            logger.debug("ZitoMedia Notifier Action....");
        }

        WorkflowService workflowService = serviceRegistry.getWorkflowService();
        //PersonService personService = serviceRegistry.getPersonService();
        AuthorityService authorityService = serviceRegistry.getAuthorityService();
        NodeService nodeService = serviceRegistry.getNodeService();

        String workflowName = action.getParameterValue(PARAM_WORKFLOW_NAME) != null ? (String) action.getParameterValue(PARAM_GROUP_NAME) : "activiti$ZitoMediaRenewal";

        WorkflowDefinition wfDefinition = workflowService
                .getDefinitionByName(workflowName);
        if (wfDefinition == null) {
            logger.error("Failed to find workflow with name " + workflowName);
        } else {

            // Create a workflow node reference
            NodeRef workflowNodeRef = workflowService.createPackage(null);

            nodeService.addChild(workflowNodeRef, nodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "Payload for the workflow package"));

            Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();

            Date dueDate = new Date(System.currentTimeMillis());

            parameters.put(WorkflowModel.ASSOC_PACKAGE, workflowNodeRef);

            //NodeRef personNodeId = personService.getPerson("admin");

            String groupName = action.getParameterValue(PARAM_GROUP_NAME) != null ? (String) action.getParameterValue(PARAM_GROUP_NAME) : "GROUP_FA";
            NodeRef groupNodeId = authorityService.getAuthorityNodeRef(groupName);

            if (groupNodeId != null) {
                parameters.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, groupNodeId);
                parameters.put(WorkflowModel.PROP_DUE_DATE, dueDate);
                parameters.put(WorkflowModel.PROP_DESCRIPTION, "PROP_DESCRIPTION");
                parameters.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);

                Long now = new Date().getTime();
                Date firstNotificationDate = (Date) nodeService.getProperty(nodeRef, ZitoMediaModel.PROP_ZITOMEDIA_FIRST_NOTIFICATION_DATE);

                String description = "Contact the customer of " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                if (firstNotificationDate != null && firstNotificationDate.getTime() < now) {
                    description += "(First Notification is due)";
                } else {
                    Date secondNotificationDate = (Date) nodeService.getProperty(nodeRef, ZitoMediaModel.PROP_ZITOMEDIA_SECOND_NOTIFICATION_DATE);
                    if (secondNotificationDate != null && secondNotificationDate.getTime() < now) {
                        description += "(Second Notification is due)";
                    }
                }

                parameters.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, description);

                WorkflowPath wfPath = workflowService.startWorkflow(wfDefinition.getId(), parameters);

                if (logger.isDebugEnabled()) {
                    logger.debug("Started workflow " + wfPath.toString() + " and assigned to group " + groupName);
                }
            } else {
                logger.error("Failed to find group with name " + groupName);
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_WORKFLOW_NAME, DataTypeDefinition.TEXT,
                false, getParamDisplayLabel(PARAM_WORKFLOW_NAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_GROUP_NAME, DataTypeDefinition.TEXT,
                false, getParamDisplayLabel(PARAM_GROUP_NAME)));
    }
}
