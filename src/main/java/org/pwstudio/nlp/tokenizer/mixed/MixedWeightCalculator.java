package org.pwstudio.nlp.tokenizer.mixed;

import static org.pwstudio.nlp.utility.Predefine.dTemp;
import static org.pwstudio.nlp.utility.Predefine.logger;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.CoreBiGramTableDictionary;
import org.pwstudio.nlp.dictionary.CoreDictionaryTransformMatrixDictionary;
import org.pwstudio.nlp.dictionary.TransformMatrixDictionary;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WeightCalculator;
import org.pwstudio.nlp.utility.Predefine;

public class MixedWeightCalculator extends WeightCalculator {

	public double calculateWeight(Vertex from, Vertex to)
	{
        int frequency = from.getAttribute().totalFrequency;
        if (frequency == 0)
        {
            frequency = 1;  // 防止发生除零错误
        }
        int toFrequency = to.getAttribute().totalFrequency;
//        if (toFrequency == 0)
//        {
//            frequency = 1;  // 防止发生除零错误
//        }
        int nTwoWordsFreq = CoreBiGramTableDictionary.getBiFrequency(from.wordID, to.wordID);
        if (nTwoWordsFreq > frequency)
        	nTwoWordsFreq = frequency;
        double value = -Math.log(Predefine.dSmoothingPara * toFrequency / (Predefine.MAX_FREQUENCY)
        		+ (1 - Predefine.dSmoothingPara) * ((1 - dTemp) * nTwoWordsFreq / frequency + dTemp));
        if (value < 0.0)
        {
            value = -value;
        }
        if (PwNLP.Config.DEBUG)
        	logger.info(String.format("%5s frequency:%6d, %s nTwoWordsFreq:%3d, weight:%.2f", from.word, 
        			frequency, from.word + "@" + to.word, nTwoWordsFreq, value));
        double value2 = probablity(from, to);
        return value;
	}
	
    /**
     * 计算序列的概率`
     * @param roleTagList
     * @param transformMatrixDictionary
     * @return
     */
    public static double probablity(Vertex from, Vertex to) {
    	TransformMatrixDictionary<Nature> matrix = 
    			CoreDictionaryTransformMatrixDictionary.transformMatrixDictionary;
    	double totalProb = 0.0;
    	double[] toEmitProb = new double[to.attribute.nature.length];
    	for (int j=0; j<to.attribute.nature.length; j++) {
    		int toTotal = matrix.getTotalFrequency(to.attribute.nature[j]);
    		toEmitProb[j] = (double) (to.attribute.frequency[j] + 1e-8) / toTotal;
    	}
    	for (int i=0; i<from.attribute.nature.length; i++) {
    		Nature fromNature = from.attribute.nature[i];
    		int fromTotal = matrix.getTotalFrequency(fromNature);
    		double fromEmitProb = Math.log((from.attribute.frequency[i] + 1e-8) / fromTotal);
        	for (int j=0; j<to.attribute.nature.length; j++) {
        		totalProb += fromEmitProb * toEmitProb[j] * (
        				matrix.getFrequency(fromNature, to.attribute.nature[j]) + 1e-8) / matrix.getTotalFrequency(fromNature);
        	}
    	}
    	return -Math.log(totalProb) -Math.log(Nature.values().length / Predefine.MAX_FREQUENCY);
    }

	@Override
	public double maxWeight() {
		return -Math.log(Predefine.dSmoothingPara * 1.0 / Predefine.MAX_FREQUENCY);
	}

}
