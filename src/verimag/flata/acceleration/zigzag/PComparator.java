package verimag.flata.acceleration.zigzag;

import java.util.*;

public class PComparator implements Comparator<Object> {
	public int compare(Object p1, Object p2) { 
		return ((Point) p1).getLength() - ((Point) p2).getLength(); 
	}
}
