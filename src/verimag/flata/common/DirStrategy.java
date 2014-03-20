/**
 * 
 */
package verimag.flata.common;

public enum DirStrategy {
	FW, BW;
	
	public static DirStrategy getDefault() {
		return get(STR_DEFAULT);
	}
	public static DirStrategy get(String str) {
		if (str.equals(STR_FW))
			return FW;
		else //if (str.equals(STR_FW))
			return BW;
	}
	
	public static String STR_FW = "fw";
	public static String STR_BW = "bw";
	
	public static String STR_DEFAULT = STR_FW;
}