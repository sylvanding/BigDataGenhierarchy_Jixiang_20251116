package index.tree.ghtree;

import core.MetricSpaceData;
import index.tree.InternalNode;
import index.tree.TreeNode;

import java.util.Arrays;
import java.util.List;

/**
 * GH树内部节点
 *
 * GH树（Generalized Hyperplane Tree）使用两个支撑点(pivot)通过超平面划分空间。
 * 超平面将空间分为两部分：离pivot1近的数据放左子树，离pivot2近的数据放右子树。
 *
 * 划分规则：
 * - d(x, p1) < d(x, p2) → 左子树
 * - d(x, p1) >= d(x, p2) → 右子树
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class GHInternalNode extends InternalNode {

    /**
     * 构造GH树内部节点
     *
     * @param pivot1 第一个支撑点
     * @param pivot2 第二个支撑点
     * @param leftChild 左子树（离pivot1近的数据）
     * @param rightChild 右子树（离pivot2近的数据）
     * @param depth 节点深度
     */
    public GHInternalNode(MetricSpaceData pivot1, MetricSpaceData pivot2, TreeNode leftChild,
            TreeNode rightChild, int depth) {
        this.pivots = Arrays.asList(pivot1, pivot2);
        this.children = Arrays.asList(leftChild, rightChild);
        this.depth = depth;
    }

    /**
     * 获取第一个支撑点
     *
     * @return pivot1
     */
    public MetricSpaceData getPivot1() {
        return pivots.get(0);
    }

    /**
     * 获取第二个支撑点
     *
     * @return pivot2
     */
    public MetricSpaceData getPivot2() {
        return pivots.get(1);
    }

    /**
     * 获取左子树
     *
     * @return 左子树（离pivot1近的数据）
     */
    public TreeNode getLeftChild() {
        return children.get(0);
    }

    /**
     * 获取右子树
     *
     * @return 右子树（离pivot2近的数据）
     */
    public TreeNode getRightChild() {
        return children.get(1);
    }

    /**
     * 获取左子树数据量
     *
     * @return 左子树包含的数据数量
     */
    public int getLeftSize() {
        return children.get(0).size();
    }

    /**
     * 获取右子树数据量
     *
     * @return 右子树包含的数据数量
     */
    public int getRightSize() {
        return children.get(1).size();
    }

    @Override
    public String getNodeInfo() {
        return String.format("GHInternalNode[depth=%d, left=%d, right=%d, p1=%s, p2=%s]", depth,
                getLeftSize(), getRightSize(), pivots.get(0).toString(), pivots.get(1).toString());
    }
}

