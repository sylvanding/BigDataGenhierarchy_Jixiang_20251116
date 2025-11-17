package index;

import core.MetricFunction;
import core.MetricSpaceData;

import java.util.ArrayList;
import java.util.List;

/**
 * Pivot Table索引
 *
 * Pivot Table是一种基于三角不等式的索引结构：
 * 1. 选择k个支撑点(pivots)
 * 2. 预计算并存储每个数据对象到每个支撑点的距离
 * 3. 查询时利用三角不等式进行剪枝
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class PivotTable {
    private List<MetricSpaceData> pivots; // 支撑点列表
    private List<MetricSpaceData> dataset; // 数据集
    private double[][] distanceTable; // 距离表 [dataIndex][pivotIndex]
    private MetricFunction metric; // 距离函数

    // 统计信息
    private long buildDistanceCalculations; // 构建时的距离计算次数

    // 查询统计信息（最后一次查询）
    private long lastQueryDistanceCalculations; // 最后一次查询的距离计算次数
    private long lastQueryPruned; // 最后一次查询的剪枝数量
    private long lastQueryIncluded; // 最后一次查询的直接包含数量
    private long lastQueryVerified; // 最后一次查询的验证数量

    /**
     * 构建Pivot Table
     * @param dataset 数据集
     * @param numPivots 支撑点数量
     * @param metric 距离函数
     * @param pivotSelectionMethod 支撑点选择方法
     */
    public PivotTable(List<? extends MetricSpaceData> dataset, int numPivots, MetricFunction metric,
            PivotSelectionMethod pivotSelectionMethod) {
        this(dataset, numPivots, metric, pivotSelectionMethod, true);
    }

    /**
     * 构建Pivot Table
     * @param dataset 数据集
     * @param numPivots 支撑点数量
     * @param metric 距离函数
     * @param pivotSelectionMethod 支撑点选择方法
     * @param verbose 是否打印详细信息
     */
    public PivotTable(List<? extends MetricSpaceData> dataset, int numPivots, MetricFunction metric,
            PivotSelectionMethod pivotSelectionMethod, boolean verbose) {
        this.dataset = new ArrayList<>(dataset);
        this.metric = metric;
        this.buildDistanceCalculations = 0;

        if (verbose) {
            System.out.println("=== 开始构建Pivot Table ===");
            System.out.println("数据集大小: " + dataset.size());
            System.out.println("支撑点数量: " + numPivots);
            System.out.println("选择方法: " + pivotSelectionMethod);
        }

        long startTime = System.currentTimeMillis();

        // 选择支撑点
        this.pivots = PivotSelector.selectPivots(dataset, numPivots, pivotSelectionMethod, metric,
                verbose);

        // 构建距离表
        buildDistanceTable(verbose);

        long endTime = System.currentTimeMillis();

        if (verbose) {
            System.out.println("Pivot Table构建完成，总耗时: " + (endTime - startTime) + " ms");
            System.out.println("=================================\n");
        }
    }

    /**
     * 构建距离表
     */
    private void buildDistanceTable() {
        buildDistanceTable(true);
    }

    /**
     * 构建距离表
     * @param verbose 是否打印详细信息
     */
    private void buildDistanceTable(boolean verbose) {
        if (verbose) {
            System.out.println("构建距离表...");
        }

        int n = dataset.size();
        int k = pivots.size();

        distanceTable = new double[n][k];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k; j++) {
                distanceTable[i][j] = metric.getDistance(dataset.get(i), pivots.get(j));
                buildDistanceCalculations++;
            }

            // 显示进度
            if (verbose && (i + 1) % 10000 == 0) {
                System.out.println("  已处理: " + (i + 1) + " / " + n);
            }
        }

        long endTime = System.currentTimeMillis();

        if (verbose) {
            System.out.println("距离表构建完成:");
            System.out.println("  规模: " + n + " x " + k);
            System.out.println("  距离计算次数: " + buildDistanceCalculations);
            System.out.println("  耗时: " + (endTime - startTime) + " ms");
        }
    }

    /**
     * 获取数据到支撑点的距离
     * @param dataIndex 数据索引
     * @param pivotIndex 支撑点索引
     * @return 距离值
     */
    public double getDistanceToPivot(int dataIndex, int pivotIndex) {
        return distanceTable[dataIndex][pivotIndex];
    }

    /**
     * 获取支撑点列表
     * @return 支撑点列表
     */
    public List<MetricSpaceData> getPivots() {
        return pivots;
    }

    /**
     * 获取数据集
     * @return 数据集
     */
    public List<MetricSpaceData> getDataset() {
        return dataset;
    }

    /**
     * 获取距离函数
     * @return 距离函数
     */
    public MetricFunction getMetric() {
        return metric;
    }

    /**
     * 获取构建时的距离计算次数
     * @return 距离计算次数
     */
    public long getBuildDistanceCalculations() {
        return buildDistanceCalculations;
    }

    /**
     * 获取支撑点数量
     * @return 支撑点数量
     */
    public int getNumPivots() {
        return pivots.size();
    }

    /**
     * 获取数据集大小
     * @return 数据集大小
     */
    public int getDatasetSize() {
        return dataset.size();
    }

    /**
     * 设置最后一次查询的统计信息
     */
    public void setLastQueryStatistics(long distanceCalculations, long pruned, long included,
            long verified) {
        this.lastQueryDistanceCalculations = distanceCalculations;
        this.lastQueryPruned = pruned;
        this.lastQueryIncluded = included;
        this.lastQueryVerified = verified;
    }

    /**
     * 获取最后一次查询的距离计算次数
     */
    public long getLastQueryDistanceCalculations() {
        return lastQueryDistanceCalculations;
    }

    /**
     * 获取最后一次查询的剪枝数量
     */
    public long getLastQueryPruned() {
        return lastQueryPruned;
    }

    /**
     * 获取最后一次查询的直接包含数量
     */
    public long getLastQueryIncluded() {
        return lastQueryIncluded;
    }

    /**
     * 获取最后一次查询的验证数量
     */
    public long getLastQueryVerified() {
        return lastQueryVerified;
    }
}

