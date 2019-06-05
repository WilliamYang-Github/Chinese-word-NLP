package org.pwstudio.nlp.model.classifier.naivebayes;

import org.pwstudio.nlp.corpus.io.ByteArray;
import org.pwstudio.nlp.corpus.io.ICacheAble;

import java.io.DataOutputStream;
import java.util.Arrays;

/**
 * 特征函数，其实是tag.size个特征函数的集合
 * @author TylunasLi
 */
public class FeatureFunctionWeights implements ICacheAble
{
    /**
     * 特征函数名称
     */
    char[] name;
    /**
     * 权值，按照index对应于tag的id
     */
    float[] w;
    /**
     * 训练时的累计权值
     * 按照index对应于tag的id
     */
    int total;
    /**
     * 上次更新时的训练实例脚步
     * 按照index对应于tag的id
     */
    int[] count;

    /**
     * 创建一个特征函数权重表
     * @param o
     * @param tagSize
     */
    public FeatureFunctionWeights(char[] o, int tagSize)
    {
        this.name = o;
        w = new float[tagSize];
        total = 0;
        count = new int[tagSize];
    }

    /**
     * 丛数据源读取时使用的构造函数
     */
    public FeatureFunctionWeights()
    {
    }

    public FeatureFunctionWeights(float[] weights)
    {
        w = weights;
	}
	
    @Override
    public void save(DataOutputStream out) throws Exception
    {
        out.writeInt(name.length);
        for (char c : name)
        {
            out.writeChar(c);
        }
        out.writeInt(total);
        out.writeInt(w.length);
        for (float v : w)
        {
            out.writeFloat(v);
        }
    }

    @Override
    public boolean load(ByteArray byteArray)
    {
        int size = byteArray.nextInt();
        name = new char[size];
        for (int i = 0; i < size; ++i)
        {
            name[i] = byteArray.nextChar();
        }
        total = byteArray.nextInt();
        size = byteArray.nextInt();
        w = new float[size];
        for (int i = 0; i < size; ++i)
        {
            w[i] = byteArray.nextFloat();
        }
        return true;
    }
    
    public void update(int stepCount, int tagId, int delta)
    {
        if (count == null)
        {
        	count = new int[w.length];
        	for (int i=0; i<w.length; i++)
        	{
        		count[i] = (int) w[i]*total;
        	}
        }
        count[tagId] += delta;
        total += delta;
    }

    public void build(int[] categoryCount, int totalDimension)
    {
        if (count == null)
        {
            return;
        }
        for (int tagId = 0; tagId < w.length; tagId++)
        {
            w[tagId] = (float) Math.log((count[tagId]+1.0) / (categoryCount[tagId]+totalDimension));
        }
    }

    @Override
    public String toString()
    {
        return "FeatureWeight [name=" + Arrays.toString(name) + ", w=" + Arrays.toString(w) + "]";
    }

    /**
     * 获取某一标签的特征权重
     * @param tagId
     * @return
     */
    public float weight(int tagId)
    {
        return w[tagId];
    }

    public char[] getName()
    {
        return name;
    }

}