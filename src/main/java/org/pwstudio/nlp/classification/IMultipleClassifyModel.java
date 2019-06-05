package org.pwstudio.nlp.classification;

import org.pwstudio.nlp.model.IModel;
import org.pwstudio.nlp.classification.vo.InstanceVector;
import org.pwstudio.nlp.classification.vo.PredictResult;

/**
 * 云问封装的多分类（多类选一）模型接口
 * @author 李辰刚
 *
 */
public interface IMultipleClassifyModel extends IModel {

	/**
	 * 预测一条数据所对应的标签
	 * @param record  待分类数据 
	 * @return        两类别的置信度
	 */
	float[] predictConfidence(InstanceVector record);

	/**
	 * 预测一条数据所对应的标签
	 * @param record  待分类数据 
	 * @return        预测结果对象
	 */
	PredictResult predict(InstanceVector record);
}
