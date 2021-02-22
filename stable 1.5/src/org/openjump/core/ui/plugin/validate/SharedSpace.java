package org.openjump.core.ui.plugin.validate;

import org.openjump.core.ui.plugin.validate.pojo.MatchList;

public class SharedSpace {

	private static SharedSpace singleSharedSpace;
	
	private MatchList matchList = null;
	
	
	private SharedSpace() {}
	
	/**
	 * The instance getter of Single Pattern
	 * @return the single SharedSpaceInstance
	 */
	public static synchronized SharedSpace getInstance() {
        if (singleSharedSpace == null) {
        	singleSharedSpace = new SharedSpace ();
        }
		return singleSharedSpace;
	}
	
	
	public void storeMatchList(MatchList matchList) {
		this.matchList = matchList;
		System.out.println("--SharedSpace-- match list has been stored");
	}
	
	public MatchList getMatchList() {
		return this.matchList;
	}
}
