/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/1 23:04</create-date>
 *
 * <copyright file="SimplifiedChineseDictionary.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.dictionary.ts;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import org.pwstudio.nlp.utility.Predefine;

import static org.pwstudio.nlp.utility.Predefine.logger;

/**
 * 简体=繁体词典
 * @author hankcs
 */
public class SimplifiedChineseDictionary extends BaseChineseDictionary
{
    /**
     * 简体=繁体
     */
    static AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<String>();
    
    static
    {
        long start = System.currentTimeMillis();
        if (!load(PwNLP.Config.tcDictionaryRoot + "s2t.txt", trie, false))
        {
            throw new IllegalArgumentException("简繁词典" + PwNLP.Config.tcDictionaryRoot + "s2t.txt" + Predefine.BIN_EXT + "加载失败");
        }

        logger.info("简繁词典" + PwNLP.Config.tcDictionaryRoot + "s2t.txt" + Predefine.BIN_EXT + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }

    public static String convertToTraditionalChinese(String simplifiedChineseString)
    {
        return segLongest(simplifiedChineseString.toCharArray(), trie);
    }

    public static String convertToTraditionalChinese(char[] simplifiedChinese)
    {
        return segLongest(simplifiedChinese, trie);
    }

    public static String getTraditionalChinese(String simplifiedChinese)
    {
        return trie.get(simplifiedChinese);
    }
}
