ZitoMedia AP Alfresco Project
=====

## Build ##

To build the project, simple run following maven command under the project root

```
mvn clean package -DskipTests=true
```

This should build two AMP files and two zip files under the sub project target folders.

## Run ##

## Embedded Alfresco

To run the Alfresco as embedded server, you need to run following maven command under zitomedia-ap-repo sub-project
 
```
mvn integration-test -Pamp-to-war 
``` 

and then run the following maven command under zitomedia-ap-share sub-project

```
mvn integration-test -Pamp-to-war -Dmaven.tomcat.port=8081 
``` 

## Standalone Alfresco 

To install modules directly to the expanded war folders of your local Alfresco instance, 
you will need to create a build.properties file under the project root. 

The properties file needs to provide the location of the local Alfresco instance such as

```
alfresco.root=/Users/drq/dev/alfresco/alfresco-5.0.a
```

## Setup ##

1. Create a new share site such as account-payable
2. Add Accounting Group and James Rigas to the site as Site Manager
3. Add all supervisors to the site as Site Consumer
4. Browse to the Document Library page of the site
5. Under the root folder, create a folder with name Invoices
6. Go the "Managing Permissions" page of the Invoices folder
7. Disable "Inherited Permissions" and then add Accounting Group and James Rigas as Manager for the folder
8. Under the Invoices folder, create following sub folders
    * Assigned
        * Under Assigned folder, create a sub-folder for each supervisor. The supervisor's name needs to
        be the folder name.
        * The corresponding supervisor of the folder also needs to be added as Collaborator of the folder.
    * Incoming
        * This will be folder that Accounting group users drop invoices and start the approval workflow
    * Processing
        * Use the "AP Invoices" space template to create it
    * Processed
        * Use the "AP Invoices" space template to create it.
    * Rejected
        * Use the "AP Invoices" space template to create it.
        * Add an additional sub-folder with name "Supervisor Rejected"
9. For Accounting group users and James Rigas user, they can customize their dashboard and replace "My Tasks" 
dashlet with "Final Approval Tasks" dashlet.        