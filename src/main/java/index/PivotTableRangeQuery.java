package index;

import core.MetricFunction;
import core.MetricSpaceData;
import query.RangeQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于Pivot Table的范围查询
 *
 * 利用三角不等式进行剪枝：
 * - 排除规则: |d(p,q) - d(p,s)| > r => d(q,s) > r，可以剪枝
 * - 包含规则: d(p,q) + d(p,s) <= r => d(q,s) <= r，可以直接包含
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public class PivotTableRangeQuery {

    /**
     * 执行基于Pivot Table的范围查询
     * @param pivotTable Pivot Table索引
     * @param query 查询对象
     * @return 查询结果列表
     */
    public static List<MetricSpaceData> execute(PivotTable pivotTable, RangeQuery query) {

        List<MetricSpaceData> results = new ArrayList<>();
        long distanceCalculations = 0;
        int pruned = 0;
        int included = 0;
        int verified = 0;

        MetricSpaceData queryObject = query.getQueryObject();
        double radius = query.getRadius();
        MetricFunction metric = pivotTable.getMetric();

        System.out.println("=== Pivot Table范围查询 ===");
        System.out.println("查询半径: " + radius);

        // 预计算查询对象到所有支撑点的距离
        int numPivots = pivotTable.getPivots().size();
        double[] queryToPivotDist = new double[numPivots];
        for (int j = 0; j < numPivots; j++) {
            queryToPivotDist[j] = metric.getDistance(queryObject, pivotTable.getPivots().get(j));
            distanceCalculations++;
        }

        // 对每个数据对象，尝试使用三角不等式剪枝
        List<MetricSpaceData> dataset = pivotTable.getDataset();
        for (int i = 0; i < dataset.size(); i++) {
            MetricSpaceData data = dataset.get(i);
            boolean canPrune = false;
            boolean canInclude = false;

            // 尝试使用每个支撑点进行剪枝
            for (int j = 0; j < numPivots; j++) {
                double dps = pivotTable.getDistanceToPivot(i, j); // d(pivot, data)
                double dpq = queryToPivotDist[j]; // d(pivot, query)

                // 排除规则: |d(p,q) - d(p,s)| > r => d(q,s) > r
                if (Math.abs(dpq - dps) > radius) {
                    canPrune = true;
                    pruned++;
                    break;
                }

                // 包含规则: d(p,q) + d(p,s) <= r => d(q,s) <= r
                if (dpq + dps <= radius) {
                    canInclude = true;
                    included++;
                    break;
                }
            }

            if (canInclude) {
                // 可以直接判定为查询结果
                results.add(data);
            } else if (!canPrune) {
                // 无法剪枝，需要计算实际距离验证
                double distance = metric.getDistance(queryObject, data);
                distanceCalculations++;
                verified++;

                if (distance <= radius) {
                    results.add(data);
                }
            }
        }

        System.out.println("\n查询统计:");
        System.out.println("  数据集大小: " + dataset.size());
        System.out.println("  支撑点数量: " + numPivots);
        System.out.println("  距离计算次数: " + distanceCalculations);
        System.out.println("  剪枝数量: " + pruned);
        System.out.println("  直接包含数量: " + included);
        System.out.println("  需要验证数量: " + verified);
        System.out
                .println("  剪枝率: " + String.format("%.2f", 100.0 * pruned / dataset.size()) + "%");
        System.out.println("  结果数量: " + results.size());
        System.out.println("============================\n");

        return results;
    }
}

