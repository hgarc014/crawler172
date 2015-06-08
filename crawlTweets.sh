#!/bin/bash

if  [ -z $1 ] || [ -z $2 ] || [ -z $3 ] || [ -z $4 ] ;then
    echo "./runCrawlerExecutable.sh <Tweets> <File Sizes (mb)> <Threads> <outputdir>"
    echo "OR..."
    echo "./runCrawlerExecutable.sh <Tweets> <File Sizes (mb)> <Threads> <outputdir> <save Every # Tweets>"
    exit
fi


if [ -d "$4" ];then
    START=$(date +%s)
    java -jar jars/crawler.jar $1 $2 $3 $4 $5

    END=$(date +%s.%N)
    dt=$(echo "$END - $START" | bc)
    dd=$(echo "$dt/86400" | bc)
    dt2=$(echo "$dt-86400*$dd" | bc)
    dh=$(echo "$dt2/3600" | bc)
    dt3=$(echo "$dt2-3600*$dh" | bc)
    dm=$(echo "$dt3/60" | bc)
    ds=$(echo "$dt3-60*$dm" | bc)

    printf "Total runtime: %d:%02d:%02d:%02.4f\n" $dd $dh $dm $ds
else
    echo "seems you provided an invalid directory"
fi
