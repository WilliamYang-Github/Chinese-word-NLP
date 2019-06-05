package org.pwstudio.nlp.classification;

import java.util.List;

import org.pwstudio.nlp.model.IModel;
import org.pwstudio.nlp.classification.vo.InstanceVector;
import org.pwstudio.nlp.classification.vo.PredictResult;

/**
 * 封装的多选分类（多个二分类）模型接口
 * @author 李辰刚
 *
 */
public interface IMultiBinaryClassifyModel extends IModel {

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
	List<PredictResult> predict(InstanceVector record);
}
