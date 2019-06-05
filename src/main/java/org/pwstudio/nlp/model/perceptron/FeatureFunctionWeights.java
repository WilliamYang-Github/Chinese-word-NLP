/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/12/9 20:57</create-date>
 *
 * <copyright file="FeatureFunction.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.model.perceptron;

import java.io.DataOutputStream;

import org.pwstudio.nlp.corpus.io.ByteArray;
import org.pwstudio.nlp.corpus.io.ICacheAble;

/**
 * 特征函数，其实是tag.size个特征函数的集合
 * @author hankcs
 */
public class FeatureFunctionWeights implements ICacheAble
{
    /**
     * 环境参数
     */
    char[] o;
    /**
     * 权值，按照index对应于tag的id
     */
    int[] w;
    /**
     * 训练时的累计权值
     * 按照index对应于tag的id
     */
    int[] total;
    /**
     * 上次更新时的训练实例脚步
     * 按照index对应于tag的id
     */
    int[] lastStep;

    /**
     * 创建一个特征函数权重表
     * @param o
     * @param tagSize
     */
    public FeatureFunctionWeights(char[] o, int tagSize)
    {
        this.o = o;
        w = new int[tagSize];
        total = new int[tagSize];
        lastStep = new int[tagSize];
    }

    public FeatureFunctionWeights()
    {
    }

    @Override
    public void save(DataOutputStream out) throws Exception
    {
        out.writeInt(o.length);
        for (char c : o)
        {
            out.writeChar(c);
        }
        out.writeInt(w.length);
        for (int v : w)
        {
            out.writeInt(v);
        }
    }

    @Override
    public boolean load(ByteArray byteArray)
    {
        int size = byteArray.nextInt();
        o = new char[size];
        for (int i = 0; i < size; ++i)
        {
            o[i] = byteArray.nextChar();
        }
        size = byteArray.nextInt();
        w = new int[size];
        for (int i = 0; i < size; ++i)
        {
            w[i] = byteArray.nextInt();
        }
        return true;
    }
    
    public void update(int stepCount, int tagId, int delta)
    {
        int numStepBeforeUpdate = stepCount - lastStep[tagId];
        total[tagId] += w[tagId]*numStepBeforeUpdate;
        lastStep[tagId] = stepCount;
        w[tagId] += delta;
    }

    public void average(int totalStep)
    {
        for (int tagId = 0; tagId < w.length; tagId++)
        {
            int numStepBeforeUpdate = totalStep - lastStep[tagId];
            total[tagId] += w[tagId]*numStepBeforeUpdate;
            w[tagId] = total[tagId]/totalStep;
        }
    }
}