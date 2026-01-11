package index.tree.linearpartition;

import core.MetricSpaceData;
import index.tree.InternalNode;
import index.tree.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 完全线性划分树内部节点
 * 
 * 在3维支撑点空间中使用线性超平面划分。
 *
 * 支撑点空间坐标：(d1, d2, d3) = (d(x,p1), d(x,p2), d(x,p3))
 *
 * 划分方式（正交划分）：
 * - 按d1的中位数划分为2部分
 * - 按d2的中位数划分为2部分
 * - 按d3的中位数划分为2部分
 * 总共2^3=8个子区域
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class LinearPartitionInternalNode extends InternalNode {

    /** pivot数量 */
    public static final int NUM_PIVOTS = 3;

    /** 子节点数量 */
    public static final int NUM_CHILDREN = 8;

    /** 划分阈值（每个维度的中位数） */
    private double[] splitThreshold;

    /** 每个子树在每个维度的距离下界 */
    private double[][] lowerBound;

    /** 每个子树在每个维度的距离上界 */
    private double[][] upperBound;

    /** 每个子树的数据量 */
    private int[] childSizes;

    /**
     * 构造线性划分内部节点
     *
     * @param pivots 3个支撑点
     * @param children 8个子节点
     * @param splitThreshold 3个划分阈值
     * @param lowerBound 下界矩阵 [8][3]
     * @param upperBound 上界矩阵 [8][3]
     * @param depth 节点深度
     */
    public LinearPartitionInternalNode(List<MetricSpaceData> pivots, List<TreeNode> children,
            double[] splitThreshold, double[][] lowerBound, double[][] upperBound, int depth) {
        this.pivots = new ArrayList<>(pivots);
        this.children = new ArrayList<>(children);
        this.splitThreshold = splitThreshold.clone();
        this.lowerBound = deepCopy(lowerBound);
        this.upperBound = deepCopy(upperBound);
        this.depth = depth;

        this.childSizes = new int[NUM_CHILDREN];
        for (int i = 0; i < NUM_CHILDREN; i++) {
            if (children.get(i) != null) {
                childSizes[i] = children.get(i).size();
            }
        }
    }

    /**
     * 获取子节点索引
     *
     * @param d1 到pivot1的距离
     * @param d2 到pivot2的距离
     * @param d3 到pivot3的距离
     * @return 子节点索引（0-7）
     */
    public int getChildIndex(double d1, double d2, double d3) {
        int idx = 0;
        if (d1 > splitThreshold[0])
            idx |= 1;
        if (d2 > splitThreshold[1])
            idx |= 2;
        if (d3 > splitThreshold[2])
            idx |= 4;
        return idx;
    }

    /**
     * 判断是否需要访问子节点
     *
     * 在支撑点空间中，查询区域是一个以(dq1, dq2, dq3)为中心的边长2r的立方体。
     * 若子区域与查询立方体不相交，则可排除。
     *
     * @param childIdx 子节点索引
     * @param dq 查询对象的支撑点空间坐标
     * @param radius 查询半径
     * @return 是否需要访问
     */
    public boolean shouldVisitChild(int childIdx, double[] dq, double radius) {
        if (children.get(childIdx) == null) {
            return false;
        }

        for (int dim = 0; dim < NUM_PIVOTS; dim++) {
            double L = lowerBound[childIdx][dim];
            double U = upperBound[childIdx][dim];

            // 查询区域在该维度的范围是 [dq[dim] - r, dq[dim] + r]
            // 若不相交则可排除
            if (dq[dim] + radius < L || dq[dim] - radius > U) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断子节点是否完全包含在查询范围内
     *
     * 若子区域完全在查询立方体内，可以批量返回。
     * 注意：这里是在支撑点空间中判断，不等于度量空间中的包含。
     *
     * @param childIdx 子节点索引
     * @param dq 查询对象的支撑点空间坐标
     * @param radius 查询半径
     * @return 是否完全包含
     */
    public boolean isChildFullyContained(int childIdx, double[] dq, double radius) {
        for (int dim = 0; dim < NUM_PIVOTS; dim++) {
            double L = lowerBound[childIdx][dim];
            double U = upperBound[childIdx][dim];

            // 检查子区域是否完全在查询立方体内
            if (!(L >= dq[dim] - radius && U <= dq[dim] + radius)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取第i个pivot
     */
    public MetricSpaceData getPivot(int i) {
        return pivots.get(i);
    }

    /**
     * 获取第i个划分阈值
     */
    public double getSplitThreshold(int i) {
        return splitThreshold[i];
    }

    /**
     * 获取所有划分阈值
     */
    public double[] getSplitThresholds() {
        return splitThreshold.clone();
    }

    /**
     * 获取第i个子节点
     */
    public TreeNode getChild(int i) {
        return children.get(i);
    }

    /**
     * 获取子节点的距离下界
     */
    public double getLowerBound(int childIdx, int dimIdx) {
        return lowerBound[childIdx][dimIdx];
    }

    /**
     * 获取子节点的距离上界
     */
    public double getUpperBound(int childIdx, int dimIdx) {
        return upperBound[childIdx][dimIdx];
    }

    /**
     * 获取第i个子节点的数据量
     */
    public int getChildSize(int i) {
        return childSizes[i];
    }

    /**
     * 重写size方法
     */
    @Override
    public int size() {
        int total = NUM_PIVOTS;
        for (int i = 0; i < NUM_CHILDREN; i++) {
            total += childSizes[i];
        }
        return total;
    }

    private double[][] deepCopy(double[][] src) {
        if (src == null)
            return null;
        double[][] dst = new double[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i].clone();
        }
        return dst;
    }

    @Override
    public String getNodeInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("LinearPartitionInternalNode[depth=%d, pivots=[", depth));
        for (int i = 0; i < NUM_PIVOTS; i++) {
            if (i > 0)
                sb.append(",");
            sb.append("ID").append(pivots.get(i).getDataId());
        }
        sb.append("], children=[");
        for (int i = 0; i < NUM_CHILDREN; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(childSizes[i]);
        }
        sb.append("]]");
        return sb.toString();
    }
}
