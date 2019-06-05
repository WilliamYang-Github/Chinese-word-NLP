/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2015/1/19 21:02</create-date>
 *
 * <copyright file="Node.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.seg.Viterbi.Path;

import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.utility.MathTools;

/**
 * @author hankcs
 */
public class Node
{
    /**
     * 到该节点的最短路径的前驱节点
     */
    Node from;
    /**
     * 最短路径对应的权重
     */
    double weight;
    /**
     * 节点代表的顶点
     */
    Vertex vertex;

    public Node(Vertex vertex)
    {
        this.vertex = vertex;
    }

    public void updateFrom(Node from)
    {
        double weight = from.weight + MathTools.calculateWeight(from.vertex, this.vertex);
        if (this.from == null || this.weight > weight)
        {
            this.from = from;
            this.weight = weight;
        }
    }

    @Override
    public String toString()
    {
        return vertex.toString();
    }
}
