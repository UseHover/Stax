package com.hover.stax.bounty;

import java.util.List;

class SectionedBountyAction {
	public String header;
	public List<BountyAction> bountyActionList;

	public SectionedBountyAction(String header, List<BountyAction> bountyActionList) {
		this.header = header;
		this.bountyActionList = bountyActionList;
	}

	public SectionedBountyAction() {
	}
}
