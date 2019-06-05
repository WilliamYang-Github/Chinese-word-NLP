/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/17 14:31</create-date>
 *
 * <copyright file="ISuggester.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.suggest;

import java.util.List;

/**
 * @author hankcs
 */
public interface ISuggester
{
    void addSentence(String sentence);

    /**
     * 清空该推荐器中的所有句子
     */
    void removeAllSentences();

    /**
     * 根据一个输入的句子推荐相似的句子
     *
     * @param key
     * @param size
     * @return
     */
    List<String> suggest(String key, int size);
}
