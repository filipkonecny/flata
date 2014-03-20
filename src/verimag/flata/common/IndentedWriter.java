package verimag.flata.common;

import java.io.IOException;
import java.io.Writer;

/**
 * Class that helps to generate indented streams. The type of the stream used must
 * be a subclass of Writer class and must be specified in the constructor or by the  
 * {@link #setWriter(Writer)} method.
 * <p> 
 * Except a bunch of indented-write methods (
 * {@link #writeln(String)}, 
 * {@link #writeln(String)}, 
 * {@link #startLine(String)},
 * {@link #appendLine(String)},
 * {@link #finishLine(String)}),  
 * it provides one unindented-write method {@link #write(StringBuffer)} which simply passes 
 * the text to the specified writer.
 * <p>
 * It also provides {@link #flush()} and {@link #close()} methods which pass their tasks
 * to the specified writer.
 */
public class IndentedWriter {
	private static String indentStep_def = "   ";
	private String indentStep = indentStep_def;
	private StringBuffer indent;
	private int indentCnt;
	{
		indent = new StringBuffer();
		indentCnt = 0;
	}
	public String indent() { 
		return new String(indent); 
	}
	public int indentCnt() { return indentCnt; }
	public void indentInc() { 
		indent.append(indentStep);
		indentCnt++;
	}
	public void indentDec() {
		int inxEnd = indent.length();
		indent.replace(inxEnd-indentStep.length(), inxEnd, "");
		indentCnt--;
	}
	public String indentStep() { return new String(indentStep); }
	
	private Writer writer;
	public void setWriter(Writer aWriter) {
		writer = aWriter;
	}
	public Writer getWriter() {
		return writer;
	}
	
	public IndentedWriter() {
		this(null);
	}
	public IndentedWriter(Writer aWriter, String userIndentStep) {
		this(aWriter);
		indentStep = userIndentStep;
	}
	public IndentedWriter(Writer aWriter) {
		writer = aWriter;
	}
	public IndentedWriter(Writer aWriter, int aInitialIndent) {
		writer = aWriter;
		for (int i=0; i<aInitialIndent; ++i) {
			indentInc();
		}
	}
	
	public IndentedWriter(IndentedWriter other, Writer aWriter) {
		this.indent = new StringBuffer(other.indent);
		this.lineInProceess = other.lineInProceess;
		this.writer = aWriter;
	}
	
	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			System.err.println("Output problems with flush.");
			System.err.println(e.getMessage());
		}
	}
	public void close() throws IOException {
		writer.close();
	}
	
	public void write(String aString) {
		write(new StringBuffer(aString));
	}
	public void write(StringBuffer aString) {
		if (writer==null)
			throw new RuntimeException("indented writer: uninitialized writer");
		
		try {
			writer.append(aString);
		} catch (IOException e) {
			System.err.println("Output problems with write.");
			System.err.println(e.getMessage());
		}
	}
	
	private boolean lineInProceess = false;
	
	public void writeln() {
		writeln("");
	}
	public void writeln(String aString) {
		if (lineInProceess)
			throw new RuntimeException("IndentWriter: previous line has not been closed.");
		writeln(new StringBuffer(aString));
	}
	public void writeln(StringBuffer aSb) {
		write((new StringBuffer(indent)).append(aSb+CR.NEWLINE));
	}
	public void startLine(String aString) {
		startLine(new StringBuffer(aString));
	}
	public void startLine(StringBuffer aSb) {
		write((new StringBuffer(indent)).append(aSb));
		lineInProceess = true;
	}
	public void appendLine(String aString) {
		appendLine(new StringBuffer(aString));
	}
	public void appendLine(StringBuffer aSb) {
		write(aSb);
	}
	public void finishLine(String aString) {
		finishLine(new StringBuffer(aString));
	}
	public void finishLine(StringBuffer aSb) {
		write((new StringBuffer(aSb+CR.NEWLINE)));
		lineInProceess = false;
	}
	public void finishLine() {
		finishLine("");
	}
}
