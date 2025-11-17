package datatype.vector;

import core.MetricFunction;
import core.MetricSpaceData;

/**
 * 闵可夫斯基距离（Minkowski Distance）
 *
 * 闵可夫斯基距离是向量空间中的一类距离函数，定义为：
 * L_p(x, y) = (Σ|x_i - y_i|^p)^(1/p), 其中 p >= 1
 *
 * 特殊情况：
 * - p = 1: 曼哈顿距离（Manhattan Distance）
 * - p = 2: 欧几里得距离（Euclidean Distance）
 * - p = ∞: 切比雪夫距离（Chebyshev Distance）
 * 
 * @author Jixiang Ding
 * @version 1.0
 */
public class MinkowskiDistance implements MetricFunction {
    private static final long serialVersionUID = 1L;

    /**
     * L-p距离的p值
     * p = 0 表示 L∞ 距离（切比雪夫距离）
     * p >= 1 表示 L-p 距离
     */
    private final int p;

    /**
     * 常用距离的静态实例
     */
    public static final MinkowskiDistance L1 = new MinkowskiDistance(1); // 曼哈顿距离
    public static final MinkowskiDistance L2 = new MinkowskiDistance(2); // 欧几里得距离
    public static final MinkowskiDistance LINF = new MinkowskiDistance(0); // 切比雪夫距离

    /**
     * 构造闵可夫斯基距离函数
     * @param p L-p距离的p值，p=0表示L∞，p>=1表示L-p
     * @throws IllegalArgumentException 如果p < 0
     */
    public MinkowskiDistance(int p) {
        if (p < 0) {
            throw new IllegalArgumentException("p值必须 >= 0");
        }
        this.p = p;
    }

    /**
     * 获取p值
     * @return p值，0表示L∞，其他值表示L-p
     */
    public int getP() {
        return p;
    }

    /**
     * 计算两个向量之间的闵可夫斯基距离
     * @param obj1 第一个向量
     * @param obj2 第二个向量
     * @return 两个向量之间的距离
     * @throws IllegalArgumentException 如果输入不是VectorData类型或维度不匹配
     */
    @Override
    public double getDistance(MetricSpaceData obj1, MetricSpaceData obj2) {
        // 类型检查
        if (!(obj1 instanceof VectorData) || !(obj2 instanceof VectorData)) {
            throw new IllegalArgumentException("闵可夫斯基距离只能应用于向量数据类型");
        }

        VectorData v1 = (VectorData) obj1;
        VectorData v2 = (VectorData) obj2;

        // 维度检查
        if (v1.getDimension() != v2.getDimension()) {
            throw new IllegalArgumentException(
                    String.format("向量维度不匹配: v1=%d, v2=%d", v1.getDimension(), v2.getDimension()));
        }

        return calculateLpDistance(v1.getCoordinates(), v2.getCoordinates());
    }

    /**
     * 计算两个向量数组之间的L-p距离
     * @param v1 第一个向量的坐标数组
     * @param v2 第二个向量的坐标数组
     * @return L-p距离
     */
    private double calculateLpDistance(double[] v1, double[] v2) {
        if (p == 0) {
            // L∞距离（切比雪夫距离）：max(|x_i - y_i|)
            return calculateLInfDistance(v1, v2);
        } else if (p == 1) {
            // L1距离（曼哈顿距离）：Σ|x_i - y_i|
            return calculateL1Distance(v1, v2);
        } else if (p == 2) {
            // L2距离（欧几里得距离）：sqrt(Σ(x_i - y_i)^2)
            return calculateL2Distance(v1, v2);
        } else {
            // 通用L-p距离：(Σ|x_i - y_i|^p)^(1/p)
            return calculateGeneralLpDistance(v1, v2);
        }
    }

    /**
     * 计算L1距离（曼哈顿距离）
     */
    private double calculateL1Distance(double[] v1, double[] v2) {
        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.abs(v1[i] - v2[i]);
        }
        return sum;
    }

    /**
     * 计算L2距离（欧几里得距离）
     */
    private double calculateL2Distance(double[] v1, double[] v2) {
        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            double diff = v1[i] - v2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * 计算L∞距离（切比雪夫距离）
     */
    private double calculateLInfDistance(double[] v1, double[] v2) {
        double maxDiff = 0.0;
        for (int i = 0; i < v1.length; i++) {
            double diff = Math.abs(v1[i] - v2[i]);
            maxDiff = Math.max(maxDiff, diff);
        }
        return maxDiff;
    }

    /**
     * 计算通用L-p距离
     */
    private double calculateGeneralLpDistance(double[] v1, double[] v2) {
        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.pow(Math.abs(v1[i] - v2[i]), p);
        }
        return Math.pow(sum, 1.0 / p);
    }

    /**
     * 获取距离函数的名称
     * @return 距离函数的名称
     */
    @Override
    public String getMetricName() {
        if (p == 0) {
            return "L-Infinity (Chebyshev Distance)";
        } else if (p == 1) {
            return "L-1 (Manhattan Distance)";
        } else if (p == 2) {
            return "L-2 (Euclidean Distance)";
        } else {
            return "L-" + p + " (Minkowski Distance)";
        }
    }

    @Override
    public String toString() {
        return getMetricName();
    }
}

