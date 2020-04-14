package com.nlp.demo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestMessageFilter {

	public static void main(String[] args) {
		Map<String, Set<String>> map = NlpService.filterSetOfMessages(getSampleArrayList());
		map.entrySet().stream().forEach(mapContent -> {
			System.out.println(" map content is::::: " + mapContent);
		});
	}

	private static Set<String> getSampleArrayList() {
		Set<String> stringSet = new HashSet<String>();
		stringSet.add("its is grateful beautiful friends are fulfilling an incredibly joyful accomplishment.");
		stringSet.add("What an truly terrible idea");
		stringSet.add("How are you");
		stringSet.add("VPN is slow");
		stringSet.add("VPN is down");
		stringSet.add("VPN is worst");
		stringSet.add("I am Software Engineer");
		stringSet.add("I am Software Developer");
		stringSet.add("I am Engineer");
		stringSet.add("i am facing VPN slowness");
		stringSet.add("i faced issue where VPN is performing slow");
		stringSet.add("i am facing VPN slowness");
		stringSet.add("i am getting issues related to VPN slowness");
		stringSet.add("there are VPN slowness issues");
		stringSet.add("How are you");
		stringSet.add("how are you doing");
		stringSet.add("how do you do");
		stringSet.add("You are bad guy");
		stringSet.add("You are not good guy");
		return stringSet;
	}

}
