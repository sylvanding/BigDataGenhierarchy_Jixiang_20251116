package query;

import core.MetricSpaceData;

/**
 * 范围查询定义类
 *
 * 范围查询：给定查询对象q和查询半径r，找出所有与q距离不超过r的数据对象
 * Result = {s ∈ S | d(q, s) ≤ r}
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class RangeQuery {
    private final MetricSpaceData queryObject; // 查询对象
    private final double radius; // 查询半径

    /**
     * 构造范围查询
     * @param queryObject 查询对象
     * @param radius 查询半径，必须 >= 0
     * @throws IllegalArgumentException 如果半径为负
     */
    public RangeQuery(MetricSpaceData queryObject, double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("查询半径不能为负: " + radius);
        }
        this.queryObject = queryObject;
        this.radius = radius;
    }

    /**
     * 获取查询对象
     * @return 查询对象
     */
    public MetricSpaceData getQueryObject() {
        return queryObject;
    }

    /**
     * 获取查询半径
     * @return 查询半径
     */
    public double getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        return String.format("RangeQuery[q=%s, r=%.4f]", queryObject, radius);
    }
}

