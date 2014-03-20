/**
 * 
 */
package verimag.flata.common;

public enum SCCStrategy {
	GLOBAL, DFS, BFS, RANDOM;
	
	public static SCCStrategy getDefault() {
		return get(STR_DEFAULT);
	}
	public static SCCStrategy get(String str) {
		if (str.equals(STR_DFS))
			return DFS;
		else if (str.equals(STR_BFS))
			return BFS;
		else if (str.equals(STR_GLOBAL))
			return GLOBAL;
		else //if (str.equals(STR_RANDOM))
			return RANDOM;			
	}
	
	public static String STR_DFS = "dfs";
	public static String STR_BFS = "bfs";
	public static String STR_GLOBAL = "global";
	public static String STR_RANDOM = "random";
	
	public static String STR_DEFAULT = STR_GLOBAL;
}