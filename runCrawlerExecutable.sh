#!/bin/bash

if  [ -z $1 ] || [ -z $2 ] || [ -z $3 ] ;then
    if  [ ! -z "$1" ] && [ "$1"  == "--help" ] ;then
        echo "add list of languages..."
        exit
    fi
    echo "./runCrawlerExecutable.sh <Tweets> <File Sizes (mb)> <outputdir>"
    echo "./runCrawlerExecutable.sh --help"
    exit
fi


if [ -d "$3" ];then
    java -jar crawler.jar $1 $2 $3
else
    echo "seems you provided an invalid directory"
fi

