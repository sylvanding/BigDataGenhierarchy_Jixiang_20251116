package index.tree.mvptree;

import core.MetricSpaceData;
import index.tree.InternalNode;
import index.tree.TreeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MVP树内部节点
 *
 * 使用3个pivot进行嵌套球形划分，产生2^3=8个子区域。
 * 每个子区域由3个二进制位标识：
 * - bit 0: 第1个pivot的内/外球（0=内球，1=外球）
 * - bit 1: 第2个pivot的内/外球
 * - bit 2: 第3个pivot的内/外球
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class MVPInternalNode extends InternalNode {

    /** pivot数量 */
    public static final int NUM_PIVOTS = 3;

    /** 子节点数量 */
    public static final int NUM_CHILDREN = 8;

    /** 划分半径（每个pivot的中位数距离） */
    private double[] splitRadius;

    /** 每个子树到每个pivot的距离下界 */
    private double[][] lowerBound;

    /** 每个子树到每个pivot的距离上界 */
    private double[][] upperBound;

    /** 每个子树的数据量 */
    private int[] childSizes;

    /**
     * 构造MVP树内部节点
     *
     * @param pivots 3个支撑点
     * @param children 8个子节点
     * @param splitRadius 3个划分半径
     * @param lowerBound 距离下界矩阵 [8][3]
     * @param upperBound 距离上界矩阵 [8][3]
     * @param depth 节点深度
     */
    public MVPInternalNode(List<MetricSpaceData> pivots, List<TreeNode> children,
            double[] splitRadius, double[][] lowerBound, double[][] upperBound, int depth) {
        this.pivots = new ArrayList<>(pivots);
        this.children = new ArrayList<>(children);
        this.splitRadius = splitRadius.clone();
        this.lowerBound = deepCopy(lowerBound);
        this.upperBound = deepCopy(upperBound);
        this.depth = depth;

        // 计算子树大小
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
     * @param distToPivots 数据到3个pivot的距离数组
     * @return 子节点索引（0-7）
     */
    public int getChildIndex(double[] distToPivots) {
        int idx = 0;
        if (distToPivots[0] > splitRadius[0])
            idx |= 1; // bit 0
        if (distToPivots[1] > splitRadius[1])
            idx |= 2; // bit 1
        if (distToPivots[2] > splitRadius[2])
            idx |= 4; // bit 2
        return idx;
    }

    /**
     * 判断是否需要访问某个子节点（范围查询剪枝）
     *
     * 剪枝条件：若查询球与子树在任一pivot维度上不相交，则可排除
     *
     * @param childIdx 子节点索引
     * @param distToQuery 查询对象到各pivot的距离
     * @param radius 查询半径
     * @return 是否需要访问该子节点
     */
    public boolean shouldVisitChild(int childIdx, double[] distToQuery, double radius) {
        if (children.get(childIdx) == null) {
            return false;
        }

        for (int p = 0; p < NUM_PIVOTS; p++) {
            double dq = distToQuery[p];
            double L = lowerBound[childIdx][p];
            double U = upperBound[childIdx][p];

            // 剪枝条件：查询球与子树距离范围不相交
            if (dq + radius < L || dq - radius > U) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断子节点是否完全包含在查询范围内（包含规则）
     *
     * 若 d(q, p) + U <= r 对于某个pivot成立，则子树全部在查询范围内
     *
     * @param childIdx 子节点索引
     * @param distToQuery 查询对象到各pivot的距离
     * @param radius 查询半径
     * @return 是否完全包含
     */
    public boolean isChildFullyContained(int childIdx, double[] distToQuery, double radius) {
        for (int p = 0; p < NUM_PIVOTS; p++) {
            double dq = distToQuery[p];
            double U = upperBound[childIdx][p];

            // 若 d(q, p) + U <= r，则子树全部在查询范围内
            if (dq + U <= radius) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取第i个pivot
     */
    public MetricSpaceData getPivot(int i) {
        return pivots.get(i);
    }

    /**
     * 获取第i个划分半径
     */
    public double getSplitRadius(int i) {
        return splitRadius[i];
    }

    /**
     * 获取所有划分半径
     */
    public double[] getSplitRadii() {
        return splitRadius.clone();
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
    public double getLowerBound(int childIdx, int pivotIdx) {
        return lowerBound[childIdx][pivotIdx];
    }

    /**
     * 获取子节点的距离上界
     */
    public double getUpperBound(int childIdx, int pivotIdx) {
        return upperBound[childIdx][pivotIdx];
    }

    /**
     * 获取第i个子节点的数据量
     */
    public int getChildSize(int i) {
        return childSizes[i];
    }

    /**
     * 重写size方法，包含pivot本身
     * MVP树的pivot不在子树中，需要单独计数
     */
    @Override
    public int size() {
        int total = NUM_PIVOTS; // 3个pivot
        for (int i = 0; i < NUM_CHILDREN; i++) {
            total += childSizes[i];
        }
        return total;
    }

    /**
     * 深拷贝二维数组
     */
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
        sb.append(String.format("MVPInternalNode[depth=%d, pivots=[", depth));
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
