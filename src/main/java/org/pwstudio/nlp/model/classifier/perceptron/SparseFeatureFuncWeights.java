package org.pwstudio.nlp.model.classifier.perceptron;

import org.pwstudio.nlp.corpus.io.ByteArray;
import org.pwstudio.nlp.corpus.io.ICacheAble;

import java.io.DataOutputStream;
import java.util.Arrays;

/**
 * 特征函数，其实是tag.size个特征函数的集合
 * @author TylunasLi
 */
public class SparseFeatureFuncWeights implements ICacheAble
{
    /**
     * 特征函数名称
     */
    char[] name;
    /**
     * 权值，按照index对应于tag的id
     */
    int[] ids;
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
    public SparseFeatureFuncWeights(char[] o)
    {
        this.name = o;
    }

    /**
     * 丛数据源读取时使用的构造函数
     */
    public SparseFeatureFuncWeights()
    {
    }
    
    @Override
    public void save(DataOutputStream out) throws Exception
    {
        out.writeInt(name.length);
        for (char c : name)
        {
            out.writeChar(c);
        }
        out.writeInt(ids.length);
        for (int v : ids)
        {
            out.writeInt(v);
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
        name = new char[size];
        for (int i = 0; i < size; ++i)
        {
            name[i] = byteArray.nextChar();
        }
        size = byteArray.nextInt();
        ids = new int[size];
        for (int i = 0; i < size; ++i)
        {
            ids[i] = byteArray.nextInt();
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
        int innerId = -1;
        if (ids != null)
        {
            for (int i=0; i<ids.length; i++)
            {
                if (tagId == ids[i])
                    innerId = i;
            }
        }
        if (innerId == -1)
        {
            ids = expandOne(ids);
            innerId = ids.length-1;
            ids[ids.length-1] = tagId;
            w = expandOne(w);
            total = expandOne(total);
            lastStep = expandOne(lastStep);
        }
        int numStepBeforeUpdate = stepCount - lastStep[innerId];
        total[innerId] += w[innerId]*numStepBeforeUpdate;
        lastStep[innerId] = stepCount;
        w[innerId] += delta;
    }

    private int[] expandOne(int[] old) {
        if (old == null) return new int[1]; 
        int[] newArray = new int[old.length+1];
        System.arraycopy(old, 0, newArray, 0, old.length);
        return newArray;
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

    @Override
    public String toString()
    {
        return "FeatureWeight [name=" + new String(name) + ", w=" + Arrays.toString(w) + "]";
    }

    /**
     * 获取某一标签的特征权重
     * @param tagId
     * @return
     */
    public float weight(int tagId)
    {
        if (ids == null)
            return 0;
        for (int i=0; i<ids.length; i++)
        {
            if (tagId == ids[i])
                return w[i];
        }
        return 0;
    }

    public char[] getName()
    {
        return name;
    }

}
