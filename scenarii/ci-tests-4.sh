#!/bin/bash

DOMAIN=$1
PATTERN=Hello

if [ -z "$1"  ]
  then
    echo " "
    echo "Syntaxe : ./ci-tests-4.sh <domaine>"
    echo "          ./ci-tests-4.sh cloudunit.dev "
    echo " "
    exit -1
fi

echo "DOMAIN:"$DOMAIN


JAR=../target/CloudUnitCLI.jar

for fichier in level4/*4.cmd
do
    echo "#####################################################"
    echo "Testing : $fichier"
    echo "#####################################################"
    java -jar $JAR --cmdfile $fichier

    counter=0
    STATUS="false"
    APPLICATION=`echo ${fichier} | cut -d'/' -f 2`
    APPLICATION=`echo $APPLICATION | cut -d'.' -f 1`
    CURL_COMMAND="curl http://${APPLICATION}-usertest4-user.$DOMAIN | grep $PATTERN"
    RETURN=-1
	
	until [ "$RETURN" -eq "0" ] || [ "$counter" -eq "100" ] ;
	do
		echo -n -e "\nWaiting for deployment : " $counter " / 100 ";
	        echo ""	
		eval $CURL_COMMAND
		RETURN=$?	
        	
		if [ "$RETURN" -eq "0" ]; 
		then
 		  STATUS="true"		  
		fi
        	
		sleep 1
	    ((counter++))	
	done

        echo " " >> resultats4.log
        echo $CURL_COMMAND $RETURN >> resultats1.log

	if [ "$STATUS" = "true" ]; 
	then
	  	echo "Success" >> resultats4.log
	else
        echo "Fail" >> resultats4.log
	fi
 
done

## Suppression de l'ensemble des applications Level1
java -jar $JAR --cmdfile level4/delete.clean


