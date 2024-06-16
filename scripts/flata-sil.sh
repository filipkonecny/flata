#!/bin/bash
export CLASSPATH=./flata.jar:./lib/*:$CLASSPATH
if [ -z "$2" ]
then
    java verimag.sil.Main $1
else
    java verimag.sil.Main -solver $2 $1
fi

