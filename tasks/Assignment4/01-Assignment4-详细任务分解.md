# Assignment 4 详细任务分解

## 一、任务概览

| 任务 | 分值 | 预计工时 | 优先级 |
|------|------|----------|--------|
| 任务1：实现三种索引 | 30分 | 15-20h | P0 |
| 任务2：正确性验证 | 10分 | 3-5h | P0 |
| 任务3：理论分析 | 30分 | 5-8h | P1 |
| 任务4：实验分析 | 30分 | 8-12h | P1 |
| 实验报告撰写 | - | 5-8h | P1 |

---

## 二、任务1详细分解：实现三种索引（30分）

### 阶段1.1：基础设施准备（2-3h）

#### 1.1.1 扩展TreeConfig

- [ ] 添加numPivots配置项
- [ ] 添加numPartitionsPerPivot配置项
- [ ] 添加PartitionMode枚举
- [ ] 添加getTotalPartitions()计算方法

**文件**：`src/main/java/index/tree/common/TreeConfig.java`

#### 1.1.2 实现MultiPivotSelector

- [ ] 创建MultiPivotSelector类
- [ ] 实现随机选择策略
- [ ] 实现FFT选择策略
- [ ] 实现最大分散度选择策略
- [ ] 添加随机种子支持（确保可重复性）
- [ ] 编写单元测试

**文件**：`src/main/java/index/tree/common/MultiPivotSelector.java`

#### 1.1.3 创建多Pivot内部节点基类

- [ ] 创建MultiPivotInternalNode抽象类
- [ ] 定义公共属性（pivots数组、距离范围等）
- [ ] 定义抽象方法接口

**文件**：`src/main/java/index/tree/MultiPivotInternalNode.java`

---

### 阶段1.2：3-pivot MVPT实现（4-5h）

#### 1.2.1 MVPInternalNode

- [ ] 创建MVPInternalNode类
- [ ] 实现3个pivot存储
- [ ] 实现8个子节点存储
- [ ] 实现划分半径存储
- [ ] 实现距离范围（lowerBound/upperBound）存储
- [ ] 实现getChildIndex()方法
- [ ] 实现shouldVisitChild()剪枝判断
- [ ] 实现isChildFullyContained()包含判断

**文件**：`src/main/java/index/tree/mvptree/MVPInternalNode.java`

```java
// 核心数据结构
class MVPInternalNode {
    MetricSpaceData[] pivots;        // 3个
    TreeNode[] children;              // 8个
    double[] splitRadius;             // 3个
    double[][] lowerBound;            // [8][3]
    double[][] upperBound;            // [8][3]
}
```

#### 1.2.2 MVPTree主类

- [ ] 创建MVPTree类继承TreeIndex
- [ ] 实现buildTree()递归构建
- [ ] 实现rangeQuery()范围查询
- [ ] 实现统计信息收集
- [ ] 实现getIndexName()

**文件**：`src/main/java/index/tree/mvptree/MVPTree.java`

#### 1.2.3 辅助方法

- [ ] computeDistances()：计算数据到所有pivot的距离
- [ ] computeSplitRadius()：计算各pivot的划分半径（中位数）
- [ ] partitionData()：将数据分配到8个子集
- [ ] computeBounds()：计算每个子集的距离范围

#### 1.2.4 MVPT单元测试

- [ ] 创建MVPTreeTest类
- [ ] 测试构建正确性
- [ ] 测试范围查询正确性
- [ ] 测试边界情况

**文件**：`src/test/java/index/tree/mvptree/MVPTreeTest.java`

---

### 阶段1.3：3-pivot CGHT实现（4-5h）

#### 1.3.1 CGHInternalNode

- [ ] 创建CGHInternalNode类
- [ ] 存储3个pivot
- [ ] 存储子节点（4个或8个）
- [ ] 存储距离差范围
- [ ] 实现getChildIndex()（基于距离差符号）
- [ ] 实现shouldVisitChild()剪枝判断

**文件**：`src/main/java/index/tree/cght/CGHInternalNode.java`

```java
// 划分策略：基于距离差
// delta12 = d1 - d2, delta13 = d1 - d3
// 4路划分：
//   子树0: delta12 < 0, delta13 < 0
//   子树1: delta12 >= 0, delta13 < 0
//   子树2: delta12 < 0, delta13 >= 0
//   子树3: delta12 >= 0, delta13 >= 0
```

#### 1.3.2 CGHTree主类

- [ ] 创建CGHTree类继承TreeIndex
- [ ] 实现buildTree()递归构建
- [ ] 实现rangeQuery()范围查询
- [ ] 实现剪枝规则（扩展自GHTree）

**文件**：`src/main/java/index/tree/cght/CGHTree.java`

**剪枝规则**：

```
对于查询(q, r)和子树i：
若 d(q,p1) - d(q,p2) > 2r 且 子树i的delta12 < 0，排除
若 d(q,p2) - d(q,p1) > 2r 且 子树i的delta12 >= 0，排除
类似地检查delta13
```

#### 1.3.3 CGHT单元测试

- [ ] 创建CGHTreeTest类
- [ ] 测试构建正确性
- [ ] 测试范围查询正确性
- [ ] 与线性扫描对比验证

**文件**：`src/test/java/index/tree/cght/CGHTreeTest.java`

---

### 阶段1.4：完全线性划分树实现（4-5h）

#### 1.4.1 LinearPartitionInternalNode

- [ ] 创建LinearPartitionInternalNode类
- [ ] 存储3个pivot
- [ ] 存储8个子节点
- [ ] 存储划分阈值（3个维度的中位数）
- [ ] 存储每个子节点的坐标范围
- [ ] 实现getChildIndex()
- [ ] 实现shouldVisitChild()

**文件**：`src/main/java/index/tree/linearpartition/LinearPartitionInternalNode.java`

```java
// 支撑点空间划分
// 数据坐标: (d1, d2, d3) = (d(x,p1), d(x,p2), d(x,p3))
// 划分阈值: (t1, t2, t3) = 各维度中位数
// 8个区域由(d1>t1, d2>t2, d3>t3)的组合确定
```

#### 1.4.2 LinearPartitionTree主类

- [ ] 创建LinearPartitionTree类继承TreeIndex
- [ ] 实现buildTree()递归构建
- [ ] 实现rangeQuery()范围查询
- [ ] 实现坐标空间剪枝

**文件**：`src/main/java/index/tree/linearpartition/LinearPartitionTree.java`

**剪枝规则**：

```
查询对象坐标: (dq1, dq2, dq3)
查询区域: 以该点为中心，半径r的球
在支撑点空间中，使用切比雪夫距离，查询区域是边长2r的立方体
若子区域与查询立方体不相交，则排除
```

#### 1.4.3 线性划分树单元测试

- [ ] 创建LinearPartitionTreeTest类
- [ ] 测试构建正确性
- [ ] 测试范围查询正确性

**文件**：`src/test/java/index/tree/linearpartition/LinearPartitionTreeTest.java`

---

## 三、任务2详细分解：正确性验证（10分）

### 阶段2.1：准备测试数据（1h）

#### 2.1.1 选择数据集

- [ ] 数据集1：低维向量（2-4维），如clusteredvector-2d
- [ ] 数据集2：蛋白质序列（长度6），如yeast子集
- [ ] 准备小规模测试数据（便于手工验证）

#### 2.1.2 配置参数

- [ ] 设置maxLeafSize确保树高≥2
- [ ] 记录pivot选择的随机种子
- [ ] 准备查询对象集合

### 阶段2.2：正确性测试（2-3h）

#### 2.2.1 创建正确性测试类

**文件**：`src/test/java/index/tree/MultiPivotCorrectnessTest.java`

- [ ] 测试MVPT查询结果与线性扫描一致
- [ ] 测试CGHT查询结果与线性扫描一致
- [ ] 测试LinearPartitionTree查询结果与线性扫描一致
- [ ] 测试三种索引结果相互一致

#### 2.2.2 构建过程可视化

- [ ] 输出树结构信息
- [ ] 输出pivot选择结果
- [ ] 输出数据划分情况
- [ ] 输出距离范围信息

#### 2.2.3 查询过程可视化

- [ ] 输出查询对象信息
- [ ] 输出访问的节点路径
- [ ] 输出剪枝决策
- [ ] 输出最终查询结果

### 阶段2.3：结果记录（1h）

- [ ] 截图/记录测试输出
- [ ] 整理到实验报告中
- [ ] 说明验证方法和结论

---

## 四、任务3详细分解：理论分析（30分）

### 阶段3.1：数据划分方式分析（2h）

#### 3.1.1 MVPT划分分析

- [ ] 球形嵌套划分的几何意义
- [ ] 子区域形状分析
- [ ] 支撑点信息利用方式

#### 3.1.2 CGHT划分分析

- [ ] 超平面组合划分的几何意义
- [ ] 距离差的信息含义
- [ ] 与原始GHTree的关系

#### 3.1.3 线性划分分析

- [ ] 支撑点空间的含义
- [ ] 线性边界的性质
- [ ] 与度量空间的映射关系

### 阶段3.2：剪枝能力分析（2h）

#### 3.2.1 各索引剪枝条件

- [ ] 推导MVPT剪枝条件
- [ ] 推导CGHT剪枝条件
- [ ] 推导线性划分剪枝条件

#### 3.2.2 剪枝效果理论比较

- [ ] 分析什么情况下各索引剪枝效果好
- [ ] 分析查询半径对剪枝的影响
- [ ] 分析数据分布对剪枝的影响

### 阶段3.3：复杂度分析（1h）

- [ ] 构建时间复杂度
- [ ] 查询时间复杂度（平均/最坏）
- [ ] 空间复杂度
- [ ] 距离计算次数分析

### 阶段3.4：优缺点总结（1h）

- [ ] MVPT优缺点
- [ ] CGHT优缺点
- [ ] 线性划分优缺点
- [ ] 适用场景分析

---

## 五、任务4详细分解：实验分析（30分）

### 阶段4.1：实验设计（2h）

#### 4.1.1 评价指标设计

- [ ] 构建时间
- [ ] 查询时间
- [ ] 距离计算次数
- [ ] 节点访问次数
- [ ] 剪枝率

#### 4.1.2 实验变量设计

- [ ] 数据集选择
- [ ] 查询半径范围
- [ ] 数据规模变化

#### 4.1.3 控制变量设计

- [ ] 统一pivot选择
- [ ] 统一maxLeafSize
- [ ] 统一查询对象

### 阶段4.2：实验实现（4h）

#### 4.2.1 创建性能测试框架

**文件**：`src/test/java/performance/MultiPivotComparisonTest.java`

- [ ] 实现性能指标收集
- [ ] 实现多轮运行取平均
- [ ] 实现结果输出（CSV/表格）

#### 4.2.2 创建演示程序

**文件**：`src/main/java/examples/assignment4/MultiPivotPerformanceAnalysis.java`

- [ ] 加载不同数据集
- [ ] 统一构建三种索引
- [ ] 执行查询实验
- [ ] 输出性能报告

### 阶段4.3：实验执行（3h）

#### 4.3.1 数据集1实验

- [ ] 构建性能记录
- [ ] 不同查询半径的查询性能
- [ ] 记录详细数据

#### 4.3.2 数据集2实验

- [ ] 构建性能记录
- [ ] 不同查询半径的查询性能
- [ ] 记录详细数据

### 阶段4.4：结果分析（3h）

- [ ] 汇总实验数据
- [ ] 生成对比图表
- [ ] 分析性能差异原因
- [ ] 验证理论分析预测
- [ ] 总结各索引适用场景

---

## 六、实验报告撰写（5-8h）

### 6.1 报告结构

```
1. 引言 (1页)
2. 理论基础 (3-4页)
3. 算法实现 (4-5页)
4. 正确性验证 (2-3页)
5. 理论对比分析 (4-5页)
6. 实验对比分析 (5-6页)
7. 总结与展望 (1页)
```

### 6.2 LaTeX文件

- [ ] 创建main.tex
- [ ] 创建各章节tex文件
- [ ] 准备图表
- [ ] 格式调整
- [ ] 最终检查

---

## 七、时间规划建议

### 第1周

| 天 | 任务 |
|----|------|
| Day 1-2 | 阶段1.1基础设施 + 阶段1.2 MVPT实现 |
| Day 3-4 | 阶段1.3 CGHT实现 |
| Day 5-6 | 阶段1.4 线性划分实现 |
| Day 7 | 阶段2正确性验证 |

### 第2周

| 天 | 任务 |
|----|------|
| Day 1-2 | 阶段3理论分析 |
| Day 3-4 | 阶段4实验设计与执行 |
| Day 5-6 | 实验报告撰写 |
| Day 7 | 检查、完善、提交 |

---

## 八、检查清单

### 代码检查

- [ ] 三种索引都实现了Index接口
- [ ] 使用了相同的pivot选择器
- [ ] 所有测试通过
- [ ] 代码有适当的注释

### 实验检查

- [ ] 至少2个数据集
- [ ] 树高至少2层
- [ ] 正确性验证完成
- [ ] 性能数据完整

### 报告检查

- [ ] 理论分析深入
- [ ] 实验方案完整
- [ ] 图表规范
- [ ] 参考文献完整
- [ ] LaTeX编译无错误

---

**文档版本**：v1.0  
**创建日期**：2026-01-11  
**维护者**：Jixiang Ding
