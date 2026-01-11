package examples.assignment3;

import core.MetricSpaceData;
import core.MetricFunction;
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import io.VectorDataReader;
import index.tree.ghtree.GHTree;
import index.tree.vptree.VPTree;
import index.tree.common.TreeConfig;
import query.RangeQuery;
import query.LinearScanRangeQuery;
import query.KNNResult;

import java.util.*;

/**
 * Assignment 3: GH树和VP树功能演示程序
 *
 * 本程序演示：
 * 1. GH树和VP树的构建过程
 * 2. 树结构的可视化展示
 * 3. 范围查询的执行与验证
 * 4. kNN查询的执行与验证
 * 5. 与线性扫描结果的对比验证
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class TreeDemo {

    private static final String SEPARATOR = "=".repeat(70);
    private static final String SUB_SEPARATOR = "-".repeat(50);

    public static void main(String[] args) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("    Assignment 3: GH树 (超平面树) 与 VP树 (优势点树) 演示");
        System.out.println(SEPARATOR);
        System.out.println();

        try {
            // 1. 创建小规模测试数据（用于详细展示）
            demonstrateWithSmallData();

            // 2. 使用文件数据进行演示
            demonstrateWithFileData();

        } catch (Exception e) {
            System.err.println("演示过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("演示完成！");
        System.out.println(SEPARATOR);
    }

    /**
     * 使用小规模数据演示（便于手工验证）
     */
    private static void demonstrateWithSmallData() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("第一部分：使用小规模数据演示（便于手工验证）");
        System.out.println(SEPARATOR);

        // 创建简单的2D向量数据
        List<VectorData> dataset = createSimpleDataset();
        MetricFunction metric = MinkowskiDistance.L2;

        System.out.println("\n【1. 测试数据集】");
        System.out.println(SUB_SEPARATOR);
        System.out.println("数据类型: 2维向量");
        System.out.println("距离函数: 欧几里得距离 (L2)");
        System.out.println("数据数量: " + dataset.size());
        System.out.println("\n数据点列表:");
        for (VectorData v : dataset) {
            double[] coords = v.getCoordinates();
            System.out.printf("  ID=%d: (%.1f, %.1f)%n", v.getDataId(), coords[0], coords[1]);
        }

        // 构建GH树（详细模式）
        System.out.println("\n【2. GH树构建演示】");
        System.out.println(SUB_SEPARATOR);

        TreeConfig ghConfig = new TreeConfig.Builder().maxLeafSize(3).minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42) // 固定随机种子，便于重现
                .verbose(true).build();

        GHTree ghTree = new GHTree(ghConfig);
        ghTree.buildIndex(dataset, metric);

        System.out.println("\nGH树结构:");
        ghTree.printTree();

        // 构建VP树（详细模式）
        System.out.println("\n【3. VP树构建演示】");
        System.out.println(SUB_SEPARATOR);

        TreeConfig vpConfig = new TreeConfig.Builder().maxLeafSize(3).minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42) // 使用相同随机种子
                .verbose(true).build();

        VPTree vpTree = new VPTree(vpConfig);
        vpTree.buildIndex(dataset, metric);

        System.out.println("\nVP树结构:");
        vpTree.printTree();

        // 范围查询演示
        System.out.println("\n【4. 范围查询演示】");
        System.out.println(SUB_SEPARATOR);

        VectorData queryPoint = new VectorData(100, new double[] {5.0, 5.0});
        double radius = 3.0;

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("查询半径: " + radius);
        System.out.println();

        // 线性扫描结果（作为基准）
        System.out.println("--- 线性扫描结果（基准） ---");
        ghTree.resetStatistics();
        List<MetricSpaceData> linearResults = LinearScanRangeQuery.execute(dataset,
                new RangeQuery(queryPoint, radius), metric, false);
        System.out.println("结果数量: " + linearResults.size());
        System.out.println("距离计算次数: " + dataset.size());
        printRangeResults(linearResults, queryPoint, metric);

        // GH树查询
        System.out.println("\n--- GH树范围查询 ---");
        ghTree.resetStatistics();
        List<MetricSpaceData> ghResults = ghTree.rangeQuery(queryPoint, radius);
        System.out.println("结果数量: " + ghResults.size());
        System.out.println("距离计算次数: " + ghTree.getDistanceComputations());
        System.out.println("节点访问次数: " + ghTree.getNodeAccesses());
        printRangeResults(ghResults, queryPoint, metric);

        // VP树查询
        System.out.println("\n--- VP树范围查询 ---");
        vpTree.resetStatistics();
        List<MetricSpaceData> vpResults = vpTree.rangeQuery(queryPoint, radius);
        System.out.println("结果数量: " + vpResults.size());
        System.out.println("距离计算次数: " + vpTree.getDistanceComputations());
        System.out.println("节点访问次数: " + vpTree.getNodeAccesses());
        printRangeResults(vpResults, queryPoint, metric);

        // 验证结果一致性
        System.out.println("\n--- 结果一致性验证 ---");
        boolean consistent = verifyResultConsistency(linearResults, ghResults, vpResults);
        System.out.println("GH树结果与线性扫描一致: " + (setEquals(linearResults, ghResults) ? "✓" : "✗"));
        System.out.println("VP树结果与线性扫描一致: " + (setEquals(linearResults, vpResults) ? "✓" : "✗"));

        // kNN查询演示
        System.out.println("\n【5. kNN查询演示】");
        System.out.println(SUB_SEPARATOR);

        int k = 3;
        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("k = " + k);
        System.out.println();

        // GH树kNN
        System.out.println("--- GH树kNN查询 ---");
        ghTree.resetStatistics();
        List<MetricSpaceData> ghKnnResults = ghTree.knnQuery(queryPoint, k);
        System.out.println("距离计算次数: " + ghTree.getDistanceComputations());
        System.out.println("节点访问次数: " + ghTree.getNodeAccesses());
        printKnnResults(ghKnnResults, queryPoint, metric);

        // VP树kNN
        System.out.println("\n--- VP树kNN查询 ---");
        vpTree.resetStatistics();
        List<MetricSpaceData> vpKnnResults = vpTree.knnQuery(queryPoint, k);
        System.out.println("距离计算次数: " + vpTree.getDistanceComputations());
        System.out.println("节点访问次数: " + vpTree.getNodeAccesses());
        printKnnResults(vpKnnResults, queryPoint, metric);
    }

    /**
     * 使用文件数据进行演示
     */
    private static void demonstrateWithFileData() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("第二部分：使用文件数据演示");
        System.out.println(SEPARATOR);

        String[] testFiles = {"UMAD-Dataset/full/Vector/unzipped/clusteredvector-2d-100k-100c.txt",
                "UMAD-Dataset/examples/Vector/test_vectors_2d.txt"};

        String filePath = null;
        for (String path : testFiles) {
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                filePath = path;
                break;
            }
        }

        if (filePath == null) {
            System.out.println("\n⚠ 未找到测试数据文件，跳过文件数据演示。");
            System.out.println("请确保以下文件之一存在:");
            for (String path : testFiles) {
                System.out.println("  - " + path);
            }
            return;
        }

        try {
            // 读取数据
            System.out.println("\n【1. 加载数据】");
            System.out.println(SUB_SEPARATOR);
            int maxCount = 5000; // 限制数据量便于演示
            List<VectorData> dataset = VectorDataReader.readFromFile(filePath, maxCount, true);
            MetricFunction metric = MinkowskiDistance.L2;

            // 构建GH树
            System.out.println("\n【2. 构建GH树】");
            System.out.println(SUB_SEPARATOR);

            TreeConfig ghConfig = new TreeConfig.Builder().maxLeafSize(50).minTreeHeight(3)
                    .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42)
                    .verbose(false).build();

            GHTree ghTree = new GHTree(ghConfig);
            long startTime = System.currentTimeMillis();
            ghTree.buildIndex(dataset, metric);
            long ghBuildTime = System.currentTimeMillis() - startTime;

            System.out.println("构建时间: " + ghBuildTime + " ms");
            System.out.println("树高度: " + ghTree.getTreeHeight());
            System.out.println("总节点数: " + ghTree.getTotalNodes());
            System.out.println("叶子节点数: " + ghTree.getLeafNodes());
            System.out.println("构建距离计算: " + ghTree.getBuildDistanceComputations());

            // 构建VP树
            System.out.println("\n【3. 构建VP树】");
            System.out.println(SUB_SEPARATOR);

            TreeConfig vpConfig = new TreeConfig.Builder().maxLeafSize(50).minTreeHeight(3)
                    .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42)
                    .verbose(false).build();

            VPTree vpTree = new VPTree(vpConfig);
            startTime = System.currentTimeMillis();
            vpTree.buildIndex(dataset, metric);
            long vpBuildTime = System.currentTimeMillis() - startTime;

            System.out.println("构建时间: " + vpBuildTime + " ms");
            System.out.println("树高度: " + vpTree.getTreeHeight());
            System.out.println("总节点数: " + vpTree.getTotalNodes());
            System.out.println("叶子节点数: " + vpTree.getLeafNodes());
            System.out.println("构建距离计算: " + vpTree.getBuildDistanceComputations());

            // 范围查询性能对比
            System.out.println("\n【4. 范围查询性能对比】");
            System.out.println(SUB_SEPARATOR);

            VectorData queryPoint = dataset.get(0); // 使用第一个数据点作为查询点
            double[] radii = {0.5, 1.0, 2.0};

            System.out.println("查询点: " + queryPoint);
            System.out.println();

            System.out.printf("%-10s | %-12s | %-12s | %-12s | %-12s | %-12s%n", "半径", "线性扫描",
                    "GH树", "VP树", "GH剪枝率", "VP剪枝率");
            System.out.println("-".repeat(80));

            for (double radius : radii) {
                // 线性扫描
                List<MetricSpaceData> linearResults = LinearScanRangeQuery.execute(dataset,
                        new RangeQuery(queryPoint, radius), metric, false);
                int linearDist = dataset.size();

                // GH树
                ghTree.resetStatistics();
                List<MetricSpaceData> ghResults = ghTree.rangeQuery(queryPoint, radius);
                int ghDist = ghTree.getDistanceComputations();

                // VP树
                vpTree.resetStatistics();
                List<MetricSpaceData> vpResults = vpTree.rangeQuery(queryPoint, radius);
                int vpDist = vpTree.getDistanceComputations();

                // 计算剪枝率
                double ghPruneRate = 100.0 * (1.0 - (double) ghDist / linearDist);
                double vpPruneRate = 100.0 * (1.0 - (double) vpDist / linearDist);

                System.out.printf("%-10.1f | %-12d | %-12d | %-12d | %-11.1f%% | %-11.1f%%%n",
                        radius, linearDist, ghDist, vpDist, ghPruneRate, vpPruneRate);

                // 验证结果一致性
                if (!setEquals(linearResults, ghResults)) {
                    System.out.println("  ⚠ GH树结果与线性扫描不一致！");
                }
                if (!setEquals(linearResults, vpResults)) {
                    System.out.println("  ⚠ VP树结果与线性扫描不一致！");
                }
            }

            // kNN查询性能对比
            System.out.println("\n【5. kNN查询性能对比】");
            System.out.println(SUB_SEPARATOR);

            int[] kValues = {5, 10, 20};

            System.out.println("查询点: " + queryPoint);
            System.out.println();

            System.out.printf("%-6s | %-12s | %-12s | %-12s | %-12s%n", "k", "线性扫描", "GH树", "VP树",
                    "GH剪枝率");
            System.out.println("-".repeat(65));

            for (int k : kValues) {
                // 线性扫描
                int linearDist = dataset.size();

                // GH树
                ghTree.resetStatistics();
                List<MetricSpaceData> ghResults = ghTree.knnQuery(queryPoint, k);
                int ghDist = ghTree.getDistanceComputations();

                // VP树
                vpTree.resetStatistics();
                List<MetricSpaceData> vpResults = vpTree.knnQuery(queryPoint, k);
                int vpDist = vpTree.getDistanceComputations();

                double ghPruneRate = 100.0 * (1.0 - (double) ghDist / linearDist);
                double vpPruneRate = 100.0 * (1.0 - (double) vpDist / linearDist);

                System.out.printf("%-6d | %-12d | %-12d | %-12d | %-11.1f%%%n", k, linearDist,
                        ghDist, vpDist, ghPruneRate);
            }

        } catch (Exception e) {
            System.err.println("加载文件数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建简单的测试数据集
     */
    private static List<VectorData> createSimpleDataset() {
        List<VectorData> dataset = new ArrayList<>();

        // 创建一组易于理解的2D点
        double[][] points = {{1.0, 1.0}, {2.0, 2.0}, {3.0, 1.0}, {4.0, 4.0}, {5.0, 2.0}, {6.0, 5.0},
                {7.0, 3.0}, {8.0, 6.0}, {9.0, 4.0}, {10.0, 7.0}, {3.0, 5.0}, {6.0, 1.0}, {2.0, 7.0},
                {8.0, 2.0}, {4.0, 8.0}};

        for (int i = 0; i < points.length; i++) {
            dataset.add(new VectorData(i, points[i]));
        }

        return dataset;
    }

    /**
     * 打印范围查询结果
     */
    private static void printRangeResults(List<MetricSpaceData> results, MetricSpaceData query,
            MetricFunction metric) {
        System.out.println("结果详情:");

        // 计算距离并排序
        List<Map.Entry<MetricSpaceData, Double>> sortedResults = new ArrayList<>();
        for (MetricSpaceData data : results) {
            double dist = metric.getDistance(query, data);
            sortedResults.add(new AbstractMap.SimpleEntry<>(data, dist));
        }
        sortedResults.sort(Comparator.comparingDouble(Map.Entry::getValue));

        for (Map.Entry<MetricSpaceData, Double> entry : sortedResults) {
            VectorData v = (VectorData) entry.getKey();
            double[] coords = v.getCoordinates();
            System.out.printf("  ID=%d: (%.1f, %.1f), 距离=%.4f%n", v.getDataId(), coords[0],
                    coords[1], entry.getValue());
        }
    }

    /**
     * 打印kNN查询结果
     */
    private static void printKnnResults(List<MetricSpaceData> results, MetricSpaceData query,
            MetricFunction metric) {
        System.out.println("前" + results.size() + "个最近邻:");

        int rank = 1;
        for (MetricSpaceData data : results) {
            double dist = metric.getDistance(query, data);
            VectorData v = (VectorData) data;
            double[] coords = v.getCoordinates();
            System.out.printf("  #%d: ID=%d (%.1f, %.1f), 距离=%.4f%n", rank++, v.getDataId(),
                    coords[0], coords[1], dist);
        }
    }

    /**
     * 验证结果一致性
     */
    private static boolean verifyResultConsistency(List<MetricSpaceData> linear,
            List<MetricSpaceData> gh, List<MetricSpaceData> vp) {
        return setEquals(linear, gh) && setEquals(linear, vp);
    }

    /**
     * 检查两个结果集是否包含相同的元素（忽略顺序）
     */
    private static boolean setEquals(List<MetricSpaceData> list1, List<MetricSpaceData> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        Set<Integer> ids1 = new HashSet<>();
        Set<Integer> ids2 = new HashSet<>();

        for (MetricSpaceData d : list1) {
            ids1.add(d.getDataId());
        }
        for (MetricSpaceData d : list2) {
            ids2.add(d.getDataId());
        }

        return ids1.equals(ids2);
    }
}
