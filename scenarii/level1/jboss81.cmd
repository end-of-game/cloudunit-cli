connect -login johndoe -password abc2015

create-app -name jboss81 -type jboss-8
use jboss81	

deploy -path archives/helloworld.war -openBrowser false 

disconnect
