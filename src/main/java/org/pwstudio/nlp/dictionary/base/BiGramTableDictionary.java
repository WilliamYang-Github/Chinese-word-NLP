package org.pwstudio.nlp.dictionary.base;

/**
 * 标注二元接续词典，采用整型储存，高性能
 *
 * @OriginalAuthor hankcs
 * @author iyunwen
 */
public class BiGramTableDictionary {
    
    private AttributeDictionary unigramDict;
    /**
     * 描述了词在pair中的范围，具体说来<br>
     * 给定一个词idA，从pair[start[idA]]开始的start[idA + 1] - start[idA]描述了一些接续的频次
     */
    protected int start[];
    /**
     * pair[偶数n]表示key，pair[n+1]表示frequency
     */
    protected int pair[];
    /**
     * 双词词语对的总数    
     */
    protected long totalfrequency;
    
    public BiGramTableDictionary(AttributeDictionary dict, int start[], int pair[]) {
        this.unigramDict = dict;
        this.start = start;
        this.pair = pair;
    }
    
    /**
     * 二分搜索，由于二元接续前一个词固定时，后一个词比较少，所以二分也能取得很高的性能
     * @param a 目标数组
     * @param fromIndex 开始下标
     * @param length 长度
     * @param key 词的id
     * @return 共现频次
     */
    private static int binarySearch(int[] a, int fromIndex, int length, int key)
    {
        int low = fromIndex;
        int high = fromIndex + length - 1;

        while (low <= high)
        {
            int mid = (low + high) >>> 1;
            int midVal = a[mid << 1];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * 获取共现频次
     *
     * @param a 第一个词
     * @param b 第二个词
     * @return 第一个词@第二个词出现的频次
     */
    public int getBiFrequency(String a, String b)
    {
        int idA = unigramDict.trie.exactMatchSearch(a);
        if (idA == -1)
        {
            return 0;
        }
        int idB = unigramDict.trie.exactMatchSearch(b);
        if (idB == -1)
        {
            return 0;
        }
        int index = binarySearch(pair, start[idA], start[idA + 1] - start[idA], idB);
        if (index < 0) return 0;
        index <<= 1;
        return pair[index + 1];
    }

    /**
     * 获取共现频次
     * @param idA 第一个词的id
     * @param idB 第二个词的id
     * @return 共现频次
     */
    public int getBiFrequency(int idA, int idB)
    {
        if (idA == -1 || idB == -1)
        {
            return 0;
        }
        int index = binarySearch(pair, start[idA], start[idA + 1] - start[idA], idB);
        if (index < 0) return 0;
        index <<= 1;
        return pair[index + 1];
    }

    /**
     * 获取词语的ID
     *
     * @param a 词语
     * @return id
     */
    public int getWordID(String a)
    {
        return unigramDict.getWordID(a);
    }
    
    public int[] getStart()
    {
        return start;
    }

    public int[] getPair()
    {
        return pair;
    }

}
