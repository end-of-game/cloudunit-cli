connect -login johndoe -password abc2015

create-app -name jboss83 -type jboss-8
use jboss83

change-jvm-memory -size 1024
add-module -name mongo-2-6

deploy -path archives/mongo.war -openBrowser false

disconnect


