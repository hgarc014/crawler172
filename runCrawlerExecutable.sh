#!/bin/bash

if  [ -z $1 ] || [ -z $2 ] || [ -z $3 ] || [ -z $4 ] ;then
    echo "./runCrawlerExecutable.sh <Tweets> <File Sizes (mb)> <Threads> <outputdir>"
    exit
fi


if [ -d "$4" ];then
    java -jar crawler.jar $1 $2 $3 $4
else
    echo "seems you provided an invalid directory"
fi

