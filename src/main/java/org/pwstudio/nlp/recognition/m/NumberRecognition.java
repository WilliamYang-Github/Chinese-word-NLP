package org.pwstudio.nlp.recognition.m;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.corpus.tag.M;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.m.NumberPatternDictionary;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.utility.TextUtility;

public class NumberRecognition
{
    protected static final String chineseUnitSmall="十百千拾佰仟";
    
    protected static final String chineseUnit="亿万十百千拾佰仟";
    
    /**
     * 复杂数词识别
     * @param pWordSegResult
     * @param wordNetOptimum
     * @param wordNetAll
     * @return
     */
    public static boolean recognize(final List<Vertex> pWordSegResult,
            final WordNet wordNetOptimum, final WordNet wordNetAll)
    {
        final List<M> MTagList = roleTag(pWordSegResult);
        if (PwNLP.Config.DEBUG)
        {
            final StringBuilder sbLog = new StringBuilder();
            final Iterator<Vertex> iterator = pWordSegResult.iterator();
            sbLog.append('[');
            for (M tag : MTagList)
            {
                sbLog.append(iterator.next().realWord);
                sbLog.append('/').append(tag).append(" ,");
            }
            if (sbLog.length() > 1)
                sbLog.delete(sbLog.length() - 2, sbLog.length());
            sbLog.append(']');
            System.out.printf("数量词角色标注：%s\n", sbLog.toString());
        }
        NumberPatternDictionary.parsePattern(MTagList, pWordSegResult, 
                wordNetOptimum, wordNetAll);
        return true;
    }

    public static List<M> roleTag(final List<Vertex> vertexList)
    {
        final List<M> tagList = new LinkedList<M>();
        for (final Vertex vertex : vertexList)
        {
            M tag = M.Z;
            M basetag = NumberPatternDictionary.wordDict.get(vertex.realWord);
            if (basetag != null)
                tag = basetag;
            else if (vertex.realWord.length() == 1 && chineseUnit.contains(vertex.realWord))
                tag = M.L;
            else if (TextUtility.isAllChineseNum(vertex.realWord))
            {
                tag = M.M;
            }
            else if (TextUtility.isAllNum(vertex.realWord))
            {
                tag = M.A;
            }
            else if (vertex.hasNature(Nature.m))
            {
                tag = M.M;
            }
            else
            {
                tag = NumberPatternDictionary.wordDict.get(vertex.realWord);
            }
            if (tag == null)
            {
                tag = M.Z;
            }
            tagList.add(tag);
        }
        return tagList;
    }

}
