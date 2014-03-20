grammar Calc;

options {
	k=1; // lookahead
	output=AST;
	ASTLabelType=CommonTree; // type of $stat.tree ref etc...
} 



tokens {

//	NEQ = '!=';

	PLUS = '+' ;
	MINUS = '-' ;
	MULT = '*' ;

//	NOT = '!' ;
	AND = '&&' ;
//	OR = '||' ; // handled together with DIVIDES

	EQ = '=' ;
//	DIVIDES = '|' ; // handled together with OR

	// calculator
	PRINT = 'print';
	PRINT_ARMC = 'print_armc';
	DOMAIN = 'domain';
	RANGE = 'range';
	EXISTS = 'exists';
	TERMINATION = 'termination';
	PREFPERIOD = 'closure';	
	COMPOSE = '@' ;
	
	COMMA = ',' ;
	WN = 'wN' ;
	ARRAYS = 'arrays' ;
	SCALARS = 'scalars' ;
	
	TRUE = 'true' ;
	FALSE = 'false' ;
	LPAR_C = '{' ;
	RPAR_C = '}' ;
	LPAR = '(' ;
	RPAR = ')' ;
	
    LOCALS = 'locals';
    GLOBALS = 'globals';
    PORT_IN = 'in';
    PORT_OUT = 'out';

	// imaginary AST tokens
	AUTOMATA ;
	AUTOMATON ;
	AUTOMATON_WITH_SYMBOLS ;
	INITIAL = 'initial';
	FINAL = 'final';
	ERROR = 'error';
	TRANSITIONS ;
	TRANSITION ;
	TERMS ;
	GUARDS ;
	GUARD ;
	ACTIONS ;
	ACTION ;
	CONSTRAINTS ;
	CONSTRAINT ;
	CALL ;
	
	CONSTR_INPUT ;

	ASSIGN = ':=';
}

@header {
	package verimag.flata.parsers;

	//import verimag.flata.parsers.CALexer;
}
@lexer::header {
	package verimag.flata.parsers;
}


@lexer::members {
	public Token nextToken() {
		while (true) {
			this.state.token = null;
			this.state.channel = Token.DEFAULT_CHANNEL;
			this.state.tokenStartCharIndex = input.index();
			this.state.tokenStartCharPositionInLine = input.getCharPositionInLine();
			this.state.tokenStartLine = input.getLine();
			this.state.text = null;
			if ( input.LA(1)==CharStream.EOF ) {
				return Token.EOF_TOKEN;
			}
			try {
				mTokens();
				if ( this.state.token==null ) {
					emit();
				}
				else if ( this.state.token==Token.SKIP_TOKEN ) {
					continue;
				}
				return this.state.token;
			}
			catch (RecognitionException re) {
				reportError(re);
				throw new RuntimeException("Parsing ended."); // or throw Error
			}
		}
	}
}

// Optional step: Disable automatic error recovery
@members {
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
}
// Alter code generation so catch-clauses get replace with
// this action.
@rulecatch {
	//catch (RecognitionException e) {
	//	throw e;
	//}
}
@lexer::rulecatch {
	catch (RecognitionException e) {
		//throw e;
		System.exit(-1);
	}
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

constrs_input
	: ( constr_input )*
	;
constr_input
	: LPAR_C constraints RPAR_C
	  -> ^( CONSTR_INPUT constraints)
	;
	

calc
	: (calc_statement)*
	;
calc_statement
	: calc_print
	| calc_print_armc
	| calc_store
	| calc_termination
	| calc_prefperiod
	;
calc_store
	: ID^ ':'! be_e0 ';'!
	;
calc_print
	: PRINT^ print_term (','! print_term)* ';'!
	;
calc_print_armc
	: PRINT_ARMC^ be_e0 ';'!
	;
calc_prefperiod
	: PREFPERIOD^ LPAR! ID COMMA! ID COMMA! ID  RPAR! ';'!
	;
print_term
	: strstr | be_e0
	;
	
calc_termination
	: TERMINATION^ be_e0 ';'!
	;

// constraints: any boolean combination of atomic constraints (linear and modulo)


constraints
	: be_e0
	;


/** note: parsing doesn't work for the following:
 *  be_dd 	// disjunction
	  : be_cc be_ddd
        -> ^(OR be_cc be_ddd)
	  ;
    be_ddd 
      : // epsilon
      | OR! be_cc be_ddd
      ;
 * use the following instead
 *  be_dd 	// disjunction
	  : be_cc be_ddd?
        -> ^(OR be_cc be_ddd?)
	  ;
    be_ddd 
      : OR! be_cc be_ddd?
      ;
 */
/* ------------------ boolean expressions ---------------------- */
be_e0	// existentials allowed in calculator
	: EXISTS^ (ID | PRIMED_ID) (COMMA! (ID | PRIMED_ID))* '.'! be_e1
	| be_e1
	;

be_e1 	// disjunction
	: be_e2 be_ee1?
	  -> ^(OR be_e2 be_ee1?)
	;
be_ee1 
	: OR! be_e2 be_ee1?
	;
be_e2 	// composition
	: be_e3 be_ee2?
	  -> ^(COMPOSE be_e3 be_ee2?)
	;
be_ee2 
	: COMPOSE! be_e3 be_ee2?
	;
be_e3	// conjunction
	: be_nn be_ee3?
	  -> ^(AND be_nn be_ee3?)
	;
be_ee3
	: AND! be_nn be_ee3?
	;
be_nn	// negation
	: NOT^ be_ff
	| DOMAIN^ LPAR! ID RPAR!
	| RANGE^ LPAR! ID RPAR!
	| be_ff
	;
be_ff
	: (constraint (closure)? (abstr)* ) => 
	  constraint (closure)? (abstr)*
	| LPAR! be_e0 RPAR! (closure)? (abstr)*
	;
	
closure
	: CL_PLUS^ ID?
	| CL_STAR^ ID?
	| CL_EXPR^ LPAR! terms RPAR! 
	;

abstr
	: ABSTR_D^
	| ABSTR_O^
	| ABSTR_L^
	;

// priority of closure operators is higher than of any boolean operator
// priority of composition operator is higher than of disjunction an lower than of conjunction
// closure nodes in ASTs are always children of AND 

constraint
	: (terms cop terms) =>
	  terms cop terms
	  -> ^(CONSTRAINT terms cop terms)
	| (ID) => ID // used by calculator to store relations
	| TRUE
	  -> ^(TRUE)
	| FALSE
	  -> ^(FALSE)
	;
cop
	: EQ|NEQ|LEQ|LESS|GEQ|GREATER|DIVIDES
	;


terms
	: ae_aa
	;

/* ------------------ arithmetic expressions ---------------------- */
ae_aa 	// addition
	: pm? ae_mm ae_aaa?
	 -> ^(PLUS pm? ae_mm ae_aaa?)
	;
ae_aaa 
	: pm ae_mm ae_aaa?
	;
ae_mm	// multiplication
	: ae_ff ae_mmm?
	  -> ^(MULT ae_ff ae_mmm?)
	;
ae_mmm
	: MULT! ae_ff ae_mmm?
	;
ae_ff
	: LPAR! ae_aa RPAR!
	| PRIMED_ID
	| ID 
	| CONST
	;
pm
	: PLUS
	| MINUS
	;


/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/


CONST 
	:	  (NUM)+ 
	;

ID 
	:	  ALPHA (ALPHANUM)* ( '\'' {$type=PRIMED_ID; } )?
	;
LESS
	:	'<' (EQ {$type=LEQ;} )?
	;
GREATER
	:	'>' (EQ {$type=GEQ;} )?	
	;
DIVIDES
	:	'|' ('|' {$type=OR;} )?	
	;
NOT
	:	'!' ('=' {$type=NEQ;} )?	
	;
CL_EXPR
	:	'^' (   '+' {$type=CL_PLUS;}
	          | '*' {$type=CL_STAR;}
	        )?
	;
ABSTR
	:	'#' (   'D' {$type=ABSTR_D;}
	          | 'O' {$type=ABSTR_O;}
	          | 'L' {$type=ABSTR_L;}
	        )
	;


WHITESPACE 
	:	(	' ' 
			| '\t' 
			| '\f' 
			| '\r' 
			| '\n' 
		)+ 	
	{ $channel = HIDDEN; } 
	;

SINGLE_COMMENT: '//' (~('\r' | '\n'))* { skip(); };
MULTI_COMMENT options { greedy = false; }
  : '/*' .* '*/' { skip(); }
//  | 'strategy' (ALPHANUM)* LPAR_C .* RPAR_C { skip(); }
  ;

strstr
	:  STRSTR
	;

STRSTR
	: '"' 
	  (~('"'))*
	  '"'
	;

fragment NUM
	:	  '0'..'9'
	;
fragment ALPHA 
	:	  'a'..'z' 
		| 'A'..'Z'
		| '_'
	;
fragment ALPHANUM
	:	  NUM 
		| ALPHA
		| '#'
	;
