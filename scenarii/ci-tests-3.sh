#!/bin/bash

DOMAIN=$1
PATTERN=nicolas

if [ -z "$1"  ]
  then
    echo " "
    echo "Syntaxe : ./ci-tests-3 <domaine>"
    echo "          ./ci-tests-3 cloudunit.dev "
    echo " "
    exit -1
fi

echo "DOMAIN:"$DOMAIN


JAR=../target/CloudUnitCLI.jar

for fichier in level3/*3.cmd
do
    echo "#####################################################"
    echo "Testing : $fichier"
    echo "#####################################################"
    java -jar $JAR --cmdfile $fichier

    counter=0
    STATUS="false"
    APPLICATION=`echo ${fichier} | cut -d'/' -f 2`
    APPLICATION=`echo $APPLICATION | cut -d'.' -f 1`
    CURL_COMMAND="curl --data \"name=nicolas&country=france\" http://${APPLICATION}-usertest3-user.$DOMAIN/addPerson | grep $PATTERN"
    RETURN=-1
	
	until [ "$RETURN" -eq "0" ] || [ "$counter" -eq "100" ] ;
	do
		echo -n -e "\nWaiting for deployment : " $counter " / 100 ";
		
		eval $CURL_COMMAND
		RETURN=$?	
        	
		if [ "$RETURN" -eq "0" ]; 
		then
 		  STATUS="true"		  
		fi
        	
		sleep 1
	    ((counter++))	
	done

        echo " " >> resultats3.log
        echo $CURL_COMMAND $RETURN >> resultats3.log

	if [ "$STATUS" = "true" ]; 
	then
	  	echo "Success" >> resultats3.log
	else
        echo "Fail" >> resultats3.log
	fi
 
done

## Suppression de l'ensemble des applications Level2
java -jar $JAR --cmdfile level3/delete.clean


