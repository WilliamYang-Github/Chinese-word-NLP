package org.pwstudio.nlp.recognition.nt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.algorithm.Viterbi;
import org.pwstudio.nlp.corpus.dictionary.item.EnumItem;
import org.pwstudio.nlp.corpus.tag.NTC;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.nt.CompanyNameDictionary;
import org.pwstudio.nlp.dictionary.nt.NTCDictionary;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;

/**
 * 组织机构名称识别
 * @author hankcs
 */
public class PMMCompanyRecognition
{
    
	public static PMMCompanyRecognition instance = new PMMCompanyRecognition();
	
	/**
	 * 上一词语和下一标签的关联关系
	 */
	private static NTCDictionary lastTagDict;
	
    public static boolean recognize(List<Vertex> pWordSegResult, WordNet wordNetOptimum, WordNet wordNetAll)
    {
        List<EnumItem<NTC>> roleTagList = roleTag(pWordSegResult, wordNetAll);
        List<EnumItem<NTC>> nextRoleTagList = roleMextTag(pWordSegResult, wordNetAll);
        if (PwNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            for (EnumItem<NTC> NTEnumItem : roleTagList)
            {
                sbLog.append('[');
                sbLog.append(iterator.next().realWord);
                sbLog.append(' ');
                sbLog.append(NTEnumItem);
                sbLog.append(']');
            }
            System.out.printf("机构名角色观察：%s\n", sbLog.toString());
        }
        List<NTC> NTList = viterbiExCompute(roleTagList, nextRoleTagList);
        if (PwNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            sbLog.append('[');
            for (NTC NT : NTList)
            {
                sbLog.append(iterator.next().realWord);
                sbLog.append('/');
                sbLog.append(NT);
                sbLog.append(" ,");
            }
            if (sbLog.length() > 1) sbLog.delete(sbLog.length() - 2, sbLog.length());
            sbLog.append(']');
            System.out.printf("机构名角色标注：%s\n", sbLog.toString());
        }

        CompanyNameDictionary.parsePattern(NTList, pWordSegResult, wordNetOptimum, wordNetAll);
        return true;
    }

    public static List<EnumItem<NTC>> roleTag(List<Vertex> vertexList, WordNet wordNetAll)
    {
        List<EnumItem<NTC>> tagList = new LinkedList<EnumItem<NTC>>();
        //        int line = 0;
        for (Vertex vertex : vertexList)
        {
            // 构成更长的
            Nature nature = vertex.guessNature();
            switch (nature)
            {
                case ns:
                case nsf:
                {
                    if (vertex.getAttribute().totalFrequency <= 1000)
                    {
                        tagList.add(new EnumItem<NTC>(NTC.F, 1000));
                    }
                    else break;
                }
                continue;
            }

            EnumItem<NTC> NTEnumItem = CompanyNameDictionary.dictionary.get(vertex.word);  // 此处用等效词，更加精准
            if (NTEnumItem == null)
            {
                NTEnumItem = new EnumItem<NTC>(NTC.Z, vertex.attribute.totalFrequency);
            }
            tagList.add(NTEnumItem);
//            line += vertex.realWord.length();
        }
        return tagList;
    }

    public static List<EnumItem<NTC>> roleMextTag(List<Vertex> vertexList, WordNet wordNetAll)
    {
        List<EnumItem<NTC>> tagList = new LinkedList<EnumItem<NTC>>();
        for (Vertex vertex : vertexList)
        {
            EnumItem<NTC> NTEnumItem = lastTagDict.get(vertex.word);  // 此处用等效词，更加精准
            if (NTEnumItem == null)
            {
                NTEnumItem = new EnumItem<NTC>(NTC.Z, vertex.attribute.totalFrequency);
            }
            tagList.add(NTEnumItem);
        }
        return tagList;
    }

    /**
     * 维特比算法求解最优标签
     *
     * @param roleTagList
     * @return
     */
    public static List<NTC> viterbiExCompute(List<EnumItem<NTC>> roleTagList, final List<EnumItem<NTC>> nextRoleTagList)
    {
        return Viterbi.computeForEnum(roleTagList, new Viterbi.HMMWeightCalculator<NTC>(CompanyNameDictionary.transformMatrixDictionary){
            @Override
            public double calculateWeight(double last, int curIdx, EnumItem<NTC> item, NTC prevtag, NTC curTag)
            {
                if (curIdx == 0)
                    return -Math.log((item.getFrequency(curTag) + 1e-8) / transformMatrixDictionary.getTotalFrequency(curTag));
                double step = transformMatrixDictionary.transititon_probability[prevtag.ordinal()][curTag.ordinal()];
                step = step + Math.log(item.getFrequency(curTag) / item.getTotalFrequency());
                EnumItem<NTC> w_1_item = nextRoleTagList.get(curIdx);
                step = step - Math.log(w_1_item.getFrequency(curTag) / w_1_item.getTotalFrequency());
                return last + step;
            }
            
        });
    }
	
}
