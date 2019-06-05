/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/12/10 0:23</create-date>
 *
 * <copyright file="ISaveAble.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.io;

import java.io.DataOutputStream;

/**
 * 可写入或读取二进制
 * @author hankcs
 */
public interface ICacheAble
{
    /**
     * 写入
     * @param out
     * @throws Exception
     */
    void save(DataOutputStream out) throws Exception;

    /**
     * 载入
     * @param byteArray
     * @return
     */
    boolean load(ByteArray byteArray); // 目前的设计并不好，应该抛异常而不是返回布尔值
}
