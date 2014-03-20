/**
 * 
 */
package verimag.flata.presburger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RenameM extends Rename {
	private Map<String,String> m = new HashMap<String,String>();
	@Override
	public String getNewNameFor(String aName) {
		return m.get(aName);
	}
	@Override
	public void put(String k, String v) {
		m.put(k, v);
	}

	public RenameM() {}
	public RenameM(RenameM other) {
		this.m.putAll(other.m);
	}
	
	
	private static String inlinePref = "_";
	private static String inlineSuf = "_";
	private static String inlineRegex = inlinePref+"(.)*"+inlineSuf;
	private static Pattern inlinePattern = Pattern.compile(inlineRegex);
	public static int inlineDef = 0; // default
	//private static String inlinePrefix = "I";
	private static int getInlinePrefix(String name) {
		Matcher matcher = inlinePattern.matcher(name);
		if (matcher.find()) {
			String s = matcher.group();
			String ss = s.substring(1,s.length()-1);
			if (ss.length() != 0) {
				try {
					return Integer.parseInt(ss);
				} catch (NumberFormatException e) {
				}
			}
		}
		return inlineDef;
	}
	public static int getInlinePrefix(String[] names) {
		int max = inlineDef;
		for (String name : names) {
			max = Math.max(max, getInlinePrefix(name));
		}
		return max;
	}
	
	public static RenameM createForInline(String[] names, int n) {
		RenameM ret = new RenameM();
		
		if (names.length == 0)
			return ret;
		
		for (String name : names) {
			ret.put(name, inlinePref+n+inlineSuf+name);
		}
		
//		for (String name : names) {
//			int i = getInlinePrefix(name);
//			String base;
//			if (i == inlineDef) {
//				base = name;
//			} else {
//				String aux = inlinePref+i+inlineSuf;
//				base = name.substring(aux.length(), name.length());
//			}
//			ret.put(name, inlinePref+n+inlineSuf+base);
//		}
		
		return ret;
	}
	public void inferPrimed() {
		List<String> aux = new LinkedList<String>(m.keySet());
		for (String s : aux) {
			put(s+Variable.primeSuf, m.get(s)+Variable.primeSuf);
		}
	}
}