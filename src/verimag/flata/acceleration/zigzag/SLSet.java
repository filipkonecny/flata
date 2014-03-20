package verimag.flata.acceleration.zigzag;

import java.util.Vector;

public interface SLSet {
	public SLSet copy();
	public void clear();
	public Vector<LinSet> getLinearSets();
	
	public void translate(Point x);
	public void union(SLSet s);
	
	public void addPoint(Point x);
	public void addGenerator(Point x);
	
	public SLSet sum(SLSet s);
	public void minimize();
	public void tighten();
	public void close(SLSet s1, SLSet s2);
	public int maxPosSteps();
	
	public boolean empty();
	public boolean equals(SLSet s);
	public boolean includes(SLSet s);	
}
