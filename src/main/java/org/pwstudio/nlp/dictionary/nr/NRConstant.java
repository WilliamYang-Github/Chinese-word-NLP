/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/12/30 23:17</create-date>
 *
 * <copyright file="NRConstant.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.dictionary.nr;

import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.utility.Predefine;

/**
 * 人名识别中常用的一些常量
 * @author hankcs
 */
public class NRConstant
{
    /**
     * 本词典专注的词的ID
     */
    public static final int WORD_ID = CoreDictionary.getWordID(Predefine.TAG_PEOPLE);
    /**
     * 本词典专注的词的属性
     */
    public static final CoreDictionary.Attribute ATTRIBUTE = CoreDictionary.get(WORD_ID);
}
