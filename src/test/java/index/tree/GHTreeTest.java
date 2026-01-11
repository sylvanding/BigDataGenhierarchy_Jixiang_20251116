package index.tree;

import core.MetricSpaceData;
import core.MetricFunction;
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import index.tree.ghtree.GHTree;
import index.tree.common.TreeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GH树功能测试
 *
 * 测试GH树的构建、范围查询和kNN查询功能
 *
 * @author Jixiang Ding
 */
@DisplayName("GH树功能测试")
public class GHTreeTest {

    private List<VectorData> dataset;
    private MetricFunction metric;
    private TreeConfig config;

    @BeforeEach
    void setUp() {
        // 创建测试数据集
        dataset = createTestDataset();
        metric = MinkowskiDistance.L2;
        config = new TreeConfig.Builder().maxLeafSize(3).minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT).randomSeed(42).verbose(false)
                .build();
    }

    /**
     * 创建测试数据集（15个2D点）
     */
    private List<VectorData> createTestDataset() {
        List<VectorData> data = new ArrayList<>();
        double[][] points = {{1.0, 1.0}, {2.0, 2.0}, {3.0, 1.0}, {4.0, 4.0}, {5.0, 2.0}, {6.0, 5.0},
                {7.0, 3.0}, {8.0, 6.0}, {9.0, 4.0}, {10.0, 7.0}, {3.0, 5.0}, {6.0, 1.0}, {2.0, 7.0},
                {8.0, 2.0}, {4.0, 8.0}};
        for (int i = 0; i < points.length; i++) {
            data.add(new VectorData(i, points[i]));
        }
        return data;
    }

    @Test
    @DisplayName("测试1: GH树构建")
    void testBuildTree() {
        System.out.println("\n=== 测试1: GH树构建 ===");

        GHTree tree = new GHTree(config);
        tree.buildIndex(dataset, metric);

        // 验证树已构建
        assertNotNull(tree.getRoot(), "树根节点不应为空");

        // 验证树高度
        int height = tree.getTreeHeight();
        assertTrue(height >= config.getMinTreeHeight(),
                "树高度应至少为" + config.getMinTreeHeight() + "，实际为" + height);

        // 验证数据总量
        assertEquals(dataset.size(), tree.getRoot().size(), "树中数据总量应等于数据集大小");

        System.out.println("构建成功！");
        System.out.println("  树高度: " + height);
        System.out.println("  总节点数: " + tree.getTotalNodes());
        System.out.println("  叶子节点数: " + tree.getLeafNodes());
        System.out.println("  构建距离计算: " + tree.getBuildDistanceComputations());
        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试2: GH树范围查询")
    void testRangeQuery() {
        System.out.println("\n=== 测试2: GH树范围查询 ===");

        GHTree tree = new GHTree(config);
        tree.buildIndex(dataset, metric);

        // 查询点和半径
        VectorData queryPoint = new VectorData(100, new double[] {5.0, 5.0});
        double radius = 3.0;

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("查询半径: " + radius);

        // 执行范围查询
        List<MetricSpaceData> results = tree.rangeQuery(queryPoint, radius);

        // 手工计算预期结果
        Set<Integer> expectedIds = new HashSet<>();
        for (VectorData v : dataset) {
            double dist = metric.getDistance(queryPoint, v);
            if (dist <= radius) {
                expectedIds.add(v.getDataId());
            }
        }

        // 验证结果
        assertEquals(expectedIds.size(), results.size(), "结果数量应与预期一致");

        Set<Integer> actualIds = new HashSet<>();
        for (MetricSpaceData d : results) {
            actualIds.add(d.getDataId());
        }
        assertEquals(expectedIds, actualIds, "结果集应与预期一致");

        System.out.println("预期结果数量: " + expectedIds.size());
        System.out.println("实际结果数量: " + results.size());
        System.out.println("距离计算次数: " + tree.getDistanceComputations());
        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试3: GH树kNN查询")
    void testKNNQuery() {
        System.out.println("\n=== 测试3: GH树kNN查询 ===");

        GHTree tree = new GHTree(config);
        tree.buildIndex(dataset, metric);

        VectorData queryPoint = new VectorData(100, new double[] {5.0, 5.0});
        int k = 3;

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("k = " + k);

        // 执行kNN查询
        List<MetricSpaceData> results = tree.knnQuery(queryPoint, k);

        // 手工计算预期结果
        List<Map.Entry<Integer, Double>> distances = new ArrayList<>();
        for (VectorData v : dataset) {
            double dist = metric.getDistance(queryPoint, v);
            distances.add(new AbstractMap.SimpleEntry<>(v.getDataId(), dist));
        }
        distances.sort(Comparator.comparingDouble(Map.Entry::getValue));

        // 验证结果数量
        assertEquals(k, results.size(), "结果数量应等于k");

        // 验证结果正确性（前k个最近邻）
        Set<Integer> expectedIds = new HashSet<>();
        for (int i = 0; i < k; i++) {
            expectedIds.add(distances.get(i).getKey());
        }

        Set<Integer> actualIds = new HashSet<>();
        for (MetricSpaceData d : results) {
            actualIds.add(d.getDataId());
        }

        assertEquals(expectedIds, actualIds, "kNN结果应正确");

        System.out.println("结果:");
        int rank = 1;
        for (MetricSpaceData d : results) {
            double dist = metric.getDistance(queryPoint, d);
            System.out.printf("  #%d: ID=%d, 距离=%.4f%n", rank++, d.getDataId(), dist);
        }
        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试4: 空数据集处理")
    void testEmptyDataset() {
        System.out.println("\n=== 测试4: 空数据集处理 ===");

        GHTree tree = new GHTree(config);

        // 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            tree.buildIndex(new ArrayList<>(), metric);
        }, "空数据集应抛出异常");

        System.out.println("正确抛出异常！");
        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试5: 树高度控制")
    void testTreeHeightControl() {
        System.out.println("\n=== 测试5: 树高度控制 ===");

        // 创建较大的数据集
        List<VectorData> largeDataset = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 0; i < 500; i++) {
            largeDataset.add(new VectorData(i,
                    new double[] {rand.nextDouble() * 100, rand.nextDouble() * 100}));
        }

        // 测试不同的最小树高
        int[] minHeights = {3, 4, 5};

        for (int minHeight : minHeights) {
            TreeConfig testConfig = new TreeConfig.Builder().maxLeafSize(20)
                    .minTreeHeight(minHeight).pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                    .randomSeed(42).build();

            GHTree tree = new GHTree(testConfig);
            tree.buildIndex(largeDataset, metric);

            assertTrue(tree.getTreeHeight() >= minHeight,
                    "树高应至少为" + minHeight + "，实际为" + tree.getTreeHeight());

            System.out.printf("最小树高设置=%d，实际树高=%d ✓%n", minHeight, tree.getTreeHeight());
        }

        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试6: 统计信息")
    void testStatistics() {
        System.out.println("\n=== 测试6: 统计信息 ===");

        GHTree tree = new GHTree(config);
        tree.buildIndex(dataset, metric);

        // 执行查询
        VectorData queryPoint = new VectorData(100, new double[] {5.0, 5.0});
        tree.rangeQuery(queryPoint, 3.0);

        String stats = tree.getStatistics();
        assertNotNull(stats, "统计信息不应为空");
        assertTrue(stats.contains("GH-Tree"), "统计信息应包含索引名称");

        System.out.println(stats);
        System.out.println("测试通过！\n");
    }
}
