package org.pwstudio.nlp.seg.processor;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.LanguageModel;
import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.dictionary.CoreDictionary.Attribute;
import org.pwstudio.nlp.seg.Config;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.utility.Predefine;
import org.pwstudio.nlp.utility.TextUtility;

/**
 * 合并有效时间词
 * @author iyunwen-李辰刚
 *
 */
public class TimeProcessor extends PostProcessor {

	protected static HashSet<String> timeMap;
	
	static {
		timeMap = new HashSet<String>();
        timeMap.add("世纪");
        timeMap.add("年代");
		timeMap.add("年");
		timeMap.add("年初");
		timeMap.add("年末");
		timeMap.add("月");
		timeMap.add("月份");
		timeMap.add("日");
		timeMap.add("号");
		timeMap.add("时");
		timeMap.add("点");
		timeMap.add("分");
		timeMap.add("秒");
	} 
	
	@Override
	public boolean isActive(Config config) {
		return config.timeRecognize;
	}
	
	
	@Override
	public void processVertexes(Vertex[] vertexList, WordNet fullWordNet) {
	    Attribute timeAttribute = CoreDictionary.get(CoreDictionary.T_WORD_ID);
		int startWord = 0, endWord = 0;
		boolean isTimeWord = false;
		String keyElement = null;
		for (int i = vertexList.length-2; i>=0; i--) {
			Vertex vertex = vertexList[i];
			if (vertex == null)
				continue;
			if (timeMap.contains(vertex.realWord)) {
				startWord = endWord = i;
				isTimeWord = true;
				keyElement = vertexList[i].realWord;
			}else if (isTimeWord && vertex.attribute.hasNature(Nature.m)) {
				if (keyElement.equals("年")){
					if (TextUtility.isYearTime(vertex.realWord)) {
						mergeAndReplace(vertexList, fullWordNet, i, endWord, 
								Predefine.TAG_TIME, CoreDictionary.T_WORD_ID, timeAttribute);
					}
					isTimeWord = false;
				}else if (vertex.realWord.length() <= 2) {
					mergeAndReplace(vertexList, fullWordNet, i, endWord, 
							Predefine.TAG_TIME, CoreDictionary.T_WORD_ID, timeAttribute);
					endWord = startWord = 0;
					isTimeWord = false;
				}
			}else{
				isTimeWord = false;
			}
		}
	}

	/**
	 * 把连续的时间词合并为一个
	 */
	public static class TimeCodeRecognizer extends PreProcessor {

		final Pattern datePattern = Pattern.compile("\\d{4}[-/][0-2]?[0-9][-/][0-3]?[0-9]");
		final Pattern timePattern = Pattern.compile("[0-2]?[0-9][:：][0-5][0-9]([:：][0-5][0-9])?");
		
		@Override
		public boolean isActive(Config config) {
			return config.timeRecognize;
		}
		
		@Override
		protected Nature getNature() {
			return Nature.t;
		}

		@Override
		public void recognizeWord(char[] sentence, WordNet wordNetAll) {
			String original = new String(sentence);
			Matcher matcher = datePattern.matcher(original);
			while (matcher.find()) {
				wordNetAll.add(matcher.start(0)+1, new Vertex(Predefine.TAG_TIME, matcher.group(0), 
				        CoreDictionary.get(CoreDictionary.T_WORD_ID), CoreDictionary.T_WORD_ID));
			}
			matcher = timePattern.matcher(original);
			while (matcher.find()) {
				wordNetAll.add(matcher.start(0)+1, new Vertex(Predefine.TAG_TIME, matcher.group(0), 
				        CoreDictionary.get(CoreDictionary.T_WORD_ID), CoreDictionary.T_WORD_ID));
			}
		}
		
	};
	
	public static TimeCodeRecognizer TimeCodeProcessor = new TimeCodeRecognizer();

	/**
	 * 把连续的时间词合并为一个
	 */
	public static PostProcessor timeMerger = new PostProcessor() {

		@Override
		public boolean isActive(Config config) {
			return config.mergeTime;
		}
		
		@Override
		public void processVertexes(Vertex[] vertexList, WordNet fullWordNet) {
			short isTime=0;
			int startWord = 0, endWord = 0;
			Vertex numberAttribute = null;
			for (int i=0; i<vertexList.length; i++) {
				Vertex vertex = vertexList[i];
				if (vertex == null)
					continue;
				if (vertex.hasNature(Nature.t) && vertex.attribute.nature.length == 1)
					numberAttribute = vertex;
				if (vertex.hasNature(Nature.t) && vertex.attribute.nature[0] == Nature.t) {
					if (isTime == 0) {
						isTime = 1;
						startWord = i;
					}
					endWord = i;
				}else{
					if (endWord - startWord > 0) {
						if (numberAttribute == null)
							numberAttribute=vertexList[startWord];
						mergeAndReplace(vertexList, fullWordNet, startWord, endWord, 
								Predefine.TAG_TIME, numberAttribute.wordID, numberAttribute.attribute);
					}
					endWord = startWord = 0;
					isTime = 0;
				}
			}
		}
		
	};
}