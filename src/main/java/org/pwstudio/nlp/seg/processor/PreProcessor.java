package org.pwstudio.nlp.seg.processor;

import java.util.List;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.LanguageModel;
import org.pwstudio.nlp.seg.Config;
import org.pwstudio.nlp.seg.common.WordNet;

/**
 * 文本预处理器<br>
 * 获取并预处理文本中的一些词语，加入词网中
 * @author TylunasLi
 *
 */
public abstract class PreProcessor {
	
	/**
	 * 判断是否激活当前的后处理器
	 * @param config
	 * @return
	 */
	public boolean isActive(Config config) {
		return false;
	}

	protected abstract Nature getNature();
	
	/** 
	 * 根据一些规则，预先识别某些词语，加入词网
	 * @param sentence
	 * @param wordNetAll
	 */
	public abstract void recognizeWord(char[] sentence, WordNet wordNetAll);

}
