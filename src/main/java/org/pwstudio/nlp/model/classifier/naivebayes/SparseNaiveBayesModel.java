package org.pwstudio.nlp.model.classifier.naivebayes;

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
 * 稀疏特征的朴素贝叶斯模型
 *
 * @author iyunwen-李辰刚
 */
public class SparseNaiveBayesModel implements IMultipleClassifyModel, ICacheAble
{
	
    /**
     * 标签和id的相互转换
     */
    public Map<String, Integer> tag2id;
    /**
     * id转标签
     */
    protected String[] id2tag;
    /**
     * 特征函数及权重集合
     */
    ITrie<FeatureFunctionWeights> featureFunctionTrie;
    /**
     * 训练特征函数集合
     */
    BinTrie<FeatureFunctionWeights> trainFeatureFunctions;
    /**
     * 先验概率
     */
    protected int[] categoryCount;
    /**
     * 先验概率
     */
    protected float[] priorProbablity;
    /**
     * 当前执行的总步数
     */
    protected int totalCount;

    protected int labelSize;

    public SparseNaiveBayesModel()
    {
        featureFunctionTrie = new BinTrie<FeatureFunctionWeights>();
    }

    /**
     * 训练模型用的加载方式
     * @param id2tag
     * @param featureTemplateLists 
     */
    public SparseNaiveBayesModel(String[] id2tag)
    {
        this.id2tag = id2tag;
        tag2id = new HashMap<String, Integer>(id2tag.length);
        for (int i = 0; i < id2tag.length; i++)
        {
            tag2id.put(id2tag[i], i);
        }
        this.priorProbablity = new float[id2tag.length];
        this.categoryCount = new int[id2tag.length];
        trainFeatureFunctions = new BinTrie<FeatureFunctionWeights>();
        featureFunctionTrie = trainFeatureFunctions;
        totalCount = 0;
    }

    /**
     * 以指定的trie树结构储存内部特征函数
     * @param featureFunctionTrie
     */
    public SparseNaiveBayesModel(ITrie<FeatureFunctionWeights> featureFunctionTrie)
    {
        this.featureFunctionTrie = featureFunctionTrie;
    }

    protected void onLoadTxtFinished() {
        // do nothing
    }
    
    /**
     * 添加单个实例进行训练
     * @param instance
     */
    public void train(InstanceVector record) {
    	int category = record.getLabel();
    	categoryCount[category]++;
        List<FeatureFunctionWeights> weights = queryFeatureFuncionList(record, true);
        for (FeatureFunctionWeights weight : weights)
        {
            weight.update(totalCount, category, 1);
        }
        totalCount++;
    }

    /**
     * 直接保存
     */
    public void build() {
        TreeMap<String,FeatureFunctionWeights> keyValueMap = new TreeMap<String,FeatureFunctionWeights>();
        Set<Map.Entry<String, FeatureFunctionWeights>> entrySet = trainFeatureFunctions.entrySet();
        for (Map.Entry<String, FeatureFunctionWeights> entry : entrySet)
        {
        	entry.getValue().build(categoryCount, featureFunctionTrie.size());
            keyValueMap.put(entry.getKey(), entry.getValue());
        }
        featureFunctionTrie.build(keyValueMap);
        for (int category=0; category<priorProbablity.length; category++)
        {
        	priorProbablity[category] = (float) Math.log((double) categoryCount[category] / totalCount);
        }

    }

	@Override
	public float[] predictConfidence(InstanceVector record) {

        float[] predictionScores = new float[id2tag.length];
        for (int category=0; category<id2tag.length; category++)
        {
            float logprob = 0.0F*priorProbablity[category]; //用类目的对数似然初始化概率

            //对文档中的每个特征
            List<FeatureFunctionWeights> weights = queryFeatureFuncionList(record, false);
            for (FeatureFunctionWeights weight : weights)
            {
//                int occurrences = 0; //获取其在文档中的频次
            	// 使用伯努利分布
            	// 将对数似然乘上频次
                logprob += weight.weight(category);
            }
            predictionScores[category] = logprob;
        }
        //计算分母概率

        return predictionScores;
	}

    /**
     * 维特比后向算法标注
     *
     * @param table
     */
    public PredictResult predict(InstanceVector record) {
    	float[] probs = predictConfidence(record);
    	float maxProb = -Float.MAX_VALUE;
    	int maxId = probs.length - 1;
    	for (int i=0; i<probs.length; i++) {
    		if (probs[i] > maxProb) {
    			maxProb = probs[i];
    			maxId = i;
    		}
    	}
    	PredictResult result = new PredictResult();
		result.setClassId(maxId);
    	result.setClassName(id2tag[maxId]);
    	result.setConfidence(Math.exp(maxProb));
    	return result;
    }

    /**
     * 根据特征函数计算输出
     * @param table
     * @param current
     * @return
     */
    protected List<FeatureFunctionWeights> queryFeatureFuncionList(InstanceVector instance, boolean train)
    {
		Set<String> featureKeyList = instance.getFeatureKeys();
		if (PwNLP.Config.DEBUG) {
			logger.info(instance.getOriginalInfo() + "/" + instance.getLabel() + ": " + featureKeyList);
		}
        ArrayList<FeatureFunctionWeights> functionList = new ArrayList<FeatureFunctionWeights>(featureKeyList.size());
        for (String feature : featureKeyList)
        {
        	collectFeature(feature, train, functionList);
        }

        return functionList;
    }

	private void collectFeature(String feature, boolean train, List<FeatureFunctionWeights> functionList) {
		char[] chars = feature.toCharArray();
		FeatureFunctionWeights featureFunction = featureFunctionTrie.get(chars);
		if (featureFunction == null) 
		{
			if (train)
			{
				featureFunction = new FeatureFunctionWeights(chars, id2tag.length);
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
    protected static int computeScore(List<FeatureFunctionWeights> functionList, int tag)
    {
        int score = 0;
        for (FeatureFunctionWeights function : functionList)
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
        FeatureFunctionWeights[] valueArray = featureFunctionTrie.getValueArray(new FeatureFunctionWeights[0]);
        out.writeInt(valueArray.length);
        for (FeatureFunctionWeights featureFunction : valueArray)
        {
            featureFunction.save(out);
        }
        featureFunctionTrie.save(out);
        if (categoryCount != null)
        {
            out.writeInt(categoryCount.length);
            for (int i = 0; i < categoryCount.length; i++)
            {
                out.writeInt(categoryCount[i]);
            }
        }
        else
        {
            out.writeInt(0);
        }
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
            FeatureFunctionWeights[] valueArray = new FeatureFunctionWeights[byteArray.nextInt()];
            for (int i = 0; i < valueArray.length; i++)
            {
                valueArray[i] = new FeatureFunctionWeights();
                valueArray[i].load(byteArray);
            }
            featureFunctionTrie.load(byteArray, valueArray);
            size = byteArray.nextInt();
            if (size == 0)
            {
                priorProbablity = new float[id2tag.length];
                for (int i = 0; i < size; i++)
                {
                    priorProbablity[i] = 1.0F/id2tag.length;
                }
                return true;
            }
            categoryCount = new int[size];
            for (int i = 0; i < size; i++)
            {
            	categoryCount[i] = byteArray.nextInt();
            }
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
