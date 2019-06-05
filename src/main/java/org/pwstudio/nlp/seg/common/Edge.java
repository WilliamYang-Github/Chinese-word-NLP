/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/05/2014/5/21 19:33</create-date>
 *
 * <copyright file="Edge.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.seg.common;

/**
 * 基础边，不允许构造
 * @author hankcs
 */
public class Edge
{
    /**
     * 花费
     */
    public double weight;
    /**
     * 节点名字，调试用
     */
    String name;

    protected Edge(double weight, String name)
    {
        this.weight = weight;
        this.name = name;
    }
}
