#!/bin/bash

echo "[LEXER+PARSER]"
java -classpath ~/workspace/lib/antlr-3.3-complete.jar org.antlr.Tool Calc.g
echo "[TREEPARSER]"
java -classpath ~/workspace/lib/antlr-3.3-complete.jar org.antlr.Tool CalcT.g
