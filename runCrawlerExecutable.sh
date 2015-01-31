#!/bin/bash

if  [ -z $1 ] || [ -z $2 ] || [ -z $3 ] ;then
    echo "./runCrawlerExecutable.sh <Tweets> <File Sizes (mb)> <outputdir>"
    exit
fi


if [ -d "$3" ];then
    java -jar crawler.jar $1 $2 $3
else
    echo "seems you provided an invalid directory"
fi

