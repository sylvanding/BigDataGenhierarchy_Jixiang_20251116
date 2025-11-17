package index;

/**
 * 支撑点选择方法枚举
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public enum PivotSelectionMethod {
    /**
     * 随机选择
     */
    RANDOM,

    /**
     * Farthest-First Traversal (FFT)
     * 每次选择距离已选支撑点最远的点
     */
    FFT,

    /**
     * 选择距离数据集中心最近的点
     */
    CENTER,

    /**
     * 选择距离数据集边界最近的点
     */
    BORDER
}

