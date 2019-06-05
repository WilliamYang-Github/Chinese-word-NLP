package org.pwstudio.nlp.dictionary;

import org.pwstudio.nlp.collection.trie.DoubleArrayTrie;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.CoreDictionary.Attribute;
import org.pwstudio.nlp.dictionary.base.AttributeDictionary;
import org.pwstudio.nlp.dictionary.base.BiGramTableDictionary;
import org.pwstudio.nlp.utility.Predefine;

/**
 * 词典套件，也可以叫语言模型<br>
 * 包含：  1）词典
 *         2）二元接续信息 （若有）
 *         3）词-词性/词性-词接续信息（若有）
 *         4）词性转移矩阵 （若有）
 *         5）以上模型使用的参数
 * @author iyunwen-李辰刚
 */
public class LanguageModel
{

    public AttributeDictionary unigram;
    
    public BiGramTableDictionary bigram;
    
    public TransformMatrixDictionary<Nature> tagMatrix;

    // 一些特殊的WORD_ID
    public int NR_WORD_ID;
    public int NS_WORD_ID;
    public int NT_WORD_ID;
    public int T_WORD_ID;
    public int X_WORD_ID;
    public int M_WORD_ID;
    public int NX_WORD_ID;
    public int NW_WORD_ID;
    public int MQ_WORD_ID;
    
    protected static LanguageModel defaultModel = null;
    
    LanguageModel(AttributeDictionary unigramDict, BiGramTableDictionary bigram)
    {
        this.unigram = unigramDict;
        this.bigram = bigram;
        NR_WORD_ID = unigram.getWordID(Predefine.TAG_PEOPLE);
        NS_WORD_ID = unigram.getWordID(Predefine.TAG_PLACE);
        NT_WORD_ID = unigram.getWordID(Predefine.TAG_GROUP);
        NX_WORD_ID = unigram.getWordID(Predefine.TAG_OTHER);
        NW_WORD_ID = unigram.getWordID(Predefine.TAG_NEW);
        T_WORD_ID = unigram.getWordID(Predefine.TAG_TIME);
        M_WORD_ID = unigram.getWordID(Predefine.TAG_NUMBER);
        MQ_WORD_ID = unigram.getWordID(Predefine.TAG_QUANTIFIER);
        X_WORD_ID = unigram.getWordID(Predefine.TAG_CLUSTER);
    }
    
    public static LanguageModel create(AttributeDictionary unigramDict, 
            BiGramTableDictionary bigram) {
        return new LanguageModel(unigramDict, bigram);
    }

    public DoubleArrayTrie<Attribute>.Searcher getSearcher(char[] charArray, int i) {
        return unigram.getTrie().getSearcher(charArray, i);
    }

    public static LanguageModel defaultDictioanry() {
        if (defaultModel == null) {
            AttributeDictionary coreAttributeDictionary = new AttributeDictionary(
                    CoreDictionary.trie, Predefine.MAX_FREQUENCY);
            defaultModel=LanguageModel.create(coreAttributeDictionary, 
                    new BiGramTableDictionary(coreAttributeDictionary, CoreBiGramTableDictionary.start, 
                            CoreBiGramTableDictionary.pair));
        }
        return defaultModel;
    }

    public int getWordID(String word)
    {
        return unigram.getWordID(word);
    }

    public Attribute get(int id)
    {
        return unigram.get(id);
    }

    public Attribute getTermAttribute(String word)
    {
        return unigram.get(word);
    }

    public double getTotalUnigramFrequency()
    {
        return unigram.getTotalFrequency();
    }

    public boolean contains(String key)
    {
        return unigram.contains(key);
    }

    public int getTermFrequency(String term)
    {
        return unigram.getTermFrequency(term);
    }

    public int count()
    {
        return unigram.count();
    }

    public int getBiFrequency(String a, String b)
    {
        return bigram.getBiFrequency(a, b);
    }

    public int getBiFrequency(int idA, int idB)
    {
        return bigram.getBiFrequency(idA, idB);
    }

}
