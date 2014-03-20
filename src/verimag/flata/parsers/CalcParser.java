// $ANTLR 3.3 Nov 30, 2010 12:50:56 Calc.g 2013-11-11 16:03:47

	package verimag.flata.parsers;

	//import verimag.flata.parsers.CALexer;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

@SuppressWarnings("unused")
public class CalcParser extends Parser {
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


        public CalcParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public CalcParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return CalcParser.tokenNames; }
    public String getGrammarFileName() { return "Calc.g"; }


    	//protected void mismatch(IntStream input, int ttype, BitSet follow)
    	//	throws RecognitionException
    	//	{
    	//		throw new MismatchedTokenException(ttype, input);
    	//	}
    	//public Object recoverFromMismatchedSet(IntStream input,
    	//	RecognitionException e,
    	//	BitSet follow)
    	//	throws RecognitionException
    	//	{
    	//		throw e;
    	//	}
        public void reportError(RecognitionException e) {
        	super.reportError(e);
        	System.exit(-1);
        }


    public static class constrs_input_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constrs_input"
    // Calc.g:153:1: constrs_input : ( constr_input )* ;
    public final CalcParser.constrs_input_return constrs_input() throws RecognitionException {
        CalcParser.constrs_input_return retval = new CalcParser.constrs_input_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.constr_input_return constr_input1 = null;



        try {
            // Calc.g:154:2: ( ( constr_input )* )
            // Calc.g:154:4: ( constr_input )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // Calc.g:154:4: ( constr_input )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==LPAR_C) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // Calc.g:154:6: constr_input
            	    {
            	    pushFollow(FOLLOW_constr_input_in_constrs_input465);
            	    constr_input1=constr_input();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, constr_input1.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "constrs_input"

    public static class constr_input_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constr_input"
    // Calc.g:156:1: constr_input : LPAR_C constraints RPAR_C -> ^( CONSTR_INPUT constraints ) ;
    public final CalcParser.constr_input_return constr_input() throws RecognitionException {
        CalcParser.constr_input_return retval = new CalcParser.constr_input_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAR_C2=null;
        Token RPAR_C4=null;
        CalcParser.constraints_return constraints3 = null;


        CommonTree LPAR_C2_tree=null;
        CommonTree RPAR_C4_tree=null;
        RewriteRuleTokenStream stream_RPAR_C=new RewriteRuleTokenStream(adaptor,"token RPAR_C");
        RewriteRuleTokenStream stream_LPAR_C=new RewriteRuleTokenStream(adaptor,"token LPAR_C");
        RewriteRuleSubtreeStream stream_constraints=new RewriteRuleSubtreeStream(adaptor,"rule constraints");
        try {
            // Calc.g:157:2: ( LPAR_C constraints RPAR_C -> ^( CONSTR_INPUT constraints ) )
            // Calc.g:157:4: LPAR_C constraints RPAR_C
            {
            LPAR_C2=(Token)match(input,LPAR_C,FOLLOW_LPAR_C_in_constr_input478); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAR_C.add(LPAR_C2);

            pushFollow(FOLLOW_constraints_in_constr_input480);
            constraints3=constraints();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_constraints.add(constraints3.getTree());
            RPAR_C4=(Token)match(input,RPAR_C,FOLLOW_RPAR_C_in_constr_input482); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAR_C.add(RPAR_C4);



            // AST REWRITE
            // elements: constraints
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 158:4: -> ^( CONSTR_INPUT constraints )
            {
                // Calc.g:158:7: ^( CONSTR_INPUT constraints )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONSTR_INPUT, "CONSTR_INPUT"), root_1);

                adaptor.addChild(root_1, stream_constraints.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "constr_input"

    public static class calc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "calc"
    // Calc.g:162:1: calc : ( calc_statement )* ;
    public final CalcParser.calc_return calc() throws RecognitionException {
        CalcParser.calc_return retval = new CalcParser.calc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.calc_statement_return calc_statement5 = null;



        try {
            // Calc.g:163:2: ( ( calc_statement )* )
            // Calc.g:163:4: ( calc_statement )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // Calc.g:163:4: ( calc_statement )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=PRINT && LA2_0<=PRINT_ARMC)||(LA2_0>=TERMINATION && LA2_0<=PREFPERIOD)||LA2_0==ID) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // Calc.g:163:5: calc_statement
            	    {
            	    pushFollow(FOLLOW_calc_statement_in_calc508);
            	    calc_statement5=calc_statement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, calc_statement5.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "calc"

    public static class calc_statement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "calc_statement"
    // Calc.g:165:1: calc_statement : ( calc_print | calc_print_armc | calc_store | calc_termination | calc_prefperiod );
    public final CalcParser.calc_statement_return calc_statement() throws RecognitionException {
        CalcParser.calc_statement_return retval = new CalcParser.calc_statement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.calc_print_return calc_print6 = null;

        CalcParser.calc_print_armc_return calc_print_armc7 = null;

        CalcParser.calc_store_return calc_store8 = null;

        CalcParser.calc_termination_return calc_termination9 = null;

        CalcParser.calc_prefperiod_return calc_prefperiod10 = null;



        try {
            // Calc.g:166:2: ( calc_print | calc_print_armc | calc_store | calc_termination | calc_prefperiod )
            int alt3=5;
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
            case TERMINATION:
                {
                alt3=4;
                }
                break;
            case PREFPERIOD:
                {
                alt3=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // Calc.g:166:4: calc_print
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_calc_print_in_calc_statement520);
                    calc_print6=calc_print();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, calc_print6.getTree());

                    }
                    break;
                case 2 :
                    // Calc.g:167:4: calc_print_armc
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_calc_print_armc_in_calc_statement525);
                    calc_print_armc7=calc_print_armc();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, calc_print_armc7.getTree());

                    }
                    break;
                case 3 :
                    // Calc.g:168:4: calc_store
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_calc_store_in_calc_statement530);
                    calc_store8=calc_store();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, calc_store8.getTree());

                    }
                    break;
                case 4 :
                    // Calc.g:169:4: calc_termination
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_calc_termination_in_calc_statement535);
                    calc_termination9=calc_termination();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, calc_termination9.getTree());

                    }
                    break;
                case 5 :
                    // Calc.g:170:4: calc_prefperiod
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_calc_prefperiod_in_calc_statement540);
                    calc_prefperiod10=calc_prefperiod();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, calc_prefperiod10.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "calc_statement"

    public static class calc_store_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "calc_store"
    // Calc.g:172:1: calc_store : ID ':' be_e0 ';' ;
    public final CalcParser.calc_store_return calc_store() throws RecognitionException {
        CalcParser.calc_store_return retval = new CalcParser.calc_store_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID11=null;
        Token char_literal12=null;
        Token char_literal14=null;
        CalcParser.be_e0_return be_e013 = null;


        CommonTree ID11_tree=null;
        CommonTree char_literal12_tree=null;
        CommonTree char_literal14_tree=null;

        try {
            // Calc.g:173:2: ( ID ':' be_e0 ';' )
            // Calc.g:173:4: ID ':' be_e0 ';'
            {
            root_0 = (CommonTree)adaptor.nil();

            ID11=(Token)match(input,ID,FOLLOW_ID_in_calc_store550); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID11_tree = (CommonTree)adaptor.create(ID11);
            root_0 = (CommonTree)adaptor.becomeRoot(ID11_tree, root_0);
            }
            char_literal12=(Token)match(input,74,FOLLOW_74_in_calc_store553); if (state.failed) return retval;
            pushFollow(FOLLOW_be_e0_in_calc_store556);
            be_e013=be_e0();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e013.getTree());
            char_literal14=(Token)match(input,75,FOLLOW_75_in_calc_store558); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "calc_store"

    public static class calc_print_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "calc_print"
    // Calc.g:175:1: calc_print : PRINT print_term ( ',' print_term )* ';' ;
    public final CalcParser.calc_print_return calc_print() throws RecognitionException {
        CalcParser.calc_print_return retval = new CalcParser.calc_print_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PRINT15=null;
        Token char_literal17=null;
        Token char_literal19=null;
        CalcParser.print_term_return print_term16 = null;

        CalcParser.print_term_return print_term18 = null;


        CommonTree PRINT15_tree=null;
        CommonTree char_literal17_tree=null;
        CommonTree char_literal19_tree=null;

        try {
            // Calc.g:176:2: ( PRINT print_term ( ',' print_term )* ';' )
            // Calc.g:176:4: PRINT print_term ( ',' print_term )* ';'
            {
            root_0 = (CommonTree)adaptor.nil();

            PRINT15=(Token)match(input,PRINT,FOLLOW_PRINT_in_calc_print569); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            PRINT15_tree = (CommonTree)adaptor.create(PRINT15);
            root_0 = (CommonTree)adaptor.becomeRoot(PRINT15_tree, root_0);
            }
            pushFollow(FOLLOW_print_term_in_calc_print572);
            print_term16=print_term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, print_term16.getTree());
            // Calc.g:176:22: ( ',' print_term )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==COMMA) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // Calc.g:176:23: ',' print_term
            	    {
            	    char_literal17=(Token)match(input,COMMA,FOLLOW_COMMA_in_calc_print575); if (state.failed) return retval;
            	    pushFollow(FOLLOW_print_term_in_calc_print578);
            	    print_term18=print_term();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, print_term18.getTree());

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            char_literal19=(Token)match(input,75,FOLLOW_75_in_calc_print582); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "calc_print"

    public static class calc_print_armc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "calc_print_armc"
    // Calc.g:178:1: calc_print_armc : PRINT_ARMC be_e0 ';' ;
    public final CalcParser.calc_print_armc_return calc_print_armc() throws RecognitionException {
        CalcParser.calc_print_armc_return retval = new CalcParser.calc_print_armc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PRINT_ARMC20=null;
        Token char_literal22=null;
        CalcParser.be_e0_return be_e021 = null;


        CommonTree PRINT_ARMC20_tree=null;
        CommonTree char_literal22_tree=null;

        try {
            // Calc.g:179:2: ( PRINT_ARMC be_e0 ';' )
            // Calc.g:179:4: PRINT_ARMC be_e0 ';'
            {
            root_0 = (CommonTree)adaptor.nil();

            PRINT_ARMC20=(Token)match(input,PRINT_ARMC,FOLLOW_PRINT_ARMC_in_calc_print_armc593); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            PRINT_ARMC20_tree = (CommonTree)adaptor.create(PRINT_ARMC20);
            root_0 = (CommonTree)adaptor.becomeRoot(PRINT_ARMC20_tree, root_0);
            }
            pushFollow(FOLLOW_be_e0_in_calc_print_armc596);
            be_e021=be_e0();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e021.getTree());
            char_literal22=(Token)match(input,75,FOLLOW_75_in_calc_print_armc598); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "calc_print_armc"

    public static class calc_prefperiod_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "calc_prefperiod"
    // Calc.g:181:1: calc_prefperiod : PREFPERIOD LPAR ID COMMA ID COMMA ID RPAR ';' ;
    public final CalcParser.calc_prefperiod_return calc_prefperiod() throws RecognitionException {
        CalcParser.calc_prefperiod_return retval = new CalcParser.calc_prefperiod_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PREFPERIOD23=null;
        Token LPAR24=null;
        Token ID25=null;
        Token COMMA26=null;
        Token ID27=null;
        Token COMMA28=null;
        Token ID29=null;
        Token RPAR30=null;
        Token char_literal31=null;

        CommonTree PREFPERIOD23_tree=null;
        CommonTree LPAR24_tree=null;
        CommonTree ID25_tree=null;
        CommonTree COMMA26_tree=null;
        CommonTree ID27_tree=null;
        CommonTree COMMA28_tree=null;
        CommonTree ID29_tree=null;
        CommonTree RPAR30_tree=null;
        CommonTree char_literal31_tree=null;

        try {
            // Calc.g:182:2: ( PREFPERIOD LPAR ID COMMA ID COMMA ID RPAR ';' )
            // Calc.g:182:4: PREFPERIOD LPAR ID COMMA ID COMMA ID RPAR ';'
            {
            root_0 = (CommonTree)adaptor.nil();

            PREFPERIOD23=(Token)match(input,PREFPERIOD,FOLLOW_PREFPERIOD_in_calc_prefperiod609); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            PREFPERIOD23_tree = (CommonTree)adaptor.create(PREFPERIOD23);
            root_0 = (CommonTree)adaptor.becomeRoot(PREFPERIOD23_tree, root_0);
            }
            LPAR24=(Token)match(input,LPAR,FOLLOW_LPAR_in_calc_prefperiod612); if (state.failed) return retval;
            ID25=(Token)match(input,ID,FOLLOW_ID_in_calc_prefperiod615); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID25_tree = (CommonTree)adaptor.create(ID25);
            adaptor.addChild(root_0, ID25_tree);
            }
            COMMA26=(Token)match(input,COMMA,FOLLOW_COMMA_in_calc_prefperiod617); if (state.failed) return retval;
            ID27=(Token)match(input,ID,FOLLOW_ID_in_calc_prefperiod620); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID27_tree = (CommonTree)adaptor.create(ID27);
            adaptor.addChild(root_0, ID27_tree);
            }
            COMMA28=(Token)match(input,COMMA,FOLLOW_COMMA_in_calc_prefperiod622); if (state.failed) return retval;
            ID29=(Token)match(input,ID,FOLLOW_ID_in_calc_prefperiod625); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID29_tree = (CommonTree)adaptor.create(ID29);
            adaptor.addChild(root_0, ID29_tree);
            }
            RPAR30=(Token)match(input,RPAR,FOLLOW_RPAR_in_calc_prefperiod628); if (state.failed) return retval;
            char_literal31=(Token)match(input,75,FOLLOW_75_in_calc_prefperiod631); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "calc_prefperiod"

    public static class print_term_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "print_term"
    // Calc.g:184:1: print_term : ( strstr | be_e0 );
    public final CalcParser.print_term_return print_term() throws RecognitionException {
        CalcParser.print_term_return retval = new CalcParser.print_term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.strstr_return strstr32 = null;

        CalcParser.be_e0_return be_e033 = null;



        try {
            // Calc.g:185:2: ( strstr | be_e0 )
            int alt5=2;
            alt5 = dfa5.predict(input);
            switch (alt5) {
                case 1 :
                    // Calc.g:185:4: strstr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_strstr_in_print_term642);
                    strstr32=strstr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, strstr32.getTree());

                    }
                    break;
                case 2 :
                    // Calc.g:185:13: be_e0
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_be_e0_in_print_term646);
                    be_e033=be_e0();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e033.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "print_term"

    public static class calc_termination_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "calc_termination"
    // Calc.g:188:1: calc_termination : TERMINATION be_e0 ';' ;
    public final CalcParser.calc_termination_return calc_termination() throws RecognitionException {
        CalcParser.calc_termination_return retval = new CalcParser.calc_termination_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token TERMINATION34=null;
        Token char_literal36=null;
        CalcParser.be_e0_return be_e035 = null;


        CommonTree TERMINATION34_tree=null;
        CommonTree char_literal36_tree=null;

        try {
            // Calc.g:189:2: ( TERMINATION be_e0 ';' )
            // Calc.g:189:4: TERMINATION be_e0 ';'
            {
            root_0 = (CommonTree)adaptor.nil();

            TERMINATION34=(Token)match(input,TERMINATION,FOLLOW_TERMINATION_in_calc_termination658); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            TERMINATION34_tree = (CommonTree)adaptor.create(TERMINATION34);
            root_0 = (CommonTree)adaptor.becomeRoot(TERMINATION34_tree, root_0);
            }
            pushFollow(FOLLOW_be_e0_in_calc_termination661);
            be_e035=be_e0();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e035.getTree());
            char_literal36=(Token)match(input,75,FOLLOW_75_in_calc_termination663); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "calc_termination"

    public static class constraints_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constraints"
    // Calc.g:195:1: constraints : be_e0 ;
    public final CalcParser.constraints_return constraints() throws RecognitionException {
        CalcParser.constraints_return retval = new CalcParser.constraints_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.be_e0_return be_e037 = null;



        try {
            // Calc.g:196:2: ( be_e0 )
            // Calc.g:196:4: be_e0
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_be_e0_in_constraints678);
            be_e037=be_e0();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e037.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "constraints"

    public static class be_e0_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "be_e0"
    // Calc.g:200:1: be_e0 : ( EXISTS ( ID | PRIMED_ID ) ( COMMA ( ID | PRIMED_ID ) )* '.' be_e1 | be_e1 );
    public final CalcParser.be_e0_return be_e0() throws RecognitionException {
        CalcParser.be_e0_return retval = new CalcParser.be_e0_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EXISTS38=null;
        Token set39=null;
        Token COMMA40=null;
        Token set41=null;
        Token char_literal42=null;
        CalcParser.be_e1_return be_e143 = null;

        CalcParser.be_e1_return be_e144 = null;


        CommonTree EXISTS38_tree=null;
        CommonTree set39_tree=null;
        CommonTree COMMA40_tree=null;
        CommonTree set41_tree=null;
        CommonTree char_literal42_tree=null;

        try {
            // Calc.g:220:2: ( EXISTS ( ID | PRIMED_ID ) ( COMMA ( ID | PRIMED_ID ) )* '.' be_e1 | be_e1 )
            int alt7=2;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // Calc.g:220:4: EXISTS ( ID | PRIMED_ID ) ( COMMA ( ID | PRIMED_ID ) )* '.' be_e1
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    EXISTS38=(Token)match(input,EXISTS,FOLLOW_EXISTS_in_be_e0695); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EXISTS38_tree = (CommonTree)adaptor.create(EXISTS38);
                    root_0 = (CommonTree)adaptor.becomeRoot(EXISTS38_tree, root_0);
                    }
                    set39=(Token)input.LT(1);
                    if ( (input.LA(1)>=ID && input.LA(1)<=PRIMED_ID) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set39));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // Calc.g:220:29: ( COMMA ( ID | PRIMED_ID ) )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==COMMA) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // Calc.g:220:30: COMMA ( ID | PRIMED_ID )
                    	    {
                    	    COMMA40=(Token)match(input,COMMA,FOLLOW_COMMA_in_be_e0707); if (state.failed) return retval;
                    	    set41=(Token)input.LT(1);
                    	    if ( (input.LA(1)>=ID && input.LA(1)<=PRIMED_ID) ) {
                    	        input.consume();
                    	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set41));
                    	        state.errorRecovery=false;state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return retval;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);

                    char_literal42=(Token)match(input,76,FOLLOW_76_in_be_e0720); if (state.failed) return retval;
                    pushFollow(FOLLOW_be_e1_in_be_e0723);
                    be_e143=be_e1();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e143.getTree());

                    }
                    break;
                case 2 :
                    // Calc.g:221:4: be_e1
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_be_e1_in_be_e0728);
                    be_e144=be_e1();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e144.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "be_e0"

    public static class be_e1_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "be_e1"
    // Calc.g:224:1: be_e1 : be_e2 ( be_ee1 )? -> ^( OR be_e2 ( be_ee1 )? ) ;
    public final CalcParser.be_e1_return be_e1() throws RecognitionException {
        CalcParser.be_e1_return retval = new CalcParser.be_e1_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.be_e2_return be_e245 = null;

        CalcParser.be_ee1_return be_ee146 = null;


        RewriteRuleSubtreeStream stream_be_e2=new RewriteRuleSubtreeStream(adaptor,"rule be_e2");
        RewriteRuleSubtreeStream stream_be_ee1=new RewriteRuleSubtreeStream(adaptor,"rule be_ee1");
        try {
            // Calc.g:225:2: ( be_e2 ( be_ee1 )? -> ^( OR be_e2 ( be_ee1 )? ) )
            // Calc.g:225:4: be_e2 ( be_ee1 )?
            {
            pushFollow(FOLLOW_be_e2_in_be_e1741);
            be_e245=be_e2();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_be_e2.add(be_e245.getTree());
            // Calc.g:225:10: ( be_ee1 )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==OR) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // Calc.g:225:10: be_ee1
                    {
                    pushFollow(FOLLOW_be_ee1_in_be_e1743);
                    be_ee146=be_ee1();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_be_ee1.add(be_ee146.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: be_e2, be_ee1
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 226:4: -> ^( OR be_e2 ( be_ee1 )? )
            {
                // Calc.g:226:7: ^( OR be_e2 ( be_ee1 )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(OR, "OR"), root_1);

                adaptor.addChild(root_1, stream_be_e2.nextTree());
                // Calc.g:226:18: ( be_ee1 )?
                if ( stream_be_ee1.hasNext() ) {
                    adaptor.addChild(root_1, stream_be_ee1.nextTree());

                }
                stream_be_ee1.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "be_e1"

    public static class be_ee1_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "be_ee1"
    // Calc.g:228:1: be_ee1 : OR be_e2 ( be_ee1 )? ;
    public final CalcParser.be_ee1_return be_ee1() throws RecognitionException {
        CalcParser.be_ee1_return retval = new CalcParser.be_ee1_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token OR47=null;
        CalcParser.be_e2_return be_e248 = null;

        CalcParser.be_ee1_return be_ee149 = null;


        CommonTree OR47_tree=null;

        try {
            // Calc.g:229:2: ( OR be_e2 ( be_ee1 )? )
            // Calc.g:229:4: OR be_e2 ( be_ee1 )?
            {
            root_0 = (CommonTree)adaptor.nil();

            OR47=(Token)match(input,OR,FOLLOW_OR_in_be_ee1769); if (state.failed) return retval;
            pushFollow(FOLLOW_be_e2_in_be_ee1772);
            be_e248=be_e2();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e248.getTree());
            // Calc.g:229:14: ( be_ee1 )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==OR) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // Calc.g:229:14: be_ee1
                    {
                    pushFollow(FOLLOW_be_ee1_in_be_ee1774);
                    be_ee149=be_ee1();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, be_ee149.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "be_ee1"

    public static class be_e2_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "be_e2"
    // Calc.g:231:1: be_e2 : be_e3 ( be_ee2 )? -> ^( COMPOSE be_e3 ( be_ee2 )? ) ;
    public final CalcParser.be_e2_return be_e2() throws RecognitionException {
        CalcParser.be_e2_return retval = new CalcParser.be_e2_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.be_e3_return be_e350 = null;

        CalcParser.be_ee2_return be_ee251 = null;


        RewriteRuleSubtreeStream stream_be_e3=new RewriteRuleSubtreeStream(adaptor,"rule be_e3");
        RewriteRuleSubtreeStream stream_be_ee2=new RewriteRuleSubtreeStream(adaptor,"rule be_ee2");
        try {
            // Calc.g:232:2: ( be_e3 ( be_ee2 )? -> ^( COMPOSE be_e3 ( be_ee2 )? ) )
            // Calc.g:232:4: be_e3 ( be_ee2 )?
            {
            pushFollow(FOLLOW_be_e3_in_be_e2787);
            be_e350=be_e3();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_be_e3.add(be_e350.getTree());
            // Calc.g:232:10: ( be_ee2 )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==COMPOSE) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // Calc.g:232:10: be_ee2
                    {
                    pushFollow(FOLLOW_be_ee2_in_be_e2789);
                    be_ee251=be_ee2();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_be_ee2.add(be_ee251.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: be_e3, be_ee2
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 233:4: -> ^( COMPOSE be_e3 ( be_ee2 )? )
            {
                // Calc.g:233:7: ^( COMPOSE be_e3 ( be_ee2 )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(COMPOSE, "COMPOSE"), root_1);

                adaptor.addChild(root_1, stream_be_e3.nextTree());
                // Calc.g:233:23: ( be_ee2 )?
                if ( stream_be_ee2.hasNext() ) {
                    adaptor.addChild(root_1, stream_be_ee2.nextTree());

                }
                stream_be_ee2.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "be_e2"

    public static class be_ee2_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "be_ee2"
    // Calc.g:235:1: be_ee2 : COMPOSE be_e3 ( be_ee2 )? ;
    public final CalcParser.be_ee2_return be_ee2() throws RecognitionException {
        CalcParser.be_ee2_return retval = new CalcParser.be_ee2_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPOSE52=null;
        CalcParser.be_e3_return be_e353 = null;

        CalcParser.be_ee2_return be_ee254 = null;


        CommonTree COMPOSE52_tree=null;

        try {
            // Calc.g:236:2: ( COMPOSE be_e3 ( be_ee2 )? )
            // Calc.g:236:4: COMPOSE be_e3 ( be_ee2 )?
            {
            root_0 = (CommonTree)adaptor.nil();

            COMPOSE52=(Token)match(input,COMPOSE,FOLLOW_COMPOSE_in_be_ee2815); if (state.failed) return retval;
            pushFollow(FOLLOW_be_e3_in_be_ee2818);
            be_e353=be_e3();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e353.getTree());
            // Calc.g:236:19: ( be_ee2 )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==COMPOSE) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // Calc.g:236:19: be_ee2
                    {
                    pushFollow(FOLLOW_be_ee2_in_be_ee2820);
                    be_ee254=be_ee2();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, be_ee254.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "be_ee2"

    public static class be_e3_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "be_e3"
    // Calc.g:238:1: be_e3 : be_nn ( be_ee3 )? -> ^( AND be_nn ( be_ee3 )? ) ;
    public final CalcParser.be_e3_return be_e3() throws RecognitionException {
        CalcParser.be_e3_return retval = new CalcParser.be_e3_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.be_nn_return be_nn55 = null;

        CalcParser.be_ee3_return be_ee356 = null;


        RewriteRuleSubtreeStream stream_be_nn=new RewriteRuleSubtreeStream(adaptor,"rule be_nn");
        RewriteRuleSubtreeStream stream_be_ee3=new RewriteRuleSubtreeStream(adaptor,"rule be_ee3");
        try {
            // Calc.g:239:2: ( be_nn ( be_ee3 )? -> ^( AND be_nn ( be_ee3 )? ) )
            // Calc.g:239:4: be_nn ( be_ee3 )?
            {
            pushFollow(FOLLOW_be_nn_in_be_e3832);
            be_nn55=be_nn();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_be_nn.add(be_nn55.getTree());
            // Calc.g:239:10: ( be_ee3 )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==AND) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // Calc.g:239:10: be_ee3
                    {
                    pushFollow(FOLLOW_be_ee3_in_be_e3834);
                    be_ee356=be_ee3();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_be_ee3.add(be_ee356.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: be_ee3, be_nn
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 240:4: -> ^( AND be_nn ( be_ee3 )? )
            {
                // Calc.g:240:7: ^( AND be_nn ( be_ee3 )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(AND, "AND"), root_1);

                adaptor.addChild(root_1, stream_be_nn.nextTree());
                // Calc.g:240:19: ( be_ee3 )?
                if ( stream_be_ee3.hasNext() ) {
                    adaptor.addChild(root_1, stream_be_ee3.nextTree());

                }
                stream_be_ee3.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "be_e3"

    public static class be_ee3_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "be_ee3"
    // Calc.g:242:1: be_ee3 : AND be_nn ( be_ee3 )? ;
    public final CalcParser.be_ee3_return be_ee3() throws RecognitionException {
        CalcParser.be_ee3_return retval = new CalcParser.be_ee3_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND57=null;
        CalcParser.be_nn_return be_nn58 = null;

        CalcParser.be_ee3_return be_ee359 = null;


        CommonTree AND57_tree=null;

        try {
            // Calc.g:243:2: ( AND be_nn ( be_ee3 )? )
            // Calc.g:243:4: AND be_nn ( be_ee3 )?
            {
            root_0 = (CommonTree)adaptor.nil();

            AND57=(Token)match(input,AND,FOLLOW_AND_in_be_ee3859); if (state.failed) return retval;
            pushFollow(FOLLOW_be_nn_in_be_ee3862);
            be_nn58=be_nn();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, be_nn58.getTree());
            // Calc.g:243:15: ( be_ee3 )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==AND) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // Calc.g:243:15: be_ee3
                    {
                    pushFollow(FOLLOW_be_ee3_in_be_ee3864);
                    be_ee359=be_ee3();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, be_ee359.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "be_ee3"

    public static class be_nn_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "be_nn"
    // Calc.g:245:1: be_nn : ( NOT be_ff | DOMAIN LPAR ID RPAR | RANGE LPAR ID RPAR | be_ff );
    public final CalcParser.be_nn_return be_nn() throws RecognitionException {
        CalcParser.be_nn_return retval = new CalcParser.be_nn_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NOT60=null;
        Token DOMAIN62=null;
        Token LPAR63=null;
        Token ID64=null;
        Token RPAR65=null;
        Token RANGE66=null;
        Token LPAR67=null;
        Token ID68=null;
        Token RPAR69=null;
        CalcParser.be_ff_return be_ff61 = null;

        CalcParser.be_ff_return be_ff70 = null;


        CommonTree NOT60_tree=null;
        CommonTree DOMAIN62_tree=null;
        CommonTree LPAR63_tree=null;
        CommonTree ID64_tree=null;
        CommonTree RPAR65_tree=null;
        CommonTree RANGE66_tree=null;
        CommonTree LPAR67_tree=null;
        CommonTree ID68_tree=null;
        CommonTree RPAR69_tree=null;

        try {
            // Calc.g:246:2: ( NOT be_ff | DOMAIN LPAR ID RPAR | RANGE LPAR ID RPAR | be_ff )
            int alt14=4;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // Calc.g:246:4: NOT be_ff
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    NOT60=(Token)match(input,NOT,FOLLOW_NOT_in_be_nn876); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NOT60_tree = (CommonTree)adaptor.create(NOT60);
                    root_0 = (CommonTree)adaptor.becomeRoot(NOT60_tree, root_0);
                    }
                    pushFollow(FOLLOW_be_ff_in_be_nn879);
                    be_ff61=be_ff();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, be_ff61.getTree());

                    }
                    break;
                case 2 :
                    // Calc.g:247:4: DOMAIN LPAR ID RPAR
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    DOMAIN62=(Token)match(input,DOMAIN,FOLLOW_DOMAIN_in_be_nn884); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOMAIN62_tree = (CommonTree)adaptor.create(DOMAIN62);
                    root_0 = (CommonTree)adaptor.becomeRoot(DOMAIN62_tree, root_0);
                    }
                    LPAR63=(Token)match(input,LPAR,FOLLOW_LPAR_in_be_nn887); if (state.failed) return retval;
                    ID64=(Token)match(input,ID,FOLLOW_ID_in_be_nn890); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ID64_tree = (CommonTree)adaptor.create(ID64);
                    adaptor.addChild(root_0, ID64_tree);
                    }
                    RPAR65=(Token)match(input,RPAR,FOLLOW_RPAR_in_be_nn892); if (state.failed) return retval;

                    }
                    break;
                case 3 :
                    // Calc.g:248:4: RANGE LPAR ID RPAR
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    RANGE66=(Token)match(input,RANGE,FOLLOW_RANGE_in_be_nn898); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RANGE66_tree = (CommonTree)adaptor.create(RANGE66);
                    root_0 = (CommonTree)adaptor.becomeRoot(RANGE66_tree, root_0);
                    }
                    LPAR67=(Token)match(input,LPAR,FOLLOW_LPAR_in_be_nn901); if (state.failed) return retval;
                    ID68=(Token)match(input,ID,FOLLOW_ID_in_be_nn904); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ID68_tree = (CommonTree)adaptor.create(ID68);
                    adaptor.addChild(root_0, ID68_tree);
                    }
                    RPAR69=(Token)match(input,RPAR,FOLLOW_RPAR_in_be_nn906); if (state.failed) return retval;

                    }
                    break;
                case 4 :
                    // Calc.g:249:4: be_ff
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_be_ff_in_be_nn912);
                    be_ff70=be_ff();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, be_ff70.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "be_nn"

    public static class be_ff_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "be_ff"
    // Calc.g:251:1: be_ff : ( ( constraint ( closure )? ( abstr )* )=> constraint ( closure )? ( abstr )* | LPAR be_e0 RPAR ( closure )? ( abstr )* );
    public final CalcParser.be_ff_return be_ff() throws RecognitionException {
        CalcParser.be_ff_return retval = new CalcParser.be_ff_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAR74=null;
        Token RPAR76=null;
        CalcParser.constraint_return constraint71 = null;

        CalcParser.closure_return closure72 = null;

        CalcParser.abstr_return abstr73 = null;

        CalcParser.be_e0_return be_e075 = null;

        CalcParser.closure_return closure77 = null;

        CalcParser.abstr_return abstr78 = null;


        CommonTree LPAR74_tree=null;
        CommonTree RPAR76_tree=null;

        try {
            // Calc.g:252:2: ( ( constraint ( closure )? ( abstr )* )=> constraint ( closure )? ( abstr )* | LPAR be_e0 RPAR ( closure )? ( abstr )* )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( ((LA19_0>=PLUS && LA19_0<=MINUS)) && (synpred1_Calc())) {
                alt19=1;
            }
            else if ( (LA19_0==LPAR) ) {
                int LA19_2 = input.LA(2);

                if ( (synpred1_Calc()) ) {
                    alt19=1;
                }
                else if ( (true) ) {
                    alt19=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 2, input);

                    throw nvae;
                }
            }
            else if ( (LA19_0==PRIMED_ID) && (synpred1_Calc())) {
                alt19=1;
            }
            else if ( (LA19_0==ID) && (synpred1_Calc())) {
                alt19=1;
            }
            else if ( (LA19_0==CONST) && (synpred1_Calc())) {
                alt19=1;
            }
            else if ( (LA19_0==TRUE) && (synpred1_Calc())) {
                alt19=1;
            }
            else if ( (LA19_0==FALSE) && (synpred1_Calc())) {
                alt19=1;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // Calc.g:252:4: ( constraint ( closure )? ( abstr )* )=> constraint ( closure )? ( abstr )*
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_constraint_in_be_ff943);
                    constraint71=constraint();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, constraint71.getTree());
                    // Calc.g:253:15: ( closure )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( ((LA15_0>=CL_PLUS && LA15_0<=CL_EXPR)) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // Calc.g:253:16: closure
                            {
                            pushFollow(FOLLOW_closure_in_be_ff946);
                            closure72=closure();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, closure72.getTree());

                            }
                            break;

                    }

                    // Calc.g:253:26: ( abstr )*
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( ((LA16_0>=ABSTR_D && LA16_0<=ABSTR_L)) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // Calc.g:253:27: abstr
                    	    {
                    	    pushFollow(FOLLOW_abstr_in_be_ff951);
                    	    abstr73=abstr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, abstr73.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop16;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // Calc.g:254:4: LPAR be_e0 RPAR ( closure )? ( abstr )*
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    LPAR74=(Token)match(input,LPAR,FOLLOW_LPAR_in_be_ff958); if (state.failed) return retval;
                    pushFollow(FOLLOW_be_e0_in_be_ff961);
                    be_e075=be_e0();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, be_e075.getTree());
                    RPAR76=(Token)match(input,RPAR,FOLLOW_RPAR_in_be_ff963); if (state.failed) return retval;
                    // Calc.g:254:22: ( closure )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( ((LA17_0>=CL_PLUS && LA17_0<=CL_EXPR)) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // Calc.g:254:23: closure
                            {
                            pushFollow(FOLLOW_closure_in_be_ff967);
                            closure77=closure();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, closure77.getTree());

                            }
                            break;

                    }

                    // Calc.g:254:33: ( abstr )*
                    loop18:
                    do {
                        int alt18=2;
                        int LA18_0 = input.LA(1);

                        if ( ((LA18_0>=ABSTR_D && LA18_0<=ABSTR_L)) ) {
                            alt18=1;
                        }


                        switch (alt18) {
                    	case 1 :
                    	    // Calc.g:254:34: abstr
                    	    {
                    	    pushFollow(FOLLOW_abstr_in_be_ff972);
                    	    abstr78=abstr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, abstr78.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop18;
                        }
                    } while (true);


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "be_ff"

    public static class closure_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "closure"
    // Calc.g:257:1: closure : ( CL_PLUS ( ID )? | CL_STAR ( ID )? | CL_EXPR LPAR terms RPAR );
    public final CalcParser.closure_return closure() throws RecognitionException {
        CalcParser.closure_return retval = new CalcParser.closure_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CL_PLUS79=null;
        Token ID80=null;
        Token CL_STAR81=null;
        Token ID82=null;
        Token CL_EXPR83=null;
        Token LPAR84=null;
        Token RPAR86=null;
        CalcParser.terms_return terms85 = null;


        CommonTree CL_PLUS79_tree=null;
        CommonTree ID80_tree=null;
        CommonTree CL_STAR81_tree=null;
        CommonTree ID82_tree=null;
        CommonTree CL_EXPR83_tree=null;
        CommonTree LPAR84_tree=null;
        CommonTree RPAR86_tree=null;

        try {
            // Calc.g:258:2: ( CL_PLUS ( ID )? | CL_STAR ( ID )? | CL_EXPR LPAR terms RPAR )
            int alt22=3;
            switch ( input.LA(1) ) {
            case CL_PLUS:
                {
                alt22=1;
                }
                break;
            case CL_STAR:
                {
                alt22=2;
                }
                break;
            case CL_EXPR:
                {
                alt22=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }

            switch (alt22) {
                case 1 :
                    // Calc.g:258:4: CL_PLUS ( ID )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CL_PLUS79=(Token)match(input,CL_PLUS,FOLLOW_CL_PLUS_in_closure986); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CL_PLUS79_tree = (CommonTree)adaptor.create(CL_PLUS79);
                    root_0 = (CommonTree)adaptor.becomeRoot(CL_PLUS79_tree, root_0);
                    }
                    // Calc.g:258:13: ( ID )?
                    int alt20=2;
                    int LA20_0 = input.LA(1);

                    if ( (LA20_0==ID) ) {
                        alt20=1;
                    }
                    switch (alt20) {
                        case 1 :
                            // Calc.g:258:13: ID
                            {
                            ID80=(Token)match(input,ID,FOLLOW_ID_in_closure989); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            ID80_tree = (CommonTree)adaptor.create(ID80);
                            adaptor.addChild(root_0, ID80_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // Calc.g:259:4: CL_STAR ( ID )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CL_STAR81=(Token)match(input,CL_STAR,FOLLOW_CL_STAR_in_closure995); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CL_STAR81_tree = (CommonTree)adaptor.create(CL_STAR81);
                    root_0 = (CommonTree)adaptor.becomeRoot(CL_STAR81_tree, root_0);
                    }
                    // Calc.g:259:13: ( ID )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==ID) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // Calc.g:259:13: ID
                            {
                            ID82=(Token)match(input,ID,FOLLOW_ID_in_closure998); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            ID82_tree = (CommonTree)adaptor.create(ID82);
                            adaptor.addChild(root_0, ID82_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // Calc.g:260:4: CL_EXPR LPAR terms RPAR
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CL_EXPR83=(Token)match(input,CL_EXPR,FOLLOW_CL_EXPR_in_closure1004); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CL_EXPR83_tree = (CommonTree)adaptor.create(CL_EXPR83);
                    root_0 = (CommonTree)adaptor.becomeRoot(CL_EXPR83_tree, root_0);
                    }
                    LPAR84=(Token)match(input,LPAR,FOLLOW_LPAR_in_closure1007); if (state.failed) return retval;
                    pushFollow(FOLLOW_terms_in_closure1010);
                    terms85=terms();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, terms85.getTree());
                    RPAR86=(Token)match(input,RPAR,FOLLOW_RPAR_in_closure1012); if (state.failed) return retval;

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "closure"

    public static class abstr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "abstr"
    // Calc.g:263:1: abstr : ( ABSTR_D | ABSTR_O | ABSTR_L );
    public final CalcParser.abstr_return abstr() throws RecognitionException {
        CalcParser.abstr_return retval = new CalcParser.abstr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ABSTR_D87=null;
        Token ABSTR_O88=null;
        Token ABSTR_L89=null;

        CommonTree ABSTR_D87_tree=null;
        CommonTree ABSTR_O88_tree=null;
        CommonTree ABSTR_L89_tree=null;

        try {
            // Calc.g:264:2: ( ABSTR_D | ABSTR_O | ABSTR_L )
            int alt23=3;
            switch ( input.LA(1) ) {
            case ABSTR_D:
                {
                alt23=1;
                }
                break;
            case ABSTR_O:
                {
                alt23=2;
                }
                break;
            case ABSTR_L:
                {
                alt23=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }

            switch (alt23) {
                case 1 :
                    // Calc.g:264:4: ABSTR_D
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    ABSTR_D87=(Token)match(input,ABSTR_D,FOLLOW_ABSTR_D_in_abstr1025); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ABSTR_D87_tree = (CommonTree)adaptor.create(ABSTR_D87);
                    root_0 = (CommonTree)adaptor.becomeRoot(ABSTR_D87_tree, root_0);
                    }

                    }
                    break;
                case 2 :
                    // Calc.g:265:4: ABSTR_O
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    ABSTR_O88=(Token)match(input,ABSTR_O,FOLLOW_ABSTR_O_in_abstr1031); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ABSTR_O88_tree = (CommonTree)adaptor.create(ABSTR_O88);
                    root_0 = (CommonTree)adaptor.becomeRoot(ABSTR_O88_tree, root_0);
                    }

                    }
                    break;
                case 3 :
                    // Calc.g:266:4: ABSTR_L
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    ABSTR_L89=(Token)match(input,ABSTR_L,FOLLOW_ABSTR_L_in_abstr1037); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ABSTR_L89_tree = (CommonTree)adaptor.create(ABSTR_L89);
                    root_0 = (CommonTree)adaptor.becomeRoot(ABSTR_L89_tree, root_0);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "abstr"

    public static class constraint_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constraint"
    // Calc.g:273:1: constraint : ( ( terms cop terms )=> terms cop terms -> ^( CONSTRAINT terms cop terms ) | ( ID )=> ID | TRUE -> ^( TRUE ) | FALSE -> ^( FALSE ) );
    public final CalcParser.constraint_return constraint() throws RecognitionException {
        CalcParser.constraint_return retval = new CalcParser.constraint_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID93=null;
        Token TRUE94=null;
        Token FALSE95=null;
        CalcParser.terms_return terms90 = null;

        CalcParser.cop_return cop91 = null;

        CalcParser.terms_return terms92 = null;


        CommonTree ID93_tree=null;
        CommonTree TRUE94_tree=null;
        CommonTree FALSE95_tree=null;
        RewriteRuleTokenStream stream_FALSE=new RewriteRuleTokenStream(adaptor,"token FALSE");
        RewriteRuleTokenStream stream_TRUE=new RewriteRuleTokenStream(adaptor,"token TRUE");
        RewriteRuleSubtreeStream stream_cop=new RewriteRuleSubtreeStream(adaptor,"rule cop");
        RewriteRuleSubtreeStream stream_terms=new RewriteRuleSubtreeStream(adaptor,"rule terms");
        try {
            // Calc.g:274:2: ( ( terms cop terms )=> terms cop terms -> ^( CONSTRAINT terms cop terms ) | ( ID )=> ID | TRUE -> ^( TRUE ) | FALSE -> ^( FALSE ) )
            int alt24=4;
            int LA24_0 = input.LA(1);

            if ( ((LA24_0>=PLUS && LA24_0<=MINUS)) && (synpred2_Calc())) {
                alt24=1;
            }
            else if ( (LA24_0==LPAR) && (synpred2_Calc())) {
                alt24=1;
            }
            else if ( (LA24_0==PRIMED_ID) && (synpred2_Calc())) {
                alt24=1;
            }
            else if ( (LA24_0==ID) ) {
                int LA24_4 = input.LA(2);

                if ( (synpred2_Calc()) ) {
                    alt24=1;
                }
                else if ( (synpred3_Calc()) ) {
                    alt24=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 24, 4, input);

                    throw nvae;
                }
            }
            else if ( (LA24_0==CONST) && (synpred2_Calc())) {
                alt24=1;
            }
            else if ( (LA24_0==TRUE) ) {
                alt24=3;
            }
            else if ( (LA24_0==FALSE) ) {
                alt24=4;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // Calc.g:274:4: ( terms cop terms )=> terms cop terms
                    {
                    pushFollow(FOLLOW_terms_in_constraint1066);
                    terms90=terms();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_terms.add(terms90.getTree());
                    pushFollow(FOLLOW_cop_in_constraint1068);
                    cop91=cop();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cop.add(cop91.getTree());
                    pushFollow(FOLLOW_terms_in_constraint1070);
                    terms92=terms();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_terms.add(terms92.getTree());


                    // AST REWRITE
                    // elements: terms, terms, cop
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 276:4: -> ^( CONSTRAINT terms cop terms )
                    {
                        // Calc.g:276:7: ^( CONSTRAINT terms cop terms )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONSTRAINT, "CONSTRAINT"), root_1);

                        adaptor.addChild(root_1, stream_terms.nextTree());
                        adaptor.addChild(root_1, stream_cop.nextTree());
                        adaptor.addChild(root_1, stream_terms.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // Calc.g:277:4: ( ID )=> ID
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    ID93=(Token)match(input,ID,FOLLOW_ID_in_constraint1096); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ID93_tree = (CommonTree)adaptor.create(ID93);
                    adaptor.addChild(root_0, ID93_tree);
                    }

                    }
                    break;
                case 3 :
                    // Calc.g:278:4: TRUE
                    {
                    TRUE94=(Token)match(input,TRUE,FOLLOW_TRUE_in_constraint1102); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE94);



                    // AST REWRITE
                    // elements: TRUE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 279:4: -> ^( TRUE )
                    {
                        // Calc.g:279:7: ^( TRUE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_TRUE.nextNode(), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // Calc.g:280:4: FALSE
                    {
                    FALSE95=(Token)match(input,FALSE,FOLLOW_FALSE_in_constraint1116); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE95);



                    // AST REWRITE
                    // elements: FALSE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 281:4: -> ^( FALSE )
                    {
                        // Calc.g:281:7: ^( FALSE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_FALSE.nextNode(), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "constraint"

    public static class cop_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cop"
    // Calc.g:283:1: cop : ( EQ | NEQ | LEQ | LESS | GEQ | GREATER | DIVIDES );
    public final CalcParser.cop_return cop() throws RecognitionException {
        CalcParser.cop_return retval = new CalcParser.cop_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set96=null;

        CommonTree set96_tree=null;

        try {
            // Calc.g:284:2: ( EQ | NEQ | LEQ | LESS | GEQ | GREATER | DIVIDES )
            // Calc.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set96=(Token)input.LT(1);
            if ( input.LA(1)==EQ||(input.LA(1)>=NEQ && input.LA(1)<=DIVIDES) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set96));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "cop"

    public static class terms_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "terms"
    // Calc.g:288:1: terms : ae_aa ;
    public final CalcParser.terms_return terms() throws RecognitionException {
        CalcParser.terms_return retval = new CalcParser.terms_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.ae_aa_return ae_aa97 = null;



        try {
            // Calc.g:289:2: ( ae_aa )
            // Calc.g:289:4: ae_aa
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_ae_aa_in_terms1159);
            ae_aa97=ae_aa();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ae_aa97.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "terms"

    public static class ae_aa_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ae_aa"
    // Calc.g:293:1: ae_aa : ( pm )? ae_mm ( ae_aaa )? -> ^( PLUS ( pm )? ae_mm ( ae_aaa )? ) ;
    public final CalcParser.ae_aa_return ae_aa() throws RecognitionException {
        CalcParser.ae_aa_return retval = new CalcParser.ae_aa_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.pm_return pm98 = null;

        CalcParser.ae_mm_return ae_mm99 = null;

        CalcParser.ae_aaa_return ae_aaa100 = null;


        RewriteRuleSubtreeStream stream_pm=new RewriteRuleSubtreeStream(adaptor,"rule pm");
        RewriteRuleSubtreeStream stream_ae_mm=new RewriteRuleSubtreeStream(adaptor,"rule ae_mm");
        RewriteRuleSubtreeStream stream_ae_aaa=new RewriteRuleSubtreeStream(adaptor,"rule ae_aaa");
        try {
            // Calc.g:294:2: ( ( pm )? ae_mm ( ae_aaa )? -> ^( PLUS ( pm )? ae_mm ( ae_aaa )? ) )
            // Calc.g:294:4: ( pm )? ae_mm ( ae_aaa )?
            {
            // Calc.g:294:4: ( pm )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( ((LA25_0>=PLUS && LA25_0<=MINUS)) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // Calc.g:294:4: pm
                    {
                    pushFollow(FOLLOW_pm_in_ae_aa1174);
                    pm98=pm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_pm.add(pm98.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_ae_mm_in_ae_aa1177);
            ae_mm99=ae_mm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ae_mm.add(ae_mm99.getTree());
            // Calc.g:294:14: ( ae_aaa )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( ((LA26_0>=PLUS && LA26_0<=MINUS)) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // Calc.g:294:14: ae_aaa
                    {
                    pushFollow(FOLLOW_ae_aaa_in_ae_aa1179);
                    ae_aaa100=ae_aaa();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ae_aaa.add(ae_aaa100.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: ae_mm, ae_aaa, pm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 295:3: -> ^( PLUS ( pm )? ae_mm ( ae_aaa )? )
            {
                // Calc.g:295:6: ^( PLUS ( pm )? ae_mm ( ae_aaa )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PLUS, "PLUS"), root_1);

                // Calc.g:295:13: ( pm )?
                if ( stream_pm.hasNext() ) {
                    adaptor.addChild(root_1, stream_pm.nextTree());

                }
                stream_pm.reset();
                adaptor.addChild(root_1, stream_ae_mm.nextTree());
                // Calc.g:295:23: ( ae_aaa )?
                if ( stream_ae_aaa.hasNext() ) {
                    adaptor.addChild(root_1, stream_ae_aaa.nextTree());

                }
                stream_ae_aaa.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "ae_aa"

    public static class ae_aaa_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ae_aaa"
    // Calc.g:297:1: ae_aaa : pm ae_mm ( ae_aaa )? ;
    public final CalcParser.ae_aaa_return ae_aaa() throws RecognitionException {
        CalcParser.ae_aaa_return retval = new CalcParser.ae_aaa_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.pm_return pm101 = null;

        CalcParser.ae_mm_return ae_mm102 = null;

        CalcParser.ae_aaa_return ae_aaa103 = null;



        try {
            // Calc.g:298:2: ( pm ae_mm ( ae_aaa )? )
            // Calc.g:298:4: pm ae_mm ( ae_aaa )?
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_pm_in_ae_aaa1207);
            pm101=pm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, pm101.getTree());
            pushFollow(FOLLOW_ae_mm_in_ae_aaa1209);
            ae_mm102=ae_mm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ae_mm102.getTree());
            // Calc.g:298:13: ( ae_aaa )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( ((LA27_0>=PLUS && LA27_0<=MINUS)) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // Calc.g:298:13: ae_aaa
                    {
                    pushFollow(FOLLOW_ae_aaa_in_ae_aaa1211);
                    ae_aaa103=ae_aaa();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ae_aaa103.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "ae_aaa"

    public static class ae_mm_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ae_mm"
    // Calc.g:300:1: ae_mm : ae_ff ( ae_mmm )? -> ^( MULT ae_ff ( ae_mmm )? ) ;
    public final CalcParser.ae_mm_return ae_mm() throws RecognitionException {
        CalcParser.ae_mm_return retval = new CalcParser.ae_mm_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        CalcParser.ae_ff_return ae_ff104 = null;

        CalcParser.ae_mmm_return ae_mmm105 = null;


        RewriteRuleSubtreeStream stream_ae_mmm=new RewriteRuleSubtreeStream(adaptor,"rule ae_mmm");
        RewriteRuleSubtreeStream stream_ae_ff=new RewriteRuleSubtreeStream(adaptor,"rule ae_ff");
        try {
            // Calc.g:301:2: ( ae_ff ( ae_mmm )? -> ^( MULT ae_ff ( ae_mmm )? ) )
            // Calc.g:301:4: ae_ff ( ae_mmm )?
            {
            pushFollow(FOLLOW_ae_ff_in_ae_mm1223);
            ae_ff104=ae_ff();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ae_ff.add(ae_ff104.getTree());
            // Calc.g:301:10: ( ae_mmm )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==MULT) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // Calc.g:301:10: ae_mmm
                    {
                    pushFollow(FOLLOW_ae_mmm_in_ae_mm1225);
                    ae_mmm105=ae_mmm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ae_mmm.add(ae_mmm105.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: ae_ff, ae_mmm
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 302:4: -> ^( MULT ae_ff ( ae_mmm )? )
            {
                // Calc.g:302:7: ^( MULT ae_ff ( ae_mmm )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MULT, "MULT"), root_1);

                adaptor.addChild(root_1, stream_ae_ff.nextTree());
                // Calc.g:302:20: ( ae_mmm )?
                if ( stream_ae_mmm.hasNext() ) {
                    adaptor.addChild(root_1, stream_ae_mmm.nextTree());

                }
                stream_ae_mmm.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "ae_mm"

    public static class ae_mmm_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ae_mmm"
    // Calc.g:304:1: ae_mmm : MULT ae_ff ( ae_mmm )? ;
    public final CalcParser.ae_mmm_return ae_mmm() throws RecognitionException {
        CalcParser.ae_mmm_return retval = new CalcParser.ae_mmm_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MULT106=null;
        CalcParser.ae_ff_return ae_ff107 = null;

        CalcParser.ae_mmm_return ae_mmm108 = null;


        CommonTree MULT106_tree=null;

        try {
            // Calc.g:305:2: ( MULT ae_ff ( ae_mmm )? )
            // Calc.g:305:4: MULT ae_ff ( ae_mmm )?
            {
            root_0 = (CommonTree)adaptor.nil();

            MULT106=(Token)match(input,MULT,FOLLOW_MULT_in_ae_mmm1250); if (state.failed) return retval;
            pushFollow(FOLLOW_ae_ff_in_ae_mmm1253);
            ae_ff107=ae_ff();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ae_ff107.getTree());
            // Calc.g:305:16: ( ae_mmm )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==MULT) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // Calc.g:305:16: ae_mmm
                    {
                    pushFollow(FOLLOW_ae_mmm_in_ae_mmm1255);
                    ae_mmm108=ae_mmm();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ae_mmm108.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "ae_mmm"

    public static class ae_ff_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ae_ff"
    // Calc.g:307:1: ae_ff : ( LPAR ae_aa RPAR | PRIMED_ID | ID | CONST );
    public final CalcParser.ae_ff_return ae_ff() throws RecognitionException {
        CalcParser.ae_ff_return retval = new CalcParser.ae_ff_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAR109=null;
        Token RPAR111=null;
        Token PRIMED_ID112=null;
        Token ID113=null;
        Token CONST114=null;
        CalcParser.ae_aa_return ae_aa110 = null;


        CommonTree LPAR109_tree=null;
        CommonTree RPAR111_tree=null;
        CommonTree PRIMED_ID112_tree=null;
        CommonTree ID113_tree=null;
        CommonTree CONST114_tree=null;

        try {
            // Calc.g:308:2: ( LPAR ae_aa RPAR | PRIMED_ID | ID | CONST )
            int alt30=4;
            switch ( input.LA(1) ) {
            case LPAR:
                {
                alt30=1;
                }
                break;
            case PRIMED_ID:
                {
                alt30=2;
                }
                break;
            case ID:
                {
                alt30=3;
                }
                break;
            case CONST:
                {
                alt30=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }

            switch (alt30) {
                case 1 :
                    // Calc.g:308:4: LPAR ae_aa RPAR
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    LPAR109=(Token)match(input,LPAR,FOLLOW_LPAR_in_ae_ff1266); if (state.failed) return retval;
                    pushFollow(FOLLOW_ae_aa_in_ae_ff1269);
                    ae_aa110=ae_aa();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ae_aa110.getTree());
                    RPAR111=(Token)match(input,RPAR,FOLLOW_RPAR_in_ae_ff1271); if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // Calc.g:309:4: PRIMED_ID
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    PRIMED_ID112=(Token)match(input,PRIMED_ID,FOLLOW_PRIMED_ID_in_ae_ff1277); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PRIMED_ID112_tree = (CommonTree)adaptor.create(PRIMED_ID112);
                    adaptor.addChild(root_0, PRIMED_ID112_tree);
                    }

                    }
                    break;
                case 3 :
                    // Calc.g:310:4: ID
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    ID113=(Token)match(input,ID,FOLLOW_ID_in_ae_ff1282); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ID113_tree = (CommonTree)adaptor.create(ID113);
                    adaptor.addChild(root_0, ID113_tree);
                    }

                    }
                    break;
                case 4 :
                    // Calc.g:311:4: CONST
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CONST114=(Token)match(input,CONST,FOLLOW_CONST_in_ae_ff1288); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CONST114_tree = (CommonTree)adaptor.create(CONST114);
                    adaptor.addChild(root_0, CONST114_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "ae_ff"

    public static class pm_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "pm"
    // Calc.g:313:1: pm : ( PLUS | MINUS );
    public final CalcParser.pm_return pm() throws RecognitionException {
        CalcParser.pm_return retval = new CalcParser.pm_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set115=null;

        CommonTree set115_tree=null;

        try {
            // Calc.g:314:2: ( PLUS | MINUS )
            // Calc.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set115=(Token)input.LT(1);
            if ( (input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set115));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "pm"

    public static class strstr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "strstr"
    // Calc.g:372:1: strstr : STRSTR ;
    public final CalcParser.strstr_return strstr() throws RecognitionException {
        CalcParser.strstr_return retval = new CalcParser.strstr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STRSTR116=null;

        CommonTree STRSTR116_tree=null;

        try {
            // Calc.g:373:2: ( STRSTR )
            // Calc.g:373:5: STRSTR
            {
            root_0 = (CommonTree)adaptor.nil();

            STRSTR116=(Token)match(input,STRSTR,FOLLOW_STRSTR_in_strstr1662); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            STRSTR116_tree = (CommonTree)adaptor.create(STRSTR116);
            adaptor.addChild(root_0, STRSTR116_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        	//catch (RecognitionException e) {
        	//	throw e;
        	//}
        finally {
        }
        return retval;
    }
    // $ANTLR end "strstr"

    // $ANTLR start synpred1_Calc
    public final void synpred1_Calc_fragment() throws RecognitionException {   
        // Calc.g:252:4: ( constraint ( closure )? ( abstr )* )
        // Calc.g:252:5: constraint ( closure )? ( abstr )*
        {
        pushFollow(FOLLOW_constraint_in_synpred1_Calc923);
        constraint();

        state._fsp--;
        if (state.failed) return ;
        // Calc.g:252:16: ( closure )?
        int alt31=2;
        int LA31_0 = input.LA(1);

        if ( ((LA31_0>=CL_PLUS && LA31_0<=CL_EXPR)) ) {
            alt31=1;
        }
        switch (alt31) {
            case 1 :
                // Calc.g:252:17: closure
                {
                pushFollow(FOLLOW_closure_in_synpred1_Calc926);
                closure();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }

        // Calc.g:252:27: ( abstr )*
        loop32:
        do {
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( ((LA32_0>=ABSTR_D && LA32_0<=ABSTR_L)) ) {
                alt32=1;
            }


            switch (alt32) {
        	case 1 :
        	    // Calc.g:252:28: abstr
        	    {
        	    pushFollow(FOLLOW_abstr_in_synpred1_Calc931);
        	    abstr();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    break loop32;
            }
        } while (true);


        }
    }
    // $ANTLR end synpred1_Calc

    // $ANTLR start synpred2_Calc
    public final void synpred2_Calc_fragment() throws RecognitionException {   
        // Calc.g:274:4: ( terms cop terms )
        // Calc.g:274:5: terms cop terms
        {
        pushFollow(FOLLOW_terms_in_synpred2_Calc1054);
        terms();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_cop_in_synpred2_Calc1056);
        cop();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_terms_in_synpred2_Calc1058);
        terms();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Calc

    // $ANTLR start synpred3_Calc
    public final void synpred3_Calc_fragment() throws RecognitionException {   
        // Calc.g:277:4: ( ID )
        // Calc.g:277:5: ID
        {
        match(input,ID,FOLLOW_ID_in_synpred3_Calc1091); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Calc

    // Delegated rules

    public final boolean synpred2_Calc() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_Calc_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_Calc() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_Calc_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_Calc() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_Calc_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA5 dfa5 = new DFA5(this);
    protected DFA7 dfa7 = new DFA7(this);
    protected DFA14 dfa14 = new DFA14(this);
    static final String DFA5_eotS =
        "\15\uffff";
    static final String DFA5_eofS =
        "\15\uffff";
    static final String DFA5_minS =
        "\1\4\14\uffff";
    static final String DFA5_maxS =
        "\1\111\14\uffff";
    static final String DFA5_acceptS =
        "\1\uffff\1\1\1\2\12\uffff";
    static final String DFA5_specialS =
        "\15\uffff}>";
    static final String[] DFA5_transitionS = {
            "\2\2\5\uffff\3\2\7\uffff\2\2\2\uffff\1\2\27\uffff\2\2\1\uffff"+
            "\1\2\14\uffff\1\2\7\uffff\1\1",
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

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "184:1: print_term : ( strstr | be_e0 );";
        }
    }
    static final String DFA7_eotS =
        "\14\uffff";
    static final String DFA7_eofS =
        "\14\uffff";
    static final String DFA7_minS =
        "\1\4\13\uffff";
    static final String DFA7_maxS =
        "\1\101\13\uffff";
    static final String DFA7_acceptS =
        "\1\uffff\1\1\1\2\11\uffff";
    static final String DFA7_specialS =
        "\14\uffff}>";
    static final String[] DFA7_transitionS = {
            "\2\2\5\uffff\2\2\1\1\7\uffff\2\2\2\uffff\1\2\27\uffff\2\2\1"+
            "\uffff\1\2\14\uffff\1\2",
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

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "200:1: be_e0 : ( EXISTS ( ID | PRIMED_ID ) ( COMMA ( ID | PRIMED_ID ) )* '.' be_e1 | be_e1 );";
        }
    }
    static final String DFA14_eotS =
        "\13\uffff";
    static final String DFA14_eofS =
        "\13\uffff";
    static final String DFA14_minS =
        "\1\4\12\uffff";
    static final String DFA14_maxS =
        "\1\101\12\uffff";
    static final String DFA14_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\6\uffff";
    static final String DFA14_specialS =
        "\13\uffff}>";
    static final String[] DFA14_transitionS = {
            "\2\4\5\uffff\1\2\1\3\10\uffff\2\4\2\uffff\1\4\27\uffff\2\4\1"+
            "\uffff\1\1\14\uffff\1\4",
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

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "245:1: be_nn : ( NOT be_ff | DOMAIN LPAR ID RPAR | RANGE LPAR ID RPAR | be_ff );";
        }
    }
 

    public static final BitSet FOLLOW_constr_input_in_constrs_input465 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_LPAR_C_in_constr_input478 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_constraints_in_constr_input480 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_RPAR_C_in_constr_input482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_calc_statement_in_calc508 = new BitSet(new long[]{0x000200000000C602L});
    public static final BitSet FOLLOW_calc_print_in_calc_statement520 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_calc_print_armc_in_calc_statement525 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_calc_store_in_calc_statement530 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_calc_termination_in_calc_statement535 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_calc_prefperiod_in_calc_statement540 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_calc_store550 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_74_in_calc_store553 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_be_e0_in_calc_store556 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_calc_store558 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRINT_in_calc_print569 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000202L});
    public static final BitSet FOLLOW_print_term_in_calc_print572 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000800L});
    public static final BitSet FOLLOW_COMMA_in_calc_print575 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000202L});
    public static final BitSet FOLLOW_print_term_in_calc_print578 = new BitSet(new long[]{0x0000000000020000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_calc_print582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRINT_ARMC_in_calc_print_armc593 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_be_e0_in_calc_print_armc596 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_calc_print_armc598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PREFPERIOD_in_calc_prefperiod609 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_LPAR_in_calc_prefperiod612 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_ID_in_calc_prefperiod615 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_COMMA_in_calc_prefperiod617 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_ID_in_calc_prefperiod620 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_COMMA_in_calc_prefperiod622 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_ID_in_calc_prefperiod625 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_RPAR_in_calc_prefperiod628 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_calc_prefperiod631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_strstr_in_print_term642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_be_e0_in_print_term646 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TERMINATION_in_calc_termination658 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_be_e0_in_calc_termination661 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_calc_termination663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_be_e0_in_constraints678 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXISTS_in_be_e0695 = new BitSet(new long[]{0x0006000000000000L});
    public static final BitSet FOLLOW_set_in_be_e0698 = new BitSet(new long[]{0x0000000000020000L,0x0000000000001000L});
    public static final BitSet FOLLOW_COMMA_in_be_e0707 = new BitSet(new long[]{0x0006000000000000L});
    public static final BitSet FOLLOW_set_in_be_e0710 = new BitSet(new long[]{0x0000000000020000L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_be_e0720 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_be_e1_in_be_e0723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_be_e1_in_be_e0728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_be_e2_in_be_e1741 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_be_ee1_in_be_e1743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OR_in_be_ee1769 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_be_e2_in_be_ee1772 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_be_ee1_in_be_ee1774 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_be_e3_in_be_e2787 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_be_ee2_in_be_e2789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMPOSE_in_be_ee2815 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_be_e3_in_be_ee2818 = new BitSet(new long[]{0x0000000000010002L});
    public static final BitSet FOLLOW_be_ee2_in_be_ee2820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_be_nn_in_be_e3832 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_be_ee3_in_be_e3834 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_be_ee3859 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_be_nn_in_be_ee3862 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_be_ee3_in_be_ee3864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_be_nn876 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_be_ff_in_be_nn879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOMAIN_in_be_nn884 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_LPAR_in_be_nn887 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_ID_in_be_nn890 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_RPAR_in_be_nn892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RANGE_in_be_nn898 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_LPAR_in_be_nn901 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_ID_in_be_nn904 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_RPAR_in_be_nn906 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_be_ff_in_be_nn912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constraint_in_be_ff943 = new BitSet(new long[]{0x07E0000000000002L});
    public static final BitSet FOLLOW_closure_in_be_ff946 = new BitSet(new long[]{0x0700000000000002L});
    public static final BitSet FOLLOW_abstr_in_be_ff951 = new BitSet(new long[]{0x0700000000000002L});
    public static final BitSet FOLLOW_LPAR_in_be_ff958 = new BitSet(new long[]{0x0016000002603830L,0x0000000000000002L});
    public static final BitSet FOLLOW_be_e0_in_be_ff961 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_RPAR_in_be_ff963 = new BitSet(new long[]{0x07E0000000000002L});
    public static final BitSet FOLLOW_closure_in_be_ff967 = new BitSet(new long[]{0x0700000000000002L});
    public static final BitSet FOLLOW_abstr_in_be_ff972 = new BitSet(new long[]{0x0700000000000002L});
    public static final BitSet FOLLOW_CL_PLUS_in_closure986 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_ID_in_closure989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CL_STAR_in_closure995 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_ID_in_closure998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CL_EXPR_in_closure1004 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_LPAR_in_closure1007 = new BitSet(new long[]{0x0006000002000030L,0x0000000000000002L});
    public static final BitSet FOLLOW_terms_in_closure1010 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_RPAR_in_closure1012 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ABSTR_D_in_abstr1025 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ABSTR_O_in_abstr1031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ABSTR_L_in_abstr1037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_terms_in_constraint1066 = new BitSet(new long[]{0xF800000000000100L,0x0000000000000001L});
    public static final BitSet FOLLOW_cop_in_constraint1068 = new BitSet(new long[]{0x0006000002000030L,0x0000000000000002L});
    public static final BitSet FOLLOW_terms_in_constraint1070 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_constraint1096 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_constraint1102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_constraint1116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_cop0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ae_aa_in_terms1159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pm_in_ae_aa1174 = new BitSet(new long[]{0x0006000002000030L,0x0000000000000002L});
    public static final BitSet FOLLOW_ae_mm_in_ae_aa1177 = new BitSet(new long[]{0x0000000000000032L});
    public static final BitSet FOLLOW_ae_aaa_in_ae_aa1179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pm_in_ae_aaa1207 = new BitSet(new long[]{0x0006000002000030L,0x0000000000000002L});
    public static final BitSet FOLLOW_ae_mm_in_ae_aaa1209 = new BitSet(new long[]{0x0000000000000032L});
    public static final BitSet FOLLOW_ae_aaa_in_ae_aaa1211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ae_ff_in_ae_mm1223 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_ae_mmm_in_ae_mm1225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MULT_in_ae_mmm1250 = new BitSet(new long[]{0x0006000002000030L,0x0000000000000002L});
    public static final BitSet FOLLOW_ae_ff_in_ae_mmm1253 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_ae_mmm_in_ae_mmm1255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAR_in_ae_ff1266 = new BitSet(new long[]{0x0006000002000030L,0x0000000000000002L});
    public static final BitSet FOLLOW_ae_aa_in_ae_ff1269 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_RPAR_in_ae_ff1271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRIMED_ID_in_ae_ff1277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_ae_ff1282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONST_in_ae_ff1288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pm0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRSTR_in_strstr1662 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constraint_in_synpred1_Calc923 = new BitSet(new long[]{0x07E0000000000002L});
    public static final BitSet FOLLOW_closure_in_synpred1_Calc926 = new BitSet(new long[]{0x0700000000000002L});
    public static final BitSet FOLLOW_abstr_in_synpred1_Calc931 = new BitSet(new long[]{0x0700000000000002L});
    public static final BitSet FOLLOW_terms_in_synpred2_Calc1054 = new BitSet(new long[]{0xF800000000000100L,0x0000000000000001L});
    public static final BitSet FOLLOW_cop_in_synpred2_Calc1056 = new BitSet(new long[]{0x0006000002000030L,0x0000000000000002L});
    public static final BitSet FOLLOW_terms_in_synpred2_Calc1058 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred3_Calc1091 = new BitSet(new long[]{0x0000000000000002L});

}