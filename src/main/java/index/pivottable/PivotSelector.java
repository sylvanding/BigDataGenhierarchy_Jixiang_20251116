package index.pivottable;

import core.MetricFunction;
import core.MetricSpaceData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 支撑点选择器
 *
 * 实现多种支撑点选择算法
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class PivotSelector {

    /**
     * 选择支撑点
     * @param dataset 数据集
     * @param numPivots 支撑点数量
     * @param method 选择方法
     * @param metric 距离函数
     * @return 支撑点列表
     */
    public static List<MetricSpaceData> selectPivots(List<? extends MetricSpaceData> dataset,
            int numPivots, PivotSelectionMethod method, MetricFunction metric) {
        return selectPivots(dataset, numPivots, method, metric, true);
    }

    /**
     * 选择支撑点
     * @param dataset 数据集
     * @param numPivots 支撑点数量
     * @param method 选择方法
     * @param metric 距离函数
     * @param verbose 是否打印详细信息
     * @return 支撑点列表
     */
    public static List<MetricSpaceData> selectPivots(List<? extends MetricSpaceData> dataset,
            int numPivots, PivotSelectionMethod method, MetricFunction metric, boolean verbose) {

        if (numPivots <= 0) {
            throw new IllegalArgumentException("支撑点数量必须大于0");
        }

        if (numPivots > dataset.size()) {
            if (verbose) {
                System.out.println("警告：支撑点数量大于数据集大小，调整为数据集大小");
            }
            numPivots = dataset.size();
        }

        if (verbose) {
            System.out.println("开始选择支撑点:");
            System.out.println("  方法: " + method);
            System.out.println("  数量: " + numPivots);
        }

        long startTime = System.currentTimeMillis();
        List<MetricSpaceData> pivots;

        switch (method) {
            case RANDOM:
                pivots = selectRandomPivots(dataset, numPivots);
                break;
            case FFT:
                pivots = selectFFTPivots(dataset, numPivots, metric);
                break;
            case CENTER:
                pivots = selectCenterPivots(dataset, numPivots, metric);
                break;
            case BORDER:
                pivots = selectBorderPivots(dataset, numPivots, metric);
                break;
            default:
                throw new IllegalArgumentException("不支持的支撑点选择方法: " + method);
        }

        long endTime = System.currentTimeMillis();
        if (verbose) {
            System.out.println("支撑点选择完成，耗时: " + (endTime - startTime) + " ms\n");
        }

        return pivots;
    }

    /**
     * 随机选择支撑点
     */
    private static List<MetricSpaceData> selectRandomPivots(List<? extends MetricSpaceData> dataset,
            int numPivots) {

        List<MetricSpaceData> pivots = new ArrayList<>();
        List<MetricSpaceData> candidates = new ArrayList<>(dataset);
        Random random = new Random(42); // 固定种子以保证可重复性

        for (int i = 0; i < numPivots && !candidates.isEmpty(); i++) {
            int index = random.nextInt(candidates.size());
            pivots.add(candidates.remove(index));
        }

        return pivots;
    }

    /**
     * FFT (Farthest-First Traversal) 算法选择支撑点
     * 每次选择距离已选支撑点最远的点
     */
    private static List<MetricSpaceData> selectFFTPivots(List<? extends MetricSpaceData> dataset,
            int numPivots, MetricFunction metric) {

        List<MetricSpaceData> pivots = new ArrayList<>();

        // 第一个支撑点：随机选择
        Random random = new Random(42);
        pivots.add(dataset.get(random.nextInt(dataset.size())));

        // 后续支撑点：选择距离已选支撑点最远的点
        while (pivots.size() < numPivots) {
            double maxMinDist = -1;
            MetricSpaceData farthest = null;

            for (MetricSpaceData candidate : dataset) {
                // 跳过已选的支撑点
                if (pivots.contains(candidate))
                    continue;

                // 计算candidate到已选支撑点的最小距离
                double minDist = Double.MAX_VALUE;
                for (MetricSpaceData pivot : pivots) {
                    double dist = metric.getDistance(candidate, pivot);
                    minDist = Math.min(minDist, dist);
                }

                // 选择最小距离最大的candidate
                if (minDist > maxMinDist) {
                    maxMinDist = minDist;
                    farthest = candidate;
                }
            }

            if (farthest != null) {
                pivots.add(farthest);
            } else {
                break;
            }
        }

        return pivots;
    }

    /**
     * 选择距离数据集中心最近的点作为支撑点
     */
    private static List<MetricSpaceData> selectCenterPivots(List<? extends MetricSpaceData> dataset,
            int numPivots, MetricFunction metric) {

        // 简化实现：先随机选择一个中心，然后选择距离中心最近的k个点
        Random random = new Random(42);
        MetricSpaceData center = dataset.get(random.nextInt(dataset.size()));

        // 计算所有点到中心的距离
        List<DistancePair> pairs = new ArrayList<>();
        for (MetricSpaceData data : dataset) {
            double dist = metric.getDistance(center, data);
            pairs.add(new DistancePair(data, dist));
        }

        // 按距离升序排序
        pairs.sort((a, b) -> Double.compare(a.distance, b.distance));

        // 选择前k个点
        List<MetricSpaceData> pivots = new ArrayList<>();
        for (int i = 0; i < Math.min(numPivots, pairs.size()); i++) {
            pivots.add(pairs.get(i).data);
        }

        return pivots;
    }

    /**
     * 选择距离数据集边界最近的点作为支撑点
     */
    private static List<MetricSpaceData> selectBorderPivots(List<? extends MetricSpaceData> dataset,
            int numPivots, MetricFunction metric) {

        // 简化实现：选择距离其他点平均距离最大的点（边界点）
        List<DistancePair> pairs = new ArrayList<>();

        for (MetricSpaceData candidate : dataset) {
            double sumDist = 0;
            int count = 0;

            for (MetricSpaceData other : dataset) {
                if (candidate != other) {
                    sumDist += metric.getDistance(candidate, other);
                    count++;
                }
            }

            double avgDist = (count > 0) ? sumDist / count : 0;
            pairs.add(new DistancePair(candidate, avgDist));
        }

        // 按平均距离降序排序
        pairs.sort((a, b) -> Double.compare(b.distance, a.distance));

        // 选择前k个点
        List<MetricSpaceData> pivots = new ArrayList<>();
        for (int i = 0; i < Math.min(numPivots, pairs.size()); i++) {
            pivots.add(pairs.get(i).data);
        }

        return pivots;
    }

    /**
     * 辅助类：数据对象和距离的配对
     */
    private static class DistancePair {
        MetricSpaceData data;
        double distance;

        DistancePair(MetricSpaceData data, double distance) {
            this.data = data;
            this.distance = distance;
        }
    }
}
