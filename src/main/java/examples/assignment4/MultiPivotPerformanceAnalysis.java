package examples.assignment4;

import core.MetricSpaceData;
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import datatype.protein.ProteinData;
import datatype.protein.AlignmentDistance;
import index.tree.common.TreeConfig;
import index.tree.common.MultiPivotSelector;
import index.tree.mvptree.MVPTree;
import index.tree.cght.CGHTree;
import index.tree.linearpartition.LinearPartitionTree;
import io.ProteinDataReader;

import java.util.*;

/**
 * Assignment 4 多Pivot树索引性能对比分析
 *
 * 对比分析三种3-pivot索引的性能：
 * 1. 构建性能对比
 * 2. 范围查询性能对比（不同查询半径）
 * 3. kNN查询性能对比
 * 4. 剪枝效率对比
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class MultiPivotPerformanceAnalysis {

    public static void main(String[] args) {
        System.out
                .println("======================================================================");
        System.out.println("       Assignment 4: 多Pivot树索引性能对比分析");
        System.out.println(
                "======================================================================\n");

        // 实验1: 低维向量数据集
        experiment1_LowDimensionalVectors();

        // 实验2: 高维向量数据集
        experiment2_HighDimensionalVectors();

        // 实验3: 蛋白质序列数据集
        experiment3_ProteinSequences();

        // 实验4: 参数影响分析
        experiment4_ParameterAnalysis();

        System.out.println(
                "\n======================================================================");
        System.out.println("分析完成！");
        System.out
                .println("======================================================================");
    }

    /**
     * 实验1: 低维向量数据集 (2维)
     */
    private static void experiment1_LowDimensionalVectors() {
        System.out.println(
                "================================================================================");
        System.out.println("实验1: 低维向量数据集 (2维)");
        System.out.println(
                "================================================================================\n");

        // 生成聚类数据
        int dataSize = 1000;
        int dim = 2;
        List<VectorData> data = generateClusteredVectors(dataSize, dim, 5, 42);

        System.out.println("数据集: 聚类2D向量");
        System.out.println("数据量: " + dataSize);
        System.out.println("维度: " + dim + "\n");

        runExperiment(data, new MinkowskiDistance(2), new double[] {1.0, 2.0, 3.0});
    }

    /**
     * 实验2: 高维向量数据集 (10维)
     */
    private static void experiment2_HighDimensionalVectors() {
        System.out.println(
                "\n================================================================================");
        System.out.println("实验2: 高维向量数据集 (10维)");
        System.out.println(
                "================================================================================\n");

        int dataSize = 500;
        int dim = 10;
        List<VectorData> data = generateUniformVectors(dataSize, dim, 42);

        System.out.println("数据集: 均匀10D向量");
        System.out.println("数据量: " + dataSize);
        System.out.println("维度: " + dim + "\n");

        runExperiment(data, new MinkowskiDistance(2), new double[] {2.0, 3.0, 4.0});
    }

    /**
     * 实验3: 蛋白质序列数据集
     */
    private static void experiment3_ProteinSequences() {
        System.out.println(
                "\n================================================================================");
        System.out.println("实验3: 蛋白质序列数据集");
        System.out.println(
                "================================================================================\n");

        String proteinFile = "UMAD-Dataset/full/Protein/unzipped/yeast.txt";
        int fragmentLength = 6;

        try {
            List<ProteinData> data =
                    ProteinDataReader.readFromFile(proteinFile, 500, fragmentLength);

            if (data.size() < 50) {
                System.out.println("蛋白质数据不足，跳过此实验");
                return;
            }

            System.out.println("数据集: 酵母蛋白质序列");
            System.out.println("数据量: " + data.size());
            System.out.println("序列长度: " + fragmentLength + "\n");

            runExperimentProtein(data, new AlignmentDistance(), new double[] {1.0, 2.0, 3.0});

        } catch (Exception e) {
            System.out.println("无法加载蛋白质数据: " + e.getMessage());
            System.out.println("使用模拟蛋白质序列数据进行测试\n");

            List<ProteinData> mockData = generateMockProteinData(200, 6, 42);
            runExperimentProtein(mockData, new AlignmentDistance(), new double[] {1.0, 2.0, 3.0});
        }
    }

    /**
     * 实验4: 参数影响分析
     */
    private static void experiment4_ParameterAnalysis() {
        System.out.println(
                "\n================================================================================");
        System.out.println("实验4: 参数影响分析");
        System.out.println(
                "================================================================================\n");

        int dataSize = 500;
        List<VectorData> data = generateClusteredVectors(dataSize, 2, 5, 42);
        MinkowskiDistance euclidean = new MinkowskiDistance(2);

        // 4.1 最大叶子节点大小的影响
        System.out.println("【4.1 最大叶子节点大小的影响】");
        System.out.println("-".repeat(60));

        int[] leafSizes = {5, 10, 25, 50, 100};

        System.out.printf("%-12s | %-8s | %-8s | %-8s | %-12s | %-12s%n", "叶子大小", "MVP高", "CGH高",
                "LP高", "MVP距离计算", "CGH距离计算");
        System.out.println("-".repeat(80));

        for (int leafSize : leafSizes) {
            TreeConfig config = new TreeConfig.Builder().maxLeafSize(leafSize).minTreeHeight(2)
                    .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42L)
                    .verbose(false).build();

            MVPTree mvpTree = new MVPTree(config);
            CGHTree cghTree = new CGHTree(config);
            LinearPartitionTree lpTree = new LinearPartitionTree(config);

            mvpTree.buildIndex(data, euclidean);
            cghTree.buildIndex(data, euclidean);
            lpTree.buildIndex(data, euclidean);

            // 执行查询
            VectorData query = data.get(0);
            double radius = 2.0;

            mvpTree.resetStatistics();
            cghTree.resetStatistics();
            lpTree.resetStatistics();

            mvpTree.rangeQuery(query, radius);
            cghTree.rangeQuery(query, radius);
            lpTree.rangeQuery(query, radius);

            System.out.printf("%-12d | %-8d | %-8d | %-8d | %-12d | %-12d%n", leafSize,
                    mvpTree.getTreeHeight(), cghTree.getTreeHeight(), lpTree.getTreeHeight(),
                    mvpTree.getDistanceComputations(), cghTree.getDistanceComputations());
        }

        // 4.2 Pivot选择策略的影响
        System.out.println("\n【4.2 Pivot选择策略的影响】");
        System.out.println("-".repeat(60));

        TreeConfig.PivotSelectionStrategy[] strategies =
                {TreeConfig.PivotSelectionStrategy.RANDOM, TreeConfig.PivotSelectionStrategy.FFT,
                        TreeConfig.PivotSelectionStrategy.MAX_SPREAD};

        System.out.printf("%-14s | %-12s | %-12s | %-12s | %-12s%n", "策略", "MVP构建距离", "CGH构建距离",
                "MVP查询距离", "CGH查询距离");
        System.out.println("-".repeat(75));

        for (TreeConfig.PivotSelectionStrategy strategy : strategies) {
            TreeConfig config = new TreeConfig.Builder().maxLeafSize(25).minTreeHeight(2)
                    .pivotStrategy(strategy).randomSeed(42L).verbose(false).build();

            MVPTree mvpTree = new MVPTree(config);
            CGHTree cghTree = new CGHTree(config);

            mvpTree.buildIndex(data, euclidean);
            cghTree.buildIndex(data, euclidean);

            int mvpBuildDist = mvpTree.getBuildDistanceComputations();
            int cghBuildDist = cghTree.getBuildDistanceComputations();

            VectorData query = data.get(0);
            double radius = 2.0;

            mvpTree.resetStatistics();
            cghTree.resetStatistics();

            mvpTree.rangeQuery(query, radius);
            cghTree.rangeQuery(query, radius);

            System.out.printf("%-14s | %-12d | %-12d | %-12d | %-12d%n", strategy, mvpBuildDist,
                    cghBuildDist, mvpTree.getDistanceComputations(),
                    cghTree.getDistanceComputations());
        }
    }

    /**
     * 运行实验（向量数据）
     */
    private static void runExperiment(List<VectorData> data, MinkowskiDistance metric,
            double[] radii) {
        TreeConfig config = new TreeConfig.Builder().maxLeafSize(Math.max(10, data.size() / 50))
                .minTreeHeight(2).pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42L).verbose(false).build();

        // 构建索引
        System.out.println("【构建性能】");
        System.out.println("-".repeat(60));

        MVPTree mvpTree = new MVPTree(config);
        CGHTree cghTree = new CGHTree(config);
        LinearPartitionTree lpTree = new LinearPartitionTree(config);

        long start = System.currentTimeMillis();
        mvpTree.buildIndex(data, metric);
        long mvpBuildTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        cghTree.buildIndex(data, metric);
        long cghBuildTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        lpTree.buildIndex(data, metric);
        long lpBuildTime = System.currentTimeMillis() - start;

        System.out.printf("%-16s | %-12s | %-12s | %-8s | %-8s%n", "索引类型", "构建时间(ms)", "距离计算", "树高",
                "节点数");
        System.out.println("-".repeat(65));
        System.out.printf("%-16s | %-12d | %-12d | %-8d | %-8d%n", "MVP-Tree", mvpBuildTime,
                mvpTree.getBuildDistanceComputations(), mvpTree.getTreeHeight(),
                mvpTree.getTotalNodes());
        System.out.printf("%-16s | %-12d | %-12d | %-8d | %-8d%n", "CGH-Tree", cghBuildTime,
                cghTree.getBuildDistanceComputations(), cghTree.getTreeHeight(),
                cghTree.getTotalNodes());
        System.out.printf("%-16s | %-12d | %-12d | %-8d | %-8d%n", "LP-Tree", lpBuildTime,
                lpTree.getBuildDistanceComputations(), lpTree.getTreeHeight(),
                lpTree.getTotalNodes());

        // 范围查询性能
        System.out.println("\n【范围查询性能】");
        System.out.println("-".repeat(60));

        VectorData query = data.get(0);

        System.out.printf("%-10s | %-12s | %-12s | %-12s | %-12s | %-10s | %-10s%n", "半径", "线性扫描",
                "MVP-Tree", "CGH-Tree", "LP-Tree", "MVP剪枝率", "CGH剪枝率");
        System.out.println("-".repeat(95));

        for (double r : radii) {
            int linearCount = data.size(); // 线性扫描的距离计算次数

            mvpTree.resetStatistics();
            cghTree.resetStatistics();
            lpTree.resetStatistics();

            mvpTree.rangeQuery(query, r);
            cghTree.rangeQuery(query, r);
            lpTree.rangeQuery(query, r);

            double mvpPruneRate =
                    100.0 * (1 - (double) mvpTree.getDistanceComputations() / linearCount);
            double cghPruneRate =
                    100.0 * (1 - (double) cghTree.getDistanceComputations() / linearCount);

            System.out.printf("%-10.1f | %-12d | %-12d | %-12d | %-12d | %-9.1f%% | %-9.1f%%%n", r,
                    linearCount, mvpTree.getDistanceComputations(),
                    cghTree.getDistanceComputations(), lpTree.getDistanceComputations(),
                    mvpPruneRate, cghPruneRate);
        }

        // kNN查询性能
        System.out.println("\n【kNN查询性能】");
        System.out.println("-".repeat(60));

        int[] kValues = {5, 10, 20};

        System.out.printf("%-6s | %-12s | %-12s | %-12s | %-12s | %-10s%n", "k", "线性扫描", "MVP-Tree",
                "CGH-Tree", "LP-Tree", "MVP剪枝率");
        System.out.println("-".repeat(75));

        for (int k : kValues) {
            mvpTree.resetStatistics();
            cghTree.resetStatistics();
            lpTree.resetStatistics();

            mvpTree.knnQuery(query, k);
            cghTree.knnQuery(query, k);
            lpTree.knnQuery(query, k);

            double mvpPruneRate =
                    100.0 * (1 - (double) mvpTree.getDistanceComputations() / data.size());

            System.out.printf("%-6d | %-12d | %-12d | %-12d | %-12d | %-9.1f%%%n", k, data.size(),
                    mvpTree.getDistanceComputations(), cghTree.getDistanceComputations(),
                    lpTree.getDistanceComputations(), mvpPruneRate);
        }
    }

    /**
     * 运行实验（蛋白质数据）
     */
    private static void runExperimentProtein(List<ProteinData> data, AlignmentDistance metric,
            double[] radii) {
        TreeConfig config = new TreeConfig.Builder().maxLeafSize(Math.max(5, data.size() / 20))
                .minTreeHeight(2).pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42L).verbose(false).build();

        System.out.println("【构建性能】");
        System.out.println("-".repeat(60));

        MVPTree mvpTree = new MVPTree(config);
        CGHTree cghTree = new CGHTree(config);
        LinearPartitionTree lpTree = new LinearPartitionTree(config);

        long start = System.currentTimeMillis();
        mvpTree.buildIndex(data, metric);
        long mvpTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        cghTree.buildIndex(data, metric);
        long cghTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        lpTree.buildIndex(data, metric);
        long lpTime = System.currentTimeMillis() - start;

        System.out.printf("%-16s | %-12s | %-12s | %-8s | %-8s%n", "索引类型", "构建时间(ms)", "距离计算", "树高",
                "节点数");
        System.out.println("-".repeat(65));
        System.out.printf("%-16s | %-12d | %-12d | %-8d | %-8d%n", "MVP-Tree", mvpTime,
                mvpTree.getBuildDistanceComputations(), mvpTree.getTreeHeight(),
                mvpTree.getTotalNodes());
        System.out.printf("%-16s | %-12d | %-12d | %-8d | %-8d%n", "CGH-Tree", cghTime,
                cghTree.getBuildDistanceComputations(), cghTree.getTreeHeight(),
                cghTree.getTotalNodes());
        System.out.printf("%-16s | %-12d | %-12d | %-8d | %-8d%n", "LP-Tree", lpTime,
                lpTree.getBuildDistanceComputations(), lpTree.getTreeHeight(),
                lpTree.getTotalNodes());

        // 范围查询
        System.out.println("\n【范围查询性能】");
        System.out.println("-".repeat(60));

        ProteinData query = data.get(0);

        System.out.printf("%-10s | %-12s | %-12s | %-12s | %-12s | %-10s%n", "半径", "线性扫描",
                "MVP-Tree", "CGH-Tree", "LP-Tree", "MVP剪枝率");
        System.out.println("-".repeat(80));

        for (double r : radii) {
            mvpTree.resetStatistics();
            cghTree.resetStatistics();
            lpTree.resetStatistics();

            mvpTree.rangeQuery(query, r);
            cghTree.rangeQuery(query, r);
            lpTree.rangeQuery(query, r);

            double mvpPruneRate =
                    100.0 * (1 - (double) mvpTree.getDistanceComputations() / data.size());

            System.out.printf("%-10.1f | %-12d | %-12d | %-12d | %-12d | %-9.1f%%%n", r,
                    data.size(), mvpTree.getDistanceComputations(),
                    cghTree.getDistanceComputations(), lpTree.getDistanceComputations(),
                    mvpPruneRate);
        }
    }

    /**
     * 生成聚类向量数据
     */
    private static List<VectorData> generateClusteredVectors(int count, int dim, int clusters,
            long seed) {
        Random rand = new Random(seed);
        List<VectorData> data = new ArrayList<>();

        // 生成聚类中心
        double[][] centers = new double[clusters][dim];
        for (int c = 0; c < clusters; c++) {
            for (int d = 0; d < dim; d++) {
                centers[c][d] = rand.nextDouble() * 10;
            }
        }

        // 在各聚类周围生成点
        for (int i = 0; i < count; i++) {
            int cluster = rand.nextInt(clusters);
            double[] coords = new double[dim];
            for (int d = 0; d < dim; d++) {
                coords[d] = centers[cluster][d] + rand.nextGaussian() * 0.5;
            }
            data.add(new VectorData(i, coords));
        }

        return data;
    }

    /**
     * 生成均匀分布向量数据
     */
    private static List<VectorData> generateUniformVectors(int count, int dim, long seed) {
        Random rand = new Random(seed);
        List<VectorData> data = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double[] coords = new double[dim];
            for (int d = 0; d < dim; d++) {
                coords[d] = rand.nextDouble() * 10;
            }
            data.add(new VectorData(i, coords));
        }

        return data;
    }

    /**
     * 生成模拟蛋白质数据
     */
    private static List<ProteinData> generateMockProteinData(int count, int length, long seed) {
        Random rand = new Random(seed);
        char[] aminoAcids = "ACDEFGHIKLMNPQRSTVWY".toCharArray();
        List<ProteinData> data = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            StringBuilder seq = new StringBuilder();
            for (int j = 0; j < length; j++) {
                seq.append(aminoAcids[rand.nextInt(aminoAcids.length)]);
            }
            data.add(new ProteinData(i, seq.toString()));
        }

        return data;
    }
}
