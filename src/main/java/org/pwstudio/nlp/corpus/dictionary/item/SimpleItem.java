/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 15:00</create-date>
 *
 * <copyright file="SimpleItem.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.dictionary.item;

import java.util.*;

/**
 * @author hankcs
 */
public class SimpleItem<T>
{
    /**
     * 该条目的标签
     */
    public Map<T, Integer> labelMap;

    public SimpleItem()
    {
        labelMap = new TreeMap<T, Integer>();
    }

    public void addLabel(T label)
    {
        Integer frequency = labelMap.get(label);
        if (frequency == null)
        {
            frequency = 1;
        }
        else
        {
            ++frequency;
        }

        labelMap.put(label, frequency);
    }

    /**
     * 添加一个标签和频次
     * @param label
     * @param frequency
     */
    public void addLabel(T label, Integer frequency)
    {
        Integer innerFrequency = labelMap.get(label);
        if (innerFrequency == null)
        {
            innerFrequency = frequency;
        }
        else
        {
            innerFrequency += frequency;
        }

        labelMap.put(label, innerFrequency);
    }

    /**
     * 删除一个标签
     * @param label 标签
     */
    public void removeLabel(T label)
    {
        labelMap.remove(label);
    }

    public boolean containsLabel(T label)
    {
        return labelMap.containsKey(label);
    }

    public int getFrequency(T label)
    {
        Integer frequency = labelMap.get(label);
        if (frequency == null) return 0;
        return frequency;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        ArrayList<Map.Entry<T, Integer>> entries = new ArrayList<Map.Entry<T, Integer>>(labelMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<T, Integer>>()
        {
            @Override
            public int compare(Map.Entry<T, Integer> o1, Map.Entry<T, Integer> o2)
            {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });
        for (Map.Entry<T, Integer> entry : entries)
        {
            sb.append(entry.getKey());
            sb.append(' ');
            sb.append(entry.getValue());
            sb.append(' ');
        }
        return sb.toString();
    }

//    public static SimpleItem<String> create(String param)
//    {
//        if (param == null) return null;
//        String[] array = param.split(" ");
//        return create(array);
//    }
//
//    public static SimpleItem<String> create(String param[])
//    {
//        if (param.length % 2 == 1) return null;
//        SimpleItem<String> item = new SimpleItem<String>();
//        int natureCount = (param.length) / 2;
//        for (int i = 0; i < natureCount; ++i)
//        {
//            item.labelMap.put(param[2 * i], Integer.parseInt(param[1 + 2 * i]));
//        }
//        return item;
//    }

    /**
     * 合并两个条目，两者的标签map会合并
     * @param other
     */
    public void combine(SimpleItem<T> other)
    {
        for (Map.Entry<T, Integer> entry : other.labelMap.entrySet())
        {
            addLabel(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取全部频次
     * @return
     */
    public int getTotalFrequency()
    {
        int frequency = 0;
        for (Integer f : labelMap.values())
        {
            frequency += f;
        }
        return frequency;
    }

    public T getMostLikelyLabel()
    {
        return labelMap.entrySet().iterator().next().getKey();
    }
    
    /**
     * 当前词语的标签计数
     * @return
     */
    public int getLabelCount()
    {
        return labelMap.size();
    }
}
