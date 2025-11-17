package index;

import core.MetricFunction;
import core.MetricSpaceData;
import query.KNNQuery;
import query.KNNResult;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 基于Pivot Table的kNN查询
 *
 * 使用动态查询半径的策略：
 * 1. 维护一个最大堆保存当前的k个最近邻
 * 2. 动态更新查询半径为当前第k近邻的距离
 * 3. 利用三角不等式和动态半径进行剪枝
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class PivotTableKNNQuery {

    /**
     * 执行基于Pivot Table的kNN查询
     * @param pivotTable Pivot Table索引
     * @param query 查询对象
     * @return kNN结果列表（按距离升序排列）
     */
    public static List<KNNResult> execute(PivotTable pivotTable, KNNQuery query) {
        return execute(pivotTable, query, true);
    }

    /**
     * 执行基于Pivot Table的kNN查询
     * @param pivotTable Pivot Table索引
     * @param query 查询对象
     * @param verbose 是否打印详细信息
     * @return kNN结果列表（按距离升序排列）
     */
    public static List<KNNResult> execute(PivotTable pivotTable, KNNQuery query, boolean verbose) {

        // 使用优先队列维护k个最近邻
        PriorityQueue<KNNResult> maxHeap = new PriorityQueue<>();
        long distanceCalculations = 0;
        int pruned = 0;
        int verified = 0;

        MetricSpaceData queryObject = query.getQueryObject();
        int k = query.getK();
        MetricFunction metric = pivotTable.getMetric();

        if (verbose) {
            System.out.println("=== Pivot Table kNN查询 ===");
            System.out.println("k值: " + k);
        }

        // 预计算查询对象到所有支撑点的距离
        int numPivots = pivotTable.getPivots().size();
        double[] queryToPivotDist = new double[numPivots];
        for (int j = 0; j < numPivots; j++) {
            queryToPivotDist[j] = metric.getDistance(queryObject, pivotTable.getPivots().get(j));
            distanceCalculations++;
        }

        // 动态查询半径（当前第k近邻的距离）
        double currentRadius = Double.MAX_VALUE;

        // 对每个数据对象进行处理
        List<MetricSpaceData> dataset = pivotTable.getDataset();
        for (int i = 0; i < dataset.size(); i++) {
            MetricSpaceData data = dataset.get(i);
            boolean canPrune = false;

            // 尝试使用支撑点剪枝
            for (int j = 0; j < numPivots; j++) {
                double dps = pivotTable.getDistanceToPivot(i, j);
                double dpq = queryToPivotDist[j];

                // 使用当前查询半径进行剪枝
                if (Math.abs(dpq - dps) > currentRadius) {
                    canPrune = true;
                    pruned++;
                    break;
                }
            }

            if (!canPrune) {
                // 计算实际距离
                double distance = metric.getDistance(queryObject, data);
                distanceCalculations++;
                verified++;

                if (maxHeap.size() < k) {
                    maxHeap.offer(new KNNResult(data, distance));
                    if (maxHeap.size() == k) {
                        currentRadius = maxHeap.peek().getDistance();
                    }
                } else if (distance < currentRadius) {
                    maxHeap.poll();
                    maxHeap.offer(new KNNResult(data, distance));
                    currentRadius = maxHeap.peek().getDistance();
                }
            }
        }

        // 转为列表并排序
        List<KNNResult> results = new ArrayList<>(maxHeap);
        results.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));

        // 保存统计信息到PivotTable
        pivotTable.setLastQueryStatistics(distanceCalculations, pruned, 0, verified);

        if (verbose) {
            System.out.println("\n查询统计:");
            System.out.println("  数据集大小: " + dataset.size());
            System.out.println("  k值: " + k);
            System.out.println("  支撑点数量: " + numPivots);
            System.out.println("  距离计算次数: " + distanceCalculations);
            System.out.println("  剪枝数量: " + pruned);
            System.out.println("  需要验证数量: " + verified);
            System.out.println(
                    "  剪枝率: " + String.format("%.2f", 100.0 * pruned / dataset.size()) + "%");
            System.out.println("  返回结果数: " + results.size());
            System.out.println("===========================\n");
        }

        return results;
    }
}

