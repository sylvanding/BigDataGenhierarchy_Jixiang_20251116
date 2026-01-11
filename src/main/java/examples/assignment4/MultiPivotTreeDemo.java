package examples.assignment4;

import core.MetricSpaceData;
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import index.tree.common.TreeConfig;
import index.tree.common.MultiPivotSelector;
import index.tree.mvptree.MVPTree;
import index.tree.cght.CGHTree;
import index.tree.linearpartition.LinearPartitionTree;
import io.VectorDataReader;

import java.util.*;

/**
 * Assignment 4 多Pivot树索引功能演示
 *
 * 展示3-pivot MVPT、CGHT和完全线性划分树的核心功能：
 * 1. 构建过程
 * 2. 树结构可视化
 * 3. 范围查询结果验证
 * 4. kNN查询结果验证
 * 5. 与线性扫描结果一致性检验
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class MultiPivotTreeDemo {

    public static void main(String[] args) {
        System.out
                .println("======================================================================");
        System.out.println("          Assignment 4: 多Pivot树索引功能演示");
        System.out.println(
                "======================================================================\n");

        // 第一部分：小规模数据演示
        demoWithSmallDataset();

        // 第二部分：使用文件数据演示
        demoWithFileData();

        System.out.println(
                "\n======================================================================");
        System.out.println("演示完成！");
        System.out
                .println("======================================================================");
    }

    /**
     * 使用小规模数据演示（便于手工验证）
     */
    private static void demoWithSmallDataset() {
        System.out
                .println("======================================================================");
        System.out.println("第一部分：小规模数据演示");
        System.out.println(
                "======================================================================\n");

        // 创建测试数据
        List<VectorData> testData = new ArrayList<>();
        double[][] points = {{1.0, 1.0}, {2.0, 2.0}, {3.0, 1.0}, {4.0, 4.0}, {5.0, 2.0}, {6.0, 5.0},
                {7.0, 3.0}, {8.0, 6.0}, {9.0, 4.0}, {10.0, 7.0}, {3.0, 5.0}, {6.0, 1.0}, {2.0, 7.0},
                {8.0, 2.0}, {4.0, 8.0}};

        for (int i = 0; i < points.length; i++) {
            testData.add(new VectorData(i, points[i]));
        }

        MinkowskiDistance euclidean = new MinkowskiDistance(2);

        // 【1. 展示测试数据】
        System.out.println("【1. 测试数据集】");
        System.out.println("-".repeat(50));
        System.out.println("数据类型: 2维向量");
        System.out.println("距离函数: 欧几里得距离 (L2)");
        System.out.println("数据数量: " + testData.size());
        System.out.println("\n数据点列表:");
        for (VectorData v : testData) {
            System.out.printf("  ID=%d: (%.1f, %.1f)%n", v.getDataId(), v.getCoordinates()[0],
                    v.getCoordinates()[1]);
        }

        // 配置树参数
        TreeConfig config = new TreeConfig.Builder().maxLeafSize(3).minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42L).verbose(true)
                .build();

        // 【2. MVP树构建演示】
        System.out.println("\n【2. MVP树构建演示】");
        System.out.println("-".repeat(50));

        MVPTree mvpTree = new MVPTree(config);
        mvpTree.buildIndex(testData, euclidean);

        System.out.println("\nMVP树结构:");
        mvpTree.printTree();

        // 【3. CGH树构建演示】
        System.out.println("\n【3. CGH树构建演示】");
        System.out.println("-".repeat(50));

        CGHTree cghTree = new CGHTree(config);
        cghTree.buildIndex(testData, euclidean);

        System.out.println("\nCGH树结构:");
        cghTree.printTree();

        // 【4. 线性划分树构建演示】
        System.out.println("\n【4. 线性划分树构建演示】");
        System.out.println("-".repeat(50));

        LinearPartitionTree lpTree = new LinearPartitionTree(config);
        lpTree.buildIndex(testData, euclidean);

        System.out.println("\n线性划分树结构:");
        lpTree.printTree();

        // 【5. 范围查询演示】
        System.out.println("\n【5. 范围查询演示】");
        System.out.println("-".repeat(50));

        VectorData query = new VectorData(100, new double[] {5.0, 5.0});
        double radius = 3.0;

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("查询半径: " + radius);

        // 关闭verbose模式进行查询
        TreeConfig queryConfig = new TreeConfig.Builder().maxLeafSize(3).minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42L).verbose(false)
                .build();

        MVPTree mvpTreeQuery = new MVPTree(queryConfig);
        CGHTree cghTreeQuery = new CGHTree(queryConfig);
        LinearPartitionTree lpTreeQuery = new LinearPartitionTree(queryConfig);

        mvpTreeQuery.buildIndex(testData, euclidean);
        cghTreeQuery.buildIndex(testData, euclidean);
        lpTreeQuery.buildIndex(testData, euclidean);

        // 线性扫描结果
        System.out.println("\n--- 线性扫描结果（基准） ---");
        List<VectorData> linearResults = new ArrayList<>();
        for (VectorData v : testData) {
            double dist = euclidean.getDistance(query, v);
            if (dist <= radius) {
                linearResults.add(v);
            }
        }
        linearResults.sort((a, b) -> Double.compare(euclidean.getDistance(query, a),
                euclidean.getDistance(query, b)));

        System.out.println("结果数量: " + linearResults.size());
        System.out.println("距离计算次数: " + testData.size());
        System.out.println("结果详情:");
        for (VectorData v : linearResults) {
            System.out.printf("  ID=%d: (%.1f, %.1f), 距离=%.4f%n", v.getDataId(),
                    v.getCoordinates()[0], v.getCoordinates()[1], euclidean.getDistance(query, v));
        }

        // MVP树范围查询
        System.out.println("\n--- MVP树范围查询 ---");
        mvpTreeQuery.resetStatistics();
        List<MetricSpaceData> mvpResults = mvpTreeQuery.rangeQuery(query, radius);
        System.out.println("结果数量: " + mvpResults.size());
        System.out.println("距离计算次数: " + mvpTreeQuery.getDistanceComputations());
        System.out.println("节点访问次数: " + mvpTreeQuery.getNodeAccesses());

        // CGH树范围查询
        System.out.println("\n--- CGH树范围查询 ---");
        cghTreeQuery.resetStatistics();
        List<MetricSpaceData> cghResults = cghTreeQuery.rangeQuery(query, radius);
        System.out.println("结果数量: " + cghResults.size());
        System.out.println("距离计算次数: " + cghTreeQuery.getDistanceComputations());
        System.out.println("节点访问次数: " + cghTreeQuery.getNodeAccesses());

        // 线性划分树范围查询
        System.out.println("\n--- 线性划分树范围查询 ---");
        lpTreeQuery.resetStatistics();
        List<MetricSpaceData> lpResults = lpTreeQuery.rangeQuery(query, radius);
        System.out.println("结果数量: " + lpResults.size());
        System.out.println("距离计算次数: " + lpTreeQuery.getDistanceComputations());
        System.out.println("节点访问次数: " + lpTreeQuery.getNodeAccesses());

        // 验证一致性
        System.out.println("\n--- 结果一致性验证 ---");
        Set<Integer> linearIds = new HashSet<>();
        Set<Integer> mvpIds = new HashSet<>();
        Set<Integer> cghIds = new HashSet<>();
        Set<Integer> lpIds = new HashSet<>();

        for (VectorData v : linearResults)
            linearIds.add(v.getDataId());
        for (MetricSpaceData d : mvpResults)
            mvpIds.add(d.getDataId());
        for (MetricSpaceData d : cghResults)
            cghIds.add(d.getDataId());
        for (MetricSpaceData d : lpResults)
            lpIds.add(d.getDataId());

        System.out.println("MVP树结果与线性扫描一致: " + (mvpIds.equals(linearIds) ? "✓" : "✗"));
        System.out.println("CGH树结果与线性扫描一致: " + (cghIds.equals(linearIds) ? "✓" : "✗"));
        System.out.println("线性划分树结果与线性扫描一致: " + (lpIds.equals(linearIds) ? "✓" : "✗"));

        // 【6. kNN查询演示】
        System.out.println("\n【6. kNN查询演示】");
        System.out.println("-".repeat(50));

        int k = 3;
        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("k = " + k);

        mvpTreeQuery.resetStatistics();
        cghTreeQuery.resetStatistics();
        lpTreeQuery.resetStatistics();

        List<MetricSpaceData> mvpKNN = mvpTreeQuery.knnQuery(query, k);
        List<MetricSpaceData> cghKNN = cghTreeQuery.knnQuery(query, k);
        List<MetricSpaceData> lpKNN = lpTreeQuery.knnQuery(query, k);

        System.out.println("\n--- MVP树kNN结果 ---");
        System.out.println("距离计算次数: " + mvpTreeQuery.getDistanceComputations());
        for (int i = 0; i < mvpKNN.size(); i++) {
            System.out.printf("  #%d: ID=%d, 距离=%.4f%n", i + 1, mvpKNN.get(i).getDataId(),
                    euclidean.getDistance(query, mvpKNN.get(i)));
        }

        System.out.println("\n--- CGH树kNN结果 ---");
        System.out.println("距离计算次数: " + cghTreeQuery.getDistanceComputations());
        for (int i = 0; i < cghKNN.size(); i++) {
            System.out.printf("  #%d: ID=%d, 距离=%.4f%n", i + 1, cghKNN.get(i).getDataId(),
                    euclidean.getDistance(query, cghKNN.get(i)));
        }

        System.out.println("\n--- 线性划分树kNN结果 ---");
        System.out.println("距离计算次数: " + lpTreeQuery.getDistanceComputations());
        for (int i = 0; i < lpKNN.size(); i++) {
            System.out.printf("  #%d: ID=%d, 距离=%.4f%n", i + 1, lpKNN.get(i).getDataId(),
                    euclidean.getDistance(query, lpKNN.get(i)));
        }
    }

    /**
     * 使用文件数据演示
     */
    private static void demoWithFileData() {
        System.out.println(
                "\n======================================================================");
        System.out.println("第二部分：使用文件数据演示");
        System.out.println(
                "======================================================================\n");

        // 尝试加载向量数据
        String vectorFile = "UMAD-Dataset/examples/Vector/test_vectors_2d.txt";

        System.out.println("【1. 加载向量数据】");
        System.out.println("-".repeat(50));

        try {
            List<VectorData> vectorData = VectorDataReader.readFromFile(vectorFile, 0, false);

            if (vectorData.size() < 5) {
                System.out.println("数据量太少，跳过文件数据演示");
                return;
            }

            System.out.println("数据集信息：");
            System.out.println("  文件路径: " + vectorFile);
            System.out.println("  向量维度: 2");
            System.out.println("  数据总量: " + vectorData.size());
            System.out.println("成功读取 " + vectorData.size() + " 个向量\n");

            MinkowskiDistance euclidean = new MinkowskiDistance(2);

            TreeConfig config =
                    new TreeConfig.Builder().maxLeafSize(Math.max(3, vectorData.size() / 10))
                            .minTreeHeight(2).pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                            .randomSeed(42L).verbose(false).build();

            // 构建三种索引
            System.out.println("【2. 构建三种索引】");
            System.out.println("-".repeat(50));

            MVPTree mvpTree = new MVPTree(config);
            CGHTree cghTree = new CGHTree(config);
            LinearPartitionTree lpTree = new LinearPartitionTree(config);

            long start = System.currentTimeMillis();
            mvpTree.buildIndex(vectorData, euclidean);
            long mvpTime = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            cghTree.buildIndex(vectorData, euclidean);
            long cghTime = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            lpTree.buildIndex(vectorData, euclidean);
            long lpTime = System.currentTimeMillis() - start;

            System.out.println("MVP树: 构建时间=" + mvpTime + "ms, 高度=" + mvpTree.getTreeHeight()
                    + ", 节点数=" + mvpTree.getTotalNodes());
            System.out.println("CGH树: 构建时间=" + cghTime + "ms, 高度=" + cghTree.getTreeHeight()
                    + ", 节点数=" + cghTree.getTotalNodes());
            System.out.println("线性划分树: 构建时间=" + lpTime + "ms, 高度=" + lpTree.getTreeHeight()
                    + ", 节点数=" + lpTree.getTotalNodes());

            // 范围查询性能对比
            System.out.println("\n【3. 范围查询性能对比】");
            System.out.println("-".repeat(50));

            VectorData queryPoint = vectorData.get(0);
            double[] radii = {0.5, 1.0, 2.0};

            System.out.printf("%-10s | %-12s | %-12s | %-12s | %-12s%n", "半径", "线性扫描", "MVP树",
                    "CGH树", "线性划分树");
            System.out.println("-".repeat(70));

            for (double r : radii) {
                // 线性扫描
                int linearCount = 0;
                for (VectorData v : vectorData) {
                    if (euclidean.getDistance(queryPoint, v) <= r)
                        linearCount++;
                }

                mvpTree.resetStatistics();
                cghTree.resetStatistics();
                lpTree.resetStatistics();

                mvpTree.rangeQuery(queryPoint, r);
                cghTree.rangeQuery(queryPoint, r);
                lpTree.rangeQuery(queryPoint, r);

                System.out.printf("%-10.1f | %-12d | %-12d | %-12d | %-12d%n", r, vectorData.size(),
                        mvpTree.getDistanceComputations(), cghTree.getDistanceComputations(),
                        lpTree.getDistanceComputations());
            }

        } catch (Exception e) {
            System.out.println("无法读取文件数据: " + e.getMessage());
            System.out.println("请确保文件存在: " + vectorFile);
        }
    }
}
