/**
 * 
 */
package verimag.flata.presburger;

import java.util.Collection;
import java.util.LinkedList;


public class DBCParam {
	// form: x-y <= ak + b
	public Collection<DBC> col_dbc = new LinkedList<DBC>(); 
	// form: ak + b <= 0
	public Collection<ParamBound> col_param = new LinkedList<ParamBound>();
}