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

    /**
     * 构建Pivot Table
     * @param dataset 数据集
     * @param numPivots 支撑点数量
     * @param metric 距离函数
     * @param pivotSelectionMethod 支撑点选择方法
     */
    public PivotTable(List<? extends MetricSpaceData> dataset, int numPivots, MetricFunction metric,
            PivotSelectionMethod pivotSelectionMethod) {
        this.dataset = new ArrayList<>(dataset);
        this.metric = metric;
        this.buildDistanceCalculations = 0;

        System.out.println("=== 开始构建Pivot Table ===");
        System.out.println("数据集大小: " + dataset.size());
        System.out.println("支撑点数量: " + numPivots);
        System.out.println("选择方法: " + pivotSelectionMethod);

        long startTime = System.currentTimeMillis();

        // 选择支撑点
        this.pivots = PivotSelector.selectPivots(dataset, numPivots, pivotSelectionMethod, metric);

        // 构建距离表
        buildDistanceTable();

        long endTime = System.currentTimeMillis();
        System.out.println("Pivot Table构建完成，总耗时: " + (endTime - startTime) + " ms");
        System.out.println("=================================\n");
    }

    /**
     * 构建距离表
     */
    private void buildDistanceTable() {
        System.out.println("构建距离表...");

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
            if ((i + 1) % 10000 == 0) {
                System.out.println("  已处理: " + (i + 1) + " / " + n);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("距离表构建完成:");
        System.out.println("  规模: " + n + " x " + k);
        System.out.println("  距离计算次数: " + buildDistanceCalculations);
        System.out.println("  耗时: " + (endTime - startTime) + " ms");
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
}

