#!/bin/bash
PROJECT_DIR=/home/issta19-ae/Documents/Adlib
ADSDKS_DIR=/home/issta19-ae/Documents/AdSDKs/adplatforms

java -Xmx8G -jar $PROJECT_DIR/Adlib.jar $PROJECT_DIR/wala.properties $ADSDKS_DIR/mm.jar $ADSDKS_DIR/mm_input
