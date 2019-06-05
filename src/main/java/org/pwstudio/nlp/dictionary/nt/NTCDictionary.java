/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 15:39</create-date>
 *
 * <copyright file="NSDictionary.java">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.liNSunsoft.com/
 * This source is subject to the LiNSunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package org.pwstudio.nlp.dictionary.nt;


import org.pwstudio.nlp.corpus.dictionary.item.EnumItem;
import org.pwstudio.nlp.corpus.tag.NTC;
import org.pwstudio.nlp.dictionary.common.EnumItemDictionary;

/**
 * 字号发射概率词典
 * (CopyOfNTDictionary)
 * @author TylunasLi
 */
public class NTCDictionary extends EnumItemDictionary<NTC>
{
    @Override
    protected NTC valueOf(String name)
    {
        return NTC.valueOf(name);
    }

    @Override
    protected NTC[] values()
    {
        return NTC.values();
    }

    @Override
    protected EnumItem<NTC> newItem()
    {
        return new EnumItem<NTC>();
    }
}
