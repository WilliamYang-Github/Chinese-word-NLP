/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/17 16:07</create-date>
 *
 * <copyright file="NS.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.tag;

/**
 * 地名角色标签
 *
 * @author hankcs
 */
public enum NS
{
    /**
     * 地名的上文 我【来到】中关园
     */
    A,
    /**
     * 地名的下文 刘家村/和/下岸村/【相邻】
     */
    B,
    /**
     * 中国地名的第一个字
     */
    C,
    /**
     * 中国地名的第二个字
     */
    D,
    /**
     * 中国地名的第三个字
     */
    E,
    /**
     * 方位词
     */
//    F,
    /**
     * 带有行政区划后缀的完整地名【上海市】
     */
    G,
    /**
     * 中国地名的后缀海淀/【区】
     */
    H,
    /**
     * 不带行政区划后缀的整个地名【上海】
     */
//    P,
    /**
     * 连接词刘家村/【和】/下岸村/相邻
     */
    X,
    /**
     * 其它非地名成分
     */
    Z,

    /**
     * 句子的开头
     */
    S,
}
