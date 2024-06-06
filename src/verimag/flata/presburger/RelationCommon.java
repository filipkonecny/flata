package verimag.flata.presburger;

import java.util.Collection;

import org.sosy_lab.java_smt.api.BooleanFormula;

import verimag.flata.common.Answer;
import verimag.flata.common.FlataJavaSMT;
import verimag.flata.common.IndentedWriter;
import verimag.flata.presburger.Relation.RelType;

public abstract class RelationCommon {

	public abstract BooleanFormula toJSMTAsConj(FlataJavaSMT fjsmt);
	public abstract void update(ConstProps cps);
	public abstract boolean simpleContradiction();
	public abstract Answer satisfiable();
	public abstract boolean isDBRel();
	public abstract boolean isOctagon();
	public abstract boolean isLinear();
	public abstract boolean isModulo();
	public abstract ConstProps inConst();
	public abstract ConstProps outConst();
	public abstract boolean isARMCCompatible();
	//public abstract boolean isFASTCompatible();
	public abstract RelationCommon[] closureMaybeStar();
	public abstract RelationCommon asCompact();
	public abstract RelationCommon copy();
	public abstract RelationCommon copy(Rename aRenVals, VariablePool aVP);
	public abstract RelType getType();
	public abstract void refVars(Collection<Variable> aCol);
	public abstract void refVarsAsUnp(Collection<Variable> aCol);
	public abstract Collection<Variable> identVars();
	public boolean contradictory() {
		return this.satisfiable().isFalse();
	}
	public String typeName() {
		return getType().nameShort();
	}
	public abstract boolean isIdentity();

}
