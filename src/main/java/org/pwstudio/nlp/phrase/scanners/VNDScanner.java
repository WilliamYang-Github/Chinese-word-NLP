package org.pwstudio.nlp.phrase.scanners;

import java.util.List;

import org.pwstudio.nlp.seg.common.Term;

/**
 * 处理 `"v/ 得/ /d" "/v 不/ /d" 格式
 * @author TylunasLi
 *
 */
public class VNDScanner extends AbstractScanner {

	@Override
	public int size()
	{
		return 3;
	}

	@Override
	protected boolean activated(Term[] sentence, int start) {
		// TODO 自动生成的方法存根
		return false;
	}

	@Override
	protected int transform(Term[] sentence, int start, List<Term> outputPhrase) {
		// TODO 自动生成的方法存根
		return 2;
	}

}
