/**
 * 
 */
package verimag.flata.presburger;

public abstract class Rename {
	public abstract String getNewNameFor(String aName);
	public abstract void put(String k, String v);
//	public Variable getNewVariable(Variable v) {
//		return new Variable(getNewNameFor(v.name));
//	}
}