package org.pwstudio.nlp.recognition.nt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.algorithm.Viterbi;
import org.pwstudio.nlp.corpus.dictionary.item.EnumItem;
import org.pwstudio.nlp.corpus.tag.NT;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.nt.NTDictionary;
import org.pwstudio.nlp.dictionary.nt.OrganizationDictionary;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;

/**
 * 组织机构名称识别
 * @author hankcs
 */
public class PMMOrgRecognition
{
    
	public static PMMOrgRecognition instance = new PMMOrgRecognition();
	
	/**
	 * 上一词语和下一标签的关联关系
	 */
	private static NTDictionary lastTagDict;
	
    public static boolean recognize(List<Vertex> pWordSegResult, WordNet wordNetOptimum, WordNet wordNetAll)
    {
        List<EnumItem<NT>> roleTagList = roleTag(pWordSegResult, wordNetAll);
        List<EnumItem<NT>> nextRoleTagList = roleMextTag(pWordSegResult, wordNetAll);
        if (PwNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            for (EnumItem<NT> NTEnumItem : roleTagList)
            {
                sbLog.append('[');
                sbLog.append(iterator.next().realWord);
                sbLog.append(' ');
                sbLog.append(NTEnumItem);
                sbLog.append(']');
            }
            System.out.printf("机构名角色观察：%s\n", sbLog.toString());
        }
        List<NT> NTList = viterbiExCompute(roleTagList, nextRoleTagList);
        if (PwNLP.Config.DEBUG)
        {
            StringBuilder sbLog = new StringBuilder();
            Iterator<Vertex> iterator = pWordSegResult.iterator();
            sbLog.append('[');
            for (NT NT : NTList)
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

        OrganizationDictionary.parsePattern(NTList, pWordSegResult, wordNetOptimum, wordNetAll);
        return true;
    }

    public static List<EnumItem<NT>> roleTag(List<Vertex> vertexList, WordNet wordNetAll)
    {
        List<EnumItem<NT>> tagList = new LinkedList<EnumItem<NT>>();
        //        int line = 0;
        for (Vertex vertex : vertexList)
        {
            // 构成更长的
            Nature nature = vertex.guessNature();
            switch (nature)
            {
                case nrf:
                {
                    if (vertex.getAttribute().totalFrequency <= 1000)
                    {
                        tagList.add(new EnumItem<NT>(NT.F, 1000));
                    }
                    else break;
                }
                continue;
                case ni:
                case nic:
                case nis:
                case nit:
                {
                    EnumItem<NT> ntEnumItem = new EnumItem<NT>(NT.K, 1000);
                    ntEnumItem.addLabel(NT.D, 1000);
                    tagList.add(ntEnumItem);
                }
                continue;
                case m:
                {
                    EnumItem<NT> ntEnumItem = new EnumItem<NT>(NT.M, 1000);
                    tagList.add(ntEnumItem);
                }
                continue;
            }

            EnumItem<NT> NTEnumItem = OrganizationDictionary.dictionary.get(vertex.word);  // 此处用等效词，更加精准
            if (NTEnumItem == null)
            {
                NTEnumItem = new EnumItem<NT>(NT.Z, vertex.attribute.totalFrequency);
            }
            tagList.add(NTEnumItem);
//            line += vertex.realWord.length();
        }
        return tagList;
    }

    public static List<EnumItem<NT>> roleMextTag(List<Vertex> vertexList, WordNet wordNetAll)
    {
        List<EnumItem<NT>> tagList = new LinkedList<EnumItem<NT>>();
        for (Vertex vertex : vertexList)
        {
            EnumItem<NT> NTEnumItem = lastTagDict.get(vertex.word);  // 此处用等效词，更加精准
            if (NTEnumItem == null)
            {
                NTEnumItem = new EnumItem<NT>(NT.Z, vertex.attribute.totalFrequency);
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
    public static List<NT> viterbiExCompute(List<EnumItem<NT>> roleTagList, final List<EnumItem<NT>> nextRoleTagList)
    {
        return Viterbi.computeForEnum(roleTagList, new Viterbi.HMMWeightCalculator<NT>(OrganizationDictionary.transformMatrixDictionary) {
            @Override
            public double calculateWeight(double last, int curIdx, EnumItem<NT> item, NT prevtag, NT curTag)
            {
                if (curIdx == 0)
                    return -Math.log((item.getFrequency(curTag) + 1e-8) / transformMatrixDictionary.getTotalFrequency(curTag));
                double step = transformMatrixDictionary.transititon_probability[prevtag.ordinal()][curTag.ordinal()];
                step = step + -Math.log(((double) item.getFrequency(curTag)) / item.getTotalFrequency());
                EnumItem<NT> w_1_item = nextRoleTagList.get(curIdx);
                step = step - -Math.log((w_1_item.getFrequency(curTag) + 1e-8) / w_1_item.getTotalFrequency());
                return last + step;
            }
        });
    }
	
}
