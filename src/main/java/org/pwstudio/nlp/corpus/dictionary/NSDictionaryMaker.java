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
import org.pwstudio.nlp.corpus.tag.NS;
import org.pwstudio.nlp.corpus.util.CorpusUtil;
import org.pwstudio.nlp.corpus.util.Precompiler;
import org.pwstudio.nlp.tokenizer.StandardTokenizer;
import org.pwstudio.nlp.utility.Predefine;

import static org.pwstudio.nlp.utility.Predefine.logger;

/**
 * @author hankcs
 */
public class NSDictionaryMaker extends CommonDictionaryMaker
{
    public NSDictionaryMaker(EasyDictionary dictionary)
    {
        super(dictionary);
        StandardTokenizer.SEGMENT.enableAllNamedEntityRecognize(false);
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
        // 跳过没有实体的句子
        for (List<IWord> wordList : sentenceList)
        {
            boolean noEntity = true;
            for (IWord word : wordList)
            {
                if (!word.getLabel().equals(NS.Z.toString()) && !word.getLabel().equals(NS.S.toString()))
                    noEntity = false;
            }
            if (noEntity)
                continue;
            for (IWord word : wordList)
            {
//                if (!word.getLabel().equals(NS.Z.toString()))
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
    
    public void splitWithoutProperNS(List<IWord> wordList)
    {
        ListIterator<IWord> iterator = wordList.listIterator();
        while (iterator.hasNext())
        {
            IWord word = iterator.next();
            boolean split = true;
            if (word instanceof CompoundWord)
            {
                split = false;
                CompoundWord compound = (CompoundWord) word;
                if ("ns".equals(compound.getLabel()))
                {
                    for (Word inword : compound.innerList)
                    {
                        if (inword.getLabel().startsWith("ns"))
                        {
                            split = true;
                            break;
                        }
                    }
                }
                if (split)
                {
                    for (Word inword : compound.innerList)
                    {
                        iterator.add(inword);
                    }
                }
            }
        }
    }

    @Override
    protected void roleTag(List<List<IWord>> sentenceList)
    {
        int i = 0;
        for (List<IWord> wordList : sentenceList)
        {
            Precompiler.compileWithoutNS(wordList);
            if (verbose)
            {
                System.out.print(++i + " / " + sentenceList.size() + " ");
                System.out.println("原始语料 " + wordList);
            }
            LinkedList<IWord> wordLinkedList = (LinkedList<IWord>) wordList;
            wordLinkedList.addLast(new Word(Predefine.TAG_END, "Z"));
            if (verbose) System.out.println("添加首尾 " + wordList);
            // 标注上文
            Iterator<IWord> iterator = wordLinkedList.iterator();
            IWord pre = iterator.next();
            while (iterator.hasNext())
            {
                IWord current = iterator.next();
                if (current.getLabel().startsWith("ns") && !pre.getLabel().startsWith("ns"))
                {
                    pre.setLabel(NS.A.toString());
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
                if (current.getLabel().startsWith("ns") && !pre.getLabel().startsWith("ns"))
                {
                    pre.setLabel(NS.B.toString());
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
                if (first.getLabel().startsWith("ns") && third.getLabel().startsWith("ns") && !second.getLabel().startsWith("ns"))
                {
                    second.setLabel(NS.X.toString());
                }
                first = second;
                second = third;
            }
            if (verbose) System.out.println("标注中间 " + wordList);
            // 拆分地名
            splitWithoutProperNS(wordLinkedList);
            if (verbose) System.out.println("拆分地名 " + wordList);
            // 处理整个
            ListIterator<IWord> listIterator = wordLinkedList.listIterator();
            while (listIterator.hasNext())
            {
                IWord word = listIterator.next();
                String label = word.getLabel();
                if (label.equals(label.toUpperCase())) continue;
                if (label.startsWith("ns"))
                {
                    String value = word.getValue();
                    int longestSuffixLength = PlaceSuffixDictionary.dictionary.getLongestSuffixLength(value);
                    int wordLength = value.length() - longestSuffixLength;
                    if (longestSuffixLength == 0 || wordLength == 0)
                    {
                        word.setLabel(NS.G.toString());
                        continue;
                    }
                    listIterator.remove();
                    if (wordLength > 3)
                    {
                        listIterator.add(new Word(value.substring(0, wordLength), NS.G.toString()));
                        listIterator.add(new Word(value.substring(wordLength), NS.H.toString()));
                        continue;
                    }
                    for (int l = 1, tag = NS.C.ordinal(); l <= wordLength; ++l, ++tag)
                    {
                        listIterator.add(new Word(value.substring(l - 1, l), NS.values()[tag].toString()));
                    }
                    listIterator.add(new Word(value.substring(wordLength), NS.H.toString()));
                }
                else
                {
                    word.setLabel(NS.Z.toString());
                }
            }
            wordLinkedList.addFirst(new Word(Predefine.TAG_BIGIN, "S"));
            if (verbose) System.out.println("处理整个 " + wordList);
        }
    }
}
