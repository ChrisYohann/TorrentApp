#!/bin/bash

SOURCEPATH=$(ls -d ./src/*/ ./src/*/*/ ./src/*/*/*/)
SOURCEPATH=$(echo $SOURCEPATH | tr " " :)
CLASSPATH="./bin:./lib/*"

java -Dfile.encoding=UTF-8 -classpath $CLASSPATH view.TorrentView 
