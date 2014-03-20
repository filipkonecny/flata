package verimag.flata.common;

@SuppressWarnings("serial")
public class StopReduction extends RuntimeException {

	public static enum StopType {
		TRACE, NOTRACE, NOLONGTRACE
	}
	
	private StopType type;
//	public StopReduction() {
//	}
	public StopReduction(StopType aType) {
		type = aType;
	}
	
	public boolean typeTrace() { return type == StopType.TRACE; }
	public boolean typeNoLongTrace() { return type == StopType.NOLONGTRACE; }
	public boolean typeNoTrace() { return type == StopType.NOTRACE; }
}
