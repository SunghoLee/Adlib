#!/bin/bash

for file in ../AdSDKs/adplatforms/*.jar; do
   jar="${file##*/}";
   input="${file%.*}";
   output="${name%.*}";
   java -Xmx8G -jar Adlib.jar wala.properties $file "$input"_input > "$output"_log 
done 
