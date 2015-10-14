connect -login johndoe -password abc2015

create-app -name ref64 -type tomcat-6
use ref64

deploy -path archives/helloworld.war -openBrowser false

create-snapshot -applicationName ref64 -tag snap-ref64

rm-app -name ref64 -scriptUsage

clone -applicationName tomcat64 -tag snap-ref64

disconnect


