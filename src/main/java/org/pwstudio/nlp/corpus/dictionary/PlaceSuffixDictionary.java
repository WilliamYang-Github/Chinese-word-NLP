/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/17 17:43</create-date>
 *
 * <copyright file="PlaceSuffixDictionary.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.dictionary;

import org.pwstudio.nlp.corpus.dictionary.SuffixDictionary;
import org.pwstudio.nlp.utility.Predefine;

/**
 * 做一个简单的封装
 * @author hankcs
 */
public class PlaceSuffixDictionary
{
    public static SuffixDictionary dictionary = new SuffixDictionary();
    static
    {
        dictionary.addAll(Predefine.POSTFIX_SINGLE);
        dictionary.addAll(Predefine.POSTFIX_MUTIPLE);
    }
}
