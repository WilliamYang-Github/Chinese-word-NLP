package org.pwstudio.nlp.dictionary.base;

import org.pwstudio.nlp.collection.trie.DoubleArrayTrie;
import org.pwstudio.nlp.dictionary.CoreDictionary.Attribute;

/**
 * 词语属性词典，采用DAT数组
 * @origianlAuthor hankcs
 * @author TylunasLi
 */
public class AttributeDictionary
{
	/**
	 * 核心DAT词典
	 */
	DoubleArrayTrie<Attribute> trie;
	/**
	 * 一阶词语的总频次
	 */
	int totalFrequency;

	public AttributeDictionary(DoubleArrayTrie<Attribute> coreTrie,
			int totalFrequency)
	{
		this.trie = coreTrie;
		this.totalFrequency = totalFrequency;
	}

	/**
	 * 访问Trie树
	 * @return
	 */
	public DoubleArrayTrie<Attribute> getTrie()
	{
		return this.trie;
	}

	/**
	 * 获取词语的总频率
	 * @return
	 */
	public int getTotalFrequency()
	{
		return this.totalFrequency;
	}

	/**
	 * 获取词语属性
	 * @param key 词语
	 * @return
	 */
	public Attribute get(String key)
	{
		return this.trie.get(key);
	}

	/**
	 * 根据ID获取词语属性
	 * @param wordID
	 * @return
	 */
	public Attribute get(int wordID)
	{
		return this.trie.get(wordID);
	}

    /**
     * 是否包含词语
     * @param key
     * @return
     */
	public boolean contains(String key)
	{
		return this.trie.exactMatchSearch(key) != -1;
	}

    /**
     * 获取某个词语的词性
     * @param key
     * @return
     */
	public int getTermFrequency(String term)
	{
		Attribute attribute = this.get(term);
		return attribute == null ? 0 : attribute.totalFrequency;
	}

    /**
     * 获取词语的ID
     * @param a 词语
     * @return ID,如果不存在,则返回-1
     */
	public int getWordID(String word)
	{
		return this.trie.exactMatchSearch(word);
	}

	/**
	 * 获取词典中词语的总数
	 * @return
	 */
	public int count()
	{
		return this.trie.size();
	}

}
