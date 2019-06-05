package org.pwstudio.nlp.seg.common;

/**
 * 节点间费用计算的抽象
 * @author TylunasLi
 *
 */
public abstract class WeightCalculator {
	protected double smoothing;

	public WeightCalculator() {
		this.smoothing = 0.1;
	}

	/**
	 * 计算当前情况下两个节点之间的路径费用
	 * @param from
	 * @param to
	 * @return
	 */
	public abstract double calculateWeight(Vertex from, Vertex to);

	/**
	 * 计算当前情况下一个节点的最大权重
	 * @return
	 */
	public abstract double maxWeight();
}
