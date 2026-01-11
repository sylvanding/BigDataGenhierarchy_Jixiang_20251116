package index.tree;

import core.MetricSpaceData;
import java.util.ArrayList;
import java.util.List;

/**
 * 叶子节点类
 *
 * 树状索引的叶子节点，存储实际的数据对象。
 * 叶子节点是树的最底层节点，不包含子节点。
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class LeafNode implements TreeNode {

    /** 存储的数据对象列表 */
    private List<MetricSpaceData> data;

    /** 节点深度 */
    private int depth;

    /**
     * 构造叶子节点
     *
     * @param data 数据对象列表
     * @param depth 节点深度
     */
    public LeafNode(List<? extends MetricSpaceData> data, int depth) {
        this.data = new ArrayList<>(data);
        this.depth = depth;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public int getDepth() {
        return depth;
    }

    /**
     * 获取存储的数据对象列表
     *
     * @return 数据对象列表
     */
    public List<MetricSpaceData> getData() {
        return data;
    }

    @Override
    public String getNodeInfo() {
        return String.format("LeafNode[depth=%d, size=%d]", depth, data.size());
    }

    @Override
    public String toString() {
        return getNodeInfo();
    }
}

