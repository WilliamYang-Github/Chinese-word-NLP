/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/12/24 23:17</create-date>
 *
 * <copyright file="SpeedTokenizer.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.tokenizer;

import java.util.List;

import org.pwstudio.nlp.seg.Segment;
import org.pwstudio.nlp.seg.Other.DoubleArrayTrieSegment;
import org.pwstudio.nlp.seg.common.Term;

/**
 * 极速分词，基于Double Array Trie实现的词典分词，适用于“高吞吐量”“精度一般”的场合
 * @author hankcs
 */
public class SpeedTokenizer
{
    /**
     * 预置分词器
     */
    public static final Segment SEGMENT = new DoubleArrayTrieSegment();
    public static List<Term> segment(String text)
    {
        return SEGMENT.seg(text.toCharArray());
    }

    /**
     * 分词
     * @param text 文本
     * @return 分词结果
     */
    public static List<Term> segment(char[] text)
    {
        return SEGMENT.seg(text);
    }

    /**
     * 切分为句子形式
     * @param text 文本
     * @return 句子列表
     */
    public static List<List<Term>> seg2sentence(String text)
    {
        return SEGMENT.seg2sentence(text);
    }
}
