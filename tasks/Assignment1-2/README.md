# 任务计划文档说明

本目录包含了完整的项目任务计划，旨在系统地完成Assignment 1和Assignment 2。

## 文档列表

### 1. 00-项目整体架构规划.md

**用途**: 项目全局视图

- 代码仓库整体架构设计
- 与原始UMAD代码的对应关系
- 理论基础与代码实现的映射
- 开发流程和时间规划
- 质量保证标准

**建议**: 开始项目前务必仔细阅读此文档，建立整体认知

### 2. 01-Assignment1-详细任务分解.md

**用途**: Assignment 1 的详细实施指南

- 9个主要任务的详细分解
- 每个任务的具体实现步骤
- 完整的测试用例设计
- 实验报告撰写指南
- 代码示例和算法伪代码

**重点内容**:

- 任务1: 环境搭建（最高优先级）
- 任务2-3: 抽象类设计（最高优先级）
- 任务4-7: 具体实现（高优先级）
- 任务8: 综合测试（中优先级）
- 任务9: 实验报告（高优先级）

**建议使用方式**:

1. 按照任务序号依次完成
2. 每完成一个任务，立即测试
3. 在文档中勾选完成的子任务
4. 遇到问题参考"常见问题与解决方案"章节

### 3. 02-Assignment2-详细任务分解.md

**用途**: Assignment 2 的详细实施指南

- 4个主要任务的详细分解
- 线性扫描查询实现
- Pivot Table索引实现
- 性能分析实验设计
- 实验报告撰写指南

**重点内容**:

- 任务1: 线性扫描查询（最高优先级）
  - 1.1 范围查询
  - 1.2 kNN查询
  - 1.3 dkNN查询
- 任务2: Pivot Table索引（最高优先级）
  - 2.1 索引结构设计
  - 2.2 支撑点选择算法
  - 2.3-2.4 基于索引的查询
- 任务3: 性能分析实验（中优先级）
- 任务4: 实验报告（高优先级）

**建议使用方式**:

1. 在完成Assignment 1的基础上进行
2. 先实现线性扫描，再实现索引
3. 重点关注正确性验证（对比两种方法的结果）
4. 性能实验要设计多组对照实验

### 4. 03-理论基础整理.md

**用途**: 核心理论知识汇总

- 度量空间基本概念
- 常见度量空间实例（向量、序列）
- 相似性查询算法原理
- Pivot Table索引原理
- 支撑点选择方法
- 数据集格式说明
- 性能评估指标

**重点内容**:

- 第一章: 度量空间三大性质（必须理解）
- 第二章: 闵可夫斯基距离和mPAM矩阵（需要实现）
- 第三章: 查询算法原理（Assignment 2核心）
- 第四章: Pivot Table原理（三角不等式的应用）
- 第六章: 数据集格式（数据读取必备）

**建议使用方式**:

1. 作为实现代码时的理论参考
2. 编写报告时引用相关理论
3. 设计测试用例时参考示例计算
4. 遇到概念不清楚时查阅相应章节

## 使用流程建议

### Phase 1: 准备阶段

1. 阅读 `00-项目整体架构规划.md`，了解全局
2. 阅读 `03-理论基础整理.md` 第1-2章，理解核心概念
3. 探索UMAD-OriginalCode，熟悉参考代码
4. 准备开发环境和数据集

### Phase 2: Assignment 1 实现

1. 打开 `01-Assignment1-详细任务分解.md`
2. 按照任务1-8的顺序依次实现
3. 每完成一个模块，立即编写测试
4. 边实现边完善代码注释
5. 完成任务9: 撰写实验报告

**每日工作流程**:

```
1. 查看当前任务的详细说明
2. 参考03-理论基础整理.md理解原理
3. 查看UMAD-OriginalCode参考实现
4. 编写自己的代码实现
5. 运行测试验证正确性
6. 在文档中勾选完成的子任务
7. 提交代码到Git仓库
```

### Phase 3: Assignment 2 实现

1. 打开 `02-Assignment2-详细任务分解.md`
2. 按照任务1-4的顺序实现
3. 重点关注正确性验证
4. 进行系统的性能实验
5. 撰写实验报告

**性能实验流程**:

```
1. 设计实验方案（参考任务3）
2. 编写实验代码
3. 运行实验收集数据
4. 保存数据到CSV文件
5. 使用Python绘制性能曲线
6. 分析实验结果
7. 在报告中展示和讨论
```

## 关键文件清单

### 代码文件

根据架构规划，你需要创建以下核心文件：

**Assignment 1**:

```
src/main/java/
├── core/
│   ├── MetricSpaceData.java         # 度量空间数据抽象类
│   └── MetricFunction.java          # 距离函数接口
├── datatype/
│   ├── vector/
│   │   ├── VectorData.java          # 向量数据类型
│   │   └── MinkowskiDistance.java   # 闵可夫斯基距离
│   └── protein/
│       ├── ProteinData.java         # 蛋白质序列类型
│       └── AlignmentDistance.java   # Alignment距离
└── io/
    ├── VectorDataReader.java        # 向量数据读取器
    └── ProteinDataReader.java       # 蛋白质数据读取器
```

**Assignment 2**:

```
src/main/java/
├── query/
│   ├── RangeQuery.java              # 范围查询
│   ├── KNNQuery.java                # kNN查询
│   ├── DKNNQuery.java               # dkNN查询
│   ├── KNNResult.java               # kNN结果类
│   ├── LinearScanRangeQuery.java    # 线性扫描范围查询
│   ├── LinearScanKNNQuery.java      # 线性扫描kNN查询
│   └── LinearScanDKNNQuery.java     # 线性扫描dkNN查询
└── index/
    ├── PivotTable.java              # Pivot Table索引
    ├── PivotSelectionMethod.java    # 支撑点选择方法枚举
    ├── PivotSelector.java           # 支撑点选择器
    ├── PivotTableRangeQuery.java    # 基于Pivot Table的范围查询
    └── PivotTableKNNQuery.java      # 基于Pivot Table的kNN查询
```

**测试文件**:

```
src/test/java/
├── datatype/
│   ├── VectorDataTest.java
│   ├── VectorDistanceTest.java
│   ├── ProteinDataTest.java
│   └── ProteinDistanceTest.java
├── query/
│   ├── RangeQueryTest.java
│   ├── KNNQueryTest.java
│   └── DKNNQueryTest.java
└── index/
    ├── PivotTableTest.java
    └── PivotTablePerformanceTest.java
```

**报告文件**:

```
Assignment1/lab-report-1/main.tex
Assignment2/lab-report-2/main.tex
```

## 进度跟踪建议

### 使用清单

在每个任务文档中，都有 `[ ]` 清单，建议：

1. 开始任务时，标记为 `[WIP]`
2. 完成任务时，标记为 `[✓]`
3. 遇到问题时，标记为 `[?]` 并记录问题

示例：

```
- [✓] 创建MetricSpaceData.java
- [WIP] 实现VectorData.java
- [?] 如何处理向量维度不一致的情况？
- [ ] 实现MinkowskiDistance.java
```

### 使用Git

建议的Git工作流：

```bash
# 每个大任务创建一个分支
git checkout -b assignment1-task1-environment

# 完成子任务后提交
git add .
git commit -m "完成环境搭建，添加pom.xml"

# 任务完成后合并到主分支
git checkout main
git merge assignment1-task1-environment
git tag assignment1-task1-done
```

### 记录问题和解决方案

在tasks目录下可以创建 `issues.md` 文件记录：

```markdown
## 问题1: 蛋白质序列比对算法性能较慢
**日期**: 2024-11-16
**问题描述**: 对长序列进行比对时，计算时间过长
**解决方案**: 
1. 只处理6-mers片段而非完整序列
2. 优化动态规划算法，使用滚动数组

## 问题2: Pivot Table查询结果与线性扫描不一致
**日期**: 2024-11-17
**问题描述**: ...
**解决方案**: ...
```

## 报告撰写建议

### LaTeX模板位置

- Assignment 1: `Assignment1/lab-report-1/main.tex`
- Assignment 2: `Assignment2/lab-report-2/main.tex`

### 报告写作流程

1. **边实现边写**: 不要等到全部实现完再写报告
2. **及时记录**: 测试结果、实验数据及时保存
3. **图片保存**: 重要的测试输出图片保存在对应的LaTeX的assets目录下
4. **定期编译**: 每完成一节就编译一次，检查格式
5. **代码展示**: 使用 `listings` 包展示代码
6. **图表规范**: 所有图表都要有编号和标题
7. **参考文献**: 引用理论和参考代码时要标注

### 报告检查清单

**提交前检查**:

- [✓] 所有章节都已完成
- [✓] 代码片段格式正确
- [✓] 所有图表都有标题和引用
- [✓] 测试结果数据真实可信
- [✓] 性能分析图表清晰
- [✓] 参考文献格式规范
- [✓] LaTeX编译无错误无警告
- [✓] PDF可以正常打开阅读
- [✓] 页码、目录正确

## 资源链接

### 理论资源

- full.md: 完整理论教材
- 1.2 数据集介绍.md: 数据集格式和使用说明
- UMAD-OriginalCode: 参考实现代码

### 开发资源

- Java 12 文档: <https://docs.oracle.com/en/java/javase/12/>
- JUnit 4 文档: <https://junit.org/junit4/>
- Maven 文档: <https://maven.apache.org/guides/>

### LaTeX资源

- Overleaf 文档: <https://www.overleaf.com/learn>
- LaTeX 数学公式: <https://en.wikibooks.org/wiki/LaTeX/Mathematics>
- Listings 包文档: <https://ctan.org/pkg/listings>

## 常见问题 FAQ

### Q1: 任务太多，应该从哪里开始？

**A**: 严格按照优先级标记进行：

1. 最高优先级的任务必须先完成
2. Assignment 1 必须在 Assignment 2 之前完成
3. 每个Assignment内部按任务编号顺序进行

### Q2: 代码实现和参考代码有多少相似度合适？

**A**:

- 核心思想和算法可以借鉴
- 类名、变量名、代码结构应该重新设计
- 要理解原理，用自己的方式实现
- 添加详细的中文注释
- 目标是学习，不是复制

### Q3: 测试用例如何设计才算"有代表性"？

**A**:

- 包含边界情况（空数据、单个数据）
- 包含典型情况（正常规模数据）
- 包含特殊情况（相同数据、极端值）
- 易于手工验证（简单数据）
- 使用实际数据集

### Q4: 实验报告应该多详细？

**A**:

- 代码不要全部贴，选择核心部分
- 测试要展示计算过程，不只是结果
- 性能实验要有数据支撑，不能只是文字描述
- 图表要清晰，数据要真实
- 分析要深入，不要只是罗列结果

### Q5: 遇到技术问题怎么办？

**A**:

1. 先查阅任务文档的"常见问题"章节
2. 查看理论基础整理文档
3. 参考UMAD-OriginalCode的实现
4. 搜索Java/Maven相关文档
5. 记录问题和解决方案

## 验收自检表

### Assignment 1 验收

- [✓] 代码结构清晰，符合架构设计
- [✓] MetricSpaceData和MetricFunction设计合理
- [✓] VectorData和MinkowskiDistance实现正确
- [✓] ProteinData和AlignmentDistance实现正确
- [✓] 能正确读取UMAD数据集
- [✓] 每种数据类型至少3个测试用例
- [✓] 验证了度量空间三大性质
- [✓] 实验报告结构完整
- [✓] 代码有完整的JavaDoc注释
- [✓] 提交了可运行的源代码

### Assignment 2 验收

- [✓] 实现了范围查询、kNN、dkNN三种查询
- [✓] 实现了Pivot Table索引
- [✓] 实现了至少2种支撑点选择算法
- [✓] Pivot Table查询结果与线性扫描一致
- [✓] 每种查询至少3个测试用例
- [✓] 完成了支撑点数量影响实验
- [✓] 完成了支撑点选择策略对比实验
- [✓] 完成了线性扫描vs索引性能对比
- [✓] 生成了清晰的性能图表
- [✓] 实验报告包含详细的性能分析

## 总结

这套任务计划文档力求做到：

1. **完整**: 覆盖所有必要的任务和知识点
2. **详细**: 每个任务都有具体的实现步骤
3. **实用**: 提供代码示例和测试用例
4. **有序**: 按照优先级和依赖关系组织
5. **可验证**: 提供明确的验收标准

---

**最后更新**: 2025-11-16
**文档版本**: v1.0
**作者**: Jixiang Ding
