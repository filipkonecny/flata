package verimag.flata;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import nts.parser.NTS;

import verimag.flata.automata.ca.CA;
import verimag.flata.automata.cg.CG;
import verimag.flata.common.CR;
import verimag.flata.presburger.CompositeRel;


public class View {

	public static void main(String[] args) throws IOException {
		
		// TODO: remove
		CR.launchYices();

		CR.initFLataJavaSMT();
		
		//quick fix
		CompositeRel.onlyLin = true;
		
		File inputFile = new File(args[0]);
		
		NTS nts = verimag.flata.Main.parseNTS(inputFile);
		CG cg = verimag.flata.Main.nts2cg(nts);
		
		//CA ca = CAs.createAutomaton(inputFile);
		CA ca = cg.mainProc();
		ca.bu_copy();
		
		//ca.bu();
		
		boolean printT = (args.length<=2 || !args[2].equals("false"));
		
		view(ca, args[1], printT);
		
		// TODO: remove
		CR.terminateYices();
		
	}	
	
	public static void view(CA ca, String fname) {
		view(ca,fname, true);
	}
	public static void view(CA ca, String fname, boolean printT) {
		File fDot = new File(fname);
		try {
		FileWriter w = new java.io.FileWriter(fDot);
		if (!printT)
			ca.unsetDotTrans();
		w.append(ca.toDotLang(ca.name()));
		if (!printT)
			ca.setDotTrans();
		w.flush();
		w.close();
		} catch (IOException e) {
			System.err.println("error: IO problems");
		}
	}
	
}
