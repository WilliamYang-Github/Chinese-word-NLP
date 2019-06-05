package org.pwstudio.nlp.phrase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.phrase.scanners.ANAScanner;
import org.pwstudio.nlp.phrase.scanners.AbstractScanner;
import org.pwstudio.nlp.phrase.scanners.VLeVScanner;
import org.pwstudio.nlp.phrase.scanners.VLiaoScanner;
import org.pwstudio.nlp.phrase.scanners.VMeiVScanner;
import org.pwstudio.nlp.phrase.scanners.VVScanner;
import org.pwstudio.nlp.phrase.scanners.VYiQvScanner;
import org.pwstudio.nlp.seg.common.Term;

/**
 * 
 * @author TylunasLi
 *
 */
public class PhraseNormalizer {

	protected List<AbstractScanner> scanners;
	
	public static final Term begin = new Term("始##始", Nature.begin); /* 26 */
	public static final Term end = new Term("末##末", Nature.end); /* 27 */

	public static PhraseNormalizer createNormalizer()
	{
	    PhraseNormalizer instance = new PhraseNormalizer(); /* 31 */
	    instance.addScanner(ANAScanner.instance).addScanner(VLeVScanner.instance)
	            .addScanner(VMeiVScanner.instance).addScanner(VLiaoScanner.instance)
	            .addScanner(VVScanner.instance).addScanner(VYiQvScanner.instance); /* 32 */
	    return instance; /* 35 */
	}

	public PhraseNormalizer addScanner(AbstractScanner scanner)
	{
	    this.scanners.add(scanner); /* 47 */
	    return this; /* 48 */
	}

	public List normalizeSentence(List<Term> inputSentence)
	{
	    return this.normalizeSentence(inputSentence, false, true); /* 53 */
	}

	public List normalizeSentence(List<Term> inputSentence, boolean recordOrder, boolean firstLayer)
	{
	    List<Term> result = new ArrayList<Term>(inputSentence.size() + 2); /* 59 */
	    Term[] sentence = new Term[inputSentence.size() + 2]; /* 60 */
	    sentence[0] = begin; /* 61 */
	    for(int i = 0; i < inputSentence.size(); ++i) /* 62 */
	        sentence[i + 1] = inputSentence.get(i); /* 63 */

	    sentence[sentence.length - 1] = end; /* 65 */
	    LinkedList<Term> outputPhrase = new LinkedList<Term>(); /* 66 */
	    boolean hit = false; /* 67 */
	    int start = 0; /* 69 */

        while(start < sentence.length)
        {
            for (AbstractScanner scanner : this.scanners) /* 71 */
            {
                outputPhrase.clear(); /* 73 */
                if (start + scanner.size() <= sentence.length) /* 74 */
                {
                    int processed = scanner.runScanner(sentence, start, outputPhrase); /* 76 */
                    if (processed > 0) /* 77 */
                    {
                        hit = true; /* 79 */
                        for(int i = 0; i < outputPhrase.size(); ++i) { /* 80 */
                            Term term = outputPhrase.get(i); /* 81 */
                            if (recordOrder) { /* 82 */
                                term = new Term(term.word, term.nature); /* 83 */
                                term.offset = start + i; /* 84 */
                            }
                            result.add(term); /* 86 */
                        }
                        start += processed; /* 88 */
                        break; /* 89 */
                    }
                }
            }
            if (!hit) /* 93 */
            {
                if (recordOrder && firstLayer) /* 95 */
                    sentence[start].offset = start; /* 96 */

                result.add(sentence[start]); /* 98 */
                ++start; /* 99 */
            }
            hit = false; /* 101 */
        }
        return result.subList(1, result.size() - 1); /* 103 */
	}


}
