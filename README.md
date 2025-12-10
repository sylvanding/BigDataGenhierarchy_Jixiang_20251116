# 度量空间数据管理与分析系统

## 项目简介

基于Java的通用度量空间数据管理与分析系统，实现了度量空间数据的存储、距离计算、相似性查询和多种索引结构。

## 功能特性

### Assignment 1 - 基础数据处理

- ✅ 度量空间数据抽象框架
- ✅ 向量数据类型（支持任意维度）
- ✅ 闵可夫斯基距离（L1, L2, L∞, Lp）
- ✅ 蛋白质序列数据类型
- ✅ 基于mPAM矩阵的序列比对距离
- ✅ UMAD数据集读取功能

### Assignment 2 - 查询算法与Pivot Table索引

- ✅ 线性扫描查询算法
  - 范围查询 (Range Query)
  - k近邻查询 (kNN Query)
  - 多样化k近邻查询 (dkNN Query)
- ✅ Pivot Table索引
  - 多种支撑点选择策略（Random, FFT, Center, Border）
  - 基于三角不等式的剪枝优化
  - 范围查询和kNN查询加速

### Assignment 3 - 树状索引结构

- ✅ GH树（Generalized Hyperplane Tree）
  - 超平面划分策略
  - 范围查询与kNN查询
- ✅ VP树（Vantage Point Tree）
  - 球形划分策略
  - 范围查询与kNN查询
- ✅ 性能对比分析框架

## 技术栈

- **编程语言**: Java 12+
- **构建工具**: Maven 3.x
- **测试框架**: JUnit 4.13.2
- **开发环境**: VS Code (Recommended)

## 项目结构

```
BigDataGenhierarchy_Jixiang_20251116/
├── src/main/java/
│   ├── core/                      # 核心抽象类
│   ├── datatype/                  # 数据类型实现（vector, protein）
│   ├── io/                        # 数据读取模块
│   ├── query/                     # 查询算法
│   ├── index/                     # 索引结构
│   │   ├── pivottable/            # Pivot Table索引
│   │   └── tree/                  # 树状索引（GHTree, VPTree）
│   └── examples/                  # 演示和分析程序
│       ├── assignment1_2/         # Assignment 1-2 演示
│       └── assignment3/           # Assignment 3 演示
├── src/test/java/                 # 测试代码
├── UMAD-Dataset/                  # 数据集目录
├── QUICKSTART/                    # 快速开始指南（包括终端输出记录）
│   ├── QUICKSTART-Assignment1-2.md
│   └── QUICKSTART-Assignment3.md
├── tasks/                         # 任务规划文档
├── Assignment1/, Assignment2/, Assignment3/  # 实验报告
├── pom.xml
└── README.md
```

## 快速开始

### 1. 环境要求

- **JDK**: 12 或更高版本
- **Maven**: 3.6 或更高版本

```bash
# 验证环境：Windows PowerShell

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

### 2. 编译项目

```bash
mvn clean compile
```

### 3. 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=VectorDataTest
mvn test -Dtest=QueryTest
```

### 4. 运行演示程序

**Assignment 1-2 演示**:

```bash
# Windows PowerShell
mvn exec:java "-Dexec.mainClass=examples.assignment1_2.Demo"
mvn exec:java "-Dexec.mainClass=examples.assignment1_2.PerformanceAnalysis"

# Linux/Mac
mvn exec:java -Dexec.mainClass=examples.assignment1_2.Demo
mvn exec:java -Dexec.mainClass=examples.assignment1_2.PerformanceAnalysis
```

**Assignment 3 演示**:

```bash
# Windows PowerShell
mvn exec:java "-Dexec.mainClass=examples.assignment3.TreeDemo"
mvn exec:java "-Dexec.mainClass=examples.assignment3.TreePerformanceAnalysis"

# Linux/Mac
mvn exec:java -Dexec.mainClass=examples.assignment3.TreeDemo
mvn exec:java -Dexec.mainClass=examples.assignment3.TreePerformanceAnalysis
```

### 5. 查看详细输出

所有演示程序的详细输出和测试结果保存在 `QUICKSTART/` 目录：

- `QUICKSTART-Assignment1-2.md` - Assignment 1-2 的运行结果和说明
- `QUICKSTART-Assignment3.md` - Assignment 3 的运行结果和说明

## 使用示例

### 示例1：向量数据与距离计算

```java
import datatype.vector.VectorData;
import datatype.vector.MinkowskiDistance;
import io.VectorDataReader;

// 读取向量数据
List<VectorData> vectors = VectorDataReader.readFromFile(
    "UMAD-Dataset/full/Vector/unziped/uniformvector-20dim-1m.txt", 1000);

// 计算L2距离
MinkowskiDistance metric = MinkowskiDistance.L2;
double distance = metric.getDistance(vectors.get(0), vectors.get(1));
```

### 示例2：范围查询

```java
import query.*;
import index.pivottable.*;

// 线性扫描查询
VectorData queryObject = vectors.get(0);
List<MetricSpaceData> results = LinearScanRangeQuery.execute(
    vectors, new RangeQuery(queryObject, 0.1), MinkowskiDistance.L2);

// 使用Pivot Table索引
PivotTable pivotTable = new PivotTable(
    vectors, 20, MinkowskiDistance.L2, PivotSelectionMethod.FFT);
List<MetricSpaceData> indexResults = PivotTableRangeQuery.execute(
    pivotTable, new RangeQuery(queryObject, 0.1));
```

### 示例3：使用GH树索引

```java
import index.tree.ghtree.GHTree;
import index.tree.common.TreeConfig;

// 配置树参数
TreeConfig config = new TreeConfig.Builder()
    .maxLeafSize(50)
    .minTreeHeight(3)
    .pivotStrategy(TreeConfig.PivotSelectionStrategy.RANDOM)
    .build();

// 构建GH树
GHTree ghTree = new GHTree(config);
ghTree.buildIndex(vectors, MinkowskiDistance.L2);

// 执行查询
List<MetricSpaceData> results = ghTree.rangeQuery(queryObject, 0.1);
List<MetricSpaceData> knnResults = ghTree.knnQuery(queryObject, 10);
```

### 示例4：使用VP树索引

```java
import index.tree.vptree.VPTree;

// 构建VP树
VPTree vpTree = new VPTree(config);
vpTree.buildIndex(vectors, MinkowskiDistance.L2);

// 执行查询
List<MetricSpaceData> results = vpTree.rangeQuery(queryObject, 0.1);
List<MetricSpaceData> knnResults = vpTree.knnQuery(queryObject, 10);
```

## 数据集说明

### 向量数据集

| 数据集 | 维度 | 数量 | 文件路径 |
|--------|------|------|----------|
| Uniform 5-d | 5 | 1M | `UMAD-Dataset/full/Vector/unziped/randomvector-5-1m.txt` |
| Uniform 20-d | 20 | 1M | `UMAD-Dataset/full/Vector/unziped/uniformvector-20dim-1m.txt` |
| Clustered 2-d | 2 | 100K | `UMAD-Dataset/full/Vector/unziped/clusteredvector-2d-100k-100c.txt` |
| Texas | 2 | 1.3M | `UMAD-Dataset/full/Vector/unziped/texas.txt` |
| Hawaii | 2 | 62K | `UMAD-Dataset/full/Vector/unziped/hawaii.txt` |

### 蛋白质数据集

| 数据集 | 序列数 | 文件路径 |
|--------|--------|----------|
| Yeast | 6,298 | `UMAD-Dataset/full/Protein/unziped/yeast.txt` |

## 核心算法

### 闵可夫斯基距离

$$L_p(x, y) = \left(\sum_{i=1}^{n} |x_i - y_i|^p\right)^{1/p}$$

- p = 1: 曼哈顿距离
- p = 2: 欧几里得距离
- p = ∞: 切比雪夫距离

### Pivot Table索引

利用三角不等式进行剪枝：

- **排除规则**: $|d(p, q) - d(p, s)| > r \Rightarrow d(q, s) > r$
- **包含规则**: $d(p, q) + d(p, s) \leq r \Rightarrow d(q, s) \leq r$

### GH树

- **划分方式**: 使用两个pivot点通过超平面划分空间
- **划分规则**: $d(x, p_1) < d(x, p_2)$ → 左子树，否则 → 右子树
- **剪枝规则**: 基于查询对象到两个pivot的距离差

### VP树

- **划分方式**: 使用一个pivot点通过球形划分空间
- **划分规则**: 按到pivot的距离排序，中位数分割
- **剪枝规则**: 基于距离范围 $[L, U]$ 进行剪枝

## 性能优化建议

### 数据集选择

对于测试和开发，使用 `maxCount` 参数限制读取数量：

```java
// 只读取前1000条数据
List<VectorData> vectors = VectorDataReader.readFromFile("path/to/data.txt", 1000);
```

### Pivot Table优化

- **支撑点数量**: 数据集1K-10K推荐5-10个，10K-100K推荐10-20个
- **选择策略**: FFT策略效果好但构建慢，RANDOM策略适合快速测试

### 树索引优化

- **树高控制**: 通过 `minTreeHeight` 参数确保树高至少为3层
- **叶子容量**: 通过 `maxLeafSize` 参数控制叶子节点大小
- **Pivot策略**: 选择合适的pivot选择策略以提高查询效率

## 测试覆盖

- ✅ 向量数据构造和距离计算
- ✅ 蛋白质序列处理和比对距离
- ✅ 度量空间三大性质验证
- ✅ 范围查询、kNN查询、dkNN查询正确性
- ✅ Pivot Table索引构建与查询
- ✅ GH树和VP树构建与查询
- ✅ 索引查询与线性扫描结果一致性

## 常见问题

### Q1: Windows中文乱码

```bash
chcp 65001
$OutputEncoding = [System.Text.Encoding]::UTF8
```

### Q2: 内存不足

```bash
# 增加JVM堆内存
export MAVEN_OPTS="-Xmx4g"
mvn test
```

### Q3: 查询速度慢

1. 使用索引结构加速（Pivot Table / GH Tree / VP Tree）
2. 减少数据集大小进行测试
3. 调整索引参数（pivot数量、树高、叶子容量等）

## 项目文档

- `QUICKSTART/` - 快速开始指南和运行输出
- `tasks/Assignment1-2/` - Assignment 1-2 任务规划
- `tasks/Assignment3/` - Assignment 3 任务规划
- `Assignment1/`, `Assignment2/`, `Assignment3/` - 实验报告

## 联系方式

- **作者**: Jixiang Ding
- **项目地址**: [BigDataGenhierarchy_Jixiang_20251116](https://github.com/sylvanding/BigDataGenhierarchy_Jixiang_20251116)

---

**最后更新**: 2025年12月10日  
**版本**: 2.0.0
