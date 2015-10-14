connect -login johndoe -password abc2015

create-app -name ref84 -type tomcat-8
use ref84

deploy -path archives/helloworld.war -openBrowser false

create-snapshot -applicationName ref84 -tag snap-ref84

rm-app -name ref84 -scriptUsage

clone -applicationName tomcat84 -tag snap-ref84

disconnect


