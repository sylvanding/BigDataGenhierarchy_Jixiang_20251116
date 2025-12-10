package index.tree;

import core.MetricSpaceData;
import core.MetricFunction;
import index.Index;
import index.tree.common.TreeConfig;
import index.tree.common.TreeHeightController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 树状索引抽象基类
 * 
 * 定义了树状索引的公共结构和方法，是GH树和VP树的父类。
 * 提供了树的构建、统计信息收集、可视化等公共功能。
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public abstract class TreeIndex implements Index {
    
    /** 根节点 */
    protected TreeNode root;
    
    /** 树配置 */
    protected TreeConfig config;
    
    /** 距离函数 */
    protected MetricFunction metric;
    
    /** 原始数据集 */
    protected List<? extends MetricSpaceData> dataset;
    
    /** 树高控制器 */
    protected TreeHeightController heightController;
    
    /** 随机数生成器 */
    protected Random random;
    
    // ========== 统计信息 ==========
    
    /** 构建时的距离计算次数 */
    protected int buildDistanceComputations = 0;
    
    /** 查询时的距离计算次数 */
    protected int queryDistanceComputations = 0;
    
    /** 查询时的节点访问次数 */
    protected int nodeAccesses = 0;
    
    /** 树高度 */
    protected int treeHeight = 0;
    
    /** 总节点数 */
    protected int totalNodes = 0;
    
    /** 叶子节点数 */
    protected int leafNodes = 0;
    
    /** 内部节点数 */
    protected int internalNodes = 0;
    
    /** 构建时间（毫秒） */
    protected long buildTimeMs = 0;
    
    /**
     * 构造函数
     * 
     * @param config 树配置
     */
    public TreeIndex(TreeConfig config) {
        this.config = config;
        this.heightController = new TreeHeightController(config);
        
        // 初始化随机数生成器
        if (config.getRandomSeed() != null) {
            this.random = new Random(config.getRandomSeed());
        } else {
            this.random = new Random();
        }
    }
    
    /**
     * 构建索引（模板方法）
     * 
     * 实现了索引构建的通用流程：
     * 1. 验证输入参数
     * 2. 初始化
     * 3. 递归构建树
     * 4. 计算统计信息
     */
    @Override
    public void buildIndex(List<? extends MetricSpaceData> dataset, MetricFunction metric) {
        // 验证输入
        if (dataset == null || dataset.isEmpty()) {
            throw new IllegalArgumentException("数据集不能为空");
        }
        if (metric == null) {
            throw new IllegalArgumentException("距离函数不能为空");
        }
        
        this.dataset = dataset;
        this.metric = metric;
        this.buildDistanceComputations = 0;
        
        if (config.isVerbose()) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("开始构建 " + getIndexName());
            System.out.println("=".repeat(60));
            System.out.println("数据集大小: " + dataset.size());
            System.out.println("配置: " + config);
        }
        
        // 构建树
        long startTime = System.currentTimeMillis();
        this.root = buildTreeRecursive(new ArrayList<>(dataset), 0);
        this.buildTimeMs = System.currentTimeMillis() - startTime;
        
        // 计算统计信息
        calculateStatistics();
        
        if (config.isVerbose()) {
            System.out.println("\n构建完成!");
            System.out.println("-".repeat(40));
            System.out.println("构建耗时: " + buildTimeMs + " ms");
            System.out.println("树高度: " + treeHeight);
            System.out.println("总节点数: " + totalNodes);
            System.out.println("  - 内部节点: " + internalNodes);
            System.out.println("  - 叶子节点: " + leafNodes);
            System.out.println("构建距离计算次数: " + buildDistanceComputations);
            System.out.println("=".repeat(60));
        }
    }
    
    /**
     * 递归构建树（由子类实现）
     * 
     * @param data 当前节点的数据
     * @param depth 当前深度
     * @return 构建的树节点
     */
    protected abstract TreeNode buildTreeRecursive(List<MetricSpaceData> data, int depth);
    
    /**
     * 计算统计信息
     */
    protected void calculateStatistics() {
        if (root != null) {
            treeHeight = calculateHeight(root);
            totalNodes = countNodes(root);
            leafNodes = countLeafNodes(root);
            internalNodes = totalNodes - leafNodes;
        }
    }
    
    /**
     * 计算树的高度
     * 
     * @param node 当前节点
     * @return 以该节点为根的子树高度
     */
    protected int calculateHeight(TreeNode node) {
        if (node == null || node.isLeaf()) {
            return 0;
        }
        
        InternalNode internal = (InternalNode) node;
        int maxChildHeight = 0;
        for (TreeNode child : internal.getChildren()) {
            maxChildHeight = Math.max(maxChildHeight, calculateHeight(child));
        }
        return 1 + maxChildHeight;
    }
    
    /**
     * 统计总节点数
     * 
     * @param node 当前节点
     * @return 以该节点为根的子树节点数
     */
    protected int countNodes(TreeNode node) {
        if (node == null) return 0;
        if (node.isLeaf()) return 1;
        
        int count = 1;
        for (TreeNode child : ((InternalNode) node).getChildren()) {
            count += countNodes(child);
        }
        return count;
    }
    
    /**
     * 统计叶子节点数
     * 
     * @param node 当前节点
     * @return 以该节点为根的子树叶子节点数
     */
    protected int countLeafNodes(TreeNode node) {
        if (node == null) return 0;
        if (node.isLeaf()) return 1;
        
        int count = 0;
        for (TreeNode child : ((InternalNode) node).getChildren()) {
            count += countLeafNodes(child);
        }
        return count;
    }
    
    /**
     * 打印树结构
     * 
     * 以可视化方式打印树的结构，便于调试和验证。
     */
    public void printTree() {
        System.out.println("\n" + getIndexName() + " 树结构:");
        System.out.println("-".repeat(50));
        if (root != null) {
            printTreeRecursive(root, "", true);
        } else {
            System.out.println("(空树)");
        }
        System.out.println("-".repeat(50));
    }
    
    /**
     * 递归打印树结构
     * 
     * @param node 当前节点
     * @param prefix 前缀字符串
     * @param isLast 是否是最后一个子节点
     */
    protected void printTreeRecursive(TreeNode node, String prefix, boolean isLast) {
        String connector = isLast ? "└── " : "├── ";
        System.out.println(prefix + connector + getNodeDescription(node));
        
        if (!node.isLeaf()) {
            InternalNode internal = (InternalNode) node;
            List<TreeNode> children = internal.getChildren();
            for (int i = 0; i < children.size(); i++) {
                String childPrefix = prefix + (isLast ? "    " : "│   ");
                printTreeRecursive(children.get(i), childPrefix, i == children.size() - 1);
            }
        }
    }
    
    /**
     * 获取节点描述（由子类实现以提供更详细的信息）
     * 
     * @param node 节点
     * @return 节点描述字符串
     */
    protected String getNodeDescription(TreeNode node) {
        if (node.isLeaf()) {
            return String.format("叶子节点 [数据量=%d]", node.size());
        } else {
            return String.format("内部节点 [数据量=%d]", node.size());
        }
    }
    
    // ========== Index接口实现 ==========
    
    @Override
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(50)).append("\n");
        sb.append(getIndexName()).append(" 统计信息\n");
        sb.append("=".repeat(50)).append("\n");
        
        sb.append("【数据信息】\n");
        sb.append(String.format("  数据集大小: %,d\n", dataset != null ? dataset.size() : 0));
        
        sb.append("【树结构】\n");
        sb.append(String.format("  树高度: %d\n", treeHeight));
        sb.append(String.format("  总节点数: %,d\n", totalNodes));
        sb.append(String.format("  内部节点数: %,d\n", internalNodes));
        sb.append(String.format("  叶子节点数: %,d\n", leafNodes));
        
        sb.append("【构建性能】\n");
        sb.append(String.format("  构建时间: %,d ms\n", buildTimeMs));
        sb.append(String.format("  构建距离计算次数: %,d\n", buildDistanceComputations));
        
        sb.append("【查询性能（累计）】\n");
        sb.append(String.format("  查询距离计算次数: %,d\n", queryDistanceComputations));
        sb.append(String.format("  节点访问次数: %,d\n", nodeAccesses));
        
        sb.append("=".repeat(50));
        return sb.toString();
    }
    
    @Override
    public void resetStatistics() {
        queryDistanceComputations = 0;
        nodeAccesses = 0;
    }
    
    @Override
    public int getDistanceComputations() {
        return queryDistanceComputations;
    }
    
    @Override
    public int getDatasetSize() {
        return dataset != null ? dataset.size() : 0;
    }
    
    // ========== Getters ==========
    
    public TreeNode getRoot() {
        return root;
    }
    
    public TreeConfig getConfig() {
        return config;
    }
    
    public MetricFunction getMetric() {
        return metric;
    }
    
    public int getTreeHeight() {
        return treeHeight;
    }
    
    public int getTotalNodes() {
        return totalNodes;
    }
    
    public int getLeafNodes() {
        return leafNodes;
    }
    
    public int getInternalNodes() {
        return internalNodes;
    }
    
    public int getBuildDistanceComputations() {
        return buildDistanceComputations;
    }
    
    public long getBuildTimeMs() {
        return buildTimeMs;
    }
    
    public int getNodeAccesses() {
        return nodeAccesses;
    }
}

