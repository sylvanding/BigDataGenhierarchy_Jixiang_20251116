# 度量空间数据管理与分析系统

## 项目简介

本项目是一个基于Java的通用度量空间数据管理与分析系统，实现了度量空间数据的存储、距离计算、相似性查询和索引功能。

## 系统特性

### Assignment 1 - 基础数据处理系统

- ✅ 度量空间数据抽象框架
- ✅ 向量数据类型（支持任意维度）
- ✅ 闵可夫斯基距离（L1, L2, L∞, Lp）
- ✅ 蛋白质序列数据类型
- ✅ 基于mPAM矩阵的序列比对距离
- ✅ UMAD数据集读取功能

### Assignment 2 - 相似性查询与索引

- ✅ 线性扫描查询算法
  - 范围查询 (Range Query)
  - k近邻查询 (kNN Query)
  - 多样化k近邻查询 (dkNN Query)
- ✅ Pivot Table索引
  - 多种支撑点选择策略（Random, FFT, Center, Border）
  - 基于三角不等式的剪枝优化
  - 范围查询和kNN查询加速

## 技术栈

- **编程语言**: Java 12
- **构建工具**: Maven 3.x
- **测试框架**: JUnit 4.13.2
- **开发环境**: VS Code (Recommended)

## 项目结构

```
BigDataGenhierarchy_Jixiang_20251116/
├── src/
│   ├── main/java/
│   │   ├── core/                          # 核心抽象类
│   │   │   ├── MetricSpaceData.java       # 度量空间数据抽象类
│   │   │   └── MetricFunction.java        # 距离函数接口
│   │   ├── datatype/                      # 具体数据类型实现
│   │   │   ├── vector/
│   │   │   │   ├── VectorData.java        # 向量数据类型
│   │   │   │   └── MinkowskiDistance.java # 闵可夫斯基距离
│   │   │   └── protein/
│   │   │       ├── ProteinData.java       # 蛋白质序列类型
│   │   │       └── AlignmentDistance.java # 序列比对距离
│   │   ├── io/                            # 数据读取模块
│   │   │   ├── VectorDataReader.java     # 向量数据读取器
│   │   │   └── ProteinDataReader.java    # 蛋白质数据读取器
│   │   ├── query/                         # 查询模块
│   │   │   ├── RangeQuery.java           # 范围查询
│   │   │   ├── KNNQuery.java             # kNN查询
│   │   │   ├── DKNNQuery.java            # dkNN查询
│   │   │   ├── LinearScanRangeQuery.java # 线性扫描范围查询
│   │   │   ├── LinearScanKNNQuery.java   # 线性扫描kNN查询
│   │   │   └── LinearScanDKNNQuery.java  # 线性扫描dkNN查询
│   │   └── index/                         # 索引模块
│   │       ├── PivotTable.java           # Pivot Table索引
│   │       ├── PivotSelector.java        # 支撑点选择器
│   │       ├── PivotSelectionMethod.java # 支撑点选择方法
│   │       ├── PivotTableRangeQuery.java # 基于索引的范围查询
│   │       └── PivotTableKNNQuery.java   # 基于索引的kNN查询
│   └── test/java/                         # 测试代码
│       ├── datatype/
│       │   ├── VectorDataTest.java
│       │   └── ProteinDataTest.java
│       └── query/
│           └── QueryTest.java
├── UMAD-Dataset/                          # 数据集目录
│   ├── examples/                          # 测试数据
│   └── full/                              # 完整数据集
├── docs/                                  # 文档目录
├── tasks/                                 # 任务规划文档
├── pom.xml                                # Maven配置文件
├── QUICKSTART.md                          # 快速开始指南
└── README.md
```

## 快速开始

### 1. 环境要求

- **JDK**: 12 或更高版本
- **Maven**: 3.6 或更高版本
- **操作系统**: Windows / Linux / macOS

```bash
# Windows PowerShell

java -version
# java version "18.0.2.1" 2022-08-18
# Java(TM) SE Runtime Environment (build 18.0.2.1+1-1)
# Java HotSpot(TM) 64-Bit Server VM (build 18.0.2.1+1-1, mixed mode, sharing)

javac -version
# javac 18.0.2.1

mvn -v
# Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
# Maven home: A:\tools\apache-maven-3.9.11
# Java version: 18.0.2.1, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk-18.0.2.1
# Default locale: en_US, platform encoding: UTF-8
# OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
```

### 2. 安装步骤

#### 2.1 克隆项目

```bash
git clone --depth 1 -b main https://github.com/sylvanding/BigDataGenhierarchy_Jixiang_20251116
cd BigDataGenhierarchy_Jixiang_20251116
```

#### 2.2 编译项目

使用Maven编译项目：

```bash
mvn clean compile
```

#### 2.3 运行测试

运行所有测试用例：

```bash
mvn test
```

运行特定测试类：

```bash
# 测试向量数据类型
mvn test -Dtest=VectorDataTest

# 测试蛋白质序列类型
mvn test -Dtest=ProteinDataTest

# 测试查询功能
mvn test -Dtest=QueryTest
```

#### 2.4 查看测试结果

测试结果会在控制台输出，显示：

- ✅ 测试用例执行情况
- 📊 计算过程和中间结果
- 📈 性能统计信息

## 使用示例

### 示例1：向量数据处理

```java
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import io.VectorDataReader;
import java.util.List;

// 读取向量数据
List<VectorData> vectors = VectorDataReader.readFromFile(
    "UMAD-Dataset/full/Vector/unziped/uniformvector-20dim-1m.txt", 1000);

// 创建距离函数
MinkowskiDistance metric = MinkowskiDistance.L2;

// 计算两个向量之间的距离
VectorData v1 = vectors.get(0);
VectorData v2 = vectors.get(1);
double distance = metric.getDistance(v1, v2);

System.out.println("L2距离: " + distance);
```

### 示例2：范围查询

```java
import query.*;
import index.*;

// 创建查询对象
VectorData queryObject = vectors.get(0);
RangeQuery query = new RangeQuery(queryObject, 0.1);

// 方法1：线性扫描
List<MetricSpaceData> results1 = LinearScanRangeQuery.execute(
    vectors, query, MinkowskiDistance.L2);

// 方法2：使用Pivot Table索引
PivotTable pivotTable = new PivotTable(
    vectors, 20, MinkowskiDistance.L2, PivotSelectionMethod.FFT);
List<MetricSpaceData> results2 = PivotTableRangeQuery.execute(
    pivotTable, query);

System.out.println("线性扫描结果: " + results1.size());
System.out.println("索引查询结果: " + results2.size());
```

### 示例3：kNN查询

```java
import query.*;

// 创建kNN查询
VectorData queryObject = vectors.get(0);
KNNQuery query = new KNNQuery(queryObject, 10);

// 执行查询
List<KNNResult> results = LinearScanKNNQuery.execute(
    vectors, query, MinkowskiDistance.L2);

// 输出结果
System.out.println("Top-10 最近邻:");
for (int i = 0; i < results.size(); i++) {
    KNNResult result = results.get(i);
    System.out.println((i+1) + ". " + result.getData() + 
                     ", distance = " + result.getDistance());
}
```

### 示例4：多样化kNN查询

```java
import query.*;

// 创建dkNN查询（多样性权重0.8）
VectorData queryObject = vectors.get(0);
DKNNQuery query = new DKNNQuery(queryObject, 10, 0.8);

// 执行查询
List<KNNResult> results = LinearScanDKNNQuery.execute(
    vectors, query, MinkowskiDistance.L2);

System.out.println("多样化Top-10结果:");
for (KNNResult result : results) {
    System.out.println(result);
}
```

### 示例5：蛋白质序列处理

```java
import datatype.protein.*;
import io.ProteinDataReader;

// 读取蛋白质序列（6-mers片段）
List<ProteinData> proteins = ProteinDataReader.readFromFile(
    "UMAD-Dataset/full/Protein/unziped/yeast.txt", 1000, 6);

// 创建距离函数
AlignmentDistance metric = new AlignmentDistance(6);

// 计算序列比对距离
ProteinData p1 = proteins.get(0);
ProteinData p2 = proteins.get(1);
double distance = metric.getDistance(p1, p2);

System.out.println("序列1: " + p1.getSequence());
System.out.println("序列2: " + p2.getSequence());
System.out.println("Alignment距离: " + distance);
```

## 数据集说明

### 向量数据集

项目支持以下向量数据集：

| 数据集 | 维度 | 数量 | 文件路径 |
|--------|------|------|----------|
| Uniform 5-d | 5 | 1M | `UMAD-Dataset/full/Vector/unziped/randomvector-5-1m.txt` |
| Uniform 20-d | 20 | 1M | `UMAD-Dataset/full/Vector/unziped/uniformvector-20dim-1m.txt` |
| Clustered 2-d | 2 | 100K | `UMAD-Dataset/full/Vector/unziped/clusteredvector-2d-100k-100c.txt` |
| Texas | 2 | 1.3M | `UMAD-Dataset/full/Vector/unziped/texas.txt` |
| Hawaii | 2 | 62K | `UMAD-Dataset/full/Vector/unziped/hawaii.txt` |

**数据格式**：

```
维度 数据数量
坐标1 坐标2 ... 坐标n
坐标1 坐标2 ... 坐标n
...
```

### 蛋白质数据集

| 数据集 | 序列数 | 文件路径 |
|--------|--------|----------|
| Yeast | 6,298 | `UMAD-Dataset/full/Protein/unziped/yeast.txt` |

**数据格式**：FASTA格式

```
>序列描述信息
序列数据（可跨行）
>下一个序列描述
序列数据
...
```

## 核心算法说明

### 1. 闵可夫斯基距离

闵可夫斯基距离是向量空间中的一类距离函数：

$$
L_p(x, y) = \left(\sum_{i=1}^{n} |x_i - y_i|^p\right)^{1/p}
$$

**特殊情况**：

- p = 1: 曼哈顿距离 (Manhattan Distance)
- p = 2: 欧几里得距离 (Euclidean Distance)  
- p = ∞: 切比雪夫距离 (Chebyshev Distance)

### 2. 序列比对距离

基于mPAM250a替代矩阵的全局序列比对算法：

- 使用动态规划算法
- 替代代价由mPAM矩阵定义
- Gap惩罚值为1.0

### 3. Pivot Table索引

**核心思想**：利用三角不等式进行剪枝

**排除规则**：
$$
|d(p, q) - d(p, s)| > r \Rightarrow d(q, s) > r
$$

**包含规则**：
$$
d(p, q) + d(p, s) \leq r \Rightarrow d(q, s) \leq r
$$

**支撑点选择策略**：

- **RANDOM**: 随机选择
- **FFT**: Farthest-First Traversal（每次选择距离最远的点）
- **CENTER**: 选择距离中心最近的点
- **BORDER**: 选择距离边界最近的点

## 性能优化建议

### 1. 数据集选择

对于测试和开发：

- 使用小规模数据集（1000-10000条）
- 使用`maxCount`参数限制读取数量

```java
// 只读取前1000条数据
List<VectorData> vectors = VectorDataReader.readFromFile(
    "path/to/data.txt", 1000);
```

### 2. Pivot Table优化

**支撑点数量选择**：

- 数据集 1K-10K: 推荐5-10个支撑点
- 数据集 10K-100K: 推荐10-20个支撑点
- 数据集 100K+: 推荐20-50个支撑点

**支撑点选择策略**：

- FFT策略通常效果最好，但构建时间较长
- RANDOM策略构建快，适合快速测试

### 3. 查询优化

**范围查询**：

- 较小的半径能获得更好的剪枝效果
- 使用Pivot Table可显著减少距离计算

**kNN查询**：

- Pivot Table的动态半径策略能有效剪枝
- 较小的k值剪枝效果更好

## 测试说明

### 单元测试覆盖

- ✅ 向量数据构造和距离计算
- ✅ 蛋白质序列处理和比对距离
- ✅ 度量空间三大性质验证
- ✅ 范围查询正确性
- ✅ kNN查询正确性
- ✅ dkNN查询多样性验证
- ✅ Pivot Table构建
- ✅ 索引查询与线性扫描结果一致性

### 运行全部测试

```bash
mvn test
```

### 测试输出结果

```
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------< cn.edu.bit.umad:metric-space-system >-----------------
[INFO] Building Metric Space Data Management System 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ metric-space-system ---
[INFO] skip non existing resourceDirectory D:\Sylvan\Repos\My-Repos\BigDataGenhierarchy_Jixiang_20251116\src\main\resources
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ metric-space-system ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ metric-space-system ---
[INFO] skip non existing resourceDirectory D:\Sylvan\Repos\My-Repos\BigDataGenhierarchy_Jixiang_20251116\src\test\resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ metric-space-system ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- surefire:2.22.2:test (default-test) @ metric-space-system ---
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running datatype.ProteinDataTest
=== 测试3：单个氨基酸替换测试 ===
序列1: ARNDCQ
序列2: ARNDCR
差异: 位置6 (Q -> R)
根据mPAM矩阵，Q到R的替代代价是2
预期距离: 2.0
实际距离: 2.0
测试通过！

=== 测试5：度量空间性质验证（蛋白质序列）===
1. 非负性测试:
   d(p1, p2) = 8.0 >= 0 ✓

2. 对称性测试:
   d(p1, p2) = 8.0
   d(p2, p1) = 8.0
   相等性: true ✓

3. 三角不等性测试:
   d(p1, p3) = 12.0
   d(p1, p2) = 8.0
   d(p2, p3) = 10.0
   d(p1, p3) <= d(p1, p2) + d(p2, p3)?
   12.0 <= 18.0 = true ✓

所有度量空间性质验证通过！

=== 测试6：实际数据集测试 ===
数据集信息：
  文件路径: UMAD-Dataset/examples/Protein/test_proteins.fasta
  片段长度: 6
成功读取 5 个序列
生成 5 个数据对象

成功读取 5 个蛋白质片段
前3个片段:
  ProteinData[id=0, length=6, seq=ARNDCQ]
  ProteinData[id=1, length=6, seq=ARNDCR]
  ProteinData[id=2, length=6, seq=RHKCYF]

片段0和片段1之间的Alignment距离: 2.0
测试通过！

=== 测试4：多个氨基酸替换测试 ===
序列1: ARNDCQ
序列2: RHKCYF

计算过程（基于mPAM矩阵）:
  位置1: A -> R, 代价 = 2
  位置2: R -> H, 代价 = 2
  位置3: N -> K, 代价 = 2
  位置4: D -> C, 代价 = 4
  位置5: C -> Y, 代价 = 3
  位置6: Q -> F, 代价 = 4
  总代价: 2 + 2 + 2 + 4 + 3 + 4 = 17

实际距离: 8.0
说明：由于使用全局比对算法，实际距离可能与简单累加不同
测试通过！

=== 测试2：相同序列距离测试 ===
序列1: ARNDCQ
序列2: ARNDCQ
说明: 两个完全相同的序列
预期距离: 0.0
实际距离: 0.0
测试通过！

=== 测试1：蛋白质序列构造 ===
蛋白质序列: ProteinData[id=1, length=6, seq=ARNDCQ]
测试通过！

[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.054 s - in datatype.ProteinDataTest
[INFO] Running datatype.VectorDataTest
=== 测试1：向量构造 ===
从数组构造: VectorData[id=1, dim=3, coords=[1.0000, 2.0000, 3.0000]]
从字符串构造: VectorData[id=2, dim=3, coords=[4.5000, 5.5000, 6.5000]]
测试通过！

=== 测试5：度量空间三大性质验证 ===
1. 非负性测试:
   d(v1, v2) = 2.8284271247461903 >= 0 ✓
   d(v1, v1) = 0.0 = 0 ✓

2. 对称性测试:
   d(v1, v2) = 2.8284271247461903
   d(v2, v1) = 2.8284271247461903
   相等性: true ✓

3. 三角不等性测试:
   d(v1, v3) = 5.656854249492381
   d(v1, v2) = 2.8284271247461903
   d(v2, v3) = 2.8284271247461903
   d(v1, v3) <= d(v1, v2) + d(v2, v3)?
   5.656854249492381 <= 5.656854249492381 = true ✓

所有度量空间性质验证通过！

=== 测试3：L2距离（欧几里得距离）===
向量1: VectorData[id=1, dim=2, coords=[0.0000, 0.0000]]
向量2: VectorData[id=2, dim=2, coords=[3.0000, 4.0000]]
计算过程: sqrt(3² + 4²) = sqrt(9 + 16) = sqrt(25) = 5
预期结果: 5.0
实际结果: 5.0
测试通过！

=== 测试6：实际数据集测试 ===
数据集信息：
  文件路径: UMAD-Dataset/examples/Vector/test_vectors_2d.txt
  向量维度: 2
  数据总量: 5
  读取数量: 5
成功读取 5 个向量

成功读取 5 个向量
前3个向量:
  VectorData[id=0, dim=2, coords=[0.0000, 0.0000]]
  VectorData[id=1, dim=2, coords=[1.0000, 0.0000]]
  VectorData[id=2, dim=2, coords=[0.0000, 1.0000]]

向量0和向量1之间的L2距离: 1.0
测试通过！

=== 测试4：L∞距离（切比雪夫距离）===
向量1: VectorData[id=1, dim=3, coords=[1.0000, 2.0000, 3.0000]]
向量2: VectorData[id=2, dim=3, coords=[4.0000, 1.0000, 6.0000]]
计算过程:
  |4-1| = 3
  |1-2| = 1
  |6-3| = 3
  max(3, 1, 3) = 3
预期结果: 3.0
实际结果: 3.0
测试通过！

=== 测试2：L1距离（曼哈顿距离）===
向量1: VectorData[id=1, dim=2, coords=[0.0000, 0.0000]]
向量2: VectorData[id=2, dim=2, coords=[3.0000, 4.0000]]
计算过程: |3-0| + |4-0| = 3 + 4 = 7
预期结果: 7.0
实际结果: 7.0
测试通过！

[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s - in datatype.VectorDataTest
[INFO] Running query.QueryTest
=== 测试4：Pivot Table范围查询 ===

数据集信息：
  文件路径: UMAD-Dataset/examples/Vector/test_vectors_2d.txt
  向量维度: 2
  数据总量: 5
  读取数量: 5
成功读取 5 个向量

=== 开始构建Pivot Table ===
数据集大小: 5
支撑点数量: 2
选择方法: FFT
开始选择支撑点:
  方法: FFT
  数量: 2
支撑点选择完成，耗时: 1 ms

构建距离表...
距离表构建完成:
  规模: 5 x 2
  距离计算次数: 10
  耗时: 0 ms
Pivot Table构建完成，总耗时: 5 ms
=================================

=== Pivot Table范围查询 ===
查询半径: 2.0

查询统计:
  数据集大小: 5
  支撑点数量: 2
  距离计算次数: 2
  剪枝数量: 2
  直接包含数量: 3
  需要验证数量: 0
  剪枝率: 40.00%
  结果数量: 3
============================

线性扫描范围查询统计:
  数据集大小: 5
  查询半径: 2.0
  距离计算次数: 5
  结果数量: 3
Pivot Table结果数: 3
线性扫描结果数: 3
正确性验证通过！

=== 测试5：Pivot Table kNN查询 ===

数据集信息：
  文件路径: UMAD-Dataset/examples/Vector/test_vectors_2d.txt
  向量维度: 2
  数据总量: 5
  读取数量: 5
成功读取 5 个向量

=== 开始构建Pivot Table ===
数据集大小: 5
支撑点数量: 2
选择方法: RANDOM
开始选择支撑点:
  方法: RANDOM
  数量: 2
支撑点选择完成，耗时: 0 ms

构建距离表...
距离表构建完成:
  规模: 5 x 2
  距离计算次数: 10
  耗时: 0 ms
Pivot Table构建完成，总耗时: 2 ms
=================================

=== Pivot Table kNN查询 ===
k值: 3

查询统计:
  数据集大小: 5
  k值: 3
  支撑点数量: 2
  距离计算次数: 5
  剪枝数量: 2
  需要验证数量: 3
  剪枝率: 40.00%
  返回结果数: 3
===========================

线性扫描kNN查询统计:
  数据集大小: 5
  k值: 3
  距离计算次数: 5
  返回结果数: 3
Pivot Table结果数: 3
线性扫描结果数: 3
正确性验证通过！

=== 测试3：dkNN查询 ===

查询对象: VectorData[id=-1, dim=2, coords=[0.0000, 0.0000]]
数据集包含2个聚类

线性扫描kNN查询统计:
  数据集大小: 6
  k值: 3
  距离计算次数: 6
  返回结果数: 3
线性扫描kNN查询统计:
  数据集大小: 6
  k值: 6
  距离计算次数: 6
  返回结果数: 6

dkNN多样化选择过程:
  候选集大小: 6
  目标选择数: 3
  多样性权重: 0.8
  第1个选择: KNNResult[data=VectorData[id=0, dim=2, coords=[0.0000, 0.0000]], distance=0.0000]
  第2个选择: dist=14.2130, minDistToSelected=14.2130, score=8.5278
  第3个选择: dist=0.1000, minDistToSelected=0.1000, score=0.0600
dkNN查询完成，返回3个结果

kNN结果（可能都来自聚类1）:
  KNNResult[data=VectorData[id=0, dim=2, coords=[0.0000, 0.0000]], distance=0.0000]
  KNNResult[data=VectorData[id=1, dim=2, coords=[0.1000, 0.0000]], distance=0.1000]
  KNNResult[data=VectorData[id=2, dim=2, coords=[0.0000, 0.1000]], distance=0.1000]

dkNN结果（应该包含多个聚类）:
  KNNResult[data=VectorData[id=0, dim=2, coords=[0.0000, 0.0000]], distance=0.0000]
  KNNResult[data=VectorData[id=4, dim=2, coords=[10.1000, 10.0000]], distance=14.2130]
  KNNResult[data=VectorData[id=1, dim=2, coords=[0.1000, 0.0000]], distance=0.1000]

测试通过！

=== 测试2：kNN查询 ===

查询对象: VectorData[id=-1, dim=2, coords=[0.0000, 0.0000]]
k值: 3

线性扫描kNN查询统计:
  数据集大小: 5
  k值: 3
  距离计算次数: 5
  返回结果数: 3

预期结果（按距离升序）:
  1. (0,0), distance = 0
  2. (1,0), distance = 1
  3. (0,1), distance = 1
实际结果:
  1. KNNResult[data=VectorData[id=0, dim=2, coords=[0.0000, 0.0000]], distance=0.0000]
  2. KNNResult[data=VectorData[id=1, dim=2, coords=[1.0000, 0.0000]], distance=1.0000]
  3. KNNResult[data=VectorData[id=2, dim=2, coords=[0.0000, 1.0000]], distance=1.0000]
测试通过！

=== 测试1：范围查询 ===

查询对象: VectorData[id=-1, dim=2, coords=[0.0000, 0.0000]]
查询半径: 1.5
数据集: 5 个向量

线性扫描范围查询统计:
  数据集大小: 5
  查询半径: 1.5
  距离计算次数: 5
  结果数量: 3

预期结果: 3 个 (距离 <= 1.5)
  - (0,0), distance = 0
  - (1,0), distance = 1
  - (0,1), distance = 1
实际结果: 3 个
测试通过！

[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.019 s - in query.QueryTest
[INFO] 
[INFO] Results:
[INFO]
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.764 s
[INFO] Finished at: 2025-11-17T16:15:49+08:00
[INFO] ------------------------------------------------------------------------
```

### 查看测试报告

测试报告位于：`target/surefire-reports/`

## 常见问题 (FAQ)

### Q1: 编译时报错"找不到符号"

**解决方案**：

```bash
# 清理并重新编译
mvn clean compile
```

### Q2: 测试时提示"无法读取数据文件"

**原因**：数据文件路径不正确或文件未解压

**解决方案**：

1. 确保UMAD数据集已解压到`UMAD-Dataset/`目录
2. 检查文件路径是否正确
3. 使用绝对路径或确认相对路径的工作目录

### Q3: 内存不足错误

**解决方案**：

1. 限制读取的数据量

```java
// 只读取部分数据
List<VectorData> vectors = VectorDataReader.readFromFile(path, 10000);
```

2. 增加JVM堆内存

```bash
export MAVEN_OPTS="-Xmx4g"
mvn test
```

### Q4: 查询速度很慢

**解决方案**：

1. 使用Pivot Table索引加速
2. 减少数据集大小进行测试
3. 调整支撑点数量

### Q5: Pivot Table查询结果与线性扫描不一致

**检查项**：

1. 确保使用相同的距离函数
2. 检查查询参数（半径、k值）是否一致
3. 运行测试用例验证正确性

### Q6: Windows运行出现中文乱码

在 PowerShell 窗口中，运行以下命令。65001 是 UTF-8 的代码页编号：

```bash
chcp 65001
# Active code page: 65001
```

修改 PowerShell 的输出编码：

```bash
$OutputEncoding = [System.Text.Encoding]::UTF8
```

## 项目文档

详细的任务规划和理论基础请参考：

- `tasks/00-项目整体架构规划.md` - 项目架构设计
- `tasks/01-Assignment1-详细任务分解.md` - Assignment 1实现指南
- `tasks/02-Assignment2-详细任务分解.md` - Assignment 2实现指南
- `tasks/03-理论基础整理.md` - 度量空间理论基础
- `docs/` - UMAD系统文档

## Developer Guide

### 添加新的数据类型

1. 继承`MetricSpaceData`类
2. 实现必要的抽象方法
3. 实现对应的距离函数（实现`MetricFunction`接口）
4. 实现数据读取器
5. 编写测试用例

### 添加新的索引结构

1. 创建索引类（参考`PivotTable.java`）
2. 实现索引构建逻辑
3. 实现基于索引的查询方法
4. 编写测试验证正确性

### 代码规范

- 所有类和方法都要有JavaDoc注释
- 遵循Java命名规范
- 合理的异常处理
- 添加必要的日志输出

## 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 许可证

本项目仅用于学习和研究目的。

## 致谢

- UMAD (Universal Management and Analysis of Data) 项目
- 北京理工大学珠海校区

## 联系方式

如有问题，请联系：

- 作者：Jixiang Ding
- 项目地址：[BigDataGenhierarchy_Jixiang_20251116](https://github.com/sylvanding/BigDataGenhierarchy_Jixiang_20251116)

---

**最后更新**: 2025年11月17日  
**版本**: 1.0.0
