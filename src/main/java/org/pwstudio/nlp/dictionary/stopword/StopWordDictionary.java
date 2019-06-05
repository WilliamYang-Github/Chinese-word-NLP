/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/15 19:38</create-date>
 *
 * <copyright file="StopwordDictionary.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.dictionary.stopword;

import java.io.*;
import java.util.Collection;

import org.pwstudio.nlp.collection.MDAG.MDAGSet;
import org.pwstudio.nlp.dictionary.common.CommonDictionary;
import org.pwstudio.nlp.seg.common.Term;

import static org.pwstudio.nlp.utility.Predefine.logger;

/**
 * @author hankcs
 */
public class StopWordDictionary extends MDAGSet implements Filter
{
    public StopWordDictionary(File file) throws IOException
    {
        super(file);
    }

    public StopWordDictionary(Collection<String> strCollection)
    {
        super(strCollection);
    }

    public StopWordDictionary()
    {
    }

    public StopWordDictionary(String stopWordDictionaryPath) throws IOException
    {
        super(stopWordDictionaryPath);
    }

    @Override
    public boolean shouldInclude(Term term)
    {
        return contains(term.word);
    }
}
