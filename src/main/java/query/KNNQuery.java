package query;

import core.MetricSpaceData;

/**
 * k近邻查询定义类
 *
 * kNN查询：给定查询对象q和整数k，找出距离q最近的k个数据对象
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class KNNQuery {
    private final MetricSpaceData queryObject; // 查询对象
    private final int k; // 近邻数量

    /**
     * 构造k近邻查询
     * @param queryObject 查询对象
     * @param k 近邻数量，必须 > 0
     * @throws IllegalArgumentException 如果k <= 0
     */
    public KNNQuery(MetricSpaceData queryObject, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k必须大于0: " + k);
        }
        this.queryObject = queryObject;
        this.k = k;
    }

    /**
     * 获取查询对象
     * @return 查询对象
     */
    public MetricSpaceData getQueryObject() {
        return queryObject;
    }

    /**
     * 获取k值
     * @return k值
     */
    public int getK() {
        return k;
    }

    @Override
    public String toString() {
        return String.format("KNNQuery[q=%s, k=%d]", queryObject, k);
    }
}

