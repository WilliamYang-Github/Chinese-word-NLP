/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2015/3/13 18:36</create-date>
 *
 * <copyright file="CommonSuffixExtractor.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.pwstudio.nlp.collection.trie.bintrie.BinTrie;
import org.pwstudio.nlp.corpus.occurrence.TermFrequency;
import org.pwstudio.nlp.seg.common.Term;
import org.pwstudio.nlp.tokenizer.StandardTokenizer;

/**
 * 公共后缀提取工具
 * @author hankcs
 */
public class CommonSuffixExtractor
{
    TFDictionary tfDictionary;

    public CommonSuffixExtractor()
    {
        tfDictionary = new TFDictionary();
    }

    public void add(String key)
    {
        tfDictionary.add(key);
    }

    public List<String> extractSuffixExtended(int length, int size)
    {
        return extractSuffix(length, size, true);
    }

    /**
     * 提取公共后缀
     * @param length 公共后缀长度
     * @param size 频率最高的前多少个公共后缀
     * @param extend 长度是否拓展为从1到length为止的后缀
     * @return 公共后缀列表
     */
    public List<String> extractSuffix(int length, int size, boolean extend)
    {
        TFDictionary suffixTreeSet = new TFDictionary();
        for (String key : tfDictionary.keySet())
        {
            if (key.length() > length)
            {
                suffixTreeSet.add(key.substring(key.length() - length, key.length()));
                if (extend)
                {
                    for (int l = 1; l < length; ++l)
                    {
                        suffixTreeSet.add(key.substring(key.length() - l, key.length()));
                    }
                }
            }
        }

        if (extend)
        {
            size *= length;
        }

        return extract(suffixTreeSet, size);
    }

    private static List<String> extract(TFDictionary suffixTreeSet, int size)
    {
        List<String> suffixList = new ArrayList<String>(size);
        for (TermFrequency termFrequency : suffixTreeSet.values())
        {
            if (suffixList.size() >= size) break;
            suffixList.add(termFrequency.getKey());
        }

        return suffixList;
    }

    /**
     * 此方法认为后缀一定是整个的词语，所以length是以词语为单位的
     * @param length
     * @param size
     * @param extend
     * @return
     */
    public List<String> extractSuffixByWords(int length, int size, boolean extend)
    {
        TFDictionary suffixTreeSet = new TFDictionary();
        for (String key : tfDictionary.keySet())
        {
            List<Term> termList = StandardTokenizer.segment(key);
            if (termList.size() > length)
            {
                suffixTreeSet.add(combine(termList.subList(termList.size() - length, termList.size())));
                if (extend)
                {
                    for (int l = 1; l < length; ++l)
                    {
                        suffixTreeSet.add(combine(termList.subList(termList.size() - l, termList.size())));
                    }
                }
            }
        }

        return extract(suffixTreeSet, size);
    }


    private static String combine(List<Term> termList)
    {
        StringBuilder sbResult = new StringBuilder();
        for (Term term : termList)
        {
            sbResult.append(term.word);
        }

        return sbResult.toString();
    }
}
