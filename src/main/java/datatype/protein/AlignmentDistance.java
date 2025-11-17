package datatype.protein;

import core.MetricFunction;
import core.MetricSpaceData;

/**
 * 基于mPAM的序列比对距离
 * 
 * 使用改进的PAM（Point Accepted Mutation）替代矩阵计算蛋白质序列之间的比对距离。
 * 采用全局序列比对算法（类似Needleman-Wunsch），但使用mPAM矩阵作为替代代价。
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public class AlignmentDistance implements MetricFunction {
    private static final long serialVersionUID = 1L;

    /**
     * mPAM250a扩展权重矩阵
     * 21x21的对称矩阵，表示不同氨基酸之间的替代代价
     * 行列顺序：A, R, N, D, C, Q, E, G, H, I, L, K, M, F, P, S, T, W, Y, V, OTHER
     * 
     * 矩阵值表示替代代价，值越小表示替代越容易（氨基酸越相似）
     */
    private static final double[][] MPAM_MATRIX = {
            // A   R   N   D   C   Q   E   G   H   I   L   K   M   F   P   S   T   W   Y   V   OTHER
            {0, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 5, 4, 2, 7}, // A
            {2, 0, 2, 2, 4, 2, 2, 2, 2, 3, 3, 2, 2, 4, 2, 2, 2, 4, 4, 3, 7}, // R
            {2, 2, 0, 2, 4, 2, 2, 2, 2, 3, 3, 2, 2, 4, 2, 2, 2, 5, 4, 2, 7}, // N
            {2, 2, 2, 0, 4, 2, 2, 2, 2, 3, 3, 2, 3, 4, 2, 2, 2, 6, 4, 2, 7}, // D
            {3, 4, 4, 4, 0, 4, 4, 3, 4, 3, 4, 4, 4, 4, 3, 3, 3, 7, 3, 3, 7}, // C
            {2, 2, 2, 2, 4, 0, 2, 2, 2, 3, 3, 2, 2, 4, 2, 2, 2, 5, 4, 3, 7}, // Q
            {2, 2, 2, 2, 4, 2, 0, 2, 2, 3, 3, 2, 3, 4, 2, 2, 2, 6, 4, 2, 7}, // E
            {2, 2, 2, 2, 3, 2, 2, 0, 2, 2, 3, 2, 2, 4, 2, 2, 2, 6, 4, 2, 7}, // G
            {2, 2, 2, 2, 4, 2, 2, 2, 0, 3, 3, 2, 3, 3, 2, 2, 2, 5, 3, 3, 7}, // H
            {2, 3, 3, 3, 3, 3, 3, 2, 3, 0, 1, 3, 2, 2, 2, 2, 2, 5, 3, 2, 7}, // I
            {2, 3, 3, 3, 4, 3, 3, 3, 3, 1, 0, 3, 1, 2, 3, 3, 2, 4, 2, 1, 7}, // L
            {2, 2, 2, 2, 4, 2, 2, 2, 2, 3, 3, 0, 2, 4, 2, 2, 2, 4, 4, 3, 7}, // K
            {2, 2, 2, 3, 4, 2, 3, 2, 3, 2, 1, 2, 0, 2, 2, 2, 2, 4, 3, 2, 7}, // M
            {3, 4, 4, 4, 4, 4, 4, 4, 3, 2, 2, 4, 2, 0, 4, 3, 3, 3, 1, 2, 7}, // F
            {2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 3, 2, 2, 4, 0, 2, 2, 5, 4, 2, 7}, // P
            {2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 3, 2, 2, 3, 2, 0, 2, 5, 4, 2, 7}, // S
            {2, 2, 2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 3, 2, 2, 0, 5, 3, 2, 7}, // T
            {5, 4, 5, 6, 7, 5, 6, 6, 5, 5, 4, 4, 4, 3, 5, 5, 5, 0, 4, 5, 7}, // W
            {4, 4, 4, 4, 3, 4, 4, 4, 3, 3, 2, 4, 3, 1, 4, 4, 3, 4, 0, 3, 7}, // Y
            {2, 3, 2, 2, 3, 3, 2, 2, 3, 2, 1, 3, 2, 2, 2, 2, 2, 5, 3, 0, 7}, // V
            {7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 0} // OTHER
    };

    /**
     * Gap惩罚值（空位罚分）
     */
    private static final double GAP_PENALTY = 1.0;

    /**
     * 片段长度（用于比对）
     */
    private final int fragmentLength;

    /**
     * 构造Alignment距离函数
     * @param fragmentLength 片段长度（默认为6，即6-mers）
     */
    public AlignmentDistance(int fragmentLength) {
        this.fragmentLength = fragmentLength;
    }

    /**
     * 构造Alignment距离函数（默认片段长度为6）
     */
    public AlignmentDistance() {
        this(6);
    }

    /**
     * 获取片段长度
     * @return 片段长度
     */
    public int getFragmentLength() {
        return fragmentLength;
    }

    /**
     * 计算两个蛋白质序列之间的比对距离
     * @param obj1 第一个蛋白质序列
     * @param obj2 第二个蛋白质序列
     * @return 比对距离
     * @throws IllegalArgumentException 如果输入不是ProteinData类型
     */
    @Override
    public double getDistance(MetricSpaceData obj1, MetricSpaceData obj2) {
        // 类型检查
        if (!(obj1 instanceof ProteinData) || !(obj2 instanceof ProteinData)) {
            throw new IllegalArgumentException("Alignment距离只能应用于蛋白质序列数据类型");
        }

        ProteinData p1 = (ProteinData) obj1;
        ProteinData p2 = (ProteinData) obj2;

        return globalAlignment(p1.getEncodedSequence(), p2.getEncodedSequence());
    }

    /**
     * 计算两个序列的全局比对距离
     * 使用动态规划算法（类似Needleman-Wunsch）
     * @param seq1 第一个序列的编码形式
     * @param seq2 第二个序列的编码形式
     * @return 比对距离
     */
    private double globalAlignment(byte[] seq1, byte[] seq2) {
        int m = seq1.length;
        int n = seq2.length;

        // 处理特殊情况
        if (m == 0 && n == 0)
            return 0.0;
        if (m == 0)
            return n * GAP_PENALTY;
        if (n == 0)
            return m * GAP_PENALTY;

        // 动态规划表
        double[][] dp = new double[m + 1][n + 1];

        // 初始化第一行和第一列
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i * GAP_PENALTY;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j * GAP_PENALTY;
        }

        // 填充动态规划表
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                // 匹配/替换代价
                double substitutionCost = getSubstitutionCost(seq1[i - 1], seq2[j - 1]);
                double match = dp[i - 1][j - 1] + substitutionCost;

                // 删除代价
                double delete = dp[i - 1][j] + GAP_PENALTY;

                // 插入代价
                double insert = dp[i][j - 1] + GAP_PENALTY;

                // 选择代价最小的操作
                dp[i][j] = Math.min(match, Math.min(delete, insert));
            }
        }

        return dp[m][n];
    }

    /**
     * 获取两个氨基酸之间的替代代价
     * @param aa1 第一个氨基酸的编码值（0-20）
     * @param aa2 第二个氨基酸的编码值（0-20）
     * @return 替代代价（来自mPAM矩阵）
     */
    private double getSubstitutionCost(byte aa1, byte aa2) {
        // 确保索引在有效范围内
        int index1 = Math.min(Math.max(aa1, 0), 20);
        int index2 = Math.min(Math.max(aa2, 0), 20);

        return MPAM_MATRIX[index1][index2];
    }

    /**
     * 获取距离函数的名称
     * @return 距离函数的名称
     */
    @Override
    public String getMetricName() {
        return "Alignment Distance (based on mPAM250a matrix)";
    }

    @Override
    public String toString() {
        return getMetricName();
    }
}

