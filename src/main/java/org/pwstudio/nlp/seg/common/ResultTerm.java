/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2015/2/14 0:05</create-date>
 *
 * <copyright file="ResultTerm.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.seg.common;

/**
 * 一个通用的Term
* @author hankcs
*/
public class ResultTerm<V>
{
    public String word;
    public V label;
    public int offset;

    public ResultTerm(String word, V label, int offset)
    {
        this.word = word;
        this.label = label;
        this.offset = offset;
    }

    @Override
    public String toString()
    {
        return word + '/' + label;
    }
}
