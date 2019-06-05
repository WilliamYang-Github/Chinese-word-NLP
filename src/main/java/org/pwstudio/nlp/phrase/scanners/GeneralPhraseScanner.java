package org.pwstudio.nlp.phrase.scanners;

import java.util.ArrayList;
import java.util.List;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.seg.common.Term;


/**
 * 通用短语匹配算法
 * @author TylunasLi
 *
 */
public abstract class GeneralPhraseScanner extends AbstractScanner {

    /**
     * 字符串形式的模板
     */
    protected String[][] stringPatterns;
    /**
     * 词语形式的模板，可以带有词性限制
     */
    protected List<List<Term>> termPatterns;

    public void loadStringTemplate(String[] rules)
    {
        this.stringPatterns = new String[rules.length][]; /* 28 */
        for(int i = 0; i < rules.length; ++i)/* 29 */
        { 
            String[] keys = rules[i].split(" "); /* 31 */
            this.stringPatterns[i] = keys; /* 32 */
        }
    } /* 34 */

    public void loadTermTemplate(String[] rules)
    {
        if (this.termPatterns == null) /* 38 */
            this.termPatterns = new ArrayList<List<Term>>(rules.length); /* 39 */
        for(String ruleString : rules)/* 40 */
        { 
            String[] nodes = ruleString.split(" "); /* 42 */
            List<Term> rule = new ArrayList<Term>(nodes.length); /* 43 */
            for(String node : nodes) /* 44 */
            {
                Term term = parseTermPattern(node); /* 46 */
                rule.add(term); /* 47 */
            }
            this.termPatterns.add(rule); /* 49 */
        }
    } /* 51 */
    /**
     * 
     * @param word
     * @return
     */
    public static Term parseTermPattern(String word)
    {
        Term term = null; /* 59 */
        boolean noTag = true; /* 60 */
        String[] parts = new String[1]; /* 61 */
        if (word.length() > 1) /* 62 */
        {
            parts = word.split("/"); /* 64 */
            if (parts.length == 2 || parts.length == 3) /* 65 */
                noTag = false; /* 66 */
        }
        if (noTag) /* 68 */
        {
            term = new Term(word, null); /* 70 */
        }
        else
        {
            Nature nature = null; /* 74 */
            try
            {
                nature = parts.length == 2 ? (Nature)Enum.valueOf(Nature.class, parts[1]) : (Nature)Enum.valueOf(Nature.class, parts[2]); /* 77 */
            }
            catch (IllegalArgumentException e) /* 79 */
            {
                nature = null; /* 81 */
            }
            if (parts.length == 2) /* 83 */
            {
                term = new Term(parts[0], nature); /* 85 */
            }
            else
            {
                term = new Term("/", nature); /* 89 */
            }
        }
        return term; /* 92 */
    }

    /**
     * 
     * @param phraseBank
     * @param sentence
     * @param start
     * @return
     */
    public static int HitOnePhrase(String[][] phraseBank, Term[] sentence, int start)
    {
        for(int num = 0; num < phraseBank.length; ++num) /* 104 */
        {
            String[] pattern = phraseBank[num]; /* 106 */
            boolean matched = true; /* 107 */
            if (sentence.length - start < pattern.length) /* 108 */
                continue;
            for(int i = 0; i < pattern.length; ++i) /* 110 */
            {
                if (!sentence[start + i].word.equals(pattern[i])) /* 112 */
                    matched = false; /* 113 */
            }
            
            if (matched) /* 116 */
                return num; /* 117 */
        }
        return -1; /* 119 */
    }

    /**
     * 
     * @param phraseBank
     * @param sentence
     * @param start
     * @return
     */
    public static int HitOnePhrase(List<List<Term>> phraseBank, Term[] sentence, int start)
    {
        for(int num = 0; num < phraseBank.size(); ++num) /* 131 */
        {
            List<Term> pattern = phraseBank.get(num); /* 133 */
            boolean matched = true; /* 134 */
            if (sentence.length - start < pattern.size()) /* 135 */
                continue;
            for(int i = 0; i < pattern.size(); ++i) /* 137 */
            {
                if (!isTermMatched(sentence[start + i], (Term)pattern.get(i))) /* 139 */
                    matched = false; /* 140 */
            }

            if (matched) /* 143 */
                return num; /* 144 */
        }
        return -1; /* 146 */
    }

    /**
     * 判断当前词语是否与模式相匹配
     * @param test
     * @param filter
     * @return
     */
    public static boolean isTermMatched(Term test, Term filter) 
    {
        if (!filter.word.isEmpty() && filter.nature != null) /* 157 */
        {
            return test.equals(filter); /* 159 */
        }
        else if (!filter.word.isEmpty() && filter.word.equals(test.word))
        { /* 161 */
            return true; /* 163 */
        }
        else
        {
            return filter.nature != null && test.nature.startsWith(filter.nature.name()); /* 165 */
        }
    }

}
