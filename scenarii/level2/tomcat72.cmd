connect -login johndoe -password abc2015

create-app -name tomcat72 -type tomcat-7
use tomcat72

add-module -name mysql-5-5
change-jvm-memory -size 1024
add-jvm-option "-Dnicolas=muller"

deploy -path archives/pizzas-mysql.war -openBrowser false

disconnect

