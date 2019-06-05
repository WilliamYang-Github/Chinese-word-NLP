package org.pwstudio.nlp.dictionary.m;

import java.util.List;
import java.util.TreeMap;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import org.pwstudio.nlp.collection.trie.DoubleArrayTrie;
import org.pwstudio.nlp.collection.trie.bintrie.BinTrie;
import org.pwstudio.nlp.collection.trie.bintrie.BinTrieSearcher;
import org.pwstudio.nlp.corpus.tag.M;
import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.dictionary.CoreDictionary.Attribute;
import org.pwstudio.nlp.recognition.EntityObserver;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.utility.Predefine;
import org.pwstudio.nlp.utility.TextUtility;

/**
 * 精确数字识别
 * @author 李辰刚
 *
 */
public class NumberPatternDictionary
{
    /**
     * 特殊字符映射用的词典
     */
    //public static TreeMap<String, T> numberTagMap;
    
    /**
     * 本词典专注的词的ID
     */
    static final int WORD_ID = CoreDictionary.getWordID(Predefine.TAG_NUMBER);
    /**
     * 本词典专注的词的属性
     */
    static final Attribute ATTRIBUTE = CoreDictionary.get(WORD_ID);
    /**
     * 分词阶段字符映射用的词典
     */
    public static BinTrie<M> wordDict;
    /**
     * 模式库
     */
    public static AhoCorasickDoubleArrayTrie<NumberPattern> trie;
    
    public static String[] normalword={"第","几","余","来","多","半","数",
            "一","二","三","四","五","六","七","八","九","〇","○","两",
            "壹","贰","叁","肆","伍","陆","柒","捌","玖","零",
            "十","百","千","万","亿","兆","拾","佰","仟","百分点",
            "K","k","点","分之","%","％","成","倍"};
    public static M []  normalvalue = {M.O, M.K, M.H, M.H, M.K, M.K, M.K,
            M.M, M.M, M.M, M.M, M.M, M.M, M.M, M.M, M.M, M.M, M.M, M.M,
            M.M, M.M, M.M, M.M, M.M, M.M, M.M, M.M, M.M, M.M,
            M.L, M.L, M.L, M.L, M.L, M.L, M.L, M.L, M.L, M.L, 
            M.B, M.B, M.X, M.X, M.U, M.U, M.U, M.U};

    static
    {
        wordDict = new BinTrie<M>();
        TreeMap<String, M> numberTagMap = new TreeMap<String, M>();
        for (int i=0; i<normalword.length; i++)
        {
            numberTagMap.put(normalword[i], normalvalue[i]);
        }
        wordDict.build(numberTagMap);
        
        //整数小节
        String[] numSections={"M",  /* 一 */
                "LM",           /* 十五 */			"ML",           /* 二十 */
                "MLM",          /* 二十五 */		"MLZM",         /* 一百零八 */
                "MLML",         /* 二百二十 */		"MLMLM",        /* 二百二十一 */
                "MLMLML",       /* 一千两百五十 */	"MLMLMLM",      /* 四千三百二十一 */
                "ZM",           /* 零五 */			"ZML"           /*  */
        };
        trie = new AhoCorasickDoubleArrayTrie<NumberPattern>();
        //复杂数字
        TreeMap<String, NumberPattern> map = new TreeMap<String, NumberPattern>();
        for (NumberPattern pattern: NumberPattern.values())
        {
            map.put(pattern.name(), pattern);
        }
        trie.build(map);
    }

    /**
     * 对数字Pattern进行识别
     * @param tagList
     * @param vertexList
     * @param wordNetOptimum
     * @param wordNetAll
     */
    public static void parsePattern(List<M> tagList, List<Vertex> vertexList, final WordNet wordNetOptimum,
            final WordNet wordNetAll)
    {
        StringBuilder sbPattern = new StringBuilder(tagList.size());
        for (M m : tagList)
        	sbPattern.append(m.toString());
        String pattern = sbPattern.toString();
        final String[] patternNet = new String[vertexList.size()];
        trie.parseText(pattern, new AhoCorasickDoubleArrayTrie.IHit<NumberPattern>()
        {
            @Override
            public void hit(int begin, int end, NumberPattern pattern)
            {
                int length = end - begin;
                if (patternNet[end-1] == null || patternNet[end-1].length() < length)
                    patternNet[end-1]=pattern.name();
             }

        });
        // 长度越大，越可靠
        for (int offset = patternNet.length - 1; offset >= 0; ) {
            if (patternNet[offset] == null) {
                --offset;
                continue;
            }
            //到  offset 为止长度为  patternNet[offset].length() 的串是一个数量
            int start = offset-patternNet[offset].length()+1;
            StringBuilder sbName = new StringBuilder();
            for (int i = start; i<=offset; ++i)
            {
                sbName.append(vertexList.get(i).realWord);
            }
            String name = sbName.toString();
            if (PwNLP.Config.DEBUG)
            {
                System.out.printf("识别出数字：%s %s\n", name, patternNet[offset]);
            }
            int startLine = 0;
            for (int i = 0; i < start; ++i)
            {
                startLine += vertexList.get(i).realWord.length();
            }
            Vertex newVertex = new Vertex(Predefine.TAG_NUMBER, name, ATTRIBUTE, WORD_ID);
            wordNetOptimum.insert(startLine, newVertex, wordNetAll);
            wordNetAll.add(startLine, newVertex);
            offset -= patternNet[offset].length();
        }
    }

    /**
     * 对数字Pattern进行识别
     * @param tagList
     * @param vertexList
     * @param wordNetOptimum
     * @param wordNetAll
     */
    public static void extractFromPattern(List<M> tagList, final List<String> sentenceSequnece, 
            EntityObserver<NumberPattern> listener)
    {
        StringBuilder sbSquence = new StringBuilder(tagList.size());
        for (M m : tagList)
            sbSquence.append(m.toString());
        String squence = sbSquence.toString();
        final NumberPattern[] patternNet = new NumberPattern[tagList.size()];
        trie.parseText(squence, new AhoCorasickDoubleArrayTrie.IHit<NumberPattern>()
        {
            @Override
            public void hit(int begin, int end, NumberPattern pattern)
            {
                int length = end - begin;
                if (patternNet[end-1] == null || patternNet[end-1].name().length() < length)
                    patternNet[end-1]=pattern;
             }

        });
        // 长度越大，越可靠
        for (int offset = patternNet.length - 1; offset >= 0; ) {
            if (patternNet[offset] == null) {
                --offset;
                continue;
            }
            //到  offset 为止长度为  patternNet[offset].length() 的串是一个数量
            int start = offset-patternNet[offset].name().length()+1;
//            if (listener != null)
//                listener.onHit(sentenceSequnece, start, offset, patternNet[offset]);
            offset -= patternNet[offset].name().length();
        }
    }
    
    /**
     * 中文数词分词方法
     * @param word
     * @param parts
     * @param tags
     */
    public static void segNumber(final String word, List<String> parts, List<M> tags)
    {
        /** 由于已经是没有交叠的原子的词语， 直接看各个parts */
//        List<AhoCorasickDoubleArrayTrie<M>.Hit<M>> fragments = 
//                NumberPatternDictionary.wordDict.parseText(word);
//        ListIterator<AhoCorasickDoubleArrayTrie<M>.Hit<M>> iter = fragments.listIterator();
    	BinTrieSearcher<M> searcher = new BinTrieSearcher<M>(NumberPatternDictionary.wordDict, 0, word.toCharArray());
        for (int i=0; i<word.length(); ) {
            if (searcher.next()/*iter.hasNext()*/)
            {
            //    AhoCorasickDoubleArrayTrie<M>.Hit<M> atom = iter.next();
                if (searcher.begin-i > 0)
                {
                    String other = word.substring(i, searcher.begin);
                    if (TextUtility.isAllNum(other)) {
                        parts.add(other);
                        tags.add(M.A);
                    }
                    else
                    {
                        parts.add(other);
                        tags.add(M.Z);
                    } 
                }
                parts.add(word.substring(searcher.begin, searcher.begin+searcher.length));
                tags.add(searcher.value);
                i=searcher.begin+searcher.length;
            }
            else if (word.length()-i > 0)
            {
                String other = word.substring(i, word.length());
                if (TextUtility.isAllNum(other))
                {
                    parts.add(other);
                    tags.add(M.A);
                }
                else
                {
                    parts.add(other);
                    tags.add(M.Q);
                } 
                i=word.length();
            }
        }
    }

    /**
     * 处理 BadCase的规则
     * @param pattern
     * @param sequnece
     * @param begin
     * @param end
     * @return
     */
    public static boolean isBadCase(NumberPattern pattern, List<String> sequnece, int begin, int end)
    {
        String first = sequnece.get(begin);
        String tail = first.substring(first.length()-1);
        switch (pattern)
        {
            case AK:
            {
                if ("0".equals(tail))
                    return false;
                return true;
            }
            case MK:
            {
                if (M.L == NumberPatternDictionary.wordDict.get(tail))
                    return false;
                return true;
            }
            case K:
            {
                if ("多".equals(first))
                    return true;
                return false;
            }
            case LXK:
            case MXK:
            case MXM:
            case LXM:
            case AXM:
            {
                String middle = sequnece.get(begin+1);
                String last = sequnece.get(begin+2);
                if ("多".equals(last) || "半".equals(last))
                    return true;
                if ("分之".equals(middle))
                    return false;
                if (last.contains("十") || last.contains("百"))
                    return true;
                if (begin+3 >= sequnece.size())
                    return false;
                String next = sequnece.get(begin+3);
                if ("分".equals(next)
                        || ("一".equals(last) && "起".equals(next)))
                    return true;
                return false;
            }
            default:
                return false;
        }
    }
}
