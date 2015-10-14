#!/bin/bash

export DIR=target
export CLIENT_JAR=CloudUnitCLI.jar
export SCRIPT_DIRECTORY=../src/test/resources/script-test

echo "starting cloudunit test"
cd $DIR

##### TOMCAT + MYSQL ########

echo "TEST CREATE TOMCAT"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-create-tomcat7.cu
echo "TEST ADD MYSQL"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-add-mysql-tomcat7.cu

echo "TEST STOP TOMCAT"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-stop-tomcat7.cu

echo "TEST START TOMCAT"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-start-tomcat7.cu

echo "TEST MEMORY TOMCAT"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-add-memory-tomcat7.cu

#Start deployment on TOMCAT 7

cd ../src/test/resources/projects_for_deployment
tar xf pizzaiolo.tar.gz
cd ../../../../target

echo "TEST DEPLOY TOMCAT"
cd ../src/test/resources/projects_for_deployment/pizzaiolo

mvn clean package
cd ../../../../../target
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-deploy-tomcat7.cu
sleep 60

wget http://test1-johndoe-anonymous.cloudunit.dev

echo `cat index.html | grep 'Pizza'`
rm index.html

rm -rf ../src/test/resources/projects_for_deployment/pizzaiolo
echo "TEST LIST APP"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-list-app.cu
echo "TEST SNAPSHOT"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-snapshot-tomcat7.cu
echo "REMOVE MYSQL FROM TOMCAT APP"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-remove-mysql-tomcat7.cu
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-add-mysql-tomcat7.cu
echo "TEST REMOVE APP TOMCAT WITH MYSQL"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-remove-tomcat7.cu

##### JBOSS + MYSQL ########
echo "TEST CREATE JBOSS"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-create-jboss7.cu
echo "TEST ADD MYSQL"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-add-mysql-jboss7.cu

echo "TEST STOP JBOSS"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-stop-jboss7.cu

echo "TEST START JBOSS"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-start-jboss7.cu

echo "TEST MEMORY JBOSS"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-add-memory-jboss7.cu

#Start deployment on JBOSS 7

cd ../src/test/resources/projects_for_deployment
tar xf wicket-db-ear-jboss.tar.gz

cd ../../../../target

cd ../src/test/resources/projects_for_deployment/wicket-db-ear-jboss

mvn clean package
cd ../../../../../target
echo "TEST DEPLOY JBOSS"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-deploy-jboss7.cu
sleep 60

wget http://test2-johndoe-anonymous.cloudunit.dev/jboss-wicket-ear-war/

echo `cat index.html | grep 'Wicket'`
rm index.html

rm -rf ../src/test/resources/projects_for_deployment/wicket-db-ear-jboss

echo "REMOVE MYSQL FROM JBOSS APP"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-remove-mysql-jboss7.cu
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-add-mysql-jboss7.cu
echo "TEST REMOVE APP JBOSS WITH MYSQL"
java -jar $CLIENT_JAR --cmdfile $SCRIPT_DIRECTORY/test-remove-jboss7.cu
