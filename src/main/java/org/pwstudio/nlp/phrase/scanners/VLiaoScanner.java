package org.pwstudio.nlp.phrase.scanners;

import java.util.List;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.seg.common.Term;

/**
 * 
 * @author TylunasLi
 *
 */
public class VLiaoScanner extends AbstractScanner
{

    public static VLiaoScanner instance = new VLiaoScanner();
    private int size = 2;

    @Override
    public int size()
    {
        return 2; /* 22 */
    }

    @Override
    protected boolean activated(Term[] sentence, int start)
    {
        size = 2; /* 28 */
        if ("得了".equals(sentence[start + 1].word)) /* 29 */
        {
            size = 2; /* 31 */
            return true; /* 32 */
        }
        else if ("不了".equals(sentence[start + 1].word)) /* 34 */
        {
            size = 2; /* 36 */
            return true; /* 37 */
        }
        
        
        else if (sentence.length - start < 3) /* 41 */
            return false; /* 42 */
        if (!isLe(sentence[start + 2])) /* 43 */
            return false; /* 44 */
        else if ("得".equals(sentence[start + 1].word)) /* 45 */
        {
            size = 3; /* 47 */
            return true; /* 48 */
        }
        else if ("不".equals(sentence[start + 1].word)) /* 50 */
        {
            size = 3; /* 52 */
            return true; /* 53 */
        }
        else
            return false; /* 56 */
    }

    @Override
    protected int transform(Term[] sentence, int start, List<Term> outputPhrase)
    {
        if (sentence[start + 1].word.startsWith("得")) /* 62 */
        {
            outputPhrase.add(can); /* 64 */
        }
        else if (sentence[start + 1].word.startsWith("不")) /* 66 */
        {
            outputPhrase.add(cannot); /* 68 */
        }
        outputPhrase.add(sentence[start]); /* 70 */
        return size; /* 71 */
    }

}
