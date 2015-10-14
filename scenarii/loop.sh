
#!/bin/bash

NBLOOPS=$1
DOMAIN=$2

CPT=1

rm -f resultats*

while [ $CPT -le $NBLOOPS ]
do
  echo "---------------------------------------"
  echo "------------- $CPT / $NBLOOPS boucles"
  echo "---------------------------------------" 
  bash ci-tests-1.sh $DOMAIN
  bash ci-tests-2.sh $DOMAIN
  bash ci-tests-3.sh $DOMAIN
  bash ci-tests-4.sh $DOMAIN 
  ((CPT++))
done
