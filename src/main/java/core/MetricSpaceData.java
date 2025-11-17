package core;

import java.io.Serializable;

/**
 * 度量空间数据抽象父类
 *
 * 这是所有度量空间数据类型的抽象基类，定义了度量空间数据对象的基本接口。
 * 任何可以定义距离函数的数据类型都应该继承此类。
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public abstract class MetricSpaceData implements Serializable, Comparable<MetricSpaceData> {
    private static final long serialVersionUID = 1L;

    /**
     * 数据对象的唯一标识ID
     */
    protected int dataId;

    /**
     * 构造函数
     * @param dataId 数据对象的唯一标识ID
     */
    public MetricSpaceData(int dataId) {
        this.dataId = dataId;
    }

    /**
     * 获取数据对象的ID
     * @return 数据对象的唯一标识ID
     */
    public int getDataId() {
        return dataId;
    }

    /**
     * 设置数据对象的ID
     * @param dataId 数据对象的唯一标识ID
     */
    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    /**
     * 获取数据的维度或大小
     * 对于向量数据，返回向量的维度
     * 对于序列数据，返回序列的长度
     * @return 数据的维度或大小
     */
    public abstract int getDimension();

    /**
     * 获取数据的字符串表示
     * @return 数据的字符串表示，用于调试和输出
     */
    @Override
    public abstract String toString();

    /**
     * 比较两个数据对象
     * 默认按照dataId进行比较
     * @param other 另一个数据对象
     * @return 比较结果：负数表示小于，0表示等于，正数表示大于
     */
    @Override
    public int compareTo(MetricSpaceData other) {
        return Integer.compare(this.dataId, other.dataId);
    }

    /**
     * 判断两个数据对象是否相等
     * 默认根据dataId判断
     * @param obj 另一个对象
     * @return 如果相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MetricSpaceData other = (MetricSpaceData) obj;
        return this.dataId == other.dataId;
    }

    /**
     * 获取对象的哈希码
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(dataId);
    }
}

