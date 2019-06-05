package org.pwstudio.nlp.model.classifier.perceptron;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.classification.IMultipleClassifyModel;
import org.pwstudio.nlp.classification.vo.InstanceVector;
import org.pwstudio.nlp.classification.vo.PredictResult;
import org.pwstudio.nlp.collection.trie.ITrie;
import org.pwstudio.nlp.collection.trie.bintrie.BinTrie;
import org.pwstudio.nlp.corpus.io.ByteArray;
import org.pwstudio.nlp.corpus.io.ICacheAble;
import org.pwstudio.nlp.utility.TextUtility;

import static org.pwstudio.nlp.utility.Predefine.logger;

/**
 *  特正向量和类别均很稀疏的感知器分类器
 * 
 *  @author TylunasLi
 *
 */
public class SparseRestrictedPerceptronModel implements IMultipleClassifyModel, ICacheAble
{
    /**
     * 标签和id的相互转换
     */
    Map<String, Integer> tag2id;
    /**
     * id转标签
     */
    protected String[] id2tag;
    /**
     * 特征函数及权重集合
     */
    public ITrie<SparseFeatureFuncWeights> featureFunctionTrie;
    /**
     * 训练特征函数集合
     */
    public BinTrie<SparseFeatureFuncWeights> trainFeatureFunctions;
    /**
     * 当前执行的总步数
     */
    protected int step;

    public SparseRestrictedPerceptronModel()
    {
        featureFunctionTrie = new BinTrie<SparseFeatureFuncWeights>();
    }

    /**
     * 训练模型用的加载方式
     * @param id2tag
     * @param featureTemplateLists 
     */
    public SparseRestrictedPerceptronModel(String[] id2tag)
    {
        this.id2tag = id2tag;
        tag2id = new HashMap<String, Integer>();
        for (int i=0; i<id2tag.length; i++)
        {
            tag2id.put(id2tag[i], i);
        }
        trainFeatureFunctions = new BinTrie<SparseFeatureFuncWeights>();
        featureFunctionTrie = trainFeatureFunctions;
        step = 0;
    }
    
    /**
     * 添加单个实例进行训练
     * @param instance
     */
    public void train(InstanceVector instance, int[] probableTags)
    {
        PredictResult result = probableTags == null ? predict(instance) : predict(instance, probableTags);
        int tag = instance.getLabel();
        int predictedTag = result.getClassId();
        if (tag == predictedTag)
            return;
        for (SparseFeatureFuncWeights function : queryFeatureFuncionList(instance, true))
        {
            function.update(step, tag, 1);
            function.update(step, predictedTag, -1);
        }
        step++;
    }
    
    /**
     * 求每个函数的平均值
     */
    public void average()
    {
        TreeMap<String,SparseFeatureFuncWeights> map = new TreeMap<String, SparseFeatureFuncWeights>();
        Set<Map.Entry<String, SparseFeatureFuncWeights>> entrySet = trainFeatureFunctions.entrySet();
        for (Map.Entry<String, SparseFeatureFuncWeights> entry : entrySet)
        {
            entry.getValue().average(step);
            map.put(entry.getKey(), entry.getValue());
        }
        featureFunctionTrie.build(map);
    }

    public void build()
    {
        TreeMap<String,SparseFeatureFuncWeights> keyValueMap = new TreeMap<String,SparseFeatureFuncWeights>();
        Set<Map.Entry<String, SparseFeatureFuncWeights>> entrySet = trainFeatureFunctions.entrySet();
        for (Map.Entry<String, SparseFeatureFuncWeights> entry : entrySet)
        {
            keyValueMap.put(entry.getKey(), entry.getValue());
        }
        featureFunctionTrie.build(keyValueMap);
    }

    @Override
    public float[] predictConfidence(InstanceVector instance)
    {
        float[] scores = new float[id2tag.length];
        List<SparseFeatureFuncWeights> functionList = queryFeatureFuncionList(instance, false);
        for (SparseFeatureFuncWeights function : functionList)
        {
            for (int i=0; i<id2tag.length; i++)
            {
                scores[i] += function.weight(i);
            }
        }
        return scores;
    }

    /**
     * 维特比后向算法标注
     *
     * @param table
     */
    public PredictResult predict(InstanceVector instance) {
    	float[] scores = predictConfidence(instance);
    	float maxScore = -Float.MAX_VALUE;
    	int maxId = scores.length - 1;
    	for (int i=0; i<scores.length; i++) {
    		if (scores[i] > maxScore) {
    			maxScore = scores[i];
    			maxId = i;
    		}
    	}
    	PredictResult result = new PredictResult();
		result.setClassId(maxId);
    	result.setClassName(id2tag[maxId]);
    	result.setConfidence(Math.exp(maxScore));
    	return result;
    }
    
    public PredictResult predict(InstanceVector instance, int[] probableTags)
    {
        int[] scores = new int[probableTags.length];
        List<SparseFeatureFuncWeights> functionList = queryFeatureFuncionList(instance, false);
        for (SparseFeatureFuncWeights function : functionList)
        {
            for (int i=0; i<probableTags.length; i++)
            {
                scores[i] += function.weight(probableTags[i]);
            }
        }
        int maxScore = -Integer.MIN_VALUE;
        int maxId = scores.length - 1;
        for (int i=0; i<scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxId = i;
            }
        }
        PredictResult result = new PredictResult();
        result.setClassId(probableTags[maxId]);
        result.setClassName(id2tag[maxId]);
        result.setConfidence(Math.exp(maxScore));
        return result;
    }


    /**
     * 根据特征函数计算输出
     * @param table
     * @param current
     * @return
     */
    protected List<SparseFeatureFuncWeights> queryFeatureFuncionList(InstanceVector instance, boolean train)
    {
		Set<String> featureKeyList = instance.getFeatureKeys();
		if (PwNLP.Config.DEBUG) {
			logger.info(instance.getOriginalInfo() + "/" + instance.getLabel() + ": " + featureKeyList);
		}
        ArrayList<SparseFeatureFuncWeights> functionList = new ArrayList<SparseFeatureFuncWeights>(featureKeyList.size());
        for (String feature : featureKeyList)
        {
        	collectFeature(feature, train, functionList);
        }

        return functionList;
    }

	private void collectFeature(String feature, boolean train, List<SparseFeatureFuncWeights> functionList) {
		char[] chars = feature.toCharArray();
		SparseFeatureFuncWeights featureFunction = featureFunctionTrie.get(chars);
		if (featureFunction == null) 
		{
			if (train)
			{
				featureFunction = new SparseFeatureFuncWeights(chars);
				if (trainFeatureFunctions != null)
					trainFeatureFunctions.put(chars, featureFunction);
			}
			else
			{
				return;
			}
		}
		functionList.add(featureFunction);
	}

    /**
     * 给一系列特征函数结合tag打分
     *
     * @param scoreList
     * @param tag
     * @return
     */
    protected static int computeScore(List<SparseFeatureFuncWeights> functionList, int tag)
    {
        int score = 0;
        for (SparseFeatureFuncWeights function : functionList)
        {
            score += function.weight(tag);
        }
        return score;
    }

    @Override
    public void save(DataOutputStream out) throws Exception
    {
        out.writeInt(id2tag.length);
        for (String tag : id2tag)
        {
            out.writeUTF(tag);
        }
        SparseFeatureFuncWeights[] valueArray = featureFunctionTrie.getValueArray(new SparseFeatureFuncWeights[0]);
        out.writeInt(valueArray.length);
        for (SparseFeatureFuncWeights featureFunction : valueArray)
        {
            featureFunction.save(out);
        }
        featureFunctionTrie.save(out);
    }

    @Override
    public boolean load(ByteArray byteArray)
    {
        if (byteArray == null) return false;
        try
        {
            int size = byteArray.nextInt();
            id2tag = new String[size];
            for (int i = 0; i < id2tag.length; i++)
            {
                id2tag[i] = byteArray.nextUTF();
            }
            SparseFeatureFuncWeights[] valueArray = new SparseFeatureFuncWeights[byteArray.nextInt()];
            for (int i = 0; i < valueArray.length; i++)
            {
                valueArray[i] = new SparseFeatureFuncWeights();
                valueArray[i].load(byteArray);
            }
            featureFunctionTrie.load(byteArray, valueArray);
        }
        catch (Exception e)
        {
            logger.warning("缓存载入失败，可能是由于版本变迁带来的不兼容。具体异常是：\n" + TextUtility.exceptionToString(e));
            return false;
        }
        onLoadFinished();
        return true;
    }
    
    public void onLoadFinished() {
    }
//
//    public void debug()
//    {
//    	logger.debug("feature size:" + trainFeatureFunctions.size());
//    	logger.debug("update instance:" + count);
//        for (int[] line : matrix)
//        {
//        	logger.debug(Arrays.toString(line));
//        }
//        count = 0;
//    }
}
