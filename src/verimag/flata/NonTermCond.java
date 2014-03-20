package verimag.flata;
import java.io.File;
import java.util.Collection;

import verimag.flata.presburger.ModuloRel;
import verimag.flata.presburger.Relation;
import verimag.flata.presburger.VariablePool;
import verimag.flata.presburger.Relation.RelType;


public class NonTermCond {

	public static void main(String[] args) {
		
		//System.out.println("Non-termination condition");
		
		VariablePool pool = VariablePool.createEmptyPoolNoDeclar();
		Collection<ModuloRel> col = verimag.flata.parsers.MParser.parseRel(new File(args[0]), null, pool);
		
		if (col.size() != 1) {
			System.err.println("incorrect input");
			System.exit(-1);
		}
		
		Relation rel = Relation.toMinType(col.iterator().next());
		
		if (rel.getType() == RelType.DBREL) {
			Relation cond = rel.weakestNontermCond();
			if (cond.contradictory())
				System.out.println("False");
			else 
				System.out.println(cond.toString());
		}
		
	}
	
}
