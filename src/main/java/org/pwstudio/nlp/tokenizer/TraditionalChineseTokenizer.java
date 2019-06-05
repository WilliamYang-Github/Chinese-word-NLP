/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/20 20:20</create-date>
 *
 * <copyright file="NLPTokenizer.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.tokenizer;

import java.util.LinkedList;
import java.util.List;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.dictionary.other.CharTable;
import org.pwstudio.nlp.dictionary.ts.SimplifiedChineseDictionary;
import org.pwstudio.nlp.seg.Segment;
import org.pwstudio.nlp.seg.common.Term;
import org.pwstudio.nlp.utility.SentencesUtil;

/**
 * 繁体中文分词器
 *
 * @author hankcs
 */
public class TraditionalChineseTokenizer
{
    /**
     * 预置分词器
     */
    public static Segment SEGMENT = PwNLP.newSegment();

    private static List<Term> segSentence(String text)
    {
        String sText = CharTable.convert(text);
        List<Term> termList = SEGMENT.seg(sText);
        int offset = 0;
        for (Term term : termList)
        {
            String tText;
            term.offset = offset;
            if (term.length() == 1 || (tText = SimplifiedChineseDictionary.getTraditionalChinese(term.word)) == null)
            {
                term.word = text.substring(offset, offset + term.length());
                offset += term.length();
            }
            else
            {
                offset += term.length();
                term.word = tText;
            }
        }

        return termList;
    }

    public static List<Term> segment(String text)
    {
        List<Term> termList = new LinkedList<Term>();
        for (String sentence : SentencesUtil.toSentenceList(text))
        {
            termList.addAll(segSentence(sentence));
        }

        return termList;
    }

    /**
     * 分词
     *
     * @param text 文本
     * @return 分词结果
     */
    public static List<Term> segment(char[] text)
    {
        return segment(CharTable.convert(text));
    }

    /**
     * 切分为句子形式
     *
     * @param text 文本
     * @return 句子列表
     */
    public static List<List<Term>> seg2sentence(String text)
    {
        List<List<Term>> resultList = new LinkedList<List<Term>>();
        {
            for (String sentence : SentencesUtil.toSentenceList(text))
            {
                resultList.add(segment(sentence));
            }
        }

        return resultList;
    }
}
