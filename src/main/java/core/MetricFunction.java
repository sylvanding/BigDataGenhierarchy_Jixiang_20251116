package core;

import java.io.Serializable;

/**
 * 度量空间距离函数接口
 *
 * 定义了度量空间中两个数据对象之间距离计算的通用接口。
 * 任何合法的距离函数都必须满足度量空间的三大基本性质：
 * 1. 非负性：d(x,y) >= 0，且 d(x,y) = 0 当且仅当 x = y
 * 2. 对称性：d(x,y) = d(y,x)
 * 3. 三角不等性：d(x,z) <= d(x,y) + d(y,z)
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public interface MetricFunction extends Serializable {

    /**
     * 计算两个度量空间数据对象之间的距离
     *
     * 该方法必须满足度量空间的三个基本性质：
     * 1. 非负性：返回值必须 >= 0，且当两个对象相同时返回 0
     * 2. 对称性：d(obj1, obj2) = d(obj2, obj1)
     * 3. 三角不等性：对于任意三个对象 x, y, z，必须满足 d(x, z) <= d(x, y) + d(y, z)
     *
     * @param obj1 第一个数据对象
     * @param obj2 第二个数据对象
     * @return 两个数据对象之间的距离，必须 >= 0
     * @throws IllegalArgumentException 如果两个对象的类型不匹配或不支持该距离函数
     */
    double getDistance(MetricSpaceData obj1, MetricSpaceData obj2);

    /**
     * 获取距离函数的名称
     * @return 距离函数的名称，用于标识和输出
     */
    String getMetricName();
}

