#!/bin/bash
export CLASSPATH=./flata.jar:./lib/*:$CLASSPATH
if [ -z "$2" ]
then
    java verimag.flata.Main -t-fullincl -t-merge-prec -acc-outgoing $1
else
    java verimag.flata.Main -t-fullincl -t-merge-prec -acc-outgoing -solver $2 $1
fi