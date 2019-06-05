/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/05/2014/5/21 20:13</create-date>
 *
 * <copyright file="PathNode.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.seg.NShort.Path;

/**
 * 路径上的节点
 * @author hankcs
 */
public class PathNode
{
    /**
     * 节点前驱
     */
    public int from;
    /**
     * 节点在顶点数组中的下标
     */
    public int index;

    /**
     * 构造一个节点
     * @param from 节点前驱
     * @param index 节点在顶点数组中的下标
     */
    public PathNode(int from, int index)
    {
        this.from = from;
        this.index = index;
    }

    @Override
    public String toString()
    {
        return "PathNode{" +
                "from=" + from +
                ", index=" + index +
                '}';
    }
}
