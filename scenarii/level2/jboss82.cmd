connect -login johndoe -password abc2015

create-app -name jboss82 -type jboss-8
use jboss82

add-module -name mysql-5-5
change-jvm-memory -size 1024
add-jvm-option "-Dnicolas=muller"

deploy -path archives/pizzas-mysql.war -openBrowser false

disconnect

