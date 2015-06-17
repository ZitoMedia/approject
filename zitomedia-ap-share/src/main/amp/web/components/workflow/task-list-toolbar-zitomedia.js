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
 * ZitoMedia task list toolbar component.
 *
 * @namespace Alfresco
 * @class Alfresco.component.TaskListToolbarZitoMedia
 */
(function () {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;
    Selector = YAHOO.util.Selector;

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
    Alfresco.component.TaskListToolbarZitoMedia = function (htmlId) {
        Alfresco.component.TaskListToolbarZitoMedia.superclass.constructor.call(this, htmlId);
        return this;
    };

    YAHOO.extend(Alfresco.component.TaskListToolbarZitoMedia, Alfresco.component.TaskListToolbar, {

        options: {
            sorts: {}
        },
        /**
         * Fired by YUI when parent element is available for scripting.
         * Initial History Manager event registration
         *
         * @method onReady
         */
        onReady: function WLTZM_onReady() {
            this.widgets.startWorkflowButton = Alfresco.util.createYUIButton(this, "startWorkflow-button", this.onStartWorkflowButtonClick, {});
            Dom.removeClass(Selector.query(".hidden", this.id + "-body", true), "hidden");

            // Create sort menu
            this.widgets.sortMenuButton = Alfresco.util.createYUIButton(this, "sort", this.onSortSelected,
                {
                    type: "menu",
                    menu: "sort-menu",
                    lazyloadmenu: false
                });
            var sort = "dueDes";
            this.widgets.sortMenuButton.set("label", this.msg("sort." + sort) + " " + Alfresco.constants.MENU_ARROW_SYMBOL);
            this.widgets.sortMenuButton.value = sort;

            // Display the toolbar now that we have selected the filter
            Dom.removeClass(Selector.query(".right div", this.id, true), "hidden");

            // Store the menu parameters
            Alfresco.SORT_MENU_BAR_PARAMETERS = this.options.sorts[sort];
        },

        /**
         * Reloads the list with the new filter and updates the filter menu button's label
         *
         * @param p_sType {string} The event
         * @param p_aArgs {array} Event arguments
         */
        onSortSelected: function WLTZM_onSortSelected(p_sType, p_aArgs) {
            var menuItem = p_aArgs[1];

            if (menuItem) {
                this.widgets.sortMenuButton.set("label", menuItem.cfg.getProperty("text") + " " + Alfresco.constants.MENU_ARROW_SYMBOL);
                this.widgets.sortMenuButton.value = menuItem.value;
                // Store the menu parameters
                Alfresco.SORT_MENU_BAR_PARAMETERS = this.options.sorts[menuItem.value];
                YAHOO.Bubbling.fire("changeSorter", {
                    "sortedBy" : menuItem.value,
                    "parameters" : this.options.sorts[menuItem.value]
                });
            }
        }
    });
})();
