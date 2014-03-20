package verimag.flata.acceleration.zigzag.flataofpca;

import java.util.*;

import verimag.flata.acceleration.Accelerator;
import verimag.flata.acceleration.zigzag.*;
import verimag.flata.presburger.*;

// interface between transition and DBM
public class ZigzagClosure implements Accelerator {

	public static int DEBUG_NO = 0;
	public static int DEBUG_LOW = 1;
	public static int DEBUG_MEDIUM = 2;
	public static int DEBUG_LEVEL = DEBUG_LOW;
	
	private Vector<String> sym_table; // all symbols - incl. zero, auxiliary
	private Vector<String> symUnpOrig; // (both unprimed and primed) does not incl. zero, auxiliary
	private int auxVars;
	private LinearTerm[] substitution; // original symbols -> linear terms
	private ZigzagMatrix fw;
	private ZigzagMatrix bw;
	boolean isOctagon;

	public static String AUX_VAR_PREF = "#";
	public static String getAuxVar(int i) { return AUX_VAR_PREF + i; }
	
	public int accelerationFactor() {
		Zigzag z = new Zigzag(sym_table.size(), fw, bw);
		return z.accelerationFactor(); 
	}
	
	public OfpcaOutput computeClosure() {
		return computeClosure(isOctagon, OctagonalClosure.NOCHECK);
	}
	
	private OfpcaOutput computeClosure(boolean octagonal, boolean check) {
		
		Graph g = new Graph(sym_table.size(), fw, bw);		
		
		OctagonalClosure closure = new OctagonalClosure(sym_table, g, octagonal);
		
		try {
			closure.normalize(check);
		} catch (OutOfMemoryError e) {			
			if (!Relation.CLOSURE_ONLY) {
				System.out.println("Out of memory: ");
				System.out.println("\t" + g.getNodeCount() + " nodes"); 
				System.out.println("\t" + g.getEdgeCount() + " edges"); 
				System.out.println("\t" + TreeDictionary.count + " dictionary nodes");
				System.out.println("\t" + Zigzag.count + " zigzags");
				System.out.println("\t" + Node.count + " actual created nodes");
				System.out.println("\t" + Edge.count + " actual created edges");
				System.out.println("\t" + ZigzagEdge.count + " zigzag edges");
				throw (e);
			} else {
				System.err.println("################################## Out of memory #########################################");
				Relation.AccelerationComp.outOfMem = true;
				return null;
			}
		}

		//System.out.println("Zigzag automaton has " + g.getNodeCount() + " nodes");
		//System.out.println("Automaton built in " + closure.buildTime() + " msec");
		//System.out.println("Octagonal check in " + closure.checkTime() + " msec");
		//System.out.println("Total time: " + closure.totalTime() + " msec");
		
		// TODO: move code from  Output class
		
		OfpcaOutput output = new OfpcaOutput(closure, symUnpOrig, sym_table.size()-auxVars, octagonal);
		
		//output.printEdgesSingulars();
		if (DEBUG_LEVEL >= DEBUG_LOW)
			output.printClosure(fw,bw,sym_table);
		
		return output;
	}
	
	public Collection<Relation> closureAsDisjunctConstraints() {
		
		OfpcaOutput oa = this.computeClosure();
		
		if (!Relation.CLOSURE_ONLY)
			return (new OfpcaOutputElimination(this.varPool)).eliminate(oa, substitution);
		else
			return new LinkedList<Relation>();
		//return closureAsDisjunctConstraints(oa);
	}

	public void dbm2fwbw(DBM aDBM, String[] mat_domain) {
		Matrix aMat = aDBM.mat();
		
		// create forward and backward matrices 
		
		// ignore the diagonal
		
		auxVars = 0;
		
		int size = aMat.size();
		int nVars = size / 2;
		for (int i=0; i<nVars; i++) {
			for (int j=0; j<nVars; j++) {
				if (i == j)
					continue;
				if (aMat.get(i,j).isFinite())
					auxVars++;
				if (aMat.get(i+nVars,j+nVars).isFinite())
					auxVars++;
			}
		}
		
		int fwsize = nVars + auxVars;
		
		fw = new ZigzagMatrix(fwsize,ZigzagMatrix.STRING_TYPE);
		bw = new ZigzagMatrix(fwsize,ZigzagMatrix.STRING_TYPE);
		
		sym_table = new Vector<String>();
		//sym_orig = new Vector<String>();
		
		for (int i=0; i<mat_domain.length/2; i++) {
			sym_table.add(mat_domain[i]);
			//sym_orig.add(mat_domain[i].toString());
		}
		
		int tmpAuxVars = 0;
		for (int i=0; i<nVars; i++) {
			for (int j=0; j<nVars; j++) {
				
				// matrix viewed as
				//      X  X'
				//     ---------
				// X  | A  B
				// X' | C  D
				
				Field fB = aMat.get(i, j+nVars);
				if (fB.isFinite()) {
					fw.writes(i, j, ""+fB.toInt());
				}
				Field fC = aMat.get(i+nVars, j);
				if (fC.isFinite()) {
					bw.writes(i, j, ""+fC.toInt());
				}
				
				if (i == j)
					continue;
				
				Field fA = aMat.get(i,j);
				if (fA.isFinite()) {
					int inx = nVars+tmpAuxVars;
					sym_table.add(getAuxVar(inx));
					int val = fA.toInt();
					fw.writes(i,inx,""+val);
					bw.writes(inx,j,""+0);
					tmpAuxVars++;					
				}
				Field fD = aMat.get(i+nVars,j+nVars);
				if (fD.isFinite()) {
					int inx = nVars+tmpAuxVars;
					sym_table.add(getAuxVar(inx));
					int val = fD.toInt();
					bw.writes(i,inx,""+val);
					fw.writes(inx,j,""+0);
					tmpAuxVars++;
				}
			}			
		}
	}
	
	private Vector<String> varsOrigToStringVec(Variable[] aVarsOrig) {
		Vector<String> ret = new Vector<String>();
		
		for (Variable v : aVarsOrig)
			ret.add(v.name());
		
		return ret;
	}
	
	private VariablePool varPool;
	
	private String[] inferEncDomain(Variable[] vars) {
		String[] ret;
		if (this.isOctagon) {
			int l = vars.length;
			ret = new String[2*l];
			int ii=0;
			for (int i=0; i<l; i++) {
				String s = vars[i].name();
				ret[ii++] = s+"+";
				ret[ii++] = s+"-";
			}
		} else {
			int l = vars.length;
			ret = new String[l+2];
			int lh = l / 2;
			ret[0] = "$";
			ret[lh+1] = "$'";
			for (int i=0; i<lh; i++) {
				String s = vars[i].name();
				ret[i+1] = s;
				ret[i+lh+2] = s+"'";
			}
		}
		return ret;
	}
	
	
	// contract: if DBM, first Linear term in the array corresponds to zero 
	// if Octagon, ...
	public Relation[] closure(DBM aDBM, boolean aIsOctagon, LinearTerm[] aSubstitution, Variable[] aVarsOrig) {
		
		varPool = aVarsOrig[0].vp();
		
		symUnpOrig = varsOrigToStringVec(Arrays.copyOfRange(aVarsOrig, 0, aVarsOrig.length/2));
		isOctagon = aIsOctagon;
		substitution = aSubstitution;
		
		dbm2fwbw(aDBM, inferEncDomain(aVarsOrig));
//		if (DEBUG_LEVEL >= DEBUG_LOW) {
//			try {
//				if (ClosureTestSet.bw != null)
//					ClosureTestSet.bw.write("F/B variables: "+this.sym_table.size()+"\n");
//				System.out.println("F/B variables: "+this.sym_table.size()+"\n");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		return closureAsDisjunctConstraints().toArray(new Relation[0]);
		
	}
	
//	public int accelerationFactor(DBM aDBM) {
//		
//		dbm2fwbw(aDBM);
//		
//		return accelerationFactor(); 
//	}

	
}
