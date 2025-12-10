package index.tree.common;

/**
 * 树高控制器
 * 
 * 控制树的构建过程，确保树达到指定的最小高度。
 * 通过动态调整叶子节点创建条件来实现树高控制。
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public class TreeHeightController {
    
    /** 树配置 */
    private TreeConfig config;
    
    /**
     * 构造函数
     * @param config 树配置
     */
    public TreeHeightController(TreeConfig config) {
        this.config = config;
    }
    
    /**
     * 判断是否可以创建叶子节点
     * 
     * 规则：
     * 1. 如果数据量很小（<=2），必须创建叶子节点
     * 2. 如果还没达到最小高度，不能创建叶子节点
     * 3. 如果已达到最小高度且数据量小于等于最大叶子容量，可以创建叶子
     * 
     * @param currentDepth 当前深度（根节点为0）
     * @param dataSize 当前节点的数据量
     * @return true表示可以创建叶子，false表示必须继续划分
     */
    public boolean canCreateLeaf(int currentDepth, int dataSize) {
        // 情况1：数据量太小，必须创建叶子
        if (dataSize <= 2) {
            return true;
        }
        
        // 情况2：还没达到最小高度，必须继续划分
        if (currentDepth < config.getMinTreeHeight()) {
            return false;
        }
        
        // 情况3：已达到最小高度，根据数据量判断
        return dataSize <= config.getMaxLeafSize();
    }
    
    /**
     * 计算给定数据集应该使用的最大叶子大小
     * 
     * 为了确保树达到最小高度，需要动态调整叶子大小。
     * 
     * @param datasetSize 数据集大小
     * @return 调整后的最大叶子大小
     */
    public int calculateMaxLeafSize(int datasetSize) {
        int minHeight = config.getMinTreeHeight();
        
        // 计算达到最小高度所需的最大叶子容量
        // 假设二叉树：叶子数 = 2^h，每个叶子容量 = n / 2^h
        int maxLeafForHeight = (int) Math.ceil(datasetSize / Math.pow(2, minHeight));
        
        // 返回配置值和计算值中的较小者
        return Math.min(config.getMaxLeafSize(), Math.max(1, maxLeafForHeight));
    }
    
    /**
     * 获取当前配置的最小树高
     * 
     * @return 最小树高
     */
    public int getMinTreeHeight() {
        return config.getMinTreeHeight();
    }
    
    /**
     * 获取当前配置的最大叶子容量
     * 
     * @return 最大叶子容量
     */
    public int getMaxLeafSize() {
        return config.getMaxLeafSize();
    }
}

