/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/7 10:02</create-date>
 *
 * <copyright file="Pinyin2Integer.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.dictionary.py;

/**
 * 将整型转为拼音
 * @author hankcs
 */
public class Integer2PinyinConverter
{
    public static final Pinyin[] pinyins = Pinyin.values();

    public static Pinyin getPinyin(int ordinal)
    {
        return pinyins[ordinal];
    }
}
