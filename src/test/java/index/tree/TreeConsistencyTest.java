package index.tree;

import core.MetricSpaceData;
import core.MetricFunction;
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import index.tree.ghtree.GHTree;
import index.tree.vptree.VPTree;
import index.tree.common.TreeConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GH树与VP树一致性测试
 *
 * 验证GH树和VP树在相同配置下查询结果的一致性
 * 这是Assignment 3的重要要求之一
 *
 * @author Jixiang Ding
 */
@DisplayName("GH树与VP树一致性测试")
public class TreeConsistencyTest {

    @Test
    @DisplayName("测试1: 范围查询结果一致性")
    void testRangeQueryConsistency() {
        System.out.println("\n=== 测试1: GH树与VP树范围查询结果一致性 ===");

        List<VectorData> dataset = createDataset(200);
        MetricFunction metric = MinkowskiDistance.L2;

        TreeConfig config = new TreeConfig.Builder()
                .maxLeafSize(20)
                .minTreeHeight(3)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .build();

        GHTree ghTree = new GHTree(config);
        ghTree.buildIndex(dataset, metric);

        VPTree vpTree = new VPTree(config);
        vpTree.buildIndex(dataset, metric);

        System.out.println("数据集大小: " + dataset.size());
        System.out.println("GH树高度: " + ghTree.getTreeHeight());
        System.out.println("VP树高度: " + vpTree.getTreeHeight());
        System.out.println();

        Random rand = new Random(123);
        double[] radii = { 0.5, 1.0, 2.0, 3.0, 5.0 };
        int numQueries = 20;
        int consistentCount = 0;

        System.out.printf("%-8s | %-8s | %-8s | %-8s | %-10s%n",
                "查询#", "半径", "GH结果", "VP结果", "一致性");
        System.out.println("-".repeat(50));

        for (int q = 0; q < numQueries; q++) {
            VectorData queryPoint = new VectorData(1000 + q, new double[] {
                    rand.nextDouble() * 10, rand.nextDouble() * 10
            });

            double radius = radii[q % radii.length];

            ghTree.resetStatistics();
            List<MetricSpaceData> ghResults = ghTree.rangeQuery(queryPoint, radius);

            vpTree.resetStatistics();
            List<MetricSpaceData> vpResults = vpTree.rangeQuery(queryPoint, radius);

            boolean consistent = setEquals(ghResults, vpResults);
            if (consistent)
                consistentCount++;

            System.out.printf("%-8d | %-8.1f | %-8d | %-8d | %-10s%n",
                    q + 1, radius, ghResults.size(), vpResults.size(),
                    consistent ? "✓" : "✗");

            assertTrue(consistent,
                    String.format("查询%d: GH树(%d)和VP树(%d)结果应一致",
                            q + 1, ghResults.size(), vpResults.size()));
        }

        System.out.println("-".repeat(50));
        System.out.printf("一致性: %d/%d (%.1f%%)%n",
                consistentCount, numQueries, 100.0 * consistentCount / numQueries);
        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试2: kNN查询结果一致性")
    void testKNNQueryConsistency() {
        System.out.println("\n=== 测试2: GH树与VP树kNN查询结果一致性 ===");

        List<VectorData> dataset = createDataset(200);
        MetricFunction metric = MinkowskiDistance.L2;

        TreeConfig config = new TreeConfig.Builder()
                .maxLeafSize(20)
                .minTreeHeight(3)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .build();

        GHTree ghTree = new GHTree(config);
        ghTree.buildIndex(dataset, metric);

        VPTree vpTree = new VPTree(config);
        vpTree.buildIndex(dataset, metric);

        Random rand = new Random(123);
        int[] kValues = { 1, 3, 5, 10, 20 };
        int numQueries = 15;
        int consistentCount = 0;
        int totalTests = numQueries * kValues.length;

        System.out.printf("%-8s | %-6s | %-8s | %-8s | %-10s%n",
                "查询#", "k", "GH结果", "VP结果", "一致性");
        System.out.println("-".repeat(50));

        for (int q = 0; q < numQueries; q++) {
            VectorData queryPoint = new VectorData(1000 + q, new double[] {
                    rand.nextDouble() * 10, rand.nextDouble() * 10
            });

            for (int k : kValues) {
                ghTree.resetStatistics();
                List<MetricSpaceData> ghResults = ghTree.knnQuery(queryPoint, k);

                vpTree.resetStatistics();
                List<MetricSpaceData> vpResults = vpTree.knnQuery(queryPoint, k);

                assertEquals(k, ghResults.size(), "GH树kNN结果数量应等于k");
                assertEquals(k, vpResults.size(), "VP树kNN结果数量应等于k");

                boolean consistent = setEquals(ghResults, vpResults);
                if (consistent)
                    consistentCount++;

                if (q < 3) {
                    System.out.printf("%-8d | %-6d | %-8d | %-8d | %-10s%n",
                            q + 1, k, ghResults.size(), vpResults.size(),
                            consistent ? "✓" : "✗");
                }

                assertTrue(consistent,
                        String.format("查询%d, k=%d: GH树和VP树kNN结果应一致", q + 1, k));
            }
        }

        System.out.println("...");
        System.out.println("-".repeat(50));
        System.out.printf("一致性: %d/%d (%.1f%%)%n",
                consistentCount, totalTests, 100.0 * consistentCount / totalTests);
        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试3: 不同Pivot策略的一致性")
    void testDifferentPivotStrategies() {
        System.out.println("\n=== 测试3: 不同Pivot策略下的结果一致性 ===");

        List<VectorData> dataset = createDataset(100);
        MetricFunction metric = MinkowskiDistance.L2;

        TreeConfig.PivotSelectionStrategy[] strategies = {
                TreeConfig.PivotSelectionStrategy.RANDOM,
                TreeConfig.PivotSelectionStrategy.FFT,
                TreeConfig.PivotSelectionStrategy.MAX_SPREAD
        };

        VectorData queryPoint = new VectorData(999, new double[] { 5.0, 5.0 });
        double radius = 2.0;
        int k = 5;

        // 先用线性扫描获取正确答案
        Set<Integer> expectedRangeIds = new HashSet<>();
        List<Map.Entry<Integer, Double>> allDistances = new ArrayList<>();

        for (VectorData v : dataset) {
            double dist = metric.getDistance(queryPoint, v);
            allDistances.add(new AbstractMap.SimpleEntry<>(v.getDataId(), dist));
            if (dist <= radius) {
                expectedRangeIds.add(v.getDataId());
            }
        }
        allDistances.sort(Comparator.comparingDouble(Map.Entry::getValue));

        Set<Integer> expectedKnnIds = new HashSet<>();
        for (int i = 0; i < k; i++) {
            expectedKnnIds.add(allDistances.get(i).getKey());
        }

        System.out.println("查询点: (5.0, 5.0)");
        System.out.println("范围查询半径: " + radius);
        System.out.println("kNN k值: " + k);
        System.out.println("预期范围查询结果数: " + expectedRangeIds.size());
        System.out.println();

        System.out.printf("%-15s | %-12s | %-12s | %-12s | %-12s%n",
                "策略", "GH范围", "VP范围", "GH-kNN", "VP-kNN");
        System.out.println("-".repeat(70));

        for (TreeConfig.PivotSelectionStrategy strategy : strategies) {
            TreeConfig config = new TreeConfig.Builder()
                    .maxLeafSize(10)
                    .minTreeHeight(3)
                    .pivotStrategy(strategy)
                    .randomSeed(42)
                    .build();

            GHTree ghTree = new GHTree(config);
            ghTree.buildIndex(dataset, metric);

            VPTree vpTree = new VPTree(config);
            vpTree.buildIndex(dataset, metric);

            List<MetricSpaceData> ghRange = ghTree.rangeQuery(queryPoint, radius);
            List<MetricSpaceData> vpRange = vpTree.rangeQuery(queryPoint, radius);

            Set<Integer> ghRangeIds = extractIds(ghRange);
            Set<Integer> vpRangeIds = extractIds(vpRange);

            List<MetricSpaceData> ghKnn = ghTree.knnQuery(queryPoint, k);
            List<MetricSpaceData> vpKnn = vpTree.knnQuery(queryPoint, k);

            Set<Integer> ghKnnIds = extractIds(ghKnn);
            Set<Integer> vpKnnIds = extractIds(vpKnn);

            String ghRangeStatus = ghRangeIds.equals(expectedRangeIds) ? "✓" : "✗";
            String vpRangeStatus = vpRangeIds.equals(expectedRangeIds) ? "✓" : "✗";
            String ghKnnStatus = ghKnnIds.equals(expectedKnnIds) ? "✓" : "✗";
            String vpKnnStatus = vpKnnIds.equals(expectedKnnIds) ? "✓" : "✗";

            System.out.printf("%-15s | %-4d %-7s | %-4d %-7s | %-4d %-7s | %-4d %-7s%n",
                    strategy, ghRange.size(), ghRangeStatus,
                    vpRange.size(), vpRangeStatus,
                    ghKnn.size(), ghKnnStatus,
                    vpKnn.size(), vpKnnStatus);

            assertEquals(expectedRangeIds, ghRangeIds, strategy + ": GH树范围查询结果应正确");
            assertEquals(expectedRangeIds, vpRangeIds, strategy + ": VP树范围查询结果应正确");
            assertEquals(expectedKnnIds, ghKnnIds, strategy + ": GH树kNN结果应正确");
            assertEquals(expectedKnnIds, vpKnnIds, strategy + ": VP树kNN结果应正确");
        }

        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试4: 大规模数据一致性")
    void testLargeScaleConsistency() {
        System.out.println("\n=== 测试4: 大规模数据一致性测试 ===");

        List<VectorData> dataset = createDataset(1000);
        MetricFunction metric = MinkowskiDistance.L2;

        TreeConfig config = new TreeConfig.Builder()
                .maxLeafSize(50)
                .minTreeHeight(3)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .build();

        GHTree ghTree = new GHTree(config);
        long ghStart = System.currentTimeMillis();
        ghTree.buildIndex(dataset, metric);
        long ghBuildTime = System.currentTimeMillis() - ghStart;

        VPTree vpTree = new VPTree(config);
        long vpStart = System.currentTimeMillis();
        vpTree.buildIndex(dataset, metric);
        long vpBuildTime = System.currentTimeMillis() - vpStart;

        System.out.println("数据集大小: " + dataset.size());
        System.out.printf("GH树: 高度=%d, 节点数=%d, 构建时间=%dms%n",
                ghTree.getTreeHeight(), ghTree.getTotalNodes(), ghBuildTime);
        System.out.printf("VP树: 高度=%d, 节点数=%d, 构建时间=%dms%n",
                vpTree.getTreeHeight(), vpTree.getTotalNodes(), vpBuildTime);
        System.out.println();

        Random rand = new Random(456);
        int numQueries = 50;
        int rangeConsistent = 0;
        int knnConsistent = 0;

        for (int q = 0; q < numQueries; q++) {
            VectorData queryPoint = new VectorData(10000 + q, new double[] {
                    rand.nextDouble() * 10, rand.nextDouble() * 10
            });
            double radius = 1.0 + rand.nextDouble() * 2;
            int k = 5 + rand.nextInt(10);

            ghTree.resetStatistics();
            List<MetricSpaceData> ghRange = ghTree.rangeQuery(queryPoint, radius);
            vpTree.resetStatistics();
            List<MetricSpaceData> vpRange = vpTree.rangeQuery(queryPoint, radius);

            if (setEquals(ghRange, vpRange))
                rangeConsistent++;

            ghTree.resetStatistics();
            List<MetricSpaceData> ghKnn = ghTree.knnQuery(queryPoint, k);
            vpTree.resetStatistics();
            List<MetricSpaceData> vpKnn = vpTree.knnQuery(queryPoint, k);

            if (setEquals(ghKnn, vpKnn))
                knnConsistent++;
        }

        System.out.printf("范围查询一致性: %d/%d (%.1f%%)%n",
                rangeConsistent, numQueries, 100.0 * rangeConsistent / numQueries);
        System.out.printf("kNN查询一致性: %d/%d (%.1f%%)%n",
                knnConsistent, numQueries, 100.0 * knnConsistent / numQueries);

        assertEquals(numQueries, rangeConsistent, "所有范围查询结果应一致");
        assertEquals(numQueries, knnConsistent, "所有kNN查询结果应一致");

        System.out.println("测试通过！\n");
    }

    // ========== 辅助方法 ==========

    private List<VectorData> createDataset(int size) {
        List<VectorData> data = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 0; i < size; i++) {
            data.add(new VectorData(i, new double[] {
                    rand.nextDouble() * 10, rand.nextDouble() * 10
            }));
        }
        return data;
    }

    private boolean setEquals(List<MetricSpaceData> list1, List<MetricSpaceData> list2) {
        if (list1.size() != list2.size())
            return false;
        return extractIds(list1).equals(extractIds(list2));
    }

    private Set<Integer> extractIds(List<MetricSpaceData> list) {
        Set<Integer> ids = new HashSet<>();
        for (MetricSpaceData d : list) {
            ids.add(d.getDataId());
        }
        return ids;
    }
}
