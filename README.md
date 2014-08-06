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