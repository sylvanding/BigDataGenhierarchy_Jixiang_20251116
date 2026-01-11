package index.tree.cght;

import core.MetricSpaceData;
import core.MetricFunction;
import index.tree.*;
import index.tree.common.TreeConfig;
import index.tree.common.MultiPivotSelector;
import query.KNNResult;

import java.util.*;

/**
 * 3-pivot CGHT（Complete Generalized Hyperplane Tree，完全广义超平面树）
 *
 * CGHT是GH树的多pivot扩展，使用3个支撑点进行完全超平面划分。
 * 核心思想是充分利用pivot对之间的距离差信息进行划分。
 *
 * 划分规则：
 * - 计算距离差 delta12 = d(x, p1) - d(x, p2), delta13 = d(x, p1) - d(x, p3)
 * - 根据距离差的符号划分为4个区域
 *
 * 剪枝规则：
 * - 基于GH树剪枝规则的扩展
 * - 若 |delta_q - delta_s| > 2r，则可排除
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class CGHTree extends TreeIndex {

    /** pivot数量 */
    private static final int NUM_PIVOTS = 3;

    /** 子节点数量 */
    private static final int NUM_CHILDREN = 4;

    /** 多pivot选择器 */
    private MultiPivotSelector pivotSelector;

    /**
     * 构造CGHT
     */
    public CGHTree(TreeConfig config, MultiPivotSelector pivotSelector) {
        super(config);
        this.pivotSelector = pivotSelector;
    }

    /**
     * 使用默认配置构造CGHT
     */
    public CGHTree() {
        super(new TreeConfig());
        this.pivotSelector = new MultiPivotSelector(MultiPivotSelector.SelectionStrategy.FFT);
    }

    /**
     * 构造CGHT（使用默认pivot选择器）
     */
    public CGHTree(TreeConfig config) {
        super(config);
        MultiPivotSelector.SelectionStrategy strategy;
        switch (config.getPivotStrategy()) {
            case FFT:
                strategy = MultiPivotSelector.SelectionStrategy.FFT;
                break;
            case MAX_SPREAD:
                strategy = MultiPivotSelector.SelectionStrategy.MAX_SPREAD;
                break;
            case RANDOM:
            default:
                strategy = MultiPivotSelector.SelectionStrategy.RANDOM;
                break;
        }
        this.pivotSelector = config.getRandomSeed() != null
                ? new MultiPivotSelector(strategy, config.getRandomSeed())
                : new MultiPivotSelector(strategy);
    }

    @Override
    public String getIndexName() {
        return "3-pivot CGH-Tree (完全广义超平面树)";
    }

    @Override
    protected TreeNode buildTreeRecursive(List<MetricSpaceData> data, int depth) {
        // 判断是否创建叶子节点
        if (heightController.canCreateLeaf(depth, data.size())) {
            if (config.isVerbose()) {
                System.out.printf("  深度%d: 创建叶子节点，数据量=%d%n", depth, data.size());
            }
            return new LeafNode(data, depth);
        }

        // 数据不足以选择3个pivot
        if (data.size() < NUM_PIVOTS + 1) {
            return new LeafNode(data, depth);
        }

        // 选择3个pivot
        List<MetricSpaceData> nodePivots = pivotSelector.selectPivots(data, metric, NUM_PIVOTS);
        buildDistanceComputations += pivotSelector.getDistanceComputations();

        MetricSpaceData p1 = nodePivots.get(0);
        MetricSpaceData p2 = nodePivots.get(1);
        MetricSpaceData p3 = nodePivots.get(2);

        if (config.isVerbose()) {
            System.out.printf("  深度%d: 选择支撑点 p1=%s, p2=%s, p3=%s%n", depth, p1, p2, p3);
        }

        // 从数据中移除pivot
        Set<Integer> pivotIds = new HashSet<>();
        pivotIds.add(p1.getDataId());
        pivotIds.add(p2.getDataId());
        pivotIds.add(p3.getDataId());

        List<MetricSpaceData> remainingData = new ArrayList<>();
        for (MetricSpaceData d : data) {
            if (!pivotIds.contains(d.getDataId())) {
                remainingData.add(d);
            }
        }

        if (remainingData.isEmpty()) {
            return new LeafNode(data, depth);
        }

        // 计算距离差并分配到子集
        @SuppressWarnings("unchecked")
        List<MetricSpaceData>[] partitions = new ArrayList[NUM_CHILDREN];
        @SuppressWarnings("unchecked")
        List<double[]>[] partitionDeltas = new ArrayList[NUM_CHILDREN];

        for (int i = 0; i < NUM_CHILDREN; i++) {
            partitions[i] = new ArrayList<>();
            partitionDeltas[i] = new ArrayList<>();
        }

        for (MetricSpaceData d : remainingData) {
            double d1 = metric.getDistance(d, p1);
            double d2 = metric.getDistance(d, p2);
            double d3 = metric.getDistance(d, p3);
            buildDistanceComputations += 3;

            double delta12 = d1 - d2;
            double delta13 = d1 - d3;

            int childIdx = 0;
            if (delta12 >= 0)
                childIdx |= 1;
            if (delta13 >= 0)
                childIdx |= 2;

            partitions[childIdx].add(d);
            partitionDeltas[childIdx].add(new double[] {delta12, delta13});
        }

        if (config.isVerbose()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("  深度%d: 数据划分完成，分布=[", depth));
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(partitions[i].size());
            }
            sb.append("]");
            System.out.println(sb.toString());
        }

        // 计算每个子集的delta范围
        double[][] delta12Range = new double[NUM_CHILDREN][2];
        double[][] delta13Range = new double[NUM_CHILDREN][2];

        for (int i = 0; i < NUM_CHILDREN; i++) {
            if (partitionDeltas[i].isEmpty()) {
                delta12Range[i][0] = 0;
                delta12Range[i][1] = 0;
                delta13Range[i][0] = 0;
                delta13Range[i][1] = 0;
            } else {
                double minD12 = Double.MAX_VALUE, maxD12 = Double.MIN_VALUE;
                double minD13 = Double.MAX_VALUE, maxD13 = Double.MIN_VALUE;

                for (double[] deltas : partitionDeltas[i]) {
                    minD12 = Math.min(minD12, deltas[0]);
                    maxD12 = Math.max(maxD12, deltas[0]);
                    minD13 = Math.min(minD13, deltas[1]);
                    maxD13 = Math.max(maxD13, deltas[1]);
                }

                delta12Range[i][0] = minD12;
                delta12Range[i][1] = maxD12;
                delta13Range[i][0] = minD13;
                delta13Range[i][1] = maxD13;
            }
        }

        // 递归构建子树
        List<TreeNode> childNodes = new ArrayList<>();
        for (int i = 0; i < NUM_CHILDREN; i++) {
            if (partitions[i].isEmpty()) {
                childNodes.add(null);
            } else {
                childNodes.add(buildTreeRecursive(partitions[i], depth + 1));
            }
        }

        return new CGHInternalNode(p1, p2, p3, childNodes, delta12Range, delta13Range, depth);
    }

    // ========== 范围查询实现 ==========

    @Override
    public List<MetricSpaceData> rangeQuery(MetricSpaceData queryObject, double radius) {
        List<MetricSpaceData> results = new ArrayList<>();

        if (root == null) {
            return results;
        }

        if (config.isVerbose()) {
            System.out.println("\n" + "-".repeat(50));
            System.out.println("CGH树范围查询");
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

    private void rangeQueryRecursive(TreeNode node, MetricSpaceData queryObject, double radius,
            List<MetricSpaceData> results) {
        nodeAccesses++;

        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            LeafNode leaf = (LeafNode) node;
            for (MetricSpaceData data : leaf.getData()) {
                double dist = metric.getDistance(queryObject, data);
                queryDistanceComputations++;

                if (dist <= radius) {
                    results.add(data);
                }
            }
        } else {
            CGHInternalNode internal = (CGHInternalNode) node;

            // 计算查询对象到各pivot的距离
            double d1 = metric.getDistance(queryObject, internal.getPivot1());
            double d2 = metric.getDistance(queryObject, internal.getPivot2());
            double d3 = metric.getDistance(queryObject, internal.getPivot3());
            queryDistanceComputations += 3;

            // 检查pivot是否在查询范围内
            if (d1 <= radius)
                results.add(internal.getPivot1());
            if (d2 <= radius)
                results.add(internal.getPivot2());
            if (d3 <= radius)
                results.add(internal.getPivot3());

            // 检查每个子树
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (internal.shouldVisitChild(i, d1, d2, d3, radius)) {
                    rangeQueryRecursive(internal.getChild(i), queryObject, radius, results);
                }
            }
        }
    }

    // ========== kNN查询实现 ==========

    @Override
    public List<MetricSpaceData> knnQuery(MetricSpaceData queryObject, int k) {
        PriorityQueue<KNNResult> knnHeap =
                new PriorityQueue<>((a, b) -> Double.compare(b.getDistance(), a.getDistance()));

        if (root == null || k <= 0) {
            return new ArrayList<>();
        }

        if (config.isVerbose()) {
            System.out.println("\n" + "-".repeat(50));
            System.out.println("CGH树kNN查询");
            System.out.println("-".repeat(50));
            System.out.println("查询对象: " + queryObject);
            System.out.println("k = " + k);
        }

        double[] currentRadius = {Double.MAX_VALUE};

        knnQueryRecursive(root, queryObject, k, knnHeap, currentRadius);

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

    private void knnQueryRecursive(TreeNode node, MetricSpaceData queryObject, int k,
            PriorityQueue<KNNResult> knnHeap, double[] currentRadius) {
        nodeAccesses++;

        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            LeafNode leaf = (LeafNode) node;
            for (MetricSpaceData data : leaf.getData()) {
                double dist = metric.getDistance(queryObject, data);
                queryDistanceComputations++;

                updateKNNHeap(knnHeap, data, dist, k, currentRadius);
            }
        } else {
            CGHInternalNode internal = (CGHInternalNode) node;

            double d1 = metric.getDistance(queryObject, internal.getPivot1());
            double d2 = metric.getDistance(queryObject, internal.getPivot2());
            double d3 = metric.getDistance(queryObject, internal.getPivot3());
            queryDistanceComputations += 3;

            // 检查pivot
            updateKNNHeap(knnHeap, internal.getPivot1(), d1, k, currentRadius);
            updateKNNHeap(knnHeap, internal.getPivot2(), d2, k, currentRadius);
            updateKNNHeap(knnHeap, internal.getPivot3(), d3, k, currentRadius);

            // 访问子树
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (internal.shouldVisitChild(i, d1, d2, d3, currentRadius[0])) {
                    knnQueryRecursive(internal.getChild(i), queryObject, k, knnHeap, currentRadius);
                }
            }
        }
    }

    private void updateKNNHeap(PriorityQueue<KNNResult> knnHeap, MetricSpaceData data, double dist,
            int k, double[] currentRadius) {
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

    @Override
    protected String getNodeDescription(TreeNode node) {
        if (node == null) {
            return "null";
        }
        if (node.isLeaf()) {
            return String.format("叶子节点 [数据量=%d]", node.size());
        } else {
            CGHInternalNode internal = (CGHInternalNode) node;
            return String.format("CGH内部节点 [pivots=ID%d,ID%d,ID%d, sizes=[%d,%d,%d,%d]]",
                    internal.getPivot1().getDataId(), internal.getPivot2().getDataId(),
                    internal.getPivot3().getDataId(), internal.getChildSize(0),
                    internal.getChildSize(1), internal.getChildSize(2), internal.getChildSize(3));
        }
    }

    @Override
    protected void printTreeRecursive(TreeNode node, String prefix, boolean isLast) {
        if (node == null) {
            return;
        }

        String connector = isLast ? "└── " : "├── ";
        System.out.println(prefix + connector + getNodeDescription(node));

        if (!node.isLeaf()) {
            CGHInternalNode internal = (CGHInternalNode) node;
            int nonNullCount = 0;
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (internal.getChild(i) != null)
                    nonNullCount++;
            }

            int printed = 0;
            for (int i = 0; i < NUM_CHILDREN; i++) {
                TreeNode child = internal.getChild(i);
                if (child != null) {
                    printed++;
                    String childPrefix = prefix + (isLast ? "    " : "│   ");
                    printTreeRecursive(child, childPrefix, printed == nonNullCount);
                }
            }
        }
    }
}
