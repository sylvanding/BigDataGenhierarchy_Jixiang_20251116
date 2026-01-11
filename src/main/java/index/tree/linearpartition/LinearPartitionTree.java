package index.tree.linearpartition;

import core.MetricSpaceData;
import core.MetricFunction;
import index.tree.*;
import index.tree.common.TreeConfig;
import index.tree.common.MultiPivotSelector;
import query.KNNResult;

import java.util.*;

/**
 * 3-pivot 完全线性划分树
 *
 * 在3维支撑点空间中使用线性超平面进行划分。
 * 数据被映射到支撑点空间：(d1, d2, d3) = (d(x,p1), d(x,p2), d(x,p3))
 * 使用正交划分策略，按各维度中位数划分，产生8个子区域。
 *
 * 划分规则：
 * - 计算数据到各pivot的距离，作为支撑点空间坐标
 * - 按各维度中位数进行正交划分
 *
 * 剪枝规则：
 * - 查询区域在支撑点空间中是边长2r的立方体
 * - 若子区域与查询立方体不相交，可排除
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class LinearPartitionTree extends TreeIndex {

    private static final int NUM_PIVOTS = 3;
    private static final int NUM_CHILDREN = 8;

    private MultiPivotSelector pivotSelector;

    public LinearPartitionTree(TreeConfig config, MultiPivotSelector pivotSelector) {
        super(config);
        this.pivotSelector = pivotSelector;
    }

    public LinearPartitionTree() {
        super(new TreeConfig());
        this.pivotSelector = new MultiPivotSelector(MultiPivotSelector.SelectionStrategy.FFT);
    }

    public LinearPartitionTree(TreeConfig config) {
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
        return "3-pivot Linear-Partition-Tree (完全线性划分树)";
    }

    @Override
    protected TreeNode buildTreeRecursive(List<MetricSpaceData> data, int depth) {
        if (heightController.canCreateLeaf(depth, data.size())) {
            if (config.isVerbose()) {
                System.out.printf("  深度%d: 创建叶子节点，数据量=%d%n", depth, data.size());
            }
            return new LeafNode(data, depth);
        }

        if (data.size() < NUM_PIVOTS + 1) {
            return new LeafNode(data, depth);
        }

        // 选择3个pivot
        List<MetricSpaceData> nodePivots = pivotSelector.selectPivots(data, metric, NUM_PIVOTS);
        buildDistanceComputations += pivotSelector.getDistanceComputations();

        if (config.isVerbose()) {
            System.out.printf("  深度%d: 选择支撑点 p1=%s, p2=%s, p3=%s%n", depth, nodePivots.get(0),
                    nodePivots.get(1), nodePivots.get(2));
        }

        // 移除pivot
        Set<Integer> pivotIds = new HashSet<>();
        for (MetricSpaceData p : nodePivots) {
            pivotIds.add(p.getDataId());
        }

        List<MetricSpaceData> remainingData = new ArrayList<>();
        for (MetricSpaceData d : data) {
            if (!pivotIds.contains(d.getDataId())) {
                remainingData.add(d);
            }
        }

        if (remainingData.isEmpty()) {
            return new LeafNode(data, depth);
        }

        // 将数据映射到支撑点空间
        double[][] coords = new double[remainingData.size()][NUM_PIVOTS];
        for (int i = 0; i < remainingData.size(); i++) {
            for (int j = 0; j < NUM_PIVOTS; j++) {
                coords[i][j] = metric.getDistance(remainingData.get(i), nodePivots.get(j));
                buildDistanceComputations++;
            }
        }

        // 计算划分阈值（各维度中位数）
        double[] splitThreshold = new double[NUM_PIVOTS];
        for (int j = 0; j < NUM_PIVOTS; j++) {
            double[] col = new double[coords.length];
            for (int i = 0; i < coords.length; i++) {
                col[i] = coords[i][j];
            }
            Arrays.sort(col);
            splitThreshold[j] = col[col.length / 2];
        }

        // 将数据分配到8个子集
        @SuppressWarnings("unchecked")
        List<MetricSpaceData>[] partitions = new ArrayList[NUM_CHILDREN];
        @SuppressWarnings("unchecked")
        List<double[]>[] partitionCoords = new ArrayList[NUM_CHILDREN];

        for (int i = 0; i < NUM_CHILDREN; i++) {
            partitions[i] = new ArrayList<>();
            partitionCoords[i] = new ArrayList<>();
        }

        for (int i = 0; i < remainingData.size(); i++) {
            int childIdx = 0;
            if (coords[i][0] > splitThreshold[0])
                childIdx |= 1;
            if (coords[i][1] > splitThreshold[1])
                childIdx |= 2;
            if (coords[i][2] > splitThreshold[2])
                childIdx |= 4;

            partitions[childIdx].add(remainingData.get(i));
            partitionCoords[childIdx].add(coords[i]);
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

        // 计算每个子集的坐标范围
        double[][] lowerBound = new double[NUM_CHILDREN][NUM_PIVOTS];
        double[][] upperBound = new double[NUM_CHILDREN][NUM_PIVOTS];

        for (int i = 0; i < NUM_CHILDREN; i++) {
            Arrays.fill(lowerBound[i], Double.MAX_VALUE);
            Arrays.fill(upperBound[i], Double.MIN_VALUE);

            for (double[] coord : partitionCoords[i]) {
                for (int j = 0; j < NUM_PIVOTS; j++) {
                    lowerBound[i][j] = Math.min(lowerBound[i][j], coord[j]);
                    upperBound[i][j] = Math.max(upperBound[i][j], coord[j]);
                }
            }

            if (partitions[i].isEmpty()) {
                Arrays.fill(lowerBound[i], 0);
                Arrays.fill(upperBound[i], 0);
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

        return new LinearPartitionInternalNode(nodePivots, childNodes, splitThreshold, lowerBound,
                upperBound, depth);
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
            System.out.println("线性划分树范围查询");
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
            LinearPartitionInternalNode internal = (LinearPartitionInternalNode) node;

            // 计算查询对象的支撑点空间坐标
            double[] dq = new double[NUM_PIVOTS];
            for (int i = 0; i < NUM_PIVOTS; i++) {
                dq[i] = metric.getDistance(queryObject, internal.getPivot(i));
                queryDistanceComputations++;

                // 检查pivot是否在查询范围内
                if (dq[i] <= radius) {
                    results.add(internal.getPivot(i));
                }
            }

            // 检查每个子树
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (internal.shouldVisitChild(i, dq, radius)) {
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
            System.out.println("线性划分树kNN查询");
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
            LinearPartitionInternalNode internal = (LinearPartitionInternalNode) node;

            double[] dq = new double[NUM_PIVOTS];
            for (int i = 0; i < NUM_PIVOTS; i++) {
                dq[i] = metric.getDistance(queryObject, internal.getPivot(i));
                queryDistanceComputations++;

                updateKNNHeap(knnHeap, internal.getPivot(i), dq[i], k, currentRadius);
            }

            // 按优先级访问子树
            List<int[]> childOrder = new ArrayList<>();
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (internal.getChild(i) != null) {
                    double minDist = 0;
                    for (int j = 0; j < NUM_PIVOTS; j++) {
                        double L = internal.getLowerBound(i, j);
                        double U = internal.getUpperBound(i, j);
                        if (dq[j] < L) {
                            minDist = Math.max(minDist, L - dq[j]);
                        } else if (dq[j] > U) {
                            minDist = Math.max(minDist, dq[j] - U);
                        }
                    }
                    childOrder.add(new int[] {i, (int) (minDist * 1000)});
                }
            }
            childOrder.sort(Comparator.comparingInt(a -> a[1]));

            for (int[] entry : childOrder) {
                int i = entry[0];
                if (internal.shouldVisitChild(i, dq, currentRadius[0])) {
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
            LinearPartitionInternalNode internal = (LinearPartitionInternalNode) node;
            StringBuilder sb = new StringBuilder();
            sb.append("LP内部节点 [pivots=");
            for (int i = 0; i < NUM_PIVOTS; i++) {
                if (i > 0)
                    sb.append(",");
                sb.append("ID").append(internal.getPivot(i).getDataId());
            }
            sb.append(", sizes=[");
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (i > 0)
                    sb.append(",");
                sb.append(internal.getChildSize(i));
            }
            sb.append("]]");
            return sb.toString();
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
            LinearPartitionInternalNode internal = (LinearPartitionInternalNode) node;
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
