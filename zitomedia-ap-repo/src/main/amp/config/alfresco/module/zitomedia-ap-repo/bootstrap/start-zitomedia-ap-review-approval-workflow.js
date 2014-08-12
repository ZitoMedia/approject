var workflow = actions.create("start-workflow");
workflow.parameters.workflowName = "activiti$ZitomediaMultiLevelReview";
workflow.parameters["bpm:workflowDescription"] = "Please review and approve invoice " + document.name + ".";
//workflow.parameters["bpm:groupAssignee"] = assigneeGroup;
//var futureDate = new Date();
//futureDate.setDate(futureDate.getDate() + 7);
//workflow.parameters["bpm:workflowDueDate"] = futureDate;
workflow.execute(document);