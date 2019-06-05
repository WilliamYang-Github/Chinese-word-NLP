/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/5/3 12:32</create-date>
 *
 * <copyright file="ArrayTool.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.collection.trie.bintrie.util;


import org.pwstudio.nlp.collection.trie.bintrie.BaseNode;

/**
 * @author He Han
 */
public class ArrayTool
{
    /**
     * 二分查找
     * @param branches 数组
     * @param node 要查找的node
     * @return 数组下标，小于0表示没找到
     */
    public static int binarySearch(BaseNode[] branches, BaseNode node)
    {
        int high = branches.length - 1;
        if (branches.length < 1)
        {
            return high;
        }
        int low = 0;
        while (low <= high)
        {
            int mid = (low + high) >>> 1;
            int cmp = branches[mid].compareTo(node);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
    }

    public static int binarySearch(BaseNode[] branches, char node)
    {
        int high = branches.length - 1;
        if (branches.length < 1)
        {
            return high;
        }
        int low = 0;
        while (low <= high)
        {
            int mid = (low + high) >>> 1;
            int cmp = branches[mid].compareTo(node);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
    }
}
