package index.tree.common;

/**
 * 树配置类
 * 
 * 统一管理树状索引的各种配置参数，如最大叶子节点容量、最小树高、
 * 支撑点选择策略等。使用Builder模式方便配置。
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public class TreeConfig {
    
    /** 最大叶子节点容量（默认50） */
    private int maxLeafSize;
    
    /** 最小树高（默认3） */
    private int minTreeHeight;
    
    /** 支撑点选择策略 */
    private PivotSelectionStrategy pivotStrategy;
    
    /** 是否输出详细信息 */
    private boolean verbose;
    
    /** 随机种子（用于可重复实验） */
    private Long randomSeed;
    
    /**
     * 支撑点选择策略枚举
     */
    public enum PivotSelectionStrategy {
        /** 随机选择 */
        RANDOM,
        /** 最远优先遍历 (Farthest-First Traversal) */
        FFT,
        /** 最大分散度 */
        MAX_SPREAD
    }
    
    /**
     * 默认构造函数
     * 使用默认配置参数
     */
    public TreeConfig() {
        this.maxLeafSize = 50;
        this.minTreeHeight = 3;
        this.pivotStrategy = PivotSelectionStrategy.FFT;
        this.verbose = false;
        this.randomSeed = null;
    }
    
    /**
     * Builder类，用于方便地构建TreeConfig
     */
    public static class Builder {
        private TreeConfig config = new TreeConfig();
        
        /**
         * 设置最大叶子节点容量
         * @param size 最大容量
         * @return Builder实例
         */
        public Builder maxLeafSize(int size) {
            config.maxLeafSize = size;
            return this;
        }
        
        /**
         * 设置最小树高
         * @param height 最小树高
         * @return Builder实例
         */
        public Builder minTreeHeight(int height) {
            config.minTreeHeight = height;
            return this;
        }
        
        /**
         * 设置支撑点选择策略
         * @param strategy 选择策略
         * @return Builder实例
         */
        public Builder pivotStrategy(PivotSelectionStrategy strategy) {
            config.pivotStrategy = strategy;
            return this;
        }
        
        /**
         * 设置是否输出详细信息
         * @param verbose 是否详细
         * @return Builder实例
         */
        public Builder verbose(boolean verbose) {
            config.verbose = verbose;
            return this;
        }
        
        /**
         * 设置随机种子
         * @param seed 随机种子
         * @return Builder实例
         */
        public Builder randomSeed(long seed) {
            config.randomSeed = seed;
            return this;
        }
        
        /**
         * 构建TreeConfig对象
         * @return 配置好的TreeConfig
         * @throws IllegalArgumentException 如果配置参数非法
         */
        public TreeConfig build() {
            validate();
            return config;
        }
        
        /**
         * 验证配置参数
         */
        private void validate() {
            if (config.maxLeafSize <= 0) {
                throw new IllegalArgumentException("maxLeafSize必须大于0，当前值: " + config.maxLeafSize);
            }
            if (config.minTreeHeight < 0) {
                throw new IllegalArgumentException("minTreeHeight不能为负数，当前值: " + config.minTreeHeight);
            }
        }
    }
    
    // Getters
    
    public int getMaxLeafSize() {
        return maxLeafSize;
    }
    
    public int getMinTreeHeight() {
        return minTreeHeight;
    }
    
    public PivotSelectionStrategy getPivotStrategy() {
        return pivotStrategy;
    }
    
    public boolean isVerbose() {
        return verbose;
    }
    
    public Long getRandomSeed() {
        return randomSeed;
    }
    
    // Setters
    
    public void setMaxLeafSize(int maxLeafSize) {
        this.maxLeafSize = maxLeafSize;
    }
    
    public void setMinTreeHeight(int minTreeHeight) {
        this.minTreeHeight = minTreeHeight;
    }
    
    public void setPivotStrategy(PivotSelectionStrategy pivotStrategy) {
        this.pivotStrategy = pivotStrategy;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public void setRandomSeed(Long randomSeed) {
        this.randomSeed = randomSeed;
    }
    
    @Override
    public String toString() {
        return String.format(
            "TreeConfig[maxLeafSize=%d, minTreeHeight=%d, pivotStrategy=%s, verbose=%s, seed=%s]",
            maxLeafSize, minTreeHeight, pivotStrategy, verbose, 
            randomSeed != null ? randomSeed : "auto"
        );
    }
}

