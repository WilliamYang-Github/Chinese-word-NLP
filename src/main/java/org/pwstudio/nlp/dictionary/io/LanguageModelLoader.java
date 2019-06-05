package org.pwstudio.nlp.dictionary.io;

import static org.pwstudio.nlp.utility.Predefine.logger;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.dictionary.LanguageModel;
import org.pwstudio.nlp.dictionary.CoreDictionary.Attribute;
import org.pwstudio.nlp.dictionary.base.AttributeDictionary;
import org.pwstudio.nlp.dictionary.base.BiGramTableDictionary;
import org.pwstudio.nlp.utility.Predefine;

/**
 * 加载领域对应的全套词典
 * 未来, 根据输入参数修改方法参数，
 * @author iyunwen-李辰刚 
 *
 */
public class LanguageModelLoader {
	
	/**
	 * 加载，获得某个领域的一套词典
	 * @param domainName 对应领域的名字
	 * @return
	 */
	public static LanguageModel Load(String domainName) {
		AttributeDictionary natureDict = loadUnigramDict(PwNLP.Config.CoreDictionaryPath,
				getDomainUnigramPath(domainName));
		BiGramTableDictionary bigramDict = loadBigramDict(natureDict, PwNLP.Config.BiGramDictionaryPath,
				getDomainBigramPath(domainName));
		return LanguageModel.create(natureDict, bigramDict);
	}

	public static String getDomainUnigramPath(String domain) {
		return PwNLP.Config.root + PwNLP.Config.customRoot + File.separator + domain
				+ File.separator + PwNLP.Config.defaultUnigramName;
	}
	
	public static String getDomainBigramPath(String domain) {
		return PwNLP.Config.root + PwNLP.Config.customRoot + File.separator + domain
				+ File.separator + PwNLP.Config.defaultBigramName;
	}

	/**
	 * 加载词典。缓存优先
	 * @param path 
	 * @return
	 */
	public static AttributeDictionary loadUnigramDict(String baseDictPath, String domainDictPath) {
		String binaryPath = domainDictPath + Predefine.BIN_EXT;
		AttributeDictionary mainDict = AttributeDictionaryLoader.loadFromBinary(binaryPath);
        if (mainDict != null)
        	return mainDict;
        TreeMap<String, Attribute> map = new TreeMap<String, Attribute>();
    	int totalFrequency = AttributeDictionaryLoader.loadFromText(baseDictPath, map);
    	totalFrequency += AttributeDictionaryLoader.loadFromText(domainDictPath, map);
    	if (totalFrequency == 0)
    		return null; 
    	mainDict = AttributeDictionaryLoader.buildDictionary(map, totalFrequency);
    	map = null;
        if (mainDict != null && !AttributeDictionaryLoader.saveToBinary(mainDict,binaryPath)) {
            logger.warning("缓存词典到" + binaryPath + "失败");
        }
        return mainDict;
	}
 
	/**
	 * 加载BiGram（二元接续）词典。缓存优先
	 * @param dict
	 * @param path
	 * @return
	 */
	public static BiGramTableDictionary loadBigramDict(AttributeDictionary dict, 
			String baseDictPath, String domainDictPath) {
		String binaryPath = domainDictPath + BigramTableDictionaryLoader.BINARY_EXT;
		BiGramTableDictionary biGramDict = BigramTableDictionaryLoader.loadFromBinary(dict, binaryPath);
        if (biGramDict != null)
        	return biGramDict;
        HashMap<Integer, TreeMap<Integer, Integer>> map = 
        		new HashMap<Integer, TreeMap<Integer, Integer>>(dict.count()*2);
        int totalType = BigramTableDictionaryLoader.loadFromText(dict, baseDictPath, map);
    	totalType += BigramTableDictionaryLoader.loadFromText(dict, domainDictPath, map);
        biGramDict = BigramTableDictionaryLoader.buildDictTable(dict, map, totalType);
    	map = null;
        if (biGramDict != null && !BigramTableDictionaryLoader.saveToBinary(biGramDict,binaryPath)) {
            logger.warning("缓存二元词典到" + binaryPath + "失败");
        }
        return biGramDict;
	}

	
}
