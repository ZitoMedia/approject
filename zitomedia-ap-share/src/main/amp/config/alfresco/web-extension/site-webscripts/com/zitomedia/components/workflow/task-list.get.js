// Find the default Search widget and replace it with the custom widget
for (var i=0; i<model.widgets.length; i++)
{
  if (model.widgets[i].id == "TaskList")
  {
    model.widgets[i].name = "Alfresco.ZitoMediaTaskList";
  }
}


