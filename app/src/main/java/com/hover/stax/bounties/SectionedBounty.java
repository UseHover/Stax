package com.hover.stax.bounties;

import com.hover.stax.channels.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class SectionedBounty {
	public String header;
	public List<Bounty> channelBounties;

	public SectionedBounty(String header, List<Bounty> channelBounties) {
		this.header = header;
		this.channelBounties = channelBounties;
	}

	//FIXME: THIS SEARCH SHOULD NOT OCCUR IN THE UI, INSTEAD THE END OBJECT THAT BOUNTY_VIEW_MODEL SENDS.
	public static List<SectionedBounty> get(List<Channel> channels, List<Bounty> bounties) {
		List<SectionedBounty> sectionedBounties = new ArrayList<>();
		Map<Integer, List<Bounty>> channelIdAndBountyListMap = new HashMap<>();
		Map<Integer, String> channelIdAndTitleMap = new HashMap<>();

		for(Channel channel: channels) {
			channelIdAndBountyListMap.put(channel.id, new ArrayList<>());
			channelIdAndTitleMap.put(channel.id, channel.getUssdName());
		}
		for(Bounty bounty: bounties) {
			if (channelIdAndBountyListMap.containsKey(bounty.action.channel_id)) {
				Objects.requireNonNull(channelIdAndBountyListMap.get(bounty.action.channel_id)).add(bounty);
			}
		}
		for (Map.Entry<Integer, List<Bounty>> entry : channelIdAndBountyListMap.entrySet()) {
			String header = channelIdAndTitleMap.get(entry.getKey());
			sectionedBounties.add(new SectionedBounty(header, entry.getValue()));
		}
		return sectionedBounties;
	}
}
