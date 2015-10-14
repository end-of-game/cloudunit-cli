FOR /R scripts %%G IN (*.cu) DO java -Djavax.net.ssl.trustStore=cloudunit-cert.jks -jar CloudUnitCLI.jar --cmdfile %%G
