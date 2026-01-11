package index.tree.cght;

import core.MetricSpaceData;
import index.tree.InternalNode;
import index.tree.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * CGHT内部节点（Complete Generalized Hyperplane Tree）
 *
 * 使用3个pivot进行完全超平面划分。
 *
 * 划分策略基于距离差值：
 * - delta12 = d(x, p1) - d(x, p2)
 * - delta13 = d(x, p1) - d(x, p3)
 *
 * 使用(delta12, delta13)的符号组合划分为4个主区域：
 * - 区域0: delta12 < 0, delta13 < 0
 * - 区域1: delta12 >= 0, delta13 < 0
 * - 区域2: delta12 < 0, delta13 >= 0
 * - 区域3: delta12 >= 0, delta13 >= 0
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class CGHInternalNode extends InternalNode {

    /** pivot数量 */
    public static final int NUM_PIVOTS = 3;

    /** 子节点数量（4路划分） */
    public static final int NUM_CHILDREN = 4;

    /** 每个子树的delta12范围 [min, max] */
    private double[][] delta12Range;

    /** 每个子树的delta13范围 [min, max] */
    private double[][] delta13Range;

    /** 每个子树的数据量 */
    private int[] childSizes;

    /**
     * 构造CGHT内部节点
     *
     * @param pivot1 第一个支撑点
     * @param pivot2 第二个支撑点
     * @param pivot3 第三个支撑点
     * @param children 4个子节点
     * @param delta12Range delta12范围矩阵 [4][2]
     * @param delta13Range delta13范围矩阵 [4][2]
     * @param depth 节点深度
     */
    public CGHInternalNode(MetricSpaceData pivot1, MetricSpaceData pivot2, MetricSpaceData pivot3,
            List<TreeNode> children, double[][] delta12Range, double[][] delta13Range, int depth) {
        this.pivots = new ArrayList<>();
        this.pivots.add(pivot1);
        this.pivots.add(pivot2);
        this.pivots.add(pivot3);
        this.children = new ArrayList<>(children);
        this.delta12Range = deepCopy(delta12Range);
        this.delta13Range = deepCopy(delta13Range);
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
     * 获取子节点索引（基于距离差符号的4路划分）
     *
     * @param d1 到pivot1的距离
     * @param d2 到pivot2的距离
     * @param d3 到pivot3的距离
     * @return 子节点索引（0-3）
     */
    public int getChildIndex(double d1, double d2, double d3) {
        double delta12 = d1 - d2;
        double delta13 = d1 - d3;

        int idx = 0;
        if (delta12 >= 0)
            idx |= 1; // bit 0
        if (delta13 >= 0)
            idx |= 2; // bit 1
        return idx;
    }

    /**
     * 判断是否需要访问某个子节点
     *
     * 基于GH树剪枝规则的扩展：
     * 若 delta12 - 2r > delta12Range[i][1]，排除
     * 若 delta12 + 2r < delta12Range[i][0]，排除
     * 类似地检查delta13
     *
     * @param childIdx 子节点索引
     * @param d1 查询对象到pivot1的距离
     * @param d2 查询对象到pivot2的距离
     * @param d3 查询对象到pivot3的距离
     * @param r 查询半径
     * @return 是否需要访问
     */
    public boolean shouldVisitChild(int childIdx, double d1, double d2, double d3, double r) {
        if (children.get(childIdx) == null) {
            return false;
        }

        double delta12 = d1 - d2;
        double delta13 = d1 - d3;

        double[] range12 = delta12Range[childIdx];
        double[] range13 = delta13Range[childIdx];

        // 剪枝条件
        if (delta12 - 2 * r > range12[1] || delta12 + 2 * r < range12[0]) {
            return false;
        }
        if (delta13 - 2 * r > range13[1] || delta13 + 2 * r < range13[0]) {
            return false;
        }

        return true;
    }

    /**
     * 获取第一个支撑点
     */
    public MetricSpaceData getPivot1() {
        return pivots.get(0);
    }

    /**
     * 获取第二个支撑点
     */
    public MetricSpaceData getPivot2() {
        return pivots.get(1);
    }

    /**
     * 获取第三个支撑点
     */
    public MetricSpaceData getPivot3() {
        return pivots.get(2);
    }

    /**
     * 获取第i个pivot
     */
    public MetricSpaceData getPivot(int i) {
        return pivots.get(i);
    }

    /**
     * 获取第i个子节点
     */
    public TreeNode getChild(int i) {
        return children.get(i);
    }

    /**
     * 获取子节点的delta12范围
     */
    public double[] getDelta12Range(int childIdx) {
        return delta12Range[childIdx].clone();
    }

    /**
     * 获取子节点的delta13范围
     */
    public double[] getDelta13Range(int childIdx) {
        return delta13Range[childIdx].clone();
    }

    /**
     * 获取第i个子节点的数据量
     */
    public int getChildSize(int i) {
        return childSizes[i];
    }

    /**
     * 重写size方法，包含pivot本身
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
        return String.format(
                "CGHInternalNode[depth=%d, pivots=[ID%d,ID%d,ID%d], children=[%d,%d,%d,%d]]", depth,
                pivots.get(0).getDataId(), pivots.get(1).getDataId(), pivots.get(2).getDataId(),
                childSizes[0], childSizes[1], childSizes[2], childSizes[3]);
    }
}
