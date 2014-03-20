package verimag.flata.automata.ca;

import java.lang.String;
import java.util.*;

import verimag.flata.automata.*;

public class CAState extends BaseNode implements java.lang.Comparable<CAState> {
		
	private static int id_gen = 1;
	private int id = id_gen++;
	public int id() { return id; }
	
	private String name;
	private String oldName;
	private Map<Integer, CATransition> incoming;
	private Map<Integer, CATransition> outgoing;
	
	// pointer to the original state (transformations can produce copies of states) 
	private CAState origin;
	
	
	// state type (since elimination of a state s with loops splits s to s-in and s-out): original, split-in, split-out
	public static enum Type {
		ORIG, SPLIT_IN, SPLIT_OUT;
	}
	private CAState split_in, split_out, split_orig;
	private Type type = null;
	public void type(Type aType) { type = aType; }
	

	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	public String name() { return name; }
	public void name(String aName) { name = aName; }
	
	public String oldName() { return oldName; }
	public void oldName(String aName) { oldName = aName; }
	
	public Collection<CATransition> incoming() { return new LinkedList<CATransition>(incoming.values()); }
	public Collection<CATransition> outgoing() { return new LinkedList<CATransition>(outgoing.values()); }
	
	public CAState origin() { return origin; }
	
	
	////////////////////////////////////////////////////////////////////////////////
	//////////////////////////  abstract methods  //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public static boolean add_internal(BaseArc aTransition, Map<Integer, CATransition> aMap) {
		CATransition t = (CATransition)aTransition;
		boolean b = aMap.containsKey(t.id());
		aMap.put(t.id(), t);
		return b;
	}
	public static boolean remove_internal(BaseArc aTransition, Map<Integer, CATransition> aMap) {
		CATransition t = (CATransition)aTransition;
		return aMap.remove(t.id()) != null;
//		boolean b = aMap.containsKey(t.id());
//		aMap.remove(t.id());
//		return b;
	}
	public boolean addIncoming_internal(BaseArc aTransition) {
		return add_internal(aTransition, incoming);
	}
	public boolean removeIncoming_internal(BaseArc aTransition) {
		return remove_internal(aTransition, incoming);
	}
	public boolean addOutgoing_internal(BaseArc aTransition) {
		return add_internal(aTransition, outgoing);
	}
	public boolean removeOutgoing_internal(BaseArc aTransition) {
		return remove_internal(aTransition, outgoing);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public CAState(String aName) {
		this(aName,Type.ORIG);
	}
	public CAState(String aName, Type aType) {
		super();
		name = aName;
		incoming = new HashMap<Integer,CATransition>();
		outgoing = new HashMap<Integer,CATransition>();
		
		type = aType;
		
		origin = this;
	}
	
	public static void bindSplit(CAState s, CAState split_in, CAState split_out) {
		split_in.type = Type.SPLIT_IN;
		split_out.type = Type.SPLIT_OUT;
		
		split_in.split_orig = s;
		split_out.split_orig = s;
		
		s.split_in = split_in;
		s.split_out = split_out;
	}

	
	public boolean equals(Object aObj) {
		return
			aObj instanceof CAState && 
			id == ((CAState)aObj).id;
	}
	public int hashCode() {
		return id;
	}
	public int compareTo(CAState aAnother) {
		//return this.name.compareTo(aAnother.name());
		return this.id - aAnother.id;
	}
	
	public String toString() {
		return name;
	}

	// predecessor states
	private void pred(Set<CAState> set) {
		for (CATransition out : incoming.values())
			set.add(out.from());
	}
	public Set<CAState> pred() {
		Set<CAState> set = new HashSet<CAState>();
		pred(set);
		return set;
	}
	// successor states
	private void succ(Set<CAState> set) {
		for (CATransition out : outgoing.values())
			set.add(out.to());
	}
	public Set<CAState> succ() {
		Set<CAState> set = new HashSet<CAState>();
		succ(set);
		return set;
	}
	// incident states (predecessors and successors)
	public Set<CAState> incidentStates() {
		Set<CAState> set = new HashSet<CAState>();
		pred(set);
		succ(set);
		return set;
	}
	
	public static String[] sa(Collection<CAState> sc) {
		String[] ret = new String[sc.size()];
		int i=0;
		for (CAState s : sc)
			ret[i++] = s.name;
		return ret;
	}

}
