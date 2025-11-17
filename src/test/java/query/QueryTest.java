package query;

import datatype.vector.MinkowskiDistance;
import datatype.vector.VectorData;
import index.*;
import io.VectorDataReader;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 查询功能测试
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class QueryTest {

    @Test
    public void testRangeQuery() throws IOException {
        System.out.println("=== 测试1：范围查询 ===\n");

        // 创建简单测试数据
        List<VectorData> dataset = new ArrayList<>();
        dataset.add(new VectorData(0, new double[] {0, 0}));
        dataset.add(new VectorData(1, new double[] {1, 0}));
        dataset.add(new VectorData(2, new double[] {0, 1}));
        dataset.add(new VectorData(3, new double[] {3, 4}));
        dataset.add(new VectorData(4, new double[] {5, 5}));

        // 查询对象: (0,0), 半径: 1.5
        VectorData queryObject = new VectorData(-1, new double[] {0, 0});
        RangeQuery query = new RangeQuery(queryObject, 1.5);

        System.out.println("查询对象: " + queryObject);
        System.out.println("查询半径: 1.5");
        System.out.println("数据集: " + dataset.size() + " 个向量");
        System.out.println();

        // 执行查询
        List<core.MetricSpaceData> results =
                LinearScanRangeQuery.execute(dataset, query, MinkowskiDistance.L2);

        System.out.println("\n预期结果: 3 个 (距离 <= 1.5)");
        System.out.println("  - (0,0), distance = 0");
        System.out.println("  - (1,0), distance = 1");
        System.out.println("  - (0,1), distance = 1");
        System.out.println("实际结果: " + results.size() + " 个");

        assertEquals(3, results.size());
        System.out.println("测试通过！\n");
    }

    @Test
    public void testKNNQuery() {
        System.out.println("=== 测试2：kNN查询 ===\n");

        List<VectorData> dataset = new ArrayList<>();
        dataset.add(new VectorData(0, new double[] {0, 0}));
        dataset.add(new VectorData(1, new double[] {1, 0}));
        dataset.add(new VectorData(2, new double[] {0, 1}));
        dataset.add(new VectorData(3, new double[] {3, 4}));
        dataset.add(new VectorData(4, new double[] {5, 5}));

        VectorData queryObject = new VectorData(-1, new double[] {0, 0});
        KNNQuery query = new KNNQuery(queryObject, 3);

        System.out.println("查询对象: " + queryObject);
        System.out.println("k值: 3");
        System.out.println();

        List<KNNResult> results = LinearScanKNNQuery.execute(dataset, query, MinkowskiDistance.L2);

        System.out.println("\n预期结果（按距离升序）:");
        System.out.println("  1. (0,0), distance = 0");
        System.out.println("  2. (1,0), distance = 1");
        System.out.println("  3. (0,1), distance = 1");
        System.out.println("实际结果:");
        for (int i = 0; i < results.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + results.get(i));
        }

        assertEquals(3, results.size());
        assertEquals(0.0, results.get(0).getDistance(), 0.0001);
        System.out.println("测试通过！\n");
    }

    @Test
    public void testDKNNQuery() {
        System.out.println("=== 测试3：dkNN查询 ===\n");

        // 创建聚类数据：两个聚类
        List<VectorData> dataset = new ArrayList<>();
        // 聚类1: 围绕(0,0)
        dataset.add(new VectorData(0, new double[] {0, 0}));
        dataset.add(new VectorData(1, new double[] {0.1, 0}));
        dataset.add(new VectorData(2, new double[] {0, 0.1}));
        // 聚类2: 围绕(10,10)
        dataset.add(new VectorData(3, new double[] {10, 10}));
        dataset.add(new VectorData(4, new double[] {10.1, 10}));
        dataset.add(new VectorData(5, new double[] {10, 10.1}));

        VectorData queryObject = new VectorData(-1, new double[] {0, 0});

        System.out.println("查询对象: " + queryObject);
        System.out.println("数据集包含2个聚类");
        System.out.println();

        // kNN查询（不考虑多样性）
        KNNQuery knnQuery = new KNNQuery(queryObject, 3);
        List<KNNResult> knnResults =
                LinearScanKNNQuery.execute(dataset, knnQuery, MinkowskiDistance.L2);

        // dkNN查询（高多样性）
        DKNNQuery dknnQuery = new DKNNQuery(queryObject, 3, 0.8);
        List<KNNResult> dknnResults =
                LinearScanDKNNQuery.execute(dataset, dknnQuery, MinkowskiDistance.L2);

        System.out.println("kNN结果（可能都来自聚类1）:");
        for (KNNResult r : knnResults) {
            System.out.println("  " + r);
        }

        System.out.println("\ndkNN结果（应该包含多个聚类）:");
        for (KNNResult r : dknnResults) {
            System.out.println("  " + r);
        }

        System.out.println("\n测试通过！\n");
    }

    @Test
    public void testPivotTableRangeQuery() throws IOException {
        System.out.println("=== 测试4：Pivot Table范围查询 ===\n");

        // 尝试使用测试数据
        try {
            List<VectorData> dataset = VectorDataReader
                    .readFromFile("UMAD-Dataset/examples/Vector/test_vectors_2d.txt", 5);

            if (dataset.size() < 2) {
                System.out.println("测试数据不足，跳过此测试\n");
                return;
            }

            // 构建Pivot Table
            PivotTable pivotTable =
                    new PivotTable(dataset, 2, MinkowskiDistance.L2, PivotSelectionMethod.FFT);

            // 执行范围查询
            VectorData queryObject = dataset.get(0);
            RangeQuery query = new RangeQuery(queryObject, 2.0);

            List<core.MetricSpaceData> ptResults = PivotTableRangeQuery.execute(pivotTable, query);
            List<core.MetricSpaceData> lsResults =
                    LinearScanRangeQuery.execute(dataset, query, MinkowskiDistance.L2);

            System.out.println("Pivot Table结果数: " + ptResults.size());
            System.out.println("线性扫描结果数: " + lsResults.size());

            assertEquals("结果数量应该一致", lsResults.size(), ptResults.size());
            System.out.println("正确性验证通过！\n");

        } catch (IOException e) {
            System.out.println("无法读取测试数据，跳过此测试");
            System.out.println("错误: " + e.getMessage() + "\n");
        }
    }

    @Test
    public void testPivotTableKNNQuery() throws IOException {
        System.out.println("=== 测试5：Pivot Table kNN查询 ===\n");

        try {
            List<VectorData> dataset = VectorDataReader
                    .readFromFile("UMAD-Dataset/examples/Vector/test_vectors_2d.txt", 5);

            if (dataset.size() < 2) {
                System.out.println("测试数据不足，跳过此测试\n");
                return;
            }

            // 构建Pivot Table
            PivotTable pivotTable =
                    new PivotTable(dataset, 2, MinkowskiDistance.L2, PivotSelectionMethod.RANDOM);

            // 执行kNN查询
            VectorData queryObject = dataset.get(0);
            KNNQuery query = new KNNQuery(queryObject, 3);

            List<KNNResult> ptResults = PivotTableKNNQuery.execute(pivotTable, query);
            List<KNNResult> lsResults =
                    LinearScanKNNQuery.execute(dataset, query, MinkowskiDistance.L2);

            System.out.println("Pivot Table结果数: " + ptResults.size());
            System.out.println("线性扫描结果数: " + lsResults.size());

            assertEquals("结果数量应该一致", lsResults.size(), ptResults.size());

            // 验证结果距离相同
            for (int i = 0; i < ptResults.size(); i++) {
                assertEquals("第" + i + "个结果距离应该相同", lsResults.get(i).getDistance(),
                        ptResults.get(i).getDistance(), 0.0001);
            }

            System.out.println("正确性验证通过！\n");

        } catch (IOException e) {
            System.out.println("无法读取测试数据，跳过此测试");
            System.out.println("错误: " + e.getMessage() + "\n");
        }
    }
}

