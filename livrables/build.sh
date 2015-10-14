#!/bin/bash
cd ..
mvn clean package -DskipTests
cp target/CloudUnitCLI.jar livrables/CloudUnitCLI.jar
cd livrables
mkdir cloudunit-cli
cp -r scripts/ batch-run.* CloudUnitCLI.jar cloudunit.sh cloudunit.bat cloudunit-cert.jks cloudunit-cli/
zip -r cloudunit-cli.zip cloudunit-cli
