/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/8 17:43</create-date>
 *
 * <copyright file="IWord.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.document.sentence.word;

import java.io.Serializable;

/**
 * 词语接口
 * @author hankcs
 */
public interface IWord extends Serializable
{
    /**
     * 获取单词
     * @return
     */
    String getValue();

    /**
     * 获取标签
     * @return
     */
    String getLabel();

    /**
     * 设置标签
     * @param label
     */
    void setLabel(String label);

    /**
     * 设置单词
     * @param value
     */
    void setValue(String value);

    /**
     * 单词长度
     * @return
     */
    int length();
}
