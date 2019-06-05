/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/1 19:52</create-date>
 *
 * <copyright file="SimpleDictionaryMaker.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.dictionary;

import java.util.LinkedList;
import java.util.List;

/**
 * 方便的工厂类
 * @author hankcs
 */
public class StringDictionaryMaker
{
    /**
     * 加载词典
     * @param path
     * @param separator
     * @return
     */
    public static StringDictionary load(String path, String separator)
    {
        StringDictionary dictionary = new StringDictionary(separator);
        if (dictionary.load(path)) return dictionary;
        return null;
    }

    /**
     * 加载词典
     * @param path
     * @return
     */
    public static StringDictionary load(String path)
    {
        return load(path, "=");
    }

    /**
     * 合并词典，第一个为主词典
     * @param args
     * @return
     */
    public static StringDictionary combine(StringDictionary... args)
    {
        StringDictionary[] dictionaries = args.clone();
        StringDictionary mainDictionary = dictionaries[0];
        for (int i = 1; i < dictionaries.length; ++i)
        {
            mainDictionary.combine(dictionaries[i]);
        }

        return mainDictionary;
    }

    public static StringDictionary combine(String... args)
    {
        String[] pathArray = args.clone();
        List<StringDictionary> dictionaryList = new LinkedList<StringDictionary>();
        for (String path : pathArray)
        {
            StringDictionary dictionary = load(path);
            if (dictionary == null) continue;
            dictionaryList.add(dictionary);
        }

        return combine(dictionaryList.toArray(new StringDictionary[0]));
    }
}
