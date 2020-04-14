package com.nlp.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;


/**
 * We are using stanford-corenlp in this module. which is licensed under GPL. so before using Please have a look on 
 * https://stanfordnlp.github.io/CoreNLP/index.html#license.
 *  for demo of coreNlp working please have a look on : http://corenlp.run/
 *  
 *  Some Useful Links:
 *  
 *  Documentation: https://stanfordnlp.github.io/CoreNLP/corenlp-server.html
 *  Demo:   http://corenlp.run/https://interviewbubble.com/getting-started-with-stanford-corenlp/
 *  Tutorial: https://stanfordnlp.github.io/CoreNLP/tutorials.html
 */

/**
 * @author Sachin
 *
 */

 
 
public class NlpService {

	public static void main(String[] args) {
		List<String> stringList = getSampleArrayList();
		Map<String, Set<String>> content = findSentiment(stringList);
		Set<String> filteredSet = new HashSet<String>();
		content.entrySet().stream().forEach(entry -> filteredSet.addAll(filterSetOfMessages(entry.getValue())));
		System.out.println("----------------" + filteredSet);
	}

	private static List<String> getSampleArrayList() {
		List<String> stringList = new ArrayList<String>();
		stringList.add("its is grateful beautiful friends are fulfilling an incredibly joyful accomplishment.");
		stringList.add("What an truly terrible idea");
		stringList.add("How are you");
		stringList.add("VPN is slow");
		stringList.add("VPN is down");
		stringList.add("VPN is worst");
		stringList.add("I am Software Engineer");
		stringList.add("I am Software Developer");
		stringList.add("I am Engineer");
		
		stringList.add("i am facing vpn slowness");
		stringList.add("i faced issue where vpn is performing slow");
		stringList.add("i am facing vpn slowness");
		stringList.add("i am getting issues related to vpn slowness");
		stringList.add("there are VPN slowness issues");
		stringList.add("How are you");
		stringList.add("how are you doing");
		stringList.add("how do you do");
		stringList.add("You are bad guy");
		stringList.add("You are not good guy");
		return stringList;
	}

	
	// in this method we are grouping list's content into sentiments form i.e
	// whether sentence is positive, negative o neutral.
	// we did it just to reduce comparison list size and get unique context based
	// messages
	public static Map<String, Set<String>> findSentiment(List<String> line) {
		Map<String, Set<String>> sentimentMap = new HashMap<>();
		Properties pipelineProps = new Properties();
		Properties tokenizerProps = new Properties();
		pipelineProps.setProperty("annotators", "parse, sentiment");
		pipelineProps.setProperty("parse.binaryTrees", "true");
		pipelineProps.setProperty("enforceRequirements", "false");
		tokenizerProps.setProperty("annotators", "tokenize ssplit");
		StanfordCoreNLP tokenizer = new StanfordCoreNLP(tokenizerProps);
		StanfordCoreNLP pipeline = new StanfordCoreNLP(pipelineProps);
		Annotation annotation = tokenizer.process(String.join(". ", line));
		pipeline.annotate(annotation);
		// normal output
		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			if (Objects.nonNull(sentence) && !String.valueOf(sentence).trim().equalsIgnoreCase(".")) {
				String output = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
				if (output.toLowerCase().contains("negative")) {
					Set<String> negativeComment = Objects.nonNull(sentimentMap.get("Negative"))
							? sentimentMap.get("Negative")
							: new HashSet<String>();
					negativeComment.add(String.valueOf(sentence));
					sentimentMap.put("Negative", negativeComment);
				} else if (output.toLowerCase().contains("neutral")) {
					Set<String> negativeComment = Objects.nonNull(sentimentMap.get("Neutral"))
							? sentimentMap.get("Neutral")
							: new HashSet<String>();
					negativeComment.add(String.valueOf(sentence));
					sentimentMap.put("Neutral", negativeComment);
				} else {
					Set<String> negativeComment = Objects.nonNull(sentimentMap.get("Positive"))
							? sentimentMap.get("Positive")
							: new HashSet<String>();
					negativeComment.add(String.valueOf(sentence));
					sentimentMap.put("Positive", negativeComment);
				}
			}
		}
		System.out.println("-------sentimentMap --------- "+sentimentMap);
		return sentimentMap;
	}

	// here we are using POS annotation to compare unique context based messages
	public static Set<String> filterSetOfMessages(Set<String> similarSentimentList) {
		Set<String> existingSet = new HashSet<String>();
		existingSet.addAll(similarSentimentList);
		Set<String> filteredSet = new HashSet<String>();
		Set<String> removedNoun = new HashSet<String>();
		MaxentTagger maxentTagger = new MaxentTagger("english-left3words-distsim.tagger");
		for (String similarSentiment : similarSentimentList) {
			Set<String> addedNoun = new HashSet<String>();
			String tag = maxentTagger.tagString(similarSentiment);
			String[] eachTag = tag.split("\\s+");
			for (int i = 0; i < eachTag.length; i++) {
				String value = eachTag[i].split("_")[0];
				String type = eachTag[i].split("_")[1];
				if ((type.equals("NNP") || type.equals("NN") || type.equals("PRP")) && !addedNoun.contains(value)) {
					addedNoun.add(value);
				}
			}
			if(CollectionUtils.isEmpty(addedNoun)) {
				filteredSet.add(similarSentiment);
			}else if (!validateIfNounExistInList(filteredSet, addedNoun)) {
				removedNoun.addAll(addedNoun);
				filteredSet.add(similarSentiment);
			}

		}
		return filteredSet;

	}

	
	// below method is used to filter if message is similar to context whichg we already saves in Map as a key or not
	// NOTE: below for loop is not optimal approach but Time Constraint lead to this and it need to be removed in future. 
	public static Boolean validateIfNounExistInList(Set<String> filteredSet, Set<String> nouns) {
		Boolean removeSentance = Boolean.FALSE;
		for(String nounValue:nouns) {
			for(String similarSentimentContent:filteredSet) {
				if(similarSentimentContent.contains(nounValue)) {
					removeSentance = Boolean.TRUE;
				}
			}
		}
		return removeSentance;
	}


}