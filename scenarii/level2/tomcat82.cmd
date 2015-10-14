connect -login johndoe -password abc2015

create-app -name tomcat82 -type tomcat-8
use tomcat82

add-module -name mysql-5-5
change-jvm-memory -size 1024
add-jvm-option "-Dnicolas=muller"
change-java-version -javaVersion 8

deploy -path archives/pizzas-mysql.war -openBrowser false

disconnect
