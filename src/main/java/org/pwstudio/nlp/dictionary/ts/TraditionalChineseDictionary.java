/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/1 21:04</create-date>
 *
 * <copyright file="TraditionalChineseDictionary.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.dictionary.ts;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;

import static org.pwstudio.nlp.utility.Predefine.logger;

/**
 * 繁简词典，提供简繁转换
 * @author hankcs
 */
public class TraditionalChineseDictionary extends BaseChineseDictionary
{
    /**
     * 繁体=简体
     */
    public static AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<String>();

    static
    {
        long start = System.currentTimeMillis();
        if (!load(PwNLP.Config.tcDictionaryRoot + "t2s.txt", trie, false))
        {
            throw new IllegalArgumentException("繁简词典" + PwNLP.Config.tcDictionaryRoot + "t2s.txt" + "加载失败");
        }

        logger.info("繁简词典" + PwNLP.Config.tcDictionaryRoot + "t2s.txt" + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }

    public static String convertToSimplifiedChinese(String traditionalChineseString)
    {
        return segLongest(traditionalChineseString.toCharArray(), trie);
    }

    public static String convertToSimplifiedChinese(char[] traditionalChinese)
    {
        return segLongest(traditionalChinese, trie);
    }

}
