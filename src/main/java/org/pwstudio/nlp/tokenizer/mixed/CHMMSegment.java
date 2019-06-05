package org.pwstudio.nlp.tokenizer.mixed;

import java.util.LinkedList;
import java.util.List;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.recognition.nr.JapanesePersonRecognition;
import org.pwstudio.nlp.recognition.nr.PersonRecognition;
import org.pwstudio.nlp.recognition.nr.TranslatedPersonRecognition;
import org.pwstudio.nlp.recognition.ns.PlaceRecognition;
import org.pwstudio.nlp.recognition.nt.OrganizationRecognition;
import org.pwstudio.nlp.seg.WordBasedSegment;
import org.pwstudio.nlp.seg.common.Term;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.seg.processor.*;

/**
 * 加入词法分析处理器的分词接口
 * 
 * @author TylunasLi
 *
 */
public class CHMMSegment extends WordBasedSegment {
    
    /** 预处理过程：尽可能多地识别特殊词语 */
    private List<PreProcessor> preProcess;
    /** 中间处理过程：在粗粉结果上进行处理 */
    private List<PostProcessor> middleProcess;
    /** 后处理过程：对不同粒度的要求合并词语 */
    private List<PostProcessor> postProcess;
    
    public CHMMSegment()
    {
        super();
        initProcessors();
    }

    /**
     * 加载分词结果处理组件
     */
    private void initProcessors()
    {
        preProcess = new LinkedList<PreProcessor>();
        //preProcess.add(NewWordDetector.getInstance());
        preProcess.add(TimeProcessor.TimeCodeProcessor);
        preProcess.add(URLPreProcessor.urlProcessor);
        preProcess.add(ChineseNumPreProcessor.instance);
        middleProcess = new LinkedList<PostProcessor>();
        middleProcess.add(NumberQuantifierProcessor.quantifierMerger);
        middleProcess.add(SpecialWordProcessor.codeMerger);
        middleProcess.add(SpecialWordProcessor.engNumMerger);
        //middleProcess.add(new TimeProcessor(dictionary));
        postProcess = new LinkedList<PostProcessor>();
        postProcess.add(TimeProcessor.timeMerger);
        postProcess.add(NumberQuantifierProcessor.numQuantifierMerger);
    }

	@Override
	protected List<Term> segSentence(char[] sentence) {
		WordNet wordNetAll = new WordNet(sentence);
		/** 生成词网 */
		generateWordNet(wordNetAll);
		if (PwNLP.Config.DEBUG) {
			System.out.printf("粗分词网：\n%s\n", wordNetAll);
		}
		/** 自动机构词预处理 */
		for (PreProcessor processor : preProcess)
		{
		    if (processor.isActive(config))
		        processor.recognizeWord(sentence, wordNetAll);
		}
		List<Vertex> vertexList = viterbi(wordNetAll);
		if (PwNLP.Config.DEBUG) {
			System.out.println("粗分结果" + convert(vertexList, false));
		}

		/** 用构词法处理结果 */
        Vertex[] vertexArray = new Vertex[vertexList.size()];
        vertexList.toArray(vertexArray);
        /** 合并数字和字符的编码 */
        for (PostProcessor processor : middleProcess) {
            if (processor.isActive(config))
                processor.processVertexes(vertexArray, wordNetAll);
        }

        /** 命名实体识别 */
		if (config.ner)
		{
			WordNet wordNetOptimum = new WordNet(sentence, vertexList);
			int preSize = wordNetOptimum.size();
			if (config.nameRecognize)
			{
				PersonRecognition.Recognition(vertexList, wordNetOptimum, wordNetAll);
			}
			if (config.translatedNameRecognize)
			{
				TranslatedPersonRecognition.recognition(vertexList, wordNetOptimum, wordNetAll);
			}
			if (config.japaneseNameRecognize)
			{
				JapanesePersonRecognition.recognition(vertexList, wordNetOptimum, wordNetAll);
			}
			if (config.placeRecognize)
			{
				PlaceRecognition.recognition(vertexList, wordNetOptimum, wordNetAll);
			}
			if (config.organizationRecognize)
			{
				// 层叠隐马模型——生成输出作为下一级隐马输入
				vertexList = viterbi(wordNetOptimum);
				wordNetOptimum.clear();
				wordNetOptimum.addAll(vertexList);
				preSize = wordNetOptimum.size();
				OrganizationRecognition.recognition(vertexList, wordNetOptimum, wordNetAll);
			}
			if (wordNetOptimum.size() != preSize)
			{
				vertexList = viterbi(wordNetOptimum);
				if (PwNLP.Config.DEBUG)
				{
					System.out.printf("细分词网：\n%s\n", wordNetOptimum);
				}
			}
		}
		
		if (config.useCustomDictionary) {
			if (config.indexMode > 0)
				combineByCustomDictionary(vertexList, wordNetAll);
			else combineByCustomDictionary(vertexList);
		}
        /** 数量合并、时间合并 */
        if (config.numberQuantifierRecognize || config.mergeTime) {
            vertexArray = new Vertex[vertexList.size()];
            vertexList.toArray(vertexArray);
            for (PostProcessor processor : postProcess) {
                if (processor.isActive(config)) 
                    processor.processVertexes(vertexArray, wordNetAll);
            }
            vertexList = PostProcessor.updateList(vertexArray, vertexList);
        }

		// 是否标注词性
		if (config.speechTagging) {
			speechTagging(vertexList);
		}
		vertexList = viterbi(wordNetAll);

		// 如果是索引模式则全切分
		if (config.indexMode > 0) {
			return decorateResultForIndexMode(vertexList, wordNetAll);
		}
		return convert(vertexList, config.offset);
	}

	private static List<Vertex> viterbi(WordNet wordNet)
    {
        // 避免生成对象，优化速度
        LinkedList<Vertex> nodes[] = wordNet.getVertexes();
        LinkedList<Vertex> vertexList = new LinkedList<Vertex>();
        for (Vertex node : nodes[1])
        {
            node.updateFrom(nodes[0].getFirst());
        }
        for (int i = 1; i < nodes.length - 1; ++i)
        {
            LinkedList<Vertex> nodeArray = nodes[i];
            if (nodeArray == null) continue;
            for (Vertex node : nodeArray)
            {
                if (node.from == null) continue;
                for (Vertex to : nodes[i + node.realWord.length()])
                {
                    to.updateFrom(node);
                }
            }
        }
        Vertex from = nodes[nodes.length - 1].getFirst();
        while (from != null)
        {
            vertexList.addFirst(from);
            from = from.from;
        }
        return vertexList;
    }

}
