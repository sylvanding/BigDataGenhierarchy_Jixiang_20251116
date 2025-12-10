package index.tree;

import core.MetricSpaceData;
import core.MetricFunction;
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import datatype.protein.ProteinData;
import datatype.protein.AlignmentDistance;
import index.tree.ghtree.GHTree;
import index.tree.vptree.VPTree;
import index.tree.common.TreeConfig;
import query.RangeQuery;
import query.LinearScanRangeQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GH树和VP树正确性验证测试
 *
 * 验证树索引查询结果与线性扫描结果的一致性
 * 包括向量数据和蛋白质序列数据的测试
 *
 * @author Jixiang Ding
 */
@DisplayName("树索引正确性验证测试")
public class TreeCorrectnessTest {

    @Test
    @DisplayName("测试1: 2D向量范围查询正确性")
    void testVector2DRangeQueryCorrectness() {
        System.out.println("\n=== 测试1: 2D向量范围查询正确性 ===");

        List<VectorData> dataset = createVector2DDataset(100);
        MetricFunction metric = MinkowskiDistance.L2;

        TreeConfig config = new TreeConfig.Builder()
                .maxLeafSize(10)
                .minTreeHeight(3)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .build();

        GHTree ghTree = new GHTree(config);
        ghTree.buildIndex(dataset, metric);

        VPTree vpTree = new VPTree(config);
        vpTree.buildIndex(dataset, metric);

        Random rand = new Random(123);
        double[] radii = { 0.5, 1.0, 2.0, 5.0 };
        int numQueries = 10;

        System.out.println("数据集大小: " + dataset.size());
        System.out.println("测试查询数: " + numQueries);

        int passCount = 0;
        int totalTests = numQueries * radii.length;

        for (int q = 0; q < numQueries; q++) {
            VectorData queryPoint = dataset.get(rand.nextInt(dataset.size()));

            for (double radius : radii) {
                List<MetricSpaceData> linearResults = LinearScanRangeQuery.execute(
                        dataset, new RangeQuery(queryPoint, radius), metric, false);

                ghTree.resetStatistics();
                List<MetricSpaceData> ghResults = ghTree.rangeQuery(queryPoint, radius);

                vpTree.resetStatistics();
                List<MetricSpaceData> vpResults = vpTree.rangeQuery(queryPoint, radius);

                boolean ghCorrect = setEquals(linearResults, ghResults);
                boolean vpCorrect = setEquals(linearResults, vpResults);

                if (ghCorrect && vpCorrect) {
                    passCount++;
                }

                assertTrue(ghCorrect, "GH树结果应与线性扫描一致");
                assertTrue(vpCorrect, "VP树结果应与线性扫描一致");
            }
        }

        System.out.printf("通过率: %d/%d (%.1f%%)%n", passCount, totalTests,
                100.0 * passCount / totalTests);
        System.out.println("测试通过！\n");
    }

    @Test
    @DisplayName("测试2: 高维向量(10D)范围查询正确性")
    void testHighDimVectorRangeQueryCorrectness() {
        System.out.println("\n=== 测试2: 高维向量(10D)范围查询正确性 ===");

        List<VectorData> dataset = createHighDimDataset(200, 10);
        MetricFunction metric = MinkowskiDistance.L2;

        TreeConfig config = new TreeConfig.Builder()
                .maxLeafSize(15)
                .minTreeHeight(3)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .build();

        GHTree ghTree = new GHTree(config);
        ghTree.buildIndex(dataset, metric);

        VPTree vpTree = new VPTree(config);
        vpTree.buildIndex(dataset, metric);

        Random rand = new Random(123);
        double[] radii = { 2.0, 3.0, 4.0 };
        int numQueries = 10;

        System.out.println("数据集大小: " + dataset.size());
        System.out.println("数据维度: 10");

        for (int q = 0; q < numQueries; q++) {
            VectorData queryPoint = dataset.get(rand.nextInt(dataset.size()));

            for (double radius : radii) {
                List<MetricSpaceData> linearResults = LinearScanRangeQuery.execute(
                        dataset, new RangeQuery(queryPoint, radius), metric, false);

                ghTree.resetStatistics();
                List<MetricSpaceData> ghResults = ghTree.rangeQuery(queryPoint, radius);

                vpTree.resetStatistics();
                List<MetricSpaceData> vpResults = vpTree.rangeQuery(queryPoint, radius);

                assertTrue(setEquals(linearResults, ghResults), "高维GH树结果应与线性扫描一致");
                assertTrue(setEquals(linearResults, vpResults), "高维VP树结果应与线性扫描一致");
            }
        }

        System.out.println("所有高维向量查询测试通过！\n");
    }

    @Test
    @DisplayName("测试3: kNN查询正确性")
    void testKNNQueryCorrectness() {
        System.out.println("\n=== 测试3: kNN查询正确性 ===");

        List<VectorData> dataset = createVector2DDataset(100);
        MetricFunction metric = MinkowskiDistance.L2;

        TreeConfig config = new TreeConfig.Builder()
                .maxLeafSize(10)
                .minTreeHeight(3)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .build();

        GHTree ghTree = new GHTree(config);
        ghTree.buildIndex(dataset, metric);

        VPTree vpTree = new VPTree(config);
        vpTree.buildIndex(dataset, metric);

        Random rand = new Random(123);
        int[] kValues = { 1, 3, 5, 10 };
        int numQueries = 10;

        System.out.println("数据集大小: " + dataset.size());

        for (int q = 0; q < numQueries; q++) {
            VectorData queryPoint = dataset.get(rand.nextInt(dataset.size()));

            List<Map.Entry<MetricSpaceData, Double>> allDistances = new ArrayList<>();
            for (VectorData v : dataset) {
                double dist = metric.getDistance(queryPoint, v);
                allDistances.add(new AbstractMap.SimpleEntry<>(v, dist));
            }
            allDistances.sort(Comparator.comparingDouble(Map.Entry::getValue));

            for (int k : kValues) {
                Set<Integer> expectedIds = new HashSet<>();
                for (int i = 0; i < k; i++) {
                    expectedIds.add(allDistances.get(i).getKey().getDataId());
                }

                ghTree.resetStatistics();
                List<MetricSpaceData> ghResults = ghTree.knnQuery(queryPoint, k);
                Set<Integer> ghIds = new HashSet<>();
                for (MetricSpaceData d : ghResults) {
                    ghIds.add(d.getDataId());
                }

                vpTree.resetStatistics();
                List<MetricSpaceData> vpResults = vpTree.knnQuery(queryPoint, k);
                Set<Integer> vpIds = new HashSet<>();
                for (MetricSpaceData d : vpResults) {
                    vpIds.add(d.getDataId());
                }

                assertEquals(k, ghResults.size(), "GH树kNN结果数量应等于k");
                assertEquals(k, vpResults.size(), "VP树kNN结果数量应等于k");
                assertEquals(expectedIds, ghIds, "GH树kNN结果应正确");
                assertEquals(expectedIds, vpIds, "VP树kNN结果应正确");
            }
        }

        System.out.println("所有kNN查询测试通过！\n");
    }

    @Test
    @DisplayName("测试4: 蛋白质序列数据正确性")
    void testProteinDataCorrectness() {
        System.out.println("\n=== 测试4: 蛋白质序列数据正确性 ===");

        List<ProteinData> dataset = createProteinDataset(50, 6);
        MetricFunction metric = new AlignmentDistance();

        TreeConfig config = new TreeConfig.Builder()
                .maxLeafSize(8)
                .minTreeHeight(3)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .build();

        GHTree ghTree = new GHTree(config);
        ghTree.buildIndex(dataset, metric);

        VPTree vpTree = new VPTree(config);
        vpTree.buildIndex(dataset, metric);

        System.out.println("数据集大小: " + dataset.size());
        System.out.println("序列长度: 6");
        System.out.println("GH树高: " + ghTree.getTreeHeight());
        System.out.println("VP树高: " + vpTree.getTreeHeight());

        Random rand = new Random(123);
        double[] radii = { 1.0, 2.0, 3.0 };
        int numQueries = 5;

        for (int q = 0; q < numQueries; q++) {
            ProteinData queryPoint = dataset.get(rand.nextInt(dataset.size()));

            for (double radius : radii) {
                List<MetricSpaceData> linearResults = LinearScanRangeQuery.execute(
                        dataset, new RangeQuery(queryPoint, radius), metric, false);

                ghTree.resetStatistics();
                List<MetricSpaceData> ghResults = ghTree.rangeQuery(queryPoint, radius);

                vpTree.resetStatistics();
                List<MetricSpaceData> vpResults = vpTree.rangeQuery(queryPoint, radius);

                assertTrue(setEquals(linearResults, ghResults), "蛋白质GH树结果应与线性扫描一致");
                assertTrue(setEquals(linearResults, vpResults), "蛋白质VP树结果应与线性扫描一致");
            }
        }

        System.out.println("所有蛋白质序列查询测试通过！\n");
    }

    @Test
    @DisplayName("测试5: 边界情况测试")
    void testEdgeCases() {
        System.out.println("\n=== 测试5: 边界情况测试 ===");

        List<VectorData> dataset = createVector2DDataset(50);
        MetricFunction metric = MinkowskiDistance.L2;

        TreeConfig config = new TreeConfig.Builder()
                .maxLeafSize(10)
                .minTreeHeight(2)
                .pivotStrategy(TreeConfig.PivotSelectionStrategy.FFT)
                .randomSeed(42)
                .build();

        GHTree ghTree = new GHTree(config);
        ghTree.buildIndex(dataset, metric);

        VPTree vpTree = new VPTree(config);
        vpTree.buildIndex(dataset, metric);

        VectorData queryPoint = dataset.get(0);

        // 测试1: 半径为0
        System.out.println("测试半径=0的查询...");
        List<MetricSpaceData> ghResults0 = ghTree.rangeQuery(queryPoint, 0.0);
        List<MetricSpaceData> vpResults0 = vpTree.rangeQuery(queryPoint, 0.0);
        assertEquals(1, ghResults0.size(), "半径0应返回查询点自身");
        assertEquals(1, vpResults0.size(), "半径0应返回查询点自身");
        System.out.println("  GH树: " + ghResults0.size() + "个结果 ✓");
        System.out.println("  VP树: " + vpResults0.size() + "个结果 ✓");

        // 测试2: 很大的半径
        System.out.println("测试很大半径的查询...");
        List<MetricSpaceData> ghResultsAll = ghTree.rangeQuery(queryPoint, 1000.0);
        List<MetricSpaceData> vpResultsAll = vpTree.rangeQuery(queryPoint, 1000.0);
        assertEquals(dataset.size(), ghResultsAll.size(), "大半径应返回所有数据");
        assertEquals(dataset.size(), vpResultsAll.size(), "大半径应返回所有数据");
        System.out.println("  GH树: " + ghResultsAll.size() + "个结果 ✓");
        System.out.println("  VP树: " + vpResultsAll.size() + "个结果 ✓");

        // 测试3: k=1的kNN
        System.out.println("测试k=1的kNN查询...");
        List<MetricSpaceData> ghKnn1 = ghTree.knnQuery(queryPoint, 1);
        List<MetricSpaceData> vpKnn1 = vpTree.knnQuery(queryPoint, 1);
        assertEquals(1, ghKnn1.size(), "k=1应返回1个结果");
        assertEquals(1, vpKnn1.size(), "k=1应返回1个结果");
        assertEquals(queryPoint.getDataId(), ghKnn1.get(0).getDataId(), "k=1最近邻应是查询点自身");
        System.out.println("  GH树: ID=" + ghKnn1.get(0).getDataId() + " ✓");
        System.out.println("  VP树: ID=" + vpKnn1.get(0).getDataId() + " ✓");

        System.out.println("测试通过！\n");
    }

    // ========== 辅助方法 ==========

    private List<VectorData> createVector2DDataset(int size) {
        List<VectorData> data = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 0; i < size; i++) {
            data.add(new VectorData(i, new double[] {
                    rand.nextDouble() * 10, rand.nextDouble() * 10
            }));
        }
        return data;
    }

    private List<VectorData> createHighDimDataset(int size, int dim) {
        List<VectorData> data = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 0; i < size; i++) {
            double[] coords = new double[dim];
            for (int j = 0; j < dim; j++) {
                coords[j] = rand.nextDouble() * 10;
            }
            data.add(new VectorData(i, coords));
        }
        return data;
    }

    private List<ProteinData> createProteinDataset(int size, int length) {
        List<ProteinData> data = new ArrayList<>();
        Random rand = new Random(42);
        char[] aminoAcids = "ACDEFGHIKLMNPQRSTVWY".toCharArray();

        for (int i = 0; i < size; i++) {
            StringBuilder seq = new StringBuilder();
            for (int j = 0; j < length; j++) {
                seq.append(aminoAcids[rand.nextInt(aminoAcids.length)]);
            }
            data.add(new ProteinData(i, seq.toString()));
        }
        return data;
    }

    private boolean setEquals(List<MetricSpaceData> list1, List<MetricSpaceData> list2) {
        if (list1.size() != list2.size())
            return false;

        Set<Integer> ids1 = new HashSet<>();
        Set<Integer> ids2 = new HashSet<>();

        for (MetricSpaceData d : list1)
            ids1.add(d.getDataId());
        for (MetricSpaceData d : list2)
            ids2.add(d.getDataId());

        return ids1.equals(ids2);
    }
}
