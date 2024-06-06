package verimag.flata;

import java.io.*;

import verimag.flata.common.CR;
import verimag.flata.common.Parameters;
import verimag.flata.presburger.VariablePool;;

public class Calculator {
	
	protected static void initActions() {
		initActions(true);
	}
	protected static void initActions(boolean glpk) {
		CR.initFLataJavaSMT();
		if (glpk)
			CR.launchGLPK();
		
		Parameters.initActions();
	}

	protected static void finalActions() {
		//Parameters.finalActions();
	}
	
	
	public static void main(String[] args) {
		boolean noGlpk = (args.length >= 2 && args[1].equals("-noGLPK"));
		initActions(!noGlpk);
		
		VariablePool pool = VariablePool.createEmptyPoolNoDeclar();
		verimag.flata.parsers.MParser.calc(new File(args[0]), null, pool);
		
		finalActions();
	}
	
}
