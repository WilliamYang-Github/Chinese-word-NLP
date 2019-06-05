/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 15:39</create-date>
 *
 * <copyright file="NRDictionary.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.dictionary.nr;


import java.util.TreeMap;

import org.pwstudio.nlp.corpus.dictionary.item.EnumItem;
import org.pwstudio.nlp.corpus.tag.NR;
import org.pwstudio.nlp.dictionary.common.EnumItemDictionary;

/**
 * 一个好用的人名词典
 *
 * @author hankcs
 */
public class NRDictionary extends EnumItemDictionary<NR>
{

    @Override
    protected NR valueOf(String name)
    {
        return NR.valueOf(name);
    }

    @Override
    protected NR[] values()
    {
        return NR.values();
    }

    @Override
    protected EnumItem<NR> newItem()
    {
        return new EnumItem<NR>();
    }

    @Override
    protected void onLoaded(TreeMap<String, EnumItem<NR>> map)
    {
    //    map.put(" ", new EnumItem<NR>(NR.K, NR.A)); // txt中不允许出现空格词条，这里补上
    }
}
