package org.pwstudio.nlp.recognition;

import java.util.List;

import org.pwstudio.nlp.seg.common.Vertex;

/**
 * 命名实体观察接口，
 * 既用于新的实体抽取，又可以用于外部的命名实体抽取
 * @author TylunasLi
 *
 * @param <T> 匹配模式的类型，一般为String，数字和姓名的模式用了枚举值
 */
public interface EntityObserver<T> {

	/**
	 * 模式匹配命中结果后的处理接口
	 * @param squenece	待识别实体的序列
	 * @param start		开始节点下标
	 * @param end		结束节点下标
	 * @param pattern	对应的模式
	 */
	public void onHit(Vertex[] squenece, int start, int end, T pattern);
	
}
