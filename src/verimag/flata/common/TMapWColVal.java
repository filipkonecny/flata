package verimag.flata.common;

import java.util.*;

/**
 * Generic class for the TreeMap data structure which contains a collection objects as its values.
 * Note: reference to a Class<V> object must be passed to the constructor.
 *
 * @param <E> type of elements in the collection
 * @param <K> type of keys
 * @param <V> type that is subclass of Collection<E>
 */

@SuppressWarnings("serial")
//TreeMapWithCollectionValues
public class TMapWColVal<E,K,V extends Collection<E>> extends TreeMap<K, V> {

	Class<V> valueClass;
	public TMapWColVal(Class<V> aValueClass) {
		valueClass = aValueClass;
	}
	public boolean _add(K k, E e) {
		V col = this.get(k);
		if (col==null) {
			try {
				col = valueClass.newInstance();
			} catch (Exception ex) {
				System.err.println("Cannot create '"+valueClass.getName()+"' instance.");
				System.exit(-1);
			}
			
		}
		this.put(k, col);
		return col.add(e);
	}
	public boolean _remove(K k, E e) {
		V col = this.get(k);
		if (col==null)
			return false;
		boolean b = col.remove(e);
		if (col.isEmpty())
			this.remove(k);
		return b;
	}
}
