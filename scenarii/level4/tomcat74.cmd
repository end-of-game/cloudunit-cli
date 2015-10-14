connect -login johndoe -password abc2015

create-app -name ref74 -type tomcat-7
use ref74

deploy -path archives/helloworld.war -openBrowser false

create-snapshot -applicationName ref74 -tag snap-ref74

rm-app -name ref74 -scriptUsage

clone -applicationName tomcat74 -tag snap-ref74

disconnect


