package org.pwstudio.nlp.phrase.scanners;


import java.util.ArrayList;
import java.util.List;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.seg.common.Term;


/**
 * 处理"A 不 A / A 不 AB", 变形为 "是否 AB"
 * @author TylunasLi
 *
 */
public class ANAScanner extends AbstractScanner {
	
	public static ANAScanner instance = new ANAScanner();

    protected int size = 2;

	final static Term whether = new Term("是否", Nature.v);
	
	public int size() {
		return 2;
	}

	protected boolean activated(Term[] sentence, int start) {
		if (start+size > sentence.length)
			return false;
		if (sentence[start+1].word.equals("不")) {
			if (sentence[start+2].word.startsWith(sentence[start].word))
				return true;
		}
		return false;
	}

	protected int transform(Term[] sentence, int start, List<Term> outputPhrase) {
		outputPhrase.add(whether);
		outputPhrase.add(sentence[start+2]);
		return 3;
	}
}
