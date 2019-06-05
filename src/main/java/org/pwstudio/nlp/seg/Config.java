/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/30 10:06</create-date>
 *
 * <copyright file="Config.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.seg;

/**
 * 分词器配置项
 */
public class Config
{
    /**
     * 是否是索引分词（合理地最小分割），indexMode代表全切分词语的最小长度（包含）
     */
    public int indexMode = 0;
    /**
     * 是否识别中国人名
     */
    public boolean nameRecognize = true;
    /**
     * 是否识别音译人名
     */
    public boolean translatedNameRecognize = true;
    /**
     * 是否识别日本人名
     */
    public boolean japaneseNameRecognize = false;
    /**
     * 是否识别地名
     */
    public boolean placeRecognize = false;
    /**
     * 是否识别机构
     */
    public boolean organizationRecognize = false;
    /**
     * 是否加载用户词典
     */
    public boolean useCustomDictionary = true;
    /**
     * 用户词典高优先级
     */
    public boolean forceCustomDictionary = false;
    /**
     * 词性标注
     */
    public boolean speechTagging = true;
    /**
     * 命名实体识别是否至少有一项被激活
     */
    public boolean ner = true;
    /**
     * 是否计算偏移量
     */
    public boolean offset = true;
    /**
     * 是否识别数字和量词
     */
    public boolean numberQuantifierRecognize = false;
    /**
     * 是否识别数字型时间词 <br>
     * 根据分词和标注规范，默认合并带数字的时间
     */
    public boolean timeRecognize = true;
    /**
     * 是否识别并合并时间词 <br>
     * 默认关闭。
     */
    public boolean mergeTime = false;
    /**
     * 是否使用合并代号<br>
     * 如果开启，会合并“英文+数字”或“英文+连字符+数字”。
     */
    public boolean mergeCode = true;
    /** 
     * 是否对所有连续的英文和数字进行合并<br>
     * 如果开启，会合并所有的英文和数字。
     */
    public boolean mergeLetterAndNumbers = false;
    /**
     * 空格保留方式<br>
     * 0 - 保留全部空格不处理，默认<br>
     * 1 - 多个空格保留一个<br>
     * 2 - 去除字母和数字之间的空格并连接为一个单词<br>
     */
    public int spaceMode = 0;
    /**
     * 是否开启新词语探测<br>
     * 如果开启，会采用CRF模型，尝试对不确定性部分进行分词、<br>
     * 性能会略有下降
     */
    public boolean newWordDetect = false;
    /**
     * 新词语探测范围参数，1-100<br>
     * 双字对在语言模型中评分比例，越高则越不可能分开。<br>
     * 数值越小。性能下降越大，且效果也变差
     */
    public int detectRate = 91;
    /**
     * 是否合并基本短语<br>
     * 如果开启，会调用短语词典。合并短语。
     */
    public boolean mergePhrase = false;
    /**
     * 并行分词的线程数
     */
    public int threadNumber = 1;

    /**
     * 更新命名实体识别总开关
     */
    public void updateNerConfig()
    {
        ner = nameRecognize || translatedNameRecognize || japaneseNameRecognize || placeRecognize || organizationRecognize;
    }

    /**
     * 是否是索引模式
     *
     * @return
     */
    public boolean isIndexMode()
    {
        return indexMode > 0;
    }
}
