package org.pwstudio.nlp.model.segment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pwstudio.nlp.corpus.document.sentence.word.Word;
import org.pwstudio.nlp.dictionary.other.CharTable;
import org.pwstudio.nlp.dictionary.other.CharType;


/**
 * 文本分析
 * @author TylunasLi
 *
 */
public class CharBasedSegmentTagger
{
	public static final String english = "W";
	public static final String number = "M";
    public static final String[] tags = new String[]{"B", "M", "E", "S"};
    
    static byte[] type;

    static {
    	type = new byte[65536];
    	System.arraycopy(CharType.type,0,type,0,65536);
    	for (char ch : "零○〇一二两三四五六七八九十廿百千万亿壹贰叁肆伍陆柒捌玖拾佰仟".toCharArray())
    		type[ch] = CharType.CT_CHINESE;
    	for (int ch='０'; ch <= '９'; ch++)
    		type[ch] = CharType.CT_NUM;
		for (int ch='A'; ch <= 'Z'; ch++)
			type[ch] = CharType.CT_LETTER;
		for (int ch='a'; ch <= 'z'; ch++)
			type[ch] = CharType.CT_LETTER;
		for (int ch='Ａ'; ch <= 'Ｚ'; ch++)
			type[ch] = CharType.CT_LETTER;
		for (int ch='ａ'; ch <= 'ｚ'; ch++)
			type[ch] = CharType.CT_LETTER;
	}

    /**
     * 将文本切分为CharBasedModel所需要的待标记序列
     * @param text 训练时是一个词，标记时传入句子
     * @return
     */
    public static String[][] atomSegment(char[] text)
    {
    	// 采用正规化以后的字符串作为特征
    	char[] textConverted = CharTable.convert(text);
    	ArrayList<String> part = new ArrayList<String>(textConverted.length);
    	ArrayList<String> features = new ArrayList<String>(textConverted.length);
    	int offsetAtom = 0, start = 0;
        int preType = type[textConverted[offsetAtom]];
        int curType;
        while (++offsetAtom < textConverted.length)
        {
            curType = type[textConverted[offsetAtom]];
            if (curType != preType)
            {
                // 百分数识别
                if ((textConverted[offsetAtom] == '%' || textConverted[offsetAtom] == '％')
                		&& preType == CharType.CT_NUM)
                	continue;
                // 浮点数识别
                if ((textConverted[offsetAtom] == '.' || textConverted[offsetAtom] == '．')
                		&& preType == CharType.CT_NUM)
                {
                	if (offsetAtom+1 < textConverted.length)
                	{
                		int nextType = type[textConverted[offsetAtom+1]];
                        if (nextType == CharType.CT_NUM)
                        	continue;
                	}
                }
                // 添加现在的节点
                switch (preType)
                {
                	case CharType.CT_NUM:
                	{
                		part.add(new String(text, start, offsetAtom - start));
                		features.add(number);
                		break;
                	}
                	case CharType.CT_LETTER:
                	{
                		part.add(new String(text, start, offsetAtom - start));
                		features.add(english);
                		break;
                	}
                	default :
                	{
                		for (int i=start; i<offsetAtom; i++)
                		{
                    		part.add(String.valueOf(text[i]));
                    		features.add(String.valueOf(textConverted[i]));
                		}
                		break;
                	}
                }
                start = offsetAtom;
            }
            preType = curType;
        }
        if (offsetAtom == textConverted.length)
        {
            switch (preType)
            {
            	case CharType.CT_NUM:
            	{
            		part.add(new String(text, start, offsetAtom - start));
            		features.add(number);
            		break;
            	}
            	case CharType.CT_LETTER:
            	{
            		part.add(new String(text, start, offsetAtom - start));
            		features.add(english);
            		break;
            	}
            	default :
            	{
            		for (int i=start; i<offsetAtom; i++)
            		{
                		part.add(String.valueOf(text[i]));
                		features.add(String.valueOf(textConverted[i]));
            		}
            		break;
            	}
            }
        }
        String table[][] = new String[part.size()][3];
        for (int i=0; i<table.length; i++)
        {
        	table[i][0] = features.get(i);
        	table[i][1] = part.get(i);
        }
        part.clear();
        features.clear();
        return table;
    }

    public static LinkedList<Word> toBMESTagSquence(List<Word> sentence)
    {
    	LinkedList<Word> charSquence = new LinkedList<Word>();
    	for (Word word : sentence)
    	{
    		String[][] matrix = atomSegment(word.getValue().toCharArray());
    		if (matrix.length == 1)
    		{
    			charSquence.add(new Word(matrix[0][0], tags[3]));
    			continue;
    		}
    		charSquence.add(new Word(matrix[0][0], tags[0]));
    		for (int i=1; i<matrix.length-1; i++)
    		{
    			charSquence.add(new Word(matrix[i][0], tags[1]));
    		}
			charSquence.add(new Word(matrix[matrix.length-1][0], tags[2]));
    	}
    	return charSquence;
    }
 }
