package verimag.flata;
import java.io.File;

import nts.parser.NTS;

import verimag.flata.automata.ca.CA;
import verimag.flata.automata.cg.CG;
import verimag.flata.common.CR;
import verimag.flata.common.Parameters;


public class ConstProp {

	public static void main(String[] args) {

		int i = 0;
		if (args[0].equals(Parameters.IMPLACT)) {
			Parameters.processParameter(args, 0, 1);
			i++;
		}
		
		File inputFile = new File(args[i+0]);

		CR.initFLataJavaSMT();
		
		//CA ca = CAs.createAutomaton(inputFile);
		NTS nts = Main.parseNTS(inputFile);
		CG cg = Main.nts2cg(nts);
		if (cg.procedures().size() > 1) {
			System.err.println("Multiple procedures not supported");
			System.exit(1);
		}
		CA ca = cg.mainProc();
		CR.NO_OUTPUT = true;
		
		System.out.println("Constant propagation.");
		ca.printStatTrans();
		ca.constantPropagation();
		ca.printStatTrans();
		System.out.println(ca);
		
		System.out.println("Nonloop states elimination.");
		//ca_cp.printStatTrans();
		//ca_cp.reduce_nonloop_state();
		//ca_cp.printStatTrans();
		
		
		if (args.length>2)
			CR.writeToFile(args[i+1], ca.toString());
		
		//ca.reduce();
	}
	
}
