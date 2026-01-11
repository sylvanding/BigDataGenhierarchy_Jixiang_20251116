package index.tree.mvptree;

import core.MetricSpaceData;
import core.MetricFunction;
import index.tree.*;
import index.tree.common.TreeConfig;
import index.tree.common.MultiPivotSelector;
import query.KNNResult;

import java.util.*;

/**
 * 3-pivot MVP树（Multiple Vantage Point Tree）
 *
 * MVP树是VP树的多pivot扩展，使用3个支撑点进行嵌套球形划分。
 * 每个内部节点选择3个pivot，根据数据到各pivot的距离将空间划分为8个子区域。
 *
 * 划分规则：
 * - 计算数据到3个pivot的距离
 * - 使用中位数作为各维度的划分边界
 * - 根据距离与中位数的比较，确定数据所属的子区域
 *
 * 剪枝规则（范围查询）：
 * - 若查询球与子区域在任一pivot维度不相交，可排除该子区域
 * - 若子区域完全包含在查询球内，可批量返回
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class MVPTree extends TreeIndex {

    /** pivot数量 */
    private static final int NUM_PIVOTS = 3;

    /** 子节点数量 */
    private static final int NUM_CHILDREN = 8;

    /** 多pivot选择器 */
    private MultiPivotSelector pivotSelector;

    /**
     * 构造MVP树
     *
     * @param config 树配置
     * @param pivotSelector 多pivot选择器
     */
    public MVPTree(TreeConfig config, MultiPivotSelector pivotSelector) {
        super(config);
        this.pivotSelector = pivotSelector;
    }

    /**
     * 使用默认配置构造MVP树
     */
    public MVPTree() {
        super(new TreeConfig());
        this.pivotSelector = new MultiPivotSelector(MultiPivotSelector.SelectionStrategy.FFT);
    }

    /**
     * 构造MVP树（使用默认pivot选择器）
     */
    public MVPTree(TreeConfig config) {
        super(config);
        // 根据config的策略设置pivotSelector
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
        return "3-pivot MVP-Tree (多优势点树)";
    }

    /**
     * 递归构建MVP树
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

        // 数据不足以选择3个pivot
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

        // 从数据中移除pivot
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

        // 计算所有数据到各pivot的距离
        double[][] distances = new double[remainingData.size()][NUM_PIVOTS];
        for (int i = 0; i < remainingData.size(); i++) {
            for (int j = 0; j < NUM_PIVOTS; j++) {
                distances[i][j] = metric.getDistance(remainingData.get(i), nodePivots.get(j));
                buildDistanceComputations++;
            }
        }

        // 计算划分半径（各pivot的中位数距离）
        double[] splitRadius = computeSplitRadius(distances);

        // 将数据分配到8个子集
        @SuppressWarnings("unchecked")
        List<MetricSpaceData>[] partitions = new ArrayList[NUM_CHILDREN];
        @SuppressWarnings("unchecked")
        List<double[]>[] partitionDistances = new ArrayList[NUM_CHILDREN];

        for (int i = 0; i < NUM_CHILDREN; i++) {
            partitions[i] = new ArrayList<>();
            partitionDistances[i] = new ArrayList<>();
        }

        for (int i = 0; i < remainingData.size(); i++) {
            int childIdx = 0;
            if (distances[i][0] > splitRadius[0])
                childIdx |= 1;
            if (distances[i][1] > splitRadius[1])
                childIdx |= 2;
            if (distances[i][2] > splitRadius[2])
                childIdx |= 4;

            partitions[childIdx].add(remainingData.get(i));
            partitionDistances[childIdx].add(distances[i]);
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

        // 计算每个子集的距离范围
        double[][] lowerBound = new double[NUM_CHILDREN][NUM_PIVOTS];
        double[][] upperBound = new double[NUM_CHILDREN][NUM_PIVOTS];

        for (int i = 0; i < NUM_CHILDREN; i++) {
            Arrays.fill(lowerBound[i], Double.MAX_VALUE);
            Arrays.fill(upperBound[i], Double.MIN_VALUE);

            for (double[] dist : partitionDistances[i]) {
                for (int j = 0; j < NUM_PIVOTS; j++) {
                    lowerBound[i][j] = Math.min(lowerBound[i][j], dist[j]);
                    upperBound[i][j] = Math.max(upperBound[i][j], dist[j]);
                }
            }

            // 如果子集为空，设置默认值
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

        return new MVPInternalNode(nodePivots, childNodes, splitRadius, lowerBound, upperBound,
                depth);
    }

    /**
     * 计算划分半径（各维度的中位数）
     */
    private double[] computeSplitRadius(double[][] distances) {
        double[] splitRadius = new double[NUM_PIVOTS];

        for (int j = 0; j < NUM_PIVOTS; j++) {
            double[] col = new double[distances.length];
            for (int i = 0; i < distances.length; i++) {
                col[i] = distances[i][j];
            }
            Arrays.sort(col);
            splitRadius[j] = col[col.length / 2];
        }

        return splitRadius;
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
            System.out.println("MVP树范围查询");
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
            MVPInternalNode internal = (MVPInternalNode) node;

            // 计算查询对象到各pivot的距离
            double[] distToQuery = new double[NUM_PIVOTS];
            for (int i = 0; i < NUM_PIVOTS; i++) {
                distToQuery[i] = metric.getDistance(queryObject, internal.getPivot(i));
                queryDistanceComputations++;

                // 检查pivot是否在查询范围内
                if (distToQuery[i] <= radius) {
                    results.add(internal.getPivot(i));
                }
            }

            // 检查每个子树
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (internal.getChild(i) == null) {
                    continue;
                }

                if (internal.shouldVisitChild(i, distToQuery, radius)) {
                    // 检查是否完全包含
                    if (internal.isChildFullyContained(i, distToQuery, radius)) {
                        // 批量添加子树中的所有数据
                        collectAllData(internal.getChild(i), results);
                    } else {
                        rangeQueryRecursive(internal.getChild(i), queryObject, radius, results);
                    }
                }
            }
        }
    }

    /**
     * 收集子树中的所有数据（用于包含规则）
     */
    private void collectAllData(TreeNode node, List<MetricSpaceData> results) {
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            results.addAll(((LeafNode) node).getData());
        } else {
            MVPInternalNode internal = (MVPInternalNode) node;
            // 添加pivot
            results.addAll(internal.getPivots());
            // 递归收集子树
            for (int i = 0; i < NUM_CHILDREN; i++) {
                collectAllData(internal.getChild(i), results);
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
            System.out.println("MVP树kNN查询");
            System.out.println("-".repeat(50));
            System.out.println("查询对象: " + queryObject);
            System.out.println("k = " + k);
        }

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
            MVPInternalNode internal = (MVPInternalNode) node;

            // 计算查询对象到各pivot的距离
            double[] distToQuery = new double[NUM_PIVOTS];
            for (int i = 0; i < NUM_PIVOTS; i++) {
                distToQuery[i] = metric.getDistance(queryObject, internal.getPivot(i));
                queryDistanceComputations++;

                // 检查pivot是否应加入kNN
                updateKNNHeap(knnHeap, internal.getPivot(i), distToQuery[i], k, currentRadius);
            }

            // 按优先级访问子树（离查询点更近的子树优先）
            List<int[]> childOrder = new ArrayList<>();
            for (int i = 0; i < NUM_CHILDREN; i++) {
                if (internal.getChild(i) != null) {
                    // 计算子树到查询点的最小可能距离
                    double minDist = 0;
                    for (int j = 0; j < NUM_PIVOTS; j++) {
                        double L = internal.getLowerBound(i, j);
                        double U = internal.getUpperBound(i, j);
                        if (distToQuery[j] < L) {
                            minDist = Math.max(minDist, L - distToQuery[j]);
                        } else if (distToQuery[j] > U) {
                            minDist = Math.max(minDist, distToQuery[j] - U);
                        }
                    }
                    childOrder.add(new int[] {i, (int) (minDist * 1000)});
                }
            }
            childOrder.sort(Comparator.comparingInt(a -> a[1]));

            // 访问子树
            for (int[] entry : childOrder) {
                int i = entry[0];
                if (internal.shouldVisitChild(i, distToQuery, currentRadius[0])) {
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
            MVPInternalNode internal = (MVPInternalNode) node;
            StringBuilder sb = new StringBuilder();
            sb.append("MVP内部节点 [pivots=");
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
            MVPInternalNode internal = (MVPInternalNode) node;
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
