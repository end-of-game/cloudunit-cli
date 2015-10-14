// test all memory changes
connect --login johndoe --password abc2015
use --name test1
change-jvm-memory --size 1024
change-jvm-memory --size 2048
change-jvm-memory --size 3072
change-jvm-memory --size 512