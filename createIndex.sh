#!/bin/bash

if [ -z $1 ] || [ -z $2 ];then
    echo "./createIndex <indexDir> <TweetDir>"
    exit
fi 

java -jar jars/index.jar $1 $2

