#!/bin/bash
PROJECT_DIR=/home/eshaj/Documents/repo/ADLib
ADSDKS_DIR=/home/eshaj/Documents/repo/AdSDKs/adplatforms

if [ "$3" = "alias" ]; then
  java -Xmx8G -jar $PROJECT_DIR/Adlib.jar -p $PROJECT_DIR/wala.properties -sdk $ADSDKS_DIR/$1.jar -init $ADSDKS_DIR/$1_input -s $2 -alias
else
  java -Xmx8G -jar $PROJECT_DIR/Adlib.jar -p $PROJECT_DIR/wala.properties -sdk $ADSDKS_DIR/$1.jar -init $ADSDKS_DIR/$1_input -s $2 
fi
