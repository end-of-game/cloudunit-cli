connect -login johndoe -password abc2015

create-app -name tomcat83 -type tomcat-8
use tomcat83

change-jvm-memory -size 1024
add-module -name mongo-2-6

deploy -path archives/mongo.war -openBrowser false

disconnect


