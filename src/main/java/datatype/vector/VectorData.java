package datatype.vector;

import core.MetricSpaceData;
import java.util.Arrays;

/**
 * 向量数据类型
 *
 * 表示欧几里得空间中的向量，继承自MetricSpaceData。
 * 支持从数组和字符串两种方式构造向量对象。
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class VectorData extends MetricSpaceData {
    private static final long serialVersionUID = 1L;

    /**
     * 向量的坐标值
     */
    private double[] coordinates;

    /**
     * 从坐标数组构造向量
     * @param id 向量的唯一标识ID
     * @param coordinates 向量的坐标数组
     * @throws IllegalArgumentException 如果坐标数组为空或null
     */
    public VectorData(int id, double[] coordinates) {
        super(id);
        if (coordinates == null || coordinates.length == 0) {
            throw new IllegalArgumentException("坐标数组不能为空");
        }
        this.coordinates = Arrays.copyOf(coordinates, coordinates.length);
    }

    /**
     * 从字符串解析向量
     * 字符串格式：坐标值之间用空格分隔，例如 "1.0 2.0 3.0"
     * @param id 向量的唯一标识ID
     * @param dataLine 包含坐标值的字符串
     * @throws IllegalArgumentException 如果字符串格式不正确
     */
    public VectorData(int id, String dataLine) {
        super(id);
        if (dataLine == null || dataLine.trim().isEmpty()) {
            throw new IllegalArgumentException("数据字符串不能为空");
        }

        String[] parts = dataLine.trim().split("\\s+");
        this.coordinates = new double[parts.length];

        try {
            for (int i = 0; i < parts.length; i++) {
                this.coordinates[i] = Double.parseDouble(parts[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无法解析坐标值: " + dataLine, e);
        }
    }

    /**
     * 获取向量的维度
     * @return 向量的维度
     */
    @Override
    public int getDimension() {
        return coordinates.length;
    }

    /**
     * 获取向量的坐标数组（返回副本以保护内部状态）
     * @return 向量的坐标数组副本
     */
    public double[] getCoordinates() {
        return Arrays.copyOf(coordinates, coordinates.length);
    }

    /**
     * 获取指定维度的坐标值
     * @param index 维度索引（从0开始）
     * @return 该维度的坐标值
     * @throws IndexOutOfBoundsException 如果索引超出范围
     */
    public double getCoordinate(int index) {
        if (index < 0 || index >= coordinates.length) {
            throw new IndexOutOfBoundsException("维度索引超出范围: " + index);
        }
        return coordinates[index];
    }

    /**
     * 获取向量的字符串表示
     * @return 格式为 "VectorData[id=X, dim=Y, coords=[...]]"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VectorData[id=").append(dataId);
        sb.append(", dim=").append(coordinates.length);
        sb.append(", coords=[");

        for (int i = 0; i < Math.min(coordinates.length, 5); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(String.format("%.4f", coordinates[i]));
        }

        if (coordinates.length > 5) {
            sb.append(", ...");
        }

        sb.append("]]");
        return sb.toString();
    }

    /**
     * 判断两个向量是否相等
     * 两个向量相等当且仅当它们的维度相同且所有坐标值都相等
     * @param obj 另一个对象
     * @return 如果相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        VectorData other = (VectorData) obj;
        return Arrays.equals(this.coordinates, other.coordinates);
    }

    /**
     * 获取向量的哈希码
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }
}

