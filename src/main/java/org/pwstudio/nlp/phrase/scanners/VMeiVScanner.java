package org.pwstudio.nlp.phrase.scanners;

import java.util.List;

import org.pwstudio.nlp.seg.common.Term;

/**
 * 
 * @author TylunasLi
 *
 */
public class VMeiVScanner extends AbstractScanner {
    
    
    
    public static VMeiVScanner instance = new VMeiVScanner();

    @Override
    public int size()
    {
        return 3; /* 21 */
    }

    @Override
    protected boolean activated(Term[] sentence, int start)
    {
        if (sentence[start + 1].word.equals("没")) /* 27 */
        {
            if (sentence[start + 2].word.startsWith(sentence[start].word)) /* 29 */
                return true; /* 30 */
        }
        else if (sentence[start + 1].word.startsWith("没")) /* 32 */
        {
            if (sentence[start + 1].word.endsWith(sentence[start].word)) /* 34 */
                return true; /* 35 */
        }
        return false; /* 37 */
    }

    @Override
    protected int transform(Term[] sentence, int start, List<Term> outputPhrase)
    {
        outputPhrase.add(whether); /* 43 */
        outputPhrase.add(been); /* 44 */
        outputPhrase.add(sentence[start + 2]); /* 45 */
        return 0; /* 46 */
    }
}