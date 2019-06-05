package org.pwstudio.nlp.tagging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.classification.vo.InstanceVector;
import org.pwstudio.nlp.classification.vo.PredictResult;
import org.pwstudio.nlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import org.pwstudio.nlp.corpus.io.ByteArray;
import org.pwstudio.nlp.dictionary.py.Pinyin;
import org.pwstudio.nlp.dictionary.py.PinyinDictionary;
import org.pwstudio.nlp.model.classifier.perceptron.SparseRestrictedPerceptronModel;
import org.pwstudio.nlp.model.tagger.Table;
import org.pwstudio.nlp.seg.Viterbi.ViterbiSegment;
import org.pwstudio.nlp.seg.common.Term;

public class PinyinTagger {
    
    public final static String PinyinModelPath = "data/dictionary/pinyin/pinyinClassifyModel.bin"; 

    protected static SparseRestrictedPerceptronModel model;
    
    static
    {
        model = new SparseRestrictedPerceptronModel();
        ByteArray byteArray = ByteArray.createByteArray(PwNLP.Config.root + PinyinModelPath);
        model.load(byteArray);
    }
    
    public static List<Pinyin> tagPinyin(String text, boolean remainNone)
    {
        List<Integer> ambiguousList = new ArrayList<Integer>();
        List<Pinyin> pinyinList = new ArrayList<Pinyin>(text.length());
        LinkedList<Pinyin[]>[] wordNet = fastTagPinyin(text, pinyinList, ambiguousList);
        if (ambiguousList.size() > 0)
        {
            Table table = createIntanceTable(text, null, pinyinList);
            for (Integer offsetPoint : ambiguousList)
            {
                Pinyin best = pinyinClassify(table, wordNet, offsetPoint.intValue());
                pinyinList.set(offsetPoint.intValue(), best);
            }
        }
        if (!remainNone)
        {
            List<Pinyin> newPinyinList = new ArrayList<Pinyin>(text.length());
            for (Pinyin pinyin : pinyinList)
            {
                if (pinyin != Pinyin.none5)
                    newPinyinList.add(pinyin);
            }
            pinyinList = newPinyinList;
        }
        return pinyinList;
    }
    
    public static List<List<Pinyin>> tagPinyin(List<Term> words)
    {
        StringBuilder builder = new StringBuilder();
        for (Term term : words)
            builder.append(term.word);
        String text = builder.toString();
        List<Integer> ambiguousList = new ArrayList<Integer>();
        List<Pinyin> pinyinList = new ArrayList<Pinyin>(text.length());
        LinkedList<Pinyin[]>[] wordNet = fastTagPinyin(text, pinyinList, ambiguousList);
        if (ambiguousList.size() > 0)
        {
            Table table = createIntanceTable(text, words, pinyinList);
            for (Integer offsetPoint : ambiguousList)
            {
                Pinyin best = pinyinClassify(table, wordNet, offsetPoint.intValue());
                pinyinList.set(offsetPoint.intValue(), best);
            }
        }
        List<List<Pinyin>> resultList = new ArrayList<List<Pinyin>>(words.size());
        int chrId = 0;
        for (Term term : words)
        {
            ArrayList<Pinyin> wordPinyin = new ArrayList<Pinyin>(
                    pinyinList.subList(chrId, chrId+term.length()) );
            resultList.add(wordPinyin);
            chrId+=term.length();
        }
        return resultList;
    }

    private static LinkedList<Pinyin[]>[] fastTagPinyin(String text, List<Pinyin> pinyinList, List<Integer> ambiguousList)
    {
        final LinkedList<Pinyin[]>[] wordNet = new LinkedList[text.length()];
        for (int i=0; i<wordNet.length; i++)
        {
            wordNet[i] = new LinkedList<Pinyin[]>();
        }
        PinyinDictionary.parseText(text.toCharArray(), new AhoCorasickDoubleArrayTrie.IHit<Pinyin[]>()
        {
            @Override
            public void hit(int begin, int end, Pinyin[] value)
            {
                int length = end - begin;
                if (length < value.length)
                {
                    /** 多音字, 加入词图. */
                    for (Pinyin candidate : value)
                        wordNet[begin].add(new Pinyin[]{candidate});
                }
                else 
                {
                    wordNet[begin].add(value);
                }
            }
        });
        for (int offset = 0; offset < wordNet.length; )
        {
            if (wordNet[offset].isEmpty())
            {
                pinyinList.add(Pinyin.none5);
                ++offset;
                continue;
            }
            Pinyin[] best = null;
            if (wordNet[offset].size() == 1)
                best = wordNet[offset].getFirst();
            else
            {
                for (Pinyin[] candidate : wordNet[offset])
                {
                    if (candidate.length > 1 && (best == null || best.length < candidate.length))
                        best = candidate;
                }
                if (best == null)
                {
                    best = new Pinyin[1];
                    ambiguousList.add(offset);
                }
            }
            for (Pinyin pinyin : best)
            {
                pinyinList.add(pinyin);
            }
            offset += best.length;
        }
        return wordNet;
    }

    public static Table createIntanceTable(String text, List<Term> termList, List<Pinyin> pinyinList)
    {
        String[][] features = new String[text.length()][6];
        // 分词特征
        if (termList == null)
            termList = new ViterbiSegment().seg(text);
        int i=0;
        for (Term term : termList)
        {
            int length = term.word.length();
            // 分词特征
            if (term.length() == 1)
            {
                features[i][0] = term.word;
                features[i][1] = "S";
            }
            else
            {
                features[i][0] = term.word.substring(0, 1);
                features[i][1] = "B";
                for (int j=1; j<length-1; j++)
                {
                    features[i+j][0] = term.word.substring(j, j+1);
                    features[i+j][1] = "M";
                }
                features[i+length-1][0] = term.word.substring(length-1, length);
                features[i+length-1][1] = "E";
            }
            // 词性
            for (int j=0; j<term.word.length(); j++)
                features[i+j][2] = term.nature.name();
            i += term.length();
        }
        // 拼音
        for (i=0; i<text.length(); i++)
        {
            Pinyin pinyin = pinyinList.get(i);
            String tone = pinyin == null ? "" : String.valueOf(pinyin.getTone());
            String shengmu = pinyin == null ? "" : pinyin.getShengmu().name();
            String yunmu = pinyin == null ? "" : pinyin.getYunmu().name();
            features[i][3] = tone;
            features[i][4] = shengmu;
            features[i][5] = yunmu;
        }
        Table table = new Table();
        table.v = features;
        return table;
    }

    private static Pinyin pinyinClassify(Table table, LinkedList<Pinyin[]>[] wordNet, int offset)
    {
        InstanceVector instance = createIntanceVector(table, offset);
        int[] classes = new int[wordNet[offset].size()];
        int i=0;
        for (Pinyin[] candidate : wordNet[offset])
        {
            classes[i] = candidate[0].ordinal();
            i++;
        }
        PredictResult result = model.predict(instance, classes);
        Pinyin pinyin = Pinyin.values()[result.getClassId()];
        return pinyin;
    }

    public static InstanceVector createIntanceVector(Table table, int offset)
    {
        HashMap<String, Double> vector = new HashMap<String, Double>();
        String character = table.get(offset, 0);
        int featureId = 1;
        String[] featureSet = new String[]
            {
                /** %[-1,0]:U01 */
                table.get(offset-1, 0),
                /** %[ 1,0]:U02 */
                table.get(offset+1, 0),
                /** %[-1,0]-%[1,0]:U03 */
                table.get(offset-1, 0)+"-"+table.get(offset+1, 0),
                /** %[ 0,1]:U04 */
                table.get(offset, 1),
                /** %[-1,1]:U05 */
                table.get(offset-1, 1),
                /** %[ 1,1]:U06 */
                table.get(offset+1, 1),
                /** %[0,2]:U07 */
                table.get(offset, 2),
                /** %[0,1]-%[0,2]:U08 */
                table.get(offset, 1)+"-"+table.get(offset, 2),
                /** %[-1,2]:U09 */
                table.get(offset-1, 2),
                /** %[ 1,2]:U10 */
                table.get(offset+1, 2),
                /** %[-1,3]:U11 */
                table.get(offset-1, 3),
                /** %[ 1,3]:U12 */
                table.get(offset+1, 3),
            };
        for (String feature : featureSet)
        {
            String key = String.format("%s%s:U%02d", character, feature, featureId);
            vector.put(key, 1.0);
            featureId++;
        }
        InstanceVector instance = new InstanceVector(vector, 0);
        return instance;
    }
}
