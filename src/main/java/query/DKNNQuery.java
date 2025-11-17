package query;

import core.MetricSpaceData;

/**
 * 多样化k近邻查询定义类
 *
 * dkNN查询：在kNN的基础上，保证结果的多样性，避免返回过于相似的对象
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class DKNNQuery {
    private final MetricSpaceData queryObject; // 查询对象
    private final int k; // 近邻数量
    private final double diversityWeight; // 多样性权重(0-1)

    /**
     * 构造多样化k近邻查询
     * @param queryObject 查询对象
     * @param k 近邻数量，必须 > 0
     * @param diversityWeight 多样性权重，取值范围[0,1]
     *                        0表示不考虑多样性（等同于kNN）
     *                        1表示完全考虑多样性
     * @throws IllegalArgumentException 如果参数不合法
     */
    public DKNNQuery(MetricSpaceData queryObject, int k, double diversityWeight) {
        if (k <= 0) {
            throw new IllegalArgumentException("k必须大于0: " + k);
        }
        if (diversityWeight < 0 || diversityWeight > 1) {
            throw new IllegalArgumentException("多样性权重必须在[0,1]之间: " + diversityWeight);
        }
        this.queryObject = queryObject;
        this.k = k;
        this.diversityWeight = diversityWeight;
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

    /**
     * 获取多样性权重
     * @return 多样性权重
     */
    public double getDiversityWeight() {
        return diversityWeight;
    }

    @Override
    public String toString() {
        return String.format("DKNNQuery[q=%s, k=%d, diversityWeight=%.2f]", queryObject, k,
                diversityWeight);
    }
}

