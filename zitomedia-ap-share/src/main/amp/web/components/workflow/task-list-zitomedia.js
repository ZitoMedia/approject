/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * ZitoMedia TaskList component.
 *
 * @namespace Alfresco
 * @class Alfresco.ZitoMediaTaskList
 */
(function () {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML,
        $siteURL = Alfresco.util.siteURL;

    /**
     * DocumentList constructor.
     *
     * @param htmlId {String} The HTML id of the parent element
     * @return {Alfresco.component.TaskList} The new DocumentList instance
     * @constructor
     */
    Alfresco.ZitoMediaTaskList = function (htmlId) {
        Alfresco.ZitoMediaTaskList.superclass.constructor.call(this, htmlId);
        return this;
    };

    YAHOO.extend(Alfresco.ZitoMediaTaskList, Alfresco.component.TaskList, {
        /**
         * Fired by YUI when parent element is available for scripting.
         * Initial History Manager event registration
         *
         * @method onReady
         */
        onReady: function DL_onReady() {
            var url = Alfresco.constants.PROXY_URI + "api/zitomedia-task-instances?authority=" + encodeURIComponent(Alfresco.constants.USERNAME) +
                "&properties=" + ["bpm_priority", "bpm_status", "bpm_dueDate", "bpm_description"].join(",") +
                "&exclude=" + this.options.hiddenTaskTypes.join(",");
            this.widgets.pagingDataTable = new Alfresco.util.DataTable(
                {
                    dataTable: {
                        container: this.id + "-tasks",
                        columnDefinitions: [
                            { key: "id", sortable: false, formatter: this.bind(this.renderCellIcons), width: 40 },
                            { key: "title", sortable: false, formatter: this.bind(this.renderCellTaskInfo) },
                            { key: "name", sortable: false, formatter: this.bind(this.renderCellActions), width: 200 }
                        ],
                        config: {
                            MSG_EMPTY: this.msg("message.noTasks")
                        }
                    },
                    dataSource: {
                        url: url,
                        defaultFilter: {
                            filterId: "workflows.active"
                        },
                        filterResolver: this.bind(function (filter) {
                            // Reuse method form WorkflowActions
                            var filterParamters = this.createFilterURLParameters(filter, this.options.filterParameters);

                            if (filterParamters != "") {
                                filterParamters += "&";
                            }

                            if (Alfresco.SORT_MENU_BAR_PARAMETERS) {
                                filterParamters += Alfresco.SORT_MENU_BAR_PARAMETERS;
                            }
                            return filterParamters;
                        })
                    },
                    paginator: {
                        config: {
                            containers: [this.id + "-paginator"],
                            rowsPerPage: this.options.maxItems
                        }
                    }
                });

            YAHOO.Bubbling.on("changeSorter", this.onChangeSorter, this);
        },

        onChangeSorter: function DL_onChangeSorter(layer, args) {
            var parameters = this.substituteParameters(args[1].parameters, {});
            this.widgets.pagingDataTable.loadDataTable(parameters);
        }
    });
})();
