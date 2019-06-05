package org.pwstudio.nlp.seg.processor;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.nr.TranslatedPersonDictionary;
import org.pwstudio.nlp.seg.Config;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.seg.processor.PostProcessor;
import org.pwstudio.nlp.utility.TextUtility;

/**
 * 特殊类型词语的后处理操作
 * @author TylunasLi
 *
 */
public class SpecialWordProcessor {
	
	/**
	 * 识别特殊编号
	 * 支持两种格式 1）英文字母串+数字串 如“iPhone6” 2）英文字母串+"-"+数字串, 如 “F-35”
	 */
	public static PostProcessor codeMerger = new PostProcessor() {
		
		@Override
		public boolean isActive(Config config) {
			return config.mergeCode && !config.mergeLetterAndNumbers;
		}
		/**
		 * 识别特殊编号
		 * @param list
		 */
		@Override
		public void processVertexes(Vertex[] vertexList, WordNet fullWordNet) {
			final short Out = 0;
			final short Letterpart = 1;
			final short Dash = 2;
			final short Number = 3;
			short state = Out;
			int startWord = 0, endWord = 0;
			for (int i = 0; i < vertexList.length; ++i) {
				Vertex vertex = vertexList[i];
				if (vertex == null)
					continue;
				switch (state) {
				case Out : {
					if (TextUtility.isAllLetter(vertex.realWord)) {
						startWord = i;
						state = Letterpart;
					} else if ((vertex.realWord.endsWith("-") || vertex.realWord.endsWith("_"))
							&& vertex.realWord.length() > 1) {
						String withoutlast = vertex.realWord.substring(0, vertex.realWord.length() - 1);
						if (TextUtility.isAllLetter(withoutlast)) {
							startWord = i;
							state = Dash;
						}
					}
					break;
				}
				case Letterpart : {
					if (vertex.realWord.equals("-")) {
						state = Dash;
					} else if (vertex.realWord.contentEquals("——")) {
						state = Dash;
					} else if (isPureNum(vertex.realWord)) {
						endWord = i;
						state = Number;
					} else {
						state = Out;
					}
					break;
				}
				case Dash : {
					if (isPureNum(vertex.realWord)) {
						endWord = i;
						state = Number;
					} else {
						state = Out;
					}
					break;
				}
				case Number : {
					mergeAndReplace(vertexList, fullWordNet, startWord, endWord, 
							vertexList[startWord].word, vertexList[startWord].wordID, vertexList[startWord].attribute);
					state = Out;
					break;
				}
				}
			}
		}
	};
	

	/**
	 * 识别附带空格的编号, 并去除空格
	 * “E 1230” -> “E1230”
	 */
	public static PostProcessor spacedCodeMerger = new PostProcessor() {
		
		@Override
		public boolean isActive(Config config) {
			return config.spaceMode == 2;
		}
		/**
		 * 识别特殊编号
		 * @param list
		 */
		@Override
		public void processVertexes(Vertex[] vertexList, WordNet fullWordNet) {
			final short Out = 0; 
			final short Letterpart = 1; 
			final short Space = 2; 
			final short Number = 3;
			short state = Out;
			int startWord = 0, endWord = 0;
			int spaceWord = 0;
			for (int i=0; i<vertexList.length; i++) {
				Vertex vertex = vertexList[i];
				if (vertex == null)
					continue;
				switch (state) {
					case Out: {
						if (TextUtility.isAllLetter(vertex.realWord)) {
							startWord = i;
							state = Letterpart;
						} 
						break;
					}
					case Letterpart: {
						String str = vertex.realWord.trim();
						if (str.isEmpty()) {
							spaceWord = i;
							state = Space;
						} else {
							state = Out;
						}
						break;
					}
					case Space: {
						if (isPureNum(vertex.realWord.substring(0,1))) {
							endWord = i;
							state = Number;
						} else if (TextUtility.isAllLetter(vertex.realWord)) {
							startWord = i;
							state = Letterpart;
						} else {
							state = Out;
						}
						break;
					}
					case Number: {
						//调换空格的顺序并保持长度一致
						StringBuilder newValue = new StringBuilder();
						newValue.append(vertexList[startWord].realWord).append(
								vertexList[endWord].realWord).append(vertexList[spaceWord].realWord);
						mergeAndReplace(vertexList, fullWordNet, startWord, endWord, 
								vertexList[startWord].word, vertexList[startWord].wordID, vertexList[startWord].attribute);
						vertexList[startWord].realWord = newValue.toString();
						state = Out;
						break;
					}
				}//switch
			}//for
		}//processVertexes
	};
	
	/**
	 * 连续的英文字母。数字。连字符合并为一个块
	 */
	public static PostProcessor engNumMerger = new PostProcessor() {
		
		@Override
		public boolean isActive(Config config) {
			return config.mergeLetterAndNumbers;
		}

		@Override
		public void processVertexes(Vertex[] vertexList, WordNet fullWordNet) {
			short state = 0;				//0-串外部 1-串内部
			int startWord = 0, endWord = 0;
			Vertex baseVertex = null;		//该vertex提供属性
			for (int i=0; i<vertexList.length; i++) {
				Vertex vertex = vertexList[i];
				if (vertex == null)
					continue;
				//获取Attribute
				if (baseVertex == null && (TextUtility.isAllLetter(vertex.realWord) || vertex.hasNature(Nature.x))) {
					baseVertex = vertex;
				}
				if (isMergeableASCII(vertex.realWord)) {
					if (state == 0) {
						endWord = startWord = i;
						state = 1;
					}
					else if (state == 1)
						endWord = i;
				}else{
					if (state == 1 && endWord - startWord > 0) {
						if (baseVertex == null)
							baseVertex = vertexList[endWord];
						mergeAndReplace(vertexList, fullWordNet, startWord, endWord, 
								baseVertex.word, baseVertex.wordID, baseVertex.attribute);
					}
					state=0;
				}
			}
		}
		
	};

	/**
	 * 将点号分隔的音译人名合并为一个词
	 */
	public static PostProcessor translateNameMerger = new PostProcessor() {
		
		@Override
		public boolean isActive(Config config) {
			return config.ner && !config.translatedNameRecognize;
		}

		@Override
		public void processVertexes(Vertex[] vertexList, WordNet fullWordNet) {
			final short Out = 0; 			//译名
			final short Letterpart = 1; 	//音译词
			final short Dash = 2; 			//分隔符
			final short Finish = 3;			//结束
			short state = Out;
			int startWord = 0, endWord = 0;
			for (int i=0; i<vertexList.length; i++) {
				Vertex vertex = vertexList[i];
				if (vertex == null)
					continue;
				switch (state) {
				case Out: {
					if (vertex.hasNature(Nature.nrf) || vertex.hasNature(Nature.nsf)
							|| TranslatedPersonDictionary.containsKey(vertex.realWord)) {
						startWord = i;
						state = Letterpart;
					}
					break;
				}
				case Letterpart: {
					if (vertex.realWord.equals("·"))
						state = Dash;
					else if (vertex.realWord.equals("-"))
						state = Dash;
					else if (vertex.realWord.equals("—"))
						state = Dash;
					else if (vertex.hasNature(Nature.nrf) || !vertex.hasNature(Nature.nsf)
								|| TranslatedPersonDictionary.containsKey(vertex.realWord)) {
						state = Letterpart;
					} else {
						endWord = i - 1;
						state = 3;
					}
					break;
				}
				case Dash: {
					if (vertex.hasNature(Nature.nrf) || vertex.hasNature(Nature.nsf)) {
						state = Letterpart;
					} else {
						endWord = i;
						state = Finish;
					}
					break;
				}
				case Finish: {
					if (endWord - startWord >= 2) {
						mergeAndReplace(vertexList, fullWordNet, startWord, endWord,
								vertexList[startWord].word, vertexList[startWord].wordID,
								vertexList[startWord].attribute);
					}
					state = Out;
					break;
				}
				}
			}

		}
	};

}
