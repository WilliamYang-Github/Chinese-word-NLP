/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/8 18:49</create-date>
 *
 * <copyright file="WordFactory.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.document.sentence.word;

/**
 * 一个很方便的工厂类，能够自动生成不同类型的词语
 * @author hankcs
 */
public class WordFactory
{
    /**
     * 根据参数字符串产生对应的词语
     * @param param
     * @return
     */
    public static IWord create(String param)
    {
        if (param == null) return null;
        if (param.startsWith("[") && !param.startsWith("[/"))
        {
            return CompoundWord.create(param);
        }
        else
        {
            return Word.create(param);
        }
    }
}
