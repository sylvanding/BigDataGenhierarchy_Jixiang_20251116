package datatype.protein;

import core.MetricSpaceData;

/**
 * 蛋白质序列数据类型
 *
 * 表示氨基酸序列，继承自MetricSpaceData。
 * 支持20种标准氨基酸和未知氨基酸。
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class ProteinData extends MetricSpaceData {
    private static final long serialVersionUID = 1L;

    /**
     * 20种标准氨基酸
     * 顺序：A, R, N, D, C, Q, E, G, H, I, L, K, M, F, P, S, T, W, Y, V
     */
    private static final String AMINO_ACIDS = "ARNDCQEGHILKMFPSTWYV";

    /**
     * 未知或非标准氨基酸的索引值
     */
    private static final byte UNKNOWN_AMINO_ACID = 20;

    /**
     * 氨基酸序列（字符串形式）
     */
    private String sequence;

    /**
     * 编码后的序列（字节数组）
     * 每个字节表示一个氨基酸的索引（0-19表示标准氨基酸，20表示未知）
     */
    private byte[] encodedSequence;

    /**
     * 从序列字符串构造蛋白质数据对象
     * @param id 数据对象的唯一标识ID
     * @param sequence 氨基酸序列（字符串）
     * @throws IllegalArgumentException 如果序列为空或null
     */
    public ProteinData(int id, String sequence) {
        super(id);
        if (sequence == null || sequence.isEmpty()) {
            throw new IllegalArgumentException("蛋白质序列不能为空");
        }

        // 转换为大写
        this.sequence = sequence.toUpperCase();

        // 编码序列
        this.encodedSequence = new byte[this.sequence.length()];
        for (int i = 0; i < this.sequence.length(); i++) {
            char aa = this.sequence.charAt(i);
            this.encodedSequence[i] = encodeAminoAcid(aa);
        }
    }

    /**
     * 编码单个氨基酸为字节
     * @param aa 氨基酸字符
     * @return 氨基酸的索引（0-19表示标准氨基酸，20表示未知）
     */
    private byte encodeAminoAcid(char aa) {
        int index = AMINO_ACIDS.indexOf(aa);
        if (index == -1) {
            // 处理非标准氨基酸（B, Z, U, X等）
            return UNKNOWN_AMINO_ACID;
        }
        return (byte) index;
    }

    /**
     * 获取序列长度（维度）
     * @return 序列长度
     */
    @Override
    public int getDimension() {
        return sequence.length();
    }

    /**
     * 获取氨基酸序列（字符串形式）
     * @return 氨基酸序列
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * 获取编码后的序列（字节数组）
     * @return 编码序列的副本
     */
    public byte[] getEncodedSequence() {
        byte[] copy = new byte[encodedSequence.length];
        System.arraycopy(encodedSequence, 0, copy, 0, encodedSequence.length);
        return copy;
    }

    /**
     * 获取指定位置的氨基酸字符
     * @param index 位置索引（从0开始）
     * @return 该位置的氨基酸字符
     * @throws IndexOutOfBoundsException 如果索引超出范围
     */
    public char getAminoAcidAt(int index) {
        if (index < 0 || index >= sequence.length()) {
            throw new IndexOutOfBoundsException("索引超出范围: " + index);
        }
        return sequence.charAt(index);
    }

    /**
     * 获取指定位置的编码值
     * @param index 位置索引（从0开始）
     * @return 该位置的氨基酸编码值（0-20）
     * @throws IndexOutOfBoundsException 如果索引超出范围
     */
    public byte getEncodedAminoAcidAt(int index) {
        if (index < 0 || index >= encodedSequence.length) {
            throw new IndexOutOfBoundsException("索引超出范围: " + index);
        }
        return encodedSequence[index];
    }

    /**
     * 获取蛋白质序列的字符串表示
     * @return 格式为 "ProteinData[id=X, length=Y, seq=...]"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProteinData[id=").append(dataId);
        sb.append(", length=").append(sequence.length());
        sb.append(", seq=");

        // 如果序列较短，显示全部；否则只显示前后各10个氨基酸
        if (sequence.length() <= 30) {
            sb.append(sequence);
        } else {
            sb.append(sequence.substring(0, 10));
            sb.append("...");
            sb.append(sequence.substring(sequence.length() - 10));
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * 判断两个蛋白质序列是否相等
     * 两个序列相等当且仅当它们的序列字符串相同
     * @param obj 另一个对象
     * @return 如果相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ProteinData other = (ProteinData) obj;
        return this.sequence.equals(other.sequence);
    }

    /**
     * 获取对象的哈希码
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return sequence.hashCode();
    }
}

