connect -login johndoe -password abc2015

create-app -name tomcat71 -type tomcat-7
use tomcat71

deploy -path archives/helloworld.war -openBrowser false

disconnect


