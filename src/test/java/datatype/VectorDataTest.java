package datatype;

import core.MetricSpaceData;
import datatype.vector.MinkowskiDistance;
import datatype.vector.VectorData;
import io.VectorDataReader;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 向量数据类型测试
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class VectorDataTest {

    @Test
    public void testVectorConstruction() {
        System.out.println("=== 测试1：向量构造 ===");

        // 从数组构造
        double[] coords = { 1.0, 2.0, 3.0 };
        VectorData v1 = new VectorData(1, coords);

        assertEquals(3, v1.getDimension());
        assertEquals(1.0, v1.getCoordinate(0), 0.0001);
        assertEquals(2.0, v1.getCoordinate(1), 0.0001);
        assertEquals(3.0, v1.getCoordinate(2), 0.0001);

        System.out.println("从数组构造: " + v1);

        // 从字符串构造
        VectorData v2 = new VectorData(2, "4.5 5.5 6.5");
        assertEquals(3, v2.getDimension());
        assertEquals(4.5, v2.getCoordinate(0), 0.0001);

        System.out.println("从字符串构造: " + v2);
        System.out.println("测试通过！\n");
    }

    @Test
    public void testL1Distance() {
        System.out.println("=== 测试2：L1距离（曼哈顿距离）===");

        // 测试用例1: 简单二维向量
        VectorData v1 = new VectorData(1, new double[] { 0, 0 });
        VectorData v2 = new VectorData(2, new double[] { 3, 4 });
        MinkowskiDistance metric = MinkowskiDistance.L1;

        double distance = metric.getDistance(v1, v2);

        System.out.println("向量1: " + v1);
        System.out.println("向量2: " + v2);
        System.out.println("计算过程: |3-0| + |4-0| = 3 + 4 = 7");
        System.out.println("预期结果: 7.0");
        System.out.println("实际结果: " + distance);

        assertEquals(7.0, distance, 0.0001);
        System.out.println("测试通过！\n");
    }

    @Test
    public void testL2Distance() {
        System.out.println("=== 测试3：L2距离（欧几里得距离）===");

        // 经典3-4-5直角三角形
        VectorData v1 = new VectorData(1, new double[] { 0, 0 });
        VectorData v2 = new VectorData(2, new double[] { 3, 4 });
        MinkowskiDistance metric = MinkowskiDistance.L2;

        double distance = metric.getDistance(v1, v2);

        System.out.println("向量1: " + v1);
        System.out.println("向量2: " + v2);
        System.out.println("计算过程: sqrt(3² + 4²) = sqrt(9 + 16) = sqrt(25) = 5");
        System.out.println("预期结果: 5.0");
        System.out.println("实际结果: " + distance);

        assertEquals(5.0, distance, 0.0001);
        System.out.println("测试通过！\n");
    }

    @Test
    public void testLInfDistance() {
        System.out.println("=== 测试4：L∞距离（切比雪夫距离）===");

        VectorData v1 = new VectorData(1, new double[] { 1, 2, 3 });
        VectorData v2 = new VectorData(2, new double[] { 4, 1, 6 });
        MinkowskiDistance metric = MinkowskiDistance.LINF;

        double distance = metric.getDistance(v1, v2);

        System.out.println("向量1: " + v1);
        System.out.println("向量2: " + v2);
        System.out.println("计算过程:");
        System.out.println("  |4-1| = 3");
        System.out.println("  |1-2| = 1");
        System.out.println("  |6-3| = 3");
        System.out.println("  max(3, 1, 3) = 3");
        System.out.println("预期结果: 3.0");
        System.out.println("实际结果: " + distance);

        assertEquals(3.0, distance, 0.0001);
        System.out.println("测试通过！\n");
    }

    @Test
    public void testMetricProperties() {
        System.out.println("=== 测试5：度量空间三大性质验证 ===");

        VectorData v1 = new VectorData(1, new double[] { 1, 2 });
        VectorData v2 = new VectorData(2, new double[] { 3, 4 });
        VectorData v3 = new VectorData(3, new double[] { 5, 6 });
        MinkowskiDistance metric = MinkowskiDistance.L2;

        // 1. 非负性
        double d12 = metric.getDistance(v1, v2);
        double d11 = metric.getDistance(v1, v1);

        System.out.println("1. 非负性测试:");
        System.out.println("   d(v1, v2) = " + d12 + " >= 0 ✓");
        System.out.println("   d(v1, v1) = " + d11 + " = 0 ✓");
        assertTrue("非负性", d12 >= 0);
        assertEquals("同一对象距离为0", 0.0, d11, 0.0001);

        // 2. 对称性
        double d21 = metric.getDistance(v2, v1);
        System.out.println("\n2. 对称性测试:");
        System.out.println("   d(v1, v2) = " + d12);
        System.out.println("   d(v2, v1) = " + d21);
        System.out.println("   相等性: " + (Math.abs(d12 - d21) < 0.0001) + " ✓");
        assertEquals("对称性", d12, d21, 0.0001);

        // 3. 三角不等性
        double d13 = metric.getDistance(v1, v3);
        double d23 = metric.getDistance(v2, v3);
        System.out.println("\n3. 三角不等性测试:");
        System.out.println("   d(v1, v3) = " + d13);
        System.out.println("   d(v1, v2) = " + d12);
        System.out.println("   d(v2, v3) = " + d23);
        System.out.println("   d(v1, v3) <= d(v1, v2) + d(v2, v3)?");
        System.out.println(
                "   " + d13 + " <= " + (d12 + d23) + " = " + (d13 <= d12 + d23 + 0.0001) + " ✓");
        assertTrue("三角不等性", d13 <= d12 + d23 + 0.0001);

        System.out.println("\n所有度量空间性质验证通过！\n");
    }

    @Test
    public void testRealData() throws IOException {
        System.out.println("=== 测试6：实际数据集测试 ===");

        // 尝试读取测试数据
        try {
            List<VectorData> vectors = VectorDataReader
                    .readFromFile("UMAD-Dataset/examples/Vector/test_vectors_2d.txt", 5);

            System.out.println("成功读取 " + vectors.size() + " 个向量");
            System.out.println("前3个向量:");
            for (int i = 0; i < Math.min(3, vectors.size()); i++) {
                System.out.println("  " + vectors.get(i));
            }

            if (vectors.size() >= 2) {
                MinkowskiDistance metric = MinkowskiDistance.L2;
                double dist = metric.getDistance(vectors.get(0), vectors.get(1));
                System.out.println("\n向量0和向量1之间的L2距离: " + dist);
            }

            System.out.println("测试通过！\n");
        } catch (IOException e) {
            System.out.println("警告：无法读取测试数据文件，跳过此测试");
            System.out.println("错误信息: " + e.getMessage() + "\n");
        }
    }
}
