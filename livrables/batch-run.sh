#!/bin/bash

CLIENT_JAR=CloudUnitCLI.jar

for fichier in scripts/*
do
	echo "#####################################################"
	echo "Testing : $fichier"
	echo "#####################################################"
	java -Djavax.net.ssl.trustStore=cloudunit-cert.jks -jar $CLIENT_JAR --cmdfile $fichier
done
