package index.tree;

import core.MetricSpaceData;
import java.util.List;

/**
 * 内部节点抽象类
 * 
 * 树状索引的内部节点，包含子节点和支撑点(pivot)信息。
 * GH树和VP树的内部节点都继承自此类。
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public abstract class InternalNode implements TreeNode {
    
    /** 支撑点列表 */
    protected List<MetricSpaceData> pivots;
    
    /** 子节点列表 */
    protected List<TreeNode> children;
    
    /** 节点深度 */
    protected int depth;
    
    @Override
    public boolean isLeaf() {
        return false;
    }
    
    @Override
    public int size() {
        int total = 0;
        for (TreeNode child : children) {
            total += child.size();
        }
        return total;
    }
    
    @Override
    public int getDepth() {
        return depth;
    }
    
    /**
     * 获取支撑点列表
     * 
     * @return 支撑点列表
     */
    public List<MetricSpaceData> getPivots() {
        return pivots;
    }
    
    /**
     * 获取子节点列表
     * 
     * @return 子节点列表
     */
    public List<TreeNode> getChildren() {
        return children;
    }
    
    /**
     * 获取子节点数量
     * 
     * @return 子节点数量
     */
    public int getChildCount() {
        return children != null ? children.size() : 0;
    }
    
    /**
     * 获取支撑点数量
     * 
     * @return 支撑点数量
     */
    public int getPivotCount() {
        return pivots != null ? pivots.size() : 0;
    }
    
    @Override
    public String getNodeInfo() {
        return String.format("InternalNode[depth=%d, pivots=%d, children=%d, size=%d]",
                depth, getPivotCount(), getChildCount(), size());
    }
    
    @Override
    public String toString() {
        return getNodeInfo();
    }
}

