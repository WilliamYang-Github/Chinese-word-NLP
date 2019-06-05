package org.pwstudio.nlp.dictionary.io;

import static org.pwstudio.nlp.utility.Predefine.logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.pwstudio.nlp.PwNLP;
import org.pwstudio.nlp.corpus.io.IOUtil;
import org.pwstudio.nlp.dictionary.base.AttributeDictionary;
import org.pwstudio.nlp.dictionary.base.BiGramTableDictionary;
import org.pwstudio.nlp.utility.Predefine;

/**
 * 
 * 二元接续词典加载工具
 * @author iyunwen-李辰刚
 *
 */
public class BigramTableDictionaryLoader {
    
    static final String BINARY_EXT = ".table" + Predefine.BIN_EXT;

    /**
     * 加载BiGram（二元接续）词典。缓存优先
     * @param dict
     * @param path
     * @return
     */
    public static BiGramTableDictionary load(AttributeDictionary dict, String path) {
        String binaryPath = path + BINARY_EXT;
        BiGramTableDictionary biGramDict = loadFromBinary(dict, binaryPath);
        if (biGramDict != null)
            return biGramDict;
        biGramDict = loadFromText(dict,path);
        if (biGramDict != null && !saveToBinary(biGramDict,binaryPath)) {
            logger.warning("缓存二元词典到" + binaryPath + "失败");
        }
        return biGramDict;
    }
    
    /**
     * 从单个文本文件读取词语BiGram（二元接续）词典
     * @param dict 对应的词典
     * @param path 文本文件路径
     * @return 生成的词典
     */
    public static BiGramTableDictionary loadFromText(AttributeDictionary dict, String path) {
        HashMap<Integer, TreeMap<Integer, Integer>> map = 
                new HashMap<Integer, TreeMap<Integer, Integer>>(dict.count()*2);
        int totalType = loadFromText(dict, path, map);
        if (totalType == 0)
            return null;
        BiGramTableDictionary bigram = buildDictTable(dict, map, totalType);
        map = null;
        return bigram;
    }
    
    /**
     * 从单个文本文件收集Bigram
     * @param dict
     * @param path
     * @param map
     * @return
     */
    static int loadFromText(AttributeDictionary dict, String path, 
            Map<Integer, TreeMap<Integer, Integer>> map) {
        BufferedReader br;
        int totalType = 0;
        try {
            br = new BufferedReader(new InputStreamReader(IOUtil.newInputStream(path), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null)
            {
                String[] params = line.split("\\s");
                String[] twoWord = params[0].split("@", 2);
                String a = twoWord[0];
                int idA = dict.getWordID(a);
                if (idA == -1) {
                    if (PwNLP.Config.DEBUG)
                        logger.warning(line + " 中的 " + a + "不存在于核心词典，将会忽略这一行");
                    continue;
                }
                String b = twoWord[1];
                int idB = dict.getWordID(b);
                if (idB == -1) {
                    if (PwNLP.Config.DEBUG)
                        logger.warning(line + " 中的 " + b + "不存在于核心词典，将会忽略这一行");
                    continue;
                }
                int freq = Integer.parseInt(params[1]);
                TreeMap<Integer, Integer> biMap = map.get(idA);
                if (biMap == null) {
                    biMap = new TreeMap<Integer, Integer>();
                    map.put(idA, biMap);
                }
                if (biMap.containsKey(idB)) {
                    freq += biMap.get(idB);
                }
                biMap.put(idB, freq);
                totalType += 2;
            }
            br.close();
        } catch (FileNotFoundException e) {
            logger.severe("二元词典" + path + "不存在！" + e);
            return 0;
        } catch (IOException e) {
            logger.severe("二元词典" + path + "读取错误！" + e);
            return 0;
        }
        return totalType;
    }
    
    /**
     * 创建为词典
     * @param dict
     * @param map
     * @param totalType
     * @return
     */
    static BiGramTableDictionary buildDictTable(AttributeDictionary dict, 
            Map<Integer, TreeMap<Integer, Integer>> map, int totalType) {
        int maxWordId = dict.count();
        BiGramTableDictionary bigram = null;
        int [] start = new int[maxWordId + 1];
        int [] pair = new int[totalType];  // total是接续的个数*2
        int offset = 0;

        for (int i = 0; i < maxWordId; ++i)
        {
            TreeMap<Integer, Integer> bMap = map.get(i);
            if (bMap != null)
            {
                for (Map.Entry<Integer, Integer> entry : bMap.entrySet())
                {
                    int index = offset << 1;
                    pair[index] = entry.getKey();
                    pair[index + 1] = entry.getValue();
                    ++offset;
                }
            }
            start[i + 1] = offset;
        }
        bigram = new BiGramTableDictionary(dict, start, pair);
        return bigram;
    } 

    /**
     * 从缓存文件读取词语BiGram（二元接续）词典
     * @param dict 对应的词典
     * @param path 二进制文件路径
     * @return 生成的词典
     */
    static BiGramTableDictionary loadFromBinary(AttributeDictionary dict, String path)
    {
        // 两个数组从byte转为int竟然要花4秒钟，所以改用ObjectInputStream
        BiGramTableDictionary bigram = null;
        try {
            ObjectInputStream in = new ObjectInputStream(IOUtil.newInputStream(path));
            int [] start = (int[]) in.readObject();
            /**
             * 目前Bigram的缓存直接依赖于对应的词典，
             * 所以这里必须保证二者的一致性，不然Bigram就会错乱
             */
            if (dict.count() != start.length - 1) { 
                in.close();
                return null;
            }
            int [] pair = (int[]) in.readObject();
            bigram = new BiGramTableDictionary(dict, start, pair);
            in.close();
        }
        catch (Exception e) {
            logger.warning("尝试载入缓存文件" + path + "发生异常[" + e + "]，下面将载入源文件并自动缓存……");
            return null;
        }
        return bigram;
    }

    /**
     * 保存缓存
     * @param bigram 二元接续词典
     * @param path 二进制文件路径
     * @return
     */
    static boolean saveToBinary(BiGramTableDictionary bigram, String path)
    {
        try {
            ObjectOutputStream out = new ObjectOutputStream(IOUtil.newOutputStream(path));
            out.writeObject(bigram.getStart());
            out.writeObject(bigram.getPair());
            out.close();
        } catch (Exception e) {
            logger.warning("在缓存" + path + "时发生异常" + e);
            return false;
        }

        return true;
    }

}
