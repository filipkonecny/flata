//package verimag.flata.presburger;
//
//import java.util.*;
//
//import verimag.flata.common.Answer;
//import verimag.flata.common.IndentedWriter;
//
//// relation on the top of Modulo Relation which has some variables existentially quantified
//public class ModExistsRel extends Relation {
//	
//	private List<Variable> quantified;
//	private ModuloRel rel;
//	
//	public ModExistsRel(ModuloRel aR, List<Variable> aP) {
//		rel = aR;
//		quantified = aP;
//	}
//	
//	
//	@Override
//	public Relation[] compose(Relation otherR) {
//		if (!(otherR instanceof DBRel)) {
//			return Relation.compose(this, otherR);
//		} else {
//			
//			ModExistsRel other = (ModExistsRel)otherR;
//			
//			
//			return rel.compose_param(other.rel, quantified);
//		}
//	}
//	
//	
//	
//	
//	
//	
//	@Override
//	public Relation abstractDBRel() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public Relation abstractLin() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public Relation abstractOct() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public void addImplicitActions() {
//		// TODO
//		
//	}
//	@Override
//	public Relation asCompact() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public Relation[] closure() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public Relation[] closurePlus() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public ClosureDetail closurePlus_detail() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public ClosureDetail closure_detail() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public Relation copy() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public Relation copy(Rename aRenVals, VariablePool aVP) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public Relation[] domain() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public Relation[] existElim1(Variable v) {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public Relation[] existElim2(Variable v) {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public RelType getType() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public Collection<Variable> identVars() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public ConstProps inConst() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public Answer includes(Relation other) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public Relation[] intersect(Relation other) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public boolean isARMCCompatible() {
//		throw new RuntimeException("internal error");
//	}
//	@Override
//	public boolean isDBRel() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	@Override
//	public boolean isFASTCompatible() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	@Override
//	public boolean isLinear() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	@Override
//	public boolean isModulo() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	@Override
//	public boolean isOctagon() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	@Override
//	public Relation merge(Relation other) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public Relation[] minPartition() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public ConstProps outConst() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public Relation[] range() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public void refVars(Collection<Variable> aCol) {
//		// TODO Auto-generated method stub
//		
//	}
//	@Override
//	public void refVarsAsUnp(Collection<Variable> aCol) {
//		// TODO Auto-generated method stub
//		
//	}
//	@Override
//	public Variable[] refVarsUnpPSorted() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public Answer relEquals(Relation other) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public Answer satisfiable() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public boolean simpleContradiction() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	@Override
//	public Relation[] substitute(Substitution s) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public boolean tautology() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	@Override
//	public DBRel toDBRel() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public LinearRel toLinearRel() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public ModuloRel toModuloRel() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public OctagonRel toOctagonRel() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public void update(ConstProps cps) {
//		// TODO Auto-generated method stub
//		
//	}
//	@Override
//	public Relation weakestNontermCond() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	
//	
//	
//
//}
