package examples.assignment3;

import core.MetricSpaceData;
import core.MetricFunction;
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import datatype.protein.ProteinData;
import datatype.protein.AlignmentDistance;
import io.VectorDataReader;
import io.ProteinDataReader;
import index.tree.ghtree.GHTree;
import index.tree.vptree.VPTree;
import index.tree.common.TreeConfig;
import query.RangeQuery;
import query.LinearScanRangeQuery;

import java.util.*;

/**
 * Assignment 3: 树索引性能对比分析程序
 *
 * 本程序进行：
 * 1. GH树与VP树的构建性能对比
 * 2. 范围查询性能对比
 * 3. kNN查询性能对比
 * 4. 不同数据集上的性能分析
 * 5. 不同参数对性能的影响分析
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class TreePerformanceAnalysis {

    private static final String SEPARATOR = "=".repeat(80);
    private static final String SUB_SEPARATOR = "-".repeat(60);

    public static void main(String[] args) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("    Assignment 3: GH树与VP树性能对比分析");
        System.out.println(SEPARATOR);
        System.out.println();

        try {
            // 1. 低维向量数据集实验
            runLowDimensionExperiment();

            // 2. 高维向量数据集实验
            runHighDimensionExperiment();

            // 3. 蛋白质序列数据集实验
            runProteinExperiment();

            // 4. 参数影响分析
            runParameterAnalysis();

        } catch (Exception e) {
            System.err.println("分析过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("性能分析完成！");
        System.out.println(SEPARATOR);
    }

    /**
     * 低维向量数据集实验（2-4维）
     */
    private static void runLowDimensionExperiment() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("实验1: 低维向量数据集 (2维)");
        System.out.println(SEPARATOR);

        String filePath = "UMAD-Dataset/full/Vector/unzipped/clusteredvector-2d-100k-100c.txt";
        java.io.File file = new java.io.File(filePath);

        if (!file.exists()) {
            System.out.println("⚠ 未找到低维数据文件: " + filePath);
            System.out.println("使用随机生成的2维数据进行实验...");

            // 生成随机2D数据
            List<VectorData> dataset = generateRandomVectors(10000, 2);
            runExperimentOnDataset("随机2D向量", dataset, MinkowskiDistance.L2,
                    new double[] {0.5, 1.0, 2.0}, new int[] {5, 10, 20});
        } else {
            try {
                List<VectorData> dataset = VectorDataReader.readFromFile(filePath, 10000, false);
                runExperimentOnDataset("聚类2D向量", dataset, MinkowskiDistance.L2,
                        new double[] {50.0, 100.0, 200.0}, new int[] {5, 10, 20});
            } catch (Exception e) {
                System.err.println("读取数据失败: " + e.getMessage());
            }
        }
    }

    /**
     * 高维向量数据集实验（10-20维）
     */
    private static void runHighDimensionExperiment() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("实验2: 高维向量数据集 (20维)");
        System.out.println(SEPARATOR);

        String filePath = "UMAD-Dataset/full/Vector/unzipped/uniformvector-20dim-1m.txt";
        java.io.File file = new java.io.File(filePath);

        if (!file.exists()) {
            System.out.println("⚠ 未找到高维数据文件: " + filePath);
            System.out.println("使用随机生成的20维数据进行实验...");

            List<VectorData> dataset = generateRandomVectors(5000, 20);
            runExperimentOnDataset("随机20D向量", dataset, MinkowskiDistance.L2,
                    new double[] {2.0, 3.0, 4.0}, new int[] {5, 10, 20});
        } else {
            try {
                List<VectorData> dataset = VectorDataReader.readFromFile(filePath, 5000, false);
                runExperimentOnDataset("均匀20D向量", dataset, MinkowskiDistance.L2,
                        new double[] {2.0, 3.0, 4.0}, new int[] {5, 10, 20});
            } catch (Exception e) {
                System.err.println("读取数据失败: " + e.getMessage());
            }
        }
    }

    /**
     * 蛋白质序列数据集实验
     */
    private static void runProteinExperiment() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("实验3: 蛋白质序列数据集");
        System.out.println(SEPARATOR);

        String filePath = "UMAD-Dataset/full/Protein/unzipped/yeast.txt";
        java.io.File file = new java.io.File(filePath);

        if (!file.exists()) {
            System.out.println("⚠ 未找到蛋白质数据文件: " + filePath);
            System.out.println("使用随机生成的蛋白质序列进行实验...");

            List<ProteinData> dataset = generateRandomProteins(1000, 6);
            runProteinExperimentOnDataset("随机蛋白质序列", dataset, new double[] {1.0, 2.0, 3.0},
                    new int[] {5, 10, 20});
        } else {
            try {
                List<ProteinData> dataset = ProteinDataReader.readFromFile(filePath, 1000, 6);
                if (dataset.size() > 0) {
                    runProteinExperimentOnDataset("酵母蛋白质序列", dataset, new double[] {1.0, 2.0, 3.0},
                            new int[] {5, 10, 20});
                } else {
                    System.out.println("蛋白质数据集为空，跳过实验");
                }
            } catch (Exception e) {
                System.err.println("读取蛋白质数据失败: " + e.getMessage());
            }
        }
    }

    /**
     * 参数影响分析
     */
    private static void runParameterAnalysis() {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("实验4: 参数影响分析");
        System.out.println(SEPARATOR);

        // 使用较小的数据集进行参数分析
        List<VectorData> dataset = generateRandomVectors(3000, 5);
        MetricFunction metric = MinkowskiDistance.L2;

        // 分析最大叶子节点大小的影响
        System.out.println("\n【4.1 最大叶子节点大小的影响】");
        System.out.println(SUB_SEPARATOR);

        int[] leafSizes = {10, 25, 50, 100, 200};
        VectorData query = dataset.get(0);
        double radius = 1.5;

        System.out.printf("%-12s | %-10s | %-10s | %-12s | %-12s%n", "叶子大小", "GH树高", "VP树高",
                "GH距离计算", "VP距离计算");
        System.out.println("-".repeat(70));

        for (int leafSize : leafSizes) {
            TreeConfig config = new TreeConfig.Builder().maxLeafSize(leafSize).minTreeHeight(2)
                    .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42).build();

            GHTree ghTree = new GHTree(config);
            ghTree.buildIndex(dataset, metric);
            ghTree.resetStatistics();
            ghTree.rangeQuery(query, radius);

            VPTree vpTree = new VPTree(config);
            vpTree.buildIndex(dataset, metric);
            vpTree.resetStatistics();
            vpTree.rangeQuery(query, radius);

            System.out.printf("%-12d | %-10d | %-10d | %-12d | %-12d%n", leafSize,
                    ghTree.getTreeHeight(), vpTree.getTreeHeight(),
                    ghTree.getDistanceComputations(), vpTree.getDistanceComputations());
        }

        // 分析Pivot选择策略的影响
        System.out.println("\n【4.2 Pivot选择策略的影响】");
        System.out.println(SUB_SEPARATOR);

        TreeConfig.PivotSelectionStrategy[] strategies =
                {TreeConfig.PivotSelectionStrategy.RANDOM, TreeConfig.PivotSelectionStrategy.FFT,
                        TreeConfig.PivotSelectionStrategy.MAX_SPREAD};

        System.out.printf("%-15s | %-12s | %-12s | %-12s | %-12s%n", "策略", "GH构建距离", "VP构建距离",
                "GH查询距离", "VP查询距离");
        System.out.println("-".repeat(75));

        for (TreeConfig.PivotSelectionStrategy strategy : strategies) {
            TreeConfig config = new TreeConfig.Builder().maxLeafSize(50).minTreeHeight(3)
                    .pivotStrategy(strategy).randomSeed(42).build();

            GHTree ghTree = new GHTree(config);
            ghTree.buildIndex(dataset, metric);
            int ghBuildDist = ghTree.getBuildDistanceComputations();
            ghTree.resetStatistics();
            ghTree.rangeQuery(query, radius);
            int ghQueryDist = ghTree.getDistanceComputations();

            VPTree vpTree = new VPTree(config);
            vpTree.buildIndex(dataset, metric);
            int vpBuildDist = vpTree.getBuildDistanceComputations();
            vpTree.resetStatistics();
            vpTree.rangeQuery(query, radius);
            int vpQueryDist = vpTree.getDistanceComputations();

            System.out.printf("%-15s | %-12d | %-12d | %-12d | %-12d%n", strategy, ghBuildDist,
                    vpBuildDist, ghQueryDist, vpQueryDist);
        }
    }

    /**
     * 在向量数据集上运行实验
     */
    private static void runExperimentOnDataset(String datasetName, List<VectorData> dataset,
            MetricFunction metric, double[] radii, int[] kValues) {
        System.out.println("\n数据集: " + datasetName);
        System.out.println("数据量: " + dataset.size());
        System.out.println("维度: " + dataset.get(0).getDimension());
        System.out.println();

        // 配置
        TreeConfig config = new TreeConfig.Builder().maxLeafSize(50).minTreeHeight(3)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42).build();

        // 构建索引
        System.out.println("【构建性能】");
        System.out.println(SUB_SEPARATOR);

        GHTree ghTree = new GHTree(config);
        long startTime = System.nanoTime();
        ghTree.buildIndex(dataset, metric);
        long ghBuildTime = (System.nanoTime() - startTime) / 1_000_000;

        VPTree vpTree = new VPTree(config);
        startTime = System.nanoTime();
        vpTree.buildIndex(dataset, metric);
        long vpBuildTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.printf("%-15s | %-12s | %-12s | %-10s | %-10s%n", "索引类型", "构建时间(ms)", "距离计算",
                "树高", "节点数");
        System.out.println("-".repeat(65));
        System.out.printf("%-15s | %-12d | %-12d | %-10d | %-10d%n", "GH-Tree", ghBuildTime,
                ghTree.getBuildDistanceComputations(), ghTree.getTreeHeight(),
                ghTree.getTotalNodes());
        System.out.printf("%-15s | %-12d | %-12d | %-10d | %-10d%n", "VP-Tree", vpBuildTime,
                vpTree.getBuildDistanceComputations(), vpTree.getTreeHeight(),
                vpTree.getTotalNodes());

        // 范围查询性能
        System.out.println("\n【范围查询性能】");
        System.out.println(SUB_SEPARATOR);

        // 随机选择查询点
        Random rand = new Random(123);
        int numQueries = 10;
        List<VectorData> queryPoints = new ArrayList<>();
        for (int i = 0; i < numQueries; i++) {
            queryPoints.add(dataset.get(rand.nextInt(dataset.size())));
        }

        System.out.printf("%-10s | %-12s | %-12s | %-12s | %-10s | %-10s%n", "半径", "线性扫描",
                "GH-Tree", "VP-Tree", "GH剪枝率", "VP剪枝率");
        System.out.println("-".repeat(75));

        for (double radius : radii) {
            long linearTotal = 0, ghTotal = 0, vpTotal = 0;

            for (VectorData query : queryPoints) {
                // 线性扫描
                linearTotal += dataset.size();

                // GH树
                ghTree.resetStatistics();
                ghTree.rangeQuery(query, radius);
                ghTotal += ghTree.getDistanceComputations();

                // VP树
                vpTree.resetStatistics();
                vpTree.rangeQuery(query, radius);
                vpTotal += vpTree.getDistanceComputations();
            }

            double ghPrune = 100.0 * (1.0 - (double) ghTotal / linearTotal);
            double vpPrune = 100.0 * (1.0 - (double) vpTotal / linearTotal);

            System.out.printf("%-10.1f | %-12d | %-12d | %-12d | %-9.1f%% | %-9.1f%%%n", radius,
                    linearTotal, ghTotal, vpTotal, ghPrune, vpPrune);
        }

        // kNN查询性能
        System.out.println("\n【kNN查询性能】");
        System.out.println(SUB_SEPARATOR);

        System.out.printf("%-6s | %-12s | %-12s | %-12s | %-10s | %-10s%n", "k", "线性扫描", "GH-Tree",
                "VP-Tree", "GH剪枝率", "VP剪枝率");
        System.out.println("-".repeat(70));

        for (int k : kValues) {
            long linearTotal = 0, ghTotal = 0, vpTotal = 0;

            for (VectorData query : queryPoints) {
                linearTotal += dataset.size();

                ghTree.resetStatistics();
                ghTree.knnQuery(query, k);
                ghTotal += ghTree.getDistanceComputations();

                vpTree.resetStatistics();
                vpTree.knnQuery(query, k);
                vpTotal += vpTree.getDistanceComputations();
            }

            double ghPrune = 100.0 * (1.0 - (double) ghTotal / linearTotal);
            double vpPrune = 100.0 * (1.0 - (double) vpTotal / linearTotal);

            System.out.printf("%-6d | %-12d | %-12d | %-12d | %-9.1f%% | %-9.1f%%%n", k,
                    linearTotal, ghTotal, vpTotal, ghPrune, vpPrune);
        }
    }

    /**
     * 在蛋白质数据集上运行实验
     */
    private static void runProteinExperimentOnDataset(String datasetName, List<ProteinData> dataset,
            double[] radii, int[] kValues) {
        System.out.println("\n数据集: " + datasetName);
        System.out.println("数据量: " + dataset.size());
        System.out.println("序列长度: " + dataset.get(0).getDimension());
        System.out.println();

        MetricFunction metric = new AlignmentDistance();

        TreeConfig config = new TreeConfig.Builder().maxLeafSize(30).minTreeHeight(3)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42).build();

        // 构建索引
        System.out.println("【构建性能】");
        System.out.println(SUB_SEPARATOR);

        GHTree ghTree = new GHTree(config);
        long startTime = System.nanoTime();
        ghTree.buildIndex(dataset, metric);
        long ghBuildTime = (System.nanoTime() - startTime) / 1_000_000;

        VPTree vpTree = new VPTree(config);
        startTime = System.nanoTime();
        vpTree.buildIndex(dataset, metric);
        long vpBuildTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.printf("%-15s | %-12s | %-12s | %-10s | %-10s%n", "索引类型", "构建时间(ms)", "距离计算",
                "树高", "节点数");
        System.out.println("-".repeat(65));
        System.out.printf("%-15s | %-12d | %-12d | %-10d | %-10d%n", "GH-Tree", ghBuildTime,
                ghTree.getBuildDistanceComputations(), ghTree.getTreeHeight(),
                ghTree.getTotalNodes());
        System.out.printf("%-15s | %-12d | %-12d | %-10d | %-10d%n", "VP-Tree", vpBuildTime,
                vpTree.getBuildDistanceComputations(), vpTree.getTreeHeight(),
                vpTree.getTotalNodes());

        // 范围查询性能
        System.out.println("\n【范围查询性能】");
        System.out.println(SUB_SEPARATOR);

        Random rand = new Random(123);
        int numQueries = 5; // 蛋白质距离计算较慢，减少查询次数
        List<ProteinData> queryPoints = new ArrayList<>();
        for (int i = 0; i < numQueries; i++) {
            queryPoints.add(dataset.get(rand.nextInt(dataset.size())));
        }

        System.out.printf("%-10s | %-12s | %-12s | %-12s | %-10s | %-10s%n", "半径", "线性扫描",
                "GH-Tree", "VP-Tree", "GH剪枝率", "VP剪枝率");
        System.out.println("-".repeat(75));

        for (double radius : radii) {
            long linearTotal = 0, ghTotal = 0, vpTotal = 0;

            for (ProteinData query : queryPoints) {
                linearTotal += dataset.size();

                ghTree.resetStatistics();
                ghTree.rangeQuery(query, radius);
                ghTotal += ghTree.getDistanceComputations();

                vpTree.resetStatistics();
                vpTree.rangeQuery(query, radius);
                vpTotal += vpTree.getDistanceComputations();
            }

            double ghPrune = 100.0 * (1.0 - (double) ghTotal / linearTotal);
            double vpPrune = 100.0 * (1.0 - (double) vpTotal / linearTotal);

            System.out.printf("%-10.1f | %-12d | %-12d | %-12d | %-9.1f%% | %-9.1f%%%n", radius,
                    linearTotal, ghTotal, vpTotal, ghPrune, vpPrune);
        }
    }

    /**
     * 生成随机向量数据
     */
    private static List<VectorData> generateRandomVectors(int count, int dimension) {
        List<VectorData> dataset = new ArrayList<>();
        Random rand = new Random(42);

        for (int i = 0; i < count; i++) {
            double[] coords = new double[dimension];
            for (int j = 0; j < dimension; j++) {
                coords[j] = rand.nextDouble() * 10.0; // [0, 10)范围
            }
            dataset.add(new VectorData(i, coords));
        }

        return dataset;
    }

    /**
     * 生成随机蛋白质序列
     */
    private static List<ProteinData> generateRandomProteins(int count, int length) {
        List<ProteinData> dataset = new ArrayList<>();
        Random rand = new Random(42);
        char[] aminoAcids = "ACDEFGHIKLMNPQRSTVWY".toCharArray();

        for (int i = 0; i < count; i++) {
            StringBuilder seq = new StringBuilder();
            for (int j = 0; j < length; j++) {
                seq.append(aminoAcids[rand.nextInt(aminoAcids.length)]);
            }
            dataset.add(new ProteinData(i, seq.toString()));
        }

        return dataset;
    }

}
