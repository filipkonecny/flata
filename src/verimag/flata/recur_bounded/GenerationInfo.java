package verimag.flata.recur_bounded;

import java.util.*;

import nts.interf.*;
import nts.parser.*;

public class GenerationInfo {

	private INTS nts;
	private List<ISubsystem> latest_subsystems;
	//private Subsystem latest_main;
	
	public INTS getNts() { return nts; }
	public List<ISubsystem> latestSubsystems() { return latest_subsystems; }
	//public Subsystem latestMain() { return latest_main; }
	
	void setLatestSubsystems(List<ISubsystem> aL) { latest_subsystems = aL; }
	//void setLatestMain(Subsystem aS) { latest_main = aS; }
	
	public GenerationInfo(NTS aNts) {
		nts = aNts;
	}
	
}
