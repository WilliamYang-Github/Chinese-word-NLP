package org.pwstudio.nlp.seg.processor;

import java.util.Iterator;
import java.util.LinkedList;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie.IHit;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.corpus.tag.M;
import org.pwstudio.nlp.dictionary.m.NumberPattern;
import org.pwstudio.nlp.dictionary.m.NumberPatternDictionary;
import org.pwstudio.nlp.dictionary.LanguageModel;
import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.dictionary.CoreDictionary.Attribute;
import org.pwstudio.nlp.seg.Config;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.utility.Predefine;
import org.pwstudio.nlp.utility.TextUtility;

public class ChineseNumPreProcessor extends PreProcessor {
    
    public static ChineseNumPreProcessor instance = new ChineseNumPreProcessor();

    @Override
    public boolean isActive(Config config) {
        return true;
    }

    @Override
    protected Nature getNature() {
        return Nature.m;
    }

    @Override
    public void recognizeWord(char[] sentence, WordNet wordNetAll) {
        /** 
         * Step A:    提取所有可能的数字，并用逆向最大法按数字切分句子.    <br>
         *             用NumberPatternDictionary对切分结果进行标注。
         */
        final LinkedList<String> numberSegResult = new LinkedList<String>();
        final WordNet resultGraph = wordNetAll;
        String sequence = tagNumber(wordNetAll, numberSegResult);
        final int[] startPoints = new int[sequence.length()];
        int curStart = 1;
        int i=0;
        for (String part : numberSegResult) {
            startPoints[i] = curStart;
            curStart += part.length();
            i++;
        }
        /** 
         * Step B:    提取标注结果中的NumberPattern. <br>
         *             将对应词语加入词图。
         */
        NumberPatternDictionary.trie.parseText(sequence, new IHit<NumberPattern>(){
            @Override
            public void hit(int begin, int end, NumberPattern value) {
                if (NumberPatternDictionary.isBadCase(value, numberSegResult, begin, end)) {
                    return;
                }
                StringBuilder vertexBuilder = new StringBuilder();
                for (int i=begin; i<end; i++)
                    vertexBuilder.append(numberSegResult.get(i));
                if (PwNLP.Config.DEBUG) {
                    System.out.println("识别出数字： " + vertexBuilder.toString() + " " + value.toString());
                }
                resultGraph.add(startPoints[begin], new Vertex(Predefine.TAG_NUMBER, vertexBuilder.toString(), 
                        CoreDictionary.get(CoreDictionary.M_WORD_ID), CoreDictionary.M_WORD_ID));
            }
        });
    }

    /**
     * 提取词图中的数字串
     * @param wordNetAll
     * @param result
     * @return
     */
    private String tagNumber(WordNet wordNetAll, LinkedList<String> result) {
        StringBuilder tagbuilder = new StringBuilder();
        char[] tagArray = new char[wordNetAll.charArray.length];
        final Vertex[] numberCollector = new Vertex[wordNetAll.charArray.length];
        /**
         * Step A-1: 在词图中寻找符合条件的数词或者片段
         */
        for (int i=0; i<wordNetAll.charArray.length; i++) {
            Iterator<Vertex> iter = wordNetAll.descendingIterator(i+1);
            while (iter.hasNext()) {
                Vertex vertex = iter.next();
                int end = i+vertex.realWord.length()-1;
                if ("十分".equals(vertex.realWord)) {
                    numberCollector[end-1] = new Vertex(Predefine.TAG_NUMBER, "十", 
                            new Attribute(Nature.m), -1);
                }
                boolean isNumber = vertex.hasNature(Nature.m) || NumberPatternDictionary.
                        wordDict.containsKey(vertex.realWord);
                if (isNumber) {
                    if (numberCollector[end] == null)
                        numberCollector[end] = vertex;
                }
            }
        }
        /**
         * Step A-2: 逆向最大匹配得到词语，补充没有完整嵌入的部分
         */
        M lastState =M.Z;
        for (int offset = numberCollector.length - 1; offset >= 0; ) {
            M state;
            if (numberCollector[offset] == null) {
                result.addFirst(String.valueOf(wordNetAll.charArray[offset]));
                state = M.Z;
                tagArray[offset] = 'Z';
                --offset;
            }else{
                state = tagWord(numberCollector[offset].realWord);
                if ((state == M.M || state == M.L) && (lastState == M.M || lastState == M.L)
                        && !"万亿兆".contains(numberCollector[offset].realWord)) {
                    // 要合并的数词，当前词语附加到前一词语上面
                    String last = result.pollFirst();
                    result.addFirst(numberCollector[offset].realWord + last);
                } else {
                    result.addFirst(numberCollector[offset].realWord);
                    tagArray[offset] = state.name().charAt(0);
                }
                offset -= numberCollector[offset].realWord.length();
            }
            lastState = state;
        }
        for (int k=0; k<tagArray.length; k++) {
            if (tagArray[k] != 0)
                tagbuilder.append(tagArray[k]);
        }
        return tagbuilder.toString();
    }

    private M tagWord(String chineseNumber) {
        // 直接尝试
        M tag = NumberPatternDictionary.wordDict.get(chineseNumber);
        if (tag != null)
            return tag;
        if (TextUtility.isAllNum(chineseNumber))
            return M.A;
        // 多字数词
        if (chineseNumber.length() > 1) {
            String head = chineseNumber.substring(0, 1);
            M h = NumberPatternDictionary.wordDict.get(head);
            String tail = chineseNumber.substring(chineseNumber.length()-1);
            M t = NumberPatternDictionary.wordDict.get(tail);
            if ((h == M.M || h == M.L) && (t == M.M || t == M.L))
                return M.M;
        }
        return M.Z;
    }
}
