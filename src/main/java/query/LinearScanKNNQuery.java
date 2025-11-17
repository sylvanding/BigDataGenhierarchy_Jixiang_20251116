package query;

import core.MetricFunction;
import core.MetricSpaceData;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 线性扫描k近邻查询
 *
 * 通过线性扫描所有数据对象，使用优先队列（最大堆）维护k个最近邻
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class LinearScanKNNQuery {

    /**
     * 执行kNN查询
     * @param dataset 数据集
     * @param query 查询对象
     * @param metric 距离函数
     * @return kNN结果列表（按距离升序排列）
     */
    public static List<KNNResult> execute(List<? extends MetricSpaceData> dataset, KNNQuery query,
            MetricFunction metric) {
        return execute(dataset, query, metric, true);
    }

    /**
     * 执行kNN查询
     * @param dataset 数据集
     * @param query 查询对象
     * @param metric 距离函数
     * @param verbose 是否打印统计信息
     * @return kNN结果列表（按距离升序排列）
     */
    public static List<KNNResult> execute(List<? extends MetricSpaceData> dataset, KNNQuery query,
            MetricFunction metric, boolean verbose) {

        // 使用优先队列（最大堆）维护k个最近邻
        PriorityQueue<KNNResult> maxHeap = new PriorityQueue<>();
        long distanceCalculations = 0;

        MetricSpaceData queryObject = query.getQueryObject();
        int k = query.getK();

        // 线性扫描所有数据
        for (MetricSpaceData data : dataset) {
            double distance = metric.getDistance(queryObject, data);
            distanceCalculations++;

            if (maxHeap.size() < k) {
                // 堆未满，直接加入
                maxHeap.offer(new KNNResult(data, distance));
            } else if (distance < maxHeap.peek().getDistance()) {
                // 当前距离小于堆顶，替换堆顶
                maxHeap.poll();
                maxHeap.offer(new KNNResult(data, distance));
            }
        }

        // 将结果转为列表并按距离升序排列
        List<KNNResult> results = new ArrayList<>(maxHeap);
        results.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));

        // 输出统计信息
        if (verbose) {
            System.out.println("线性扫描kNN查询统计:");
            System.out.println("  数据集大小: " + dataset.size());
            System.out.println("  k值: " + k);
            System.out.println("  距离计算次数: " + distanceCalculations);
            System.out.println("  返回结果数: " + results.size());
        }

        return results;
    }
}

