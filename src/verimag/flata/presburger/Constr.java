package verimag.flata.presburger;

import java.util.List;
import java.util.Set;

public interface Constr {
	
	public boolean isLinear();
	public boolean isModulo();

	public Set<Variable> variables();
	
	public Constr copy();
	
	public List<Constr> not();
	
}
