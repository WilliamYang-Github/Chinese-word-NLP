/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/9 14:46</create-date>
 *
 * <copyright file="NRDictionaryMaker.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.dictionary;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.pwstudio.nlp.corpus.dictionary.item.Item;
import org.pwstudio.nlp.corpus.document.CorpusLoader;
import org.pwstudio.nlp.corpus.document.Document;
import org.pwstudio.nlp.corpus.document.sentence.word.IWord;
import org.pwstudio.nlp.corpus.document.sentence.word.Word;
import org.pwstudio.nlp.corpus.tag.NR;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.corpus.util.Precompiler;
import org.pwstudio.nlp.utility.Predefine;

import static org.pwstudio.nlp.utility.Predefine.logger;

/**
 * nr词典（词典+ngram转移+词性转移矩阵）制作工具
 * @author hankcs
 */
public class NRDictionaryMaker extends CommonDictionaryMaker
{

    static final String nr = Nature.nr.toString();
    
    public NRDictionaryMaker(EasyDictionary dictionary)
    {
        super(dictionary);
    }

    @Override
    public boolean saveTxtTo(String path)
    {
        // 将非全A的词语保存下来
        DictionaryMaker.Filter filter = new DictionaryMaker.Filter()
        {
            @Override
            public boolean onSave(Item item)
            {
                if (item.getLabelCount() == 1 && item.firstLabel().equals("A"))
                    return false;
                return true;
            }
        };
        if (dictionaryMaker.saveTxtTo(path + ".txt", filter))
        {
            if (nGramDictionaryMaker.saveTxtTo(path))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void addToDictionary(List<List<IWord>> sentenceList)
    {
    //    logger.warning("开始制作词典");
        for (List<IWord> wordList : sentenceList)
        {
            // 跳过没有实体的句子
            boolean noEntity = true;
            for (IWord word : wordList)
            {
                if (!word.getLabel().equals(NR.A.toString()) && !word.getLabel().equals(NR.S.toString()))
                    noEntity = false;
            }
            if (noEntity)
                continue;
            for (IWord word : wordList)
            {
//                if (!word.getLabel().equals(NR.A.toString()))
//                {
                    dictionaryMaker.add(word);
//                }
            }
//        }
        // 制作NGram词典
//        for (List<IWord> wordList : sentenceList)
//        {
            IWord pre = null;
            for (IWord word : wordList)
            {
                if (pre != null)
                {
                    nGramDictionaryMaker.addPair(pre, word);
                    if (("L".equals(pre.getLabel()) || "B".equals(pre.getLabel())) && "B".equals(word.getLabel())) 
                        System.out.println("语料 " + wordList);
                }
                pre = word;
            }
        }
    }
    
    protected static boolean isFamilyName(IWord word)
    {
        return (Nature.nr1.name().equals(word.getLabel()));
    }

    protected static boolean isPersonTitle(IWord word)
    {
        if (word.getLabel().startsWith("nn"))
            return true;
        if ("先生".equals(word.getValue()) || "小姐".equals(word.getValue())   
                || "大爷".equals(word.getValue()) || "大妈".equals(word.getValue())
                || "女士".equals(word.getValue()) || "夫人".equals(word.getValue()) )
            return true;
        return false;
    }

    @Override
    protected void roleTag(List<List<IWord>> sentenceList)
    {
        logger.info("开始标注角色");
        int i = 0;
        for (List<IWord> wordList : sentenceList)
        {
            logger.info(++i + " / " + sentenceList.size());
            if (wordList.isEmpty()) continue;
            if (verbose) System.out.println("原始语料 " + wordList);
            Precompiler.compileForNRRecognize(wordList);
            LinkedList<IWord> wordLinkedList = (LinkedList<IWord>) wordList;
            wordLinkedList.addLast(new Word(Predefine.TAG_END, "A"));
            // 先标注K
            IWord pre = new Word("##始##", "begin");
            ListIterator<IWord> listIterator = wordLinkedList.listIterator();
            while (listIterator.hasNext())
            {
                IWord word = listIterator.next();
                if (nr.equals(word.getLabel()))
//                {
//                    word.setLabel(NR.A.name());
//                }
//                else
                {
                    if (!nr.equals(pre.getLabel()))
                    {
                        pre.setLabel(NR.K.name());
                    }
                }
                pre = word;
            }
            if (verbose) System.out.println("标注上文 " + wordList);
            // 然后标注L、M
            IWord next = listIterator.previous();
            while (listIterator.hasPrevious())
            {
                IWord word = listIterator.previous();
                if (nr.equals(word.getLabel()))
                {
                    String label = next.getLabel();
                    if (label.equals(NR.K.name()))
                        next.setLabel(NR.M.name());
                    else if (!label.equals(nr) && !isFamilyName(next))
                        next.setLabel(NR.L.name());
                }
                next = word;
            }
            if (verbose) System.out.println("标注中后 " + wordList);
            // 标注指称
            listIterator = wordLinkedList.listIterator();
            IWord first = listIterator.next();
            IWord second = listIterator.next();
            while (listIterator.hasNext())
            {
                IWord third = listIterator.next();
                if (!nr.equals(first.getLabel()) && isFamilyName(second) && isPersonTitle(third))
                {
                    if (first.getLabel().equals(NR.K.name()))
                        first.setLabel(NR.M.toString());
                    else
                        first.setLabel(NR.K.toString());
                }
                if (isFamilyName(first) && isPersonTitle(second))
                {
                //    verbose = true;
                    first.setLabel(NR.B.toString());
                    second.setLabel(NR.G.toString());
                    if (!nr.equals(third.getLabel()))
                    {
                        if (third.getLabel().equals(NR.K.name()))
                            third.setLabel(NR.M.toString());
                        else
                            third.setLabel(NR.L.toString());
                    }
                }
                first = second;
                second = third;
            }
            if (verbose) System.out.println("标注指称 " + wordList);
            //verbose = false;
            // 拆分名字
            boolean inTitle = false;
            listIterator = wordList.listIterator();
            while (listIterator.hasNext())
            {
                IWord word = listIterator.next();
                String label = word.getLabel();
                if (label.equals(label.toUpperCase())) continue;
                if (nr.equals(label))
                {
                    String name = word.getValue();
                    switch (name.length())
                    {
                        case 2:
                            if (name.startsWith("大")
                                    || name.startsWith("老")
                                    || name.startsWith("小")
                                    )
                            {
                                System.out.println("识别姓名： FB " + name);
                                listIterator.add(new Word(word.getValue().substring(1, 2), NR.B.toString()));
                                word.setValue(word.getValue().substring(0, 1));
                                word.setLabel(NR.F.toString());
                            }
                            else if (name.endsWith("哥") || name.endsWith("姐") 
                                    || name.endsWith("氏") || name.endsWith("姓")
                                    || name.endsWith("公") || name.endsWith("老")
                                    || name.endsWith("某") || name.endsWith("少")
                                    || name.endsWith("嫂") || name.endsWith("妈")
                                    || name.endsWith("总") || name.endsWith("导")
                                    )
                            {
                                System.out.println("识别姓名： BG " + name);
                                listIterator.add(new Word(word.getValue().substring(1, 2), NR.G.toString()));
                                word.setValue(word.getValue().substring(0, 1));
                                word.setLabel(NR.B.toString());
                            }
                            else if (dictionary.contains(name))
                            {
                                System.out.println("识别姓名： Y " + name);
                                word.setLabel(NR.Y.toString());
                            }
                            else
                            {
                                System.out.println("识别姓名： BE " + name);
                                listIterator.add(new Word(word.getValue().substring(1, 2), NR.E.toString()));
                                word.setValue(word.getValue().substring(0, 1));
                                word.setLabel(NR.B.toString());
                            }
                            break;
                        case 3:
                            if (name.endsWith("某某")
                                    || name.endsWith("先生") || name.endsWith("小姐")
                                    || name.endsWith("总理") || name.endsWith("主席"))
                            {
                                System.out.println("识别姓名： BG " + name);
                                listIterator.add(new Word(word.getValue().substring(1, 3), NR.G.toString()));
                                word.setValue(word.getValue().substring(0, 1));
                                word.setLabel(NR.B.toString());
                            }
                            else
                            {
                                System.out.println("识别姓名： BCD " + name);
                                listIterator.add(new Word(word.getValue().substring(1, 2), NR.C.toString()));
                                listIterator.add(new Word(word.getValue().substring(2, 3), NR.D.toString()));
                                word.setValue(word.getValue().substring(0, 1));
                                word.setLabel(NR.B.toString());
                            }
                            break;
                        default:
                            System.out.println("识别到一个暂时不能处理的姓名： " + name);
                            word.setLabel(NR.Y.toString());
                            break;
                    }
                }
                else
                {
                    word.setLabel(NR.A.name());
                }
            }
            if (verbose) System.out.println("姓名拆分 " + wordList);
            // 上文成词
            listIterator = wordList.listIterator();
            pre = new Word("##始##", "begin");
            while (listIterator.hasNext())
            {
                IWord word = listIterator.next();
                if (word.getLabel().equals(NR.B.name()))
                {
                    String combine = pre.getValue() + word.getValue();
                    if (dictionary.contains(combine))
                    {
                    //    verbose = true;
                        pre.setValue(combine);
                        pre.setLabel("U");
                        listIterator.remove();
                    }
                }
                pre = word;
            }
            if (verbose) System.out.println("上文成词 " + wordList);
            //verbose = false;
            // 头部成词
            next = new Word("##末##", "end");
            while (listIterator.hasPrevious())
            {
                IWord word = listIterator.previous();
                if (word.getLabel().equals(NR.B.name()))
                {
                    String combine = word.getValue() + next.getValue();
                    if (dictionary.contains(combine))
                    {
                    //    verbose = true;
                        next.setValue(combine);
                        next.setLabel(next.getLabel().equals(NR.C.toString()) ? NR.X.toString() : NR.Y.toString());
                        listIterator.remove();
                    }
                }
                next = word;
            }
            if (verbose) System.out.println("头部成词 " + wordList);
            //verbose = false;
            // 尾部成词
            pre = new Word("##始##", "begin");
            while (listIterator.hasNext())
            {
                IWord word = listIterator.next();
                if (word.getLabel().equals(NR.D.name()))
                {
                    String combine = pre.getValue() + word.getValue();
                    if (dictionary.contains(combine))
                    {
                    //    verbose = true;
                        pre.setValue(combine);
                        pre.setLabel(NR.Z.name());
                        listIterator.remove();
                    }
                }
                pre = word;
            }
            if (verbose) System.out.println("尾部成词 " + wordList);
            //verbose = false;
            // 下文成词
            next = new Word("##末##", "end");
            while (listIterator.hasPrevious())
            {
                IWord word = listIterator.previous();
                if (word.getLabel().equals(NR.D.name()))
                {
                    String combine = word.getValue() + next.getValue();
                    if (dictionary.contains(combine))
                    {
                    //    verbose = true;
                        next.setValue(combine);
                        next.setLabel(NR.V.name());
                        listIterator.remove();
                    }
                }
                next = word;
            }
            if (verbose) System.out.println("下文成词 " + wordList);
            //verbose = false;
            wordLinkedList.addFirst(new Word(Predefine.TAG_BIGIN, "S"));
            if (verbose) System.out.println("添加首尾 " + wordList); // || hitname
        }
    }
}
