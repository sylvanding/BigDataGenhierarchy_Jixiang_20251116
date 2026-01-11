package index.tree.vptree;

import core.MetricSpaceData;
import core.MetricFunction;
import index.tree.*;
import index.tree.common.TreeConfig;
import index.tree.vptree.VPInternalNode.DistanceRange;
import query.KNNResult;

import java.util.*;

/**
 * VP树（Vantage Point Tree）
 *
 * VP树是一种基于球形划分的度量空间索引结构。
 * 在每个内部节点选择一个支撑点(vantage point)，根据数据到支撑点的距离将空间划分为
 * 内球（距离较近）和外球（距离较远）两个区域。
 *
 * 划分规则：
 * - 计算所有数据到支撑点的距离
 * - 使用中位数作为划分边界
 * - d(x, p) <= median → 内球
 * - d(x, p) > median → 外球
 *
 * 剪枝规则（范围查询）：
 * - 如果 d(q, p) + r < L，可以剪枝该子树（查询球完全在子区域外侧）
 * - 如果 d(q, p) - r > U，可以剪枝该子树（查询球完全在子区域内侧）
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class VPTree extends TreeIndex {

    /**
     * 构造VP树
     *
     * @param config 树配置
     */
    public VPTree(TreeConfig config) {
        super(config);
    }

    /**
     * 使用默认配置构造VP树
     */
    public VPTree() {
        super(new TreeConfig());
    }

    @Override
    public String getIndexName() {
        return "VP-Tree (优势点树)";
    }

    /**
     * 递归构建VP树
     *
     * 算法步骤：
     * 1. 判断是否应该创建叶子节点
     * 2. 选择一个支撑点
     * 3. 计算所有数据到支撑点的距离
     * 4. 按距离排序，使用中位数划分
     * 5. 记录每个子区域的距离范围
     * 6. 递归构建子树
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

        // 选择支撑点
        MetricSpaceData pivot = selectPivot(data);

        if (config.isVerbose()) {
            System.out.printf("  深度%d: 选择支撑点 %s%n", depth, pivot.toString());
        }

        // 计算距离并封装
        List<DataWithDistance> dataWithDist = new ArrayList<>();
        for (MetricSpaceData obj : data) {
            if (obj.getDataId() == pivot.getDataId())
                continue; // 跳过pivot本身

            double dist = metric.getDistance(obj, pivot);
            buildDistanceComputations++;
            dataWithDist.add(new DataWithDistance(obj, dist));
        }

        // 按距离排序
        dataWithDist.sort(Comparator.comparingDouble(d -> d.distance));

        // 计算中位数并划分
        int midpoint = dataWithDist.size() / 2;
        if (midpoint == 0) {
            midpoint = 1;
        }

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
            if (config.isVerbose()) {
                System.out.printf("  警告：深度%d处数据划分不均，强制创建叶子节点%n", depth);
            }
            return new LeafNode(data, depth);
        }

        // 计算距离范围
        double innerLower = dataWithDist.get(0).distance;
        double innerUpper = dataWithDist.get(midpoint - 1).distance;
        double outerLower = dataWithDist.get(midpoint).distance;
        double outerUpper = dataWithDist.get(dataWithDist.size() - 1).distance;
        double medianDistance = (innerUpper + outerLower) / 2.0;

        DistanceRange innerRange = new DistanceRange(innerLower, innerUpper);
        DistanceRange outerRange = new DistanceRange(outerLower, outerUpper);

        if (config.isVerbose()) {
            System.out.printf("  深度%d: 数据划分完成，内球=%d%s，外球=%d%s，中位数=%.4f%n", depth, innerData.size(),
                    innerRange, outerData.size(), outerRange, medianDistance);
        }

        // 递归构建子树
        TreeNode innerChild = buildTreeRecursive(innerData, depth + 1);
        TreeNode outerChild = buildTreeRecursive(outerData, depth + 1);

        return new VPInternalNode(pivot, innerChild, outerChild, innerRange, outerRange,
                medianDistance, depth);
    }

    /**
     * 选择支撑点
     *
     * 根据配置的策略选择：
     * - RANDOM: 随机选择
     * - FFT: 使用Farthest-First Traversal
     * - MAX_SPREAD: 选择离参考点最远的点
     */
    private MetricSpaceData selectPivot(List<MetricSpaceData> data) {
        switch (config.getPivotStrategy()) {
            case FFT:
                return selectFFTPivot(data);
            case MAX_SPREAD:
                return selectMaxSpreadPivot(data);
            case RANDOM:
            default:
                return data.get(random.nextInt(data.size()));
        }
    }

    /**
     * FFT策略选择支撑点
     *
     * 随机选择一个参考点，然后选择离参考点最远的点作为支撑点
     */
    private MetricSpaceData selectFFTPivot(List<MetricSpaceData> data) {
        // 随机选择参考点
        MetricSpaceData ref = data.get(random.nextInt(data.size()));

        // 找到离参考点最远的点
        MetricSpaceData pivot = null;
        double maxDist = -1;

        // 采样以减少计算
        int sampleSize = Math.min(50, data.size());
        for (int i = 0; i < sampleSize; i++) {
            MetricSpaceData candidate = data.get(random.nextInt(data.size()));
            double dist = metric.getDistance(ref, candidate);
            buildDistanceComputations++;

            if (dist > maxDist) {
                maxDist = dist;
                pivot = candidate;
            }
        }

        return pivot != null ? pivot : ref;
    }

    /**
     * 最大分散度策略选择支撑点
     *
     * 选择数据集中方差最大的点
     */
    private MetricSpaceData selectMaxSpreadPivot(List<MetricSpaceData> data) {
        // 简化实现：使用FFT
        return selectFFTPivot(data);
    }

    /**
     * 辅助类：数据及其到支撑点的距离
     */
    private static class DataWithDistance {
        final MetricSpaceData data;
        final double distance;

        DataWithDistance(MetricSpaceData data, double distance) {
            this.data = data;
            this.distance = distance;
        }
    }

    // ========== 范围查询实现 ==========

    /**
     * 范围查询
     *
     * 找出所有与查询对象距离不超过radius的数据对象。
     * 使用VP树的剪枝规则减少距离计算次数。
     */
    @Override
    public List<MetricSpaceData> rangeQuery(MetricSpaceData queryObject, double radius) {
        List<MetricSpaceData> results = new ArrayList<>();

        if (root == null) {
            return results;
        }

        if (config.isVerbose()) {
            System.out.println("\n" + "-".repeat(50));
            System.out.println("VP树范围查询");
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
     * VP树剪枝规则：
     * 对于子区域的距离范围 [L, U]：
     * - 如果 d(q, p) + r < L，可以跳过该子树（查询球在子区域外侧）
     * - 如果 d(q, p) - r > U，可以跳过该子树（查询球在子区域内侧）
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
            VPInternalNode internal = (VPInternalNode) node;
            MetricSpaceData pivot = internal.getPivot();

            double dq = metric.getDistance(queryObject, pivot);
            queryDistanceComputations++;

            // 重要：检查pivot本身是否在查询范围内！
            // pivot不在子树中，必须单独检查
            if (dq <= radius) {
                results.add(pivot);
            }

            // 检查是否需要访问内球子树
            DistanceRange innerRange = internal.getInnerRange();
            boolean visitInner =
                    !(dq + radius < innerRange.lower || dq - radius > innerRange.upper);

            if (visitInner) {
                rangeQueryRecursive(internal.getInnerChild(), queryObject, radius, results);
            } else if (config.isVerbose()) {
                System.out.printf("    剪枝内球子树 (dq=%.3f, r=%.3f, range=%s)%n", dq, radius,
                        innerRange);
            }

            // 检查是否需要访问外球子树
            DistanceRange outerRange = internal.getOuterRange();
            boolean visitOuter =
                    !(dq + radius < outerRange.lower || dq - radius > outerRange.upper);

            if (visitOuter) {
                rangeQueryRecursive(internal.getOuterChild(), queryObject, radius, results);
            } else if (config.isVerbose()) {
                System.out.printf("    剪枝外球子树 (dq=%.3f, r=%.3f, range=%s)%n", dq, radius,
                        outerRange);
            }
        }
    }

    // ========== kNN查询实现 ==========

    /**
     * k近邻查询
     *
     * 找出与查询对象最近的k个数据对象。
     */
    @Override
    public List<MetricSpaceData> knnQuery(MetricSpaceData queryObject, int k) {
        PriorityQueue<KNNResult> knnHeap =
                new PriorityQueue<>((a, b) -> Double.compare(b.getDistance(), a.getDistance()));

        if (root == null || k <= 0) {
            return new ArrayList<>();
        }

        if (config.isVerbose()) {
            System.out.println("\n" + "-".repeat(50));
            System.out.println("VP树kNN查询");
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

    /**
     * 递归执行kNN查询
     */
    private void knnQueryRecursive(TreeNode node, MetricSpaceData queryObject, int k,
            PriorityQueue<KNNResult> knnHeap, double[] currentRadius) {
        nodeAccesses++;

        if (node.isLeaf()) {
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
            VPInternalNode internal = (VPInternalNode) node;
            MetricSpaceData pivot = internal.getPivot();

            double dq = metric.getDistance(queryObject, pivot);
            queryDistanceComputations++;

            // 重要：检查pivot本身是否应加入kNN候选！
            // pivot不在子树中，必须单独检查
            if (knnHeap.size() < k) {
                knnHeap.offer(new KNNResult(pivot, dq));
                if (knnHeap.size() == k) {
                    currentRadius[0] = knnHeap.peek().getDistance();
                }
            } else if (dq < currentRadius[0]) {
                knnHeap.poll();
                knnHeap.offer(new KNNResult(pivot, dq));
                currentRadius[0] = knnHeap.peek().getDistance();
            }

            // 决定访问顺序：先访问更可能包含近邻的子树
            double medianDist = internal.getMedianDistance();
            boolean innerFirst = dq <= medianDist;

            if (innerFirst) {
                // 先访问内球
                DistanceRange innerRange = internal.getInnerRange();
                if (!(dq + currentRadius[0] < innerRange.lower
                        || dq - currentRadius[0] > innerRange.upper)) {
                    knnQueryRecursive(internal.getInnerChild(), queryObject, k, knnHeap,
                            currentRadius);
                }
                // 再访问外球
                DistanceRange outerRange = internal.getOuterRange();
                if (!(dq + currentRadius[0] < outerRange.lower
                        || dq - currentRadius[0] > outerRange.upper)) {
                    knnQueryRecursive(internal.getOuterChild(), queryObject, k, knnHeap,
                            currentRadius);
                }
            } else {
                // 先访问外球
                DistanceRange outerRange = internal.getOuterRange();
                if (!(dq + currentRadius[0] < outerRange.lower
                        || dq - currentRadius[0] > outerRange.upper)) {
                    knnQueryRecursive(internal.getOuterChild(), queryObject, k, knnHeap,
                            currentRadius);
                }
                // 再访问内球
                DistanceRange innerRange = internal.getInnerRange();
                if (!(dq + currentRadius[0] < innerRange.lower
                        || dq - currentRadius[0] > innerRange.upper)) {
                    knnQueryRecursive(internal.getInnerChild(), queryObject, k, knnHeap,
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
            VPInternalNode internal = (VPInternalNode) node;
            return String.format("VP内部节点 [内=%d, 外=%d, median=%.3f, pivot=ID%d]",
                    internal.getInnerSize(), internal.getOuterSize(), internal.getMedianDistance(),
                    internal.getPivot().getDataId());
        }
    }
}

