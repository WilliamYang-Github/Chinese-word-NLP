/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/17 14:39</create-date>
 *
 * <copyright file="NSDictionaryMaker.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.dictionary;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.pwstudio.nlp.corpus.dictionary.item.Item;
import org.pwstudio.nlp.corpus.document.CorpusLoader;
import org.pwstudio.nlp.corpus.document.Document;
import org.pwstudio.nlp.corpus.document.sentence.word.CompoundWord;
import org.pwstudio.nlp.corpus.document.sentence.word.IWord;
import org.pwstudio.nlp.corpus.document.sentence.word.Word;
import org.pwstudio.nlp.corpus.tag.NT;
import org.pwstudio.nlp.corpus.tag.NTC;
import org.pwstudio.nlp.corpus.util.Precompiler;
import org.pwstudio.nlp.utility.Predefine;

/**
 * @author hankcs
 */
public class NTCDictionaryMaker extends CommonDictionaryMaker
{

    public NTCDictionaryMaker(EasyDictionary dictionary)
    {
        super(dictionary);
    }

    @Override
    public boolean saveTxtTo(String path)
    {
        // 将非全Z的词语保存下来
        DictionaryMaker.Filter filter = new DictionaryMaker.Filter()
        {
            @Override
            public boolean onSave(Item item)
            {
                if (item.getLabelCount() == 1 && item.firstLabel().equals("Z"))
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
//        logger.warning("开始制作词典");
        // 将非A的词语保存下来
        for (List<IWord> wordList : sentenceList)
        {
            boolean noEntity = true;
            for (IWord word : wordList)
            {
                if (!word.getLabel().equals(NTC.Z.name()) && !word.getLabel().equals(NTC.S.name()))
                    noEntity = false;
            }
            if (noEntity)
                continue;
            for (IWord word : wordList)
            {
//                if (!word.getLabel().equals(NTC.Z.toString()))
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
                }
                pre = word;
            }
        }
    }
    
    public boolean isConcernedWord(IWord word)
    {
        return word.getLabel().startsWith("ntc");
        //return word.getLabel().equals("nz");
    }

    @Override
    protected void roleTag(List<List<IWord>> sentenceList)
    {
        int i = 0;
        for (List<IWord> wordList : sentenceList)
        {
            Precompiler.compileWithoutNT(wordList);
            if (verbose)
            {
                System.out.print(++i + " / " + sentenceList.size() + " ");
                System.out.println("原始语料 " + wordList);
            }
            LinkedList<IWord> wordLinkedList = (LinkedList<IWord>) wordList;
            wordLinkedList.addFirst(new Word(Predefine.TAG_BIGIN, "S"));
            wordLinkedList.addLast(new Word(Predefine.TAG_END, "Z"));
            if (verbose) System.out.println("添加首尾 " + wordList);
            // 标注上文
            Iterator<IWord> iterator = wordLinkedList.iterator();
            IWord pre = iterator.next();
            while (iterator.hasNext())
            {
                IWord current = iterator.next();
                if (isConcernedWord(current) && !isConcernedWord(pre))
                {
                    pre.setLabel(NT.A.toString());
                }
                pre = current;
            }
            if (verbose) System.out.println("标注上文 " + wordList);
            // 标注下文
            iterator = wordLinkedList.descendingIterator();
            pre = iterator.next();
            while (iterator.hasNext())
            {
                IWord current = iterator.next();
                if (isConcernedWord(current) && !isConcernedWord(pre))
                {
                    pre.setLabel(NT.B.toString());
                }
                pre = current;
            }
            if (verbose) System.out.println("标注下文 " + wordList);
            // 标注中间
            iterator = wordLinkedList.iterator();
            IWord first = iterator.next();
            IWord second = iterator.next();
            while (iterator.hasNext())
            {
                IWord third = iterator.next();
                if (isConcernedWord(first) && isConcernedWord(third) && !isConcernedWord(second))
                {
                    second.setLabel(NT.X.toString());
                }
                first = second;
                second = third;
            }
            if (verbose) System.out.println("标注中间 " + wordList);
            // 处理整个
            ListIterator<IWord> listIterator = wordLinkedList.listIterator();
            while (listIterator.hasNext())
            {
                IWord word = listIterator.next();
                String label = word.getLabel();
                if (label.equals(label.toUpperCase())) continue;
                if (label.startsWith("ntc") || label.equals("nz"))
                {
                    StringBuilder sbPattern = new StringBuilder();
                    // 复杂机构
                    if (word instanceof CompoundWord)
                    {
                        listIterator.remove();
                        List<Word> innerWordList = ((CompoundWord) word).innerList;
                        List<Word> resultWordList = processCompondWord(innerWordList);
                        for (Word inner : resultWordList)
                        {
                            listIterator.add(inner);
                        }
                    }
                    else
                    {
                        //word.setLabel(NTC.L.toString());
                        String name = word.getValue();
                        switch (name.length())
                        {
                            case 2:
                            {
                                System.out.println("识别专名：FE " + name);
                                listIterator.add(new Word(word.getValue().substring(1, 2), NTC.E.name()));
                                word.setValue(word.getValue().substring(0, 1));
                                word.setLabel(NTC.F.name());
                            }break;
                            case 3:
                            {
                                System.out.println("识别专名： FME " + name);
                                listIterator.add(new Word(word.getValue().substring(1, 2), NTC.M.toString()));
                                listIterator.add(new Word(word.getValue().substring(2, 3), NTC.E.toString()));
                                word.setValue(word.getValue().substring(0, 1));
                                word.setLabel(NTC.F.toString());
                            }break;
                            case 4:
                            {
                                System.out.println("识别专名： FMME " + name);
                                listIterator.add(new Word(word.getValue().substring(1, 2), NTC.M.toString()));
                                listIterator.add(new Word(word.getValue().substring(2, 3), NTC.M.toString()));
                                listIterator.add(new Word(word.getValue().substring(3, 4), NTC.E.toString()));
                                word.setValue(word.getValue().substring(0, 1));
                                word.setLabel(NTC.F.toString());
                            }break;
                            default:
                            {
                                System.out.println("识别到一个暂时不能处理的姓名： " + name);
                                word.setLabel(NTC.L.toString());
                            }break;
                        }
                    }
                }
                else if ("nr".equals(label))
                {
                    /**
                     * 姓名非常容易和公司名混淆，
                     * 所以加入到模型里面避免误识别
                     */
                    String name = word.getValue();
                    for (int k=1; k<name.length()-1; k++)
                    {
                        listIterator.add(new Word(word.getValue().substring(k, k+1), NTC.Z.toString()));
                    }
                    word.setValue(name.substring(0, 1));
                    word.setLabel(NTC.Z.toString());
                }
                else
                {
                    word.setLabel(NTC.Z.toString());
                }
            }
            if (verbose) System.out.println("处理整个 " + wordList);
            wordLinkedList.getFirst().setLabel(NTC.S.toString());
        }
    }

    public List<Word> processCompondWord(List<Word> innerWordList)
    {
        List<Word> taggedList = new LinkedList<Word>();
        Word last = null;
        int ntclength = 0;
        NT status = NT.S;
        NTC curTag = NTC.Z;
        int cur = innerWordList.size() - 1;
        for (Word inner : innerWordList)
        {
            String innerLabel = inner.label;
            switch (status)
            {
                case S:
                {
                    if (innerLabel.startsWith("ns"))
                    {
                        inner.setValue(Predefine.TAG_PLACE);
                        status = NT.G;
                    }
                    else if (inner.length() == 1)
                    {
                        curTag = NTC.F;
                        status = NT.P;
                        ntclength += 1;
                    }
                    else if (inner.length() == 2 && inner.getLabel().equals("nz"))
                    {
                        curTag = NTC.C;
                        status = NT.I;
                        ntclength += 2;
                    }
                }
                break;
                case G:
                {
                    if (innerLabel.startsWith("ns"))
                    {
                        inner.setValue(Predefine.TAG_PLACE);
                        status = NT.G;
                    }
                    else if (inner.length() == 1)
                    {
                        curTag = NTC.F;
                        status = NT.P;
                        last.setLabel(NTC.A.name());
                        ntclength += 2;
                    }
                    else if (inner.length() == 2 && inner.getLabel().startsWith("nz"))
                    {
                        curTag = NTC.C;
                        status = NT.I;
                        last.setLabel(NTC.A.name());
                        ntclength += 2;
                    }
                }
                break;
                case P:
                {
                    if (dictionary.contains(inner.getValue()))
                    {
                        status = NT.C;
                        curTag = NTC.B;
                        if (last.getLabel().equals(NTC.C.name()))
                            last.setLabel(NTC.L.name());
                        else if (last.getLabel().equals(NTC.F.name()))
                            last.setLabel(NTC.L.name());
                    }
                    else
                    {
                        status = NT.I;
                        curTag = inner.length() == 1 ? NTC.M : NTC.I;
                        ntclength += inner.length();
                    }
                }
                break;
                case I:
                {
                    if ( (dictionary.contains(inner.getValue()) || ntclength >= 4)
                            || (ntclength == 3 && inner.length() >= 2) )
                    {
                        status = NT.C;
                        curTag = NTC.B;
                        if (last.getLabel().equals(NTC.C.name()))
                            last.setLabel(NTC.L.name());
                        else if (last.getLabel().equals(NTC.M.name()))
                            last.setLabel(NTC.E.name());
                        else if (last.getLabel().equals(NTC.I.name()))
                            last.setLabel(NTC.D.name());
                    }
                    else if (inner.length() == 2)
                    {
                        status = NT.J;
                        curTag = NTC.D;
                        ntclength += inner.length();
                    }
                }
                break;
                case J:
                {
                    status = NT.C;
                    curTag = NTC.B;
                }
                break;
                case C:
                {
                    curTag = NTC.Z;
                }
            }
            if (cur == 0 && last != null)
            {
                if (NTC.M.name().equals(last.getLabel()))
                {
                    last.setLabel(NTC.E.name());
                    curTag = NTC.B;
                }
                else if (NTC.A.name().equals(last.getLabel()))
                {
                    last.setLabel(NTC.Z.name());
                    curTag = NTC.Z;
                }
                else if (NTC.F.name().equals(last.getLabel()))
                {
                    curTag = NTC.E;
                }
            }
            inner.label = curTag.name();
            taggedList.add(inner);
            last = inner;
            cur--;
        }
        return taggedList; 
    }
}
