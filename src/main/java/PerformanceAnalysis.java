import core.MetricSpaceData;
import datatype.vector.MinkowskiDistance;
import datatype.vector.VectorData;
import index.*;
import io.VectorDataReader;
import query.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 性能分析与探索程序
 *
 * 本程序实现Assignment2要求的性能分析：
 * 1. 性能评估指标定义
 * 2. 支撑点数量对性能的影响分析
 * 3. 支撑点选择策略对性能的影响分析
 * 4. 查询性能对比：线性扫描 vs. Pivot Table索引
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class PerformanceAnalysis {

    // 实验配置
    private static final int[] DATASET_SIZES = {1000, 5000, 10000};
    private static final int[] PIVOT_COUNTS = {5, 10, 15, 20, 25, 30};
    private static final double[] QUERY_RADII = {0.05, 0.1, 0.15, 0.2};
    private static final int[] KNN_VALUES = {5, 10, 20, 50};
    private static final int NUM_QUERIES = 10; // 每个配置执行的查询次数

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("  度量空间索引性能分析与探索");
        System.out.println("====================================================\n");

        try {
            // 加载数据集
            System.out.println("正在加载数据集...");
            String datasetPath = "UMAD-Dataset/full/Vector/unziped/uniformvector-20dim-1m.txt";

            // 检查文件是否存在
            try {
                List<VectorData> testRead = VectorDataReader.readFromFile(datasetPath, 10);
                if (testRead.isEmpty()) {
                    System.out.println("错误：无法读取数据文件：" + datasetPath);
                    System.out.println("请确保数据文件已解压到正确位置");
                    return;
                }
            } catch (IOException e) {
                System.out.println("错误：无法读取数据文件：" + datasetPath);
                System.out.println("请确保数据文件已解压到正确位置");
                System.out.println("详细错误信息：" + e.getMessage());
                return;
            }

            System.out.println("数据集路径: " + datasetPath);
            System.out.println("数据加载成功！\n");

            // 实验1：支撑点数量对性能的影响（范围查询）
            System.out.println("\n" + "=".repeat(60));
            System.out.println("实验1: 支撑点数量对范围查询性能的影响");
            System.out.println("=".repeat(60));
            analyzePivotCountImpactOnRangeQuery(datasetPath);

            // 实验2：支撑点数量对性能的影响（kNN查询）
            System.out.println("\n" + "=".repeat(60));
            System.out.println("实验2: 支撑点数量对kNN查询性能的影响");
            System.out.println("=".repeat(60));
            analyzePivotCountImpactOnKNNQuery(datasetPath);

            // 实验3：支撑点选择策略对性能的影响
            System.out.println("\n" + "=".repeat(60));
            System.out.println("实验3: 支撑点选择策略对性能的影响");
            System.out.println("=".repeat(60));
            analyzePivotSelectionStrategies(datasetPath);

            // 实验4：线性扫描 vs Pivot Table性能对比（范围查询）
            System.out.println("\n" + "=".repeat(60));
            System.out.println("实验4: 线性扫描 vs Pivot Table (范围查询)");
            System.out.println("=".repeat(60));
            compareLinearScanVsPivotTableRange(datasetPath);

            // 实验5：线性扫描 vs Pivot Table性能对比（kNN查询）
            System.out.println("\n" + "=".repeat(60));
            System.out.println("实验5: 线性扫描 vs Pivot Table (kNN查询)");
            System.out.println("=".repeat(60));
            compareLinearScanVsPivotTableKNN(datasetPath);

            System.out.println("\n" + "=".repeat(60));
            System.out.println("所有性能分析实验完成！");
            System.out.println("=".repeat(60));

        } catch (Exception e) {
            System.err.println("性能分析过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 实验1：分析支撑点数量对范围查询性能的影响
     */
    private static void analyzePivotCountImpactOnRangeQuery(String datasetPath) throws IOException {
        System.out.println("\n【实验设置】");
        System.out.println("  数据集大小: " + DATASET_SIZES[1] + " 个向量");
        System.out.println("  支撑点数量: " + formatArray(PIVOT_COUNTS));
        System.out.println("  查询半径: " + formatArray(QUERY_RADII));
        System.out.println("  选择策略: FFT");
        System.out.println("  查询次数: " + NUM_QUERIES + " 次（取平均值）");

        int datasetSize = DATASET_SIZES[1]; // 使用5000个数据点
        List<VectorData> dataset = VectorDataReader.readFromFile(datasetPath, datasetSize, false);

        System.out.println("\n【实验结果表格】");
        System.out.println(String.format("%-10s %-10s %-15s %-15s %-15s %-15s %-10s", "支撑点数",
                "查询半径", "平均查询时间(ms)", "平均距离计算", "平均剪枝数", "平均剪枝率(%)", "结果数"));
        System.out.println("-".repeat(110));

        int totalTests = PIVOT_COUNTS.length * QUERY_RADII.length;
        int currentTest = 0;

        for (int numPivots : PIVOT_COUNTS) {
            for (double radius : QUERY_RADII) {
                currentTest++;
                System.out.print("\r进度: " + currentTest + "/" + totalTests + " ");
                System.out.flush();
                // 构建Pivot Table (静默模式)
                PivotTable pivotTable = new PivotTable(dataset, numPivots, MinkowskiDistance.L2,
                        PivotSelectionMethod.FFT, false);

                // 生成查询对象
                List<VectorData> queries = generateRandomQueries(dataset, NUM_QUERIES);

                // 执行查询并统计
                long totalTime = 0;
                long totalDistCalc = 0;
                long totalPruned = 0;
                int totalResults = 0;

                for (VectorData query : queries) {
                    RangeQuery rangeQuery = new RangeQuery(query, radius);

                    long startTime = System.nanoTime();
                    List<MetricSpaceData> results =
                            PivotTableRangeQuery.execute(pivotTable, rangeQuery, false);
                    long endTime = System.nanoTime();

                    totalTime += (endTime - startTime);
                    totalDistCalc += pivotTable.getLastQueryDistanceCalculations();
                    totalPruned += pivotTable.getLastQueryPruned();
                    totalResults += results.size();
                }

                double avgTime = totalTime / (double) NUM_QUERIES / 1_000_000.0; // 转换为毫秒
                double avgDistCalc = totalDistCalc / (double) NUM_QUERIES;
                double avgPruned = totalPruned / (double) NUM_QUERIES;
                double avgPruneRate = (avgPruned / datasetSize) * 100.0;
                double avgResults = totalResults / (double) NUM_QUERIES;

                System.out.print("\r");
                System.out.println(String.format(
                        "%-10d %-10.2f %-15.3f %-15.1f %-15.1f %-15.2f %-10.1f", numPivots, radius,
                        avgTime, avgDistCalc, avgPruned, avgPruneRate, avgResults));
            }
        }
        System.out.println();
    }

    /**
     * 实验2：分析支撑点数量对kNN查询性能的影响
     */
    private static void analyzePivotCountImpactOnKNNQuery(String datasetPath) throws IOException {
        System.out.println("\n【实验设置】");
        System.out.println("  数据集大小: " + DATASET_SIZES[1] + " 个向量");
        System.out.println("  支撑点数量: " + formatArray(PIVOT_COUNTS));
        System.out.println("  k值: " + formatArray(KNN_VALUES));
        System.out.println("  选择策略: FFT");
        System.out.println("  查询次数: " + NUM_QUERIES + " 次（取平均值）");

        int datasetSize = DATASET_SIZES[1];
        List<VectorData> dataset = VectorDataReader.readFromFile(datasetPath, datasetSize, false);

        System.out.println("\n【实验结果表格】");
        System.out.println(String.format("%-10s %-10s %-15s %-15s %-15s %-15s", "支撑点数", "k值",
                "平均查询时间(ms)", "平均距离计算", "平均剪枝数", "平均剪枝率(%)"));
        System.out.println("-".repeat(95));

        int totalTests = PIVOT_COUNTS.length * KNN_VALUES.length;
        int currentTest = 0;

        for (int numPivots : PIVOT_COUNTS) {
            for (int k : KNN_VALUES) {
                currentTest++;
                System.out.print("\r进度: " + currentTest + "/" + totalTests + " ");
                System.out.flush();
                // 构建Pivot Table (静默模式)
                PivotTable pivotTable = new PivotTable(dataset, numPivots, MinkowskiDistance.L2,
                        PivotSelectionMethod.FFT, false);

                // 生成查询对象
                List<VectorData> queries = generateRandomQueries(dataset, NUM_QUERIES);

                // 执行查询并统计
                long totalTime = 0;
                long totalDistCalc = 0;
                long totalPruned = 0;

                for (VectorData query : queries) {
                    KNNQuery knnQuery = new KNNQuery(query, k);

                    long startTime = System.nanoTime();
                    List<KNNResult> results =
                            PivotTableKNNQuery.execute(pivotTable, knnQuery, false);
                    long endTime = System.nanoTime();

                    totalTime += (endTime - startTime);
                    totalDistCalc += pivotTable.getLastQueryDistanceCalculations();
                    totalPruned += pivotTable.getLastQueryPruned();
                }

                double avgTime = totalTime / (double) NUM_QUERIES / 1_000_000.0;
                double avgDistCalc = totalDistCalc / (double) NUM_QUERIES;
                double avgPruned = totalPruned / (double) NUM_QUERIES;
                double avgPruneRate = (avgPruned / datasetSize) * 100.0;

                System.out.print("\r");
                System.out.println(String.format("%-10d %-10d %-15.3f %-15.1f %-15.1f %-15.2f",
                        numPivots, k, avgTime, avgDistCalc, avgPruned, avgPruneRate));
            }
        }
        System.out.println();
    }

    /**
     * 实验3：分析不同支撑点选择策略对性能的影响
     */
    private static void analyzePivotSelectionStrategies(String datasetPath) throws IOException {
        System.out.println("\n【实验设置】");
        System.out.println("  数据集大小: " + DATASET_SIZES[1] + " 个向量");
        System.out.println("  支撑点数量: 20");
        System.out.println("  选择策略: RANDOM, FFT");
        System.out.println("  查询类型: 范围查询(radius=0.1) 和 kNN查询(k=10)");
        System.out.println("  查询次数: " + NUM_QUERIES + " 次（取平均值）");

        int datasetSize = DATASET_SIZES[1];
        int numPivots = 20;
        double radius = 0.1;
        int k = 10;

        List<VectorData> dataset = VectorDataReader.readFromFile(datasetPath, datasetSize, false);

        System.out.println("\n【范围查询性能对比】");
        System.out.println(String.format("%-15s %-15s %-15s %-15s %-15s %-15s", "选择策略", "构建时间(ms)",
                "平均查询时间(ms)", "平均距离计算", "平均剪枝数", "平均剪枝率(%)"));
        System.out.println("-".repeat(105));

        int testCount = 0;
        for (PivotSelectionMethod method : new PivotSelectionMethod[] {PivotSelectionMethod.RANDOM,
                PivotSelectionMethod.FFT}) {
            testCount++;
            System.out.print("正在测试 " + method.name() + "... ");
            System.out.flush();

            // 构建Pivot Table并测量构建时间 (静默模式)
            long buildStart = System.nanoTime();
            PivotTable pivotTable =
                    new PivotTable(dataset, numPivots, MinkowskiDistance.L2, method, false);
            long buildEnd = System.nanoTime();
            double buildTime = (buildEnd - buildStart) / 1_000_000.0;

            // 生成查询对象
            List<VectorData> queries = generateRandomQueries(dataset, NUM_QUERIES);

            // 执行范围查询
            long totalTime = 0;
            long totalDistCalc = 0;
            long totalPruned = 0;

            for (VectorData query : queries) {
                RangeQuery rangeQuery = new RangeQuery(query, radius);

                long startTime = System.nanoTime();
                List<MetricSpaceData> results =
                        PivotTableRangeQuery.execute(pivotTable, rangeQuery, false);
                long endTime = System.nanoTime();

                totalTime += (endTime - startTime);
                totalDistCalc += pivotTable.getLastQueryDistanceCalculations();
                totalPruned += pivotTable.getLastQueryPruned();
            }

            double avgTime = totalTime / (double) NUM_QUERIES / 1_000_000.0;
            double avgDistCalc = totalDistCalc / (double) NUM_QUERIES;
            double avgPruned = totalPruned / (double) NUM_QUERIES;
            double avgPruneRate = (avgPruned / datasetSize) * 100.0;

            System.out.print("\r");
            System.out.println(String.format("%-15s %-15.3f %-15.3f %-15.1f %-15.1f %-15.2f",
                    method.name(), buildTime, avgTime, avgDistCalc, avgPruned, avgPruneRate));
        }

        System.out.println("\n【kNN查询性能对比】");
        System.out.println(String.format("%-15s %-15s %-15s %-15s %-15s %-15s", "选择策略", "构建时间(ms)",
                "平均查询时间(ms)", "平均距离计算", "平均剪枝数", "平均剪枝率(%)"));
        System.out.println("-".repeat(105));

        testCount = 0;
        for (PivotSelectionMethod method : new PivotSelectionMethod[] {PivotSelectionMethod.RANDOM,
                PivotSelectionMethod.FFT}) {
            testCount++;
            System.out.print("正在测试 " + method.name() + "... ");
            System.out.flush();

            // 构建Pivot Table (静默模式)
            long buildStart = System.nanoTime();
            PivotTable pivotTable =
                    new PivotTable(dataset, numPivots, MinkowskiDistance.L2, method, false);
            long buildEnd = System.nanoTime();
            double buildTime = (buildEnd - buildStart) / 1_000_000.0;

            // 生成查询对象
            List<VectorData> queries = generateRandomQueries(dataset, NUM_QUERIES);

            // 执行kNN查询
            long totalTime = 0;
            long totalDistCalc = 0;
            long totalPruned = 0;

            for (VectorData query : queries) {
                KNNQuery knnQuery = new KNNQuery(query, k);

                long startTime = System.nanoTime();
                List<KNNResult> results = PivotTableKNNQuery.execute(pivotTable, knnQuery, false);
                long endTime = System.nanoTime();

                totalTime += (endTime - startTime);
                totalDistCalc += pivotTable.getLastQueryDistanceCalculations();
                totalPruned += pivotTable.getLastQueryPruned();
            }

            double avgTime = totalTime / (double) NUM_QUERIES / 1_000_000.0;
            double avgDistCalc = totalDistCalc / (double) NUM_QUERIES;
            double avgPruned = totalPruned / (double) NUM_QUERIES;
            double avgPruneRate = (avgPruned / datasetSize) * 100.0;

            System.out.print("\r");
            System.out.println(String.format("%-15s %-15.3f %-15.3f %-15.1f %-15.1f %-15.2f",
                    method.name(), buildTime, avgTime, avgDistCalc, avgPruned, avgPruneRate));
        }
    }

    /**
     * 实验4：线性扫描 vs Pivot Table 性能对比（范围查询）
     */
    private static void compareLinearScanVsPivotTableRange(String datasetPath) throws IOException {
        System.out.println("\n【实验设置】");
        System.out.println("  数据集大小: " + formatArray(DATASET_SIZES));
        System.out.println("  支撑点数量: 20 (FFT策略)");
        System.out.println("  查询半径: " + formatArray(QUERY_RADII));
        System.out.println("  查询次数: " + NUM_QUERIES + " 次（取平均值）");

        System.out.println("\n【性能对比表格】");
        System.out.println(String.format("%-12s %-10s %-18s %-18s %-15s %-15s", "数据集大小", "查询半径",
                "线性扫描时间(ms)", "索引查询时间(ms)", "加速比", "剪枝率(%)"));
        System.out.println("-".repeat(105));

        int numPivots = 20;
        int totalTests = DATASET_SIZES.length * QUERY_RADII.length;
        int currentTest = 0;

        for (int datasetSize : DATASET_SIZES) {
            System.out.println(">> 正在加载 " + datasetSize + " 个数据点...");
            List<VectorData> dataset =
                    VectorDataReader.readFromFile(datasetPath, datasetSize, false);

            // 构建Pivot Table (静默模式)
            System.out.println(">> 正在构建Pivot Table...");
            PivotTable pivotTable = new PivotTable(dataset, numPivots, MinkowskiDistance.L2,
                    PivotSelectionMethod.FFT, false);

            for (double radius : QUERY_RADII) {
                currentTest++;
                System.out.print("\r>> 进度: " + currentTest + "/" + totalTests + " ");
                System.out.flush();
                // 生成查询对象
                List<VectorData> queries = generateRandomQueries(dataset, NUM_QUERIES);

                // 线性扫描
                long lsTotalTime = 0;
                for (VectorData query : queries) {
                    RangeQuery rangeQuery = new RangeQuery(query, radius);

                    long startTime = System.nanoTime();
                    List<MetricSpaceData> results = LinearScanRangeQuery.execute(dataset,
                            rangeQuery, MinkowskiDistance.L2, false);
                    long endTime = System.nanoTime();

                    lsTotalTime += (endTime - startTime);
                }
                double lsAvgTime = lsTotalTime / (double) NUM_QUERIES / 1_000_000.0;

                // Pivot Table查询
                long ptTotalTime = 0;
                long totalPruned = 0;
                for (VectorData query : queries) {
                    RangeQuery rangeQuery = new RangeQuery(query, radius);

                    long startTime = System.nanoTime();
                    List<MetricSpaceData> results =
                            PivotTableRangeQuery.execute(pivotTable, rangeQuery, false);
                    long endTime = System.nanoTime();

                    ptTotalTime += (endTime - startTime);
                    totalPruned += pivotTable.getLastQueryPruned();
                }
                double ptAvgTime = ptTotalTime / (double) NUM_QUERIES / 1_000_000.0;
                double avgPruned = totalPruned / (double) NUM_QUERIES;
                double pruneRate = (avgPruned / datasetSize) * 100.0;

                double speedup = lsAvgTime / ptAvgTime;

                System.out.print("\r");
                System.out.println(String.format("%-12d %-10.2f %-18.3f %-18.3f %-15.2fx %-15.2f",
                        datasetSize, radius, lsAvgTime, ptAvgTime, speedup, pruneRate));
            }
        }
        System.out.println();
    }

    /**
     * 实验5：线性扫描 vs Pivot Table 性能对比（kNN查询）
     */
    private static void compareLinearScanVsPivotTableKNN(String datasetPath) throws IOException {
        System.out.println("\n【实验设置】");
        System.out.println("  数据集大小: " + formatArray(DATASET_SIZES));
        System.out.println("  支撑点数量: 20 (FFT策略)");
        System.out.println("  k值: " + formatArray(KNN_VALUES));
        System.out.println("  查询次数: " + NUM_QUERIES + " 次（取平均值）");

        System.out.println("\n【性能对比表格】");
        System.out.println(String.format("%-12s %-10s %-18s %-18s %-15s %-15s", "数据集大小", "k值",
                "线性扫描时间(ms)", "索引查询时间(ms)", "加速比", "剪枝率(%)"));
        System.out.println("-".repeat(105));

        int numPivots = 20;
        int totalTests = DATASET_SIZES.length * KNN_VALUES.length;
        int currentTest = 0;

        for (int datasetSize : DATASET_SIZES) {
            System.out.println(">> 正在加载 " + datasetSize + " 个数据点...");
            List<VectorData> dataset =
                    VectorDataReader.readFromFile(datasetPath, datasetSize, false);

            // 构建Pivot Table (静默模式)
            System.out.println(">> 正在构建Pivot Table...");
            PivotTable pivotTable = new PivotTable(dataset, numPivots, MinkowskiDistance.L2,
                    PivotSelectionMethod.FFT, false);

            for (int k : KNN_VALUES) {
                currentTest++;
                System.out.print("\r>> 进度: " + currentTest + "/" + totalTests + " ");
                System.out.flush();
                // 生成查询对象
                List<VectorData> queries = generateRandomQueries(dataset, NUM_QUERIES);

                // 线性扫描
                long lsTotalTime = 0;
                for (VectorData query : queries) {
                    KNNQuery knnQuery = new KNNQuery(query, k);

                    long startTime = System.nanoTime();
                    List<KNNResult> results = LinearScanKNNQuery.execute(dataset, knnQuery,
                            MinkowskiDistance.L2, false);
                    long endTime = System.nanoTime();

                    lsTotalTime += (endTime - startTime);
                }
                double lsAvgTime = lsTotalTime / (double) NUM_QUERIES / 1_000_000.0;

                // Pivot Table查询
                long ptTotalTime = 0;
                long totalPruned = 0;
                for (VectorData query : queries) {
                    KNNQuery knnQuery = new KNNQuery(query, k);

                    long startTime = System.nanoTime();
                    List<KNNResult> results =
                            PivotTableKNNQuery.execute(pivotTable, knnQuery, false);
                    long endTime = System.nanoTime();

                    ptTotalTime += (endTime - startTime);
                    totalPruned += pivotTable.getLastQueryPruned();
                }
                double ptAvgTime = ptTotalTime / (double) NUM_QUERIES / 1_000_000.0;
                double avgPruned = totalPruned / (double) NUM_QUERIES;
                double pruneRate = (avgPruned / datasetSize) * 100.0;

                double speedup = lsAvgTime / ptAvgTime;

                System.out.print("\r");
                System.out.println(String.format("%-12d %-10d %-18.3f %-18.3f %-15.2fx %-15.2f",
                        datasetSize, k, lsAvgTime, ptAvgTime, speedup, pruneRate));
            }
        }
        System.out.println();
    }

    /**
     * 生成随机查询对象
     */
    private static List<VectorData> generateRandomQueries(List<VectorData> dataset,
            int numQueries) {
        List<VectorData> queries = new ArrayList<>();
        Random random = new Random(42); // 使用固定种子以保证可重复性

        for (int i = 0; i < numQueries; i++) {
            int idx = random.nextInt(dataset.size());
            queries.add(dataset.get(idx));
        }

        return queries;
    }

    /**
     * 格式化整数数组为字符串
     */
    private static String formatArray(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 格式化浮点数组为字符串
     */
    private static String formatArray(double[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(String.format("%.2f", arr[i]));
            if (i < arr.length - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}

