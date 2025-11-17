import core.MetricSpaceData;
import datatype.vector.MinkowskiDistance;
import datatype.vector.VectorData;
import datatype.protein.AlignmentDistance;
import datatype.protein.ProteinData;
import io.VectorDataReader;
import io.ProteinDataReader;
import query.*;
import index.*;

import java.io.IOException;
import java.util.List;

/**
 * 系统演示程序
 *
 * 展示系统的主要功能：
 * 1. 向量数据处理和距离计算
 * 2. 蛋白质序列处理和比对
 * 3. 相似性查询（范围查询、kNN、dkNN）
 * 4. Pivot Table索引和查询加速
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class Demo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  度量空间数据管理与分析系统演示");
        System.out.println("========================================\n");

        try {
            // 演示1：向量数据处理
            demoVectorData();

            // 演示2：蛋白质序列处理
            demoProteinData();

            // 演示3：相似性查询
            demoSimilarityQueries();

            // 演示4：Pivot Table索引
            demoPivotTable();

            System.out.println("\n========================================");
            System.out.println("  所有演示完成！");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("演示过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 演示1：向量数据处理
     */
    private static void demoVectorData() throws IOException {
        System.out.println("=== 演示1：向量数据处理 ===\n");

        try {
            // 读取向量数据
            System.out.println("1.1 读取向量数据...");
            List<VectorData> vectors = VectorDataReader
                    .readFromFile("UMAD-Dataset/examples/Vector/test_vectors_2d.txt", 5);

            if (vectors.isEmpty()) {
                System.out.println("警告：未能读取到向量数据，跳过此演示\n");
                return;
            }

            System.out.println("前3个向量:");
            for (int i = 0; i < Math.min(3, vectors.size()); i++) {
                System.out.println("  " + vectors.get(i));
            }

            // 计算距离
            System.out.println("\n1.2 计算距离...");
            VectorData v1 = vectors.get(0);
            VectorData v2 = vectors.get(1);

            double l1 = MinkowskiDistance.L1.getDistance(v1, v2);
            double l2 = MinkowskiDistance.L2.getDistance(v1, v2);
            double linf = MinkowskiDistance.LINF.getDistance(v1, v2);

            System.out.println("向量1: " + v1);
            System.out.println("向量2: " + v2);
            System.out.println("L1距离: " + String.format("%.4f", l1));
            System.out.println("L2距离: " + String.format("%.4f", l2));
            System.out.println("L∞距离: " + String.format("%.4f", linf));
            System.out.println();

        } catch (IOException e) {
            System.out.println("无法读取测试数据: " + e.getMessage());
            System.out.println("提示：请确保 UMAD-Dataset/examples/Vector/test_vectors_2d.txt 存在\n");
        }
    }

    /**
     * 演示2：蛋白质序列处理
     */
    private static void demoProteinData() throws IOException {
        System.out.println("=== 演示2：蛋白质序列处理 ===\n");

        try {
            // 读取蛋白质序列
            System.out.println("2.1 读取蛋白质序列（6-mers）...");
            List<ProteinData> proteins = ProteinDataReader
                    .readFromFile("UMAD-Dataset/examples/Protein/test_proteins.fasta", 5, 6);

            if (proteins.isEmpty()) {
                System.out.println("警告：未能读取到蛋白质数据，跳过此演示\n");
                return;
            }

            System.out.println("前3个序列片段:");
            for (int i = 0; i < Math.min(3, proteins.size()); i++) {
                System.out.println("  " + proteins.get(i));
            }

            // 计算比对距离
            System.out.println("\n2.2 计算序列比对距离...");
            ProteinData p1 = proteins.get(0);
            ProteinData p2 = proteins.get(1);

            AlignmentDistance metric = new AlignmentDistance(6);
            double distance = metric.getDistance(p1, p2);

            System.out.println("序列1: " + p1.getSequence());
            System.out.println("序列2: " + p2.getSequence());
            System.out.println("Alignment距离: " + String.format("%.4f", distance));
            System.out.println();

        } catch (IOException e) {
            System.out.println("无法读取测试数据: " + e.getMessage());
            System.out.println("提示：请确保 UMAD-Dataset/examples/Protein/test_proteins.fasta 存在\n");
        }
    }

    /**
     * 演示3：相似性查询
     */
    private static void demoSimilarityQueries() throws IOException {
        System.out.println("=== 演示3：相似性查询 ===\n");

        try {
            List<VectorData> vectors = VectorDataReader
                    .readFromFile("UMAD-Dataset/examples/Vector/test_vectors_2d.txt", 5);

            if (vectors.size() < 2) {
                System.out.println("数据不足，跳过此演示\n");
                return;
            }

            VectorData queryObject = vectors.get(0);

            // 范围查询
            System.out.println("3.1 范围查询 (radius=2.0)");
            RangeQuery rangeQuery = new RangeQuery(queryObject, 2.0);
            List<MetricSpaceData> rangeResults =
                    LinearScanRangeQuery.execute(vectors, rangeQuery, MinkowskiDistance.L2);
            System.out.println("找到 " + rangeResults.size() + " 个结果\n");

            // kNN查询
            System.out.println("3.2 kNN查询 (k=3)");
            KNNQuery knnQuery = new KNNQuery(queryObject, 3);
            List<KNNResult> knnResults =
                    LinearScanKNNQuery.execute(vectors, knnQuery, MinkowskiDistance.L2);
            System.out.println("Top-3 最近邻:");
            for (int i = 0; i < knnResults.size(); i++) {
                System.out.println("  " + (i + 1) + ". distance = "
                        + String.format("%.4f", knnResults.get(i).getDistance()));
            }
            System.out.println();

        } catch (IOException e) {
            System.out.println("无法读取测试数据，跳过此演示\n");
        }
    }

    /**
     * 演示4：Pivot Table索引
     */
    private static void demoPivotTable() throws IOException {
        System.out.println("=== 演示4：Pivot Table索引 ===\n");

        try {
            List<VectorData> vectors = VectorDataReader
                    .readFromFile("UMAD-Dataset/examples/Vector/test_vectors_2d.txt", 5);

            if (vectors.size() < 3) {
                System.out.println("数据不足，跳过此演示\n");
                return;
            }

            // 构建Pivot Table
            System.out.println("4.1 构建Pivot Table索引...");
            PivotTable pivotTable =
                    new PivotTable(vectors, 2, MinkowskiDistance.L2, PivotSelectionMethod.FFT);

            // 使用索引进行查询
            System.out.println("\n4.2 使用索引进行范围查询...");
            VectorData queryObject = vectors.get(0);
            RangeQuery query = new RangeQuery(queryObject, 2.0);

            List<MetricSpaceData> results = PivotTableRangeQuery.execute(pivotTable, query);

            System.out.println("查询完成！\n");

        } catch (IOException e) {
            System.out.println("无法读取测试数据，跳过此演示\n");
        }
    }
}

