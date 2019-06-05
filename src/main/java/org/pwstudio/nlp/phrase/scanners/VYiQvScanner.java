package org.pwstudio.nlp.phrase.scanners;

import java.util.List;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.seg.common.Term;

public class VYiQvScanner extends AbstractScanner {

    public static VYiQvScanner instance = new VYiQvScanner();
    private int size;

    public VYiQvScanner() {
        this.size = 2;
    }
    
    @Override
    public int size() {
        return 2;
    }

    @Override
    protected boolean activated(Term[] sentence, int start) {
        /*SL:25*/this.size = 3;
        Term base = /*EL:26*/sentence[start];
        /*SL:27*/if (!base.nature.startsWith('v'))
            /*SL:28*/return false;
        
        /*SL:31*/if (sentence.length - start >= 3)
        {
            if (isYi(sentence[start + 1]) && (isXia(sentence[start + 2]) || this.isVerbQuantifier(sentence[start + 2])))
            /*SL:34*/this.size = 3;
            /*SL:35*/return true;
        }
        /*SL:38*/if ("一下".equals(sentence[start + 1].word) || isXia(sentence[start + 1])) {
            /*SL:40*/this.size = 2;
            /*SL:41*/return true;
        }
        /*SL:43*/return false;
    }

    @Override
    protected int transform(Term[] sentence, int start, List<Term> outputPhrase) {
        outputPhrase.add(sentence[start]); /* 59 */
        return this.size; /* 60 */
    }

    
    public static boolean isXia(Term term) {
        /*SL:48*/return term.word.equals("下");
    }
    
    public boolean isVerbQuantifier(Term term) {
        /*SL:53*/return term.nature == Nature.qv;
    }
    
}
