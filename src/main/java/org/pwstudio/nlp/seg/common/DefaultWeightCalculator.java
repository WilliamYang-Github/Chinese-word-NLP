package org.pwstudio.nlp.seg.common;

import static org.pwstudio.nlp.utility.Predefine.dTemp;
import static org.pwstudio.nlp.utility.Predefine.logger;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.dictionary.CoreBiGramTableDictionary;
import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.utility.Predefine;

public class DefaultWeightCalculator extends WeightCalculator {

	@Override
	public double calculateWeight(Vertex from, Vertex to)
	{
        int frequency = Math.max(from.getAttribute().totalFrequency, 
        		CoreDictionary.getTermFrequency(from.word));
        if (frequency == 0)
        {
            frequency = 1;  // 防止发生除零错误
        }
        int toFrequency = Math.max(to.getAttribute().totalFrequency, 
        		CoreDictionary.getTermFrequency(to.word));
        int nTwoWordsFreq = CoreBiGramTableDictionary.getBiFrequency(from.wordID, to.wordID);
//        if (nTwoWordsFreq > frequency)
//        	nTwoWordsFreq = frequency;
        double value = -Math.log(Predefine.dSmoothingPara * toFrequency / (Predefine.MAX_FREQUENCY)
        		+ (1 - Predefine.dSmoothingPara) * ((1 - dTemp) * nTwoWordsFreq / frequency + dTemp));
        //value = value + from.selfWeight;
        if (value < 0.0)
        {
            value = -value;
        }
        if (PwNLP.Config.DEBUG)
        	logger.info(String.format("%5s frequency:%6d, %s nTwoWordsFreq:%3d, weight:%.2f", from.word, 
        			frequency, from.word + "@" + to.word, nTwoWordsFreq, value));
        return value;
	}

	@Override
	public double maxWeight() {
		return -Math.log(Predefine.dSmoothingPara * 1.0 / Predefine.MAX_FREQUENCY);
	}

}
