#! /bin/sh
#export LD_LIBRARY_PATH=/usr/local/lib:/usr/local/lib/jni
export CLASSPATH=./lib/flata.jar:./lib/nts.jar:./lib/antlr-3.3-complete.jar
java verimag.main.Calculator $1 -noGLPK

