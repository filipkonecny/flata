package verimag.flata;

import java.io.File;

import nts.parser.NTS;

import verimag.flata.automata.ca.CA;
import verimag.flata.automata.cg.CG;
import verimag.flata.common.CR;

public class Termination {
	
	public static void main(String[] args) {
		
		File inputFile = CR.processParameters(args);
		
		Calculator.initActions();
		
		CA.TERM = true;
		//CG cg = verimag.flata.Main.parseCG(inputFile);
		NTS nts = verimag.flata.Main.parseNTS(inputFile);
		CG cg = verimag.flata.Main.nts2cg(nts);
		cg.termination2();
		//cg.termination();
		
		Calculator.finalActions();
	}
}
