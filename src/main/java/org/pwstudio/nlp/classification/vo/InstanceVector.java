package org.pwstudio.nlp.classification.vo;

import java.util.HashMap;
import java.util.Set;

/**
 * 数据记录(实例)<br>
 * 记录由属性和类别组成，数据属性或者是向量，或者是矩阵。
 * 
 */
public class InstanceVector {
	
	/**  原始内容  */
	private String originalInfo;
	
	/**  稀疏向量  */
	private HashMap<String, Double> sparcseVector;
	
	/**  数据所对应的稠密向量表示.  */
	private double[] vector;

	/**  数据所对应的类别.  */
	private int label;
	
	/**  是否采用稠密向量表示.  */
	private boolean useDenseVector;
	
	/**  一维构造函数.  */
	public InstanceVector (String originalInfo, int label) {
		this.originalInfo = originalInfo;
		this.label = label;
		this.useDenseVector = true;
	}
	
	/**  一维构造函数.  */
	public InstanceVector (double[] vector, int label) {
		this.vector = vector;
		this.label = label;
		this.useDenseVector = true;
	}
	
	/**  二维矩阵的构造函数.  */
	public InstanceVector (HashMap<String, Double> sparcseVector, int label) {
		this.sparcseVector = sparcseVector;
		this.label = label;
		this.useDenseVector = false;
	}

    public String getOriginalInfo() {
        return originalInfo;
    }
    public void setOriginalInfo(String originalInfo) {
        this.originalInfo = originalInfo;
    }

    /**
     * 该记录的稀疏向量数据
     * @return vector
     */
    public HashMap<String, Double> getSparcseVector() {
        return sparcseVector;
    }

	/**
	 * 该记录的向量数据
	 * @return vector
	 */
	public double[] getVector() {
		return vector;
	}

	/**
	 * 该记录的类标
	 * @return
	 */
	public int getLabel() {
		return label;
	}
	/**
	 * @param label 要设置的 label
	 */
	public void setLabel(int label) {
		this.label = label;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("label:").append(label);
		if (useDenseVector)
		    sb.append("vector:").append(vector);
		else
		    sb.append("sparcseVector:").append(sparcseVector);
		return sb.toString();
	}

    public Set<String> getFeatureKeys() {
        return sparcseVector.keySet();
    }

}