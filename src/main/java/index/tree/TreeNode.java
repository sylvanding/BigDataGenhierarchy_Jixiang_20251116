package index.tree;

/**
 * 树节点接口
 *
 * 定义了树状索引中所有节点（内部节点和叶子节点）的公共接口。
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public interface TreeNode {

    /**
     * 判断是否为叶子节点
     *
     * @return true表示叶子节点，false表示内部节点
     */
    boolean isLeaf();

    /**
     * 获取节点中数据的数量
     *
     * 对于叶子节点，返回存储的数据对象数量
     * 对于内部节点，返回所有子树中数据对象的总数
     *
     * @return 数据数量
     */
    int size();

    /**
     * 获取节点深度
     *
     * @return 节点在树中的深度（根节点为0）
     */
    int getDepth();

    /**
     * 获取节点信息的字符串表示
     *
     * @return 节点信息字符串
     */
    String getNodeInfo();
}

