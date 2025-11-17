# 快速开始指南

本指南帮助你快速运行和测试度量空间数据管理与分析系统。

## 快速测试步骤

### 第一步：克隆并编译项目

```bash
git clone --depth 1 -b main https://github.com/sylvanding/BigDataGenhierarchy_Jixiang_20251116

cd BigDataGenhierarchy_Jixiang_20251116
mvn clean compile
```

### 第二步：运行测试

#### 测试向量数据处理

```bash
mvn test -Dtest=VectorDataTest
```

**你会看到**：

- ✅ 向量构造测试
- ✅ L1、L2、L∞距离计算
- ✅ 度量空间三大性质验证
- ✅ 实际数据集测试

**测试输出**：

```
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
```

#### 测试蛋白质序列处理

```bash
mvn test -Dtest=ProteinDataTest
```

**你会看到**：

- ✅ 蛋白质序列构造测试
- ✅ 序列比对距离计算
- ✅ mPAM矩阵应用验证
- ✅ 实际数据集测试

**测试输出**：

```
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
```

#### 测试查询功能

```bash
mvn test -Dtest=QueryTest
```

**你会看到**：

- ✅ 范围查询
- ✅ kNN查询
- ✅ dkNN查询（多样化）
- ✅ Pivot Table索引查询
- ✅ 索引与线性扫描结果一致性验证

**测试输出**：

```
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
支撑点选择完成，耗时: 0 ms

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
Pivot Table构建完成，总耗时: 0 ms
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
```

#### 运行所有测试

```bash
mvn test
```

**测试输出**：

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

### 第三步：运行演示程序

```bash
# Windows PowerShell
mvn exec:java "-Dexec.mainClass=Demo"

# Linux/Mac
mvn exec:java -Dexec.mainClass=Demo
```

**演示内容**：

1. 向量数据读取和距离计算
2. 蛋白质序列处理和比对
3. 相似性查询（范围、kNN、dkNN）
4. Pivot Table索引构建和查询

**演示输出**：

```
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------< cn.edu.bit.umad:metric-space-system >-----------------
[INFO] Building Metric Space Data Management System 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- exec:3.1.0:java (default-cli) @ metric-space-system ---
========================================
  度量空间数据管理与分析系统演示
========================================

=== 演示1：向量数据处理 ===

1.1 读取向量数据...
数据集信息：
  文件路径: UMAD-Dataset/examples/Vector/test_vectors_2d.txt
  向量维度: 2
  数据总量: 5
  读取数量: 5
成功读取 5 个向量

前3个向量:
  VectorData[id=0, dim=2, coords=[0.0000, 0.0000]]
  VectorData[id=1, dim=2, coords=[1.0000, 0.0000]]
  VectorData[id=2, dim=2, coords=[0.0000, 1.0000]]

1.2 计算距离...
向量1: VectorData[id=0, dim=2, coords=[0.0000, 0.0000]]
向量2: VectorData[id=1, dim=2, coords=[1.0000, 0.0000]]
L1距离: 1.0000
L2距离: 1.0000
L∞距离: 1.0000

=== 演示2：蛋白质序列处理 ===

2.1 读取蛋白质序列（6-mers）...
数据集信息：
  文件路径: UMAD-Dataset/examples/Protein/test_proteins.fasta
  片段长度: 6
成功读取 5 个序列
生成 5 个数据对象

前3个序列片段:
  ProteinData[id=0, length=6, seq=ARNDCQ]
  ProteinData[id=1, length=6, seq=ARNDCR]
  ProteinData[id=2, length=6, seq=RHKCYF]

2.2 计算序列比对距离...
序列1: ARNDCQ
序列2: ARNDCR
Alignment距离: 2.0000

=== 演示3：相似性查询 ===

数据集信息：
  文件路径: UMAD-Dataset/examples/Vector/test_vectors_2d.txt
  向量维度: 2
  数据总量: 5
  读取数量: 5
成功读取 5 个向量

3.1 范围查询 (radius=2.0)
线性扫描范围查询统计:
  数据集大小: 5
  查询半径: 2.0
  距离计算次数: 5
  结果数量: 3
找到 3 个结果

3.2 kNN查询 (k=3)
线性扫描kNN查询统计:
  数据集大小: 5
  k值: 3
  距离计算次数: 5
  返回结果数: 3
Top-3 最近邻:
  1. distance = 0.0000
  2. distance = 1.0000
  3. distance = 1.0000

=== 演示4：Pivot Table索引 ===

数据集信息：
  文件路径: UMAD-Dataset/examples/Vector/test_vectors_2d.txt
  向量维度: 2
  数据总量: 5
  读取数量: 5
成功读取 5 个向量

4.1 构建Pivot Table索引...
=== 开始构建Pivot Table ===
数据集大小: 5
支撑点数量: 2
选择方法: FFT
开始选择支撑点:
  方法: FFT
  数量: 2
支撑点选择完成，耗时: 0 ms

构建距离表...
距离表构建完成:
  规模: 5 x 2
  距离计算次数: 10
  耗时: 0 ms
Pivot Table构建完成，总耗时: 2 ms
=================================


4.2 使用索引进行范围查询...
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

查询完成！


========================================
  所有演示完成！
========================================
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.479 s
[INFO] Finished at: 2025-11-17T20:50:31+08:00
[INFO] ------------------------------------------------------------------------
```

### 第四步：运行性能分析

**运行完整的性能分析实验**：

```bash
# Windows PowerShell
mvn exec:java "-Dexec.mainClass=PerformanceAnalysis"

# Linux/Mac
mvn exec:java -Dexec.mainClass=PerformanceAnalysis
```

**性能分析内容**：

1. **实验1：支撑点数量对范围查询性能的影响**
   - 测试不同支撑点数量（5, 10, 15, 20, 25, 30）
   - 在不同查询半径下的性能表现
   - 输出查询时间、距离计算次数、剪枝率等指标

2. **实验2：支撑点数量对kNN查询性能的影响**
   - 测试不同k值（5, 10, 20, 50）
   - 分析支撑点数量与剪枝效果的关系

3. **实验3：支撑点选择策略对比**
   - 对比RANDOM和FFT两种选择策略
   - 测量构建时间和查询性能

4. **实验4-5：线性扫描 vs Pivot Table性能对比**
   - 范围查询加速比分析
   - kNN查询加速比分析
   - 不同数据集规模下的性能表现

**输出示例**：

```
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------< cn.edu.bit.umad:metric-space-system >-----------------
[INFO] Building Metric Space Data Management System 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- exec:3.1.0:java (default-cli) @ metric-space-system ---
====================================================
  度量空间索引性能分析与探索
====================================================

正在加载数据集...
数据集信息：
  文件路径: UMAD-Dataset/full/Vector/unziped/uniformvector-20dim-1m.txt
  向量维度: 20
  数据总量: 1000000
  读取数量: 10
成功读取 10 个向量

数据集路径: UMAD-Dataset/full/Vector/unziped/uniformvector-20dim-1m.txt
数据加载成功！


============================================================
实验1: 支撑点数量对范围查询性能的影响
============================================================

【实验设置】
  数据集大小: 5000 个向量
  支撑点数量: [5, 10, 15, 20, 25, 30]
  查询半径: [0.05, 0.10, 0.15, 0.20]
  选择策略: FFT
  查询次数: 10 次（取平均值）

【实验结果表格】
支撑点数       查询半径       平均查询时间(ms)      平均距离计算          平均剪枝数           平均剪枝率(%)        结果数
--------------------------------------------------------------------------------------------------------------
5          0.05       0.456           6.2             4998.7          99.97           1.0       
5          0.10       0.254           10.6            4994.3          99.89           1.0
5          0.15       0.135           35.4            4969.5          99.39           1.0
5          0.20       0.130           118.4           4886.5          97.73           1.0       
10         0.05       0.124           10.9            4999.0          99.98           1.0       
10         0.10       0.117           10.9            4999.0          99.98           1.0       
10         0.15       0.067           10.9            4999.0          99.98           1.0       
10         0.20       0.076           12.6            4997.3          99.95           1.0       
15         0.05       0.029           15.9            4999.0          99.98           1.0       
15         0.10       0.041           15.9            4999.0          99.98           1.0       
15         0.15       0.053           15.9            4999.0          99.98           1.0       
15         0.20       0.063           16.0            4998.9          99.98           1.0       
20         0.05       0.027           20.9            4999.0          99.98           1.0       
20         0.10       0.039           20.9            4999.0          99.98           1.0       
20         0.15       0.053           20.9            4999.0          99.98           1.0       
20         0.20       0.064           20.9            4999.0          99.98           1.0       
25         0.05       0.026           25.9            4999.0          99.98           1.0       
25         0.10       0.044           25.9            4999.0          99.98           1.0       
25         0.15       0.051           25.9            4999.0          99.98           1.0       
25         0.20       0.058           25.9            4999.0          99.98           1.0       
30         0.05       0.033           30.9            4999.0          99.98           1.0       
30         0.10       0.042           30.9            4999.0          99.98           1.0       
30         0.15       0.053           30.9            4999.0          99.98           1.0       
30         0.20       0.078           30.9            4999.0          99.98           1.0       


============================================================
实验2: 支撑点数量对kNN查询性能的影响
============================================================

【实验设置】
  数据集大小: 5000 个向量
  支撑点数量: [5, 10, 15, 20, 25, 30]
  k值: [5, 10, 20, 50]
  选择策略: FFT
  查询次数: 10 次（取平均值）

【实验结果表格】
支撑点数       k值         平均查询时间(ms)      平均距离计算          平均剪枝数           平均剪枝率(%)
-----------------------------------------------------------------------------------------------
5          5          1.353           4498.7          506.3           10.13
5          10         0.422           4506.1          498.9           9.98
5          20         0.349           4513.9          491.1           9.82
5          50         0.705           4527.9          477.1           9.54
10         5          0.574           4480.1          529.9           10.60
10         10         0.482           4496.5          513.5           10.27
10         20         0.609           4508.8          501.2           10.02
10         50         0.850           4526.6          483.4           9.67
15         5          0.214           4425.0          590.0           11.80
15         10         0.317           4470.8          544.2           10.88
15         20         0.179           4497.7          517.3           10.35
15         50         0.191           4523.1          491.9           9.84
20         5          0.274           4410.3          609.7           12.19
20         10         0.215           4463.6          556.4           11.13
20         20         0.181           4496.5          523.5           10.47
20         50         0.375           4523.8          496.2           9.92
25         5          0.207           4394.0          631.0           12.62
25         10         0.257           4456.9          568.1           11.36
25         20         0.187           4493.8          531.2           10.62
25         50         0.218           4523.1          501.9           10.04
30         5          0.352           4389.1          640.9           12.82
30         10         0.206           4454.5          575.5           11.51
30         20         0.211           4492.8          537.2           10.74
30         50         0.252           4522.9          507.1           10.14


============================================================
实验3: 支撑点选择策略对性能的影响
============================================================

【实验设置】
  数据集大小: 5000 个向量
  支撑点数量: 20
  选择策略: RANDOM, FFT
  查询类型: 范围查询(radius=0.1) 和 kNN查询(k=10)
  查询次数: 10 次（取平均值）

【范围查询性能对比】
选择策略            构建时间(ms)        平均查询时间(ms)      平均距离计算          平均剪枝数           平均剪枝率(%)
---------------------------------------------------------------------------------------------------------
RANDOM          5.877           0.043           20.9            4999.0          99.98
FFT             54.712          0.044           20.9            4999.0          99.98

【kNN查询性能对比】
选择策略            构建时间(ms)        平均查询时间(ms)      平均距离计算          平均剪枝数           平均剪枝率(%)
---------------------------------------------------------------------------------------------------------
RANDOM          4.150           0.666           4501.1          518.9           10.38
FFT             60.750          0.686           4463.6          556.4           11.13

============================================================
实验4: 线性扫描 vs Pivot Table (范围查询)
============================================================

【实验设置】
  数据集大小: [1000, 5000, 10000]
  支撑点数量: 20 (FFT策略)
  查询半径: [0.05, 0.10, 0.15, 0.20]
  查询次数: 10 次（取平均值）

【性能对比表格】
数据集大小        查询半径       线性扫描时间(ms)         索引查询时间(ms)         加速比             剪枝率(%)
---------------------------------------------------------------------------------------------------------
>> 正在加载 1000 个数据点...
>> 正在构建Pivot Table...
1000         0.05       0.329              0.010              34.04          x 99.90
1000         0.10       0.132              0.010              13.83          x 99.90
1000         0.15       0.096              0.009              10.28          x 99.90
1000         0.20       0.097              0.018              5.38           x 99.90
>> 正在加载 5000 个数据点...
>> 正在构建Pivot Table...
5000         0.05       0.767              0.029              26.69          x 99.98
5000         0.10       0.313              0.056              5.57           x 99.98
5000         0.15       0.303              0.053              5.74           x 99.98
5000         0.20       0.330              0.061              5.40           x 99.98
>> 正在加载 10000 个数据点...
>> 正在构建Pivot Table...
10000        0.05       2.616              0.148              17.70          x 99.99
10000        0.10       1.480              0.151              9.82           x 99.99
10000        0.15       1.541              0.184              8.39           x 99.99
10000        0.20       1.149              0.145              7.92           x 99.99


============================================================
实验5: 线性扫描 vs Pivot Table (kNN查询)
============================================================

【实验设置】
  数据集大小: [1000, 5000, 10000]
  支撑点数量: 20 (FFT策略)
  k值: [5, 10, 20, 50]
  查询次数: 10 次（取平均值）

【性能对比表格】
数据集大小        k值         线性扫描时间(ms)         索引查询时间(ms)         加速比             剪枝率(%)
---------------------------------------------------------------------------------------------------------
>> 正在加载 1000 个数据点...
>> 正在构建Pivot Table...
1000         5          0.287              0.048              6.03           x 11.70
1000         10         0.155              0.071              2.18           x 11.28
1000         20         0.177              0.046              3.82           x 10.88
1000         50         0.172              0.083              2.08           x 9.54
>> 正在加载 5000 个数据点...
>> 正在构建Pivot Table...
5000         5          0.565              0.165              3.42           x 12.19
5000         10         0.313              0.204              1.53           x 11.13
5000         20         0.189              0.173              1.09           x 10.47
5000         50         0.240              0.219              1.09           x 9.92
>> 正在加载 10000 个数据点...
>> 正在构建Pivot Table...
10000        5          3.362              1.761              1.91           x 14.62
10000        10         2.408              1.738              1.39           x 12.77
10000        20         2.243              1.606              1.40           x 11.57
10000        50         1.843              1.287              1.43           x 10.60


============================================================
所有性能分析实验完成！
============================================================
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.859 s
[INFO] Finished at: 2025-11-17T23:57:44+08:00
[INFO] ------------------------------------------------------------------------
```

**注意事项**：

1. **数据文件准备**：确保已解压数据集到 `UMAD-Dataset/full/Vector/unziped/` 目录
2. **运行时间**：完整的性能分析需要一定时间，请耐心等待
3. **内存要求**：建议JVM堆内存至少2GB
4. **结果解读**：分析程序会输出详细的性能表格和关键发现，可用于实验报告

如果需要调整实验参数，可以修改 `src/main/java/PerformanceAnalysis.java` 中的配置：

```java
// 实验配置
private static final int[] DATASET_SIZES  = {1000, 5000, 10000};
private static final int[] PIVOT_COUNTS   = {5, 10, 15, 20, 25, 30};
private static final double[] QUERY_RADII = {0.05, 0.1, 0.15, 0.2};
private static final int[] KNN_VALUES     = {5, 10, 20, 50};
private static final int NUM_QUERIES      = 10;                       // 每个配置执行的查询次数
```

## 如果遇到问题

### 问题1：找不到数据文件

**症状**：

```
无法读取测试数据文件
```

**解决方案**：
确保测试数据文件存在：

```bash
# Windows PowerShell
dir UMAD-Dataset\\examples\\Vector\\test_vectors_2d.txt
dir UMAD-Dataset\\examples\\Protein\\test_proteins.fasta

# Linux/Mac
ls UMAD-Dataset/examples/Vector/test_vectors_2d.txt
ls UMAD-Dataset/examples/Protein/test_proteins.fasta
```

### 问题2：编译错误

**症状**：

```
[ERROR] COMPILATION ERROR
```

**解决方案**：

```bash
# 清理并重新编译
mvn clean
mvn compile
```

确保你的JDK版本 >= 12：

```bash
java -version
# 应该显示 java version "12.x.x" 或更高
```

### 问题3：测试跳过

**症状**：

```
警告：无法读取测试数据文件，跳过此测试
```

**说明**：这是正常的！如果某些测试数据文件不存在，测试会跳过而不报错。

**不影响**：核心功能测试仍然会运行。

### 问题4：Windows运行出现中文乱码

**症状**：

```
中文乱码：???
```

**解决方案**：

在 PowerShell 窗口中，运行以下命令。65001 是 UTF-8 的代码页编号：

```bash
chcp 65001
# Active code page: 65001
```

修改 PowerShell 的输出编码：

```bash
$OutputEncoding = [System.Text.Encoding]::UTF8
```

一劳永逸的解决方案是，通过修改 PowerShell 的配置文件 (Profile)，让每次启动 PowerShell 时自动执行上述命令。

```bash
notepad $PROFILE
```

在配置文件中添加命令：

```
chcp 65001 > $null
# 设置 PowerShell 自身的输出编码为 UTF-8
$OutputEncoding = [System.Text.Encoding]::UTF8
# 设置控制台（包括外部命令如 java, git）的输出编码为 UTF-8
[System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

## 使用实际数据集

如果你想使用完整的UMAD数据集：

### 1. 确保数据已解压

```bash
# 检查向量数据
ls UMAD-Dataset/full/Vector/unziped/

# 检查蛋白质数据
ls UMAD-Dataset/full/Protein/unziped/
```

### 2. 修改测试代码

在测试文件中，将数据路径改为完整数据集：

```java
// 使用完整数据集（1000条）
List<VectorData> vectors = VectorDataReader.readFromFile(
    "UMAD-Dataset/full/Vector/unziped/uniformvector-20dim-1m.txt", 1000);
```

### 3. 运行测试

```bash
mvn test -Dtest=VectorDataTest#testRealData
```

## 查看测试结果

### 控制台输出

测试结果会实时显示在控制台，包括：

- 数据读取信息
- 计算过程详情
- 性能统计
- 验证结果

### 测试报告

HTML格式的详细测试报告：

```bash
# 生成测试报告
mvn surefire-report:report

# 报告位置
target/site/surefire-report.html
```

在浏览器中打开 `target/site/surefire-report.html` 查看详细报告。

## 下一步

完成快速测试后，你可以：

1. **查看详细文档**：阅读 `README.md` 了解完整功能
2. **查看任务规划**：阅读 `tasks/` 目录下的详细文档
3. **修改代码**：尝试修改参数，观察结果变化
4. **性能分析**：使用性能分析程序进行深入的性能探索
5. **撰写报告**：基于性能分析结果撰写实验报告

## 核心功能测试清单

完成以下测试，确保系统正常运行：

- [ ] ✅ 向量数据L2距离计算（3-4-5三角形测试）
- [ ] ✅ 蛋白质序列比对距离计算
- [ ] ✅ 度量空间三大性质验证
- [ ] ✅ 范围查询（radius=1.5，预期3个结果）
- [ ] ✅ kNN查询（k=3，按距离升序）
- [ ] ✅ dkNN查询（多样性权重0.8）
- [ ] ✅ Pivot Table构建（2个支撑点，FFT策略）
- [ ] ✅ 索引查询结果与线性扫描一致

## 成功标志

如果你看到以下输出，说明系统运行正常：

```
Tests run: X, Failures: 0, Errors: 0, Skipped: Y

[INFO] BUILD SUCCESS
```

其中：

- `Failures: 0` - 没有测试失败
- `Errors: 0` - 没有错误
- `Skipped: Y` - 跳过的测试（由于数据文件不存在）

## 帮助

如果仍有问题，请查看：

- 完整的 `README.md` 文档
- `tasks/` 目录下的详细任务分解文档
- Java和Maven的官方文档
