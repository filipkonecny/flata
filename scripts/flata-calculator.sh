#! /bin/sh
#export LD_LIBRARY_PATH=/usr/local/lib:/usr/local/lib/jni
export CLASSPATH=./flata.jar:./lib/*:$CLASSPATH
java verimag.flata.Calculator $1 -noGLPK

