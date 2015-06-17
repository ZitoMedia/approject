var myConfig = new XML(config.script),
    sorts = [],
    sortMap = {};

for each(var xmlSort in myConfig..sort) {
    sorts.push({
            type: xmlSort.@type.toString(),
        parameters: xmlSort.@parameters.toString()
    });
    sortMap[xmlSort.@type.toString()] = xmlSort.@parameters.toString();
}
model.sorts = sorts;

function main() {
    // Widget instantiation metadata...
    var taskListToolbar = {
        id: "TaskListToolbar",
        name: "Alfresco.component.TaskListToolbarZitoMedia",
        options: {
            sorts: sortMap
        }
    };
    model.widgets = [taskListToolbar];
}

main();

