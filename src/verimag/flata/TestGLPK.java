package verimag.flata;
import org.gnu.glpk.*;

public class TestGLPK {

	static
	{ 
		System.loadLibrary("glpk_java");
		GLPK.glp_term_out(GLPK.GLP_OFF);
		/*try
		{
		      // try to load Linux library
			 
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("library not found");
			System.exit(-1);
			// try to load Windows library
			System.loadLibrary("glpk_4_39_java"); 
		}*/
	}

//	Minimize z = (x1-x2) /2 + (1-(x1-x2)) = -.5 * x1 + .5 * x2 + 1
//
//		subject to
//		0.0<= x1 - x2 <= 0.2
//		where,
//		0.0 <= x1 <= 0.5
//		0.0 <= x2 <= 0.5

	public static void exampleProblem()
	{
		glp_prob lp;
		glp_smcp parm;
		SWIGTYPE_p_int ind;
		SWIGTYPE_p_double val;
		int ret;

//      Create problem		
		lp = GLPK.glp_create_prob();
		System.out.println("Problem created");
		GLPK.glp_set_prob_name(lp, "myProblem");

//      Define columns
		GLPK.glp_add_cols(lp, 2);
		GLPK.glp_set_col_name(lp, 1, "x1");
		GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_CV);
		GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_DB, 0, .5);
		GLPK.glp_set_col_name(lp, 2, "x2");
		GLPK.glp_set_col_kind(lp, 2, GLPKConstants.GLP_CV);
		GLPK.glp_set_col_bnds(lp, 2, GLPKConstants.GLP_DB, 0, .5);

//      Create constraints
		GLPK.glp_add_rows(lp, 2);

		GLPK.glp_set_row_name(lp, 1, "c1");
		GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_LO, -0.2, 0.);
		ind = GLPK.new_intArray(3);
		GLPK.intArray_setitem(ind, 1, 1);
		GLPK.intArray_setitem(ind, 2, 2);
		val = GLPK.new_doubleArray(3);
		GLPK.doubleArray_setitem(val, 1, -1.);
		GLPK.doubleArray_setitem(val, 2, 1.);
		GLPK.glp_set_mat_row(lp, 1, 2, ind, val);

		GLPK.glp_set_row_name(lp, 2, "c2");
		GLPK.glp_set_row_bnds(lp, 2, GLPKConstants.GLP_LO, 0., 0.);
		ind = GLPK.new_intArray(3);
		GLPK.intArray_setitem(ind, 1, 1);
		GLPK.intArray_setitem(ind, 2, 2);
		val = GLPK.new_doubleArray(3);
		GLPK.doubleArray_setitem(val, 1, 1.);
		GLPK.doubleArray_setitem(val, 2, -1.);
		GLPK.glp_set_mat_row(lp, 2, 2, ind, val);
		
//      Define objective 
		GLPK.glp_set_obj_name(lp, "z");
		GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
		GLPK.glp_set_obj_coef(lp, 0, 0.);
		GLPK.glp_set_obj_coef(lp, 1, -0.5);
		GLPK.glp_set_obj_coef(lp, 2, 0.5);

//	solve model
		parm = new glp_smcp();
		GLPK.glp_init_smcp(parm);
		ret = GLPK.glp_simplex(lp, parm);
		
//      Retrieve solution
                if (ret == 0) {
			write_lp_solution(lp);
		}
		else {
			System.out.println("The problemcould not be solved");
		};
		
		// free memory
		GLPK.glp_delete_prob(lp);
	}	

	/**
	 * 	Maximize z = y
			subject to
			s1 = 11x - y,
			s2 = -18x - y.
			s3 = 1.5x + y
			where,
			-inf <= x1,x2,x3 <= inf
			9.5 <= s1 <= inf,
			-34 <= s2 <= inf,
			1 <= s3 <= inf,
	 */
		public static void triangleProblem()
		{
			glp_prob lp;
			//glp_smcp parm;
			SWIGTYPE_p_int ind;
			SWIGTYPE_p_double val;
			int ret;

			// Create problem		
			lp = GLPK.glp_create_prob();
			System.out.println("Problem created");
			GLPK.glp_set_prob_name(lp, "triangle problem");

			// Define columns
			GLPK.glp_add_cols(lp, 2);
			GLPK.glp_set_col_name(lp, 1, "x");
			GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_IV);
			GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_FR, 0., 0.);
			GLPK.glp_set_col_name(lp, 2, "y");
			GLPK.glp_set_col_kind(lp, 2, GLPKConstants.GLP_IV);
			GLPK.glp_set_col_bnds(lp, 2, GLPKConstants.GLP_FR, 0., 0.);

			// Create constraints
			GLPK.glp_add_rows(lp, 3);

			GLPK.glp_set_row_name(lp, 1, "s1");
			GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_LO, 9.5, 0.);
			ind = GLPK.new_intArray(3);
			GLPK.intArray_setitem(ind, 1, 1);
			GLPK.intArray_setitem(ind, 2, 2);
			val = GLPK.new_doubleArray(3);
			GLPK.doubleArray_setitem(val, 1, 11.);
			GLPK.doubleArray_setitem(val, 2, -1.);
			GLPK.glp_set_mat_row(lp, 1, 2, ind, val);

			GLPK.glp_set_row_name(lp, 2, "s2");
			GLPK.glp_set_row_bnds(lp, 2, GLPKConstants.GLP_LO, -34., 0);
			ind = GLPK.new_intArray(3);
			GLPK.intArray_setitem(ind, 1, 1);
			GLPK.intArray_setitem(ind, 2, 2);
			val = GLPK.new_doubleArray(3);
			GLPK.doubleArray_setitem(val, 1, -18.);
			GLPK.doubleArray_setitem(val, 2, -1.);
			GLPK.glp_set_mat_row(lp, 2, 2, ind, val);
			
			GLPK.glp_set_row_name(lp, 3, "s3");
			GLPK.glp_set_row_bnds(lp, 3, GLPKConstants.GLP_LO, 1., 0);
			ind = GLPK.new_intArray(3);
			GLPK.intArray_setitem(ind, 1, 1);
			GLPK.intArray_setitem(ind, 2, 2);
			val = GLPK.new_doubleArray(3);
			GLPK.doubleArray_setitem(val, 1, 1.5);
			GLPK.doubleArray_setitem(val, 2, 1.);
			GLPK.glp_set_mat_row(lp, 3, 2, ind, val);
			
			// Define objective 
			GLPK.glp_set_obj_name(lp, "z");
			GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
			//GLPK.glp_set_obj_coef(lp, 0, 0.);
			GLPK.glp_set_obj_coef(lp, 1, -2.0);
			GLPK.glp_set_obj_coef(lp, 2, -1.0);

			// solve model
			//parm = new glp_smcp();
			//GLPK.glp_init_smcp(parm);
			//ret = GLPK.glp_simplex(lp, parm);
			//ret = GLPK.glp_simplex(lp, null);
			// Retrieve solution
	        //if (ret == 0) write_lp_solution(lp);
			//else System.out.println("The problem could not be solved");
			
	        glp_iocp parm_mip = new glp_iocp();
	        GLPK.glp_init_iocp(parm_mip);
	        parm_mip.setPresolve(GLPKConstants.GLP_ON);
			ret = GLPK.glp_intopt(lp, parm_mip);
			// Retrieve solution
	        if (ret == 0) write_mlp_solution(lp);
			else System.out.println("The problem could not be solved");
	        
			// free memory
			GLPK.glp_delete_prob(lp);
		}	

	
	public static void squareProblem() {
		glp_prob lp;
		SWIGTYPE_p_int ind;
		SWIGTYPE_p_double val;
		int ret;

		// Create problem		
		lp = GLPK.glp_create_prob();
		System.out.println("Problem created");
		GLPK.glp_set_prob_name(lp, "square problem");

		// Define columns
		GLPK.glp_add_cols(lp, 2);
		GLPK.glp_set_col_name(lp, 1, "x");
		GLPK.glp_set_col_kind(lp, 1, GLPKConstants.GLP_IV);
		GLPK.glp_set_col_bnds(lp, 1, GLPKConstants.GLP_FR, 0., 0.);
		GLPK.glp_set_col_name(lp, 2, "y");
		GLPK.glp_set_col_kind(lp, 2, GLPKConstants.GLP_IV);
		GLPK.glp_set_col_bnds(lp, 2, GLPKConstants.GLP_FR, 0., 0.);

		// Create constraints
		GLPK.glp_add_rows(lp, 2);

		GLPK.glp_set_row_name(lp, 1, "s1");
		GLPK.glp_set_row_bnds(lp, 1, GLPKConstants.GLP_DB, 0.5, 2.5);
		ind = GLPK.new_intArray(3);
		GLPK.intArray_setitem(ind, 1, 1);
		GLPK.intArray_setitem(ind, 2, 2);
		val = GLPK.new_doubleArray(3);
		GLPK.doubleArray_setitem(val, 1, 1.);
		GLPK.doubleArray_setitem(val, 2, 0.);
		GLPK.glp_set_mat_row(lp, 1, 2, ind, val);

		GLPK.glp_set_row_name(lp, 2, "s2");
		GLPK.glp_set_row_bnds(lp, 2, GLPKConstants.GLP_DB, 1.5, 3.5);
		ind = GLPK.new_intArray(3);
		GLPK.intArray_setitem(ind, 1, 1);
		GLPK.intArray_setitem(ind, 2, 2);
		val = GLPK.new_doubleArray(3);
		GLPK.doubleArray_setitem(val, 1, 0.);
		GLPK.doubleArray_setitem(val, 2, 1.);
		GLPK.glp_set_mat_row(lp, 2, 2, ind, val);
		
		// Define objective 
		GLPK.glp_set_obj_name(lp, "z");
		GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
		GLPK.glp_set_obj_coef(lp, 0, 0.);
		GLPK.glp_set_obj_coef(lp, 1, 1.);
		GLPK.glp_set_obj_coef(lp, 2, -1.);

		/*
		// solve model
		parm = new glp_smcp();
		GLPK.glp_init_smcp(parm);
		ret = GLPK.glp_simplex(lp, parm);
		// Retrieve solution
        if (ret == 0) write_lp_solution(lp);
		else System.out.println("The problem could not be solved");
		*/
		
		glp_iocp parm_mip = new glp_iocp();
        GLPK.glp_init_iocp(parm_mip);
        parm_mip.setPresolve(GLPKConstants.GLP_ON);
		ret = GLPK.glp_intopt(lp, parm_mip);
		// Retrieve solution
        if (ret == 0) write_mlp_solution(lp);
		else System.out.println("The problem could not be solved");
        
		// free memory
		GLPK.glp_delete_prob(lp);
	}
	
	public static void main(String[] arg) {
		//exampleProblem();
		//squareProblem();
		triangleProblem();
	}
	
	static void write_lp_solution(glp_prob lp) {

		int i;
		int n;
		String name;
		double val;
		
		name = GLPK.glp_get_obj_name(lp);
		val  = GLPK.glp_get_obj_val(lp);
		System.out.print(name);
		System.out.print(" = ");
		System.out.println(val);
		n = GLPK.glp_get_num_cols(lp);
		for(i=1; i <= n; i++)
		{
			name = GLPK.glp_get_col_name(lp, i);
			val  = GLPK.glp_get_col_prim(lp, i);
			System.out.print(name);
			System.out.print(" = ");
			System.out.println(val);
		}
	}
	
	static void write_mlp_solution(glp_prob lp) {
		
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
