package org.pwstudio.nlp.dictionary.io;

import static org.pwstudio.nlp.utility.Predefine.logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.pwstudio.nlp.collection.trie.DoubleArrayTrie;
import org.pwstudio.nlp.corpus.io.ByteArray;
import org.pwstudio.nlp.corpus.io.IOUtil;
import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.CoreDictionary.Attribute;
import org.pwstudio.nlp.dictionary.base.AttributeDictionary;
import org.pwstudio.nlp.utility.Predefine;

/**
 * 
 * 词典加载工具
 * @author iyunwen-李辰刚
 *
 */
public class AttributeDictionaryLoader {
	
	/**
	 * 加载词典统一方法
	 * @param path
	 * @return
	 */
	public static AttributeDictionary load(String path) {
		String binaryPath = path + Predefine.BIN_EXT;
		AttributeDictionary mainDict = loadFromBinary(binaryPath);
        if (mainDict != null)
        	return mainDict;
        mainDict = loadFromText(path);
        logger.info("词典加载成功! 下面将写入缓存……");
        if (mainDict != null && !saveToBinary(mainDict,binaryPath)) {
            logger.warning("缓存词典到" + binaryPath + "失败");
        }
        return mainDict;
	}

	/**
	 * 从文本加载单文件词典
	 * @param path
	 * @return
	 */
	public static AttributeDictionary loadFromText(String path)
    {
        TreeMap<String, Attribute> map = new TreeMap<String, Attribute>();
    	int totalFrequency = loadFromText(path, map);
    	if (totalFrequency == 0)
    		return null; 
    	AttributeDictionary mainDict = buildDictionary(map, totalFrequency);
    	map = null;
        return mainDict;
    }

	/**
	 * 从文本中加载一个词典
	 * @param path
	 * @param map
	 */
	static int loadFromText(String path, TreeMap<String, Attribute> map) {
		BufferedReader br;
		int totalFrequency = 0;
        try
        {
			long start = System.currentTimeMillis();
	 		br = new BufferedReader(new InputStreamReader(IOUtil.newInputStream(path), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null)
			{
			    String param[] = line.split("\\s", 2);
			    Attribute attribute = Attribute.create(param[1]);
			    addToMap(map, param[0], attribute);
			    totalFrequency += attribute.totalFrequency;
			}
			logger.warning("词典读入词条" + map.size() + " 全部频次" + totalFrequency + ". ");
			logger.warning("耗时" + (System.currentTimeMillis() - start) + "ms");
			br.close();
	    }
	    catch (FileNotFoundException e)
	    {
	        logger.warning("核心词典 " + path + " 不存在！" + e);
	        return 0;
	    }
	    catch (Exception e)
	    {
	        logger.warning("核心词典 " + path + " 读取错误！" + e);
	        return 0;
	    }
        return totalFrequency;
	}

	static void addToMap(TreeMap<String, Attribute> map, String word, Attribute attribute) {
		if (!map.containsKey(word)) {
			map.put(word, attribute);
			return;
		}
		int newArray = 0;
        ArrayList<Map.Entry<Nature, Integer>> entries = new ArrayList<Map.Entry<Nature, Integer>>();
        int totalAdded = 0;
		Attribute oldAttribute = map.get(word);
		for (int i=0; i<attribute.nature.length; i++) {
			if (!oldAttribute.hasNature(attribute.nature[i])) {
				newArray++;
	        	entries.add(new AbstractMap.SimpleEntry<Nature, Integer>(attribute.nature[i], 
	        			attribute.frequency[i]));
	        	totalAdded += attribute.frequency[i];
			} else {
				//直接合并已存在的项目
				for (int j=0; j<oldAttribute.nature.length; j++) {
					if (oldAttribute.nature[j] == attribute.nature[i]) {
						oldAttribute.frequency[j]+=attribute.frequency[i];
						oldAttribute.totalFrequency+=attribute.frequency[i];
					}
				}
			}
		}
		if (newArray > 0) {//重建数组
			//归并&排序
	        for (int i=0; i<oldAttribute.nature.length; i++) {
	        	entries.add(new AbstractMap.SimpleEntry<Nature, Integer>(oldAttribute.nature[i], 
	        			oldAttribute.frequency[i]));
	        }
	        Collections.sort(entries, new Comparator<Map.Entry<Nature, Integer>>() {
	            @Override
	            public int compare(Map.Entry<Nature, Integer> o1, 
	            		Map.Entry<Nature, Integer> o2) {
	                return o1.getValue().compareTo(o2.getValue());
	            }
	        });
			//重建
			oldAttribute.nature = new Nature[entries.size()];
	        oldAttribute.frequency = new int[entries.size()];
            for (int i = 0; i < entries.size(); ++i) {
            	oldAttribute.nature[i] = entries.get(i).getKey();
            	oldAttribute.frequency[i] = entries.get(i).getValue();
            }
            oldAttribute.totalFrequency += totalAdded;
		}
	}

	/**
	 * 从条目表构建出DAT型词典
	 * @param map
	 * @param totalFrequency
	 * @return
	 */
	static AttributeDictionary buildDictionary(TreeMap<String, Attribute> map, int totalFrequency) {
		DoubleArrayTrie<Attribute> dat = new DoubleArrayTrie<Attribute>();
		dat.build(map);
		return new AttributeDictionary(dat, totalFrequency);
	}

    /**
     * 从缓存文件读取词典
     * @param path		缓存文件路径
     * @return 词典
     */
	static AttributeDictionary loadFromBinary(String path) {
		DoubleArrayTrie<Attribute> dat = new DoubleArrayTrie<Attribute>();
        int totalFrequency = 0;
        try
        {
            ByteArray byteArray = ByteArray.createByteArray(path);
            if (byteArray == null)
            	return null;
            totalFrequency = byteArray.nextInt();
            int size = byteArray.nextInt();
            Attribute[] attributes = new Attribute[size];
            final Nature[] natureIndexArray = Nature.values();
            for (int i = 0; i < size; ++i)
            {
                // 第一个是全部频次，第二个是词性个数
                int currentTotalFrequency = byteArray.nextInt();
                int length = byteArray.nextInt();
                attributes[i] = new Attribute(length);
                attributes[i].totalFrequency = currentTotalFrequency;
                for (int j = 0; j < length; ++j)
                {
                    attributes[i].nature[j] = natureIndexArray[byteArray.nextInt()];
                    attributes[i].frequency[j] = byteArray.nextInt();
                }
            }
            if (!dat.load(byteArray, attributes) || byteArray.hasMore())
            	return null;
        }
        catch (Exception e)
        {
            logger.warning("读取失败，问题发生在" + e);
            return null;
        }
        return new AttributeDictionary(dat, totalFrequency);
    }
	
	/**
	 * 保存词典到缓存文件
	 * @param mainDict	
	 * @param path		缓存文件路径
	 * @return 保存成功返回True
	 */
	static boolean saveToBinary(AttributeDictionary mainDict, String path) {
        try {
            DataOutputStream out = new DataOutputStream(IOUtil.newOutputStream(path));
            out.writeInt(mainDict.getTotalFrequency());
            Attribute[] attributeList = new Attribute[mainDict.getTrie().size()];
            attributeList = mainDict.getTrie().getValueArray(attributeList);
            out.writeInt(attributeList.length);
            for (Attribute attribute : attributeList)
            {
                out.writeInt(attribute.totalFrequency);
                out.writeInt(attribute.nature.length);
                for (int i = 0; i < attribute.nature.length; ++i)
                {
                    out.writeInt(attribute.nature[i].ordinal());
                    out.writeInt(attribute.frequency[i]);
                }
            }
            mainDict.getTrie().save(out);
            out.close();
        } catch (Exception e) {
            logger.warning("保存失败" + e);
            return false;
        }
		return false;
	}


	
}
