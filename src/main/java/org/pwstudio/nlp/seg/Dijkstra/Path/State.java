/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/29 15:35</create-date>
 *
 * <copyright file="State.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.seg.Dijkstra.Path;

/**
 * @author hankcs
 */
public class State implements Comparable<State>
{
    /**
     * 路径花费
     */
    public double cost;
    /**
     * 当前位置
     */
    public int vertex;

    @Override
    public int compareTo(State o)
    {
        return Double.compare(cost, o.cost);
    }

    public State(double cost, int vertex)
    {
        this.cost = cost;
        this.vertex = vertex;
    }
}
