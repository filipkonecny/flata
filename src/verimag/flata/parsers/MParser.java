package verimag.flata.parsers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.antlr.runtime.RecognitionException;

import verimag.flata.presburger.ModuloRel;
import verimag.flata.presburger.VariablePool;

public class MParser {
	
public static Collection<ModuloRel> parseRel(File aInputFilePath, File aInputFilePathWithoutExt, VariablePool pool) {
		
		try {
			org.antlr.runtime.ANTLRFileStream input = new org.antlr.runtime.ANTLRFileStream(aInputFilePath.getCanonicalPath());
			verimag.flata.parsers.CalcLexer lex = new verimag.flata.parsers.CalcLexer(input);
			
			try {
				org.antlr.runtime.CommonTokenStream tokens = new org.antlr.runtime.CommonTokenStream(lex);

				CalcParser parser = new CalcParser(tokens);
				CalcParser.constr_input_return r = parser.constr_input();

				org.antlr.runtime.tree.CommonTree t = (org.antlr.runtime.tree.CommonTree)(r.getTree());						
				verimag.flata.parsers.CalcT treeparser = new verimag.flata.parsers.CalcT(new org.antlr.runtime.tree.CommonTreeNodeStream(t));
				
				treeparser.inputFilePath = aInputFilePathWithoutExt;
				return treeparser.constrInput(pool);
			} catch (RecognitionException e)  {
				lex.reportError(e);
				System.err.println(e.getMessage());
				System.err.println("Parsing ended.");
				System.exit(-1);			
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		return null;		
	}

	
	public static Collection<ModuloRel> parseRels(File aInputFilePath, File aInputFilePathWithoutExt, VariablePool pool) {
		
		try {
			org.antlr.runtime.ANTLRFileStream input = new org.antlr.runtime.ANTLRFileStream(aInputFilePath.getCanonicalPath());
			verimag.flata.parsers.CalcLexer lex = new verimag.flata.parsers.CalcLexer(input);
			
			try {
				org.antlr.runtime.CommonTokenStream tokens = new org.antlr.runtime.CommonTokenStream(lex);

				CalcParser parser = new CalcParser(tokens);
				CalcParser.constrs_input_return r = parser.constrs_input();

				org.antlr.runtime.tree.CommonTree t = (org.antlr.runtime.tree.CommonTree)(r.getTree());						
				verimag.flata.parsers.CalcT treeparser = new verimag.flata.parsers.CalcT(new org.antlr.runtime.tree.CommonTreeNodeStream(t));
				
				treeparser.inputFilePath = aInputFilePathWithoutExt;
				return treeparser.constrsInput(pool);
			} catch (RecognitionException e)  {
				lex.reportError(e);
				System.err.println(e.getMessage());
				System.err.println("Parsing ended.");
				System.exit(-1);			
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		return null;		
	}
	
	public static void calc(File aInputFilePath, File aInputFilePathWithoutExt, VariablePool pool) {
		
		try {
			org.antlr.runtime.ANTLRFileStream input = new org.antlr.runtime.ANTLRFileStream(aInputFilePath.getCanonicalPath());
			verimag.flata.parsers.CalcLexer lex = new verimag.flata.parsers.CalcLexer(input);
			
			try {
				org.antlr.runtime.CommonTokenStream tokens = new org.antlr.runtime.CommonTokenStream(lex);

				CalcParser parser = new CalcParser(tokens);
				CalcParser.calc_return r = parser.calc();

				org.antlr.runtime.tree.CommonTree t = (org.antlr.runtime.tree.CommonTree)(r.getTree());
				
				verimag.flata.parsers.CalcT treeparser = new verimag.flata.parsers.CalcT(new org.antlr.runtime.tree.CommonTreeNodeStream(t));
				
				treeparser.inputFilePath = aInputFilePathWithoutExt;
				treeparser.calc(pool);
			} catch (RecognitionException e)  {
				lex.reportError(e);
				System.err.println(e.getMessage());
				System.err.println("Parsing ended.");
				System.exit(-1);			
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}		
	}
}
