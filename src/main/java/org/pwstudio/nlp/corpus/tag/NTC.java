/*
 * <summary></summary>
 * <author>TylunasLi</author>
 * <email>pwstudio@163.com</email>
 *
 * <copyright file="NTC.java" company="南京云问网络科技有限公司">
 * Copyright (c) 2003-2014, 南京云问网络科技有限公司. All Right Reserved, http://www.iyunwen.com/
 * This source is subject to the Apache License 2.0.
 * </copyright>
 */
package org.pwstudio.nlp.corpus.tag;

/**
 * 企业字号识别应用的标记
 * @author TylunasLi
 *
 */
public enum NTC {

    /**
     * 字号的上文
     */
    A,
    /**
     * 字号的下文
     */
    B,
    /**
     * 字号的首字
     */
    F,   
    /**
     * 字号的中间字
     */
    M,
    /**、
     * 字号的尾字
     */
    E,   
    /**
     * 字号首字和中间字成词
     */
    C,
    /**
     * 字号中间字和尾字成词
     */
    D,
    /**
     * 字号中间两字成词语
     */
    I,
    /**
     * 字号是一个独立的词语
     */
    L,
    /**
     * 句子开头
     */
    S,
    /**
     * 其他非字号成分
     */
    Z

}
