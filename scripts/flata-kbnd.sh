#!/bin/bash

java -cp flata.jar:nts.jar:antlr-3.3-complete.jar verimag.flata.recur_bounded.RecurToKBounded $1 $2
