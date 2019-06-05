/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/13 13:05</create-date>
 *
 * <copyright file="ISynonym.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.synonym;

/**
 * 同义词接口
 * @author hankcs
 */
public interface ISynonym
{
    /**
     * 获取原本的词语
     * @return
     */
    String getRealWord();

    /**
     * 获取ID
     * @return
     */
    long getId();

    /**
     * 获取字符类型的ID
     * @return
     */
    String getIdString();
}
