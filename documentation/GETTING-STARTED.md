#Getting started : CloudUnit Command Line interface

This guide is relevant if you want to use CloudUnit 1.0 through the command line interface.

##Requirements

- Available started CloudUnit manager 1.0
- JRE 1.7 +

## Gettings started

Download the zip archive [here](https://github.com/Treeptik/CloudUnit/releases/download/1.0/cloudunitcli.zip). Unzip the archive in a user directory.
If you use Linux/Mac, run the cloudunit.sh file to start the application. If you use Windows, you have a cloudunit.bat file.

## Help

Once the client has run, you can access to the help command. This will be useful if you want to display all available commands

```bash
cloudunit> help
```

## Connection to the CloudUnit manager

You must have a running CloudUnit manager. Refer to the following guide to start a CloudUnit Platform.
Then, run the connect command with your credentials and the manager host location as following (by default, the host is set to localhost) :

```bash
cloudunit> connect --login johndoe --host https://admin.cloudunit.dev
Enter your password : 
***********
Trying to connect to https://admin.cloudunit.dev
Connection established
```
Now you are connected. You can use all available commands.

## Create a new app

To create an application, you must set its name and the type of java web server you want to use. For the moment, four servers are available : tomcat-6, tomcat-7, tomcat-8, jboss-8

```bash
cloudunit> create-app --name myapp --type tomcat-7
Your application myapp is currently being installed
```
## Display application informations

Once your app has been created, you can display informations about its state :

```bash
cloudunit-myapp> informations

 GENERAL 

+----------------+--------+----------------+-----------+------+------------+
|APPLICATION NAME|AUTHOR  |STARTING DATE   |SERVER TYPE|STATUS|JAVA VERSION|
+----------------+--------+----------------+-----------+------+------------+
|myapp           |Doe John|2015-10-14 10:22|TOMCAT-7   |START |jdk1.7.0_55 |
+----------------+--------+----------------+-----------+------+------------+

 GIT ADDRESS 

+----+---------------------------------------------------------+
|TYPE|REMOTE ADDRESS                                           |
+----+---------------------------------------------------------+
|GIT |ssh://johndoe@myapp.cloudunit.dev:2000/cloudunit/git/.git|
+----+---------------------------------------------------------+

 SERVER INFORMATION 

+--------+-------------+--------+------+--------+------+---------------------------------------------------------------+
|TYPE    |ADDRESS      |SSH PORT|STATUS|JVM OPTS|MEMORY|MANAGER LOCATION                                               |
+--------+-------------+--------+------+--------+------+---------------------------------------------------------------+
|TOMCAT-7|cloudunit.dev|32768   |START |NONE    |512   |http://manager-myapp-johndoe-admin.cloudunit.dev/manager/html? |
+--------+-------------+--------+------+--------+------+---------------------------------------------------------------+

 MODULES INFORMATION 

No modules found!

```
## Select the app context

To execute a command on an app, you can set the app name to the command or execute it in the app context. To change the app context, you can send the "use" command as following :

```bash
cloudunit> use myapp
Current application myapp

```

## Manage application lifecycle

There are basic command to manage your app lifecycle into CloudUnit platform :

- Stop the application (it won't use resources anymore) :

```bash
cloudunit-myapp> stop
Your application myapp is currently being stopped

```

- Start the application :

```bash
cloudunit-myapp> start
Your application myapp is currently being started

```




## Add a module

You can add a module to your application, for example, a database. For the moment, three modules are available : mysql-5-5, postgresql-9-3, mongo-2-6

```bash
cloudunit-myapp> add-module --name mysql-5-5
Your module mysql-5-5 is currently being added to your application myapp

```

Now, a module is available when you run informations command. You can use these informations to edit your datasource configuration.

```bash

 MODULES INFORMATION 

+-----------+---------------------------------------------------------------+
|MODULE NAME|mysql-5-5-1                                                    |
+-----------+---------------------------------------------------------------+
|TYPE       |mysql-5-5                                                      |
+-----------+---------------------------------------------------------------+
|DOMAIN NAME|johndoe-myapp-mysql-5-5-1.mysql-5-5.cloud.unit                 |
+-----------+---------------------------------------------------------------+
|PORT       |3306                                                           |
+-----------+---------------------------------------------------------------+
|USERNAME   |adminom8cdo5u                                                  |
+-----------+---------------------------------------------------------------+
|PASSWORD   |p3v19ir0                                                       |
+-----------+---------------------------------------------------------------+
|DATABASE   |myapp                                                          |
+-----------+---------------------------------------------------------------+
|MANAGER    |http://phpmyadmin1-myapp-johndoe-admin.cloudunit.dev/phpmyadmin|
+-----------+---------------------------------------------------------------+

```

## Deploy an application

Now, we can deploy a application based on tomcat that use a mysql datasource. By default, the CLI will open the webpage in your default browser.

```bash

cloudunit-myapp> deploy --path ~/pizzashop-mysql.war
War deployed - Access on http://myapp-johndoe-admin.cloudunit.dev

```
## Change JVM configuration

Three commands can be used to change JVM configuration : 

- Change your Java release (two releases are available : jdk1.7.0_55 and jdk1.8.0_25). By default, the application has been created with the Oracle JKD 1.7.0_55.

```bash
cloudunit-myapp> change-java-version --javaVersion jdk1.8.0_25
Your java version has been successfully changed

```

- Change your JVM memory. The available sizes (in MB) are : 512 (by default), 1024, 2048, 3072

```bash
cloudunit-myapp> change-jvm-memory --size 1024
Change memory on myapp successful

```

- Inject Java properties in your JVM :

```bash
cloudunit-myapp> add-jvm-option "-Dkey=value"
Add java options to myapp application successfully

```

## Snapshot your app and clone it

You can easily save your app state and clone all its environment.

- First, run the snapshot command to save the app state with a tag :

```bash
cloudunit-myapp> create-snapshot --tag save1
A new snapshot called save1 was successfully created.

```

- You can show the list of available tags :

```bash
cloudunit-myapp> list-snapshot
+-----+----------------+------------------+
|TAG  |DATE            |APPLICATION SOURCE|
+-----+----------------+------------------+
|save1|2015-10-14 11:23|myapp             |
+-----+----------------+------------------+

```
- Now, you can clone your app from this tag :

```bash
cloudunit-myapp> clone --tag save1 --applicationName mynewapp
Your application mynewapp was successfully created.

```

## Aliases

You can manage alias to access to your app :

- Create a new alias :

```bash
cloudunit-myapp> add-alias --alias mywebsite
Your alias mywebsite has been successfully added to myapp

```

- List all available aliases for your app :

```bash
cloudunit-myapp> list-aliases 
+---------------+
|CURRENT ALIASES|
+---------------+
|mywebsite      |
+---------------+

```

- Remove an alias

```bash
cloudunit-myapp> rm-alias --alias mywebsite
This alias has successful been deleted

```






