package query;

import core.MetricFunction;
import core.MetricSpaceData;

import java.util.ArrayList;
import java.util.List;

/**
 * 线性扫描范围查询
 * 
 * 通过线性扫描所有数据对象，计算与查询对象的距离，保留满足条件的结果
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public class LinearScanRangeQuery {

    /**
     * 执行范围查询
     * @param dataset 数据集
     * @param query 查询对象
     * @param metric 距离函数
     * @return 查询结果列表（距离 <= radius的所有对象）
     */
    public static List<MetricSpaceData> execute(List<? extends MetricSpaceData> dataset,
            RangeQuery query, MetricFunction metric) {

        List<MetricSpaceData> results = new ArrayList<>();
        long distanceCalculations = 0;

        MetricSpaceData queryObject = query.getQueryObject();
        double radius = query.getRadius();

        // 线性扫描所有数据
        for (MetricSpaceData data : dataset) {
            double distance = metric.getDistance(queryObject, data);
            distanceCalculations++;

            if (distance <= radius) {
                results.add(data);
            }
        }

        // 输出统计信息
        System.out.println("线性扫描范围查询统计:");
        System.out.println("  数据集大小: " + dataset.size());
        System.out.println("  查询半径: " + radius);
        System.out.println("  距离计算次数: " + distanceCalculations);
        System.out.println("  结果数量: " + results.size());

        return results;
    }

    /**
     * 执行范围查询（返回结果带距离信息）
     * @param dataset 数据集
     * @param query 查询对象
     * @param metric 距离函数
     * @return 查询结果列表（包含距离信息）
     */
    public static List<KNNResult> executeWithDistance(List<? extends MetricSpaceData> dataset,
            RangeQuery query, MetricFunction metric) {

        List<KNNResult> results = new ArrayList<>();

        MetricSpaceData queryObject = query.getQueryObject();
        double radius = query.getRadius();

        for (MetricSpaceData data : dataset) {
            double distance = metric.getDistance(queryObject, data);

            if (distance <= radius) {
                results.add(new KNNResult(data, distance));
            }
        }

        // 按距离升序排序
        results.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));

        return results;
    }
}

