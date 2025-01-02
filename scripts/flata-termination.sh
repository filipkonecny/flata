#! /bin/sh
#export LD_LIBRARY_PATH=/usr/local/lib:/usr/local/lib/jni
export CLASSPATH=./flata.jar:./lib/*:$CLASSPATH
if [ -z "$2" ]
then
    java verimag.flata.Main -term -t-merge-prec -t-fullincl $1
else
    java verimag.flata.Main -term -t-merge-prec -t-fullincl -solver $2 $1
fi

