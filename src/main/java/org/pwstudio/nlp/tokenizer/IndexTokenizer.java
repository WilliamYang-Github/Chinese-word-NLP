/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/19 18:33</create-date>
 *
 * <copyright file="IndexTokenizer.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.tokenizer;

import java.util.List;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.seg.Segment;
import org.pwstudio.nlp.seg.Dijkstra.DijkstraSegment;
import org.pwstudio.nlp.seg.common.Term;

/**
 * 索引分词器
 * @author hankcs
 */
public class IndexTokenizer
{
    /**
     * 预置分词器
     */
    public static final Segment SEGMENT = PwNLP.newSegment().enableIndexMode(true);
    public static List<Term> segment(String text)
    {
        return SEGMENT.seg(text);
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
