#!/bin/bash

echo "Cleaning Java Classes..."

rm -r ./bin/*

SOURCEPATH=$(ls -d ./src/*/ ./src/*/*/ ./src/*/*/*/)
SOURCEPATH=$(echo $SOURCEPATH | tr " " :)
CLASSPATH="./bin:./lib/*"
OUTDIR="./bin"
SOURCEFILES=$(ls ./src/*/*.java ./src/*/*/*.java ./src/*/*/*/*.java)

javac -d $OUTDIR -classpath $CLASSPATH -sourcepath $SOURCEPATH $SOURCEFILES
