package index.tree.vptree;

import core.MetricSpaceData;
import index.tree.InternalNode;
import index.tree.TreeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VP树内部节点
 * 
 * VP树（Vantage Point Tree）使用一个支撑点(pivot/vantage point)通过球形划分空间。
 * 根据数据到支撑点的距离，将空间分为内球和外球两部分。
 * 
 * 划分规则：
 * - d(x, p) <= median → 内球（左子树）
 * - d(x, p) > median → 外球（右子树）
 * 
 * 每个子区域记录距离范围 [lower, upper]，用于查询时的剪枝。
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public class VPInternalNode extends InternalNode {

    /**
     * 距离范围类
     * 记录子区域中数据到支撑点的距离范围
     */
    public static class DistanceRange {
        public final double lower; // 最小距离
        public final double upper; // 最大距离

        public DistanceRange(double lower, double upper) {
            this.lower = lower;
            this.upper = upper;
        }

        @Override
        public String toString() {
            return String.format("[%.4f, %.4f]", lower, upper);
        }
    }

    /** 每个子树的距离范围 */
    private List<DistanceRange> distanceRanges;

    /** 划分距离（中位数） */
    private double medianDistance;

    /**
     * 构造VP树内部节点
     * 
     * @param pivot 支撑点
     * @param innerChild 内球子树（距离较近）
     * @param outerChild 外球子树（距离较远）
     * @param innerRange 内球距离范围
     * @param outerRange 外球距离范围
     * @param medianDistance 划分距离
     * @param depth 节点深度
     */
    public VPInternalNode(MetricSpaceData pivot, TreeNode innerChild, TreeNode outerChild,
            DistanceRange innerRange, DistanceRange outerRange, double medianDistance, int depth) {
        this.pivots = Collections.singletonList(pivot);
        this.children = new ArrayList<>();
        this.children.add(innerChild);
        this.children.add(outerChild);
        this.distanceRanges = new ArrayList<>();
        this.distanceRanges.add(innerRange);
        this.distanceRanges.add(outerRange);
        this.medianDistance = medianDistance;
        this.depth = depth;
    }

    /**
     * 获取支撑点
     * 
     * @return 支撑点
     */
    public MetricSpaceData getPivot() {
        return pivots.get(0);
    }

    /**
     * 获取内球子树
     * 
     * @return 内球子树（距离较近的数据）
     */
    public TreeNode getInnerChild() {
        return children.get(0);
    }

    /**
     * 获取外球子树
     * 
     * @return 外球子树（距离较远的数据）
     */
    public TreeNode getOuterChild() {
        return children.get(1);
    }

    /**
     * 获取内球距离范围
     * 
     * @return 内球距离范围
     */
    public DistanceRange getInnerRange() {
        return distanceRanges.get(0);
    }

    /**
     * 获取外球距离范围
     * 
     * @return 外球距离范围
     */
    public DistanceRange getOuterRange() {
        return distanceRanges.get(1);
    }

    /**
     * 获取指定子树的距离范围
     * 
     * @param childIndex 子树索引（0=内球，1=外球）
     * @return 距离范围
     */
    public DistanceRange getDistanceRange(int childIndex) {
        return distanceRanges.get(childIndex);
    }

    /**
     * 获取所有距离范围
     * 
     * @return 距离范围列表
     */
    public List<DistanceRange> getDistanceRanges() {
        return distanceRanges;
    }

    /**
     * 获取划分距离（中位数）
     * 
     * @return 划分距离
     */
    public double getMedianDistance() {
        return medianDistance;
    }

    /**
     * 获取内球数据量
     * 
     * @return 内球包含的数据数量
     */
    public int getInnerSize() {
        return children.get(0).size();
    }

    /**
     * 获取外球数据量
     * 
     * @return 外球包含的数据数量
     */
    public int getOuterSize() {
        return children.get(1).size();
    }

    /**
     * 重写size方法，包含pivot本身
     * VP树的pivot不在子树中，需要单独计数
     */
    @Override
    public int size() {
        // 子树中的数据量 + pivot（1个）
        return super.size() + 1;
    }

    @Override
    public String getNodeInfo() {
        return String.format(
                "VPInternalNode[depth=%d, inner=%d%s, outer=%d%s, median=%.4f, pivot=%s]", depth,
                getInnerSize(), getInnerRange(), getOuterSize(), getOuterRange(), medianDistance,
                pivots.get(0).toString());
    }
}

