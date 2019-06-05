/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/18 20:21</create-date>
 *
 * <copyright file="Precompiler.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.util;

import java.util.List;

import org.pwstudio.nlp.corpus.dependency.CoNll.PosTagCompiler;
import org.pwstudio.nlp.corpus.document.sentence.word.IWord;
import org.pwstudio.nlp.corpus.document.sentence.word.Word;
import org.pwstudio.nlp.dictionary.other.CharType;
import org.pwstudio.nlp.utility.Predefine;
import org.pwstudio.nlp.utility.TextUtility;

/**
 * 预编译与反编译一些词语
 *
 * @author hankcs
 */
public class Precompiler
{
    /**
     * 将一个单词编译为等效字串
     *
     * @param word
     * @return
     */
    public static Word compile(Word word)
    {
        word.value = PosTagCompiler.compile(word.label, word.value);
//        switch (word.label)
//        {
//            case "ns":
//            case "nsf":
//            {
//                word.value = Predefine.TAG_PLACE;
//            }
//            break;
////            case "nz":
//            case "nx":
//            {
//                    word.value = Predefine.TAG_PROPER;
//            }
//            break;
//            case "nt":
//            case "ntc":
//            case "ntcf":
//            case "ntcb":
//            case "ntch":
//            case "nto":
//            case "ntu":
//            case "nts":
//            case "nth":
//            {
//                word.value = Predefine.TAG_GROUP;
//            }
//            break;
//            case "m":
//            case "mq":
//            {
//                word.value = Predefine.TAG_NUMBER;
//            }
//            break;
//            case "x":
//            {
//                word.value = Predefine.TAG_CLUSTER;
//            }
//            break;
//            case "xx":
//            {
//                word.value = Predefine.TAG_OTHER;
//            }
//            break;
//            case "t":
//            {
//                    word.value = Predefine.TAG_TIME;
//            }
//            break;
//            case "nr":
//            case "nrf":
//            {
//                word.value = Predefine.TAG_PEOPLE;
//            }
//            break;
//        }
        return word;
    }

    public static Word compile(IWord word)
    {
        return compile((Word)word);
    }

    /**
     * 面向姓名识别的预编译
     * @param wordList
     */
    public static void compileForNRRecognize(List<IWord> wordList)
    {
        for (IWord word : wordList)
        {
            if (isPureNumber(word, true) || isNumricTime(word, true))
            {
                word.setValue(PosTagCompiler.compile(word.getLabel(), word.getValue()));
            }
            else if (word.getLabel().equals("x"))
            {
                word.setValue(Predefine.TAG_CLUSTER);
            }
        }
    }

    /**
     * 在忽略ns的前提下预编译
     * @param wordList
     */
    public static void compileWithoutNS(List<IWord> wordList)
    {
        for (IWord word : wordList)
        {
            if (word.getLabel().startsWith("ns")) continue;
            word.setValue(PosTagCompiler.compile(word.getLabel(), word.getValue()));
//            switch (word.getLabel())
//            {
//                case "nx":
//                {
//                    word.setValue(Predefine.TAG_PROPER);
//                }
//                break;
//                case "nt":
//                case "ntc":
//                case "ntcf":
//                case "ntcb":
//                case "ntch":
//                case "nto":
//                case "ntu":
//                case "nts":
//                case "nth":
//                {
//                    word.setValue(Predefine.TAG_GROUP);
//                }
//                break;
//                case "m":
//                case "mq":
//                {
//                    word.setValue(Predefine.TAG_NUMBER);
//                }
//                break;
//                case "x":
//                {
//                    word.setValue(Predefine.TAG_CLUSTER);
//                }
//                break;
//                case "xx":
//                {
//                    word.setValue(Predefine.TAG_OTHER);
//                }
//                break;
//                case "t":
//                {
//                    word.setValue(Predefine.TAG_TIME);
//                }
//                break;
//                case "nr":
//                {
//                    word.setValue(Predefine.TAG_PEOPLE);
//                }
//                break;
//            }
        }
    }

    /**
     * 在忽略ns的前提下预编译
     * @param wordList
     */
    public static void compileWithoutNT(List<IWord> wordList)
    {
        for (IWord word : wordList)
        {
            if (word.getLabel().startsWith("nt")) continue;
            word.setValue(PosTagCompiler.compile(word.getLabel(), word.getValue()));
        }
    }
    
    /**
     * 检测一个词语的数字
     * @param word   词语
     * @param forNer 是不是实体识别的时候应当处理
     * @return
     */
    public static boolean isPureNumber(IWord word, boolean forNER)
    {
        if (word.getLabel().equals("a"))
            return false; 
        String wordStr = word.getValue();
        if (TextUtility.isAllNum(wordStr))
            return true;
        if (TextUtility.isAllChineseNum(wordStr))
        {
            if (!forNER)
                return true;
            return wordStr.length() == 1 ? false : true;
        }
        return false;
    }
    
    private final static String timeQuantitives = "年月份日号时点分秒周初:";
    
    /**
     * 判断一个词语是不是带有数字的时间词
     * @param word
     * @param forNer 是不是实体识别的时候应当处理
     * @return
     */
    public static boolean isNumricTime(IWord word, boolean forNER)
    {
        if (!word.getLabel().equals("t"))
            return false; 
        char[] chars = word.getValue().toCharArray();
        int numCount = 0;
        int keyCount = 0;
        for (int i=0; i<chars.length; i++)
        {
            if (CharType.CT_NUM == CharType.get(chars[i]))
                numCount++;
            if (!forNER && CharType.CT_CNUM == CharType.get(chars[i]))
                numCount++;
            if (timeQuantitives.indexOf(chars[i]) != -1)
                keyCount++;
        }
        if (numCount+keyCount == chars.length)
            return true;
        return false;
    }
}
