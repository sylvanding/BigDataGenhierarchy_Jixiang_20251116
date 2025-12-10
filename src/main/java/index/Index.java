package index;

import core.MetricSpaceData;
import core.MetricFunction;
import java.util.List;

/**
 * 索引接口
 *
 * 定义了所有索引结构（Pivot Table、GH树、VP树等）的统一接口。
 * 所有索引都支持构建、范围查询、k近邻查询等基本操作。
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public interface Index {

    /**
     * 构建索引
     *
     * @param dataset 数据集
     * @param metric 距离函数
     */
    void buildIndex(List<? extends MetricSpaceData> dataset, MetricFunction metric);

    /**
     * 范围查询
     *
     * 找出所有与查询对象距离不超过radius的数据对象
     *
     * @param queryObject 查询对象
     * @param radius 查询半径
     * @return 满足条件的数据对象列表
     */
    List<MetricSpaceData> rangeQuery(MetricSpaceData queryObject, double radius);

    /**
     * k近邻查询
     *
     * 找出与查询对象最近的k个数据对象
     *
     * @param queryObject 查询对象
     * @param k 近邻数量
     * @return k个最近邻数据对象列表（按距离升序排序）
     */
    List<MetricSpaceData> knnQuery(MetricSpaceData queryObject, int k);

    /**
     * 获取索引的统计信息
     *
     * @return 包含索引统计信息的字符串
     */
    String getStatistics();

    /**
     * 获取索引名称
     *
     * @return 索引的名称
     */
    String getIndexName();

    /**
     * 重置统计信息
     *
     * 清除查询过程中累计的统计数据，如距离计算次数等
     */
    void resetStatistics();

    /**
     * 获取查询过程中的距离计算次数
     *
     * @return 距离计算次数
     */
    int getDistanceComputations();

    /**
     * 获取数据集大小
     *
     * @return 数据集中数据对象的数量
     */
    int getDatasetSize();
}
