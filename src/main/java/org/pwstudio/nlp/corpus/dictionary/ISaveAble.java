/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/9 20:55</create-date>
 *
 * <copyright file="ISaveAble.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.dictionary;

/**
 * @author hankcs
 */
public interface ISaveAble
{
    /**
     * 将自己以文本文档的方式保存到磁盘
     * @param path 保存位置，包含文件名，不一定包含后缀
     * @return 是否成功
     */
    public boolean saveTxtTo(String path);
}
