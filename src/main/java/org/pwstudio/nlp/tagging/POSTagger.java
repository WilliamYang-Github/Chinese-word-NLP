package org.pwstudio.nlp.tagging;

import java.util.ArrayList;
import java.util.List;

import org.pwstudio.nlp.algorithm.Viterbi;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.dictionary.CoreDictionaryTransformMatrixDictionary;
import org.pwstudio.nlp.dictionary.CustomDictionary;
import org.pwstudio.nlp.seg.common.Term;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.utility.TextUtility;

/**
 * 独立的词性标注器
 * @author TylunasLi
 */
public class POSTagger
{

    /**
     * 对于已经经过了分词的文本标注词性
     * @param seggedWord
     * @return
     */
    public static List<Term> tagPartOfSpeech(List<String> seggedWord)
    {
        List<Vertex> vertexList = toVertexList(seggedWord);
        Viterbi.compute(vertexList, CoreDictionaryTransformMatrixDictionary.transformMatrixDictionary);
        List<Term> result = new ArrayList<Term>(seggedWord.size());
        for (int i=0; i<seggedWord.size(); i++)
        {
            Vertex vertex = vertexList.get(i+1);
            Term term = new Term(vertex.word, vertex.guessNature());
            result.add(term);
        }
        return result;
    }
    
    /**
     * 生成Vertex节点列表，便于标注词性
     * @param wordList
     * @return
     */
    protected static List<Vertex> toVertexList(List<String> wordList)
    {
        ArrayList<Vertex> vertexList = new ArrayList<Vertex>(wordList.size() + 2);
        vertexList.add(Vertex.newB());
        for (String word : wordList)
        {
            CoreDictionary.Attribute attribute = guessAttribute(word);
            Vertex vertex = new Vertex(word, attribute);
            vertexList.add(vertex);
        }
        vertexList.add(Vertex.newE());
        return vertexList;
    }

    /**
     * 查询或猜测一个词语的属性，
     * 先查词典，然后对字母、数字串的属性进行判断，最后猜测未登录词
     * @param term
     * @return
     */
    public static CoreDictionary.Attribute guessAttribute(String word)  
    {
        CoreDictionary.Attribute attribute = CoreDictionary.get(word);
        if (attribute == null)
        {
            attribute = CustomDictionary.get(word);
        }
        if (attribute == null)
        {
            if (TextUtility.isAllNum(word))
                attribute = CoreDictionary.get(CoreDictionary.M_WORD_ID);
            else if (TextUtility.isAllChineseNum(word))
                attribute = CoreDictionary.get(CoreDictionary.M_WORD_ID);
            else if (TextUtility.isAllLetterOrNum(word))
                attribute = new CoreDictionary.Attribute(Nature.nx);
            else if (word.trim().length() == 0)
                attribute = new CoreDictionary.Attribute(Nature.x);
            else attribute = new CoreDictionary.Attribute(Nature.nz);
        }
        return attribute;
    }
    
}
