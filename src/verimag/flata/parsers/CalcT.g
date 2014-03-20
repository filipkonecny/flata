tree grammar CalcT;

options { 
  ASTLabelType = CommonTree;

  tokenVocab = Calc;
}

// @SuppressWarnings({"unchecked", "unused"})
@header {
  package verimag.flata.parsers;
  
  import java.io.File;
  import org.antlr.runtime.BitSet;
  import java.util.*;
  
  import verimag.flata.presburger.*;

}

@members {

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

}


constrsInput [VariablePool pool] returns [ List<ModuloRel> ret ]
	:
	{ ret = new LinkedList<ModuloRel>(); }
	( constrInput [pool] 
	  { ret.addAll($constrInput.ret); } 
	)*
	;

constrInput [VariablePool pool] returns [ List<ModuloRel> ret ]
  : ^( CONSTR_INPUT 
      (c=constraints[null,pool])?)
      { 
        BENode root = $c.aRet;
        BENode dnfRoot = BENode.normalize(root);
        ret = dnfRoot.dnf2Rels();
      }
  ;


calc [VariablePool pool]
  @init{
  	Map<String,DisjRel> stored = new HashMap<String,DisjRel>();
  	Map<Variable,Integer> storedI = new HashMap<Variable,Integer>();
  	Map<String,Set<Variable>> params = new HashMap<String,Set<Variable>>();
  }
  : (
      calc_print[pool, stored, storedI, params]
      | calc_print_armc[pool, stored, storedI, params]  
      | calc_store[pool, stored, storedI, params]
      | calc_prefperiod[pool, stored, storedI, params]
      | calc_termination[pool, stored, storedI, params]
    )*
  ;

calc_termination [VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params]
  : ^(TERMINATION 
       constraints[null, pool] 
       {
         BENode expr = $constraints.aRet;
         expr = expr.processAtoms();
         expr.eval(pool, relvarPool, stored, storedI, params);
         expr.calc_rel().terminationAnalysis();
       }
     )
  ;
calc_print [VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params]
  : ^(PRINT 
       ( constraints[null, pool] 
         {
           BENode expr = $constraints.aRet;
           expr = expr.processAtoms();
           expr.eval(pool, relvarPool, stored, storedI, params);
           
           if (expr.calc_rel() != null)
             System.out.print(expr.calc_rel());
           else
             System.out.print(expr.calc_int());
         }
       | STRSTR
         {
           String s = $STRSTR.getToken().getText();
           System.out.print(s.substring(1, s.length()-1));
         }
       )+
       { 
         System.out.println();
       }
     )
  ;

calc_print_armc [VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params]
  : ^(PRINT_ARMC 
       constraints[null, pool] 
         {
           BENode expr = $constraints.aRet;
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
        }
      )
  ;

calc_store [VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params]
  : ^(ID constraints[null, pool]) 
    {
      BENode expr = $constraints.aRet;
      expr = expr.processAtoms();
      Set<Variable> pp = expr.eval(pool, relvarPool, stored, storedI, params);
      stored.put($ID.getToken().getText(), expr.calc_rel());
      params.put($ID.getToken().getText(), pp);
    }
  ;
calc_prefperiod [VariablePool pool, Map<String,DisjRel> stored, Map<Variable,Integer> storedI, Map<String,Set<Variable>> params]
  : ^(PREFPERIOD id1=ID id2=ID constraints[null, pool])
    {
      BENode expr = $constraints.aRet;
      expr = expr.processAtoms();
      expr.eval(pool, relvarPool, stored, storedI, params);
      DisjRel dr = expr.calc_rel();
      PrefixPeriod pp = dr.prefixPeriod();
      Variable vb = pool.giveVariable($id1.getToken().getText());
      Variable vc = pool.giveVariable($id2.getToken().getText());
      storedI.put(vb, pp.b());
      storedI.put(vc, pp.c());
    }
  ;


/** 
 * closure nodes in ASTs are always children of AND
 *    AND(.. ek CLOSURE ABSTR1 .. ABSTRn ...)
 *    -->
 *    AND(.. ABSTRn(..(ABSTR1(CLOSURE(ek))) ...) 
 */
constraints [ BENode aPred, VariablePool pool ] returns [ BENode aRet ]
  :  ^(AND { $aRet = new BENode(aPred, BENode.BENodeType.AND); BENode prev1 = null, prev2 = null; } 
         (  c=constraints[$aRet,pool] 
         { $aRet.addSucc($c.aRet); 
           prev2 = prev1; prev1 = $c.aRet;
           if (prev1.type().isClosure() || prev1.type().isAbstr()) { // change the AST structure to have closures as parents of accelerated or abstracted relations
             $aRet.removeSucc(prev2);
             prev1.addSucc(prev2);
             prev2.pred(prev1);
           } 
         } 
         )+ 
      )
  |  CL_STAR { $aRet = BENode.calc_closure_star(aPred); }
  |  CL_PLUS { $aRet = BENode.calc_closure_plus(aPred); }
  |  ^(CL_EXPR terms[pool]) { $aRet = BENode.calc_closure_expr(aPred, $terms.rConstr); }
  |  ^(CL_STAR ID) 
     { $aRet = BENode.calc_closure_star_n(aPred, pool.giveVariable($ID.getToken().getText()));
       //  /*quick hack*/relvarPool.giveVariable($ID.getToken().getText());
     }
  |  ^(CL_PLUS ID) 
     { $aRet = BENode.calc_closure_plus_n(aPred, pool.giveVariable($ID.getToken().getText()));
       //  /*quick hack*/relvarPool.giveVariable($ID.getToken().getText());
     }
  
  |  ABSTR_D { $aRet = BENode.abstr_d(aPred); }
  |  ABSTR_O { $aRet = BENode.abstr_o(aPred); }
  |  ABSTR_L { $aRet = BENode.abstr_l(aPred); }
  
  |  ^(DOMAIN { $aRet = new BENode(aPred, BENode.BENodeType.DOMAIN); } (c=constraints[$aRet,pool] { $aRet.addSucc($c.aRet); } ) )
  |  ^(RANGE { $aRet = new BENode(aPred, BENode.BENodeType.RANGE); } (c=constraints[$aRet,pool] { $aRet.addSucc($c.aRet); } ) )
  
  |  ^(COMPOSE { $aRet = new BENode(aPred, BENode.BENodeType.COMPOSE); } (c=constraints[$aRet,pool] { $aRet.addSucc($c.aRet); } )+ )
  |  ID { $aRet = BENode.calc_id(aPred, $ID.getToken().getText()); }
  |  ^(EXISTS 
      {List<Variable> vars = new LinkedList<Variable>();} 
      ( (id=ID | id=PRIMED_ID) 
        { vars.add(pool.giveVariable($ID.getToken().getText()));
          /*quick hack*/relvarPool.giveVariable($ID.getToken().getText());
        } 
      )+ 
      { $aRet = BENode.calc_exists(aPred, vars); } 
      (c=constraints[$aRet,pool] { $aRet.addSucc($c.aRet); } ) )
  
  |  ^(OR { $aRet = new BENode(aPred, BENode.BENodeType.OR); } (c=constraints[$aRet,pool] { $aRet.addSucc($c.aRet); } )+ )
  |  ^(NOT { $aRet = new BENode(aPred, BENode.BENodeType.NOT); } (c=constraints[$aRet,pool] { $aRet.addSucc($c.aRet); } ) )
  |  ^(TRUE { $aRet = new BENode(aPred, BENode.BENodeType.TRUE); } )
  |  ^(FALSE { $aRet = new BENode(aPred, BENode.BENodeType.FALSE); } )
  |  c=constraint[aPred,pool] { $aRet = $c.aRet; }
  ;

constraint [BENode aPred, VariablePool pool] returns [BENode aRet]
  @init{
    LinearConstr lhs = new LinearConstr();
    LinearConstr rhs = new LinearConstr();
    int operator;
  }
  @after {
    BENode.ASTConstrType auxT = parsertype2asttype(operator);
    BENode.ASTConstr aux = new BENode.ASTConstr(lhs,rhs,auxT);
    $aRet = new BENode(aPred,aux);
  }
  : ^(CONSTRAINT
      tlhs=terms [pool]
      ( op=EQ|op=NEQ|op=LEQ|op=LESS|op=GEQ|op=GREATER|op=DIVIDES )
      {
        operator = $op.getToken().getType();
        if (operator == DIVIDES) {
          LinearTerm con = $tlhs.rConstr.get(null);
          if (con == null || $tlhs.rConstr.size() != 1) {
            System.err.println("Divisor must be a number, not ("+$tlhs.rConstr+")");
            System.exit(-1);
          }
        }
      }
      trhs=terms [pool]
      {
        lhs = $tlhs.rConstr;
        rhs = $trhs.rConstr;
      }
     )
  ;

terms [VariablePool pool] returns [LinearConstr rConstr]
  @init{
    $rConstr = new LinearConstr();
  } 
  : ^(PLUS {boolean m=false;} (PLUS | (MINUS {m=true;}))? t1=terms [pool]
       {
         $rConstr = $t1.rConstr;
         if (m) {
           $rConstr = $rConstr.times(-1);
         }
       } 
       ( 
         (PLUS {m=false;} | (MINUS {m=true;}) ) t2=terms [pool] 
           {
             LinearConstr aux =  $t2.rConstr;
             if (m) {
               aux = aux.times(-1);
             }
             $rConstr.addLinTerms(aux);
           }
       )*
     )
  | ^(MULT t1=terms [pool]
       { $rConstr = $t1.rConstr; }
       ( 
         t2=terms [pool]
         {
           LinearConstr old = $rConstr;
           $rConstr = old.times($t2.rConstr);
           checkLinTerm($rConstr, old, $t2.rConstr);
         } 
       )*
     )
  | (f=ID | f=PRIMED_ID) 
    {
      /*quick hack*/relvarPool.giveVariable(getTextFromNode(f));
      Variable var = pool.giveVariable(getTextFromNode(f));
      rConstr.addLinTerm(new LinearTerm(var,1));
    }
  | CONST 
    {
      int coef = java.lang.Integer.parseInt($CONST.getToken().getText());
      rConstr.addLinTerm(new LinearTerm(null,coef));
    }
  ;
