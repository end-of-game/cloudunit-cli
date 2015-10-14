connect -login johndoe -password abc2015

create-app -name tomcat81 -type tomcat-8
use tomcat81

deploy -path archives/helloworld.war -openBrowser false

disconnect


