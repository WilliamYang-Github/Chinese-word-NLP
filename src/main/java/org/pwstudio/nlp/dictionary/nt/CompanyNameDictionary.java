/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 14:47</create-date>
 *
 * <copyright file="PersonDictionary.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.dictionary.nt;

import java.util.List;
import java.util.TreeMap;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import org.pwstudio.nlp.corpus.dictionary.item.EnumItem;
import org.pwstudio.nlp.corpus.tag.NTC;
import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.dictionary.TransformMatrixDictionary;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.utility.Predefine;

import static org.pwstudio.nlp.utility.Predefine.logger;

/**
 * 机构名识别用的词典，实际上是对两个词典的包装
 * (Copy Of OrganizationDictionary)
 * @author TylunasLi
 */
public class CompanyNameDictionary
{
    /**
     * 机构名词典
     */
    public static NTCDictionary dictionary;
    /**
     * 转移矩阵词典
     */
    public static TransformMatrixDictionary<NTC> transformMatrixDictionary;
    /**
     * AC算法用到的Trie树
     */
    public static AhoCorasickDoubleArrayTrie<NTCPattern> trie;
    /**
     * 本词典专注的词的ID
     */
    static final int WORD_ID = CoreDictionary.getWordID(Predefine.TAG_GROUP);
    /**
     * 本词典专注的词的属性
     */
    static final CoreDictionary.Attribute ATTRIBUTE = CoreDictionary.get(WORD_ID);

    private static void addKeyword(TreeMap<String, String> patternMap, String keyword)
    {
        patternMap.put(keyword, keyword);
    }
    static
    {
        long start = System.currentTimeMillis();
        dictionary = new NTCDictionary();
        dictionary.load(PwNLP.Config.OrganizationDictionaryPath);
        logger.info(PwNLP.Config.OrganizationDictionaryPath + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
        transformMatrixDictionary = new TransformMatrixDictionary<NTC>(NTC.class);
        transformMatrixDictionary.load(PwNLP.Config.OrganizationDictionaryTrPath);
        trie = new AhoCorasickDoubleArrayTrie<NTCPattern>();
        TreeMap<String, NTCPattern> patternMap = new TreeMap<String, NTCPattern>();
        for (NTCPattern pattern : NTCPattern.values())
        {
            patternMap.put(pattern.name(), pattern);
        }
        trie.build(patternMap);
    }

    /**
     * 模式匹配
     *
     * @param tagList         确定的标注序列
     * @param vertexList     原始的未加角色标注的序列
     * @param wordNetOptimum 待优化的图
     * @param wordNetAll
     */
    public static void parsePattern(List<NTC> tagList, List<Vertex> vertexList, final WordNet wordNetOptimum, final WordNet wordNetAll)
    {
//        ListIterator<Vertex> listIterator = vertexList.listIterator();
        StringBuilder sbPattern = new StringBuilder(tagList.size());
        for (NTC nt : tagList)
        {
            sbPattern.append(nt.toString());
        }
        String pattern = sbPattern.toString();
        final Vertex[] wordArray = vertexList.toArray(new Vertex[0]);
        trie.parseText(pattern, new AhoCorasickDoubleArrayTrie.IHit<NTCPattern>()
        {
            @Override
            public void hit(int begin, int end, NTCPattern pattern)
            {
                StringBuilder sbName = new StringBuilder();
                for (int i = begin; i < end; ++i)
                {
                    sbName.append(wordArray[i].realWord);
                }
                String name = sbName.toString();
                // 对一些bad case做出调整
                if (isBadCase(name)) return;

                // 正式算它是一个名字
                if (PwNLP.Config.DEBUG)
                {
                    System.out.printf("识别出机构名：%s %s\n", name, pattern);
                }
                int offset = 0;
                for (int i = 0; i < begin; ++i)
                {
                    offset += wordArray[i].realWord.length();
                }
                wordNetOptimum.insert(offset, new Vertex(Predefine.TAG_GROUP, name, ATTRIBUTE, WORD_ID), wordNetAll);
            }
        });
    }

    /**
     * 因为任何算法都无法解决100%的问题，总是有一些bad case，这些bad case会以“单词 Z 1”的形式加入词典中<BR>
     * 这个方法返回是否是bad case
     *
     * @param name
     * @return
     */
    static boolean isBadCase(String name)
    {
        EnumItem<NTC> nrEnumItem = dictionary.get(name);
        if (nrEnumItem == null) return false;
        return nrEnumItem.containsLabel(NTC.Z);
    }
}
