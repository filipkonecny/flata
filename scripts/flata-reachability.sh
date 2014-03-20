#!/bin/bash
export CLASSPATH=./flata.jar:./lib/nts.jar:./lib/antlr-3.3-complete.jar
java verimag.flata.Main -t-fullincl -t-merge-prec -acc-outgoing $1
