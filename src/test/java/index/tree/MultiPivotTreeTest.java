package index.tree;

import core.MetricSpaceData;
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import index.tree.common.TreeConfig;
import index.tree.common.MultiPivotSelector;
import index.tree.mvptree.MVPTree;
import index.tree.cght.CGHTree;
import index.tree.linearpartition.LinearPartitionTree;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多Pivot树索引测试类
 *
 * 测试3-pivot MVPT、CGHT和完全线性划分树的功能
 *
 * @author Jixiang Ding
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiPivotTreeTest {

    private static List<VectorData> testData;
    private static MinkowskiDistance euclidean;
    private static TreeConfig config;

    @BeforeAll
    static void setUp() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("多Pivot树索引测试");
        System.out.println("=".repeat(60));

        // 创建测试数据：15个2D点
        testData = new ArrayList<>();
        double[][] points = {{1.0, 1.0}, {2.0, 2.0}, {3.0, 1.0}, {4.0, 4.0}, {5.0, 2.0}, {6.0, 5.0},
                {7.0, 3.0}, {8.0, 6.0}, {9.0, 4.0}, {10.0, 7.0}, {3.0, 5.0}, {6.0, 1.0}, {2.0, 7.0},
                {8.0, 2.0}, {4.0, 8.0}};

        for (int i = 0; i < points.length; i++) {
            testData.add(new VectorData(i, points[i]));
        }

        euclidean = new MinkowskiDistance(2);
        config = new TreeConfig.Builder().maxLeafSize(3).minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42L).verbose(false)
                .build();
    }

    // ========== MVP树测试 ==========

    @Test
    @Order(1)
    void testMVPTreeBuild() {
        System.out.println("\n=== 测试1: MVP树构建 ===");

        MVPTree mvpTree = new MVPTree(config);
        mvpTree.buildIndex(testData, euclidean);

        System.out.println("构建成功!");
        System.out.println("  树高度: " + mvpTree.getTreeHeight());
        System.out.println("  总节点数: " + mvpTree.getTotalNodes());
        System.out.println("  叶子节点数: " + mvpTree.getLeafNodes());
        System.out.println("  构建距离计算: " + mvpTree.getBuildDistanceComputations());

        assertTrue(mvpTree.getTreeHeight() >= config.getMinTreeHeight(),
                "树高应至少为" + config.getMinTreeHeight());
        System.out.println("测试通过！\n");
    }

    @Test
    @Order(2)
    void testMVPTreeRangeQuery() {
        System.out.println("\n=== 测试2: MVP树范围查询 ===");

        MVPTree mvpTree = new MVPTree(config);
        mvpTree.buildIndex(testData, euclidean);

        VectorData query = new VectorData(100, new double[] {5.0, 5.0});
        double radius = 3.0;

        mvpTree.resetStatistics();
        List<MetricSpaceData> results = mvpTree.rangeQuery(query, radius);

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("查询半径: " + radius);
        System.out.println("结果数量: " + results.size());
        System.out.println("距离计算次数: " + mvpTree.getDistanceComputations());

        // 线性扫描验证
        int expectedCount = 0;
        for (VectorData v : testData) {
            if (euclidean.getDistance(query, v) <= radius) {
                expectedCount++;
            }
        }

        assertEquals(expectedCount, results.size(), "结果数量应与线性扫描一致");
        System.out.println("测试通过！\n");
    }

    @Test
    @Order(3)
    void testMVPTreeKNNQuery() {
        System.out.println("\n=== 测试3: MVP树kNN查询 ===");

        MVPTree mvpTree = new MVPTree(config);
        mvpTree.buildIndex(testData, euclidean);

        VectorData query = new VectorData(100, new double[] {5.0, 5.0});
        int k = 3;

        mvpTree.resetStatistics();
        List<MetricSpaceData> results = mvpTree.knnQuery(query, k);

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("k = " + k);
        System.out.println("结果:");
        for (int i = 0; i < results.size(); i++) {
            double dist = euclidean.getDistance(query, results.get(i));
            System.out.printf("  #%d: ID=%d, 距离=%.4f%n", i + 1, results.get(i).getDataId(), dist);
        }

        assertEquals(k, results.size(), "应返回" + k + "个结果");
        System.out.println("测试通过！\n");
    }

    // ========== CGH树测试 ==========

    @Test
    @Order(4)
    void testCGHTreeBuild() {
        System.out.println("\n=== 测试4: CGH树构建 ===");

        CGHTree cghTree = new CGHTree(config);
        cghTree.buildIndex(testData, euclidean);

        System.out.println("构建成功!");
        System.out.println("  树高度: " + cghTree.getTreeHeight());
        System.out.println("  总节点数: " + cghTree.getTotalNodes());
        System.out.println("  叶子节点数: " + cghTree.getLeafNodes());
        System.out.println("  构建距离计算: " + cghTree.getBuildDistanceComputations());

        assertTrue(cghTree.getTreeHeight() >= config.getMinTreeHeight());
        System.out.println("测试通过！\n");
    }

    @Test
    @Order(5)
    void testCGHTreeRangeQuery() {
        System.out.println("\n=== 测试5: CGH树范围查询 ===");

        CGHTree cghTree = new CGHTree(config);
        cghTree.buildIndex(testData, euclidean);

        VectorData query = new VectorData(100, new double[] {5.0, 5.0});
        double radius = 3.0;

        cghTree.resetStatistics();
        List<MetricSpaceData> results = cghTree.rangeQuery(query, radius);

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("查询半径: " + radius);
        System.out.println("结果数量: " + results.size());
        System.out.println("距离计算次数: " + cghTree.getDistanceComputations());

        // 线性扫描验证
        int expectedCount = 0;
        for (VectorData v : testData) {
            if (euclidean.getDistance(query, v) <= radius) {
                expectedCount++;
            }
        }

        assertEquals(expectedCount, results.size());
        System.out.println("测试通过！\n");
    }

    @Test
    @Order(6)
    void testCGHTreeKNNQuery() {
        System.out.println("\n=== 测试6: CGH树kNN查询 ===");

        CGHTree cghTree = new CGHTree(config);
        cghTree.buildIndex(testData, euclidean);

        VectorData query = new VectorData(100, new double[] {5.0, 5.0});
        int k = 3;

        cghTree.resetStatistics();
        List<MetricSpaceData> results = cghTree.knnQuery(query, k);

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("k = " + k);
        System.out.println("结果:");
        for (int i = 0; i < results.size(); i++) {
            double dist = euclidean.getDistance(query, results.get(i));
            System.out.printf("  #%d: ID=%d, 距离=%.4f%n", i + 1, results.get(i).getDataId(), dist);
        }

        assertEquals(k, results.size());
        System.out.println("测试通过！\n");
    }

    // ========== 线性划分树测试 ==========

    @Test
    @Order(7)
    void testLinearPartitionTreeBuild() {
        System.out.println("\n=== 测试7: 线性划分树构建 ===");

        LinearPartitionTree lpTree = new LinearPartitionTree(config);
        lpTree.buildIndex(testData, euclidean);

        System.out.println("构建成功!");
        System.out.println("  树高度: " + lpTree.getTreeHeight());
        System.out.println("  总节点数: " + lpTree.getTotalNodes());
        System.out.println("  叶子节点数: " + lpTree.getLeafNodes());
        System.out.println("  构建距离计算: " + lpTree.getBuildDistanceComputations());

        assertTrue(lpTree.getTreeHeight() >= config.getMinTreeHeight());
        System.out.println("测试通过！\n");
    }

    @Test
    @Order(8)
    void testLinearPartitionTreeRangeQuery() {
        System.out.println("\n=== 测试8: 线性划分树范围查询 ===");

        LinearPartitionTree lpTree = new LinearPartitionTree(config);
        lpTree.buildIndex(testData, euclidean);

        VectorData query = new VectorData(100, new double[] {5.0, 5.0});
        double radius = 3.0;

        lpTree.resetStatistics();
        List<MetricSpaceData> results = lpTree.rangeQuery(query, radius);

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("查询半径: " + radius);
        System.out.println("结果数量: " + results.size());
        System.out.println("距离计算次数: " + lpTree.getDistanceComputations());

        // 线性扫描验证
        int expectedCount = 0;
        for (VectorData v : testData) {
            if (euclidean.getDistance(query, v) <= radius) {
                expectedCount++;
            }
        }

        assertEquals(expectedCount, results.size());
        System.out.println("测试通过！\n");
    }

    @Test
    @Order(9)
    void testLinearPartitionTreeKNNQuery() {
        System.out.println("\n=== 测试9: 线性划分树kNN查询 ===");

        LinearPartitionTree lpTree = new LinearPartitionTree(config);
        lpTree.buildIndex(testData, euclidean);

        VectorData query = new VectorData(100, new double[] {5.0, 5.0});
        int k = 3;

        lpTree.resetStatistics();
        List<MetricSpaceData> results = lpTree.knnQuery(query, k);

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("k = " + k);
        System.out.println("结果:");
        for (int i = 0; i < results.size(); i++) {
            double dist = euclidean.getDistance(query, results.get(i));
            System.out.printf("  #%d: ID=%d, 距离=%.4f%n", i + 1, results.get(i).getDataId(), dist);
        }

        assertEquals(k, results.size());
        System.out.println("测试通过！\n");
    }

    // ========== 一致性测试 ==========

    @Test
    @Order(10)
    void testConsistency() {
        System.out.println("\n=== 测试10: 三种索引结果一致性 ===");

        MVPTree mvpTree = new MVPTree(config);
        CGHTree cghTree = new CGHTree(config);
        LinearPartitionTree lpTree = new LinearPartitionTree(config);

        mvpTree.buildIndex(testData, euclidean);
        cghTree.buildIndex(testData, euclidean);
        lpTree.buildIndex(testData, euclidean);

        VectorData query = new VectorData(100, new double[] {5.0, 5.0});
        double radius = 3.0;

        List<MetricSpaceData> mvpResults = mvpTree.rangeQuery(query, radius);
        List<MetricSpaceData> cghResults = cghTree.rangeQuery(query, radius);
        List<MetricSpaceData> lpResults = lpTree.rangeQuery(query, radius);

        // 提取ID并排序
        Set<Integer> mvpIds = new HashSet<>();
        Set<Integer> cghIds = new HashSet<>();
        Set<Integer> lpIds = new HashSet<>();

        for (MetricSpaceData d : mvpResults)
            mvpIds.add(d.getDataId());
        for (MetricSpaceData d : cghResults)
            cghIds.add(d.getDataId());
        for (MetricSpaceData d : lpResults)
            lpIds.add(d.getDataId());

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("查询半径: " + radius);
        System.out.println("MVP树结果数: " + mvpResults.size());
        System.out.println("CGH树结果数: " + cghResults.size());
        System.out.println("线性划分树结果数: " + lpResults.size());

        assertEquals(mvpIds, cghIds, "MVP树与CGH树结果应一致");
        assertEquals(mvpIds, lpIds, "MVP树与线性划分树结果应一致");

        System.out.println("结果一致性: ✓");
        System.out.println("测试通过！\n");
    }

    @Test
    @Order(11)
    void testLargerDataset() {
        System.out.println("\n=== 测试11: 较大数据集测试 ===");

        // 生成200个随机点
        Random rand = new Random(42);
        List<VectorData> largeData = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            double[] coords = new double[] {rand.nextDouble() * 10, rand.nextDouble() * 10};
            largeData.add(new VectorData(i, coords));
        }

        TreeConfig largeConfig = new TreeConfig.Builder().maxLeafSize(10).minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42L).verbose(false)
                .build();

        MVPTree mvpTree = new MVPTree(largeConfig);
        CGHTree cghTree = new CGHTree(largeConfig);
        LinearPartitionTree lpTree = new LinearPartitionTree(largeConfig);

        mvpTree.buildIndex(largeData, euclidean);
        cghTree.buildIndex(largeData, euclidean);
        lpTree.buildIndex(largeData, euclidean);

        System.out.println("数据集大小: " + largeData.size());
        System.out.println(
                "MVP树: 高度=" + mvpTree.getTreeHeight() + ", 节点数=" + mvpTree.getTotalNodes());
        System.out.println(
                "CGH树: 高度=" + cghTree.getTreeHeight() + ", 节点数=" + cghTree.getTotalNodes());
        System.out
                .println("线性划分树: 高度=" + lpTree.getTreeHeight() + ", 节点数=" + lpTree.getTotalNodes());

        // 测试多个查询
        int correctCount = 0;
        int totalQueries = 10;

        for (int i = 0; i < totalQueries; i++) {
            VectorData query = largeData.get(rand.nextInt(largeData.size()));
            double radius = rand.nextDouble() * 2 + 0.5;

            // 线性扫描结果
            Set<Integer> linearIds = new HashSet<>();
            for (VectorData v : largeData) {
                if (euclidean.getDistance(query, v) <= radius) {
                    linearIds.add(v.getDataId());
                }
            }

            // 三种索引结果
            mvpTree.resetStatistics();
            cghTree.resetStatistics();
            lpTree.resetStatistics();

            Set<Integer> mvpIds = new HashSet<>();
            Set<Integer> cghIds = new HashSet<>();
            Set<Integer> lpIds = new HashSet<>();

            for (MetricSpaceData d : mvpTree.rangeQuery(query, radius))
                mvpIds.add(d.getDataId());
            for (MetricSpaceData d : cghTree.rangeQuery(query, radius))
                cghIds.add(d.getDataId());
            for (MetricSpaceData d : lpTree.rangeQuery(query, radius))
                lpIds.add(d.getDataId());

            if (mvpIds.equals(linearIds) && cghIds.equals(linearIds) && lpIds.equals(linearIds)) {
                correctCount++;
            }
        }

        System.out.println("正确率: " + correctCount + "/" + totalQueries + " ("
                + (100.0 * correctCount / totalQueries) + "%)");

        assertEquals(totalQueries, correctCount, "所有查询结果应与线性扫描一致");
        System.out.println("测试通过！\n");
    }

    @Test
    @Order(12)
    void testEmptyDataset() {
        System.out.println("\n=== 测试12: 空数据集处理 ===");

        List<VectorData> emptyData = new ArrayList<>();

        MVPTree mvpTree = new MVPTree(config);

        assertThrows(IllegalArgumentException.class, () -> {
            mvpTree.buildIndex(emptyData, euclidean);
        }, "空数据集应抛出异常");

        System.out.println("正确抛出异常！");
        System.out.println("测试通过！\n");
    }
}
