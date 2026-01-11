package io;

import datatype.vector.VectorData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 向量数据读取器
 *
 * 从UMAD数据集中读取向量数据。
 * 数据文件格式：
 * 第一行：维度 数据数量
 * 后续行：坐标1 坐标2 ... 坐标n（空格分隔）
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class VectorDataReader {

    /**
     * 从文件读取向量数据
     * @param filePath 文件路径
     * @param maxCount 最多读取的数据数量，0表示读取全部
     * @return 向量数据列表
     * @throws IOException 如果文件读取失败
     */
    public static List<VectorData> readFromFile(String filePath, int maxCount) throws IOException {
        return readFromFile(filePath, maxCount, true);
    }

    /**
     * 从文件读取向量数据
     * @param filePath 文件路径
     * @param maxCount 最多读取的数据数量，0表示读取全部
     * @param verbose 是否打印详细信息
     * @return 向量数据列表
     * @throws IOException 如果文件读取失败
     */
    public static List<VectorData> readFromFile(String filePath, int maxCount, boolean verbose)
            throws IOException {
        List<VectorData> vectors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 读取第一行：维度和数据总量
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("文件为空");
            }

            int[] header = parseHeader(headerLine);
            int dimension = header[0];
            int totalCount = header[1];

            if (verbose) {
                System.out.println("数据集信息：");
                System.out.println("  文件路径: " + filePath);
                System.out.println("  向量维度: " + dimension);
                System.out.println("  数据总量: " + totalCount);
            }

            // 确定实际读取数量
            int readCount = (maxCount > 0) ? Math.min(maxCount, totalCount) : totalCount;
            if (verbose) {
                System.out.println("  读取数量: " + readCount);
            }

            // 读取向量数据
            String line;
            int id = 0;
            int linesRead = 0;

            while ((line = reader.readLine()) != null && linesRead < readCount) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    VectorData vector = new VectorData(id, line);

                    // 验证维度
                    if (vector.getDimension() != dimension) {
                        System.err.println("警告：向量 " + id + " 的维度不匹配，跳过");
                        continue;
                    }

                    vectors.add(vector);
                    id++;
                    linesRead++;

                    // 显示进度
                    if (verbose && linesRead % 10000 == 0) {
                        System.out.println("  已读取: " + linesRead + " 条");
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("警告：解析向量失败，跳过该行: " + e.getMessage());
                }
            }

            if (verbose) {
                System.out.println("成功读取 " + vectors.size() + " 个向量\n");
            }
        }

        return vectors;
    }

    /**
     * 读取文件头信息
     * @param filePath 文件路径
     * @return [维度, 数据总量]
     * @throws IOException 如果文件读取失败
     */
    public static int[] readHeader(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("文件为空");
            }
            return parseHeader(headerLine);
        }
    }

    /**
     * 解析文件头
     * @param headerLine 头部行字符串
     * @return [维度, 数据总量]
     */
    private static int[] parseHeader(String headerLine) {
        String[] parts = headerLine.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("文件头格式不正确: " + headerLine);
        }

        try {
            int dimension = Integer.parseInt(parts[0]);
            int count = Integer.parseInt(parts[1]);
            return new int[] {dimension, count};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无法解析文件头: " + headerLine, e);
        }
    }
}
