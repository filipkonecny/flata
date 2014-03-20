#! /bin/sh
#export LD_LIBRARY_PATH=/usr/local/lib:/usr/local/lib/jni
export CLASSPATH=./flata.jar:./lib/nts.jar:./lib/antlr-3.3-complete.jar
java verimag.flata.Calculator $1 -noGLPK

