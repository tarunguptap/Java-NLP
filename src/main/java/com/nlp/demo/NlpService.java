package com.nlp.demo;

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

	public Map<String, Set<String>> getFilteredMap(List<String> messageList) {
		Map<String, Set<String>> content = findSentiment(messageList);
		Map<String, Set<String>> filteredMap = new HashMap<>();
		content.entrySet().stream().forEach(entry -> filteredMap.putAll(filterSetOfMessages(entry.getValue())));
		return filteredMap;
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
		System.out.println("-------sentimentMap --------- " + sentimentMap);
		return sentimentMap;
	}

	// here we are using POS annotation to compare unique context based messages
	public static Map<String, Set<String>> filterSetOfMessages(Set<String> similarSentimentList) {
		Map<String, Set<String>> finalMap = new HashMap<String, Set<String>>();
		Set<String> existingSet = new HashSet<String>();
		existingSet.addAll(similarSentimentList);
		Set<String> filteredSet = new HashSet<String>();
		MaxentTagger maxentTagger = new MaxentTagger("english-left3words-distsim.tagger");
		for (String similarSentiment : similarSentimentList) {
			Set<String> addedNoun = new HashSet<String>();
			String tag = maxentTagger.tagString(similarSentiment);
			String[] eachTag = tag.split("\\s+");
			for (int i = 0; i < eachTag.length; i++) {
				String value = eachTag[i].split("_")[0];
				String type = eachTag[i].split("_")[1];
				// here NNP,NN and PRP are grammatical representatiuoins where NN represent
				// Noun, NNP represent noun with Preposition etc.
				if ((type.equals("NNP") || type.equals("NN") || type.equals("PRP")) && !addedNoun.contains(value)) {
					addedNoun.add(value);
				}
			}
			Boolean isContentExist = validateIfNounExistInList(filteredSet, addedNoun, finalMap, similarSentiment);
			if (CollectionUtils.isEmpty(addedNoun) || !isContentExist) {
				filteredSet.add(similarSentiment);
				finalMap.put(similarSentiment, new HashSet<String>(1));
			}
		}
		return finalMap;

	}

	// below method is used to filter if message is similar to context whichg we
	// already saves in Map as a key or not
	// NOTE: below for loop is not optimal approach but Time Constraint lead to this
	// and it need to be removed in future.
	public static Boolean validateIfNounExistInList(Set<String> filteredSet, Set<String> nouns,
			Map<String, Set<String>> finalMap, String currentSentiment) {
		Boolean removeSentance = Boolean.FALSE;
		for (String nounValue : nouns) {
			for (String similarSentimentContent : filteredSet) {
				if (similarSentimentContent.contains(nounValue)) {
					Set<String> updatedValues = Objects.nonNull(finalMap.get(similarSentimentContent))
							? finalMap.get(similarSentimentContent)
							: new HashSet<String>();
					updatedValues.add(currentSentiment);
					finalMap.put(similarSentimentContent, updatedValues);
					removeSentance = Boolean.TRUE;
				}
			}
		}
		return removeSentance;
	}

}