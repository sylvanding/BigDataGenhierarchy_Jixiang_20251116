package query;

import core.MetricSpaceData;

/**
 * kNN查询结果类
 * 
 * 封装查询结果中的数据对象和距离信息
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public class KNNResult implements Comparable<KNNResult> {
    private final MetricSpaceData data; // 数据对象
    private final double distance; // 距离值

    /**
     * 构造查询结果
     * @param data 数据对象
     * @param distance 距离值
     */
    public KNNResult(MetricSpaceData data, double distance) {
        this.data = data;
        this.distance = distance;
    }

    /**
     * 获取数据对象
     * @return 数据对象
     */
    public MetricSpaceData getData() {
        return data;
    }

    /**
     * 获取距离值
     * @return 距离值
     */
    public double getDistance() {
        return distance;
    }

    /**
     * 比较两个结果（用于最大堆）
     * 按距离降序排列（距离大的在前）
     */
    @Override
    public int compareTo(KNNResult other) {
        return Double.compare(other.distance, this.distance);
    }

    @Override
    public String toString() {
        return String.format("KNNResult[data=%s, distance=%.4f]", data, distance);
    }
}

