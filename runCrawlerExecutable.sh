#!/bin/bash
if  [ -z $1 ] || [ -z $2 ] || [ -z $3 ] || [ -z $4 ] ;then
    echo "runCrawlerExecutable.sh <Tweets> <Size of Files> <outputdir> <languages>"
    exit
fi

java -jar crawler.jar $1 $2 $3 $4

