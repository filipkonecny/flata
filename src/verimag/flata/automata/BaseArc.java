package verimag.flata.automata;

/**
 * Skeletal abstract class of an arc of a graph. It is an element of topology introduced by 
 * {@link BaseGraph}.
 *  
 */
public abstract class BaseArc {
	
	////////////////////////////////////////////////////////////////////////////////
	//////////////////////////  abstract methods  //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public abstract BaseNode from();
	public abstract BaseNode to();
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	// weight of the edge
	protected int weight;
	public int weight() { return weight; }
	
	/**
	 * creates an arc with implicit weight 1
	 */
	protected BaseArc() {
		this(1);
	}
	/**
	 * creates an arc with the specified weight
	 */
	protected BaseArc(int aWeight) {
		weight = aWeight;
	}
	/**
	 * copy constructor
	 */
	protected BaseArc(BaseArc aOther) {
		weight = aOther.weight;
	}
	
	public boolean isLoop() {
		return to() == from();
	}
}
