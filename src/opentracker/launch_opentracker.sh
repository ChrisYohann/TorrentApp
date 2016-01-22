#!/bin/bash

#Killing existing process
pkill opentracker
cd
cd opentracker
./opentracker -i $1 -p $2 -P $3 &
echo -e "Running opentracker on IPv4 address" $1 "and port" $2 "and" $3 
echo -e "Please check on http://"$1":"$2"/stats for details"
echo -e "Tracker address : http://"$1":"$2"/announce"
