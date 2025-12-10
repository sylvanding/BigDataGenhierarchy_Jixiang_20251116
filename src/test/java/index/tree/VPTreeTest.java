package index.tree;

import core.MetricSpaceData;
import core.MetricFunction;
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import index.tree.vptree.VPTree;
import index.tree.common.TreeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VP树功能测试
 *
 * 测试VP树的构建、范围查询和kNN查询功能
 *
 * @author Jixiang Ding
 */
@DisplayName("VP树功能测试")
public class VPTreeTest {

    private List<VectorData> dataset;
    private MetricFunction metric;
    private TreeConfig config;

    @BeforeEach
    void setUp() {
        dataset = createTestDataset();
        metric = MinkowskiDistance.L2;
        config = new TreeConfig.Builder()
                .maxLeafSize(3)
                .minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .verbose(false)
                .build();
    }

    private List<VectorData> createTestDataset() {
        List<VectorData> data = new ArrayList<>();
        double[][] points = {
                { 1.0, 1.0 }, { 2.0, 2.0 }, { 3.0, 1.0 }, { 4.0, 4.0 }, { 5.0, 2.0 },
                { 6.0, 5.0 }, { 7.0, 3.0 }, { 8.0, 6.0 }, { 9.0, 4.0 }, { 10.0, 7.0 },
                { 3.0, 5.0 }, { 6.0, 1.0 }, { 2.0, 7.0 }, { 8.0, 2.0 }, { 4.0, 8.0 }
        };
        for (int i = 0; i < points.length; i++) {
            data.add(new VectorData(i, points[i]));
        }
        return data;
    }

    @Test
    @DisplayName("测试1: VP树构建")
    void testBuildTree() {
        System.out.println("\n=== 测试1: VP树构建 ===");

        VPTree tree = new VPTree(config);
        tree.buildIndex(dataset, metric);

        assertNotNull(tree.getRoot(), "树根节点不应为空");

        int height = tree.getTreeHeight();
        assertTrue(height >= config.getMinTreeHeight(),
                "树高度应至少为" + config.getMinTreeHeight() + "，实际为" + height);

        assertEquals(dataset.size(), tree.getRoot().size(),
                "树中数据总量应等于数据集大小");

        System.out.println("构建成功！");
        System.out.println("  树高度: " + height);
        System.out.println("  总节点数: " + tree.getTotalNodes());
        System.out.println("  叶子节点数: " + tree.getLeafNodes());
        System.out.println("  构建距离计算: " + tree.getBuildDistanceComputations());
        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试2: VP树范围查询")
    void testRangeQuery() {
        System.out.println("\n=== 测试2: VP树范围查询 ===");

        VPTree tree = new VPTree(config);
        tree.buildIndex(dataset, metric);

        VectorData queryPoint = new VectorData(100, new double[] { 5.0, 5.0 });
        double radius = 3.0;

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("查询半径: " + radius);

        List<MetricSpaceData> results = tree.rangeQuery(queryPoint, radius);

        // 手工计算预期结果
        Set<Integer> expectedIds = new HashSet<>();
        for (VectorData v : dataset) {
            double dist = metric.getDistance(queryPoint, v);
            if (dist <= radius) {
                expectedIds.add(v.getDataId());
            }
        }

        assertEquals(expectedIds.size(), results.size(),
                "结果数量应与预期一致");

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
    @DisplayName("测试3: VP树kNN查询")
    void testKNNQuery() {
        System.out.println("\n=== 测试3: VP树kNN查询 ===");

        VPTree tree = new VPTree(config);
        tree.buildIndex(dataset, metric);

        VectorData queryPoint = new VectorData(100, new double[] { 5.0, 5.0 });
        int k = 3;

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("k = " + k);

        List<MetricSpaceData> results = tree.knnQuery(queryPoint, k);

        // 手工计算预期结果
        List<Map.Entry<Integer, Double>> distances = new ArrayList<>();
        for (VectorData v : dataset) {
            double dist = metric.getDistance(queryPoint, v);
            distances.add(new AbstractMap.SimpleEntry<>(v.getDataId(), dist));
        }
        distances.sort(Comparator.comparingDouble(Map.Entry::getValue));

        assertEquals(k, results.size(), "结果数量应等于k");

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
    @DisplayName("测试4: 球形划分验证")
    void testSphericalPartition() {
        System.out.println("\n=== 测试4: 球形划分验证 ===");

        TreeConfig verboseConfig = new TreeConfig.Builder()
                .maxLeafSize(5)
                .minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .verbose(true)
                .build();

        VPTree tree = new VPTree(verboseConfig);
        tree.buildIndex(dataset, metric);

        assertNotNull(tree.getRoot());

        System.out.println("\nVP树结构:");
        tree.printTree();
        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试5: 不同半径的范围查询")
    void testVariousRadii() {
        System.out.println("\n=== 测试5: 不同半径的范围查询 ===");

        VPTree tree = new VPTree(config);
        tree.buildIndex(dataset, metric);

        VectorData queryPoint = new VectorData(100, new double[] { 5.0, 5.0 });
        double[] radii = { 1.0, 2.0, 3.0, 5.0, 10.0 };

        System.out.println("查询点: (5.0, 5.0)");
        System.out.printf("%-10s | %-10s | %-15s%n", "半径", "结果数", "距离计算");
        System.out.println("-".repeat(40));

        for (double radius : radii) {
            tree.resetStatistics();
            List<MetricSpaceData> results = tree.rangeQuery(queryPoint, radius);

            // 验证正确性
            int expected = 0;
            for (VectorData v : dataset) {
                if (metric.getDistance(queryPoint, v) <= radius) {
                    expected++;
                }
            }
            assertEquals(expected, results.size(), "半径" + radius + "的结果数量应正确");

            System.out.printf("%-10.1f | %-10d | %-15d%n",
                    radius, results.size(), tree.getDistanceComputations());
        }

        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试6: 统计信息")
    void testStatistics() {
        System.out.println("\n=== 测试6: 统计信息 ===");

        VPTree tree = new VPTree(config);
        tree.buildIndex(dataset, metric);

        String stats = tree.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.contains("VP-Tree"), "统计信息应包含索引名称");

        System.out.println(stats);
        System.out.println("测试通过！\n");
    }
}
