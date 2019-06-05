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

import java.io.IOException;
import java.util.List;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.seg.Segment;
import org.pwstudio.nlp.seg.common.Term;

/**
 * 可供自然语言处理用的分词器，更重视准确率。
 *
 * @author hankcs
 */
public class NLPTokenizer
{
    /**
     * 预置分词器
     */
    public static final Segment SEGMENT = PwNLP.newSegment().enableNameRecognize(true).enableTranslatedNameRecognize(true)
            .enableJapaneseNameRecognize(true).enablePlaceRecognize(true).enableOrganizationRecognize(true)
            .enablePartOfSpeechTagging(true);

    public static List<Term> segment(String text)
    {
        return SEGMENT.seg(text);
    }

    /**
     * 分词
     *
     * @param text 文本
     * @return 分词结果
     */
    public static List<Term> segment(char[] text)
    {
        return SEGMENT.seg(text);
    }

    /**
     * 切分为句子形式
     *
     * @param text 文本
     * @return 句子列表
     */
    public static List<List<Term>> seg2sentence(String text)
    {
        return SEGMENT.seg2sentence(text);
    }
}
