package verimag.flata;
import java.io.File;

import java.util.*;

import verimag.flata.acceleration.delta.DeltaClosure;
import verimag.flata.automata.ca.*;
import verimag.flata.presburger.*;


public class Closure {
	
	public static List<CompositeRel> closure2(List<Relation> rels, VariablePool pool, boolean plus) {
		List<CompositeRel> tmp = new LinkedList<CompositeRel>();
		for (Relation r : rels) {
			tmp.add(new CompositeRel(r));
		}
		return closure(tmp,pool,plus);
	}
	
	// plus  --> returns R^+
	// !plus --> returns R^*
	public static List<CompositeRel> closure(List<CompositeRel> rels, VariablePool pool, boolean plus) {
		
		CA ca = new CA("",pool);
		CAState s = ca.getStateWithName("s");
		for (CompositeRel r : rels) {
			CATransition t = new CATransition(s, s, r, ca);
			ca.addTransition(t);
		}		
		
		List<CATransition> comp = new LinkedList<CATransition>();
		verimag.flata.automata.ca.MultiloopTransformation.reduceMultiLoopState(ca, s, true, true, comp, plus);
		
		List<CompositeRel> ret = new LinkedList<CompositeRel>();
		for (CATransition t : comp) {
			ret.add(t.rel());
		}
		
		return ret;
	}
	
	public static Collection<CompositeRel> hackClosure2(VariablePool pool, Collection<Relation> col, boolean plus) {
		Collection<CompositeRel> aux = new LinkedList<CompositeRel>();
		for (Relation r : col) {
			aux.add(new CompositeRel(Relation.toMinType(r)));
		}
		return hackClosure(pool,aux, plus);
	}
	public static Collection<CompositeRel> hackClosure(VariablePool pool, Collection<CompositeRel> col, boolean plus) {
		CA ca = new CA("",pool);
		CAState s = ca.getStateWithName("s");
		for (CompositeRel r : col) {
			CATransition t = new CATransition(s, s, r, ca);
			ca.addTransition(t);
		}
		
		Collection<CATransition> comp = new LinkedList<CATransition>();
		int r = verimag.flata.automata.ca.MultiloopTransformation.reduceMultiLoopState(ca, s, true, true, comp, plus);
		
		if (r < 0) // unsuccessful
			return null;
		
		Collection<CompositeRel> ret = new LinkedList<CompositeRel>();
		for (CATransition t : comp)
			ret.add(t.rel());
		return ret;
	}
	
	public static void main(String[] args) {
		
		Relation.CLOSURE_ONLY = false;
		Relation.COMPACTNESS = false;
		Relation.closureComp = Relation.AccelerationComp.DELTA;
		if (args.length > 1 && args[1].equals("-zigzag"))
			Relation.closureComp = Relation.AccelerationComp.ZIGZAG;
		DeltaClosure.DEBUG_LEVEL = DeltaClosure.DEBUG_LOW;
		DeltaClosure.delta_parametricFW = true;
		
		//boolean detail = !(Relation.closureComp == Relation.AccelerationComp.ZIGZAG);
		
		VariablePool pool = VariablePool.createEmptyPoolNoDeclar();
		Collection<ModuloRel> col = verimag.flata.parsers.MParser.parseRels(new File(args[0]), null, pool);
		
		if (col.size() == 1) {
			Relation r = Relation.toMinType(col.iterator().next());
			CompositeRel rel = new CompositeRel(r);
			CompositeRel[] ret = rel.closureMaybeStar();
			
			System.out.println("--------------");
			for (CompositeRel rr : ret)
				System.out.println(rr.toString());
		} else {
			
			CA ca = new CA("",pool);
			CAState s = ca.getStateWithName("s");
			for (Relation r : col) {
				r = Relation.toMinType(r);
				CompositeRel cr = new CompositeRel(r);
				CATransition t = new CATransition(s, s, cr, ca);
				ca.addTransition(t);
			}
			
			Collection<CATransition> comp = new LinkedList<CATransition>();
			verimag.flata.automata.ca.MultiloopTransformation.reduceMultiLoopState(ca, s, true, true, comp);
				
			System.out.println("--------------");
			for (CATransition t : comp)
				System.out.println(t.rel().toString());
		}
	}
}
