package org.pwstudio.nlp.phrase.scanners;

import java.util.List;

import org.pwstudio.nlp.seg.common.Term;

/**
 * 重复动词
 * @author TylunasLi
 *
 */
public class VVScanner extends AbstractScanner {

	public static VVScanner instance = new VVScanner();

    @Override
	public int size()
    {   
		return 2;
	}

	@Override
	protected boolean activated(Term[] sentence, int start)
	{
	    if (sentence[start].word.equals(sentence[start + 1].word) /* 25 */
	            && (sentence[start].nature.startsWith("v") || sentence[start + 1].nature.startsWith("v")) ) /* 26 */
	        return true;
		return false;
	}

	@Override
	protected int transform(Term[] sentence, int start, List<Term> outputPhrase) {
	    outputPhrase.add(sentence[start]);
		return 2;
	}

}
