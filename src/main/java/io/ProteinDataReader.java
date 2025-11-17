package io;

import datatype.protein.ProteinData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 蛋白质序列数据读取器
 *
 * 从FASTA格式文件读取蛋白质序列数据。
 * FASTA格式：
 * - 以">"开头的行是序列描述信息
 * - 其他行是序列数据（可以跨多行）
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class ProteinDataReader {

    /**
     * 从FASTA格式文件读取蛋白质序列
     * @param filePath 文件路径
     * @param maxCount 最多读取的序列数量，0表示读取全部
     * @param fragmentLength 片段长度（用于切分长序列为固定长度的片段，0表示不切分）
     * @return 蛋白质序列列表
     * @throws IOException 如果文件读取失败
     */
    public static List<ProteinData> readFromFile(String filePath, int maxCount, int fragmentLength)
            throws IOException {
        List<ProteinData> proteins = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            System.out.println("数据集信息：");
            System.out.println("  文件路径: " + filePath);
            System.out.println("  片段长度: " + (fragmentLength > 0 ? fragmentLength : "完整序列"));

            String line;
            StringBuilder currentSequence = new StringBuilder();
            String currentHeader = null;
            int sequenceCount = 0;
            int id = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith(">")) {
                    // 处理前一个序列
                    if (currentSequence.length() > 0) {
                        List<ProteinData> fragments =
                                processSequence(id, currentSequence.toString(), fragmentLength);
                        proteins.addAll(fragments);
                        id += fragments.size();
                        sequenceCount++;

                        // 显示进度
                        if (sequenceCount % 100 == 0) {
                            System.out.println(
                                    "  已读取序列: " + sequenceCount + ", 片段总数: " + proteins.size());
                        }

                        // 检查是否达到最大数量
                        if (maxCount > 0 && proteins.size() >= maxCount) {
                            break;
                        }
                    }

                    // 开始新序列
                    currentHeader = line.substring(1);
                    currentSequence = new StringBuilder();
                } else {
                    // 累积序列数据
                    currentSequence.append(line.toUpperCase());
                }
            }

            // 处理最后一个序列
            if (currentSequence.length() > 0 && (maxCount == 0 || proteins.size() < maxCount)) {
                List<ProteinData> fragments =
                        processSequence(id, currentSequence.toString(), fragmentLength);
                int toAdd = (maxCount > 0) ? Math.min(fragments.size(), maxCount - proteins.size())
                        : fragments.size();
                proteins.addAll(fragments.subList(0, toAdd));
                sequenceCount++;
            }

            System.out.println("成功读取 " + sequenceCount + " 个序列");
            System.out.println("生成 " + proteins.size() + " 个数据对象\n");
        }

        return proteins;
    }

    /**
     * 处理单个序列，可能切分为多个片段
     * @param startId 起始ID
     * @param sequence 完整序列
     * @param fragmentLength 片段长度，0表示不切分
     * @return 蛋白质数据对象列表
     */
    private static List<ProteinData> processSequence(int startId, String sequence,
            int fragmentLength) {
        List<ProteinData> result = new ArrayList<>();

        // 过滤无效字符（只保留字母）
        StringBuilder cleanSequence = new StringBuilder();
        for (char c : sequence.toCharArray()) {
            if (Character.isLetter(c)) {
                cleanSequence.append(c);
            }
        }

        String cleanSeq = cleanSequence.toString();
        if (cleanSeq.isEmpty()) {
            return result;
        }

        if (fragmentLength <= 0 || cleanSeq.length() <= fragmentLength) {
            // 不切分，返回完整序列
            result.add(new ProteinData(startId, cleanSeq));
        } else {
            // 切分为固定长度的片段（滑动窗口）
            for (int i = 0; i <= cleanSeq.length() - fragmentLength; i++) {
                String fragment = cleanSeq.substring(i, i + fragmentLength);
                result.add(new ProteinData(startId + i, fragment));
            }
        }

        return result;
    }

    /**
     * 从FASTA文件读取蛋白质序列（不切分）
     * @param filePath 文件路径
     * @param maxCount 最多读取的序列数量
     * @return 蛋白质序列列表
     * @throws IOException 如果文件读取失败
     */
    public static List<ProteinData> readFromFile(String filePath, int maxCount) throws IOException {
        return readFromFile(filePath, maxCount, 0);
    }
}

