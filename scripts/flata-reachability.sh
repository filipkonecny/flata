#!/bin/bash
export CLASSPATH=./flata.jar:./lib/*:$CLASSPATH
java verimag.flata.Main -t-fullincl -t-merge-prec -acc-outgoing $1