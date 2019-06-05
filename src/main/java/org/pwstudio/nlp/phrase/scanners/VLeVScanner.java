package org.pwstudio.nlp.phrase.scanners;

import java.util.List;

import org.pwstudio.nlp.seg.common.Term;

/**
 * 处理 "/v 了/ule /v" "/v 了/ule 一/m /v" "/v 一/m /v"
 * @author TylunasLi
 *
 */
public class VLeVScanner extends AbstractScanner {

	public static VLeVScanner instance = new VLeVScanner();
    private int size = 3;

    @Override
	public int size()
    {
		return 3; /* 23 */
	}

	@Override
	protected boolean activated(Term[] sentence, int start)
	{
	    this.size = 3; /* 29 */
	    Term base = sentence[start]; /* 30 */
	    if (sentence.length - start >= 4) /* 31 */
	    {
	        if (sentence[start + 3].word.equals(base.word)) /* 33 */
	        {
	            if (isLe(sentence[start + 1]) && isYi(sentence[start + 2])) /* 35 */
	            {
	                this.size = 4; /* 37 */
	                return true; /* 38 */
	            }
	        }
	    }
	    else if (sentence[start + 2].word.equals(base.word) /* 42 */
                && isLe(sentence[start + 1]) || isYi(sentence[start + 1])) /* 43 */
        {
            this.size = 3; /* 45 */
            return true; /* 46 */
        }
        return false; /* 48 */
	}

	@Override
	protected int transform(Term[] sentence, int start, List<Term> outputPhrase)
	{
	    outputPhrase.add(sentence[start]); /* 54 */
	    return this.size; /* 55 */
	}

}
