/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/05/2014/5/26 13:52</create-date>
 *
 * <copyright file="UnknowWord.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.recognition.nr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.algorithm.Viterbi;
import org.pwstudio.nlp.corpus.dictionary.item.EnumItem;
import org.pwstudio.nlp.corpus.tag.NR;
import org.pwstudio.nlp.dictionary.nr.PersonDictionary;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.utility.Predefine;

/**
 * 人名识别
 * @author hankcs
 */
public class PersonRecognition
{
    public static boolean Recognition(List<Vertex> pWordSegResult, WordNet wordNetOptimum, WordNet wordNetAll)
    {
        List<EnumItem<NR>> roleTagList = roleObserve(pWordSegResult);
        if (PwNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            for (EnumItem<NR> nrEnumItem : roleTagList)
            {
                sbLog.append('[');
                sbLog.append(iterator.next().realWord);
                sbLog.append(' ');
                sbLog.append(nrEnumItem);
                sbLog.append(']');
            }
            System.out.printf("人名角色观察：%s\n", sbLog.toString());
        }
        List<NR> nrList = viterbiComputeSimply(roleTagList);
        if (PwNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            sbLog.append('[');
            for (NR nr : nrList)
            {
                sbLog.append(iterator.next().realWord);
                sbLog.append('/');
                sbLog.append(nr);
                sbLog.append(" ,");
            }
            if (sbLog.length() > 1) sbLog.delete(sbLog.length() - 2, sbLog.length());
            sbLog.append(']');
            System.out.printf("人名角色标注：%s\n", sbLog.toString());
        }

        PersonDictionary.parsePattern(nrList, pWordSegResult, wordNetOptimum, wordNetAll);
        return true;
    }

    /**
     * 角色观察(从模型中加载所有词语对应的所有角色,允许进行一些规则补充)
     * @param wordSegResult 粗分结果
     * @return
     */
    public static List<EnumItem<NR>> roleObserve(List<Vertex> wordSegResult)
    {
        List<EnumItem<NR>> tagList = new LinkedList<EnumItem<NR>>();
        Iterator<Vertex> iterator = wordSegResult.iterator();
        while (iterator.hasNext())
        {
            Vertex vertex = iterator.next();
            EnumItem<NR> nrEnumItem = PersonDictionary.dictionary.get(vertex.realWord);
            if (nrEnumItem == null)
            {

                switch (vertex.guessNature())
                {
                    case begin:
                    {
                        nrEnumItem = PersonDictionary.dictionary.get(vertex.word);
                        if (nrEnumItem == null)
                            nrEnumItem = new EnumItem<NR>(NR.S);
                    }break;
                    case nr:
                    {
                        // 有些双名实际上可以构成更长的三名
                        if (vertex.getAttribute().totalFrequency <= 1000 && vertex.realWord.length() == 2)
                        {
                            nrEnumItem = new EnumItem<NR>();
                            nrEnumItem.labelMap.put(NR.X, 2); // 认为是三字人名前2个字=双字人名的可能性更高
                            nrEnumItem.labelMap.put(NR.G, 1);
                        }
                        else nrEnumItem = new EnumItem<NR>(NR.Y, PersonDictionary.transformMatrixDictionary.getTotalFrequency(NR.A));
                    }break;
                    case nnt:
                    {
                        // 姓+职位
                        nrEnumItem = new EnumItem<NR>(NR.G, NR.K);
                    }break;
                    case end:
                    {
                        nrEnumItem = PersonDictionary.dictionary.get(vertex.word);
                        if (nrEnumItem == null)
                        {
                            nrEnumItem = new EnumItem<NR>(NR.A, Predefine.MAX_FREQUENCY / 10);
                            nrEnumItem.addLabel(NR.L, 1000);
                        }
                    }break;
                    default:
                    {
                        nrEnumItem = new EnumItem<NR>(NR.A, vertex.attribute.totalFrequency);
                    }break;
                }
            }
            tagList.add(nrEnumItem);
        }
        return tagList;
    }

    /**
     * 维特比算法求解最优标签
     * @param roleTagList
     * @return
     */
    public static List<NR> viterbiCompute(List<EnumItem<NR>> roleTagList)
    {
        return Viterbi.computeEnum(roleTagList, PersonDictionary.transformMatrixDictionary);
    }

    /**
     * 简化的"维特比算法"求解最优标签
     * @param roleTagList
     * @return
     */
    public static List<NR> viterbiComputeSimply(List<EnumItem<NR>> roleTagList)
    {
        return Viterbi.computeEnumSimply(roleTagList, PersonDictionary.transformMatrixDictionary);
    }
}
