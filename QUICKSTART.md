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

#### 测试蛋白质序列处理

```bash
mvn test -Dtest=ProteinDataTest
```

**你会看到**：

- ✅ 蛋白质序列构造测试
- ✅ 序列比对距离计算
- ✅ mPAM矩阵应用验证
- ✅ 实际数据集测试

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

#### 运行所有测试

```bash
mvn test
```

### 第三步：运行演示程序

**方法一：使用配置的默认主类**（推荐）

```bash
mvn exec:java
```

**方法二：指定主类名称**

```bash
mvn exec:java "-Dexec.mainClass=Demo"
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
4. **性能测试**：使用更大的数据集进行性能对比

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
