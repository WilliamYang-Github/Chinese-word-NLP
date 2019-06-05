/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/05/2014/5/21 19:32</create-date>
 *
 * <copyright file="EdgeFrom.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.seg.common;

/**
 * 记录了起点的边
 * @author hankcs
 */
public class EdgeFrom extends Edge
{
    public int from;

    public EdgeFrom(int from, double weight, String name)
    {
        super(weight, name);
        this.from = from;
    }

    @Override
    public String toString()
    {
        return "EdgeFrom{" +
                "from=" + from +
                ", weight=" + weight +
                ", name='" + name + '\'' +
                '}';
    }
}
