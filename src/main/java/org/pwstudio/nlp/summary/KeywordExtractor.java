/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/18 18:37</create-date>
 *
 * <copyright file="KeywordExtractor.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.summary;

import org.pwstudio.nlp.dictionary.stopword.CoreStopWordDictionary;
import org.pwstudio.nlp.seg.Segment;
import org.pwstudio.nlp.seg.common.Term;
import org.pwstudio.nlp.tokenizer.StandardTokenizer;

/**
 * 提取关键词的基类
 * @author hankcs
 */
public class KeywordExtractor
{
    /**
     * 默认分词器
     */
    Segment defaultSegment = StandardTokenizer.SEGMENT;

    /**
     * 是否应当将这个term纳入计算，词性属于名词、动词、副词、形容词
     *
     * @param term
     * @return 是否应当
     */
    public boolean shouldInclude(Term term)
    {
        // 除掉停用词
        return CoreStopWordDictionary.shouldInclude(term);
    }

    /**
     * 设置关键词提取器使用的分词器
     * @param segment 任何开启了词性标注的分词器
     * @return 自己
     */
    public KeywordExtractor setSegment(Segment segment)
    {
        defaultSegment = segment;
        return this;
    }
}
