connect -login johndoe -password abc2015

create-app -name tomcat73 -type tomcat-7
use tomcat73

change-jvm-memory -size 1024
add-module -name mongo-2-6

deploy -path archives/mongo.war -openBrowser false

disconnect


