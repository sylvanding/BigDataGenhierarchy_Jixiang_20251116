# Assignment 1 详细任务分解

## 一、任务目标回顾

开发一个基本的度量空间数据处理系统，采用UMAD数据集测试。

## 二、核心任务清单

### 任务1: 环境搭建与项目初始化 (优先级: 最高)

#### 1.1 开发环境准备

- [ ] 安装JDK 12或更高版本
- [ ] 安装Maven

#### 1.2 项目初始化

- [ ] 创建Maven项目结构
- [ ] 配置pom.xml文件

  ```xml
  - 设置Java版本为12
  - 添加JUnit 4.13.2依赖
  - 配置Maven编译插件
  ```

- [ ] 创建基本包结构

  ```
  core
  datatype.vector
  datatype.protein
  io
  ```

#### 1.3 数据集准备

- UMAD-Dataset中，full文件夹保存了全量数据集，包括Vector数据集和Protein数据集
- UMAD-Dataset/full/Vector目录下包含（及其对应的解压后的文件）：
  - Uniform 20-d vector.gz -> unziped/randomvector-5-1m.txt
  - Uniform 5-d vector.gz -> unziped/uniformvector-20dim-1m.txt
  - clusteredvector-2d-100k-100c.txt.gz -> unziped/clusteredvector-2d-100k-100c.txt
  - texas.txt.gz -> unziped/texas.txt
  - hawaii.txt.gz -> unziped/hawaii.txt
  - readme.html
- UMAD-Dataset/full/Protein目录下包含（及其对应的解压后的文件）：
  - yeast.zip -> unziped/yeast.txt
  - readme.html
- 已手动解压UMAD-Dataset/full/Vector目录下的所有文件，解压后文件保存在UMAD-Dataset/full/Vector/unziped目录下
- 已手动解压UMAD-Dataset/full/Protein目录下的所有文件，解压后文件保存在UMAD-Dataset/full/Protein/unziped目录下

- [ ] 需要创建测试用小数据集（手工验证用）保存至UMAD-Dataset/examples/Vector和UMAD-Dataset/examples/Protein目录下

### 任务2: 实现度量空间抽象父类 (优先级: 最高)

#### 2.1 设计MetricSpaceData抽象类

**参考**: UMAD-OriginalCode/src/main/java/db/type/IndexObject.java

**需要实现的核心内容**:

```java
public abstract class MetricSpaceData implements Serializable, Comparable<MetricSpaceData> {
    // 数据标识
    protected int dataId;
    
    // 获取数据维度/大小
    public abstract int getDimension();
    
    // 获取数据的字符串表示
    public abstract String toString();
    
    // 比较两个数据对象
    public abstract int compareTo(MetricSpaceData other);
    
    // 相等性判断
    public abstract boolean equals(Object obj);
    
    // 哈希码
    public abstract int hashCode();
}
```

**具体步骤**:

- [ ] 创建MetricSpaceData.java文件
- [ ] 定义抽象方法
- [ ] 添加必要的成员变量（数据ID等）
- [ ] 编写完整的JavaDoc注释
- [ ] 考虑序列化需求

**理论依据**: full.md 第1章 - 度量空间数据的抽象表示

### 任务3: 实现距离函数抽象父类 (优先级: 最高)

#### 3.1 设计MetricFunction接口

**参考**: UMAD-OriginalCode/src/main/java/metric/Metric.java

**需要实现的核心内容**:

```java
public interface MetricFunction extends Serializable {
    /**
     * 计算两个度量空间数据对象之间的距离
     * 必须满足度量空间的三个基本性质:
     * 1. 非负性: d(x,y) >= 0, 且 d(x,y) = 0 当且仅当 x = y
     * 2. 对称性: d(x,y) = d(y,x)
     * 3. 三角不等性: d(x,z) <= d(x,y) + d(y,z)
     */
    double getDistance(MetricSpaceData obj1, MetricSpaceData obj2);
    
    /**
     * 获取距离函数的名称
     */
    String getMetricName();
}
```

**具体步骤**:

- [ ] 创建MetricFunction.java接口
- [ ] 定义distance计算方法
- [ ] 添加完整的JavaDoc注释，说明度量空间三大性质
- [ ] 考虑异常处理（类型不匹配等）

**理论依据**: full.md 第1章 - 度量空间的三大基本性质

### 任务4: 实现向量数据类型 (优先级: 高)

#### 4.1 VectorData类设计

**参考**: UMAD-OriginalCode/src/main/java/db/type/DoubleVector.java

**需要实现的核心内容**:

```java
public class VectorData extends MetricSpaceData {
    private double[] coordinates;  // 向量坐标
    
    // 构造函数
    public VectorData(int id, double[] coordinates);
    
    // 从字符串解析（用于读取数据文件）
    public VectorData(int id, String dataLine);
    
    // 获取维度
    @Override
    public int getDimension();
    
    // 获取坐标数组
    public double[] getCoordinates();
    
    // 其他必要方法...
}
```

**具体步骤**:

- [ ] 创建VectorData.java类
- [ ] 实现构造函数（支持数组和字符串两种方式）
- [ ] 实现抽象方法
- [ ] 重写equals和hashCode方法
- [ ] 重写toString方法，便于调试
- [ ] 添加数据验证（维度检查等）

#### 4.2 VectorDataReader类设计

**功能**: 从UMAD数据集中读取向量数据

**数据格式回顾** (来自1.2 数据集介绍.md):

```
第一行: 维度 数据数量
后续行: 坐标1 坐标2 ... 坐标n (空白分隔)
```

**需要实现的核心内容**:

```java
public class VectorDataReader {
    /**
     * 从文件读取向量数据
     * @param filePath 文件路径
     * @param maxCount 最多读取的数据数量 (0表示全部读取)
     * @return 向量数据列表
     */
    public static List<VectorData> readFromFile(String filePath, int maxCount);
    
    /**
     * 读取文件头信息
     * @return [维度, 数据总量]
     */
    public static int[] readHeader(String filePath);
}
```

**具体步骤**:

- [ ] 创建VectorDataReader.java类
- [ ] 实现文件读取功能
- [ ] 处理各种异常情况（文件不存在、格式错误等）
- [ ] 添加读取进度提示
- [ ] 支持指定读取数量（用于测试小数据集）

### 任务5: 实现闵可夫斯基距离 (优先级: 高)

#### 5.1 MinkowskiDistance类设计

**参考**: UMAD-OriginalCode/src/main/java/metric/LMetric.java

**理论基础** (来自full.md 第2章):

```
闵可夫斯基距离(L-p距离):
  Lp(x,y) = (Σ|xi-yi|^p)^(1/p), p >= 1
  
特殊情况:
  L1: 曼哈顿距离 (Manhattan Distance)
  L2: 欧几里得距离 (Euclidean Distance)
  L∞: 切比雪夫距离 (Chebyshev Distance)
```

**需要实现的核心内容**:

```java
public class MinkowskiDistance implements MetricFunction {
    private final int p;  // L-p距离的p值，0表示L∞
    
    // 常用距离的静态实例
    public static final MinkowskiDistance L1 = new MinkowskiDistance(1);
    public static final MinkowskiDistance L2 = new MinkowskiDistance(2);
    public static final MinkowskiDistance LINF = new MinkowskiDistance(0);
    
    public MinkowskiDistance(int p);
    
    @Override
    public double getDistance(MetricSpaceData obj1, MetricSpaceData obj2);
    
    // 具体计算L-p距离
    private double calculateLpDistance(double[] v1, double[] v2);
}
```

**具体步骤**:

- [ ] 创建MinkowskiDistance.java类
- [ ] 实现L1距离计算
- [ ] 实现L2距离计算
- [ ] 实现L∞距离计算
- [ ] 实现通用Lp距离计算
- [ ] 添加类型检查（确保输入是VectorData）
- [ ] 添加维度检查
- [ ] 优化计算性能（避免不必要的中间变量）

#### 5.2 测试用例设计

**测试目标**: 验证闵可夫斯基距离计算的正确性

**测试类**: VectorDistanceTest.java

**测试用例设计**:

```java
public class VectorDistanceTest {
    
    @Test
    public void testL1Distance() {
        // 测试用例1: 简单二维向量
        // v1 = [0, 0], v2 = [3, 4]
        // 预期L1距离 = |3-0| + |4-0| = 7
        VectorData v1 = new VectorData(1, new double[]{0, 0});
        VectorData v2 = new VectorData(2, new double[]{3, 4});
        MinkowskiDistance metric = MinkowskiDistance.L1;
        
        double distance = metric.getDistance(v1, v2);
        assertEquals(7.0, distance, 0.0001);
        
        // 显示计算过程
        System.out.println("L1距离计算:");
        System.out.println("v1 = " + v1);
        System.out.println("v2 = " + v2);
        System.out.println("|3-0| + |4-0| = " + distance);
    }
    
    @Test
    public void testL2Distance() {
        // 测试用例2: 经典3-4-5直角三角形
        // v1 = [0, 0], v2 = [3, 4]
        // 预期L2距离 = sqrt(3^2 + 4^2) = 5
        VectorData v1 = new VectorData(1, new double[]{0, 0});
        VectorData v2 = new VectorData(2, new double[]{3, 4});
        MinkowskiDistance metric = MinkowskiDistance.L2;
        
        double distance = metric.getDistance(v1, v2);
        assertEquals(5.0, distance, 0.0001);
        
        System.out.println("L2距离计算:");
        System.out.println("sqrt(3^2 + 4^2) = sqrt(25) = " + distance);
    }
    
    @Test
    public void testLInfDistance() {
        // 测试用例3: L∞距离
        // v1 = [1, 2, 3], v2 = [4, 1, 6]
        // 预期L∞距离 = max(|4-1|, |1-2|, |6-3|) = max(3, 1, 3) = 3
        VectorData v1 = new VectorData(1, new double[]{1, 2, 3});
        VectorData v2 = new VectorData(2, new double[]{4, 1, 6});
        MinkowskiDistance metric = MinkowskiDistance.LINF;
        
        double distance = metric.getDistance(v1, v2);
        assertEquals(3.0, distance, 0.0001);
        
        System.out.println("L∞距离计算:");
        System.out.println("max(|4-1|, |1-2|, |6-3|) = " + distance);
    }
    
    @Test
    public void testDistanceProperties() {
        // 测试距离函数的三大性质
        VectorData v1 = new VectorData(1, new double[]{1, 2});
        VectorData v2 = new VectorData(2, new double[]{3, 4});
        VectorData v3 = new VectorData(3, new double[]{5, 6});
        MinkowskiDistance metric = MinkowskiDistance.L2;
        
        // 1. 非负性
        double d12 = metric.getDistance(v1, v2);
        assertTrue("非负性", d12 >= 0);
        
        // 2. 对称性
        double d21 = metric.getDistance(v2, v1);
        assertEquals("对称性", d12, d21, 0.0001);
        
        // 3. 三角不等性
        double d13 = metric.getDistance(v1, v3);
        double d23 = metric.getDistance(v2, v3);
        assertTrue("三角不等性", d13 <= d12 + d23);
        
        System.out.println("度量空间性质验证:");
        System.out.println("d(v1,v2) = " + d12 + " >= 0");
        System.out.println("d(v1,v2) = d(v2,v1) = " + d12);
        System.out.println("d(v1,v3) = " + d13 + " <= d(v1,v2) + d(v2,v3) = " + (d12 + d23));
    }
    
    @Test
    public void testWithRealData() {
        // 测试用例4: 使用UMAD数据集中的实际数据
        // 从数据集中读取前10个向量，计算任意两个之间的距离
        List<VectorData> vectors = VectorDataReader.readFromFile(
            "data/vector/Uniform 5-d vector.txt", 10);
        
        MinkowskiDistance metric = MinkowskiDistance.L2;
        
        // 计算第0个和第1个向量的距离
        double distance = metric.getDistance(vectors.get(0), vectors.get(1));
        
        System.out.println("实际数据测试:");
        System.out.println("Vector 0: " + vectors.get(0));
        System.out.println("Vector 1: " + vectors.get(1));
        System.out.println("L2 Distance: " + distance);
    }
}
```

**测试要点**:

- [ ] 每个测试用例要有清晰的预期结果
- [ ] 显示详细的计算过程
- [ ] 测试边界情况（零向量、相同向量等）
- [ ] 测试度量空间的三大性质
- [ ] 使用实际数据集进行测试

### 任务6: 实现蛋白质序列类型 (优先级: 高)

#### 6.1 ProteinData类设计

**参考**: UMAD-OriginalCode/src/main/java/db/type/Peptide.java

**需要实现的核心内容**:

```java
public class ProteinData extends MetricSpaceData {
    private String sequence;  // 氨基酸序列
    private byte[] encodedSequence;  // 编码后的序列（用于高效计算）
    
    // 氨基酸字母表
    private static final String AMINO_ACIDS = "ARNDCQEGHILKMFPSTWYV";
    
    public ProteinData(int id, String sequence);
    
    @Override
    public int getDimension() {
        return sequence.length();
    }
    
    public String getSequence();
    public byte[] getEncodedSequence();
    
    // 编码氨基酸为字节
    private byte encodeAminoAcid(char aa);
}
```

**具体步骤**:

- [ ] 创建ProteinData.java类
- [ ] 实现序列存储（字符串和编码两种形式）
- [ ] 实现氨基酸编码方法
- [ ] 处理未知氨基酸（B, Z, U, X等）
- [ ] 实现序列验证（只包含合法氨基酸）
- [ ] 重写toString方法

#### 6.2 ProteinDataReader类设计

**功能**: 从FASTA格式文件读取蛋白质序列

**数据格式回顾** (来自1.2 数据集介绍.md):

```
>gi|798902 (Z49209) Tpi1p [Saccharomyces cerevisiae]
MARTFFVGGNFKLNGSKQSIKEIVERLNTASIPENVEVVICPPATYLDYSVSLVKKPQVTVGAQNAYLKA
SGAFTGENSVDQIKDVGAKWVILGHSERRSYFHEDDKFIADKTKFALGQGVGVILCIGETLEEKKAGKTL
...

说明:
- 以">"开头的行是描述信息
- 其他行是序列数据（可以跨多行）
```

**需要实现的核心内容**:

```java
public class ProteinDataReader {
    /**
     * 从FASTA格式文件读取蛋白质序列
     * @param filePath 文件路径
     * @param maxCount 最多读取的序列数量
     * @param fragmentLength 片段长度（用于6-mers）
     * @return 蛋白质序列列表
     */
    public static List<ProteinData> readFromFile(
        String filePath, int maxCount, int fragmentLength);
    
    /**
     * 将长序列切分为固定长度的片段
     */
    private static List<String> splitIntoFragments(
        String sequence, int fragmentLength);
}
```

**具体步骤**:

- [ ] 创建ProteinDataReader.java类
- [ ] 实现FASTA格式解析
- [ ] 实现序列拼接（处理跨行序列）
- [ ] 实现序列切分（生成6-mers片段）
- [ ] 过滤无效字符
- [ ] 添加异常处理

### 任务7: 实现基于mPAM的Alignment距离 (优先级: 高)

#### 7.1 mPAM替代矩阵

**来源**: 1.2 数据集介绍.md，以及Peptide.java中的mPAM250aExtendedWeightMatrix

**mPAM矩阵**:

```
21x21的对称矩阵，表示不同氨基酸之间的替代代价
行列顺序: A, R, N, D, C, Q, E, G, H, I, L, K, M, F, P, S, T, W, Y, V, OTHER
```

#### 7.2 AlignmentDistance类设计

**理论基础**:

- 全局序列比对
- 动态规划算法
- 基于替代矩阵的匹配代价

**需要实现的核心内容**:

```java
public class AlignmentDistance implements MetricFunction {
    // mPAM替代矩阵（21x21）
    private static final double[][] MPAM_MATRIX = {
        // A, R, N, D, C, Q, E, G, H, I, L, K, M, F, P, S, T, W, Y, V, OTHER
        {0, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 5, 4, 2, 7}, // A
        {2, 0, 2, 2, 4, 2, 2, 2, 2, 3, 3, 2, 2, 4, 2, 2, 2, 4, 4, 3, 7}, // R
        // ... (完整矩阵)
    };
    
    private final int fragmentLength;  // 片段长度（默认6）
    
    public AlignmentDistance(int fragmentLength);
    
    @Override
    public double getDistance(MetricSpaceData obj1, MetricSpaceData obj2);
    
    /**
     * 计算两个序列片段的全局比对距离
     */
    private double globalAlignment(byte[] seq1, byte[] seq2);
    
    /**
     * 获取两个氨基酸之间的替代代价
     */
    private double getSubstitutionCost(byte aa1, byte aa2);
}
```

**具体步骤**:

- [ ] 创建AlignmentDistance.java类
- [ ] 定义mPAM矩阵常量
- [ ] 实现全局序列比对算法
- [ ] 实现基于mPAM的替代代价计算
- [ ] 添加类型检查
- [ ] 优化算法性能

**全局比对算法** (动态规划):

```
输入: 两个序列seq1[1..m], seq2[1..n]
输出: 比对距离

初始化:
  dp[0][0] = 0
  dp[i][0] = i * gap_penalty (for i = 1..m)
  dp[0][j] = j * gap_penalty (for j = 1..n)

递推:
  for i = 1 to m:
    for j = 1 to n:
      match = dp[i-1][j-1] + substitutionCost(seq1[i], seq2[j])
      delete = dp[i-1][j] + gap_penalty
      insert = dp[i][j-1] + gap_penalty
      dp[i][j] = min(match, delete, insert)

返回: dp[m][n]
```

#### 7.3 测试用例设计

**测试类**: ProteinDistanceTest.java

**测试用例设计**:

```java
public class ProteinDistanceTest {
    
    @Test
    public void testIdenticalSequences() {
        // 测试用例1: 相同序列
        // seq1 = "ARNDCQ", seq2 = "ARNDCQ"
        // 预期距离 = 0
        ProteinData p1 = new ProteinData(1, "ARNDCQ");
        ProteinData p2 = new ProteinData(2, "ARNDCQ");
        AlignmentDistance metric = new AlignmentDistance(6);
        
        double distance = metric.getDistance(p1, p2);
        assertEquals(0.0, distance, 0.0001);
        
        System.out.println("相同序列测试:");
        System.out.println("seq1: " + p1.getSequence());
        System.out.println("seq2: " + p2.getSequence());
        System.out.println("Distance: " + distance);
    }
    
    @Test
    public void testSingleSubstitution() {
        // 测试用例2: 单个氨基酸替换
        // seq1 = "ARNDCQ", seq2 = "ARNDCR"
        // 最后一个位置: Q -> R
        // 根据mPAM矩阵，Q到R的代价是2
        ProteinData p1 = new ProteinData(1, "ARNDCQ");
        ProteinData p2 = new ProteinData(2, "ARNDCR");
        AlignmentDistance metric = new AlignmentDistance(6);
        
        double distance = metric.getDistance(p1, p2);
        
        System.out.println("单个替换测试:");
        System.out.println("seq1: " + p1.getSequence());
        System.out.println("seq2: " + p2.getSequence());
        System.out.println("Position 6: Q -> R");
        System.out.println("Substitution cost from mPAM: 2");
        System.out.println("Total distance: " + distance);
        
        // 验证距离等于替代代价
        assertEquals(2.0, distance, 0.0001);
    }
    
    @Test
    public void testMultipleSubstitutions() {
        // 测试用例3: 多个替换
        // 手工计算预期结果
        ProteinData p1 = new ProteinData(1, "ARNDCQ");
        ProteinData p2 = new ProteinData(2, "RHKCYF");
        AlignmentDistance metric = new AlignmentDistance(6);
        
        double distance = metric.getDistance(p1, p2);
        
        System.out.println("多个替换测试:");
        System.out.println("seq1: " + p1.getSequence());
        System.out.println("seq2: " + p2.getSequence());
        System.out.println("计算过程:");
        System.out.println("A->R: mPAM[0][1] = 2");
        System.out.println("R->H: mPAM[1][8] = 2");
        System.out.println("N->K: mPAM[2][11] = 2");
        System.out.println("D->C: mPAM[3][4] = 4");
        System.out.println("C->Y: mPAM[4][18] = 3");
        System.out.println("Q->F: mPAM[5][13] = 4");
        System.out.println("Total: 2+2+2+4+3+4 = 17");
        System.out.println("Actual distance: " + distance);
    }
    
    @Test
    public void testWithRealData() {
        // 测试用例4: 实际数据集测试
        List<ProteinData> proteins = ProteinDataReader.readFromFile(
            "data/protein/yeast_sample.fasta", 10, 6);
        
        AlignmentDistance metric = new AlignmentDistance(6);
        
        // 计算前两个片段的距离
        if (proteins.size() >= 2) {
            double distance = metric.getDistance(proteins.get(0), proteins.get(1));
            
            System.out.println("实际数据测试:");
            System.out.println("Protein 0: " + proteins.get(0).getSequence());
            System.out.println("Protein 1: " + proteins.get(1).getSequence());
            System.out.println("Alignment distance: " + distance);
        }
    }
    
    @Test
    public void testDistanceProperties() {
        // 测试距离函数的度量空间性质
        ProteinData p1 = new ProteinData(1, "ARNDCQ");
        ProteinData p2 = new ProteinData(2, "RHKCYF");
        ProteinData p3 = new ProteinData(3, "ILMFPS");
        AlignmentDistance metric = new AlignmentDistance(6);
        
        // 非负性
        double d12 = metric.getDistance(p1, p2);
        assertTrue(d12 >= 0);
        
        // 对称性
        double d21 = metric.getDistance(p2, p1);
        assertEquals(d12, d21, 0.0001);
        
        // 三角不等性
        double d13 = metric.getDistance(p1, p3);
        double d23 = metric.getDistance(p2, p3);
        assertTrue(d13 <= d12 + d23);
        
        System.out.println("度量空间性质验证:");
        System.out.println("d(p1,p2) = " + d12);
        System.out.println("d(p2,p1) = " + d21);
        System.out.println("d(p1,p3) = " + d13);
        System.out.println("d(p2,p3) = " + d23);
        System.out.println("三角不等性: " + d13 + " <= " + (d12+d23));
    }
}
```

### 任务8: 编写综合测试程序 (优先级: 中)

#### 8.1 创建主测试类

**功能**: 综合展示所有功能，生成报告所需的测试结果

```java
public class Assignment1Demo {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Assignment 1: 度量空间数据处理系统演示");
        System.out.println("========================================\n");
        
        // 1. 向量数据测试
        demoVectorData();
        
        // 2. 蛋白质序列数据测试
        demoProteinData();
        
        // 3. 性能测试
        performanceTest();
    }
    
    private static void demoVectorData() {
        System.out.println("=== 1. 向量数据类型测试 ===\n");
        
        // 1.1 读取数据
        System.out.println("1.1 从UMAD数据集读取向量数据:");
        List<VectorData> vectors = VectorDataReader.readFromFile(
            "data/vector/Uniform 5-d vector.txt", 100);
        System.out.println("成功读取 " + vectors.size() + " 个5维向量");
        System.out.println("前3个向量:");
        for (int i = 0; i < 3 && i < vectors.size(); i++) {
            System.out.println("  Vector " + i + ": " + vectors.get(i));
        }
        System.out.println();
        
        // 1.2 L1距离测试
        System.out.println("1.2 L1距离(曼哈顿距离)计算:");
        testDistance(vectors, MinkowskiDistance.L1, "L1");
        
        // 1.3 L2距离测试
        System.out.println("1.3 L2距离(欧几里得距离)计算:");
        testDistance(vectors, MinkowskiDistance.L2, "L2");
        
        // 1.4 L∞距离测试
        System.out.println("1.4 L∞距离(切比雪夫距离)计算:");
        testDistance(vectors, MinkowskiDistance.LINF, "L∞");
        
        System.out.println();
    }
    
    private static void demoProteinData() {
        System.out.println("=== 2. 蛋白质序列类型测试 ===\n");
        
        // 2.1 读取数据
        System.out.println("2.1 从UMAD数据集读取蛋白质序列:");
        List<ProteinData> proteins = ProteinDataReader.readFromFile(
            "data/protein/yeast.fasta", 50, 6);
        System.out.println("成功读取 " + proteins.size() + " 个蛋白质6-mer片段");
        System.out.println("前3个片段:");
        for (int i = 0; i < 3 && i < proteins.size(); i++) {
            System.out.println("  Protein " + i + ": " + proteins.get(i).getSequence());
        }
        System.out.println();
        
        // 2.2 Alignment距离测试
        System.out.println("2.2 基于mPAM的Alignment距离计算:");
        AlignmentDistance metric = new AlignmentDistance(6);
        
        for (int i = 0; i < 3 && i < proteins.size() - 1; i++) {
            ProteinData p1 = proteins.get(i);
            ProteinData p2 = proteins.get(i + 1);
            double distance = metric.getDistance(p1, p2);
            
            System.out.println("Protein " + i + " vs Protein " + (i+1) + ":");
            System.out.println("  seq1: " + p1.getSequence());
            System.out.println("  seq2: " + p2.getSequence());
            System.out.println("  Alignment Distance: " + distance);
            System.out.println();
        }
    }
    
    private static void performanceTest() {
        System.out.println("=== 3. 性能测试 ===\n");
        
        // 测试不同数据规模下的计算时间
        int[] sizes = {100, 1000, 10000};
        
        System.out.println("3.1 向量距离计算性能:");
        for (int size : sizes) {
            List<VectorData> vectors = VectorDataReader.readFromFile(
                "data/vector/Uniform 5-d vector.txt", size);
            
            long startTime = System.currentTimeMillis();
            int count = 0;
            for (int i = 0; i < vectors.size() && i < 100; i++) {
                for (int j = i + 1; j < vectors.size(); j++) {
                    MinkowskiDistance.L2.getDistance(vectors.get(i), vectors.get(j));
                    count++;
                }
            }
            long endTime = System.currentTimeMillis();
            
            System.out.println("  数据规模: " + size);
            System.out.println("  距离计算次数: " + count);
            System.out.println("  总耗时: " + (endTime - startTime) + " ms");
            System.out.println();
        }
    }
    
    private static void testDistance(List<VectorData> vectors,
                                     MinkowskiDistance metric,
                                     String metricName) {
        // 计算前几个向量之间的距离
        for (int i = 0; i < 3 && i < vectors.size() - 1; i++) {
            VectorData v1 = vectors.get(i);
            VectorData v2 = vectors.get(i + 1);
            double distance = metric.getDistance(v1, v2);
            
            System.out.println("  Vector " + i + " vs Vector " + (i+1) + 
                             ": " + metricName + " = " + distance);
        }
        System.out.println();
    }
}
```

### 任务9: 实验报告撰写 (优先级: 高)

#### 9.1 报告结构

根据Assignment1-Requirements.md，报告应包含以下章节：

```
1. 引言
  1.1 研究背景与意义
  1.2 任务描述与目标
  1.3 实验环境
  1.4 报告结构

2. 系统设计
  2.1 总体架构设计
    2.1.1 设计思想与原则
    2.1.2 系统模块划分图
  2.2 核心抽象类设计
    2.2.1 度量空间数据抽象父类
    2.2.2 度量空间距离函数抽象父类
  2.3 具体子类设计
    2.3.1 向量数据类型与闵可夫斯基距离
    2.3.2 蛋白质序列类型与Alignment距离

3. 核心代码实现
  3.1 抽象基类实现
    3.1.1 度量空间数据抽象类代码
    3.1.2 度量空间距离函数抽象类代码
  3.2 向量数据模块实现
    3.2.1 从UMAD数据集读取向量
    3.2.2 闵可夫斯基距离计算
  3.3 蛋白质序列模块实现
    3.3.1 从UMAD数据集读取蛋白质序列
    3.3.2 基于mPAM的Alignment距离计算

4. 测试与结果分析
  4.1 测试环境与数据集
    4.1.1 硬件与软件环境
    4.1.2 测试数据集描述
  4.2 向量数据模块正确性验证
    4.2.1 测试用例设计
    4.2.2 计算过程展示与手动验证
    4.2.3 结果分析
  4.3 蛋白质序列模块正确性验证
    4.3.1 测试用例设计
    4.3.2 计算过程展示与手动验证
    4.3.3 结果分析

5. 总结与展望
  5.1 工作总结
  5.2 系统不足与改进方向
```

#### 9.2 各章节写作要点

**第1章 引言**:

- [ ] 1.1节: 介绍大数据的3V特性，重点是Variety（多样性），引出度量空间作为通用数据处理方法的意义
- [ ] 1.2节: 明确Assignment 1的4个核心任务
- [ ] 1.3节: 列出实验环境（JDK版本、IDE、操作系统等）
- [ ] 1.4节: 简述报告的组织结构

**第2章 系统设计**:

- [ ] 2.1.1节: 说明设计思想（面向对象、抽象优先、接口分离等）
- [ ] 2.1.2节: 绘制系统模块划分图（UML类图）
- [ ] 2.2.1节: 详细说明MetricSpaceData的设计（为什么这样设计、包含哪些抽象方法）
- [ ] 2.2.2节: 详细说明MetricFunction的设计，强调度量空间三大性质
- [ ] 2.3.1节: 说明VectorData和MinkowskiDistance的设计，包含类图
- [ ] 2.3.2节: 说明ProteinData和AlignmentDistance的设计，包含类图

**第3章 核心代码实现**:

- [ ] 3.1节: 展示抽象类的关键代码（不要全部代码，选取核心部分）
- [ ] 3.2节: 展示向量数据模块的关键代码，包括：
  - 数据读取代码片段
  - L1, L2, L∞距离计算的核心算法
- [ ] 3.3节: 展示蛋白质序列模块的关键代码，包括：
  - FASTA格式解析代码片段
  - mPAM矩阵定义
  - 全局比对算法的核心代码

**第4章 测试与结果分析**:

- [ ] 4.1节: 详细列出测试环境和使用的数据集
- [ ] 4.2节: 向量数据测试
  - 设计至少3个测试用例（简单可验证的）
  - 对每个测试用例，展示：
    - 输入数据
    - 预期结果（手工计算）
    - 程序输出结果
    - 对比验证
  - 包括度量空间三大性质的验证
- [ ] 4.3节: 蛋白质序列测试
  - 设计至少3个测试用例
  - 展示计算过程（基于mPAM矩阵）
  - 验证正确性

**第5章 总结与展望**:

- [ ] 5.1节: 总结完成的工作和取得的成果
- [ ] 5.2节: 分析系统的不足之处和可能的改进方向

#### 9.3 报告写作技巧

- [ ] 使用LaTeX编写，确保格式专业
- [ ] 插入清晰的UML类图（使用PlantUML或draw.io）
- [ ] 代码片段使用listings包，保持格式整洁
- [ ] 测试结果使用表格展示，便于对比
- [ ] 适当添加算法流程图
- [ ] 确保图表编号正确，引用一致
- [ ] 参考文献格式规范

## 三、验收标准

### 3.1 功能完整性

- [ ] 所有抽象类和接口定义清晰
- [ ] 向量数据类型和闵可夫斯基距离实现正确
- [ ] 蛋白质序列类型和Alignment距离实现正确
- [ ] 能够正确读取UMAD数据集

### 3.2 代码质量

- [ ] 代码结构清晰，符合Java规范
- [ ] 每个类和方法都有完整的JavaDoc注释
- [ ] 异常处理合理
- [ ] 没有明显的性能问题

### 3.3 测试质量

- [ ] 每种数据类型至少3个有代表性的测试
- [ ] 测试用例设计合理，易于验证
- [ ] 测试输出清晰，显示计算过程
- [ ] 验证了度量空间的三大性质

### 3.4 报告质量

- [ ] 结构完整，符合要求的大纲
- [ ] 内容充实，理论分析透彻
- [ ] 图表清晰，代码展示规范
- [ ] LaTeX编译无错误，格式专业

## 四、常见问题与解决方案

### 问题1: 如何验证距离函数的正确性？

**解决方案**:

1. 使用简单的、可以手工计算的测试数据
2. 展示详细的计算过程
3. 与预期结果对比
4. 验证度量空间三大性质

### 问题2: 蛋白质序列的6-mers如何生成？

**解决方案**:
从长序列中滑动窗口提取，例如：

```
序列: ABCDEFGH
6-mers: ABCDEF, BCDEFG, CDEFGH
```

### 问题3: 全局序列比对算法如何实现？

**解决方案**:
使用动态规划，参考Needleman-Wunsch算法，但使用mPAM矩阵作为替代代价

### 问题4: 如何处理大规模数据集？

**解决方案**:

1. 提供参数控制读取数量
2. 使用流式读取，避免一次性加载全部数据
3. 添加进度提示

## 五、参考资料

### 5.1 理论参考

- full.md 第1章: 度量空间基本概念
- full.md 第2章: 常见度量空间实例
- 1.2 数据集介绍.md: 数据格式说明

### 5.2 代码参考

- UMAD-OriginalCode/src/main/java/db/type/: 数据类型实现参考
- UMAD-OriginalCode/src/main/java/metric/: 距离函数实现参考

### 5.3 外部资源

- Java官方文档
- JUnit测试框架文档
- LaTeX文档编写指南
