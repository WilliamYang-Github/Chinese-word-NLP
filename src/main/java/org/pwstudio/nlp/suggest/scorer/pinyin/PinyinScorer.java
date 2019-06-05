/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/6 11:44</create-date>
 *
 * <copyright file="PinyinScorer.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.suggest.scorer.pinyin;

import org.pwstudio.nlp.suggest.scorer.BaseScorer;

/**
 * 拼音打分器
 * @author hankcs
 */
public class PinyinScorer extends BaseScorer<PinyinKey>
{
    @Override
    protected PinyinKey generateKey(String sentence)
    {
        PinyinKey pinyinKey = new PinyinKey(sentence);
        if (pinyinKey.size() == 0) return null;
        return pinyinKey;
    }
}
