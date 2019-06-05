/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/2 12:08</create-date>
 *
 * <copyright file="UnEmptyStringSet.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.collection.set;

import java.util.TreeSet;

/**
 * 一个不接受空白的字符串set
 * @author hankcs
 */
public class UnEmptyStringSet extends TreeSet<String>
{
    @Override
    public boolean add(String s)
    {
        if (s.trim().length() == 0) return false;

        return super.add(s);
    }
}
