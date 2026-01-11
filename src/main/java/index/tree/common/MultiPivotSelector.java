package index.tree.common;

import core.MetricSpaceData;
import core.MetricFunction;

import java.util.*;

/**
 * 多Pivot选择器
 *
 * 为三种多pivot索引（MVPT、CGHT、完全线性划分）提供统一的pivot选择方法，确保公平对比。
 * 支持多种选择策略：随机、FFT（最远优先遍历）、最大分散度、增量选择。
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public class MultiPivotSelector {

    /**
     * Pivot选择策略枚举
     */
    public enum SelectionStrategy {
        /** 随机选择 */
        RANDOM,
        /** 最远优先遍历 (Farthest-First Traversal) */
        FFT,
        /** 最大分散度 */
        MAX_SPREAD,
        /** 增量选择 */
        INCREMENTAL
    }

    /** 选择策略 */
    private SelectionStrategy strategy;

    /** 随机数生成器 */
    private Random random;

    /** 距离计算次数统计 */
    private int distanceComputations = 0;

    /**
     * 构造函数
     *
     * @param strategy 选择策略
     */
    public MultiPivotSelector(SelectionStrategy strategy) {
        this.strategy = strategy;
        this.random = new Random();
    }

    /**
     * 构造函数（指定随机种子）
     *
     * @param strategy 选择策略
     * @param randomSeed 随机种子
     */
    public MultiPivotSelector(SelectionStrategy strategy, long randomSeed) {
        this.strategy = strategy;
        this.random = new Random(randomSeed);
    }

    /**
     * 选择k个pivot
     *
     * @param data 数据集
     * @param metric 距离函数
     * @param k pivot数量
     * @return pivot列表
     */
    public List<MetricSpaceData> selectPivots(List<? extends MetricSpaceData> data,
            MetricFunction metric, int k) {

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据集不能为空");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("pivot数量必须大于0");
        }
        if (k > data.size()) {
            k = data.size();
        }

        distanceComputations = 0;

        switch (strategy) {
            case RANDOM:
                return randomSelect(data, k);
            case FFT:
                return fftSelect(data, metric, k);
            case MAX_SPREAD:
                return maxSpreadSelect(data, metric, k);
            case INCREMENTAL:
                return incrementalSelect(data, metric, k);
            default:
                return randomSelect(data, k);
        }
    }

    /**
     * 随机选择策略
     *
     * 随机选择k个不同的数据点作为pivot
     */
    private List<MetricSpaceData> randomSelect(List<? extends MetricSpaceData> data, int k) {
        List<MetricSpaceData> pivots = new ArrayList<>();
        Set<Integer> selected = new HashSet<>();

        while (pivots.size() < k) {
            int idx = random.nextInt(data.size());
            if (!selected.contains(idx)) {
                selected.add(idx);
                pivots.add(data.get(idx));
            }
        }

        return pivots;
    }

    /**
     * FFT（最远优先遍历）策略
     *
     * 依次选择离已选点集最远的点：
     * 1. 随机选择第一个pivot
     * 2. 对于后续每个pivot，选择离已选点集最远的点
     */
    private List<MetricSpaceData> fftSelect(List<? extends MetricSpaceData> data,
            MetricFunction metric, int k) {

        List<MetricSpaceData> pivots = new ArrayList<>();
        Set<Integer> selectedIds = new HashSet<>();

        // 随机选择第一个pivot
        MetricSpaceData first = data.get(random.nextInt(data.size()));
        pivots.add(first);
        selectedIds.add(first.getDataId());

        // 记录每个点到已选pivot集合的最小距离
        double[] minDistToPivots = new double[data.size()];
        Arrays.fill(minDistToPivots, Double.MAX_VALUE);

        // 依次选择后续的pivot
        while (pivots.size() < k) {
            MetricSpaceData lastPivot = pivots.get(pivots.size() - 1);
            double maxMinDist = -1;
            int bestIdx = -1;

            // 更新每个点到已选pivot集合的最小距离，并找到最远点
            for (int i = 0; i < data.size(); i++) {
                MetricSpaceData candidate = data.get(i);
                if (selectedIds.contains(candidate.getDataId())) {
                    continue;
                }

                // 计算到最新pivot的距离，更新最小距离
                double dist = metric.getDistance(candidate, lastPivot);
                distanceComputations++;

                minDistToPivots[i] = Math.min(minDistToPivots[i], dist);

                if (minDistToPivots[i] > maxMinDist) {
                    maxMinDist = minDistToPivots[i];
                    bestIdx = i;
                }
            }

            if (bestIdx >= 0) {
                MetricSpaceData nextPivot = data.get(bestIdx);
                pivots.add(nextPivot);
                selectedIds.add(nextPivot.getDataId());
            }
        }

        return pivots;
    }

    /**
     * 最大分散度策略
     *
     * 通过采样找到使两两距离之和最大化的点集
     */
    private List<MetricSpaceData> maxSpreadSelect(List<? extends MetricSpaceData> data,
            MetricFunction metric, int k) {

        // 采样以减少计算量
        int sampleSize = Math.min(100, data.size());
        List<MetricSpaceData> sample = sampleData(data, sampleSize);

        // 如果采样后点数不足k，直接返回采样结果
        if (sample.size() <= k) {
            return new ArrayList<>(sample.subList(0, Math.min(k, sample.size())));
        }

        // 计算采样点之间的距离矩阵
        double[][] distMatrix = new double[sample.size()][sample.size()];
        for (int i = 0; i < sample.size(); i++) {
            for (int j = i + 1; j < sample.size(); j++) {
                double dist = metric.getDistance(sample.get(i), sample.get(j));
                distanceComputations++;
                distMatrix[i][j] = dist;
                distMatrix[j][i] = dist;
            }
        }

        // 贪心选择：选择能使总距离最大的点集
        List<MetricSpaceData> pivots = new ArrayList<>();
        Set<Integer> selectedIndices = new HashSet<>();

        // 选择距离最远的两个点作为初始
        double maxDist = -1;
        int best1 = 0, best2 = 1;
        for (int i = 0; i < sample.size(); i++) {
            for (int j = i + 1; j < sample.size(); j++) {
                if (distMatrix[i][j] > maxDist) {
                    maxDist = distMatrix[i][j];
                    best1 = i;
                    best2 = j;
                }
            }
        }

        pivots.add(sample.get(best1));
        pivots.add(sample.get(best2));
        selectedIndices.add(best1);
        selectedIndices.add(best2);

        // 依次添加使距离和最大的点
        while (pivots.size() < k) {
            double maxTotalDist = -1;
            int bestIdx = -1;

            for (int i = 0; i < sample.size(); i++) {
                if (selectedIndices.contains(i))
                    continue;

                double totalDist = 0;
                for (int idx : selectedIndices) {
                    totalDist += distMatrix[i][idx];
                }

                if (totalDist > maxTotalDist) {
                    maxTotalDist = totalDist;
                    bestIdx = i;
                }
            }

            if (bestIdx >= 0) {
                pivots.add(sample.get(bestIdx));
                selectedIndices.add(bestIdx);
            }
        }

        return pivots;
    }

    /**
     * 增量选择策略
     *
     * 类似FFT，但考虑数据分布的均匀性
     */
    private List<MetricSpaceData> incrementalSelect(List<? extends MetricSpaceData> data,
            MetricFunction metric, int k) {
        // 使用FFT作为基础实现
        return fftSelect(data, metric, k);
    }

    /**
     * 数据采样
     */
    private List<MetricSpaceData> sampleData(List<? extends MetricSpaceData> data, int sampleSize) {
        if (data.size() <= sampleSize) {
            return new ArrayList<>(data);
        }

        Set<Integer> indices = new HashSet<>();
        while (indices.size() < sampleSize) {
            indices.add(random.nextInt(data.size()));
        }

        List<MetricSpaceData> sample = new ArrayList<>();
        for (int idx : indices) {
            sample.add(data.get(idx));
        }
        return sample;
    }

    /**
     * 获取距离计算次数
     */
    public int getDistanceComputations() {
        return distanceComputations;
    }

    /**
     * 获取当前选择策略
     */
    public SelectionStrategy getStrategy() {
        return strategy;
    }

    /**
     * 设置随机种子
     */
    public void setRandomSeed(long seed) {
        this.random = new Random(seed);
    }
}
