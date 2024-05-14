#! /bin/sh
#export LD_LIBRARY_PATH=/usr/local/lib:/usr/local/lib/jni
export CLASSPATH=./flata.jar:./lib/*:$CLASSPATH
java verimag.flata.Main -term -t-merge-prec -t-fullincl $1

