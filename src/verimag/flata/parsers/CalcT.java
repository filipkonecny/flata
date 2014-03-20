// $ANTLR 3.3 Nov 30, 2010 12:50:56 CalcT.g 2013-11-11 16:03:48

  package verimag.flata.parsers;
  
  import java.io.File;
import org.antlr.runtime.BitSet;
import java.util.*;
  
  import verimag.flata.presburger.*;



import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class CalcT extends TreeParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "PLUS", "MINUS", "MULT", "AND", "EQ", "PRINT", "PRINT_ARMC", "DOMAIN", "RANGE", "EXISTS", "TERMINATION", "PREFPERIOD", "COMPOSE", "COMMA", "WN", "ARRAYS", "SCALARS", "TRUE", "FALSE", "LPAR_C", "RPAR_C", "LPAR", "RPAR", "LOCALS", "GLOBALS", "PORT_IN", "PORT_OUT", "AUTOMATA", "AUTOMATON", "AUTOMATON_WITH_SYMBOLS", "INITIAL", "FINAL", "ERROR", "TRANSITIONS", "TRANSITION", "TERMS", "GUARDS", "GUARD", "ACTIONS", "ACTION", "CONSTRAINTS", "CONSTRAINT", "CALL", "CONSTR_INPUT", "ASSIGN", "ID", "PRIMED_ID", "OR", "NOT", "CL_PLUS", "CL_STAR", "CL_EXPR", "ABSTR_D", "ABSTR_O", "ABSTR_L", "NEQ", "LEQ", "LESS", "GEQ", "GREATER", "DIVIDES", "CONST", "NUM", "ALPHA", "ALPHANUM", "ABSTR", "WHITESPACE", "SINGLE_COMMENT", "MULTI_COMMENT", "STRSTR", "':'", "';'", "'.'"
    };
    public static final int EOF=-1;
    public static final int T__74=74;
    public static final int T__75=75;
    public static final int T__76=76;
    public static final int PLUS=4;
    public static final int MINUS=5;
    public static final int MULT=6;
    public static final int AND=7;
    public static final int EQ=8;
    public static final int PRINT=9;
    public static final int PRINT_ARMC=10;
    public static final int DOMAIN=11;
    public static final int RANGE=12;
    public static final int EXISTS=13;
    public static final int TERMINATION=14;
    public static final int PREFPERIOD=15;
    public static final int COMPOSE=16;
    public static final int COMMA=17;
    public static final int WN=18;
    public static final int ARRAYS=19;
    public static final int SCALARS=20;
    public static final int TRUE=21;
    public static final int FALSE=22;
    public static final int LPAR_C=23;
    public static final int RPAR_C=24;
    public static final int LPAR=25;
    public static final int RPAR=26;
    public static final int LOCALS=27;
    public static final int GLOBALS=28;
    public static final int PORT_IN=29;
    public static final int PORT_OUT=30;
    public static final int AUTOMATA=31;
    public static final int AUTOMATON=32;
    public static final int AUTOMATON_WITH_SYMBOLS=33;
    public static final int INITIAL=34;
    public static final int FINAL=35;
    public static final int ERROR=36;
    public static final int TRANSITIONS=37;
    public static final int TRANSITION=38;
    public static final int TERMS=39;
    public static final int GUARDS=40;
    public static final int GUARD=41;
    public static final int ACTIONS=42;
    public static final int ACTION=43;
    public static final int CONSTRAINTS=44;
    public static final int CONSTRAINT=45;
    public static final int CALL=46;
    public static final int CONSTR_INPUT=47;
    public static final int ASSIGN=48;
    public static final int ID=49;
    public static final int PRIMED_ID=50;
    public static final int OR=51;
    public static final int NOT=52;
    public static final int CL_PLUS=53;
    public static final int CL_STAR=54;
    public static final int CL_EXPR=55;
    public static final int ABSTR_D=56;
    public static final int ABSTR_O=57;
    public static final int ABSTR_L=58;
    public static final int NEQ=59;
    public static final int LEQ=60;
    public static final int LESS=61;
    public static final int GEQ=62;
    public static final int GREATER=63;
    public static final int DIVIDES=64;
    public static final int CONST=65;
    public static final int NUM=66;
    public static final int ALPHA=67;
    public static final int ALPHANUM=68;
    public static final int ABSTR=69;
    public static final int WHITESPACE=70;
    public static final int SINGLE_COMMENT=71;
    public static final int MULTI_COMMENT=72;
    public static final int STRSTR=73;

    // delegates
    // delegators


        public CalcT(TreeNodeStream input) {
            this(input, new RecognizerSharedState());
        }
        public CalcT(TreeNodeStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return CalcT.tokenNames; }
    public String getGrammarFileName() { return "CalcT.g"; }



      public static class ComposeInput {
        public Collection<Variable> vars = new LinkedList<Variable>();
        public LinearRel t1;
        public LinearRel t2;
      }
      
      public VariablePool relvarPool = VariablePool.createEmptyPoolNoDeclar();
      
      public static final int eGuard = 0;
      public static final int eAction = 1;

      public File inputFilePath;      // name of file from which the parse tree has been created (without extension)
      
      private String getTextFromNode(CommonTree aNode) {
        return aNode.getText();
      }
      
      private void checkLinTerm(LinearConstr res, LinearConstr arg1, LinearConstr arg2) {
        if (res == null) {
          System.out.println("transition label is not a linear constraint: multiplication ("+arg1+")*("+arg2+")");
        }
      }

      
      private BENode.ASTConstrType parsertype2asttype(int i) {
        switch (i) {
        case EQ:
          return BENode.ASTConstrType.EQ;
        case NEQ:
          return BENode.ASTConstrType.NEQ;
        case LEQ:
          return BENode.ASTConstrType.LEQ;
        case LESS:
          return BENode.ASTConstrType.LESS;
        case GEQ:
          return BENode.ASTConstrType.GEQ;
        case GREATER:
          return BENode.ASTConstrType.GREATER;
        case DIVIDES:
          return BENode.ASTConstrType.DIVIDES;
        default:
          throw new RuntimeException("internal error: unexpected type \""+i+"\"");
        }
      }




    // $ANTLR start "constrsInput"
    // CalcT.g:71:1: constrsInput[VariablePool pool] returns [ List<ModuloRel> ret ] : ( constrInput[pool] )* ;
    public final List<ModuloRel> constrsInput(VariablePool pool) throws RecognitionException {
        List<ModuloRel> ret = null;

        List<ModuloRel> constrInput1 = null;


        try {
            // CalcT.g:72:2: ( ( constrInput[pool] )* )
            // CalcT.g:73:2: ( constrInput[pool] )*
            {
             ret = new LinkedList<ModuloRel>(); 
            // CalcT.g:74:2: ( constrInput[pool] )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==CONSTR_INPUT) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // CalcT.g:74:4: constrInput[pool]
            	    {
            	    pushFollow(FOLLOW_constrInput_in_constrsInput64);
            	    constrInput1=constrInput(pool);

            	    state._fsp--;

            	     ret.addAll(constrInput1); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ret;
    }
    // $ANTLR end "constrsInput"


    // $ANTLR start "constrInput"
    // CalcT.g:79:1: constrInput[VariablePool pool] returns [ List<ModuloRel> ret ] : ^( CONSTR_INPUT (c= constraints[null,pool] )? ) ;
    public final List<ModuloRel> constrInput(VariablePool pool) throws RecognitionException {
        List<ModuloRel> ret = null;

        BENode c = null;


        try {
            // CalcT.g:80:3: ( ^( CONSTR_INPUT (c= constraints[null,pool] )? ) )
            // CalcT.g:80:5: ^( CONSTR_INPUT (c= constraints[null,pool] )? )
            {
            match(input,CONSTR_INPUT,FOLLOW_CONSTR_INPUT_in_constrInput97); 

            if ( input.LA(1)==Token.DOWN ) {
                match(input, Token.DOWN, null); 
                // CalcT.g:81:7: (c= constraints[null,pool] )?
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==AND||(LA2_0>=DOMAIN && LA2_0<=EXISTS)||LA2_0==COMPOSE||(LA2_0>=TRUE && LA2_0<=FALSE)||LA2_0==CONSTRAINT||LA2_0==ID||(LA2_0>=OR && LA2_0<=ABSTR_L)) ) {
                    alt2=1;
                }
                switch (alt2) {
                    case 1 :
                        // CalcT.g:81:8: c= constraints[null,pool]
                        {
                        pushFollow(FOLLOW_constraints_in_constrInput109);
                        c=constraints(null, pool);

                        state._fsp--;


                        }
                        break;

                }


                match(input, Token.UP, null); 
            }
             
                    BENode root = c;
                    BENode dnfRoot = BENode.normalize(root);
                    ret = dnfRoot.dnf2Rels();
                  

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ret;
    }
    // $ANTLR end "constrInput"


    // $ANTLR start "calc"
    // CalcT.g:90:1: calc[VariablePool pool] : ( calc_print[pool, stored, storedI, params] | calc_print_armc[pool, stored, storedI, params] | calc_store[pool, stored, storedI, params] | calc_prefperiod[pool, stored, storedI, params] | calc_termination[pool, stored, storedI, params] )* ;
    public final void calc(VariablePool pool) throws RecognitionException {

          	Map<String,DisjRel> stored = new HashMap<String,DisjRel>();
          	Map<Variable,Integer> storedI = new HashMap<Variable,Integer>();
          	Map<String,Set<Variable>> params = new HashMap<String,Set<Variable>>();
          
        try {
            // CalcT.g:96:3: ( ( calc_print[pool, stored, storedI, params] | calc_print_armc[pool, stored, storedI, params] | calc_store[pool, stored, storedI, params] | calc_prefperiod[pool, stored, storedI, params] | calc_termination[pool, stored, storedI, params] )* )
            // CalcT.g:96:5: ( calc_print[pool, stored, storedI, params] | calc_print_armc[pool, stored, storedI, params] | calc_store[pool, stored, storedI, params] | calc_prefperiod[pool, stored, storedI, params] | calc_termination[pool, stored, storedI, params] )*
            {
            // CalcT.g:96:5: ( calc_print[pool, stored, storedI, params] | calc_print_armc[pool, stored, storedI, params] | calc_store[pool, stored, storedI, params] | calc_prefperiod[pool, stored, storedI, params] | calc_termination[pool, stored, storedI, params] )*
            loop3:
            do {
                int alt3=6;
                switch ( input.LA(1) ) {
                case PRINT:
                    {
                    alt3=1;
                    }
                    break;
                case PRINT_ARMC:
                    {
                    alt3=2;
                    }
                    break;
                case ID:
                    {
                    alt3=3;
                    }
                    break;
                case PREFPERIOD:
                    {
                    alt3=4;
                    }
                    break;
                case TERMINATION:
                    {
                    alt3=5;
                    }
                    break;

                }

                switch (alt3) {
            	case 1 :
            	    // CalcT.g:97:7: calc_print[pool, stored, storedI, params]
            	    {
            	    pushFollow(FOLLOW_calc_print_in_calc151);
            	    calc_print(pool, stored, storedI, params);

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // CalcT.g:98:9: calc_print_armc[pool, stored, storedI, params]
            	    {
            	    pushFollow(FOLLOW_calc_print_armc_in_calc162);
            	    calc_print_armc(pool, stored, storedI, params);

            	    state._fsp--;


            	    }
            	    break;
            	case 3 :
            	    // CalcT.g:99:9: calc_store[pool, stored, storedI, params]
            	    {
            	    pushFollow(FOLLOW_calc_store_in_calc175);
            	    calc_store(pool, stored, storedI, params);

            	    state._fsp--;


            	    }
            	    break;
            	case 4 :
            	    // CalcT.g:100:9: calc_prefperiod[pool, stored, storedI, params]
            	    {
            	    pushFollow(FOLLOW_calc_prefperiod_in_calc186);
            	    calc_prefperiod(pool, stored, storedI, params);

            	    state._fsp--;


            	    }
            	    break;
            	case 5 :
            	    // CalcT.g:101:9: calc_termination[pool, stored, storedI, params]
            	    {
            	    pushFollow(FOLLOW_calc_termination_in_calc197);
            	    calc_termination(pool, stored, storedI, params);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "calc"


    // $ANTLR start "calc_termination"
    // CalcT.g:105:1: calc_termination[VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params] : ^( TERMINATION constraints[null, pool] ) ;
    public final void calc_termination(VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params) throws RecognitionException {
        BENode constraints2 = null;


        try {
            // CalcT.g:106:3: ( ^( TERMINATION constraints[null, pool] ) )
            // CalcT.g:106:5: ^( TERMINATION constraints[null, pool] )
            {
            match(input,TERMINATION,FOLLOW_TERMINATION_in_calc_termination221); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_constraints_in_calc_termination231);
            constraints2=constraints(null, pool);

            state._fsp--;


                     BENode expr = constraints2;
                     expr = expr.processAtoms();
                     expr.eval(pool, relvarPool, stored, storedI, params);
                     expr.calc_rel().terminationAnalysis();
                   

            match(input, Token.UP, null); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "calc_termination"


    // $ANTLR start "calc_print"
    // CalcT.g:116:1: calc_print[VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params] : ^( PRINT ( constraints[null, pool] | STRSTR )+ ) ;
    public final void calc_print(VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params) throws RecognitionException {
        CommonTree STRSTR4=null;
        BENode constraints3 = null;


        try {
            // CalcT.g:117:3: ( ^( PRINT ( constraints[null, pool] | STRSTR )+ ) )
            // CalcT.g:117:5: ^( PRINT ( constraints[null, pool] | STRSTR )+ )
            {
            match(input,PRINT,FOLLOW_PRINT_in_calc_print264); 

            match(input, Token.DOWN, null); 
            // CalcT.g:118:8: ( constraints[null, pool] | STRSTR )+
            int cnt4=0;
            loop4:
            do {
                int alt4=3;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==AND||(LA4_0>=DOMAIN && LA4_0<=EXISTS)||LA4_0==COMPOSE||(LA4_0>=TRUE && LA4_0<=FALSE)||LA4_0==CONSTRAINT||LA4_0==ID||(LA4_0>=OR && LA4_0<=ABSTR_L)) ) {
                    alt4=1;
                }
                else if ( (LA4_0==STRSTR) ) {
                    alt4=2;
                }


                switch (alt4) {
            	case 1 :
            	    // CalcT.g:118:10: constraints[null, pool]
            	    {
            	    pushFollow(FOLLOW_constraints_in_calc_print276);
            	    constraints3=constraints(null, pool);

            	    state._fsp--;


            	               BENode expr = constraints3;
            	               expr = expr.processAtoms();
            	               expr.eval(pool, relvarPool, stored, storedI, params);
            	               
            	               if (expr.calc_rel() != null)
            	                 System.out.print(expr.calc_rel());
            	               else
            	                 System.out.print(expr.calc_int());
            	             

            	    }
            	    break;
            	case 2 :
            	    // CalcT.g:129:10: STRSTR
            	    {
            	    STRSTR4=(CommonTree)match(input,STRSTR,FOLLOW_STRSTR_in_calc_print300); 

            	               String s = STRSTR4.getToken().getText();
            	               System.out.print(s.substring(1, s.length()-1));
            	             

            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);

             
                     System.out.println();
                   

            match(input, Token.UP, null); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "calc_print"


    // $ANTLR start "calc_print_armc"
    // CalcT.g:141:1: calc_print_armc[VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params] : ^( PRINT_ARMC constraints[null, pool] ) ;
    public final void calc_print_armc(VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params) throws RecognitionException {
        BENode constraints5 = null;


        try {
            // CalcT.g:142:3: ( ^( PRINT_ARMC constraints[null, pool] ) )
            // CalcT.g:142:5: ^( PRINT_ARMC constraints[null, pool] )
            {
            match(input,PRINT_ARMC,FOLLOW_PRINT_ARMC_in_calc_print_armc353); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_constraints_in_calc_print_armc363);
            constraints5=constraints(null, pool);

            state._fsp--;


                       BENode expr = constraints5;
                       expr = expr.processAtoms();
                       expr.eval(pool, relvarPool, stored, storedI, params);
                       
                       int size = expr.calc_rel().disjuncts().size();
                       if (size > 1) {
                         throw new RuntimeException("Rankfinder output for disjunctive relations is not supported.");
                       } else if (size == 0) {
                         System.out.println("relation is false");
                       } else {
                       
                         LinearRel lr = expr.calc_rel().disjuncts().get(0).toLinearRel();
            		
                         Variable[] vars = lr.refVarsUnpPSorted();
                    
                         StringBuffer current = new StringBuffer();
                         StringBuffer next = new StringBuffer();
                         int n = vars.length / 2;
                         for (int i=0; i<n; i++) {
                    	
                     	   current.append(vars[i].toString(Variable.ePRINT_p_armcPref));
                    	   next.append(vars[i+n].toString(Variable.ePRINT_p_armcPref));
                     	 
                    	   if (i != n-1) {
                    		  current.append(",");
                    		  next.append(",");
                            }
                         }
                    
                        StringBuffer lrsb = lr.toSBarmc();
                    
                        String s ="relation(from("+current+"), to("+next+"), constraint(["+lrsb+"]), space(INT), dump('result.txt')).";
                        System.out.println(s);
                      
                      }
                    

            match(input, Token.UP, null); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "calc_print_armc"


    // $ANTLR start "calc_store"
    // CalcT.g:184:1: calc_store[VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params] : ^( ID constraints[null, pool] ) ;
    public final void calc_store(VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params) throws RecognitionException {
        CommonTree ID7=null;
        BENode constraints6 = null;


        try {
            // CalcT.g:185:3: ( ^( ID constraints[null, pool] ) )
            // CalcT.g:185:5: ^( ID constraints[null, pool] )
            {
            ID7=(CommonTree)match(input,ID,FOLLOW_ID_in_calc_store400); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_constraints_in_calc_store402);
            constraints6=constraints(null, pool);

            state._fsp--;


            match(input, Token.UP, null); 

                  BENode expr = constraints6;
                  expr = expr.processAtoms();
                  Set<Variable> pp = expr.eval(pool, relvarPool, stored, storedI, params);
                  stored.put(ID7.getToken().getText(), expr.calc_rel());
                  params.put(ID7.getToken().getText(), pp);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "calc_store"


    // $ANTLR start "calc_prefperiod"
    // CalcT.g:194:1: calc_prefperiod[VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params] : ^( PREFPERIOD id1= ID id2= ID constraints[null, pool] ) ;
    public final void calc_prefperiod(VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params) throws RecognitionException {
        CommonTree id1=null;
        CommonTree id2=null;
        BENode constraints8 = null;


        try {
            // CalcT.g:195:3: ( ^( PREFPERIOD id1= ID id2= ID constraints[null, pool] ) )
            // CalcT.g:195:5: ^( PREFPERIOD id1= ID id2= ID constraints[null, pool] )
            {
            match(input,PREFPERIOD,FOLLOW_PREFPERIOD_in_calc_prefperiod426); 

            match(input, Token.DOWN, null); 
            id1=(CommonTree)match(input,ID,FOLLOW_ID_in_calc_prefperiod430); 
            id2=(CommonTree)match(input,ID,FOLLOW_ID_in_calc_prefperiod434); 
            pushFollow(FOLLOW_constraints_in_calc_prefperiod436);
            constraints8=constraints(null, pool);

            state._fsp--;


            match(input, Token.UP, null); 

                  BENode expr = constraints8;
                  expr = expr.processAtoms();
                  expr.eval(pool, relvarPool, stored, storedI, params);
                  DisjRel dr = expr.calc_rel();
                  PrefixPeriod pp = dr.prefixPeriod();
                  Variable vb = pool.giveVariable(id1.getToken().getText());
                  Variable vc = pool.giveVariable(id2.getToken().getText());
                  storedI.put(vb, pp.b());
                  storedI.put(vc, pp.c());
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "calc_prefperiod"


    // $ANTLR start "constraints"
    // CalcT.g:210:1: constraints[ BENode aPred, VariablePool pool ] returns [ BENode aRet ] : ( ^( AND (c= constraints[$aRet,pool] )+ ) | CL_STAR | CL_PLUS | ^( CL_EXPR terms[pool] ) | ^( CL_STAR ID ) | ^( CL_PLUS ID ) | ABSTR_D | ABSTR_O | ABSTR_L | ^( DOMAIN (c= constraints[$aRet,pool] ) ) | ^( RANGE (c= constraints[$aRet,pool] ) ) | ^( COMPOSE (c= constraints[$aRet,pool] )+ ) | ID | ^( EXISTS ( (id= ID | id= PRIMED_ID ) )+ (c= constraints[$aRet,pool] ) ) | ^( OR (c= constraints[$aRet,pool] )+ ) | ^( NOT (c= constraints[$aRet,pool] ) ) | ^( TRUE ) | ^( FALSE ) | c= constraint[aPred,pool] );
    public final BENode constraints(BENode aPred, VariablePool pool) throws RecognitionException {
        BENode aRet = null;

        CommonTree id=null;
        CommonTree ID10=null;
        CommonTree ID11=null;
        CommonTree ID12=null;
        BENode c = null;

        LinearConstr terms9 = null;


        try {
            // CalcT.g:217:3: ( ^( AND (c= constraints[$aRet,pool] )+ ) | CL_STAR | CL_PLUS | ^( CL_EXPR terms[pool] ) | ^( CL_STAR ID ) | ^( CL_PLUS ID ) | ABSTR_D | ABSTR_O | ABSTR_L | ^( DOMAIN (c= constraints[$aRet,pool] ) ) | ^( RANGE (c= constraints[$aRet,pool] ) ) | ^( COMPOSE (c= constraints[$aRet,pool] )+ ) | ID | ^( EXISTS ( (id= ID | id= PRIMED_ID ) )+ (c= constraints[$aRet,pool] ) ) | ^( OR (c= constraints[$aRet,pool] )+ ) | ^( NOT (c= constraints[$aRet,pool] ) ) | ^( TRUE ) | ^( FALSE ) | c= constraint[aPred,pool] )
            int alt10=19;
            alt10 = dfa10.predict(input);
            switch (alt10) {
                case 1 :
                    // CalcT.g:217:6: ^( AND (c= constraints[$aRet,pool] )+ )
                    {
                    match(input,AND,FOLLOW_AND_in_constraints468); 

                     aRet = new BENode(aPred, BENode.BENodeType.AND); BENode prev1 = null, prev2 = null; 

                    match(input, Token.DOWN, null); 
                    // CalcT.g:218:10: (c= constraints[$aRet,pool] )+
                    int cnt5=0;
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==AND||(LA5_0>=DOMAIN && LA5_0<=EXISTS)||LA5_0==COMPOSE||(LA5_0>=TRUE && LA5_0<=FALSE)||LA5_0==CONSTRAINT||LA5_0==ID||(LA5_0>=OR && LA5_0<=ABSTR_L)) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // CalcT.g:218:13: c= constraints[$aRet,pool]
                    	    {
                    	    pushFollow(FOLLOW_constraints_in_constraints487);
                    	    c=constraints(aRet, pool);

                    	    state._fsp--;

                    	     aRet.addSucc(c); 
                    	               prev2 = prev1; prev1 = c;
                    	               if (prev1.type().isClosure() || prev1.type().isAbstr()) { // change the AST structure to have closures as parents of accelerated or abstracted relations
                    	                 aRet.removeSucc(prev2);
                    	                 prev1.addSucc(prev2);
                    	                 prev2.pred(prev1);
                    	               } 
                    	             

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt5 >= 1 ) break loop5;
                                EarlyExitException eee =
                                    new EarlyExitException(5, input);
                                throw eee;
                        }
                        cnt5++;
                    } while (true);


                    match(input, Token.UP, null); 

                    }
                    break;
                case 2 :
                    // CalcT.g:229:6: CL_STAR
                    {
                    match(input,CL_STAR,FOLLOW_CL_STAR_in_constraints529); 
                     aRet = BENode.calc_closure_star(aPred); 

                    }
                    break;
                case 3 :
                    // CalcT.g:230:6: CL_PLUS
                    {
                    match(input,CL_PLUS,FOLLOW_CL_PLUS_in_constraints538); 
                     aRet = BENode.calc_closure_plus(aPred); 

                    }
                    break;
                case 4 :
                    // CalcT.g:231:6: ^( CL_EXPR terms[pool] )
                    {
                    match(input,CL_EXPR,FOLLOW_CL_EXPR_in_constraints548); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_terms_in_constraints550);
                    terms9=terms(pool);

                    state._fsp--;


                    match(input, Token.UP, null); 
                     aRet = BENode.calc_closure_expr(aPred, terms9); 

                    }
                    break;
                case 5 :
                    // CalcT.g:232:6: ^( CL_STAR ID )
                    {
                    match(input,CL_STAR,FOLLOW_CL_STAR_in_constraints562); 

                    match(input, Token.DOWN, null); 
                    ID10=(CommonTree)match(input,ID,FOLLOW_ID_in_constraints564); 

                    match(input, Token.UP, null); 
                     aRet = BENode.calc_closure_star_n(aPred, pool.giveVariable(ID10.getToken().getText()));
                           //  /*quick hack*/relvarPool.giveVariable(ID10.getToken().getText());
                         

                    }
                    break;
                case 6 :
                    // CalcT.g:236:6: ^( CL_PLUS ID )
                    {
                    match(input,CL_PLUS,FOLLOW_CL_PLUS_in_constraints581); 

                    match(input, Token.DOWN, null); 
                    ID11=(CommonTree)match(input,ID,FOLLOW_ID_in_constraints583); 

                    match(input, Token.UP, null); 
                     aRet = BENode.calc_closure_plus_n(aPred, pool.giveVariable(ID11.getToken().getText()));
                           //  /*quick hack*/relvarPool.giveVariable(ID11.getToken().getText());
                         

                    }
                    break;
                case 7 :
                    // CalcT.g:241:6: ABSTR_D
                    {
                    match(input,ABSTR_D,FOLLOW_ABSTR_D_in_constraints602); 
                     aRet = BENode.abstr_d(aPred); 

                    }
                    break;
                case 8 :
                    // CalcT.g:242:6: ABSTR_O
                    {
                    match(input,ABSTR_O,FOLLOW_ABSTR_O_in_constraints611); 
                     aRet = BENode.abstr_o(aPred); 

                    }
                    break;
                case 9 :
                    // CalcT.g:243:6: ABSTR_L
                    {
                    match(input,ABSTR_L,FOLLOW_ABSTR_L_in_constraints620); 
                     aRet = BENode.abstr_l(aPred); 

                    }
                    break;
                case 10 :
                    // CalcT.g:245:6: ^( DOMAIN (c= constraints[$aRet,pool] ) )
                    {
                    match(input,DOMAIN,FOLLOW_DOMAIN_in_constraints633); 

                     aRet = new BENode(aPred, BENode.BENodeType.DOMAIN); 

                    match(input, Token.DOWN, null); 
                    // CalcT.g:245:72: (c= constraints[$aRet,pool] )
                    // CalcT.g:245:73: c= constraints[$aRet,pool]
                    {
                    pushFollow(FOLLOW_constraints_in_constraints640);
                    c=constraints(aRet, pool);

                    state._fsp--;

                     aRet.addSucc(c); 

                    }


                    match(input, Token.UP, null); 

                    }
                    break;
                case 11 :
                    // CalcT.g:246:6: ^( RANGE (c= constraints[$aRet,pool] ) )
                    {
                    match(input,RANGE,FOLLOW_RANGE_in_constraints655); 

                     aRet = new BENode(aPred, BENode.BENodeType.RANGE); 

                    match(input, Token.DOWN, null); 
                    // CalcT.g:246:70: (c= constraints[$aRet,pool] )
                    // CalcT.g:246:71: c= constraints[$aRet,pool]
                    {
                    pushFollow(FOLLOW_constraints_in_constraints662);
                    c=constraints(aRet, pool);

                    state._fsp--;

                     aRet.addSucc(c); 

                    }


                    match(input, Token.UP, null); 

                    }
                    break;
                case 12 :
                    // CalcT.g:248:6: ^( COMPOSE (c= constraints[$aRet,pool] )+ )
                    {
                    match(input,COMPOSE,FOLLOW_COMPOSE_in_constraints680); 

                     aRet = new BENode(aPred, BENode.BENodeType.COMPOSE); 

                    match(input, Token.DOWN, null); 
                    // CalcT.g:248:74: (c= constraints[$aRet,pool] )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==AND||(LA6_0>=DOMAIN && LA6_0<=EXISTS)||LA6_0==COMPOSE||(LA6_0>=TRUE && LA6_0<=FALSE)||LA6_0==CONSTRAINT||LA6_0==ID||(LA6_0>=OR && LA6_0<=ABSTR_L)) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // CalcT.g:248:75: c= constraints[$aRet,pool]
                    	    {
                    	    pushFollow(FOLLOW_constraints_in_constraints687);
                    	    c=constraints(aRet, pool);

                    	    state._fsp--;

                    	     aRet.addSucc(c); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt6 >= 1 ) break loop6;
                                EarlyExitException eee =
                                    new EarlyExitException(6, input);
                                throw eee;
                        }
                        cnt6++;
                    } while (true);


                    match(input, Token.UP, null); 

                    }
                    break;
                case 13 :
                    // CalcT.g:249:6: ID
                    {
                    ID12=(CommonTree)match(input,ID,FOLLOW_ID_in_constraints702); 
                     aRet = BENode.calc_id(aPred, ID12.getToken().getText()); 

                    }
                    break;
                case 14 :
                    // CalcT.g:250:6: ^( EXISTS ( (id= ID | id= PRIMED_ID ) )+ (c= constraints[$aRet,pool] ) )
                    {
                    match(input,EXISTS,FOLLOW_EXISTS_in_constraints712); 

                    List<Variable> vars = new LinkedList<Variable>();

                    match(input, Token.DOWN, null); 
                    // CalcT.g:252:7: ( (id= ID | id= PRIMED_ID ) )+
                    int cnt8=0;
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( (LA8_0==ID) ) {
                            int LA8_2 = input.LA(2);

                            if ( (LA8_2==AND||(LA8_2>=DOMAIN && LA8_2<=EXISTS)||LA8_2==COMPOSE||(LA8_2>=TRUE && LA8_2<=FALSE)||LA8_2==CONSTRAINT||(LA8_2>=ID && LA8_2<=ABSTR_L)) ) {
                                alt8=1;
                            }


                        }
                        else if ( (LA8_0==PRIMED_ID) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // CalcT.g:252:9: (id= ID | id= PRIMED_ID )
                    	    {
                    	    // CalcT.g:252:9: (id= ID | id= PRIMED_ID )
                    	    int alt7=2;
                    	    int LA7_0 = input.LA(1);

                    	    if ( (LA7_0==ID) ) {
                    	        alt7=1;
                    	    }
                    	    else if ( (LA7_0==PRIMED_ID) ) {
                    	        alt7=2;
                    	    }
                    	    else {
                    	        NoViableAltException nvae =
                    	            new NoViableAltException("", 7, 0, input);

                    	        throw nvae;
                    	    }
                    	    switch (alt7) {
                    	        case 1 :
                    	            // CalcT.g:252:10: id= ID
                    	            {
                    	            id=(CommonTree)match(input,ID,FOLLOW_ID_in_constraints735); 

                    	            }
                    	            break;
                    	        case 2 :
                    	            // CalcT.g:252:18: id= PRIMED_ID
                    	            {
                    	            id=(CommonTree)match(input,PRIMED_ID,FOLLOW_PRIMED_ID_in_constraints741); 

                    	            }
                    	            break;

                    	    }

                    	     vars.add(pool.giveVariable(id.getToken().getText()));
                    	              /*quick hack*/relvarPool.giveVariable(id.getToken().getText());
                    	            

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt8 >= 1 ) break loop8;
                                EarlyExitException eee =
                                    new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);

                     aRet = BENode.calc_exists(aPred, vars); 
                    // CalcT.g:258:7: (c= constraints[$aRet,pool] )
                    // CalcT.g:258:8: c= constraints[$aRet,pool]
                    {
                    pushFollow(FOLLOW_constraints_in_constraints784);
                    c=constraints(aRet, pool);

                    state._fsp--;

                     aRet.addSucc(c); 

                    }


                    match(input, Token.UP, null); 

                    }
                    break;
                case 15 :
                    // CalcT.g:260:6: ^( OR (c= constraints[$aRet,pool] )+ )
                    {
                    match(input,OR,FOLLOW_OR_in_constraints802); 

                     aRet = new BENode(aPred, BENode.BENodeType.OR); 

                    match(input, Token.DOWN, null); 
                    // CalcT.g:260:64: (c= constraints[$aRet,pool] )+
                    int cnt9=0;
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( (LA9_0==AND||(LA9_0>=DOMAIN && LA9_0<=EXISTS)||LA9_0==COMPOSE||(LA9_0>=TRUE && LA9_0<=FALSE)||LA9_0==CONSTRAINT||LA9_0==ID||(LA9_0>=OR && LA9_0<=ABSTR_L)) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // CalcT.g:260:65: c= constraints[$aRet,pool]
                    	    {
                    	    pushFollow(FOLLOW_constraints_in_constraints809);
                    	    c=constraints(aRet, pool);

                    	    state._fsp--;

                    	     aRet.addSucc(c); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt9 >= 1 ) break loop9;
                                EarlyExitException eee =
                                    new EarlyExitException(9, input);
                                throw eee;
                        }
                        cnt9++;
                    } while (true);


                    match(input, Token.UP, null); 

                    }
                    break;
                case 16 :
                    // CalcT.g:261:6: ^( NOT (c= constraints[$aRet,pool] ) )
                    {
                    match(input,NOT,FOLLOW_NOT_in_constraints825); 

                     aRet = new BENode(aPred, BENode.BENodeType.NOT); 

                    match(input, Token.DOWN, null); 
                    // CalcT.g:261:66: (c= constraints[$aRet,pool] )
                    // CalcT.g:261:67: c= constraints[$aRet,pool]
                    {
                    pushFollow(FOLLOW_constraints_in_constraints832);
                    c=constraints(aRet, pool);

                    state._fsp--;

                     aRet.addSucc(c); 

                    }


                    match(input, Token.UP, null); 

                    }
                    break;
                case 17 :
                    // CalcT.g:262:6: ^( TRUE )
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_constraints847); 

                     aRet = new BENode(aPred, BENode.BENodeType.TRUE); 

                    if ( input.LA(1)==Token.DOWN ) {
                        match(input, Token.DOWN, null); 
                        match(input, Token.UP, null); 
                    }

                    }
                    break;
                case 18 :
                    // CalcT.g:263:6: ^( FALSE )
                    {
                    match(input,FALSE,FOLLOW_FALSE_in_constraints859); 

                     aRet = new BENode(aPred, BENode.BENodeType.FALSE); 

                    if ( input.LA(1)==Token.DOWN ) {
                        match(input, Token.DOWN, null); 
                        match(input, Token.UP, null); 
                    }

                    }
                    break;
                case 19 :
                    // CalcT.g:264:6: c= constraint[aPred,pool]
                    {
                    pushFollow(FOLLOW_constraint_in_constraints872);
                    c=constraint(aPred, pool);

                    state._fsp--;

                     aRet = c; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return aRet;
    }
    // $ANTLR end "constraints"


    // $ANTLR start "constraint"
    // CalcT.g:267:1: constraint[BENode aPred, VariablePool pool] returns [BENode aRet] : ^( CONSTRAINT tlhs= terms[pool] (op= EQ | op= NEQ | op= LEQ | op= LESS | op= GEQ | op= GREATER | op= DIVIDES ) trhs= terms[pool] ) ;
    public final BENode constraint(BENode aPred, VariablePool pool) throws RecognitionException {
        BENode aRet = null;

        CommonTree op=null;
        LinearConstr tlhs = null;

        LinearConstr trhs = null;



            LinearConstr lhs = new LinearConstr();
            LinearConstr rhs = new LinearConstr();
            int operator;
          
        try {
            // CalcT.g:278:3: ( ^( CONSTRAINT tlhs= terms[pool] (op= EQ | op= NEQ | op= LEQ | op= LESS | op= GEQ | op= GREATER | op= DIVIDES ) trhs= terms[pool] ) )
            // CalcT.g:278:5: ^( CONSTRAINT tlhs= terms[pool] (op= EQ | op= NEQ | op= LEQ | op= LESS | op= GEQ | op= GREATER | op= DIVIDES ) trhs= terms[pool] )
            {
            match(input,CONSTRAINT,FOLLOW_CONSTRAINT_in_constraint908); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_terms_in_constraint918);
            tlhs=terms(pool);

            state._fsp--;

            // CalcT.g:280:7: (op= EQ | op= NEQ | op= LEQ | op= LESS | op= GEQ | op= GREATER | op= DIVIDES )
            int alt11=7;
            switch ( input.LA(1) ) {
            case EQ:
                {
                alt11=1;
                }
                break;
            case NEQ:
                {
                alt11=2;
                }
                break;
            case LEQ:
                {
                alt11=3;
                }
                break;
            case LESS:
                {
                alt11=4;
                }
                break;
            case GEQ:
                {
                alt11=5;
                }
                break;
            case GREATER:
                {
                alt11=6;
                }
                break;
            case DIVIDES:
                {
                alt11=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // CalcT.g:280:9: op= EQ
                    {
                    op=(CommonTree)match(input,EQ,FOLLOW_EQ_in_constraint932); 

                    }
                    break;
                case 2 :
                    // CalcT.g:280:15: op= NEQ
                    {
                    op=(CommonTree)match(input,NEQ,FOLLOW_NEQ_in_constraint936); 

                    }
                    break;
                case 3 :
                    // CalcT.g:280:22: op= LEQ
                    {
                    op=(CommonTree)match(input,LEQ,FOLLOW_LEQ_in_constraint940); 

                    }
                    break;
                case 4 :
                    // CalcT.g:280:29: op= LESS
                    {
                    op=(CommonTree)match(input,LESS,FOLLOW_LESS_in_constraint944); 

                    }
                    break;
                case 5 :
                    // CalcT.g:280:37: op= GEQ
                    {
                    op=(CommonTree)match(input,GEQ,FOLLOW_GEQ_in_constraint948); 

                    }
                    break;
                case 6 :
                    // CalcT.g:280:44: op= GREATER
                    {
                    op=(CommonTree)match(input,GREATER,FOLLOW_GREATER_in_constraint952); 

                    }
                    break;
                case 7 :
                    // CalcT.g:280:55: op= DIVIDES
                    {
                    op=(CommonTree)match(input,DIVIDES,FOLLOW_DIVIDES_in_constraint956); 

                    }
                    break;

            }


                    operator = op.getToken().getType();
                    if (operator == DIVIDES) {
                      LinearTerm con = tlhs.get(null);
                      if (con == null || tlhs.size() != 1) {
                        System.err.println("Divisor must be a number, not ("+tlhs+")");
                        System.exit(-1);
                      }
                    }
                  
            pushFollow(FOLLOW_terms_in_constraint976);
            trhs=terms(pool);

            state._fsp--;


                    lhs = tlhs;
                    rhs = trhs;
                  

            match(input, Token.UP, null); 

            }


                BENode.ASTConstrType auxT = parsertype2asttype(operator);
                BENode.ASTConstr aux = new BENode.ASTConstr(lhs,rhs,auxT);
                aRet = new BENode(aPred,aux);
              
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return aRet;
    }
    // $ANTLR end "constraint"


    // $ANTLR start "terms"
    // CalcT.g:299:1: terms[VariablePool pool] returns [LinearConstr rConstr] : ( ^( PLUS ( PLUS | ( MINUS ) )? t1= terms[pool] ( ( PLUS | ( MINUS ) ) t2= terms[pool] )* ) | ^( MULT t1= terms[pool] (t2= terms[pool] )* ) | (f= ID | f= PRIMED_ID ) | CONST );
    public final LinearConstr terms(VariablePool pool) throws RecognitionException {
        LinearConstr rConstr = null;

        CommonTree f=null;
        CommonTree CONST13=null;
        LinearConstr t1 = null;

        LinearConstr t2 = null;



            rConstr = new LinearConstr();
          
        try {
            // CalcT.g:303:3: ( ^( PLUS ( PLUS | ( MINUS ) )? t1= terms[pool] ( ( PLUS | ( MINUS ) ) t2= terms[pool] )* ) | ^( MULT t1= terms[pool] (t2= terms[pool] )* ) | (f= ID | f= PRIMED_ID ) | CONST )
            int alt17=4;
            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt17=1;
                }
                break;
            case MULT:
                {
                alt17=2;
                }
                break;
            case ID:
            case PRIMED_ID:
                {
                alt17=3;
                }
                break;
            case CONST:
                {
                alt17=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }

            switch (alt17) {
                case 1 :
                    // CalcT.g:303:5: ^( PLUS ( PLUS | ( MINUS ) )? t1= terms[pool] ( ( PLUS | ( MINUS ) ) t2= terms[pool] )* )
                    {
                    match(input,PLUS,FOLLOW_PLUS_in_terms1020); 

                    boolean m=false;

                    match(input, Token.DOWN, null); 
                    // CalcT.g:303:31: ( PLUS | ( MINUS ) )?
                    int alt12=3;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==PLUS) ) {
                        int LA12_1 = input.LA(2);

                        if ( (LA12_1==PLUS||LA12_1==MULT||(LA12_1>=ID && LA12_1<=PRIMED_ID)||LA12_1==CONST) ) {
                            alt12=1;
                        }
                    }
                    else if ( (LA12_0==MINUS) ) {
                        alt12=2;
                    }
                    switch (alt12) {
                        case 1 :
                            // CalcT.g:303:32: PLUS
                            {
                            match(input,PLUS,FOLLOW_PLUS_in_terms1025); 

                            }
                            break;
                        case 2 :
                            // CalcT.g:303:39: ( MINUS )
                            {
                            // CalcT.g:303:39: ( MINUS )
                            // CalcT.g:303:40: MINUS
                            {
                            match(input,MINUS,FOLLOW_MINUS_in_terms1030); 
                            m=true;

                            }


                            }
                            break;

                    }

                    pushFollow(FOLLOW_terms_in_terms1039);
                    t1=terms(pool);

                    state._fsp--;


                             rConstr = t1;
                             if (m) {
                               rConstr = rConstr.times(-1);
                             }
                           
                    // CalcT.g:310:8: ( ( PLUS | ( MINUS ) ) t2= terms[pool] )*
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( ((LA14_0>=PLUS && LA14_0<=MINUS)) ) {
                            alt14=1;
                        }


                        switch (alt14) {
                    	case 1 :
                    	    // CalcT.g:311:10: ( PLUS | ( MINUS ) ) t2= terms[pool]
                    	    {
                    	    // CalcT.g:311:10: ( PLUS | ( MINUS ) )
                    	    int alt13=2;
                    	    int LA13_0 = input.LA(1);

                    	    if ( (LA13_0==PLUS) ) {
                    	        alt13=1;
                    	    }
                    	    else if ( (LA13_0==MINUS) ) {
                    	        alt13=2;
                    	    }
                    	    else {
                    	        NoViableAltException nvae =
                    	            new NoViableAltException("", 13, 0, input);

                    	        throw nvae;
                    	    }
                    	    switch (alt13) {
                    	        case 1 :
                    	            // CalcT.g:311:11: PLUS
                    	            {
                    	            match(input,PLUS,FOLLOW_PLUS_in_terms1073); 
                    	            m=false;

                    	            }
                    	            break;
                    	        case 2 :
                    	            // CalcT.g:311:29: ( MINUS )
                    	            {
                    	            // CalcT.g:311:29: ( MINUS )
                    	            // CalcT.g:311:30: MINUS
                    	            {
                    	            match(input,MINUS,FOLLOW_MINUS_in_terms1080); 
                    	            m=true;

                    	            }


                    	            }
                    	            break;

                    	    }

                    	    pushFollow(FOLLOW_terms_in_terms1089);
                    	    t2=terms(pool);

                    	    state._fsp--;


                    	                 LinearConstr aux =  t2;
                    	                 if (m) {
                    	                   aux = aux.times(-1);
                    	                 }
                    	                 rConstr.addLinTerms(aux);
                    	               

                    	    }
                    	    break;

                    	default :
                    	    break loop14;
                        }
                    } while (true);


                    match(input, Token.UP, null); 

                    }
                    break;
                case 2 :
                    // CalcT.g:321:5: ^( MULT t1= terms[pool] (t2= terms[pool] )* )
                    {
                    match(input,MULT,FOLLOW_MULT_in_terms1129); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_terms_in_terms1133);
                    t1=terms(pool);

                    state._fsp--;

                     rConstr = t1; 
                    // CalcT.g:323:8: (t2= terms[pool] )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( (LA15_0==PLUS||LA15_0==MULT||(LA15_0>=ID && LA15_0<=PRIMED_ID)||LA15_0==CONST) ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // CalcT.g:324:10: t2= terms[pool]
                    	    {
                    	    pushFollow(FOLLOW_terms_in_terms1167);
                    	    t2=terms(pool);

                    	    state._fsp--;


                    	               LinearConstr old = rConstr;
                    	               rConstr = old.times(t2);
                    	               checkLinTerm(rConstr, old, t2);
                    	             

                    	    }
                    	    break;

                    	default :
                    	    break loop15;
                        }
                    } while (true);


                    match(input, Token.UP, null); 

                    }
                    break;
                case 3 :
                    // CalcT.g:332:5: (f= ID | f= PRIMED_ID )
                    {
                    // CalcT.g:332:5: (f= ID | f= PRIMED_ID )
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==ID) ) {
                        alt16=1;
                    }
                    else if ( (LA16_0==PRIMED_ID) ) {
                        alt16=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 16, 0, input);

                        throw nvae;
                    }
                    switch (alt16) {
                        case 1 :
                            // CalcT.g:332:6: f= ID
                            {
                            f=(CommonTree)match(input,ID,FOLLOW_ID_in_terms1207); 

                            }
                            break;
                        case 2 :
                            // CalcT.g:332:13: f= PRIMED_ID
                            {
                            f=(CommonTree)match(input,PRIMED_ID,FOLLOW_PRIMED_ID_in_terms1213); 

                            }
                            break;

                    }


                          /*quick hack*/relvarPool.giveVariable(getTextFromNode(f));
                          Variable var = pool.giveVariable(getTextFromNode(f));
                          rConstr.addLinTerm(new LinearTerm(var,1));
                        

                    }
                    break;
                case 4 :
                    // CalcT.g:338:5: CONST
                    {
                    CONST13=(CommonTree)match(input,CONST,FOLLOW_CONST_in_terms1227); 

                          int coef = java.lang.Integer.parseInt(CONST13.getToken().getText());
                          rConstr.addLinTerm(new LinearTerm(null,coef));
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return rConstr;
    }
    // $ANTLR end "terms"

    // Delegated rules


    protected DFA10 dfa10 = new DFA10(this);
    static final String DFA10_eotS =
        "\26\uffff";
    static final String DFA10_eofS =
        "\26\uffff";
    static final String DFA10_minS =
        "\1\7\1\uffff\2\2\22\uffff";
    static final String DFA10_maxS =
        "\1\72\1\uffff\2\111\22\uffff";
    static final String DFA10_acceptS =
        "\1\uffff\1\1\2\uffff\1\4\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16"+
        "\1\17\1\20\1\21\1\22\1\23\1\5\1\2\1\6\1\3";
    static final String DFA10_specialS =
        "\26\uffff}>";
    static final String[] DFA10_transitionS = {
            "\1\1\3\uffff\1\10\1\11\1\14\2\uffff\1\12\4\uffff\1\17\1\20\26"+
            "\uffff\1\21\3\uffff\1\13\1\uffff\1\15\1\16\1\3\1\2\1\4\1\5\1"+
            "\6\1\7",
            "",
            "\1\22\1\23\3\uffff\1\23\3\uffff\3\23\2\uffff\1\23\4\uffff\2"+
            "\23\26\uffff\1\23\3\uffff\1\23\1\uffff\10\23\16\uffff\1\23",
            "\1\24\1\25\3\uffff\1\25\3\uffff\3\25\2\uffff\1\25\4\uffff\2"+
            "\25\26\uffff\1\25\3\uffff\1\25\1\uffff\10\25\16\uffff\1\25",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
    static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
    static final char[] DFA10_min = DFA.unpackEncodedStringToUnsignedChars(DFA10_minS);
    static final char[] DFA10_max = DFA.unpackEncodedStringToUnsignedChars(DFA10_maxS);
    static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
    static final short[] DFA10_special = DFA.unpackEncodedString(DFA10_specialS);
    static final short[][] DFA10_transition;

    static {
        int numStates = DFA10_transitionS.length;
        DFA10_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
        }
    }

    class DFA10 extends DFA {

        public DFA10(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 10;
            this.eot = DFA10_eot;
            this.eof = DFA10_eof;
            this.min = DFA10_min;
            this.max = DFA10_max;
            this.accept = DFA10_accept;
            this.special = DFA10_special;
            this.transition = DFA10_transition;
        }
        public String getDescription() {
            return "210:1: constraints[ BENode aPred, VariablePool pool ] returns [ BENode aRet ] : ( ^( AND (c= constraints[$aRet,pool] )+ ) | CL_STAR | CL_PLUS | ^( CL_EXPR terms[pool] ) | ^( CL_STAR ID ) | ^( CL_PLUS ID ) | ABSTR_D | ABSTR_O | ABSTR_L | ^( DOMAIN (c= constraints[$aRet,pool] ) ) | ^( RANGE (c= constraints[$aRet,pool] ) ) | ^( COMPOSE (c= constraints[$aRet,pool] )+ ) | ID | ^( EXISTS ( (id= ID | id= PRIMED_ID ) )+ (c= constraints[$aRet,pool] ) ) | ^( OR (c= constraints[$aRet,pool] )+ ) | ^( NOT (c= constraints[$aRet,pool] ) ) | ^( TRUE ) | ^( FALSE ) | c= constraint[aPred,pool] );";
        }
    }
 

    public static final BitSet FOLLOW_constrInput_in_constrsInput64 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_CONSTR_INPUT_in_constrInput97 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_constrInput109 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_calc_print_in_calc151 = new BitSet(new long[]{0x000200000000C602L});
    public static final BitSet FOLLOW_calc_print_armc_in_calc162 = new BitSet(new long[]{0x000200000000C602L});
    public static final BitSet FOLLOW_calc_store_in_calc175 = new BitSet(new long[]{0x000200000000C602L});
    public static final BitSet FOLLOW_calc_prefperiod_in_calc186 = new BitSet(new long[]{0x000200000000C602L});
    public static final BitSet FOLLOW_calc_termination_in_calc197 = new BitSet(new long[]{0x000200000000C602L});
    public static final BitSet FOLLOW_TERMINATION_in_calc_termination221 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_calc_termination231 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_PRINT_in_calc_print264 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_calc_print276 = new BitSet(new long[]{0x07FA200000613888L,0x0000000000000200L});
    public static final BitSet FOLLOW_STRSTR_in_calc_print300 = new BitSet(new long[]{0x07FA200000613888L,0x0000000000000200L});
    public static final BitSet FOLLOW_PRINT_ARMC_in_calc_print_armc353 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_calc_print_armc363 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ID_in_calc_store400 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_calc_store402 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_PREFPERIOD_in_calc_prefperiod426 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_calc_prefperiod430 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_ID_in_calc_prefperiod434 = new BitSet(new long[]{0x07FA200000613880L});
    public static final BitSet FOLLOW_constraints_in_calc_prefperiod436 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_AND_in_constraints468 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_constraints487 = new BitSet(new long[]{0x07FA200000613888L});
    public static final BitSet FOLLOW_CL_STAR_in_constraints529 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CL_PLUS_in_constraints538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CL_EXPR_in_constraints548 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_terms_in_constraints550 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CL_STAR_in_constraints562 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_constraints564 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_CL_PLUS_in_constraints581 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_constraints583 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ABSTR_D_in_constraints602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ABSTR_O_in_constraints611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ABSTR_L_in_constraints620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOMAIN_in_constraints633 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_constraints640 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_RANGE_in_constraints655 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_constraints662 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_COMPOSE_in_constraints680 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_constraints687 = new BitSet(new long[]{0x07FA200000613888L});
    public static final BitSet FOLLOW_ID_in_constraints702 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXISTS_in_constraints712 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_constraints735 = new BitSet(new long[]{0x07FE200000613880L});
    public static final BitSet FOLLOW_PRIMED_ID_in_constraints741 = new BitSet(new long[]{0x07FE200000613880L});
    public static final BitSet FOLLOW_constraints_in_constraints784 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_OR_in_constraints802 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_constraints809 = new BitSet(new long[]{0x07FA200000613888L});
    public static final BitSet FOLLOW_NOT_in_constraints825 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraints_in_constraints832 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TRUE_in_constraints847 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_FALSE_in_constraints859 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_constraint_in_constraints872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONSTRAINT_in_constraint908 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_terms_in_constraint918 = new BitSet(new long[]{0xF800000000000100L,0x0000000000000001L});
    public static final BitSet FOLLOW_EQ_in_constraint932 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_NEQ_in_constraint936 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_LEQ_in_constraint940 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_constraint944 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_GEQ_in_constraint948 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_constraint952 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_DIVIDES_in_constraint956 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_terms_in_constraint976 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_PLUS_in_terms1020 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_PLUS_in_terms1025 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_terms1030 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_terms_in_terms1039 = new BitSet(new long[]{0x0000000000000038L});
    public static final BitSet FOLLOW_PLUS_in_terms1073 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_terms1080 = new BitSet(new long[]{0x0006000000000050L,0x0000000000000002L});
    public static final BitSet FOLLOW_terms_in_terms1089 = new BitSet(new long[]{0x0000000000000038L});
    public static final BitSet FOLLOW_MULT_in_terms1129 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_terms_in_terms1133 = new BitSet(new long[]{0x0006000000000058L,0x0000000000000002L});
    public static final BitSet FOLLOW_terms_in_terms1167 = new BitSet(new long[]{0x0006000000000058L,0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_terms1207 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRIMED_ID_in_terms1213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONST_in_terms1227 = new BitSet(new long[]{0x0000000000000002L});

}