#!/bin/bash

DOMAIN=$1

echo "DOMAIN:"$DOMAIN

if [ -z "$1"  ];
  then
    echo ""
    echo "Syntaxe : ./ci-tests-2.sh <domaine>" 
    echo "          ./ci-tests-2.sh cloudunit.dev"
    echo ""
    exit -1
fi


JAR=../target/CloudUnitCLI.jar

for fichier in level2/*2.cmd
do
    echo "#####################################################"
    echo "Testing : $fichier"
    echo "#####################################################"
    java -jar $JAR --cmdfile $fichier

    counter=0
    STATUS="false"
    APPLICATION=`echo ${fichier} | cut -d'/' -f 2`
    APPLICATION=`echo $APPLICATION | cut -d'.' -f 1`
    CURL_COMMAND1="curl http://${APPLICATION}-usertest2-user.$DOMAIN | grep 1024"
    CURL_COMMAND2="curl http://${APPLICATION}-usertest2-user.$DOMAIN | grep muller"
    RETURN1=-1
    RETURN2=-1
	
	until [ "$RETURN1" -eq "0" ] || [ "$counter" -eq "200" ] ;
	do
		echo -n -e "\nWaiting for deployment $counter / 200";
		
		eval $CURL_COMMAND1
		RETURN1=$?	
        
		eval $CURL_COMMAND2
		RETURN2=$?

		if [ "$RETURN1" -eq "0" ] && [ "$RETURN2" -eq "0" ]  ; 
		then
 		  STATUS="true"		  
		fi
        	
		sleep 1
	    ((counter++))	
	done
	
	echo " " >> resultats2.log
	echo $CURL_COMMAND1 $RETURN1 >> resultats2.log
	echo $CURL_COMMAND2 $RETURN2 >> resultats2.log

	if [ "$STATUS" = "true" ]; 
	then
	  	echo "Success" >> resultats2.log
	else
        echo "Fail" >> resultats2.log
	fi
 
done

## Suppression de l'ensemble des applications Level1

java -jar $JAR --cmdfile level2/delete.clean

