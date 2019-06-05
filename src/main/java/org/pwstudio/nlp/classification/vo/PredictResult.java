package org.pwstudio.nlp.classification.vo;

/**
 * 
 * 分类预测结果对象
 * @author 李辰刚
 *
 */
public class PredictResult {
	
	/** 意图id */
	private int classId;
	
	/** 意图名称 */
	private String className;
	
	/** 预测成该意图的置信度 */
	private double confidence;
	
	public PredictResult() {
		classId = -1;
	}

	public PredictResult(int classId, double confidence) {
		this.classId = classId;
		this.confidence = confidence;
	}

	public int getClassId() {
		return classId;
	}
	public void setClassId(int classId) {
		this.classId = classId;
	}

	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}

	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	@Override
	public String toString() {
		return "PredictResult: {\"classId\":" + classId + ", \"className\":\"" + className + "\", \"confidence\":" + confidence + "}";
	}


	
}
