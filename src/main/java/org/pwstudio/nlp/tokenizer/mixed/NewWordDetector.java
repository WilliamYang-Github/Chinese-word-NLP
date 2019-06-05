package org.pwstudio.nlp.tokenizer.mixed;

import java.util.List;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.dictionary.LanguageModel;
import org.pwstudio.nlp.seg.CharacterBasedSegment;
import org.pwstudio.nlp.seg.CRF.CRFSegment;
import org.pwstudio.nlp.seg.common.Term;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.seg.processor.PreProcessor;

/**
 * 将基于字标注的新词发现结果以最低概率加入到词网中
 * 
 * @author TylunasLi
 *
 */
public class NewWordDetector extends PreProcessor {

    private CharacterBasedSegment newWordSegment;

    private static NewWordDetector instance;

    public static NewWordDetector getInstance()
    {
        if (instance == null)
            instance = new NewWordDetector();
        return instance;
    }
    
    private NewWordDetector()
    {
        this.newWordSegment = new CRFSegment();
        this.newWordSegment.enableAllNamedEntityRecognize(false)
                .enableIndexMode(false).enableOffset(true)
                .enableCustomDictionary(false).enablePartOfSpeechTagging(false);
    }

    @Override
    public void recognizeWord(char[] sentence, WordNet wordNetAll)
    {
        List<Term> terms = newWordSegment.seg(wordNetAll.charArray);
        for (Term term : terms)
        {
            Vertex old = wordNetAll.get(term.offset, term.word.length());
            if (old == null)
            {
                Vertex vertex = new Vertex(term.word, term.word,
                        CoreDictionary.get(CoreDictionary.NX_WORD_ID),
                        CoreDictionary.NX_WORD_ID);
                wordNetAll.add(term.offset + 1, vertex);
            }
        }
    }

    @Override
    protected Nature getNature() {
        return Nature.nx;
    }

}
