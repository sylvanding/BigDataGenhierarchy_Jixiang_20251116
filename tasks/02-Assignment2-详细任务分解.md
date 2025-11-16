# Assignment 2 详细任务分解

## 一、任务目标回顾

在Assignment 1的基础上实现基本的度量空间相似性查询功能。

## 二、核心任务清单

### 任务1: 实现线性扫描查询 (优先级: 最高)

#### 1.1 范围查询 (Range Query)

**理论基础** (来自full.md 3.1节):

```
范围查询定义:
  给定查询对象q和查询半径r，找出所有与q距离不超过r的数据对象
  Result = {s ∈ S | d(q, s) ≤ r}
  
算法原理:
  线性扫描所有数据，计算与查询对象的距离，保留满足条件的结果
```

**RangeQuery类设计**:

```java
public class RangeQuery {
    private final MetricSpaceData queryObject;  // 查询对象
    private final double radius;                 // 查询半径
    
    public RangeQuery(MetricSpaceData queryObject, double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("半径不能为负");
        }
        this.queryObject = queryObject;
        this.radius = radius;
    }
    
    public MetricSpaceData getQueryObject() {
        return queryObject;
    }
    
    public double getRadius() {
        return radius;
    }
}
```

**LinearScanRangeQuery类设计**:

```java
public class LinearScanRangeQuery {
    /**
     * 执行范围查询
     * @param dataset 数据集
     * @param query 查询对象
     * @param metric 距离函数
     * @return 查询结果列表
     */
    public static List<MetricSpaceData> execute(
        List<? extends MetricSpaceData> dataset,
        RangeQuery query,
        MetricFunction metric) {
        
        List<MetricSpaceData> results = new ArrayList<>();
        int distanceCalculations = 0;
        
        MetricSpaceData queryObject = query.getQueryObject();
        double radius = query.getRadius();
        
        // 线性扫描所有数据
        for (MetricSpaceData data : dataset) {
            double distance = metric.getDistance(queryObject, data);
            distanceCalculations++;
            
            if (distance <= radius) {
                results.add(data);
            }
        }
        
        System.out.println("范围查询统计:");
        System.out.println("  数据集大小: " + dataset.size());
        System.out.println("  距离计算次数: " + distanceCalculations);
        System.out.println("  结果数量: " + results.size());
        
        return results;
    }
}
```

**测试用例设计**:

```java
@Test
public void testRangeQuery_SimpleCase() {
    // 创建简单的测试数据集
    List<VectorData> dataset = new ArrayList<>();
    dataset.add(new VectorData(0, new double[]{0, 0}));
    dataset.add(new VectorData(1, new double[]{1, 0}));
    dataset.add(new VectorData(2, new double[]{0, 1}));
    dataset.add(new VectorData(3, new double[]{3, 4}));
    dataset.add(new VectorData(4, new double[]{5, 5}));
    
    // 查询对象: (0,0), 半径: 1.5
    VectorData queryObject = new VectorData(-1, new double[]{0, 0});
    RangeQuery query = new RangeQuery(queryObject, 1.5);
    
    // 执行查询
    List<MetricSpaceData> results = LinearScanRangeQuery.execute(
        dataset, query, MinkowskiDistance.L2);
    
    // 验证结果
    // 预期结果: (0,0), (1,0), (0,1)
    // 理由:
    //   d((0,0), (0,0)) = 0 <= 1.5 ✓
    //   d((0,0), (1,0)) = 1 <= 1.5 ✓
    //   d((0,0), (0,1)) = 1 <= 1.5 ✓
    //   d((0,0), (3,4)) = 5 > 1.5 ✗
    //   d((0,0), (5,5)) = 7.07 > 1.5 ✗
    
    assertEquals(3, results.size());
    
    // 显示详细结果
    System.out.println("范围查询测试:");
    System.out.println("查询对象: " + queryObject);
    System.out.println("查询半径: 1.5");
    System.out.println("预期结果数量: 3");
    System.out.println("实际结果数量: " + results.size());
    System.out.println("查询结果:");
    for (MetricSpaceData data : results) {
        double dist = MinkowskiDistance.L2.getDistance(queryObject, data);
        System.out.println("  " + data + ", distance = " + dist);
    }
}

@Test
public void testRangeQuery_EmptyResult() {
    // 测试空结果情况
    List<VectorData> dataset = new ArrayList<>();
    dataset.add(new VectorData(0, new double[]{10, 10}));
    dataset.add(new VectorData(1, new double[]{20, 20}));
    
    VectorData queryObject = new VectorData(-1, new double[]{0, 0}));
    RangeQuery query = new RangeQuery(queryObject, 1.0);
    
    List<MetricSpaceData> results = LinearScanRangeQuery.execute(
        dataset, query, MinkowskiDistance.L2);
    
    assertEquals(0, results.size());
}

@Test
public void testRangeQuery_RealData() {
    // 使用实际数据集测试
    List<VectorData> dataset = VectorDataReader.readFromFile(
        "data/vector/Uniform 5-d vector.txt", 1000);
    
    // 选择第一个向量作为查询对象
    VectorData queryObject = dataset.get(0);
    RangeQuery query = new RangeQuery(queryObject, 0.1);
    
    List<MetricSpaceData> results = LinearScanRangeQuery.execute(
        dataset, query, MinkowskiDistance.L2);
    
    System.out.println("实际数据集范围查询:");
    System.out.println("数据集大小: " + dataset.size());
    System.out.println("查询半径: 0.1");
    System.out.println("结果数量: " + results.size());
}
```

**具体实现步骤**:

- [ ] 创建RangeQuery.java类
- [ ] 创建LinearScanRangeQuery.java类
- [ ] 实现线性扫描算法
- [ ] 添加统计信息（距离计算次数等）
- [ ] 创建测试类RangeQueryTest.java
- [ ] 编写至少3个测试用例

#### 1.2 k近邻查询 (kNN Query)

**理论基础** (来自full.md 3.1节):

```
kNN查询定义:
  给定查询对象q和整数k，找出距离q最近的k个数据对象
  
算法原理:
  1. 线性扫描所有数据，计算与查询对象的距离
  2. 维护一个大小为k的最大堆，保存当前找到的k个最近邻
  3. 对于每个数据对象:
     - 如果堆未满，直接加入
     - 如果堆已满且当前距离小于堆顶，替换堆顶并调整堆
```

**KNNQuery类设计**:

```java
public class KNNQuery {
    private final MetricSpaceData queryObject;
    private final int k;
    
    public KNNQuery(MetricSpaceData queryObject, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k必须大于0");
        }
        this.queryObject = queryObject;
        this.k = k;
    }
    
    public MetricSpaceData getQueryObject() {
        return queryObject;
    }
    
    public int getK() {
        return k;
    }
}
```

**KNNResult类设计**:

```java
public class KNNResult implements Comparable<KNNResult> {
    private final MetricSpaceData data;
    private final double distance;
    
    public KNNResult(MetricSpaceData data, double distance) {
        this.data = data;
        this.distance = distance;
    }
    
    public MetricSpaceData getData() {
        return data;
    }
    
    public double getDistance() {
        return distance;
    }
    
    @Override
    public int compareTo(KNNResult other) {
        // 按距离降序排列（用于最大堆）
        return Double.compare(other.distance, this.distance);
    }
}
```

**LinearScanKNNQuery类设计**:

```java
public class LinearScanKNNQuery {
    /**
     * 执行kNN查询
     * @param dataset 数据集
     * @param query 查询对象
     * @param metric 距离函数
     * @return kNN结果列表（按距离升序排列）
     */
    public static List<KNNResult> execute(
        List<? extends MetricSpaceData> dataset,
        KNNQuery query,
        MetricFunction metric) {
        
        // 使用优先队列（最大堆）维护k个最近邻
        PriorityQueue<KNNResult> maxHeap = new PriorityQueue<>();
        int distanceCalculations = 0;
        
        MetricSpaceData queryObject = query.getQueryObject();
        int k = query.getK();
        
        // 线性扫描所有数据
        for (MetricSpaceData data : dataset) {
            double distance = metric.getDistance(queryObject, data);
            distanceCalculations++;
            
            if (maxHeap.size() < k) {
                // 堆未满，直接加入
                maxHeap.offer(new KNNResult(data, distance));
            } else if (distance < maxHeap.peek().getDistance()) {
                // 当前距离小于堆顶，替换堆顶
                maxHeap.poll();
                maxHeap.offer(new KNNResult(data, distance));
            }
        }
        
        // 将结果转为列表并按距离升序排列
        List<KNNResult> results = new ArrayList<>(maxHeap);
        results.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
        
        System.out.println("kNN查询统计:");
        System.out.println("  数据集大小: " + dataset.size());
        System.out.println("  k值: " + k);
        System.out.println("  距离计算次数: " + distanceCalculations);
        
        return results;
    }
}
```

**测试用例设计**:

```java
@Test
public void testKNNQuery_SimpleCase() {
    // 创建测试数据集
    List<VectorData> dataset = new ArrayList<>();
    dataset.add(new VectorData(0, new double[]{0, 0}));
    dataset.add(new VectorData(1, new double[]{1, 0}));
    dataset.add(new VectorData(2, new double[]{0, 1}));
    dataset.add(new VectorData(3, new double[]{3, 4}));
    dataset.add(new VectorData(4, new double[]{5, 5}));
    
    // 查询对象: (0,0), k=3
    VectorData queryObject = new VectorData(-1, new double[]{0, 0});
    KNNQuery query = new KNNQuery(queryObject, 3);
    
    // 执行查询
    List<KNNResult> results = LinearScanKNNQuery.execute(
        dataset, query, MinkowskiDistance.L2);
    
    // 验证结果
    // 预期结果（按距离升序）:
    //   (0,0), distance = 0
    //   (1,0), distance = 1
    //   (0,1), distance = 1
    
    assertEquals(3, results.size());
    assertEquals(0.0, results.get(0).getDistance(), 0.0001);
    assertEquals(1.0, results.get(1).getDistance(), 0.0001);
    assertEquals(1.0, results.get(2).getDistance(), 0.0001);
    
    // 显示结果
    System.out.println("kNN查询测试:");
    System.out.println("查询对象: " + queryObject);
    System.out.println("k值: 3");
    System.out.println("查询结果（按距离升序）:");
    for (int i = 0; i < results.size(); i++) {
        KNNResult result = results.get(i);
        System.out.println("  第" + (i+1) + "近邻: " + result.getData() + 
                         ", distance = " + result.getDistance());
    }
}

@Test
public void testKNNQuery_KLargerThanDataset() {
    // 测试k大于数据集大小的情况
    List<VectorData> dataset = new ArrayList<>();
    dataset.add(new VectorData(0, new double[]{0, 0}));
    dataset.add(new VectorData(1, new double[]{1, 0}));
    
    VectorData queryObject = new VectorData(-1, new double[]{0, 0});
    KNNQuery query = new KNNQuery(queryObject, 5);
    
    List<KNNResult> results = LinearScanKNNQuery.execute(
        dataset, query, MinkowskiDistance.L2);
    
    // 结果数量应该等于数据集大小
    assertEquals(2, results.size());
}
```

**具体实现步骤**:

- [ ] 创建KNNQuery.java类
- [ ] 创建KNNResult.java类
- [ ] 创建LinearScanKNNQuery.java类
- [ ] 实现基于优先队列的kNN算法
- [ ] 创建测试类KNNQueryTest.java
- [ ] 编写至少3个测试用例

#### 1.3 多样化k近邻查询 (dkNN Query)

**理论基础**:

```
dkNN查询定义:
  在kNN的基础上，保证结果的多样性，避免返回过于相似的对象
  
算法原理:
  1. 首先执行常规kNN查询，得到候选集
  2. 从候选集中选择多样性最大的k个对象
  3. 多样性度量：选中对象之间的最小距离最大化
  
贪心算法:
  1. 从kNN结果中选择距离查询对象最近的作为第一个结果
  2. 迭代选择下一个对象：
     - 计算候选对象到已选对象的最小距离
     - 选择最小距离最大的候选对象
```

**DKNNQuery类设计**:

```java
public class DKNNQuery {
    private final MetricSpaceData queryObject;
    private final int k;
    private final double diversityWeight;  // 多样性权重(0-1)
    
    public DKNNQuery(MetricSpaceData queryObject, int k, double diversityWeight) {
        if (k <= 0) {
            throw new IllegalArgumentException("k必须大于0");
        }
        if (diversityWeight < 0 || diversityWeight > 1) {
            throw new IllegalArgumentException("多样性权重必须在[0,1]之间");
        }
        this.queryObject = queryObject;
        this.k = k;
        this.diversityWeight = diversityWeight;
    }
    
    // getters...
}
```

**LinearScanDKNNQuery类设计**:

```java
public class LinearScanDKNNQuery {
    /**
     * 执行dkNN查询
     * @param dataset 数据集
     * @param query 查询对象
     * @param metric 距离函数
     * @return dkNN结果列表
     */
    public static List<KNNResult> execute(
        List<? extends MetricSpaceData> dataset,
        DKNNQuery query,
        MetricFunction metric) {
        
        // 第一步: 执行kNN查询，获取更多候选
        int candidateSize = query.getK() * 10;  // 候选集大小是k的10倍
        KNNQuery knnQuery = new KNNQuery(query.getQueryObject(), candidateSize);
        List<KNNResult> candidates = LinearScanKNNQuery.execute(dataset, knnQuery, metric);
        
        // 第二步: 从候选集中贪心选择多样化的k个对象
        List<KNNResult> results = new ArrayList<>();
        Set<MetricSpaceData> selected = new HashSet<>();
        
        if (!candidates.isEmpty()) {
            // 选择第一个（距离查询对象最近的）
            KNNResult first = candidates.get(0);
            results.add(first);
            selected.add(first.getData());
        }
        
        // 贪心选择剩余的k-1个对象
        while (results.size() < query.getK() && results.size() < candidates.size()) {
            MetricSpaceData bestCandidate = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            
            for (KNNResult candidate : candidates) {
                if (selected.contains(candidate.getData())) {
                    continue;
                }
                
                // 计算该候选对象的得分
                // 得分 = (1-w) * (-距离查询对象) + w * (到已选对象的最小距离)
                double distToQuery = candidate.getDistance();
                double minDistToSelected = computeMinDistanceToSelected(
                    candidate.getData(), selected, metric);
                
                double score = (1 - query.getDiversityWeight()) * (-distToQuery) +
                              query.getDiversityWeight() * minDistToSelected;
                
                if (score > bestScore) {
                    bestScore = score;
                    bestCandidate = candidate.getData();
                }
            }
            
            if (bestCandidate != null) {
                double dist = metric.getDistance(query.getQueryObject(), bestCandidate);
                results.add(new KNNResult(bestCandidate, dist));
                selected.add(bestCandidate);
            } else {
                break;
            }
        }
        
        System.out.println("dkNN查询统计:");
        System.out.println("  k值: " + query.getK());
        System.out.println("  多样性权重: " + query.getDiversityWeight());
        System.out.println("  候选集大小: " + candidates.size());
        System.out.println("  结果数量: " + results.size());
        
        return results;
    }
    
    private static double computeMinDistanceToSelected(
        MetricSpaceData candidate,
        Set<MetricSpaceData> selected,
        MetricFunction metric) {
        
        double minDist = Double.MAX_VALUE;
        for (MetricSpaceData s : selected) {
            double dist = metric.getDistance(candidate, s);
            minDist = Math.min(minDist, dist);
        }
        return minDist;
    }
}
```

**测试用例设计**:

```java
@Test
public void testDKNNQuery_CompareWithKNN() {
    // 创建聚类数据：两个聚类，每个聚类3个点
    List<VectorData> dataset = new ArrayList<>();
    // 聚类1: 围绕(0,0)
    dataset.add(new VectorData(0, new double[]{0, 0}));
    dataset.add(new VectorData(1, new double[]{0.1, 0}));
    dataset.add(new VectorData(2, new double[]{0, 0.1}));
    // 聚类2: 围绕(10,10)
    dataset.add(new VectorData(3, new double[]{10, 10}));
    dataset.add(new VectorData(4, new double[]{10.1, 10}));
    dataset.add(new VectorData(5, new double[]{10, 10.1}));
    
    VectorData queryObject = new VectorData(-1, new double[]{0, 0});
    
    // 执行kNN查询（k=3）
    KNNQuery knnQuery = new KNNQuery(queryObject, 3);
    List<KNNResult> knnResults = LinearScanKNNQuery.execute(
        dataset, knnQuery, MinkowskiDistance.L2);
    
    // 执行dkNN查询（k=3, 高多样性权重）
    DKNNQuery dknnQuery = new DKNNQuery(queryObject, 3, 0.8);
    List<KNNResult> dknnResults = LinearScanDKNNQuery.execute(
        dataset, dknnQuery, MinkowskiDistance.L2);
    
    System.out.println("kNN vs dkNN 对比测试:");
    System.out.println("\nkNN结果（可能都来自聚类1）:");
    for (KNNResult result : knnResults) {
        System.out.println("  " + result.getData() + ", distance = " + result.getDistance());
    }
    
    System.out.println("\ndkNN结果（应该包含两个聚类的点）:");
    for (KNNResult result : dknnResults) {
        System.out.println("  " + result.getData() + ", distance = " + result.getDistance());
    }
    
    // 验证dkNN结果的多样性
    // 计算dkNN结果中点之间的平均距离
    double avgDistDKNN = computeAverageDistance(dknnResults, MinkowskiDistance.L2);
    double avgDistKNN = computeAverageDistance(knnResults, MinkowskiDistance.L2);
    
    System.out.println("\n多样性分析:");
    System.out.println("  kNN结果平均距离: " + avgDistKNN);
    System.out.println("  dkNN结果平均距离: " + avgDistDKNN);
    
    // dkNN的平均距离应该大于kNN（更多样化）
    assertTrue("dkNN应该更多样化", avgDistDKNN > avgDistKNN);
}

private double computeAverageDistance(List<KNNResult> results, MetricFunction metric) {
    if (results.size() <= 1) return 0;
    
    double sum = 0;
    int count = 0;
    for (int i = 0; i < results.size(); i++) {
        for (int j = i + 1; j < results.size(); j++) {
            sum += metric.getDistance(
                results.get(i).getData(),
                results.get(j).getData());
            count++;
        }
    }
    return sum / count;
}
```

**具体实现步骤**:

- [ ] 创建DKNNQuery.java类
- [ ] 创建LinearScanDKNNQuery.java类
- [ ] 实现贪心多样化选择算法
- [ ] 创建测试类DKNNQueryTest.java
- [ ] 编写对比测试用例（kNN vs dkNN）

### 任务2: 实现Pivot Table索引 (优先级: 最高)

#### 2.1 Pivot Table结构设计

**理论基础** (来自full.md 3.3节):

```
Pivot Table定义:
  - 选择k个支撑点(pivots)
  - 预计算并存储每个数据对象到每个支撑点的距离
  - 构建距离矩阵: distance[i][j] = d(data[i], pivot[j])
  
基于三角不等式的剪枝:
  排除规则: |d(p, q) - d(p, s)| > r => d(q, s) > r
  包含规则: d(p, q) + d(p, s) ≤ r => d(q, s) ≤ r
```

**PivotTable类设计**:

```java
public class PivotTable {
    private List<MetricSpaceData> pivots;      // 支撑点列表
    private List<MetricSpaceData> dataset;     // 数据集
    private double[][] distanceTable;           // 距离表 [dataIndex][pivotIndex]
    private MetricFunction metric;              // 距离函数
    
    // 统计信息
    private int distanceCalculations;           // 距离计算次数
    
    /**
     * 构建Pivot Table
     * @param dataset 数据集
     * @param numPivots 支撑点数量
     * @param metric 距离函数
     * @param pivotSelectionMethod 支撑点选择方法
     */
    public PivotTable(List<? extends MetricSpaceData> dataset,
                     int numPivots,
                     MetricFunction metric,
                     PivotSelectionMethod pivotSelectionMethod) {
        this.dataset = new ArrayList<>(dataset);
        this.metric = metric;
        this.distanceCalculations = 0;
        
        // 选择支撑点
        this.pivots = selectPivots(dataset, numPivots, pivotSelectionMethod);
        
        // 构建距离表
        buildDistanceTable();
    }
    
    /**
     * 构建距离表
     */
    private void buildDistanceTable() {
        int n = dataset.size();
        int k = pivots.size();
        
        distanceTable = new double[n][k];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k; j++) {
                distanceTable[i][j] = metric.getDistance(dataset.get(i), pivots.get(j));
                distanceCalculations++;
            }
        }
        
        System.out.println("Pivot Table构建完成:");
        System.out.println("  数据集大小: " + n);
        System.out.println("  支撑点数量: " + k);
        System.out.println("  距离计算次数: " + distanceCalculations);
    }
    
    /**
     * 获取数据到支撑点的距离
     */
    public double getDistanceToP ivot(int dataIndex, int pivotIndex) {
        return distanceTable[dataIndex][pivotIndex];
    }
    
    // getters...
}
```

#### 2.2 支撑点选择算法

**PivotSelectionMethod枚举**:

```java
public enum PivotSelectionMethod {
    RANDOM,      // 随机选择
    FFT,         // Farthest-First Traversal
    CENTER,      // 选择距离数据集中心最近的点
    BORDER       // 选择距离数据集边界最近的点
}
```

**PivotSelector类设计**:

```java
public class PivotSelector {
    /**
     * 选择支撑点
     */
    public static List<MetricSpaceData> selectPivots(
        List<? extends MetricSpaceData> dataset,
        int numPivots,
        PivotSelectionMethod method,
        MetricFunction metric) {
        
        switch (method) {
            case RANDOM:
                return selectRandomPivots(dataset, numPivots);
            case FFT:
                return selectFFTPivots(dataset, numPivots, metric);
            case CENTER:
                return selectCenterPivots(dataset, numPivots, metric);
            case BORDER:
                return selectBorderPivots(dataset, numPivots, metric);
            default:
                throw new IllegalArgumentException("不支持的支撑点选择方法: " + method);
        }
    }
    
    /**
     * 随机选择支撑点
     */
    private static List<MetricSpaceData> selectRandomPivots(
        List<? extends MetricSpaceData> dataset, int numPivots) {
        
        List<MetricSpaceData> pivots = new ArrayList<>();
        List<MetricSpaceData> candidates = new ArrayList<>(dataset);
        Random random = new Random(42);  // 固定种子以保证可重复性
        
        for (int i = 0; i < numPivots && !candidates.isEmpty(); i++) {
            int index = random.nextInt(candidates.size());
            pivots.add(candidates.remove(index));
        }
        
        return pivots;
    }
    
    /**
     * FFT (Farthest-First Traversal) 算法选择支撑点
     * 每次选择距离已选支撑点最远的点
     */
    private static List<MetricSpaceData> selectFFTPivots(
        List<? extends MetricSpaceData> dataset,
        int numPivots,
        MetricFunction metric) {
        
        List<MetricSpaceData> pivots = new ArrayList<>();
        
        // 第一个支撑点：随机选择
        Random random = new Random(42);
        pivots.add(dataset.get(random.nextInt(dataset.size())));
        
        // 后续支撑点：选择距离已选支撑点最远的点
        while (pivots.size() < numPivots) {
            double maxMinDist = -1;
            MetricSpaceData farthest = null;
            
            for (MetricSpaceData candidate : dataset) {
                // 跳过已选的支撑点
                if (pivots.contains(candidate)) continue;
                
                // 计算candidate到已选支撑点的最小距离
                double minDist = Double.MAX_VALUE;
                for (MetricSpaceData pivot : pivots) {
                    double dist = metric.getDistance(candidate, pivot);
                    minDist = Math.min(minDist, dist);
                }
                
                // 选择最小距离最大的candidate
                if (minDist > maxMinDist) {
                    maxMinDist = minDist;
                    farthest = candidate;
                }
            }
            
            if (farthest != null) {
                pivots.add(farthest);
            } else {
                break;
            }
        }
        
        System.out.println("FFT选择的支撑点:");
        for (int i = 0; i < pivots.size(); i++) {
            System.out.println("  Pivot " + i + ": " + pivots.get(i));
        }
        
        return pivots;
    }
    
    // 其他选择方法的实现...
}
```

#### 2.3 基于Pivot Table的范围查询

**PivotTableRangeQuery类设计**:

```java
public class PivotTableRangeQuery {
    /**
     * 执行基于Pivot Table的范围查询
     */
    public static List<MetricSpaceData> execute(
        PivotTable pivotTable,
        RangeQuery query) {
        
        List<MetricSpaceData> results = new ArrayList<>();
        int distanceCalculations = 0;
        int pruned = 0;
        int included = 0;
        
        MetricSpaceData queryObject = query.getQueryObject();
        double radius = query.getRadius();
        MetricFunction metric = pivotTable.getMetric();
        
        // 预计算查询对象到所有支撑点的距离
        int numPivots = pivotTable.getPivots().size();
        double[] queryToPivotDist = new double[numPivots];
        for (int j = 0; j < numPivots; j++) {
            queryToPivotDist[j] = metric.getDistance(
                queryObject, pivotTable.getPivots().get(j));
            distanceCalculations++;
        }
        
        // 对每个数据对象，尝试使用三角不等式剪枝
        List<MetricSpaceData> dataset = pivotTable.getDataset();
        for (int i = 0; i < dataset.size(); i++) {
            MetricSpaceData data = dataset.get(i);
            boolean canPrune = false;
            boolean canInclude = false;
            
            // 尝试使用每个支撑点进行剪枝
            for (int j = 0; j < numPivots; j++) {
                double dps = pivotTable.getDistanceToP ivot(i, j);  // d(pivot, data)
                double dpq = queryToPivotDist[j];                  // d(pivot, query)
                
                // 排除规则: |d(p,q) - d(p,s)| > r => d(q,s) > r
                if (Math.abs(dpq - dps) > radius) {
                    canPrune = true;
                    pruned++;
                    break;
                }
                
                // 包含规则: d(p,q) + d(p,s) <= r => d(q,s) <= r
                if (dpq + dps <= radius) {
                    canInclude = true;
                    included++;
                    break;
                }
            }
            
            if (canInclude) {
                // 可以直接判定为查询结果
                results.add(data);
            } else if (!canPrune) {
                // 无法剪枝，需要计算实际距离
                double distance = metric.getDistance(queryObject, data);
                distanceCalculations++;
                
                if (distance <= radius) {
                    results.add(data);
                }
            }
        }
        
        System.out.println("Pivot Table范围查询统计:");
        System.out.println("  数据集大小: " + dataset.size());
        System.out.println("  支撑点数量: " + numPivots);
        System.out.println("  距离计算次数: " + distanceCalculations);
        System.out.println("  剪枝数量: " + pruned);
        System.out.println("  直接包含数量: " + included);
        System.out.println("  剪枝率: " + (100.0 * pruned / dataset.size()) + "%");
        System.out.println("  结果数量: " + results.size());
        
        return results;
    }
}
```

**测试用例设计**:

```java
@Test
public void testPivotTableBuild() {
    // 测试Pivot Table构建
    List<VectorData> dataset = VectorDataReader.readFromFile(
        "data/vector/Uniform 5-d vector.txt", 1000);
    
    PivotTable pivotTable = new PivotTable(
        dataset, 10, MinkowskiDistance.L2, PivotSelectionMethod.FFT);
    
    assertEquals(1000, pivotTable.getDataset().size());
    assertEquals(10, pivotTable.getPivots().size());
    
    // 验证距离表的正确性
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 10; j++) {
            double storedDist = pivotTable.getDistanceToP ivot(i, j);
            double actualDist = MinkowskiDistance.L2.getDistance(
                dataset.get(i), pivotTable.getPivots().get(j));
            assertEquals(storedDist, actualDist, 0.0001);
        }
    }
}

@Test
public void testPivotTableRangeQuery_Correctness() {
    // 验证Pivot Table范围查询的正确性
    List<VectorData> dataset = VectorDataReader.readFromFile(
        "data/vector/Uniform 5-d vector.txt", 1000);
    
    PivotTable pivotTable = new PivotTable(
        dataset, 10, MinkowskiDistance.L2, PivotSelectionMethod.FFT);
    
    VectorData queryObject = dataset.get(0);
    RangeQuery query = new RangeQuery(queryObject, 0.1);
    
    // 执行Pivot Table范围查询
    List<MetricSpaceData> ptResults = PivotTableRangeQuery.execute(pivotTable, query);
    
    // 执行线性扫描范围查询（作为ground truth）
    List<MetricSpaceData> lsResults = LinearScanRangeQuery.execute(
        dataset, query, MinkowskiDistance.L2);
    
    // 验证结果一致性
    assertEquals(lsResults.size(), ptResults.size());
    
    // 排序后逐个比较
    sortByDataId(ptResults);
    sortByDataId(lsResults);
    
    for (int i = 0; i < ptResults.size(); i++) {
        assertEquals(lsResults.get(i), ptResults.get(i));
    }
    
    System.out.println("Pivot Table范围查询正确性验证通过!");
}

@Test
public void testPivotTableRangeQuery_Performance() {
    // 测试Pivot Table的性能提升
    List<VectorData> dataset = VectorDataReader.readFromFile(
        "data/vector/Uniform 5-d vector.txt", 5000);
    
    int[] pivotCounts = {5, 10, 15, 20};
    
    for (int numPivots : pivotCounts) {
        PivotTable pivotTable = new PivotTable(
            dataset, numPivots, MinkowskiDistance.L2, PivotSelectionMethod.FFT);
        
        VectorData queryObject = dataset.get(0);
        RangeQuery query = new RangeQuery(queryObject, 0.1);
        
        // 执行查询并记录时间
        long startTime = System.currentTimeMillis();
        List<MetricSpaceData> results = PivotTableRangeQuery.execute(pivotTable, query);
        long endTime = System.currentTimeMillis();
        
        System.out.println("支撑点数量: " + numPivots);
        System.out.println("  查询时间: " + (endTime - startTime) + " ms");
        System.out.println("  结果数量: " + results.size());
        System.out.println();
    }
}
```

**具体实现步骤**:

- [ ] 创建PivotTable.java类
- [ ] 创建PivotSelectionMethod.java枚举
- [ ] 创建PivotSelector.java类
- [ ] 实现至少2种支撑点选择算法（RANDOM和FFT）
- [ ] 实现Pivot Table构建过程
- [ ] 创建PivotTableRangeQuery.java类
- [ ] 实现基于三角不等式的剪枝查询
- [ ] 创建测试类PivotTableTest.java
- [ ] 验证查询正确性（与线性扫描对比）

#### 2.4 基于Pivot Table的kNN查询

**PivotTableKNNQuery类设计**:

```java
public class PivotTableKNNQuery {
    /**
     * 执行基于Pivot Table的kNN查询
     */
    public static List<KNNResult> execute(
        PivotTable pivotTable,
        KNNQuery query) {
        
        // 使用优先队列维护k个最近邻
        PriorityQueue<KNNResult> maxHeap = new PriorityQueue<>();
        int distanceCalculations = 0;
        int pruned = 0;
        
        MetricSpaceData queryObject = query.getQueryObject();
        int k = query.getK();
        MetricFunction metric = pivotTable.getMetric();
        
        // 预计算查询对象到所有支撑点的距离
        int numPivots = pivotTable.getPivots().size();
        double[] queryToPivotDist = new double[numPivots];
        for (int j = 0; j < numPivots; j++) {
            queryToPivotDist[j] = metric.getDistance(
                queryObject, pivotTable.getPivots().get(j));
            distanceCalculations++;
        }
        
        // 动态查询半径（当前第k近邻的距离）
        double currentRadius = Double.MAX_VALUE;
        
        // 对每个数据对象进行处理
        List<MetricSpaceData> dataset = pivotTable.getDataset();
        for (int i = 0; i < dataset.size(); i++) {
            MetricSpaceData data = dataset.get(i);
            boolean canPrune = false;
            
            // 尝试使用支撑点剪枝
            for (int j = 0; j < numPivots; j++) {
                double dps = pivotTable.getDistanceToP ivot(i, j);
                double dpq = queryToPivotDist[j];
                
                // 使用当前查询半径进行剪枝
                if (Math.abs(dpq - dps) > currentRadius) {
                    canPrune = true;
                    pruned++;
                    break;
                }
            }
            
            if (!canPrune) {
                // 计算实际距离
                double distance = metric.getDistance(queryObject, data);
                distanceCalculations++;
                
                if (maxHeap.size() < k) {
                    maxHeap.offer(new KNNResult(data, distance));
                    if (maxHeap.size() == k) {
                        currentRadius = maxHeap.peek().getDistance();
                    }
                } else if (distance < currentRadius) {
                    maxHeap.poll();
                    maxHeap.offer(new KNNResult(data, distance));
                    currentRadius = maxHeap.peek().getDistance();
                }
            }
        }
        
        // 转为列表并排序
        List<KNNResult> results = new ArrayList<>(maxHeap);
        results.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
        
        System.out.println("Pivot Table kNN查询统计:");
        System.out.println("  数据集大小: " + dataset.size());
        System.out.println("  k值: " + k);
        System.out.println("  支撑点数量: " + numPivots);
        System.out.println("  距离计算次数: " + distanceCalculations);
        System.out.println("  剪枝数量: " + pruned);
        System.out.println("  剪枝率: " + (100.0 * pruned / dataset.size()) + "%");
        
        return results;
    }
}
```

**具体实现步骤**:

- [ ] 创建PivotTableKNNQuery.java类
- [ ] 实现动态查询半径的kNN算法
- [ ] 创建测试类，验证正确性
- [ ] 对比与线性扫描的性能差异

### 任务3: 性能分析实验 (优先级: 中)

#### 3.1 实验设计

**实验目标**:

1. 探索不同支撑点选择策略对性能的影响
2. 分析支撑点数量对性能的影响
3. 对比线性扫描和Pivot Table的性能

**实验变量**:

- 独立变量:
  - 数据集大小: 1000, 5000, 10000, 50000
  - 支撑点数量: 5, 10, 15, 20, 25, 30
  - 支撑点选择策略: RANDOM, FFT
  - 查询半径: 0.05, 0.1, 0.15, 0.2
  - 数据类型: Vector, Protein

- 因变量:
  - 查询时间 (ms)
  - 距离计算次数
  - 剪枝率 (%)
  - 索引构建时间 (ms)

**PerformanceExperiment类设计**:

```java
public class PerformanceExperiment {
    public static void main(String[] args) {
        // 实验1: 支撑点数量对性能的影响
        experimentPivotCount();
        
        // 实验2: 支撑点选择策略对性能的影响
        experimentPivotSelection();
        
        // 实验3: 线性扫描 vs Pivot Table性能对比
        experimentLinearVsPivotTable();
    }
    
    private static void experimentPivotCount() {
        System.out.println("=== 实验1: 支撑点数量对性能的影响 ===\n");
        
        // 加载数据集
        List<VectorData> dataset = VectorDataReader.readFromFile(
            "data/vector/Uniform 5-d vector.txt", 10000);
        
        int[] pivotCounts = {5, 10, 15, 20, 25, 30};
        double[] radii = {0.05, 0.1, 0.15};
        
        // 结果记录
        List<ExperimentResult> results = new ArrayList<>();
        
        for (int numPivots : pivotCounts) {
            for (double radius : radii) {
                // 构建Pivot Table
                long buildStart = System.currentTimeMillis();
                PivotTable pivotTable = new PivotTable(
                    dataset, numPivots, MinkowskiDistance.L2,
                    PivotSelectionMethod.FFT);
                long buildTime = System.currentTimeMillis() - buildStart;
                
                // 执行多个查询取平均
                int numQueries = 10;
                long totalQueryTime = 0;
                long totalDistCalc = 0;
                long totalPruned = 0;
                
                for (int i = 0; i < numQueries; i++) {
                    VectorData queryObject = dataset.get(i * 100);
                    RangeQuery query = new RangeQuery(queryObject, radius);
                    
                    long queryStart = System.currentTimeMillis();
                    List<MetricSpaceData> queryResults = 
                        PivotTableRangeQuery.execute(pivotTable, query);
                    long queryTime = System.currentTimeMillis() - queryStart;
                    
                    totalQueryTime += queryTime;
                    // 记录其他统计信息...
                }
                
                ExperimentResult result = new ExperimentResult();
                result.numPivots = numPivots;
                result.radius = radius;
                result.buildTime = buildTime;
                result.avgQueryTime = totalQueryTime / numQueries;
                result.avgDistCalc = totalDistCalc / numQueries;
                result.avgPruneRate = (100.0 * totalPruned) / (dataset.size() * numQueries);
                
                results.add(result);
                
                System.out.println("支撑点数量: " + numPivots + 
                                 ", 查询半径: " + radius);
                System.out.println("  构建时间: " + buildTime + " ms");
                System.out.println("  平均查询时间: " + result.avgQueryTime + " ms");
                System.out.println("  平均距离计算次数: " + result.avgDistCalc);
                System.out.println("  平均剪枝率: " + result.avgPruneRate + "%");
                System.out.println();
            }
        }
        
        // 保存结果到CSV文件
        saveResultsToCSV(results, "results/pivot_count_experiment.csv");
    }
    
    // 其他实验方法...
}
```

**具体实现步骤**:

- [ ] 创建PerformanceExperiment.java类
- [ ] 创建ExperimentResult.java类（记录实验结果）
- [ ] 实现3个核心实验
- [ ] 将结果保存为CSV文件
- [ ] 使用Python/Excel绘制性能图表

#### 3.2 数据分析和可视化

**使用Python绘制性能曲线**:

```python
import pandas as pd
import matplotlib.pyplot as plt

# 读取实验结果
df = pd.read_csv('results/pivot_count_experiment.csv')

# 绘制支撑点数量 vs 查询时间
plt.figure(figsize=(10, 6))
for radius in df['radius'].unique():
    data = df[df['radius'] == radius]
    plt.plot(data['numPivots'], data['avgQueryTime'], 
             marker='o', label=f'r={radius}')

plt.xlabel('支撑点数量')
plt.ylabel('平均查询时间 (ms)')
plt.title('支撑点数量对查询性能的影响')
plt.legend()
plt.grid(True)
plt.savefig('figures/pivot_count_vs_query_time.png')
plt.show()

# 绘制支撑点数量 vs 剪枝率
plt.figure(figsize=(10, 6))
for radius in df['radius'].unique():
    data = df[df['radius'] == radius]
    plt.plot(data['numPivots'], data['avgPruneRate'],
             marker='o', label=f'r={radius}')

plt.xlabel('支撑点数量')
plt.ylabel('平均剪枝率 (%)')
plt.title('支撑点数量对剪枝率的影响')
plt.legend()
plt.grid(True)
plt.savefig('figures/pivot_count_vs_prune_rate.png')
plt.show()
```

**具体实现步骤**:

- [ ] 创建Python脚本进行数据分析
- [ ] 生成多种性能对比图表
- [ ] 将图表保存到results/figures目录
- [ ] 在报告中引用这些图表

### 任务4: 实验报告撰写 (优先级: 高)

#### 4.1 报告结构

根据Assignment2-Requirements.md，报告应包含以下章节：

```
1. 引言
  1.1 研究背景与意义
  1.2 任务回顾与目标
    1.2.1 Assignment 1 工作简述
    1.2.2 Assignment 2 核心目标
  1.3 实验环境
  1.4 报告结构

2. 相似性查询与索引系统设计
  2.1 系统架构扩展
    2.1.1 在原有架构中集成查询与索引模块
    2.1.2 更新后的系统模块划分图
  2.2 线性扫描查询算法设计
    2.2.1 范围查询 (Range Query) 算法原理
    2.2.2 k近邻查询 (kNN) 算法原理
    2.2.3 多样化k近邻查询 (dkNN) 算法原理
  2.3 Pivot Table 索引设计
    2.3.1 核心思想：基于三角不等式的剪枝
    2.3.2 数据结构设计与构建流程
    2.3.3 基于Pivot Table的范围查询流程
    2.3.4 基于Pivot Table的kNN查询流程

3. 核心功能实现
  3.1 线性扫描查询实现
    3.1.1 范围查询 (Range Query) 核心代码
    3.1.2 k近邻查询 (kNN) 核心代码
    3.1.3 多样化k近邻查询 (dkNN) 核心代码
  3.2 Pivot Table 索引实现
    3.2.1 Pivot Table 构建核心代码
    3.2.2 基于Pivot Table的范围查询实现代码
    3.2.3 基于Pivot Table的kNN查询实现代码

4. 功能正确性验证
  4.1 测试环境与数据集
  4.2 线性扫描查询正确性验证
    4.2.1 范围查询 (Range Query) 测试
    4.2.2 k近邻查询 (kNN) 测试
    4.2.3 多样化k近邻查询 (dkNN) 测试
  4.3 Pivot Table 索引正确性验证
    4.3.1 构建过程验证
    4.3.2 基于索引的查询结果与线性扫描结果对比

5. 性能分析与探索
  5.1 性能评估指标定义
  5.2 实验设计
    5.2.1 数据集与查询集选择
    5.2.2 支撑点选择策略
  5.3 支撑点数量对性能的影响分析
  5.4 支撑点选择策略对性能的影响分析
  5.5 查询性能对比：线性扫描 vs. Pivot Table索引 (范围查询与kNN)

6. 总结与展望
  6.1 工作总结
  6.2 系统不足与改进方向
```

#### 4.2 各章节写作要点

**第1章 引言**:

- [ ] 1.2.1节: 简要回顾Assignment 1完成的工作（抽象类设计、两种数据类型实现）
- [ ] 1.2.2节: 明确Assignment 2的核心目标（相似性查询和索引）

**第2章 系统设计**:

- [ ] 2.2节: 详细说明三种查询算法的原理，配合伪代码
- [ ] 2.3节: 重点说明Pivot Table的核心思想
  - 三角不等式的应用
  - 排除规则和包含规则的数学推导
  - 配合图示说明剪枝原理

**第3章 核心功能实现**:

- [ ] 展示关键代码片段
- [ ] 说明实现中的关键技术点

**第4章 功能正确性验证**:

- [ ] 设计易于验证的测试用例
- [ ] 对比线性扫描和Pivot Table的查询结果
- [ ] 展示测试输出

**第5章 性能分析**:

- [ ] 5.3节: 展示支撑点数量的影响
  - 包含性能曲线图
  - 分析最优支撑点数量
- [ ] 5.4节: 对比不同支撑点选择策略
  - RANDOM vs FFT
  - 分析各自的优缺点
- [ ] 5.5节: 线性扫描 vs Pivot Table
  - 展示加速比
  - 分析不同场景下的性能差异

**LaTeX报告模板**:

```latex
% 第5.3节示例
\subsection{支撑点数量对性能的影响分析}

本实验固定数据集大小为10,000，支撑点选择策略为FFT，
在不同的查询半径下，测试不同支撑点数量对查询性能的影响。

\subsubsection{实验设置}
\begin{itemize}
    \item 数据集：Uniform 5-d vector，规模10,000
    \item 支撑点数量：5, 10, 15, 20, 25, 30
    \item 查询半径：0.05, 0.1, 0.15
    \item 查询次数：每个配置执行10次取平均
\end{itemize}

\subsubsection{实验结果}

图\ref{fig:pivot_count_query_time}展示了支撑点数量对平均查询时间的影响。
可以观察到：

\begin{enumerate}
    \item 随着支撑点数量的增加，查询时间先下降后上升
    \item 在支撑点数量为15-20时达到最优性能
    \item 支撑点过少时剪枝效果不足，支撑点过多时剪枝判断开销增大
\end{enumerate}

\begin{figure}[htbp]
    \centering
    \includegraphics[width=0.8\textwidth]{figures/pivot_count_vs_query_time.png}
    \caption{支撑点数量对查询时间的影响}
    \label{fig:pivot_count_query_time}
\end{figure}

表\ref{tab:pivot_count_results}给出了详细的实验数据。

\begin{table}[htbp]
    \centering
    \caption{不同支撑点数量的性能对比}
    \label{tab:pivot_count_results}
    \begin{tabular}{cccccc}
        \hline
        支撑点 & 半径 & 查询时间 & 距离计算 & 剪枝率 & 结果数 \\
        数量   &      & (ms)     & 次数     & (\%)   & 量     \\
        \hline
        5      & 0.1  & 145.3    & 8523     & 14.8   & 125    \\
        10     & 0.1  & 98.7     & 5234     & 47.7   & 125    \\
        15     & 0.1  & 76.2     & 3456     & 65.4   & 125    \\
        20     & 0.1  & 72.5     & 2987     & 70.1   & 125    \\
        25     & 0.1  & 75.8     & 2876     & 71.2   & 125    \\
        30     & 0.1  & 81.3     & 2745     & 72.6   & 125    \\
        \hline
    \end{tabular}
\end{table}

\subsubsection{结果分析}

从实验结果可以得出以下结论：
\begin{enumerate}
    \item \textbf{存在最优支撑点数量}：...
    \item \textbf{查询半径的影响}：...
    \item \textbf{性能提升显著}：相比线性扫描，使用20个支撑点可以减少约70\%的距离计算...
\end{enumerate}
```

## 三、验收标准

### 3.1 功能完整性

- [ ] 实现了范围查询、kNN查询、dkNN查询
- [ ] 实现了Pivot Table索引
- [ ] 实现了至少2种支撑点选择算法
- [ ] 所有功能经过充分测试

### 3.2 正确性验证

- [ ] 每个查询功能至少3个测试用例
- [ ] Pivot Table查询结果与线性扫描结果一致
- [ ] 测试用例设计合理，易于验证

### 3.3 性能分析

- [ ] 完成3个核心性能实验
- [ ] 生成清晰的性能对比图表
- [ ] 对实验结果进行深入分析

### 3.4 报告质量

- [ ] 结构完整，符合要求
- [ ] 理论分析透彻，配合图表
- [ ] 性能分析数据真实可靠
- [ ] LaTeX编译无错误

## 四、顺序规划

1. 实现线性扫描查询（范围、kNN、dkNN）
2. 实现Pivot Table索引和查询
3. 功能测试和正确性验证
4. 性能实验和数据分析
5. 撰写实验报告

## 五、参考资料

### 5.1 理论参考

- full.md 第3.1节: 相似性查询
- full.md 第3.3节: Pivot Table
- full.md 第5章: 支撑点选择

### 5.2 代码参考

- UMAD-OriginalCode/src/main/java/index/search/: 查询实现参考
- UMAD-OriginalCode/src/main/java/index/structure/PivotTable.java: 索引实现参考
