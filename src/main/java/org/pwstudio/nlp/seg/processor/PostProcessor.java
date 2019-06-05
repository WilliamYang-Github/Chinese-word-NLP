package org.pwstudio.nlp.seg.processor;

import java.util.HashMap;
import java.util.List;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.CoreDictionary.Attribute;
import org.pwstudio.nlp.dictionary.other.CharType;
import org.pwstudio.nlp.seg.Config;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.utility.TextUtility;

public abstract class PostProcessor {

	/**
	 * 在文本中替换，并且在
	 * @param vertexList
	 * @param fullWordNet
	 * @param startVertex	开始点
	 * @param endVertex		结束点，包含
	 * @param compiledWord	等效词语
	 * @param newWordId		新节点的ID
	 * @param newAttribute	新节点的属性
	 */
	public static void mergeAndReplace(Vertex[] vertexList, WordNet fullWordNet, 
			int startVertex, int endVertex, String compiledWord, int newWordId, Attribute newAttribute) {
		int startChar = 0;
		StringBuilder newVertexBuilder = new StringBuilder();
        if (startVertex + 1 == endVertex) {    // 小优化，如果只有一个词，那就不需要合并，直接应用新属性
        	vertexList[startVertex].attribute = newAttribute;
        }
		for (int i=0; i<startVertex; i++)
			if (vertexList[i] != null)
				startChar += vertexList[i].realWord.length();
		for (int i=startVertex; i<=endVertex; i++) {
			Vertex vertex = vertexList[i];
			if (vertex != null)
				newVertexBuilder.append(vertex.realWord);
		}
		Vertex newVertex = new Vertex(compiledWord,newVertexBuilder.toString(),newAttribute,newWordId);
		//加入词网
		fullWordNet.add(startChar, newVertex);
		//更新路径
		newVertex.from = vertexList[startVertex].from;
		newVertex.weight = vertexList[startVertex].weight;
		vertexList[startVertex]=newVertex;
		for (int j = startVertex+1; j <= endVertex; j++)
			vertexList[j] = null;
		int nextVertex = endVertex + 1;
		for (; nextVertex<vertexList.length && vertexList[nextVertex]==null; nextVertex++);
		if (nextVertex < vertexList.length)
			vertexList[nextVertex].from = newVertex;
	}
	
	/**
	 * 更新到合并列表
	 * @param vertexArray
	 * @param vertexList
	 * @return
	 */
	public static List<Vertex> updateList(Vertex[] vertexArray, List<Vertex> vertexList) {
		vertexList.clear();
		for (Vertex vertex : vertexArray) {
			if (vertex != null)
				vertexList.add(vertex);
		}
		return vertexList;
	} 
	
	public static boolean isPureNum(String str) {
		for (int i=0; i<str.length(); i++) {
			char ch = str.charAt(i);
			if ("0123456789".indexOf(ch) == -1 && "０１２３４５６７８９".indexOf(ch) == -1) {
				if ('.' == ch || '．' == ch && (i > 0 && i < str.length()-1))
					continue;
				return false;
			}
		}
		return true;
	}
	
	public static boolean isMergeableASCII(String str) {
		int fit = 0;
		for (int i=0; i<str.length(); i++) {
			char ch = str.charAt(i);
			switch (CharType.get(ch)) {
				case CharType.CT_LETTER:
				case CharType.CT_NUM: {
					fit++;
					break;
				}
				case CharType.CT_DELIMITER: {
					if (ch == '.' || ch == '-' || ch == '_' || ch == '—')
						fit++;
					break;
				}
			}
		}
		return (fit == str.length());
	}

	
	/**
	 * 判断是否激活当前的后处理器
	 * @param config
	 * @return
	 */
	public boolean isActive(Config config) {
		return true;
	}
	
	public abstract void processVertexes(Vertex[] vertexList, WordNet fullWordNet);
	
}
