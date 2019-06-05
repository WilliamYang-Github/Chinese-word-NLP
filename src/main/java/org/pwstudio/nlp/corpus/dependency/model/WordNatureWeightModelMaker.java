/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/20 12:27</create-date>
 *
 * <copyright file="WordNatureWeightScorer.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.dependency.model;

import java.util.Set;
import java.util.TreeSet;

import org.pwstudio.nlp.corpus.dependency.CoNll.CoNLLLoader;
import org.pwstudio.nlp.corpus.dependency.CoNll.CoNLLSentence;
import org.pwstudio.nlp.corpus.dependency.CoNll.CoNLLWord;
import org.pwstudio.nlp.corpus.dictionary.DictionaryMaker;
import org.pwstudio.nlp.corpus.document.sentence.word.Word;
import org.pwstudio.nlp.corpus.io.IOUtil;

/**
 * 生成模型打分器模型构建工具
 *
 * @author hankcs
 */
public class WordNatureWeightModelMaker
{
    public static boolean makeModel(String corpusLoadPath, String modelSavePath)
    {
        Set<String> posSet = new TreeSet<String>();
        DictionaryMaker dictionaryMaker = new DictionaryMaker();
        for (CoNLLSentence sentence : CoNLLLoader.loadSentenceList(corpusLoadPath))
        {
            for (CoNLLWord word : sentence.word)
            {
                addPair(word.NAME, word.HEAD.NAME, word.DEPREL, dictionaryMaker);
                addPair(word.NAME, wrapTag(word.HEAD.POSTAG ), word.DEPREL, dictionaryMaker);
                addPair(wrapTag(word.POSTAG), word.HEAD.NAME, word.DEPREL, dictionaryMaker);
                addPair(wrapTag(word.POSTAG), wrapTag(word.HEAD.POSTAG), word.DEPREL, dictionaryMaker);
                posSet.add(word.POSTAG);
            }
        }
        for (CoNLLSentence sentence : CoNLLLoader.loadSentenceList(corpusLoadPath))
        {
            for (CoNLLWord word : sentence.word)
            {
                addPair(word.NAME, word.HEAD.NAME, word.DEPREL, dictionaryMaker);
                addPair(word.NAME, wrapTag(word.HEAD.POSTAG ), word.DEPREL, dictionaryMaker);
                addPair(wrapTag(word.POSTAG), word.HEAD.NAME, word.DEPREL, dictionaryMaker);
                addPair(wrapTag(word.POSTAG), wrapTag(word.HEAD.POSTAG), word.DEPREL, dictionaryMaker);
                posSet.add(word.POSTAG);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String pos : posSet)
        {
            sb.append("case \"" + pos + "\":\n");
        }
        IOUtil.saveTxt("data/model/dependency/pos-thu.txt", sb.toString());
        return dictionaryMaker.saveTxtTo(modelSavePath);
    }

    private static void addPair(String from, String to, String label, DictionaryMaker dictionaryMaker)
    {
        dictionaryMaker.add(new Word(from + "@" + to, label));
        dictionaryMaker.add(new Word(from + "@", "频次"));
    }

    /**
     * 用尖括号将标签包起来
     * @param tag
     * @return
     */
    public static String wrapTag(String tag)
    {
        return "<" + tag + ">";
    }
}
