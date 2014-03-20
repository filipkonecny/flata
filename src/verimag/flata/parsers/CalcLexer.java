// $ANTLR 3.3 Nov 30, 2010 12:50:56 Calc.g 2013-11-11 16:03:47

	package verimag.flata.parsers;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class CalcLexer extends Lexer {
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


    // delegates
    // delegators

    public CalcLexer() {;} 
    public CalcLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public CalcLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "Calc.g"; }

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:42:6: ( '+' )
            // Calc.g:42:8: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:43:7: ( '-' )
            // Calc.g:43:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "MULT"
    public final void mMULT() throws RecognitionException {
        try {
            int _type = MULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:44:6: ( '*' )
            // Calc.g:44:8: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MULT"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:45:5: ( '&&' )
            // Calc.g:45:7: '&&'
            {
            match("&&"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "EQ"
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:46:4: ( '=' )
            // Calc.g:46:6: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQ"

    // $ANTLR start "PRINT"
    public final void mPRINT() throws RecognitionException {
        try {
            int _type = PRINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:47:7: ( 'print' )
            // Calc.g:47:9: 'print'
            {
            match("print"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PRINT"

    // $ANTLR start "PRINT_ARMC"
    public final void mPRINT_ARMC() throws RecognitionException {
        try {
            int _type = PRINT_ARMC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:48:12: ( 'print_armc' )
            // Calc.g:48:14: 'print_armc'
            {
            match("print_armc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PRINT_ARMC"

    // $ANTLR start "DOMAIN"
    public final void mDOMAIN() throws RecognitionException {
        try {
            int _type = DOMAIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:49:8: ( 'domain' )
            // Calc.g:49:10: 'domain'
            {
            match("domain"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOMAIN"

    // $ANTLR start "RANGE"
    public final void mRANGE() throws RecognitionException {
        try {
            int _type = RANGE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:50:7: ( 'range' )
            // Calc.g:50:9: 'range'
            {
            match("range"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RANGE"

    // $ANTLR start "EXISTS"
    public final void mEXISTS() throws RecognitionException {
        try {
            int _type = EXISTS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:51:8: ( 'exists' )
            // Calc.g:51:10: 'exists'
            {
            match("exists"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXISTS"

    // $ANTLR start "TERMINATION"
    public final void mTERMINATION() throws RecognitionException {
        try {
            int _type = TERMINATION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:52:13: ( 'termination' )
            // Calc.g:52:15: 'termination'
            {
            match("termination"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TERMINATION"

    // $ANTLR start "PREFPERIOD"
    public final void mPREFPERIOD() throws RecognitionException {
        try {
            int _type = PREFPERIOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:53:12: ( 'closure' )
            // Calc.g:53:14: 'closure'
            {
            match("closure"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PREFPERIOD"

    // $ANTLR start "COMPOSE"
    public final void mCOMPOSE() throws RecognitionException {
        try {
            int _type = COMPOSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:54:9: ( '@' )
            // Calc.g:54:11: '@'
            {
            match('@'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPOSE"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:55:7: ( ',' )
            // Calc.g:55:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "WN"
    public final void mWN() throws RecognitionException {
        try {
            int _type = WN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:56:4: ( 'wN' )
            // Calc.g:56:6: 'wN'
            {
            match("wN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WN"

    // $ANTLR start "ARRAYS"
    public final void mARRAYS() throws RecognitionException {
        try {
            int _type = ARRAYS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:57:8: ( 'arrays' )
            // Calc.g:57:10: 'arrays'
            {
            match("arrays"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ARRAYS"

    // $ANTLR start "SCALARS"
    public final void mSCALARS() throws RecognitionException {
        try {
            int _type = SCALARS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:58:9: ( 'scalars' )
            // Calc.g:58:11: 'scalars'
            {
            match("scalars"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SCALARS"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:59:6: ( 'true' )
            // Calc.g:59:8: 'true'
            {
            match("true"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:60:7: ( 'false' )
            // Calc.g:60:9: 'false'
            {
            match("false"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "LPAR_C"
    public final void mLPAR_C() throws RecognitionException {
        try {
            int _type = LPAR_C;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:61:8: ( '{' )
            // Calc.g:61:10: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAR_C"

    // $ANTLR start "RPAR_C"
    public final void mRPAR_C() throws RecognitionException {
        try {
            int _type = RPAR_C;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:62:8: ( '}' )
            // Calc.g:62:10: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAR_C"

    // $ANTLR start "LPAR"
    public final void mLPAR() throws RecognitionException {
        try {
            int _type = LPAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:63:6: ( '(' )
            // Calc.g:63:8: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAR"

    // $ANTLR start "RPAR"
    public final void mRPAR() throws RecognitionException {
        try {
            int _type = RPAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:64:6: ( ')' )
            // Calc.g:64:8: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAR"

    // $ANTLR start "LOCALS"
    public final void mLOCALS() throws RecognitionException {
        try {
            int _type = LOCALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:65:8: ( 'locals' )
            // Calc.g:65:10: 'locals'
            {
            match("locals"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LOCALS"

    // $ANTLR start "GLOBALS"
    public final void mGLOBALS() throws RecognitionException {
        try {
            int _type = GLOBALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:66:9: ( 'globals' )
            // Calc.g:66:11: 'globals'
            {
            match("globals"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GLOBALS"

    // $ANTLR start "PORT_IN"
    public final void mPORT_IN() throws RecognitionException {
        try {
            int _type = PORT_IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:67:9: ( 'in' )
            // Calc.g:67:11: 'in'
            {
            match("in"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PORT_IN"

    // $ANTLR start "PORT_OUT"
    public final void mPORT_OUT() throws RecognitionException {
        try {
            int _type = PORT_OUT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:68:10: ( 'out' )
            // Calc.g:68:12: 'out'
            {
            match("out"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PORT_OUT"

    // $ANTLR start "INITIAL"
    public final void mINITIAL() throws RecognitionException {
        try {
            int _type = INITIAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:69:9: ( 'initial' )
            // Calc.g:69:11: 'initial'
            {
            match("initial"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INITIAL"

    // $ANTLR start "FINAL"
    public final void mFINAL() throws RecognitionException {
        try {
            int _type = FINAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:70:7: ( 'final' )
            // Calc.g:70:9: 'final'
            {
            match("final"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FINAL"

    // $ANTLR start "ERROR"
    public final void mERROR() throws RecognitionException {
        try {
            int _type = ERROR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:71:7: ( 'error' )
            // Calc.g:71:9: 'error'
            {
            match("error"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ERROR"

    // $ANTLR start "ASSIGN"
    public final void mASSIGN() throws RecognitionException {
        try {
            int _type = ASSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:72:8: ( ':=' )
            // Calc.g:72:10: ':='
            {
            match(":="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ASSIGN"

    // $ANTLR start "T__74"
    public final void mT__74() throws RecognitionException {
        try {
            int _type = T__74;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:73:7: ( ':' )
            // Calc.g:73:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__74"

    // $ANTLR start "T__75"
    public final void mT__75() throws RecognitionException {
        try {
            int _type = T__75;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:74:7: ( ';' )
            // Calc.g:74:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__75"

    // $ANTLR start "T__76"
    public final void mT__76() throws RecognitionException {
        try {
            int _type = T__76;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:75:7: ( '.' )
            // Calc.g:75:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__76"

    // $ANTLR start "CONST"
    public final void mCONST() throws RecognitionException {
        try {
            int _type = CONST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:325:2: ( ( NUM )+ )
            // Calc.g:325:6: ( NUM )+
            {
            // Calc.g:325:6: ( NUM )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // Calc.g:325:7: NUM
            	    {
            	    mNUM(); 

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CONST"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:329:2: ( ALPHA ( ALPHANUM )* ( '\\'' )? )
            // Calc.g:329:6: ALPHA ( ALPHANUM )* ( '\\'' )?
            {
            mALPHA(); 
            // Calc.g:329:12: ( ALPHANUM )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='#'||(LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // Calc.g:329:13: ALPHANUM
            	    {
            	    mALPHANUM(); 

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // Calc.g:329:24: ( '\\'' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='\'') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // Calc.g:329:26: '\\''
                    {
                    match('\''); 
                    _type=PRIMED_ID; 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "LESS"
    public final void mLESS() throws RecognitionException {
        try {
            int _type = LESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:332:2: ( '<' ( EQ )? )
            // Calc.g:332:4: '<' ( EQ )?
            {
            match('<'); 
            // Calc.g:332:8: ( EQ )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='=') ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // Calc.g:332:9: EQ
                    {
                    mEQ(); 
                    _type=LEQ;

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS"

    // $ANTLR start "GREATER"
    public final void mGREATER() throws RecognitionException {
        try {
            int _type = GREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:335:2: ( '>' ( EQ )? )
            // Calc.g:335:4: '>' ( EQ )?
            {
            match('>'); 
            // Calc.g:335:8: ( EQ )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='=') ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // Calc.g:335:9: EQ
                    {
                    mEQ(); 
                    _type=GEQ;

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER"

    // $ANTLR start "DIVIDES"
    public final void mDIVIDES() throws RecognitionException {
        try {
            int _type = DIVIDES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:338:2: ( '|' ( '|' )? )
            // Calc.g:338:4: '|' ( '|' )?
            {
            match('|'); 
            // Calc.g:338:8: ( '|' )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='|') ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // Calc.g:338:9: '|'
                    {
                    match('|'); 
                    _type=OR;

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DIVIDES"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:341:2: ( '!' ( '=' )? )
            // Calc.g:341:4: '!' ( '=' )?
            {
            match('!'); 
            // Calc.g:341:8: ( '=' )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='=') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // Calc.g:341:9: '='
                    {
                    match('='); 
                    _type=NEQ;

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "CL_EXPR"
    public final void mCL_EXPR() throws RecognitionException {
        try {
            int _type = CL_EXPR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:344:2: ( '^' ( '+' | '*' )? )
            // Calc.g:344:4: '^' ( '+' | '*' )?
            {
            match('^'); 
            // Calc.g:344:8: ( '+' | '*' )?
            int alt8=3;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='+') ) {
                alt8=1;
            }
            else if ( (LA8_0=='*') ) {
                alt8=2;
            }
            switch (alt8) {
                case 1 :
                    // Calc.g:344:12: '+'
                    {
                    match('+'); 
                    _type=CL_PLUS;

                    }
                    break;
                case 2 :
                    // Calc.g:345:14: '*'
                    {
                    match('*'); 
                    _type=CL_STAR;

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CL_EXPR"

    // $ANTLR start "ABSTR"
    public final void mABSTR() throws RecognitionException {
        try {
            int _type = ABSTR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:349:2: ( '#' ( 'D' | 'O' | 'L' ) )
            // Calc.g:349:4: '#' ( 'D' | 'O' | 'L' )
            {
            match('#'); 
            // Calc.g:349:8: ( 'D' | 'O' | 'L' )
            int alt9=3;
            switch ( input.LA(1) ) {
            case 'D':
                {
                alt9=1;
                }
                break;
            case 'O':
                {
                alt9=2;
                }
                break;
            case 'L':
                {
                alt9=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }

            switch (alt9) {
                case 1 :
                    // Calc.g:349:12: 'D'
                    {
                    match('D'); 
                    _type=ABSTR_D;

                    }
                    break;
                case 2 :
                    // Calc.g:350:14: 'O'
                    {
                    match('O'); 
                    _type=ABSTR_O;

                    }
                    break;
                case 3 :
                    // Calc.g:351:14: 'L'
                    {
                    match('L'); 
                    _type=ABSTR_L;

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ABSTR"

    // $ANTLR start "WHITESPACE"
    public final void mWHITESPACE() throws RecognitionException {
        try {
            int _type = WHITESPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:357:2: ( ( ' ' | '\\t' | '\\f' | '\\r' | '\\n' )+ )
            // Calc.g:357:4: ( ' ' | '\\t' | '\\f' | '\\r' | '\\n' )+
            {
            // Calc.g:357:4: ( ' ' | '\\t' | '\\f' | '\\r' | '\\n' )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='\t' && LA10_0<='\n')||(LA10_0>='\f' && LA10_0<='\r')||LA10_0==' ') ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // Calc.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHITESPACE"

    // $ANTLR start "SINGLE_COMMENT"
    public final void mSINGLE_COMMENT() throws RecognitionException {
        try {
            int _type = SINGLE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:366:15: ( '//' (~ ( '\\r' | '\\n' ) )* )
            // Calc.g:366:17: '//' (~ ( '\\r' | '\\n' ) )*
            {
            match("//"); 

            // Calc.g:366:22: (~ ( '\\r' | '\\n' ) )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>='\u0000' && LA11_0<='\t')||(LA11_0>='\u000B' && LA11_0<='\f')||(LA11_0>='\u000E' && LA11_0<='\uFFFF')) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // Calc.g:366:23: ~ ( '\\r' | '\\n' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);

             skip(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SINGLE_COMMENT"

    // $ANTLR start "MULTI_COMMENT"
    public final void mMULTI_COMMENT() throws RecognitionException {
        try {
            int _type = MULTI_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:368:3: ( '/*' ( . )* '*/' )
            // Calc.g:368:5: '/*' ( . )* '*/'
            {
            match("/*"); 

            // Calc.g:368:10: ( . )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0=='*') ) {
                    int LA12_1 = input.LA(2);

                    if ( (LA12_1=='/') ) {
                        alt12=2;
                    }
                    else if ( ((LA12_1>='\u0000' && LA12_1<='.')||(LA12_1>='0' && LA12_1<='\uFFFF')) ) {
                        alt12=1;
                    }


                }
                else if ( ((LA12_0>='\u0000' && LA12_0<=')')||(LA12_0>='+' && LA12_0<='\uFFFF')) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // Calc.g:368:10: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);

            match("*/"); 

             skip(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MULTI_COMMENT"

    // $ANTLR start "STRSTR"
    public final void mSTRSTR() throws RecognitionException {
        try {
            int _type = STRSTR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Calc.g:377:2: ( '\"' (~ ( '\"' ) )* '\"' )
            // Calc.g:377:4: '\"' (~ ( '\"' ) )* '\"'
            {
            match('\"'); 
            // Calc.g:378:4: (~ ( '\"' ) )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( ((LA13_0>='\u0000' && LA13_0<='!')||(LA13_0>='#' && LA13_0<='\uFFFF')) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // Calc.g:378:5: ~ ( '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRSTR"

    // $ANTLR start "NUM"
    public final void mNUM() throws RecognitionException {
        try {
            // Calc.g:383:2: ( '0' .. '9' )
            // Calc.g:383:6: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "NUM"

    // $ANTLR start "ALPHA"
    public final void mALPHA() throws RecognitionException {
        try {
            // Calc.g:386:2: ( 'a' .. 'z' | 'A' .. 'Z' | '_' )
            // Calc.g:
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "ALPHA"

    // $ANTLR start "ALPHANUM"
    public final void mALPHANUM() throws RecognitionException {
        try {
            // Calc.g:391:2: ( NUM | ALPHA | '#' )
            // Calc.g:
            {
            if ( input.LA(1)=='#'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "ALPHANUM"

    public void mTokens() throws RecognitionException {
        // Calc.g:1:8: ( PLUS | MINUS | MULT | AND | EQ | PRINT | PRINT_ARMC | DOMAIN | RANGE | EXISTS | TERMINATION | PREFPERIOD | COMPOSE | COMMA | WN | ARRAYS | SCALARS | TRUE | FALSE | LPAR_C | RPAR_C | LPAR | RPAR | LOCALS | GLOBALS | PORT_IN | PORT_OUT | INITIAL | FINAL | ERROR | ASSIGN | T__74 | T__75 | T__76 | CONST | ID | LESS | GREATER | DIVIDES | NOT | CL_EXPR | ABSTR | WHITESPACE | SINGLE_COMMENT | MULTI_COMMENT | STRSTR )
        int alt14=46;
        alt14 = dfa14.predict(input);
        switch (alt14) {
            case 1 :
                // Calc.g:1:10: PLUS
                {
                mPLUS(); 

                }
                break;
            case 2 :
                // Calc.g:1:15: MINUS
                {
                mMINUS(); 

                }
                break;
            case 3 :
                // Calc.g:1:21: MULT
                {
                mMULT(); 

                }
                break;
            case 4 :
                // Calc.g:1:26: AND
                {
                mAND(); 

                }
                break;
            case 5 :
                // Calc.g:1:30: EQ
                {
                mEQ(); 

                }
                break;
            case 6 :
                // Calc.g:1:33: PRINT
                {
                mPRINT(); 

                }
                break;
            case 7 :
                // Calc.g:1:39: PRINT_ARMC
                {
                mPRINT_ARMC(); 

                }
                break;
            case 8 :
                // Calc.g:1:50: DOMAIN
                {
                mDOMAIN(); 

                }
                break;
            case 9 :
                // Calc.g:1:57: RANGE
                {
                mRANGE(); 

                }
                break;
            case 10 :
                // Calc.g:1:63: EXISTS
                {
                mEXISTS(); 

                }
                break;
            case 11 :
                // Calc.g:1:70: TERMINATION
                {
                mTERMINATION(); 

                }
                break;
            case 12 :
                // Calc.g:1:82: PREFPERIOD
                {
                mPREFPERIOD(); 

                }
                break;
            case 13 :
                // Calc.g:1:93: COMPOSE
                {
                mCOMPOSE(); 

                }
                break;
            case 14 :
                // Calc.g:1:101: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 15 :
                // Calc.g:1:107: WN
                {
                mWN(); 

                }
                break;
            case 16 :
                // Calc.g:1:110: ARRAYS
                {
                mARRAYS(); 

                }
                break;
            case 17 :
                // Calc.g:1:117: SCALARS
                {
                mSCALARS(); 

                }
                break;
            case 18 :
                // Calc.g:1:125: TRUE
                {
                mTRUE(); 

                }
                break;
            case 19 :
                // Calc.g:1:130: FALSE
                {
                mFALSE(); 

                }
                break;
            case 20 :
                // Calc.g:1:136: LPAR_C
                {
                mLPAR_C(); 

                }
                break;
            case 21 :
                // Calc.g:1:143: RPAR_C
                {
                mRPAR_C(); 

                }
                break;
            case 22 :
                // Calc.g:1:150: LPAR
                {
                mLPAR(); 

                }
                break;
            case 23 :
                // Calc.g:1:155: RPAR
                {
                mRPAR(); 

                }
                break;
            case 24 :
                // Calc.g:1:160: LOCALS
                {
                mLOCALS(); 

                }
                break;
            case 25 :
                // Calc.g:1:167: GLOBALS
                {
                mGLOBALS(); 

                }
                break;
            case 26 :
                // Calc.g:1:175: PORT_IN
                {
                mPORT_IN(); 

                }
                break;
            case 27 :
                // Calc.g:1:183: PORT_OUT
                {
                mPORT_OUT(); 

                }
                break;
            case 28 :
                // Calc.g:1:192: INITIAL
                {
                mINITIAL(); 

                }
                break;
            case 29 :
                // Calc.g:1:200: FINAL
                {
                mFINAL(); 

                }
                break;
            case 30 :
                // Calc.g:1:206: ERROR
                {
                mERROR(); 

                }
                break;
            case 31 :
                // Calc.g:1:212: ASSIGN
                {
                mASSIGN(); 

                }
                break;
            case 32 :
                // Calc.g:1:219: T__74
                {
                mT__74(); 

                }
                break;
            case 33 :
                // Calc.g:1:225: T__75
                {
                mT__75(); 

                }
                break;
            case 34 :
                // Calc.g:1:231: T__76
                {
                mT__76(); 

                }
                break;
            case 35 :
                // Calc.g:1:237: CONST
                {
                mCONST(); 

                }
                break;
            case 36 :
                // Calc.g:1:243: ID
                {
                mID(); 

                }
                break;
            case 37 :
                // Calc.g:1:246: LESS
                {
                mLESS(); 

                }
                break;
            case 38 :
                // Calc.g:1:251: GREATER
                {
                mGREATER(); 

                }
                break;
            case 39 :
                // Calc.g:1:259: DIVIDES
                {
                mDIVIDES(); 

                }
                break;
            case 40 :
                // Calc.g:1:267: NOT
                {
                mNOT(); 

                }
                break;
            case 41 :
                // Calc.g:1:271: CL_EXPR
                {
                mCL_EXPR(); 

                }
                break;
            case 42 :
                // Calc.g:1:279: ABSTR
                {
                mABSTR(); 

                }
                break;
            case 43 :
                // Calc.g:1:285: WHITESPACE
                {
                mWHITESPACE(); 

                }
                break;
            case 44 :
                // Calc.g:1:296: SINGLE_COMMENT
                {
                mSINGLE_COMMENT(); 

                }
                break;
            case 45 :
                // Calc.g:1:311: MULTI_COMMENT
                {
                mMULTI_COMMENT(); 

                }
                break;
            case 46 :
                // Calc.g:1:325: STRSTR
                {
                mSTRSTR(); 

                }
                break;

        }

    }


    protected DFA14 dfa14 = new DFA14(this);
    static final String DFA14_eotS =
        "\6\uffff\6\36\2\uffff\4\36\4\uffff\4\36\1\72\15\uffff\10\36\1\105"+
        "\6\36\1\115\1\36\4\uffff\10\36\1\uffff\7\36\1\uffff\1\136\6\36\1"+
        "\145\10\36\1\uffff\1\157\1\36\1\161\1\36\1\163\1\36\1\uffff\3\36"+
        "\1\170\1\171\4\36\1\uffff\1\176\1\uffff\1\177\1\uffff\2\36\1\u0082"+
        "\1\36\2\uffff\1\u0084\3\36\2\uffff\1\36\1\u0089\1\uffff\1\u008a"+
        "\1\uffff\1\u008b\1\u008c\2\36\4\uffff\2\36\1\u0091\1\36\1\uffff"+
        "\1\u0093\1\uffff";
    static final String DFA14_eofS =
        "\u0094\uffff";
    static final String DFA14_minS =
        "\1\11\5\uffff\1\162\1\157\1\141\1\162\1\145\1\154\2\uffff\1\116"+
        "\1\162\1\143\1\141\4\uffff\1\157\1\154\1\156\1\165\1\75\13\uffff"+
        "\1\52\1\uffff\1\151\1\155\1\156\1\151\2\162\1\165\1\157\1\43\1\162"+
        "\1\141\1\154\1\156\1\143\1\157\1\43\1\164\4\uffff\1\156\1\141\1"+
        "\147\1\163\1\157\1\155\1\145\1\163\1\uffff\1\141\1\154\1\163\2\141"+
        "\1\142\1\164\1\uffff\1\43\1\164\1\151\1\145\1\164\1\162\1\151\1"+
        "\43\1\165\1\171\1\141\1\145\2\154\1\141\1\151\1\uffff\1\43\1\156"+
        "\1\43\1\163\1\43\1\156\1\uffff\1\162\1\163\1\162\2\43\1\163\1\154"+
        "\2\141\1\uffff\1\43\1\uffff\1\43\1\uffff\1\141\1\145\1\43\1\163"+
        "\2\uffff\1\43\1\163\1\154\1\162\2\uffff\1\164\1\43\1\uffff\1\43"+
        "\1\uffff\2\43\1\155\1\151\4\uffff\1\143\1\157\1\43\1\156\1\uffff"+
        "\1\43\1\uffff";
    static final String DFA14_maxS =
        "\1\175\5\uffff\1\162\1\157\1\141\1\170\1\162\1\154\2\uffff\1\116"+
        "\1\162\1\143\1\151\4\uffff\1\157\1\154\1\156\1\165\1\75\13\uffff"+
        "\1\57\1\uffff\1\151\1\155\1\156\1\151\2\162\1\165\1\157\1\172\1"+
        "\162\1\141\1\154\1\156\1\143\1\157\1\172\1\164\4\uffff\1\156\1\141"+
        "\1\147\1\163\1\157\1\155\1\145\1\163\1\uffff\1\141\1\154\1\163\2"+
        "\141\1\142\1\164\1\uffff\1\172\1\164\1\151\1\145\1\164\1\162\1\151"+
        "\1\172\1\165\1\171\1\141\1\145\2\154\1\141\1\151\1\uffff\1\172\1"+
        "\156\1\172\1\163\1\172\1\156\1\uffff\1\162\1\163\1\162\2\172\1\163"+
        "\1\154\2\141\1\uffff\1\172\1\uffff\1\172\1\uffff\1\141\1\145\1\172"+
        "\1\163\2\uffff\1\172\1\163\1\154\1\162\2\uffff\1\164\1\172\1\uffff"+
        "\1\172\1\uffff\2\172\1\155\1\151\4\uffff\1\143\1\157\1\172\1\156"+
        "\1\uffff\1\172\1\uffff";
    static final String DFA14_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\6\uffff\1\15\1\16\4\uffff\1\24\1\25"+
        "\1\26\1\27\5\uffff\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51"+
        "\1\52\1\53\1\uffff\1\56\21\uffff\1\37\1\40\1\54\1\55\10\uffff\1"+
        "\17\7\uffff\1\32\20\uffff\1\33\6\uffff\1\22\11\uffff\1\6\1\uffff"+
        "\1\11\1\uffff\1\36\4\uffff\1\23\1\35\4\uffff\1\10\1\12\2\uffff\1"+
        "\20\1\uffff\1\30\4\uffff\1\14\1\21\1\31\1\34\4\uffff\1\7\1\uffff"+
        "\1\13";
    static final String DFA14_specialS =
        "\u0094\uffff}>";
    static final String[] DFA14_transitionS = {
            "\2\45\1\uffff\2\45\22\uffff\1\45\1\42\1\47\1\44\2\uffff\1\4"+
            "\1\uffff\1\24\1\25\1\3\1\1\1\15\1\2\1\34\1\46\12\35\1\32\1\33"+
            "\1\37\1\5\1\40\1\uffff\1\14\32\36\3\uffff\1\43\1\36\1\uffff"+
            "\1\17\1\36\1\13\1\7\1\11\1\21\1\27\1\36\1\30\2\36\1\26\2\36"+
            "\1\31\1\6\1\36\1\10\1\20\1\12\2\36\1\16\3\36\1\22\1\41\1\23",
            "",
            "",
            "",
            "",
            "",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\54\5\uffff\1\53",
            "\1\55\14\uffff\1\56",
            "\1\57",
            "",
            "",
            "\1\60",
            "\1\61",
            "\1\62",
            "\1\63\7\uffff\1\64",
            "",
            "",
            "",
            "",
            "\1\65",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
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
            "\1\74\4\uffff\1\73",
            "",
            "\1\75",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\113",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\10\36\1\114\21\36",
            "\1\116",
            "",
            "",
            "",
            "",
            "\1\117",
            "\1\120",
            "\1\121",
            "\1\122",
            "\1\123",
            "\1\124",
            "\1\125",
            "\1\126",
            "",
            "\1\127",
            "\1\130",
            "\1\131",
            "\1\132",
            "\1\133",
            "\1\134",
            "\1\135",
            "",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\137",
            "\1\140",
            "\1\141",
            "\1\142",
            "\1\143",
            "\1\144",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\146",
            "\1\147",
            "\1\150",
            "\1\151",
            "\1\152",
            "\1\153",
            "\1\154",
            "\1\155",
            "",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\156"+
            "\1\uffff\32\36",
            "\1\160",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\162",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\164",
            "",
            "\1\165",
            "\1\166",
            "\1\167",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\172",
            "\1\173",
            "\1\174",
            "\1\175",
            "",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "",
            "\1\u0080",
            "\1\u0081",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\u0083",
            "",
            "",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\u0085",
            "\1\u0086",
            "\1\u0087",
            "",
            "",
            "\1\u0088",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\u008d",
            "\1\u008e",
            "",
            "",
            "",
            "",
            "\1\u008f",
            "\1\u0090",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
            "\1\u0092",
            "",
            "\1\36\3\uffff\1\36\10\uffff\12\36\7\uffff\32\36\4\uffff\1\36"+
            "\1\uffff\32\36",
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
            return "1:1: Tokens : ( PLUS | MINUS | MULT | AND | EQ | PRINT | PRINT_ARMC | DOMAIN | RANGE | EXISTS | TERMINATION | PREFPERIOD | COMPOSE | COMMA | WN | ARRAYS | SCALARS | TRUE | FALSE | LPAR_C | RPAR_C | LPAR | RPAR | LOCALS | GLOBALS | PORT_IN | PORT_OUT | INITIAL | FINAL | ERROR | ASSIGN | T__74 | T__75 | T__76 | CONST | ID | LESS | GREATER | DIVIDES | NOT | CL_EXPR | ABSTR | WHITESPACE | SINGLE_COMMENT | MULTI_COMMENT | STRSTR );";
        }
    }
 

}