/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 17:12</create-date>
 *
 * <copyright file="Viterbi.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.algorithm;

import java.util.*;

import org.pwstudio.nlp.corpus.dictionary.item.EnumItem;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.TransformMatrixDictionary;
import org.pwstudio.nlp.seg.common.Vertex;

/**
 * 维特比算法
 *
 * @author hankcs
 */
public class Viterbi
{
    /**
     * 求解HMM模型，所有概率请提前取对数
     *
     * @param obs     观测序列
     * @param states  隐状态
     * @param start_p 初始概率（隐状态）
     * @param trans_p 转移概率（隐状态）
     * @param emit_p  发射概率 （隐状态表现为显状态的概率）
     * @return 最可能的序列
     */
    public static int[] compute(int[] obs, int[] states, double[] start_p, double[][] trans_p, double[][] emit_p)
    {
        int _max_states_value = 0;
        for (int s : states)
        {
            _max_states_value = Math.max(_max_states_value, s);
        }
        ++_max_states_value;
        double[][] V = new double[obs.length][_max_states_value];
        int[][] path = new int[_max_states_value][obs.length];

        for (int y : states)
        {
            V[0][y] = start_p[y] + emit_p[y][obs[0]];
            path[y][0] = y;
        }

        for (int t = 1; t < obs.length; ++t)
        {
            int[][] newpath = new int[_max_states_value][obs.length];

            for (int y : states)
            {
                double prob = Double.MAX_VALUE;
                int state;
                for (int y0 : states)
                {
                    double nprob = V[t - 1][y0] + trans_p[y0][y] + emit_p[y][obs[t]];
                    if (nprob < prob)
                    {
                        prob = nprob;
                        state = y0;
                        // 记录最大概率
                        V[t][y] = prob;
                        // 记录路径
                        System.arraycopy(path[state], 0, newpath[y], 0, t);
                        newpath[y][t] = y;
                    }
                }
            }

            path = newpath;
        }

        double prob = Double.MAX_VALUE;
        int state = 0;
        for (int y : states)
        {
            if (V[obs.length - 1][y] < prob)
            {
                prob = V[obs.length - 1][y];
                state = y;
            }
        }

        return path[state];
    }

    /**
     * 特化版的求解HMM模型
     *
     * @param vertexList                包含Vertex.B节点的路径
     * @param transformMatrixDictionary 词典对应的转移矩阵
     */
    public static void compute(List<Vertex> vertexList, TransformMatrixDictionary<Nature> transformMatrixDictionary)
    {
        int length = vertexList.size() - 1;
        double[][] cost = new double[2][];  // 滚动数组
        Iterator<Vertex> iterator = vertexList.iterator();
        Vertex start = iterator.next();
        Nature pre = start.attribute.nature[0];
        // 第一个是确定的
//        start.confirmNature(pre);
        // 第二个也可以简单地算出来
        Vertex preItem;
        Nature[] preTagSet;
        {
            Vertex item = iterator.next();
            cost[0] = new double[item.attribute.nature.length];
            int j = 0;
            int curIndex = 0;
            for (Nature cur : item.attribute.nature)
            {
                cost[0][j] = transformMatrixDictionary.transititon_probability[pre.ordinal()][cur.ordinal()] - Math.log((item.attribute.frequency[curIndex] + 1e-8) / transformMatrixDictionary.getTotalFrequency(cur));
                ++j;
                ++curIndex;
            }
            preTagSet = item.attribute.nature;
            preItem = item;
        }
        // 第三个开始复杂一些
        for (int i = 1; i < length; ++i)
        {
            int index_i = i & 1;
            int index_i_1 = 1 - index_i;
            Vertex item = iterator.next();
            cost[index_i] = new double[item.attribute.nature.length];
            double perfect_cost_line = Double.MAX_VALUE;
            int k = 0;
            Nature[] curTagSet = item.attribute.nature;
            for (Nature cur : curTagSet)
            {
                cost[index_i][k] = Double.MAX_VALUE;
                int j = 0;
                for (Nature p : preTagSet)
                {
                    double now = cost[index_i_1][j] + transformMatrixDictionary.transititon_probability[p.ordinal()][cur.ordinal()] - Math.log((item.attribute.frequency[k] + 1e-8) / transformMatrixDictionary.getTotalFrequency(cur));
                    if (now < cost[index_i][k])
                    {
                        cost[index_i][k] = now;
                        if (now < perfect_cost_line)
                        {
                            perfect_cost_line = now;
                            pre = p;
                        }
                    }
                    ++j;
                }
                ++k;
            }
            preItem.confirmNature(pre);
            preTagSet = curTagSet;
            preItem = item;
        }
    }

    /**
     * Viterbi求解HMM词性标注全局最优序列
     *
     * @param vertexList                包含Vertex.B节点的路径
     * @param transformMatrixDictionary 词典对应的转移矩阵
     */
    public static void computeGlobal(List<Vertex> vertexList, TransformMatrixDictionary<Nature> transformMatrixDictionary)
    {
        int length = vertexList.size() - 1;
        // 分别记录每个词性标记的前驱节点是谁
        int[][] tagRoute = new int[length][];
        int lastBest = -1;
        // 概率数组，滚动使用
        double[][] cost = new double[2][];
        Iterator<Vertex> iterator = vertexList.iterator();
        Vertex start = iterator.next();
        Nature pre = start.attribute.nature[0];
        // 第一个是确定的
//        start.confirmNature(pre);
        // 第二个也可以简单地算出来
//        Vertex preItem;
        Nature[] preTagSet;
        {
            Vertex item = iterator.next();
            cost[0] = new double[item.attribute.nature.length];
            int j = 0;
            int curIndex = 0;
            for (Nature cur : item.attribute.nature)
            {
                cost[0][j] = transformMatrixDictionary.transititon_probability[pre.ordinal()][cur.ordinal()] - Math.log((item.attribute.frequency[curIndex] + 1e-8) / transformMatrixDictionary.getTotalFrequency(cur));
                ++j;
                ++curIndex;
            }
            tagRoute[0] = new int[item.attribute.nature.length];
            preTagSet = item.attribute.nature;
//            preItem = item;
        }
        // 第三个开始复杂一些
        for (int i = 1; i < length; ++i)
        {
            int index_i = i & 1;            // 当前词的路径分数在cost中的下标
            int index_i_1 = 1 - index_i;    // 上一个词的路径分数在cost中的下标
            Vertex item = iterator.next();
            cost[index_i] = new double[item.attribute.nature.length];
            double perfect_cost_line = Double.MAX_VALUE;
            int k = 0;
            Nature[] curTagSet = item.attribute.nature;
            int [] curRoute = new int[curTagSet.length];
            for (Nature cur : curTagSet)
            {
                cost[index_i][k] = Double.MAX_VALUE;
                int j = 0;
                for (Nature p : preTagSet)
                {
                    double now = cost[index_i_1][j] + transformMatrixDictionary.transititon_probability[p.ordinal()][cur.ordinal()] - Math.log((item.attribute.frequency[k] + 1e-8) / transformMatrixDictionary.getTotalFrequency(cur));
                    if (now < cost[index_i][k])
                    {
                        cost[index_i][k] = now;
                        curRoute[k] = j;
                        if (now < perfect_cost_line)
                        {
                            perfect_cost_line = now;
                            lastBest = k;
                        }
                    }
                    ++j;
                }
                ++k;
            }
//            preItem.confirmNature(pre);
            tagRoute[i] = curRoute;
            preTagSet = curTagSet;
//            preItem = item;
        }
        int idx = lastBest;
        if (lastBest == -1)
            idx = preTagSet.length - 1;
        for (int k=length; k>0; k--)
        {
            Vertex vertex = vertexList.get(k);
            vertex.confirmNature(vertex.attribute.nature[idx]);
            idx = tagRoute[k-1][idx];
        }
    }

    /**
     * 标准版的Viterbi算法，查准率高，效率稍低
     *
     * @param roleTagList               观测序列
     * @param transformMatrixDictionary 转移矩阵
     * @param <E>                       EnumItem的具体类型
     * @return 预测结果
     */
    public static <E extends Enum<E>> List<E> computeEnum(List<EnumItem<E>> roleTagList, TransformMatrixDictionary<E> transformMatrixDictionary)
    {
        int length = roleTagList.size() - 1;
        List<E> tagList = new ArrayList<E>(roleTagList.size());
        double[][] cost = new double[2][];  // 滚动数组
        Iterator<EnumItem<E>> iterator = roleTagList.iterator();
        EnumItem<E> start = iterator.next();
        E pre = start.labelMap.entrySet().iterator().next().getKey();
        // 第一个是确定的
        tagList.add(pre);
        // 第二个也可以简单地算出来
        Set<E> preTagSet;
        {
            EnumItem<E> item = iterator.next();
            cost[0] = new double[item.labelMap.size()];
            int j = 0;
            for (E cur : item.labelMap.keySet())
            {
                cost[0][j] = transformMatrixDictionary.transititon_probability[pre.ordinal()][cur.ordinal()] - Math.log((item.getFrequency(cur) + 1e-8) / transformMatrixDictionary.getTotalFrequency(cur));
                ++j;
            }
            preTagSet = item.labelMap.keySet();
        }
        // 第三个开始复杂一些
        for (int i = 1; i < length; ++i)
        {
            int index_i = i & 1;
            int index_i_1 = 1 - index_i;
            EnumItem<E> item = iterator.next();
            cost[index_i] = new double[item.labelMap.size()];
            double perfect_cost_line = Double.MAX_VALUE;
            int k = 0;
            Set<E> curTagSet = item.labelMap.keySet();
            for (E cur : curTagSet)
            {
                cost[index_i][k] = Double.MAX_VALUE;
                int j = 0;
                for (E p : preTagSet)
                {
                    double now = cost[index_i_1][j] + transformMatrixDictionary.transititon_probability[p.ordinal()][cur.ordinal()] - Math.log((item.getFrequency(cur) + 1e-8) / transformMatrixDictionary.getTotalFrequency(cur));
                    if (now < cost[index_i][k])
                    {
                        cost[index_i][k] = now;
                        if (now < perfect_cost_line)
                        {
                            perfect_cost_line = now;
                            pre = p;
                        }
                    }
                    ++j;
                }
                ++k;
            }
            tagList.add(pre);
            preTagSet = curTagSet;
        }
        tagList.add(tagList.get(0));    // 对于最后一个##末##
        return tagList;
    }

    /**
     * 仅仅利用了转移矩阵的“维特比”算法
     *
     * @param roleTagList               观测序列
     * @param transformMatrixDictionary 转移矩阵
     * @param <E>                       EnumItem的具体类型
     * @return 预测结果
     */
    public static <E extends Enum<E>> List<E> computeEnumSimply(List<EnumItem<E>> roleTagList, TransformMatrixDictionary<E> transformMatrixDictionary)
    {
        int length = roleTagList.size() - 1;
        List<E> tagList = new LinkedList<E>();
        Iterator<EnumItem<E>> iterator = roleTagList.iterator();
        EnumItem<E> start = iterator.next();
        E pre = start.labelMap.entrySet().iterator().next().getKey();
        E perfect_tag = pre;
        // 第一个是确定的
        tagList.add(pre);
        for (int i = 0; i < length; ++i)
        {
            double perfect_cost = Double.MAX_VALUE;
            EnumItem<E> item = iterator.next();
            for (E cur : item.labelMap.keySet())
            {
                double now = transformMatrixDictionary.transititon_probability[pre.ordinal()][cur.ordinal()] - Math.log((item.getFrequency(cur) + 1e-8) / transformMatrixDictionary.getTotalFrequency(cur));
                if (perfect_cost > now)
                {
                    perfect_cost = now;
                    perfect_tag = cur;
                }
            }
            pre = perfect_tag;
            tagList.add(pre);
        }
        return tagList;
    }
    
    /**
     * 计算边的权值的方法
     * 抽象出来以便计算权重
     * @param <E>
     */
    public static interface EdgeWeightCalculator<E extends Enum<E>>
    {
        /**
         * 计算权重
         * @param last      上一个元素
         * @param curIdx    当前是第curIdx个元素
         * @param item      单个项目
         * @param prevtag   上一个标签
         * @param curTag    当前标签
         * @return
         */
        double calculateWeight(double last, int curIdx, EnumItem<E> item, E prevtag, E curTag);
    }
    
    /**
     * 标准HMM模型的Viterbi计算公式
     * 
     * @param <E>
     */
    public static class HMMWeightCalculator<E extends Enum<E>> implements EdgeWeightCalculator<E>
    {
        protected TransformMatrixDictionary<E> transformMatrixDictionary;
        
        public HMMWeightCalculator(TransformMatrixDictionary<E> transformMatrixDictionary)
        {
            this.transformMatrixDictionary = transformMatrixDictionary;
        }

        @Override
        public double calculateWeight(double last, int curIdx, EnumItem<E> item, E prevtag, E curTag)
        {
            double step = transformMatrixDictionary.transititon_probability[prevtag.ordinal()][curTag.ordinal()]
                    - Math.log((item.getFrequency(curTag) + 1e-8) / transformMatrixDictionary.getTotalFrequency(curTag));
            return last+step;
        }

    }

    /**
     * 标准版的Viterbi算法，查准率高，效率稍低
     *
     * @param roleTagList               观测序列
     * @param transformMatrixDictionary 转移矩阵
     * @param <E>                       EnumItem的具体类型
     * @return 预测结果
     */
    public static <E extends Enum<E>> List<E> computeForEnum(List<EnumItem<E>> roleTagList, EdgeWeightCalculator<E> calculator)
    {
        int length = roleTagList.size() - 1;
        // 分别记录每个词性标记的前驱节点是谁
        int[][] tagRoute = new int[length][];
        ArrayList<ArrayList<E>> tags = new ArrayList<ArrayList<E>>(roleTagList.size());
        LinkedList<E> tagList = new LinkedList<E>();
        double[][] cost = new double[2][];  // 滚动数组
        int lastBest = -1;
        Iterator<EnumItem<E>> iterator = roleTagList.iterator();
        EnumItem<E> start = iterator.next();
        E pre = start.labelMap.entrySet().iterator().next().getKey();
        // 第一个是确定的
        tagList.add(pre);
        // 第二个也可以简单地算出来
        Set<E> preTagSet;
        {
            EnumItem<E> item = iterator.next();
            cost[0] = new double[item.labelMap.size()];
            int j = 0;
            ArrayList<E> curTagList = new ArrayList<E>();
            tags.add(curTagList);
            for (E cur : item.labelMap.keySet())
            {
                cost[0][j] = calculator.calculateWeight(0.0, 0, item, pre, cur);
                ++j;
                curTagList.add(cur);
            }
            preTagSet = item.labelMap.keySet();
            tagRoute[0]=new int[preTagSet.size()];
        }
        // 第三个开始复杂一些
        for (int i = 1; i < length; ++i)
        {
            int index_i = i & 1;
            int index_i_1 = 1 - index_i;
            EnumItem<E> item = iterator.next();
            cost[index_i] = new double[item.labelMap.size()];
            double perfect_cost_line = Double.MAX_VALUE;
            int k = 0;
            Set<E> curTagSet = item.labelMap.keySet();
            ArrayList<E> curTagList = new ArrayList<E>(curTagSet.size());
            tags.add(curTagList);
            int [] curRoute = new int[curTagSet.size()];
            for (E cur : curTagSet)
            {
                curTagList.add(cur);
                cost[index_i][k] = Double.MAX_VALUE;
                int j = 0;
                for (E p : preTagSet)
                {
                    double now = calculator.calculateWeight(cost[index_i_1][j], i, item, p, cur);
                    if (now < cost[index_i][k])
                    {
                        cost[index_i][k] = now;
                        curRoute[k] = j;
                        if (now < perfect_cost_line)
                        {
                            perfect_cost_line = now;
                            lastBest = k;
                        }
                    }
                    ++j;
                }
                ++k;
            }
            tagRoute[i] = curRoute;
            preTagSet = curTagSet;
        }
        int idx = lastBest;
        if (lastBest == -1)
            idx = preTagSet.size() - 1;
        for (int k=length; k>0; k--)
        {
            tagList.addFirst(tags.get(k).get(idx));
            idx = tagRoute[k-1][idx];
        }
        return tagList;
    }
    
}
