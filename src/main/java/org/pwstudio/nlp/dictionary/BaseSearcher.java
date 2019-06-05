/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/5/10 12:10</create-date>
 *
 * <copyright file="Searcher.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.dictionary;
import java.util.Map;

/**
 * 查询字典者
 * @author He Han
 */
public abstract class BaseSearcher<V>
{
    /**
     * 待分词文本的char
     */
    protected char[] c;
    /**
     * 指向当前处理字串的开始位置（前面的已经分词分完了）
     */
    protected int offset;

    protected BaseSearcher(char[] c)
    {
        this.c = c;
    }

    protected BaseSearcher(String text)
    {
        this(text.toCharArray());
    }

    /**
     * 分出下一个词
     * @return
     */
    public abstract Map.Entry<String, V> next();

    /**
     * 获取当前偏移
     * @return
     */
    public int getOffset()
    {
        return offset;
    }
}
