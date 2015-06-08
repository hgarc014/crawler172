#!/bin/bash

APPNAME="TweetSearcher"
TOMCATFOLDER="/var/lib/tomcat7"
LUCENEFOLDER="indexes/index"

zip -r $APPNAME.zip favicon.ico tweetsearcher TweetSearcher.css TweetSearcher.html WEB-INF
mv "$APPNAME.zip" "$APPNAME.war"

sudo rm -rf "$TOMCATFOLDER/webpps/$APPNAME" "$TOMCATFOLDER/webpps/$APPNAME.war"
sudo rm -rf "$TOMCATFOLDER/$LUCENEFOLDER"
sudo cp -r $LUCENEFOLDER "$TOMCATFOLDER/indexes"
sudo mv "$APPNAME.war" "$TOMCATFOLDER/webapps"

