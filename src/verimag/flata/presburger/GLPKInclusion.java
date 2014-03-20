package verimag.flata.presburger;

import java.util.*;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;

public class GLPKInclusion {
	
	//optimization -- remember all variables
	public static Variable[] allVars = null; // all variables (both unprimed and primed);
	public static Map<Variable,Integer> varInx = null; // indices 1-n
	
	public static void initOpt(Variable[] varsUnp) {
		int len = varsUnp.length;
		
		//allVars = new Variable[len*2];
		Arrays.sort(varsUnp);
		allVars = Arrays.copyOf(varsUnp, len*2);
		varInx = new HashMap<Variable,Integer>();
		
		for (int i=0; i<len; i++) {
			allVars[i+len] = allVars[i].getCounterpart();
			
			varInx.put(allVars[i], new Integer(i));
			varInx.put(allVars[i+len], new Integer(i+len)); 
		}
	}
	

	// last argument used for negation
	private static void fill_LP_with_constraint(glp_prob lp, LinearConstr lc, int cc) {

		int row = GLPK.glp_add_rows(lp, 1);
		
		//Collection<LinearTerm> terms = lc.terms();
		LinearTerm tz = lc.get(null);
		int tz_coef = (tz == null)? 0 : tz.coeff();	
		if (cc == -1)
			tz_coef = -(tz_coef - 1);
		
		GLPK.glp_set_row_name(lp, row, "aux"+row);
		GLPK.glp_set_row_bnds(lp, row, GLPKConstants.GLP_UP, 0.,-tz_coef);

		
		Collection<LinearTerm> terms = lc.terms();
		int size = terms.size();
		if (tz != null)
			size --;
		SWIGTYPE_p_int ind = GLPK.new_intArray(size+1);
		SWIGTYPE_p_double val = GLPK.new_doubleArray(size+1);
		
		int i=0;
		for (LinearTerm lt : terms) {
			if (lt.variable() == null) {
				continue;
			}
			
			i++;
			GLPK.intArray_setitem(ind, i, varInx.get(lt.variable()).intValue()+1);
			GLPK.doubleArray_setitem(val, i, lt.coeff()*cc);
		}
		
		GLPK.glp_set_mat_row(lp, row, size, ind, val);
	}
	
	private static glp_prob set_LP(LinearRel lr) {
		
		int width = allVars.length;
		
		// Create problem		
		glp_prob lp = GLPK.glp_create_prob();
		GLPK.glp_set_prob_name(lp, "Inclusion problem");
		
		// Define columns
		GLPK.glp_add_cols(lp, width);
		for (int i=0; i<width; i++) {
			GLPK.glp_set_col_name(lp, 1+i, allVars[i].toString());
			GLPK.glp_set_col_kind(lp, 1+i, GLPKConstants.GLP_IV);
			GLPK.glp_set_col_bnds(lp, 1+i, GLPKConstants.GLP_FR, 0., 0.);
		}
				
		// Define rows and fill matrix (definition of non-zero entries is sufficient)
		//GLPK.glp_add_rows(lp, height);
		//int row=0; // rows are indexed from 1
		for (LinearConstr lc : lr.constraints()) {
			
			fill_LP_with_constraint(lp,lc, 1);
		}
		
		return lp;
	}
	
	private static void define_const_objective(glp_prob lp) {
		
		int width = allVars.length;
		
		// Define objective 
		GLPK.glp_set_obj_name(lp, "obj");
		GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
		for (int i=0; i<width; i++) {
			GLPK.glp_set_obj_coef(lp, 1+i, 0.);
		}
	}
	
		
	private static boolean hasSolution(glp_prob lp) {

		glp_iocp parm_mip = new glp_iocp();
		//parm_mip.setMsg_lev(0);
        GLPK.glp_init_iocp(parm_mip);
        parm_mip.setPresolve(GLPKConstants.GLP_ON);
        int intopt_ret = GLPK.glp_intopt(lp, parm_mip);
		int intopt_status = GLPK.glp_mip_status(lp);
		
//		System.out.println("intopt returned:"+intopt_ret);
//		System.out.println("intopt returned:"+intopt_status
//				+"  "+(intopt_status == GLPK.GLP_UNDEF)
//				+"  "+(intopt_status == GLPK.GLP_OPT)
//				+"  "+(intopt_status == GLPK.GLP_FEAS)
//				+"  "+(intopt_status == GLPK.GLP_INFEAS)
//				);
//		write_mlp_solution(lp);
		
		if (intopt_ret != 0) {
			
			// does GLP_ENOPFS = no primal feasible solution imply no infeasible solution at all ?? TODO
			if (intopt_ret == GLPK.GLP_ENOPFS)
				return false;
			
			print_matrix(lp);
			
			System.out.println("GLP_EBOUND"+" "+(GLPK.GLP_EBOUND == intopt_ret));
			System.out.println("GLP_EROOT"+" "+(GLPK.GLP_EROOT == intopt_ret));
			System.out.println("GLP_ENOPFS"+" "+(GLPK.GLP_ENOPFS == intopt_ret));
			System.out.println("GLP_ENODFS"+" "+(GLPK.GLP_ENODFS == intopt_ret));
			System.out.println("GLP_EFAIL"+" "+(GLPK.GLP_EFAIL == intopt_ret));
			System.out.println("GLP_EMIPGAP"+" "+(GLPK.GLP_EMIPGAP == intopt_ret));
			System.out.println("GLP_ETMLIM"+" "+(GLPK.GLP_ETMLIM == intopt_ret));
			System.out.println("GLP_ESTOP"+" "+(GLPK.GLP_ESTOP == intopt_ret));
			throw new RuntimeException("Unexpected GLPK status");
		}
		
		return (intopt_ret == 0) && (intopt_status == GLPK.GLP_OPT || intopt_status == GLPK.GLP_FEAS);
	}
	
	public static boolean isSatisfiable(LinearRel lr) {
		
		//System.err.print("S");
		
		if (allVars != null) {
			
			glp_prob lp = set_LP(lr);
			
			define_const_objective(lp);
			
			boolean b = hasSolution(lp);
			
			GLPK.glp_delete_prob(lp);
			
			return b;
			
		}
		
		return true;
	}

	
	public static boolean isIncluded(LinearRel lr1, LinearRel lr2) {
		
		//System.err.print("I");
		
		if (allVars != null) {
					
			glp_prob lp = set_LP(lr1);
			
			define_const_objective(lp);
			
			for (LinearConstr lc_neg : lr2.constraints()) {
				
				glp_prob lp2 = GLPK.glp_create_prob();
				GLPK.glp_copy_prob(lp2, lp, GLPK.GLP_ON);
				
				fill_LP_with_constraint(lp2,lc_neg, -1); // add negated
				
				if (hasSolution(lp2)) {
					GLPK.glp_delete_prob(lp);
					GLPK.glp_delete_prob(lp2);
					return false;
				}
				
				GLPK.glp_delete_prob(lp2);
			}
			
			GLPK.glp_delete_prob(lp);
			return true;
		}
		
		return false;
	}
	
	private static void print_matrix(glp_prob lp) {
		int rows = GLPK.glp_get_num_rows(lp);
		int cols = GLPK.glp_get_num_cols(lp);
		for (int row=1; row<=rows; row++) {
			 
			SWIGTYPE_p_int ind = GLPK.new_intArray(cols+1);
			SWIGTYPE_p_double val = GLPK.new_doubleArray(cols+1);
			GLPK.glp_get_mat_row(lp, row, ind, val);
			
			System.out.print("row "+row+": ");
			for (int i=1; i<=cols; i++) {
				int _ind = GLPK.intArray_getitem(ind, i);
				double _val = GLPK.doubleArray_getitem(val, i);
				System.out.print("("+_ind+","+_val+")");
			}
			System.out.print(" up="+GLPK.glp_get_row_ub(lp, row));
			
			System.out.println();
		}
	}
	
	private static void write_mlp_solution(glp_prob lp) {
		
		int i;
		int n;
		String name;
		double val;
		
		name = GLPK.glp_get_obj_name(lp);
		val  = GLPK.glp_mip_obj_val(lp);
		System.out.print(name);
		System.out.print(" = ");
		System.out.println(val);
		n = GLPK.glp_get_num_cols(lp);
		for(i=1; i <= n; i++)
		{
			name = GLPK.glp_get_col_name(lp, i);
			val  = GLPK.glp_mip_col_val(lp, i);
			System.out.print(name);
			System.out.print(" = ");
			System.out.println(val);
		}
	}
	

	
}
