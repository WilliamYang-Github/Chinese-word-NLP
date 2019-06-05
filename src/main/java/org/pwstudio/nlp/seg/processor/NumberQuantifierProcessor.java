package org.pwstudio.nlp.seg.processor;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.seg.Config;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.utility.Predefine;
import org.pwstudio.nlp.utility.TextUtility;

/**
 * 数词、量词的后处理操作
 * @author TylunasLi
 *
 */
public class NumberQuantifierProcessor {
	
	/**
	 * 合并中文数词
	 * 由于“一”“万”可能做副词，因此增加这个过程进行合并
	 */
	public static PostProcessor numberIdentifier = new PostProcessor() {
		
		@Override
		public void processVertexes(Vertex[] vertexList, WordNet fullWordNet) {
			short isNumber=0;
			int startWord = 0, endWord = 0;
			Vertex numberAttribute = null;
			for (int i=0; i<vertexList.length; i++) {
				Vertex vertex = vertexList[i];
				if (vertex == null)
					continue;
				if (vertex.hasNature(Nature.m) && vertex.attribute.nature.length == 1)
					numberAttribute = vertex;
				if (TextUtility.isAllChineseNum(vertex.realWord)) {
					if (isNumber == 0) {
						isNumber = 1;
						startWord = i;
					}
					endWord = i;
				}else{
					if (isNumber == 1 && endWord - startWord > 0) {
						if (numberAttribute == null)
							numberAttribute=vertexList[startWord];
						mergeAndReplace(vertexList, fullWordNet, startWord, endWord, numberAttribute.word, 
								numberAttribute.wordID, numberAttribute.attribute);
					}
					isNumber = 0;
				}
			}
		}
	};

	/**
	 * 合并单位词，形成复合单位词
	 */
	public static PostProcessor quantifierMerger = new PostProcessor() {
		
		@Override
		public void processVertexes(Vertex[] vertexList, WordNet fullWordNet) {
			final short Out = 0; 
			final short Quantifier = 1; 
			final short Slash = 2; 
			final short End = 3;
			short state = Out;
			int startWord = 0, endWord = 0;
			for (int i=0; i<vertexList.length; i++) {
				Vertex vertex = vertexList[i];
				if (vertex == null)
					continue;
				switch (state) {
				case Out: {
					if (vertex.hasNature(Nature.q) || vertex.hasNature(Nature.qv) || vertex.hasNature(Nature.qt)) {
						startWord = i;
						state = Quantifier;
					}
					break;
				}
				case Quantifier: {
					if (vertex.realWord.equals(vertexList[startWord].realWord)) {
						state = End;
					}
					if (vertex.realWord.equals("/") || vertex.realWord.equals("／"))
						state = Slash;
					else if (vertex.realWord.equals("每")) {
						state = Slash;
					} else {
						state = Out;
					}
					break;
				}
				case Slash: {
					if (vertex.hasNature(Nature.q) || vertex.hasNature(Nature.qv) || vertex.hasNature(Nature.qt)) {
						endWord = i;
						state = End;
					} else {
						state = Out;
					}
					break;
				}
				case End: {
					mergeAndReplace(vertexList, fullWordNet, startWord, endWord, vertexList[startWord].word, 
							vertexList[startWord].wordID, vertexList[startWord].attribute);
					state = Out;
					break;
				}
				}
			}
		}
	};
	
	
	/**
	 * 合并数词和量词
	 */
	public static PostProcessor numQuantifierMerger = new PostProcessor() {
		
		public boolean isActive(Config config) {
			return config.numberQuantifierRecognize;
		}

		@Override
		public void processVertexes(Vertex[] vertexList, WordNet fullWordNet) {
			final byte Initial = 0;
			final byte Number = 1;
			final byte Quantifier = 2;
			byte state = 0;
			int startWord = 0;
			int endWord = 0;

			for (int i = vertexList.length - 2; i >= 0; --i) {
				Vertex vertex = vertexList[i];
				if (vertex == null)
					continue;
				switch (state) {
				case Initial : {
					if (vertex.hasNature(Nature.q) || vertex.hasNature(Nature.qv) || vertex.hasNature(Nature.qt)) {
						endWord = i;
						state = Quantifier;
					}
					break;
				}
				case Quantifier : {
					if (vertex.hasNature(Nature.m)) {
						startWord = i;
						state = Number;
					} else {
						state = Initial;
					}
					break;
				}
				case Number : {
					mergeAndReplace(vertexList, fullWordNet, startWord, endWord,
							Predefine.TAG_NUMBER, CoreDictionary.M_WORD_ID, 
							new CoreDictionary.Attribute(Nature.mq, 1));
					state = Initial;
					break;
				}
				}
			}
		}
	};
}
