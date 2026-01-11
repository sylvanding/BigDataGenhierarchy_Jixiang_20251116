package index.tree.ghtree;

import core.MetricSpaceData;
import core.MetricFunction;
import index.tree.*;
import index.tree.common.TreeConfig;
import query.KNNResult;

import java.util.*;

/**
 * GH树（Generalized Hyperplane Tree）
 * 
 * GH树是一种基于超平面划分的度量空间索引结构。
 * 在每个内部节点选择两个支撑点p1和p2，通过比较数据到两个支撑点的距离来划分空间。
 *
 * 划分规则：
 * - 如果 d(x, p1) < d(x, p2)，则 x 分配到左子树
 * - 如果 d(x, p1) >= d(x, p2)，则 x 分配到右子树
 *
 * 剪枝规则（范围查询）：
 * - 如果 d(q, p1) - d(q, p2) > 2r，可以剪枝左子树
 * - 如果 d(q, p2) - d(q, p1) > 2r，可以剪枝右子树
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class GHTree extends TreeIndex {

    /**
     * 构造GH树
     *
     * @param config 树配置
     */
    public GHTree(TreeConfig config) {
        super(config);
    }

    /**
     * 使用默认配置构造GH树
     */
    public GHTree() {
        super(new TreeConfig());
    }

    @Override
    public String getIndexName() {
        return "GH-Tree (超平面树)";
    }

    /**
     * 递归构建GH树
     *
     * 算法步骤：
     * 1. 判断是否应该创建叶子节点
     * 2. 选择两个支撑点(p1, p2)
     * 3. 根据到两个支撑点的距离划分数据
     * 4. 递归构建左右子树
     */
    @Override
    protected TreeNode buildTreeRecursive(List<MetricSpaceData> data, int depth) {
        // 判断是否创建叶子节点
        if (heightController.canCreateLeaf(depth, data.size())) {
            if (config.isVerbose()) {
                System.out.printf("  深度%d: 创建叶子节点，数据量=%d%n", depth, data.size());
            }
            return new LeafNode(data, depth);
        }

        // 选择两个支撑点
        MetricSpaceData[] pivots = selectTwoPivots(data);
        MetricSpaceData pivot1 = pivots[0];
        MetricSpaceData pivot2 = pivots[1];

        if (config.isVerbose()) {
            System.out.printf("  深度%d: 选择支撑点 p1=%s, p2=%s%n", depth, pivot1.toString(),
                    pivot2.toString());
        }

        // 划分数据
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

        if (config.isVerbose()) {
            System.out.printf("  深度%d: 数据划分完成，左=%d，右=%d%n", depth, leftData.size(),
                    rightData.size());
        }

        // 处理极端情况：所有数据都分到一侧
        if (leftData.isEmpty() || rightData.isEmpty()) {
            if (config.isVerbose()) {
                System.out.printf("  警告：深度%d处数据划分不均，强制创建叶子节点%n", depth);
            }
            return new LeafNode(data, depth);
        }

        // 递归构建子树
        TreeNode leftChild = buildTreeRecursive(leftData, depth + 1);
        TreeNode rightChild = buildTreeRecursive(rightData, depth + 1);

        return new GHInternalNode(pivot1, pivot2, leftChild, rightChild, depth);
    }

    /**
     * 选择两个支撑点
     *
     * 根据配置的策略选择：
     * - RANDOM: 随机选择两个不同的点
     * - FFT: 使用Farthest-First Traversal
     * - MAX_SPREAD: 选择距离最远的两个点
     *
     * @param data 数据集
     * @return 包含两个支撑点的数组
     */
    private MetricSpaceData[] selectTwoPivots(List<MetricSpaceData> data) {
        switch (config.getPivotStrategy()) {
            case FFT:
                return selectFFTPivots(data);
            case MAX_SPREAD:
                return selectMaxSpreadPivots(data);
            case RANDOM:
            default:
                return selectRandomPivots(data);
        }
    }

    /**
     * 随机选择策略
     */
    private MetricSpaceData[] selectRandomPivots(List<MetricSpaceData> data) {
        int idx1 = random.nextInt(data.size());
        int idx2;
        do {
            idx2 = random.nextInt(data.size());
        } while (idx2 == idx1 && data.size() > 1);

        return new MetricSpaceData[] {data.get(idx1), data.get(idx2)};
    }

    /**
     * FFT（Farthest-First Traversal）策略
     *
     * 1. 随机选择第一个pivot
     * 2. 选择离第一个pivot最远的点作为第二个pivot
     */
    private MetricSpaceData[] selectFFTPivots(List<MetricSpaceData> data) {
        // 随机选择第一个pivot
        MetricSpaceData pivot1 = data.get(random.nextInt(data.size()));

        // 找到离pivot1最远的点作为pivot2
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
     *
     * 通过采样找到数据集中距离最远的两个点
     */
    private MetricSpaceData[] selectMaxSpreadPivots(List<MetricSpaceData> data) {
        // 采样以减少计算量
        int sampleSize = Math.min(50, data.size());
        List<MetricSpaceData> sample = sampleData(data, sampleSize);

        MetricSpaceData pivot1 = null;
        MetricSpaceData pivot2 = null;
        double maxDist = -1;

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
    private List<MetricSpaceData> sampleData(List<MetricSpaceData> data, int sampleSize) {
        if (data.size() <= sampleSize) {
            return data;
        }

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

    // ========== 范围查询实现 ==========

    /**
     * 范围查询
     *
     * 找出所有与查询对象距离不超过radius的数据对象。
     * 使用GH树的剪枝规则减少距离计算次数。
     */
    @Override
    public List<MetricSpaceData> rangeQuery(MetricSpaceData queryObject, double radius) {
        List<MetricSpaceData> results = new ArrayList<>();

        if (root == null) {
            return results;
        }

        if (config.isVerbose()) {
            System.out.println("\n" + "-".repeat(50));
            System.out.println("GH树范围查询");
            System.out.println("-".repeat(50));
            System.out.println("查询对象: " + queryObject);
            System.out.println("查询半径: " + radius);
        }

        rangeQueryRecursive(root, queryObject, radius, results);

        if (config.isVerbose()) {
            System.out.println("查询结果数量: " + results.size());
            System.out.println("距离计算次数: " + queryDistanceComputations);
            System.out.println("节点访问次数: " + nodeAccesses);
            System.out.println("-".repeat(50));
        }

        return results;
    }

    /**
     * 递归执行范围查询
     *
     * GH树剪枝规则：
     * - 如果 d(q, p1) - d(q, p2) > 2r，可以跳过左子树
     * - 如果 d(q, p2) - d(q, p1) > 2r，可以跳过右子树
     */
    private void rangeQueryRecursive(TreeNode node, MetricSpaceData queryObject, double radius,
            List<MetricSpaceData> results) {
        nodeAccesses++;

        if (node.isLeaf()) {
            // 叶子节点：检查所有数据
            LeafNode leaf = (LeafNode) node;
            for (MetricSpaceData data : leaf.getData()) {
                double dist = metric.getDistance(queryObject, data);
                queryDistanceComputations++;

                if (dist <= radius) {
                    results.add(data);
                }
            }
        } else {
            // 内部节点：应用剪枝规则
            GHInternalNode internal = (GHInternalNode) node;
            MetricSpaceData p1 = internal.getPivot1();
            MetricSpaceData p2 = internal.getPivot2();

            double d1 = metric.getDistance(queryObject, p1);
            double d2 = metric.getDistance(queryObject, p2);
            queryDistanceComputations += 2;

            // 检查是否需要访问左子树
            // 剪枝条件：d1 - d2 > 2r
            if (!(d1 - d2 > 2 * radius)) {
                rangeQueryRecursive(internal.getLeftChild(), queryObject, radius, results);
            } else if (config.isVerbose()) {
                System.out.printf("    剪枝左子树 (d1-d2=%.3f > 2r=%.3f)%n", d1 - d2, 2 * radius);
            }

            // 检查是否需要访问右子树
            // 剪枝条件：d2 - d1 > 2r
            if (!(d2 - d1 > 2 * radius)) {
                rangeQueryRecursive(internal.getRightChild(), queryObject, radius, results);
            } else if (config.isVerbose()) {
                System.out.printf("    剪枝右子树 (d2-d1=%.3f > 2r=%.3f)%n", d2 - d1, 2 * radius);
            }
        }
    }

    // ========== kNN查询实现 ==========

    /**
     * k近邻查询
     *
     * 找出与查询对象最近的k个数据对象。
     * 使用优先队列维护当前k个最近邻，动态更新查询半径。
     */
    @Override
    public List<MetricSpaceData> knnQuery(MetricSpaceData queryObject, int k) {
        // 使用最大堆维护k个最近邻
        PriorityQueue<KNNResult> knnHeap =
                new PriorityQueue<>((a, b) -> Double.compare(b.getDistance(), a.getDistance()));

        if (root == null || k <= 0) {
            return new ArrayList<>();
        }

        if (config.isVerbose()) {
            System.out.println("\n" + "-".repeat(50));
            System.out.println("GH树kNN查询");
            System.out.println("-".repeat(50));
            System.out.println("查询对象: " + queryObject);
            System.out.println("k = " + k);
        }

        // 初始查询半径为无穷大
        double[] currentRadius = {Double.MAX_VALUE};

        knnQueryRecursive(root, queryObject, k, knnHeap, currentRadius);

        // 提取结果
        List<MetricSpaceData> results = new ArrayList<>();
        List<KNNResult> tempList = new ArrayList<>(knnHeap);
        tempList.sort(Comparator.comparingDouble(KNNResult::getDistance));

        for (KNNResult result : tempList) {
            results.add(result.getData());
        }

        if (config.isVerbose()) {
            System.out.println("查询结果数量: " + results.size());
            System.out.println("距离计算次数: " + queryDistanceComputations);
            System.out.println("节点访问次数: " + nodeAccesses);
            System.out.println("-".repeat(50));
        }

        return results;
    }

    /**
     * 递归执行kNN查询
     */
    private void knnQueryRecursive(TreeNode node, MetricSpaceData queryObject, int k,
            PriorityQueue<KNNResult> knnHeap, double[] currentRadius) {
        nodeAccesses++;

        if (node.isLeaf()) {
            // 叶子节点：检查所有数据
            LeafNode leaf = (LeafNode) node;
            for (MetricSpaceData data : leaf.getData()) {
                double dist = metric.getDistance(queryObject, data);
                queryDistanceComputations++;

                if (knnHeap.size() < k) {
                    knnHeap.offer(new KNNResult(data, dist));
                    if (knnHeap.size() == k) {
                        currentRadius[0] = knnHeap.peek().getDistance();
                    }
                } else if (dist < currentRadius[0]) {
                    knnHeap.poll();
                    knnHeap.offer(new KNNResult(data, dist));
                    currentRadius[0] = knnHeap.peek().getDistance();
                }
            }
        } else {
            // 内部节点：应用剪枝规则
            GHInternalNode internal = (GHInternalNode) node;
            MetricSpaceData p1 = internal.getPivot1();
            MetricSpaceData p2 = internal.getPivot2();

            double d1 = metric.getDistance(queryObject, p1);
            double d2 = metric.getDistance(queryObject, p2);
            queryDistanceComputations += 2;

            // 决定访问顺序：优先访问更可能包含近邻的子树
            boolean leftFirst = d1 <= d2;

            if (leftFirst) {
                // 先访问左子树
                if (!(d1 - d2 > 2 * currentRadius[0])) {
                    knnQueryRecursive(internal.getLeftChild(), queryObject, k, knnHeap,
                            currentRadius);
                }
                // 再访问右子树
                if (!(d2 - d1 > 2 * currentRadius[0])) {
                    knnQueryRecursive(internal.getRightChild(), queryObject, k, knnHeap,
                            currentRadius);
                }
            } else {
                // 先访问右子树
                if (!(d2 - d1 > 2 * currentRadius[0])) {
                    knnQueryRecursive(internal.getRightChild(), queryObject, k, knnHeap,
                            currentRadius);
                }
                // 再访问左子树
                if (!(d1 - d2 > 2 * currentRadius[0])) {
                    knnQueryRecursive(internal.getLeftChild(), queryObject, k, knnHeap,
                            currentRadius);
                }
            }
        }
    }

    @Override
    protected String getNodeDescription(TreeNode node) {
        if (node.isLeaf()) {
            return String.format("叶子节点 [数据量=%d]", node.size());
        } else {
            GHInternalNode internal = (GHInternalNode) node;
            return String.format("GH内部节点 [左=%d, 右=%d, p1=ID%d, p2=ID%d]", internal.getLeftSize(),
                    internal.getRightSize(), internal.getPivot1().getDataId(),
                    internal.getPivot2().getDataId());
        }
    }
}

