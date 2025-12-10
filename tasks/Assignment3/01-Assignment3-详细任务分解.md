# Assignment 3 详细任务分解

本文档将Assignment 3的任务细化到具体的类和方法级别，并提供详细的实现指导。

## 目录

- [一、任务分解总览](#一任务分解总览)
- [二、阶段一：基础设施搭建](#二阶段一基础设施搭建)
- [三、阶段二：GH树实现](#三阶段二gh树实现)
- [四、阶段三：VP树实现](#四阶段三vp树实现)
- [五、阶段四：查询算法实现](#五阶段四查询算法实现)
- [六、阶段五：测试与验证](#六阶段五测试与验证)
- [七、阶段六：性能实验](#七阶段六性能实验)
- [八、阶段七：报告撰写](#八阶段七报告撰写)

---

## 一、任务分解总览

### 1.1 任务依赖关系图

```
阶段一：基础设施 
    ├── Index接口
    ├── TreeNode接口
    ├── TreeIndex抽象类
    ├── TreeConfig配置类
    └── TreeHeightController
          ↓
阶段二：GH树实现
    ├── GHInternalNode
    ├── GHLeafNode（复用LeafNode）
    └── GHTree + GHTreeBuilder
          ↓
阶段三：VP树实现
    ├── VPInternalNode
    ├── VPLeafNode（复用LeafNode）
    └── VPTree + VPTreeBuilder
          ↓
阶段四：查询算法
    ├── GHRangeQuery + GHKNNQuery
    └── VPRangeQuery + VPKNNQuery
          ↓
阶段五：测试验证
    ├── 单元测试
    ├── 正确性测试
    └── 一致性测试
          ↓
阶段六：性能实验
    ├── 数据集准备
    ├── 性能测试框架
    └── 实验执行
          ↓
阶段七：报告撰写
```

### 1.2 工作量估算

| 阶段 | 预计工时 | 难度 | 优先级 |
|-----|----------|------|--------|
| 阶段一 | 8小时 | 中 | 最高 |
| 阶段二 | 12小时 | 高 | 最高 |
| 阶段三 | 12小时 | 高 | 最高 |
| 阶段四 | 10小时 | 高 | 高 |
| 阶段五 | 10小时 | 中 | 高 |
| 阶段六 | 12小时 | 中 | 中 |
| 阶段七 | 16小时 | 中 | 中 |
| **总计** | **80小时** | - | - |

---

## 二、阶段一：基础设施搭建

### 任务2.1：创建索引接口（Index.java）

**位置**：`src/main/java/index/Index.java`

**目标**：定义所有索引结构的统一接口

**实现要点**：

```java
package index;

import core.MetricSpaceData;
import core.MetricFunction;
import java.util.List;

public interface Index {
    // 构建索引
    void buildIndex(List<? extends MetricSpaceData> dataset, MetricFunction metric);
    
    // 范围查询
    List<MetricSpaceData> rangeQuery(MetricSpaceData queryObject, double radius);
    
    // k近邻查询
    List<MetricSpaceData> knnQuery(MetricSpaceData queryObject, int k);
    
    // 获取统计信息
    String getStatistics();
    
    // 获取索引名称
    String getIndexName();
    
    // 重置统计信息
    void resetStatistics();
}
```

**验收标准**：

- [ ] 接口编译通过
- [ ] JavaDoc注释完整
- [ ] 方法签名清晰明确

---

### 任务2.2：创建树节点接口（TreeNode.java）

**位置**：`src/main/java/index/tree/TreeNode.java`

**实现要点**：

```java
package index.tree;

public interface TreeNode {
    // 判断是否为叶子节点
    boolean isLeaf();
    
    // 获取节点中数据的数量
    int size();
    
    // 获取节点深度
    int getDepth();
}
```

---

### 任务2.3：创建内部节点抽象类（InternalNode.java）

**位置**：`src/main/java/index/tree/InternalNode.java`

**实现要点**：

```java
package index.tree;

import core.MetricSpaceData;
import java.util.List;

public abstract class InternalNode implements TreeNode {
    protected List<MetricSpaceData> pivots;
    protected List<TreeNode> children;
    protected int depth;
    
    @Override
    public boolean isLeaf() {
        return false;
    }
    
    @Override
    public int size() {
        return children.stream().mapToInt(TreeNode::size).sum();
    }
    
    @Override
    public int getDepth() {
        return depth;
    }
    
    // Getters
    public List<MetricSpaceData> getPivots() { return pivots; }
    public List<TreeNode> getChildren() { return children; }
    
    // 抽象方法：判断是否需要访问子节点
    public abstract boolean shouldVisitChild(int childIndex, 
                                            MetricSpaceData queryObject, 
                                            double radius);
}
```

---

### 任务2.4：创建叶子节点类（LeafNode.java）

**位置**：`src/main/java/index/tree/LeafNode.java`

**实现要点**：

```java
package index.tree;

import core.MetricSpaceData;
import java.util.ArrayList;
import java.util.List;

public class LeafNode implements TreeNode {
    private List<MetricSpaceData> data;
    private int depth;
    
    public LeafNode(List<? extends MetricSpaceData> data, int depth) {
        this.data = new ArrayList<>(data);
        this.depth = depth;
    }
    
    @Override
    public boolean isLeaf() {
        return true;
    }
    
    @Override
    public int size() {
        return data.size();
    }
    
    @Override
    public int getDepth() {
        return depth;
    }
    
    public List<MetricSpaceData> getData() {
        return data;
    }
}
```

**注意事项**：

- 叶子节点使用新的ArrayList复制数据，避免外部修改
- depth字段记录节点在树中的深度，便于统计

---

### 任务2.5：创建树配置类（TreeConfig.java）

**位置**：`src/main/java/index/tree/common/TreeConfig.java`

**实现要点**：

```java
package index.tree.common;

public class TreeConfig {
    // 配置参数
    private int maxLeafSize;           // 最大叶子节点容量（默认50）
    private int minTreeHeight;          // 最小树高（默认3）
    private PivotSelectionStrategy pivotStrategy;
    private boolean verbose;            // 是否输出详细信息
    
    // 默认构造函数
    public TreeConfig() {
        this.maxLeafSize = 50;
        this.minTreeHeight = 3;
        this.pivotStrategy = PivotSelectionStrategy.RANDOM;
        this.verbose = false;
    }
    
    // Builder模式
    public static class Builder {
        private TreeConfig config = new TreeConfig();
        
        public Builder maxLeafSize(int size) {
            config.maxLeafSize = size;
            return this;
        }
        
        public Builder minTreeHeight(int height) {
            config.minTreeHeight = height;
            return this;
        }
        
        public Builder pivotStrategy(PivotSelectionStrategy strategy) {
            config.pivotStrategy = strategy;
            return this;
        }
        
        public Builder verbose(boolean verbose) {
            config.verbose = verbose;
            return this;
        }
        
        public TreeConfig build() {
            // 验证配置
            if (config.maxLeafSize <= 0) {
                throw new IllegalArgumentException("maxLeafSize必须大于0");
            }
            if (config.minTreeHeight < 0) {
                throw new IllegalArgumentException("minTreeHeight不能为负");
            }
            return config;
        }
    }
    
    // Getters...
    
    public enum PivotSelectionStrategy {
        RANDOM,         // 随机选择
        FFT,            // 最远优先遍历
        MAX_SPREAD      // 最大分散度
    }
}
```

**使用示例**：

```java
TreeConfig config = new TreeConfig.Builder()
    .maxLeafSize(50)
    .minTreeHeight(3)
    .pivotStrategy(TreeConfig.PivotSelectionStrategy.RANDOM)
    .verbose(true)
    .build();
```

---

### 任务2.6：创建树高控制器（TreeHeightController.java）

**位置**：`src/main/java/index/tree/common/TreeHeightController.java`

**核心算法**：

```java
package index.tree.common;

public class TreeHeightController {
    private TreeConfig config;
    
    public TreeHeightController(TreeConfig config) {
        this.config = config;
    }
    
    /**
     * 判断是否可以创建叶子节点
     * @param currentDepth 当前深度（根节点为0）
     * @param dataSize 当前节点的数据量
     * @return true表示可以创建叶子，false表示必须继续划分
     */
    public boolean canCreateLeaf(int currentDepth, int dataSize) {
        // 情况1：如果数据量很小（<=2），必须创建叶子
        if (dataSize <= 2) {
            return true;
        }
        
        // 情况2：如果还没达到最小高度，必须继续划分
        if (currentDepth < config.getMinTreeHeight()) {
            return false;
        }
        
        // 情况3：如果已达到最小高度，根据数据量判断
        return dataSize <= config.getMaxLeafSize();
    }
    
    /**
     * 计算给定数据集应该使用的最大叶子大小，以确保达到最小树高
     * @param datasetSize 数据集大小
     * @return 调整后的最大叶子大小
     */
    public int calculateMaxLeafSize(int datasetSize) {
        int minHeight = config.getMinTreeHeight();
        
        // 假设二叉树：
        // 树高h时，最多有2^h个叶子
        // 如果有n个数据，最大叶子容量为L，则至少需要n/L个叶子
        // 要使 n/L <= 2^h，即 L >= n/2^h
        
        int maxLeafForHeight = (int) Math.ceil(
            datasetSize / Math.pow(2, minHeight)
        );
        
        // 返回配置值和计算值中的较小者
        return Math.min(config.getMaxLeafSize(), maxLeafForHeight);
    }
}
```

**测试用例**：

```java
@Test
public void testCanCreateLeaf() {
    TreeConfig config = new TreeConfig.Builder()
        .minTreeHeight(3)
        .maxLeafSize(50)
        .build();
    TreeHeightController controller = new TreeHeightController(config);
    
    // 深度0，数据100条 -> 不能创建叶子
    assertFalse(controller.canCreateLeaf(0, 100));
    
    // 深度3，数据50条 -> 可以创建叶子
    assertTrue(controller.canCreateLeaf(3, 50));
    
    // 深度2，数据2条 -> 可以创建叶子（数据太少）
    assertTrue(controller.canCreateLeaf(2, 2));
}
```

---

### 任务2.7：创建树索引抽象类（TreeIndex.java）

**位置**：`src/main/java/index/tree/TreeIndex.java`

**核心实现**：

```java
package index.tree;

import core.MetricSpaceData;
import core.MetricFunction;
import index.Index;
import index.tree.common.TreeConfig;
import index.tree.common.TreeHeightController;
import java.util.List;

public abstract class TreeIndex implements Index {
    // 核心属性
    protected TreeNode root;
    protected TreeConfig config;
    protected MetricFunction metric;
    protected List<? extends MetricSpaceData> dataset;
    protected TreeHeightController heightController;
    
    // 统计信息
    protected int buildDistanceComputations = 0;
    protected int queryDistanceComputations = 0;
    protected int nodeAccesses = 0;
    protected int treeHeight = 0;
    protected int totalNodes = 0;
    protected int leafNodes = 0;
    
    /**
     * 构造函数
     */
    public TreeIndex(TreeConfig config) {
        this.config = config;
        this.heightController = new TreeHeightController(config);
    }
    
    /**
     * 构建索引（模板方法）
     */
    @Override
    public void buildIndex(List<? extends MetricSpaceData> dataset, 
                          MetricFunction metric) {
        this.dataset = dataset;
        this.metric = metric;
        this.buildDistanceComputations = 0;
        
        // 验证输入
        if (dataset == null || dataset.isEmpty()) {
            throw new IllegalArgumentException("数据集不能为空");
        }
        if (metric == null) {
            throw new IllegalArgumentException("距离函数不能为空");
        }
        
        // 构建树
        long startTime = System.currentTimeMillis();
        this.root = buildTreeRecursive(dataset, 0);
        long endTime = System.currentTimeMillis();
        
        // 计算统计信息
        calculateStatistics();
        
        if (config.isVerbose()) {
            System.out.println("索引构建完成：");
            System.out.println("  耗时: " + (endTime - startTime) + "ms");
            System.out.println("  树高: " + treeHeight);
            System.out.println("  节点数: " + totalNodes);
            System.out.println("  距离计算: " + buildDistanceComputations);
        }
    }
    
    /**
     * 递归构建树（子类实现）
     * @param data 当前节点的数据
     * @param depth 当前深度
     * @return 构建的节点
     */
    protected abstract TreeNode buildTreeRecursive(
        List<? extends MetricSpaceData> data, int depth);
    
    /**
     * 计算统计信息
     */
    protected void calculateStatistics() {
        if (root != null) {
            treeHeight = calculateHeight(root);
            totalNodes = countNodes(root);
            leafNodes = countLeafNodes(root);
        }
    }
    
    // 辅助方法
    protected int calculateHeight(TreeNode node) {
        if (node == null || node.isLeaf()) {
            return 0;
        }
        InternalNode internal = (InternalNode) node;
        int maxChildHeight = 0;
        for (TreeNode child : internal.getChildren()) {
            maxChildHeight = Math.max(maxChildHeight, calculateHeight(child));
        }
        return 1 + maxChildHeight;
    }
    
    protected int countNodes(TreeNode node) {
        if (node == null) return 0;
        if (node.isLeaf()) return 1;
        
        int count = 1;
        for (TreeNode child : ((InternalNode) node).getChildren()) {
            count += countNodes(child);
        }
        return count;
    }
    
    protected int countLeafNodes(TreeNode node) {
        if (node == null) return 0;
        if (node.isLeaf()) return 1;
        
        int count = 0;
        for (TreeNode child : ((InternalNode) node).getChildren()) {
            count += countLeafNodes(child);
        }
        return count;
    }
    
    @Override
    public String getStatistics() {
        return String.format(
            "索引: %s\n" +
            "数据集大小: %d\n" +
            "树高度: %d\n" +
            "总节点数: %d\n" +
            "叶子节点数: %d\n" +
            "构建距离计算: %d\n" +
            "查询距离计算: %d\n" +
            "节点访问次数: %d",
            getIndexName(),
            dataset.size(),
            treeHeight,
            totalNodes,
            leafNodes,
            buildDistanceComputations,
            queryDistanceComputations,
            nodeAccesses
        );
    }
    
    @Override
    public void resetStatistics() {
        queryDistanceComputations = 0;
        nodeAccesses = 0;
    }
}
```

---

## 三、阶段二：GH树实现

### 任务3.1：实现GH树内部节点（GHInternalNode.java）

**位置**：`src/main/java/index/tree/ghtree/GHInternalNode.java`

**GH树特点**：

- 使用**两个pivot点**（p1和p2）
- 通过**超平面**划分空间
- 数据划分规则：d(x, p1) < d(x, p2) → 左子树，否则 → 右子树

**实现代码**：

```java
package index.tree.ghtree;

import core.MetricSpaceData;
import index.tree.InternalNode;
import index.tree.TreeNode;
import java.util.List;

/**
 * GH树内部节点
 * 使用两个pivot点通过超平面划分空间
 */
public class GHInternalNode extends InternalNode {
    /**
     * 构造函数
     * @param pivot1 第一个pivot
     * @param pivot2 第二个pivot  
     * @param leftChild 左子树（离p1近）
     * @param rightChild 右子树（离p2近）
     * @param depth 节点深度
     */
    public GHInternalNode(MetricSpaceData pivot1, MetricSpaceData pivot2,
                          TreeNode leftChild, TreeNode rightChild, int depth) {
        this.pivots = List.of(pivot1, pivot2);
        this.children = List.of(leftChild, rightChild);
        this.depth = depth;
    }
    
    /**
     * GH树的剪枝规则：
     * - 设d1 = d(q, p1), d2 = d(q, p2)
     * - 如果d1 - d2 > 2r，可以排除左子树（index=0）
     * - 如果d2 - d1 > 2r，可以排除右子树（index=1）
     * 
     * @param childIndex 子节点索引（0=左，1=右）
     * @param queryObject 查询对象
     * @param radius 查询半径
     * @return true表示需要访问，false表示可以剪枝
     */
    @Override
    public boolean shouldVisitChild(int childIndex, 
                                   MetricSpaceData queryObject, 
                                   double radius) {
        // 注意：此方法需要访问距离值，实际使用时需要传入预计算的距离
        // 因此这里只是接口实现，真正的剪枝逻辑在GHRangeQuery中
        return true;
    }
    
    // 便捷的getter方法
    public MetricSpaceData getPivot1() {
        return pivots.get(0);
    }
    
    public MetricSpaceData getPivot2() {
        return pivots.get(1);
    }
    
    public TreeNode getLeftChild() {
        return children.get(0);
    }
    
    public TreeNode getRightChild() {
        return children.get(1);
    }
}
```

---

### 任务3.2：实现GH树主类（GHTree.java）

**位置**：`src/main/java/index/tree/ghtree/GHTree.java`

**核心算法**：

```java
package index.tree.ghtree;

import core.MetricSpaceData;
import index.tree.*;
import index.tree.common.TreeConfig;
import java.util.*;

/**
 * GH树（Generalized Hyperplane Tree）
 * 使用超平面划分空间的树状索引结构
 */
public class GHTree extends TreeIndex {
    
    public GHTree(TreeConfig config) {
        super(config);
    }
    
    @Override
    public String getIndexName() {
        return "GH-Tree";
    }
    
    /**
     * 递归构建GH树
     * 
     * 算法步骤：
     * 1. 判断是否应该创建叶子节点
     * 2. 选择两个pivot点（p1, p2）
     * 3. 将数据划分为两部分：
     *    - 左子树：d(x, p1) < d(x, p2)
     *    - 右子树：d(x, p1) >= d(x, p2)
     * 4. 递归构建左右子树
     */
    @Override
    protected TreeNode buildTreeRecursive(List<? extends MetricSpaceData> data, 
                                         int depth) {
        // 步骤1：判断是否创建叶子节点
        if (heightController.canCreateLeaf(depth, data.size())) {
            return new LeafNode(data, depth);
        }
        
        // 步骤2：选择两个pivot
        MetricSpaceData[] pivots = selectTwoPivots(data);
        MetricSpaceData pivot1 = pivots[0];
        MetricSpaceData pivot2 = pivots[1];
        
        // 步骤3：划分数据
        List<MetricSpaceData> leftData = new ArrayList<>();
        List<MetricSpaceData> rightData = new ArrayList<>();
        
        for (MetricSpaceData obj : data) {
            double d1 = metric.getDistance(obj, pivot1);
            double d2 = metric.getDistance(obj, pivot2);
            buildDistanceComputations += 2;
            
            if (d1 < d2) {
                leftData.add(obj);
            } else {
                rightData.add(obj);
            }
        }
        
        // 处理极端情况：所有数据都在一侧
        if (leftData.isEmpty() || rightData.isEmpty()) {
            if (config.isVerbose()) {
                System.out.println("警告：深度" + depth + "处数据划分不均，" +
                    "左=" + leftData.size() + "，右=" + rightData.size());
            }
            
            // 强制创建叶子节点
            return new LeafNode(data, depth);
        }
        
        // 步骤4：递归构建子树
        TreeNode leftChild = buildTreeRecursive(leftData, depth + 1);
        TreeNode rightChild = buildTreeRecursive(rightData, depth + 1);
        
        // 返回内部节点
        return new GHInternalNode(pivot1, pivot2, leftChild, rightChild, depth);
    }
    
    /**
     * 选择两个pivot点
     * 
     * 策略选择（根据config）：
     * - RANDOM：随机选择两个不同的点
     * - FFT：使用Farthest-First Traversal
     * - MAX_SPREAD：选择距离最远的两个点
     */
    private MetricSpaceData[] selectTwoPivots(List<? extends MetricSpaceData> data) {
        MetricSpaceData[] pivots = new MetricSpaceData[2];
        
        switch (config.getPivotStrategy()) {
            case RANDOM:
                pivots = selectRandomPivots(data);
                break;
            case FFT:
                pivots = selectFFTPivots(data);
                break;
            case MAX_SPREAD:
                pivots = selectMaxSpreadPivots(data);
                break;
            default:
                pivots = selectRandomPivots(data);
        }
        
        return pivots;
    }
    
    /**
     * 随机选择策略
     */
    private MetricSpaceData[] selectRandomPivots(List<? extends MetricSpaceData> data) {
        Random random = new Random();
        int idx1 = random.nextInt(data.size());
        int idx2;
        do {
            idx2 = random.nextInt(data.size());
        } while (idx2 == idx1);
        
        return new MetricSpaceData[] {data.get(idx1), data.get(idx2)};
    }
    
    /**
     * FFT（Farthest-First Traversal）策略
     * 1. 随机选择第一个pivot
     * 2. 选择离第一个pivot最远的点作为第二个pivot
     */
    private MetricSpaceData[] selectFFTPivots(List<? extends MetricSpaceData> data) {
        Random random = new Random();
        MetricSpaceData pivot1 = data.get(random.nextInt(data.size()));
        
        MetricSpaceData pivot2 = null;
        double maxDist = -1;
        
        for (MetricSpaceData obj : data) {
            double dist = metric.getDistance(pivot1, obj);
            buildDistanceComputations++;
            
            if (dist > maxDist) {
                maxDist = dist;
                pivot2 = obj;
            }
        }
        
        return new MetricSpaceData[] {pivot1, pivot2};
    }
    
    /**
     * 最大分散度策略
     * 找到数据集中距离最远的两个点
     */
    private MetricSpaceData[] selectMaxSpreadPivots(List<? extends MetricSpaceData> data) {
        MetricSpaceData pivot1 = null;
        MetricSpaceData pivot2 = null;
        double maxDist = -1;
        
        // 采样以减少计算量
        int sampleSize = Math.min(100, data.size());
        List<? extends MetricSpaceData> sample = sampleData(data, sampleSize);
        
        for (int i = 0; i < sample.size(); i++) {
            for (int j = i + 1; j < sample.size(); j++) {
                double dist = metric.getDistance(sample.get(i), sample.get(j));
                buildDistanceComputations++;
                
                if (dist > maxDist) {
                    maxDist = dist;
                    pivot1 = sample.get(i);
                    pivot2 = sample.get(j);
                }
            }
        }
        
        return new MetricSpaceData[] {pivot1, pivot2};
    }
    
    /**
     * 数据采样
     */
    private List<? extends MetricSpaceData> sampleData(
            List<? extends MetricSpaceData> data, int sampleSize) {
        if (data.size() <= sampleSize) {
            return data;
        }
        
        Random random = new Random();
        Set<Integer> indices = new HashSet<>();
        while (indices.size() < sampleSize) {
            indices.add(random.nextInt(data.size()));
        }
        
        List<MetricSpaceData> sample = new ArrayList<>();
        for (int idx : indices) {
            sample.add(data.get(idx));
        }
        return sample;
    }
    
    // 查询方法在任务5中实现
    @Override
    public List<MetricSpaceData> rangeQuery(MetricSpaceData queryObject, double radius) {
        // 待实现
        return null;
    }
    
    @Override
    public List<MetricSpaceData> knnQuery(MetricSpaceData queryObject, int k) {
        // 待实现
        return null;
    }
}
```

**测试用例**（GHTreeTest.java）：

```java
@Test
public void testGHTreeBuild() {
    // 准备数据
    List<VectorData> dataset = createSmallDataset(); // 10个2维向量
    
    // 配置
    TreeConfig config = new TreeConfig.Builder()
        .maxLeafSize(3)
        .minTreeHeight(2)
        .pivotStrategy(TreeConfig.PivotSelectionStrategy.RANDOM)
        .build();
    
    // 构建树
    GHTree tree = new GHTree(config);
    tree.buildIndex(dataset, new MinkowskiDistance(2));
    
    // 验证
    assertNotNull(tree.root);
    assertTrue(tree.treeHeight >= 2, "树高应至少为2");
    System.out.println(tree.getStatistics());
}
```

---

## 四、阶段三：VP树实现

### 任务4.1：实现VP树内部节点（VPInternalNode.java）

**VP树特点**：

- 使用**一个或多个pivot点**
- 通过**球形**划分空间
- 存储每个子区域的**距离范围** [lower, upper]

**实现代码**：

```java
package index.tree.vptree;

import core.MetricSpaceData;
import index.tree.InternalNode;
import index.tree.TreeNode;
import java.util.*;

/**
 * VP树内部节点
 * 使用球形划分空间
 */
public class VPInternalNode extends InternalNode {
    // 每个子树对应的距离范围
    private List<DistanceRange> distanceRanges;
    
    /**
     * 距离范围类
     */
    public static class DistanceRange {
        public double lower;  // 下界
        public double upper;  // 上界
        
        public DistanceRange(double lower, double upper) {
            this.lower = lower;
            this.upper = upper;
        }
        
        @Override
        public String toString() {
            return String.format("[%.3f, %.3f]", lower, upper);
        }
    }
    
    /**
     * 构造函数
     * @param pivot 主pivot点
     * @param children 子节点列表
     * @param distanceRanges 每个子节点对应的距离范围
     * @param depth 节点深度
     */
    public VPInternalNode(MetricSpaceData pivot,
                          List<TreeNode> children,
                          List<DistanceRange> distanceRanges,
                          int depth) {
        this.pivots = Collections.singletonList(pivot);
        this.children = new ArrayList<>(children);
        this.distanceRanges = new ArrayList<>(distanceRanges);
        this.depth = depth;
    }
    
    /**
     * VP树的剪枝规则：
     * 对于子树i的距离范围[Li, Ui]：
     * - 如果 d(q, p) + r < Li，可以排除子树i
     * - 如果 d(q, p) - r > Ui，可以排除子树i
     */
    @Override
    public boolean shouldVisitChild(int childIndex, 
                                   MetricSpaceData queryObject, 
                                   double radius) {
        // 实际剪枝逻辑在VPRangeQuery中实现
        return true;
    }
    
    public MetricSpaceData getPivot() {
        return pivots.get(0);
    }
    
    public DistanceRange getDistanceRange(int childIndex) {
        return distanceRanges.get(childIndex);
    }
    
    public List<DistanceRange> getDistanceRanges() {
        return distanceRanges;
    }
}
```

---

### 任务4.2：实现VP树主类（VPTree.java）

**核心算法**：

```java
package index.tree.vptree;

import core.MetricSpaceData;
import index.tree.*;
import index.tree.common.TreeConfig;
import index.tree.vptree.VPInternalNode.DistanceRange;
import java.util.*;

/**
 * VP树（Vantage Point Tree）
 * 使用球形划分空间的树状索引结构
 */
public class VPTree extends TreeIndex {
    
    public VPTree(TreeConfig config) {
        super(config);
    }
    
    @Override
    public String getIndexName() {
        return "VP-Tree";
    }
    
    /**
     * 递归构建VP树
     * 
     * 算法步骤：
     * 1. 判断是否应该创建叶子节点
     * 2. 选择一个pivot点
     * 3. 计算所有数据到pivot的距离
     * 4. 根据距离排序，划分为两个（或多个）子区域
     * 5. 记录每个子区域的距离范围[lower, upper]
     * 6. 递归构建子树
     */
    @Override
    protected TreeNode buildTreeRecursive(List<? extends MetricSpaceData> data, 
                                         int depth) {
        // 步骤1：判断是否创建叶子节点
        if (heightController.canCreateLeaf(depth, data.size())) {
            return new LeafNode(data, depth);
        }
        
        // 步骤2：选择pivot
        MetricSpaceData pivot = selectPivot(data);
        
        // 步骤3：计算距离并排序
        List<DataWithDistance> dataWithDist = new ArrayList<>();
        for (MetricSpaceData obj : data) {
            if (obj == pivot) continue; // 跳过pivot本身
            
            double dist = metric.getDistance(obj, pivot);
            buildDistanceComputations++;
            dataWithDist.add(new DataWithDistance(obj, dist));
        }
        
        // 按距离排序
        dataWithDist.sort(Comparator.comparingDouble(d -> d.distance));
        
        // 步骤4：划分数据（二分）
        int midpoint = dataWithDist.size() / 2;
        List<MetricSpaceData> innerData = new ArrayList<>();
        List<MetricSpaceData> outerData = new ArrayList<>();
        
        for (int i = 0; i < dataWithDist.size(); i++) {
            if (i < midpoint) {
                innerData.add(dataWithDist.get(i).data);
            } else {
                outerData.add(dataWithDist.get(i).data);
            }
        }
        
        // 处理极端情况
        if (innerData.isEmpty() || outerData.isEmpty()) {
            return new LeafNode(data, depth);
        }
        
        // 步骤5：计算距离范围
        double innerLower = dataWithDist.get(0).distance;
        double innerUpper = dataWithDist.get(midpoint - 1).distance;
        double outerLower = dataWithDist.get(midpoint).distance;
        double outerUpper = dataWithDist.get(dataWithDist.size() - 1).distance;
        
        List<DistanceRange> ranges = Arrays.asList(
            new DistanceRange(innerLower, innerUpper),
            new DistanceRange(outerLower, outerUpper)
        );
        
        // 步骤6：递归构建子树
        TreeNode innerChild = buildTreeRecursive(innerData, depth + 1);
        TreeNode outerChild = buildTreeRecursive(outerData, depth + 1);
        
        List<TreeNode> children = Arrays.asList(innerChild, outerChild);
        
        return new VPInternalNode(pivot, children, ranges, depth);
    }
    
    /**
     * 选择pivot点
     */
    private MetricSpaceData selectPivot(List<? extends MetricSpaceData> data) {
        switch (config.getPivotStrategy()) {
            case RANDOM:
                return data.get(new Random().nextInt(data.size()));
            case FFT:
                return selectFFTPivot(data);
            default:
                return data.get(new Random().nextInt(data.size()));
        }
    }
    
    /**
     * FFT策略选择pivot
     */
    private MetricSpaceData selectFFTPivot(List<? extends MetricSpaceData> data) {
        // 随机选择一个参考点
        MetricSpaceData ref = data.get(new Random().nextInt(data.size()));
        
        // 找到离参考点最远的点作为pivot
        MetricSpaceData pivot = null;
        double maxDist = -1;
        
        int sampleSize = Math.min(50, data.size());
        for (int i = 0; i < sampleSize; i++) {
            MetricSpaceData candidate = data.get(new Random().nextInt(data.size()));
            double dist = metric.getDistance(ref, candidate);
            buildDistanceComputations++;
            
            if (dist > maxDist) {
                maxDist = dist;
                pivot = candidate;
            }
        }
        
        return pivot;
    }
    
    /**
     * 辅助类：数据及其到pivot的距离
     */
    private static class DataWithDistance {
        MetricSpaceData data;
        double distance;
        
        DataWithDistance(MetricSpaceData data, double distance) {
            this.data = data;
            this.distance = distance;
        }
    }
    
    // 查询方法在任务5中实现
    @Override
    public List<MetricSpaceData> rangeQuery(MetricSpaceData queryObject, double radius) {
        return null;
    }
    
    @Override
    public List<MetricSpaceData> knnQuery(MetricSpaceData queryObject, int k) {
        return null;
    }
}
```

---

## 五、阶段四：查询算法实现

由于篇幅限制，查询算法的详细实现请参考后续任务。关键要点：

### 任务5.1：GH树范围查询（GHRangeQuery.java）

**剪枝规则**：

- d(q, p1) - d(q, p2) > 2r → 剪枝左子树
- d(q, p2) - d(q, p1) > 2r → 剪枝右子树

### 任务5.2：VP树范围查询（VPRangeQuery.java）

**剪枝规则**：

- d(q, p) + r < L → 剪枝
- d(q, p) - r > U → 剪枝

### 任务5.3：kNN查询实现

使用优先队列维护当前k个最近邻。

---

## 六、阶段五：测试与验证

### 任务6.1：单元测试

每个类都需要对应的单元测试。

### 任务6.2：正确性验证

与线性扫描结果对比。

### 任务6.3：一致性测试

验证GHT和VPT返回相同的结果。

---

## 七、阶段六：性能实验

详细实验方案参考报告要求。

---

## 八、阶段七：报告撰写

按照Assignment3-Requirements.md中的报告结构撰写。

---

## 附录A：关键算法伪代码

### A.1 GH树构建算法

```
function BuildGHTree(data, depth):
    if canCreateLeaf(depth, size(data)):
        return LeafNode(data, depth)
    
    // 选择两个pivot
    (p1, p2) = selectTwoPivots(data)
    
    // 划分数据
    leftData = []
    rightData = []
    for each x in data:
        if distance(x, p1) < distance(x, p2):
            add x to leftData
        else:
            add x to rightData
    
    // 递归构建
    leftChild = BuildGHTree(leftData, depth + 1)
    rightChild = BuildGHTree(rightData, depth + 1)
    
    return GHInternalNode(p1, p2, leftChild, rightChild, depth)
```

### A.2 VP树构建算法

```
function BuildVPTree(data, depth):
    if canCreateLeaf(depth, size(data)):
        return LeafNode(data, depth)
    
    // 选择pivot
    p = selectPivot(data)
    
    // 计算距离并排序
    distances = []
    for each x in data (x ≠ p):
        distances.add((x, distance(x, p)))
    sort distances by distance value
    
    // 二分划分
    mid = size(distances) / 2
    innerData = distances[0:mid]
    outerData = distances[mid:end]
    
    // 计算范围
    innerRange = [min(innerData.distances), max(innerData.distances)]
    outerRange = [min(outerData.distances), max(outerData.distances)]
    
    // 递归构建
    innerChild = BuildVPTree(innerData, depth + 1)
    outerChild = BuildVPTree(outerData, depth + 1)
    
    return VPInternalNode(p, [innerChild, outerChild], 
                          [innerRange, outerRange], depth)
```

---

## 附录B：常见问题

### Q1：如何确保树高至少为3层？

A：使用TreeHeightController，在canCreateLeaf方法中判断当前深度，如果小于minTreeHeight且数据量足够，则强制继续划分。

### Q2：GHT和VPT如何使用相同的pivot？

A：在构建前预先选择所有pivot，存储在列表中，两种树按相同顺序使用。

### Q3：如果数据划分极度不均怎么办？

A：记录警告日志，必要时降级为叶子节点。

---

本文档持续更新中...
