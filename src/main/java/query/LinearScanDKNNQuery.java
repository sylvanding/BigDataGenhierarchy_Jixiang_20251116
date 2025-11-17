package query;

import core.MetricFunction;
import core.MetricSpaceData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 线性扫描多样化k近邻查询
 *
 * 使用贪心算法选择多样化的k个最近邻：
 * 1. 首先执行kNN查询获取候选集
 * 2. 从候选集中贪心选择多样性最大的k个对象
 *
 * @author Jixiang Ding
 * @version 1.0
 */
public class LinearScanDKNNQuery {

    /**
     * 执行dkNN查询
     * @param dataset 数据集
     * @param query 查询对象
     * @param metric 距离函数
     * @return dkNN结果列表
     */
    public static List<KNNResult> execute(List<? extends MetricSpaceData> dataset, DKNNQuery query,
            MetricFunction metric) {
        return execute(dataset, query, metric, true);
    }

    /**
     * 执行dkNN查询
     * @param dataset 数据集
     * @param query 查询对象
     * @param metric 距离函数
     * @param verbose 是否打印详细信息
     * @return dkNN结果列表
     */
    public static List<KNNResult> execute(List<? extends MetricSpaceData> dataset, DKNNQuery query,
            MetricFunction metric, boolean verbose) {

        // 第一步: 执行kNN查询，获取更多候选（候选集大小是k的10倍）
        int candidateSize = Math.min(query.getK() * 10, dataset.size());
        KNNQuery knnQuery = new KNNQuery(query.getQueryObject(), candidateSize);
        List<KNNResult> candidates = LinearScanKNNQuery.execute(dataset, knnQuery, metric, verbose);

        if (verbose) {
            System.out.println("\ndkNN多样化选择过程:");
            System.out.println("  候选集大小: " + candidates.size());
            System.out.println("  目标选择数: " + query.getK());
            System.out.println("  多样性权重: " + query.getDiversityWeight());
        }

        // 第二步: 从候选集中贪心选择多样化的k个对象
        List<KNNResult> results = new ArrayList<>();
        Set<MetricSpaceData> selected = new HashSet<>();

        if (!candidates.isEmpty()) {
            // 选择第一个（距离查询对象最近的）
            KNNResult first = candidates.get(0);
            results.add(first);
            selected.add(first.getData());
            if (verbose) {
                System.out.println("  第1个选择: " + first);
            }
        }

        // 贪心选择剩余的k-1个对象
        int iteration = 1;
        while (results.size() < query.getK() && results.size() < candidates.size()) {
            MetricSpaceData bestCandidate = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            double bestDistToQuery = 0;
            double bestMinDistToSelected = 0;

            for (KNNResult candidate : candidates) {
                if (selected.contains(candidate.getData())) {
                    continue;
                }

                // 计算该候选对象的得分
                // 得分 = (1-w) * (-距离查询对象) + w * (到已选对象的最小距离)
                // 距离查询对象越近越好（用负值），到已选对象越远越好
                double distToQuery = candidate.getDistance();
                double minDistToSelected =
                        computeMinDistanceToSelected(candidate.getData(), selected, metric);

                double score = (1 - query.getDiversityWeight()) * (-distToQuery)
                        + query.getDiversityWeight() * minDistToSelected;

                if (score > bestScore) {
                    bestScore = score;
                    bestCandidate = candidate.getData();
                    bestDistToQuery = distToQuery;
                    bestMinDistToSelected = minDistToSelected;
                }
            }

            if (bestCandidate != null) {
                double dist = metric.getDistance(query.getQueryObject(), bestCandidate);
                KNNResult result = new KNNResult(bestCandidate, dist);
                results.add(result);
                selected.add(bestCandidate);

                iteration++;
                if (verbose) {
                    System.out.println(
                            String.format("  第%d个选择: dist=%.4f, minDistToSelected=%.4f, score=%.4f",
                                    iteration, bestDistToQuery, bestMinDistToSelected, bestScore));
                }
            } else {
                break;
            }
        }

        if (verbose) {
            System.out.println("dkNN查询完成，返回" + results.size() + "个结果\n");
        }

        return results;
    }

    /**
     * 计算候选对象到已选对象集合的最小距离
     */
    private static double computeMinDistanceToSelected(MetricSpaceData candidate,
            Set<MetricSpaceData> selected, MetricFunction metric) {

        double minDist = Double.MAX_VALUE;
        for (MetricSpaceData s : selected) {
            double dist = metric.getDistance(candidate, s);
            minDist = Math.min(minDist, dist);
        }
        return minDist;
    }
}

