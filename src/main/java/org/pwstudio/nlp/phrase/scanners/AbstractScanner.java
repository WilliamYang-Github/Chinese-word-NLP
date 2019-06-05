package org.pwstudio.nlp.phrase.scanners;

import java.util.List;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.seg.common.Term;

/**
 * 文本规范化扫描器的抽象类
 * @author TylunasLi
 *
 */
public abstract class AbstractScanner {

    protected static final Term whether = new Term("是否", Nature.v);
	
	protected static final Term been = new Term("已经", Nature.d);
	
	protected static final Term can = new Term("能够", Nature.v);
	
	protected static final Term cannot = new Term("无法", Nature.v);

	/**
	 * @return 扫描器的尺寸
	 */
	public abstract int size();

	/**
	 * 
	 * @param sentence
	 * @param start
	 * @param outputPhrase
	 * @return
	 */
	public synchronized int runScanner(Term[] sentence, int start, List<Term> outputPhrase) {
		int matched = 0;
		if (activated(sentence,start))
			matched = transform(sentence,start,outputPhrase);
		return matched;
	}
	
	/**
	 * 是否激活当前的扫描器
	 * @param sentence
	 * @param start
	 * @return
	 */
	protected abstract boolean activated(Term[] sentence, int start);

	/**
	 * 激活后输出生成结果
	 * @param sentence
	 * @param start
	 * @param outputPhrase
	 * @return
	 */
	protected abstract int transform(Term[] sentence, int start, List<Term> outputPhrase);

	public static boolean isLe(Term term)
	{
	    return term.nature == Nature.ule || "了".equals(term.word); /* 76 */
	}

	public static boolean isYi(Term term)
	{
	    return "一".equals(term.word); /* 81 */
	}

}