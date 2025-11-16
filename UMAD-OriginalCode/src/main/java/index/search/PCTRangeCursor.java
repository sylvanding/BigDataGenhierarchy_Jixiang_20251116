package index.search;

import algorithms.voronoi.Voronoi;
import algorithms.voronoi.VoronoiEdge;
import db.type.IndexObject;
import index.structure.PCTInternalNode;
import index.structure.Node;
import index.type.NodeSearchAction;
import manager.ObjectIOManager;
import metric.LMetric;
import metric.Metric;

import java.util.*;

/**
 *
 * <p>
 *     PCT索引树的范围搜索结果。我们知道索引树结构的不同，决定了不同类型索引树搜索算法的不同。
 *     PCT索引树的搜索过程可以分为两个大的步骤，首先是在支撑点空间中筛选出落在查询超立方体中的数据，然后针对前一步中筛选出的数据
 *     进行一次线性扫描判断最终的范围查询结果。搜索的伪代码如下：
 *     <pre>
 *         Result search(Centroids, q, r):
 *            Result = Centroids;
 *            for i in 0, 1, …, m:
 *            for j in i+1, i+2, …, m:
 *             int ans = locateQB(Centroids, i, j, q, r); //判断查询超立方体的位置
 *             if ans > 0:
 *                 Result.remove(Centroids[j]);   //查询超立方体完全位于i侧
 *             else if ans < 0:
 *                 Result.remove(Centroids[i]);   //查询超立方体完全位于j侧
 *            return Result;
 *     </pre>
 *     其中m是PCT索引树每层的划分块数，Centroids是每层的维诺超多面体的站点组成的集合，q为查询对象在支撑点空间的像，r为范围查询半径。
 * </p><br><br>
 *     {@code locateQB()}函数是查询超立方体的定位函数。主要用于判断查询超立方体QB(q)和维诺超多面体VR(c_i)、VR(c_j)的位置关系。<br>
 *      对于判断查询超立方体QB(q)和VR(c_i )、VR(c_j )的位置关系，一般有以下三种处理方法：<br>
 *          <ul>
 *              <li>
 *                  方法一：仿造GH的处理方法<br>
 *                      <strong>定理1</strong><br>
 *                      对于任意两个维诺超多面体VR(c_i )和VR(c_j)，若d(q,c_i )-d(q,c_j )>2√k r,则d(x,c_i )>d(x,c_j )，∀x∈VR(c_j)恒成立。
 *                      其中q为查询对象在支撑点空间的像，r为范围查询半径，k为支撑点空间的维度。<br>
 *                      因此我们可以使用双曲线𝑑𝑞,𝑐𝑖−d(q,𝑐𝑗)=2𝑘r来描述临界的情况，算法的伪代码如下:
 *                      <pre>
 *                          int locateQB(C, i, j, q, r):
 *                              k = q.dimension; //获取支撑点空间维度
 *                              if d(C[i], q) – d(C[j], q) > 2√k r:
 *                                  return -1;
 *                              else if d(C[j], q) – d(C[i], q) > 2√k r
 *                                  return 1;
 *                              return 0;
 *                      </pre>
 *              </li>
 *              <li>
 *                  方法二：向量法<br>
 *                       度量空间由于没有坐标，只能利用三角不等式，因此GH树的搜索判断方式在支撑点空间中并不是最优。
 *                       因为支撑点空间也是多维空间，所以我们可以利用空间解析几何的办法判断查询超立方体的顶点是否在超平面的同一侧。
 *                       <strong>定理2</strong><br>
 *                           假设Vertex(q) 为以查询点q为中心的查询超立方体QB(q)的顶点的集合， c_i和c_j为任意两个位置互异的站点，
 *                           a 为c_i和c_j连线的中点，则QB(q)⊂VR(c_j )充要条件是∀v∈Vertex(q),(av) ⃗⋅(c_i c_j ) ⃗≥0恒成立。<br>
 *                               算法的伪代码如下：
 *                               <pre>
 *                                   int locateQB(C, i, j, q, r):
 *                                      k = q.dimension;   //获取支撑点空间的维度
 *                                      a = calMidPoint(C[i], C[j]);  //计算中点a
 *                                      cicj = initVector(C[i], C[j]);  //计算向量cicj
 *                                      vote = 0;
 *                                      for v in Vertex(q):
 *                                          av = initVector(a, v);
 *                                          r = cdot(av, cicj);
 *                                          if r < 0:
 *                                              vote += 1;
 *                                          else if r > 0:
 *                                              vote -= 1;
 *                                      if abs(vote) != pow(2, k):
 *                                          return 0;
 *                                      return vote;
 *                               </pre>
 *              </li>
 *              <li>
 *                  方法三：r-邻域法<br>
 *                      r-邻域指的是划分边界周围，落入其中的查询点需要同时搜索两侧数据的区域。除了上述方法外，我们还可以求查询点到超平面的距离，
 *                      然后利用划分超平面的r-邻域进行排除。如果查询点到超平面的距离小于划分超平面的r-邻域，则两侧都不能排除，
 *                      都需要进一步的搜索,否则可以排除一侧。<br>
 *                      设维诺超平面的法向量α ⃗=(a_1,…,a_n )，查询半径为r,则r邻域大小为：<br>
 *                              N(r)=2*r*((∑_(i=1)^(i=n)▒|a_i | )/√(∑_(i=1)^(i=n)▒a_i^2 ))<br>
 *                      算法的伪代码如下:
 *                      <pre>
 *                          int locateQB(C, i, j, q, r):
 *                              cicj = initVector(C[i], C[j]);  //计算向量cicj
 *                              Nr = calRNeighbour(cicj, r);  //计算r邻域宽度
 *                              dis = calDisFromPlane(q, P(i), P(j));  //计算到划分平面的距离
 *                              if dis < Nr/2:
 *                                  return 0;
 *                              if d(q, C[i]) > d(q, C[j]):
 *                                  return 1;
 *                              else
 *                                  return -1;
 *                      </pre>
 *              </li>
 *          </ul>
 *          <br><br>
 *              前面给出了搜索算法的伪代码。从伪代码中我们可以看到搜索算法的核心是内部的O(n^2)的双重循环判断。针对双重循环的判断，
 *              我们分别使用了排除定理和MBR对查询进行优化。<br>
 *                  <strong>排除定理</strong><br>
 *                      定义所有站点所构成的站点集合为C，以q为中心的查询超立方体顶点所构成的集合为Vertex(q)。
 *                      对于任意一个站点c_i （c_i∈C），将其作为依据点判断站点c_j （c_j∈C且i≠j）所在的维诺超多面体VR(c_j)能否被排除，
 *                      如果 dis(v,c_i )<dis(v,c_j )，∀v∈Vertex(q)恒成立，那么VR(c_j)可以被排除，且Exclude(c_j )⊂Exclude(c_i )。
 *                      其中，dis(v,c_i )表示顶点v(v∈Vertex(q))和站点c_i （c_i∈C）的距离，Exclude(c_i )表示站点c_i作为依据点
 *                      能排除的维诺超多面体的站点所构成的集合。
 *                      <br>
 *                          排除定理说明了能被站点c_j排除掉的维诺超多面体，也能被站点c_i所排除。
 *                          因此我们从查询立方体顶点所在的站点开始排除判断，此过程中被排除的维诺超多面体都可以直接排除（
 *                          即它们的站点作为依据点来判断其他站点所在的维诺超多面体并不能带来更多的排除率，因为c_j能排除的站点，c_i都可以排除）。
 *                          <br>
 *                              优化之后的搜索算法的伪代码如下:
 *                              <pre>
 *                                  Result search(C, q, r):
 *                                     Result ={};
 *                                     T = C;  //存储所有待判断的站点
 *                                     Vertex= generateVertex(q, r);  //生成查询超立方体的顶点集合
 *                                     for v in Vertex:
 *                                         pv = locateV(C, v);  //定位v所在的数据块的站点
 *                                         Result.add(pv);
 *                                     T.removeAll(Result);
 *                                     if Result.size==1:
 *                                         return Result;  //如果查询超立方体位于同一个数据块中，则直接返回结果
 *                                     for i in 0, 1, …, T.size:
 *                                         for j in i+1, i+2, …, T.size:
 *                                             int ans = locateQB(C, T[i].index, T[j].index, q, r); // T[i].index获取该站点在C中的下标
 *                                             if ans > 0:
 *                                                 T.remove(T[j]);   //查询超立方体完全位于i侧
 *                                             else if ans < 0:
 *                                                 T.remove(T[i]);   //查询超立方体完全位于j侧
 *                                     Result.addAll(T);
 *                                     return Result;
 *                              </pre>
 *                              <br>
 *       从前面我们知道，相似性查询Q(q,r)在度量空间中表现为一个以查询对象q为球心的半径为r的超球体。
 *       而映射到支撑点空间中后，表现为一个以查询对象q为中心，边长为2r的超立方体QB(q)，且超立方体的每个超平面均平行于每个维度上的坐标平面。
 *       在数学上，集合的最小限定矩形（Minimum Bound Rectangle）指的是描述几何对象（如点、线、面等）最大范围的矩形。
 *       由于查询超立方体的每个超平面均平行于支撑点空间的每个维度的坐标平面，因此我们可以尝试存储每个数据块在每个维度上的数据边界，
 *       构建数据的最小限定超立方体。在查询的时候通过最小限定超立方体进行预排除，以此来减少搜索算法中O(n^2)循环的循环次数。<br>
 *           综合了上述优化后的搜索的伪代码如下:<br>
 *               <pre>
 *                   Action[] search(C, q, r):
 *                     Unknown = {}; //初始化待搜索集合为空
 *                     LinearScan = {};  //初始化线性扫描集合为空
 *                     T = C;  //存储所有待判断的站点
 *                     Vertex = generateVertex(q, r);  //生成查询超立方体的顶点集合
 *                     for v in Vertex:
 *                         pv = locateV(C, v);  //定位v所在的数据块的站点
 *                         Unknown.add(pv);
 *                     T.removeAll(Unknown);
 *                     if Unknown.size==1:
 *                         return createAction(Unknown, LinearScan);  //如果查询超立方体位于同一个数据块中，则直接返回结果
 *                     for t in T:
 *                         if isVRInQB(t, q, r):  //如果子块完全位于查询超立方体中
 *                             LinearScan.add(t);  //该子块直接进行线性扫描
 *                         else if isVRNoneQB(t, q, r):
 *                             T.remove(t);
 *                         else:
 *                             Unknown.add(t);
 *                     for i in 0, 1, …, T.size:
 *                         for j in i+1, i+2, …, T.size:
 *                             int ans = locateQB(C, T[i].index, T[j].index, q, r); // T[i].index获取该站点在C中的下标
 *                             if ans > 0:
 *                                 T.remove(T[j]);   //查询超立方体完全位于i侧
 *                             else if ans < 0:
 *                                 T.remove(T[i]);   //查询超立方体完全位于j侧
 *                     Unknown.addAll(T);
 *                     return createAction(Unknown, LinearScan);
 *               </pre>
 *
 *
 *
 *
 * @see index.structure.PCTIndex
 * @author liulinfeng 2023/1/4
 */
public class PCTRangeCursor extends RangeCursor
{
    /**
     * 范围查询构造函数
     *
     * @param oiom        io读写对象
     * @param metric      搜索使用的距离函数
     * @param rootAddress 要搜索的索引树的根节点
     */
    public PCTRangeCursor(ObjectIOManager oiom, Metric metric, long rootAddress)
    {
        super(oiom, metric, rootAddress);
    }

    /**
     * KMP范围搜索结果的构造函数
     *
     * @param query       查询对象
     * @param oiom        数据库读写对象
     * @param metric      建树映射使用的距离函数
     * @param rootAddress 树的根指针
     */
    public PCTRangeCursor(RangeQuery query, ObjectIOManager oiom, Metric metric, long rootAddress)
    {
        super(query, oiom, metric, rootAddress);
    }


//    /**
//     * 该方法返回对每个孩子的处理方法。
//     *
//     * <p>
//     * 不同种类的索引树实现该方法，通过枚举类{@link NodeSearchAction}标记{@link Node}的每个子节点的处理方法。
//     * 如果{@code NodeSearchAction[i] == RESULTNONE}表示第i个孩子节点被剪枝，稍后的搜索不进入该孩子。
//     * 如果{@code NodeSearchAction[i] == RESULTUNKNOWN}表示第i个孩子节点不能排除，稍后的搜索进入该孩子。
//     * 如果{@code NodeSearchAction[i] == RESULTALL}表示第i个孩子节点的所有数据都应该被添加到结果集中。
//     * </p>
//     *
//     * @param node                 需要搜索的节点
//     * @param metric               距离函数
//     * @param query                查询对象
//     * @param radius               查询半径
//     * @param queryToPivotDistance 查询对象到该节点的各个支撑点的距离
//     * @return 标记孩子状态的数组
//     * @see NodeSearchAction
//     */
//    @Override
//    public NodeSearchAction[] willTheSubTreeFurtherSearch(Node node, Metric metric, IndexObject query, double radius, double[] queryToPivotDistance)
//    {
//        if (!(node instanceof PCTInternalNode))
//            throw new RuntimeException("KMP范围搜索传入的必须是KMPInternalNode");
//        PCTInternalNode aNode = (PCTInternalNode) node;
//        //获取孩子的数目
//        int                numChildren = aNode.getNumChildren();
//        NodeSearchAction[] actions     = new NodeSearchAction[numChildren];
//        //初始化所有孩子节点为待搜索状态
//        Arrays.fill(actions, NodeSearchAction.RESULTUNKNOWN);
//        //遍历所有孩子对
//        for (int i = 0; i < numChildren; i++)
//        {
//            for (int j = 0; j < numChildren; j++)
//            {
//                //补丁，因为每次判断只排除对面的数据，后期把这个补丁靠标记去掉
//                if (i == j)
//                    continue;
//                //在这里标记处理方法
//                // 方案一、仿造GH的处理方法，d(c1,q) - d(c2, q) > 2*r  可以排除c1子树。该方法未充分利用欧式空间
//                //                if (LMetric.EuclideanDistanceMetric.getDistance(aNode.getCentroidOf(i), queryToPivotDistance) - LMetric.EuclideanDistanceMetric.getDistance(aNode.getCentroidOf(j), queryToPivotDistance) > 2 * radius)
//                //                {
//                //                    actions[i] = NodeSearchAction.RESULTNONE;
//                //                }
//
//                // 方案二、 利用欧式空间的向量计算
//                // q(x0, y0....z0)  a(x1, y1...z1)  b(x2, y2....z2) 则a与b的中间点O((x1+x2...xk)/2, (y1+y2....yk)/2)
//                //搜索只需要判断q与c1和c2的垂直平分线之间的投影dis
//                //oq * ab/|ab| = dis
//                //dis < -k*r 排除b；  dis > k*r  排除a
//                //                double[] oq = new double[queryToPivotDistance.length];
//                //                double[] a = aNode.getCentroidOf(i);
//                //                double[] b = aNode.getCentroidOf(j);
//                //                //计算向量oq
//                //                for (int dim=0; dim< oq.length; dim++){
//                //                    oq[dim] = queryToPivotDistance[dim] - (a[dim] + b[dim])/2;
//                //                }
//                //                //计算单位向量ab
//                //                double[] ab = new double[queryToPivotDistance.length];
//                //                double l_ab = LMetric.EuclideanDistanceMetric.getDistance(a,b);
//                //                for (int dim=0; dim<ab.length; dim++){
//                //                    ab[dim] = (b[dim] - a[dim])/l_ab;
//                //                }
//                //                //计算投影dis
//                //                double dis = 0;
//                //                for (int dim=0; dim<oq.length; dim++){
//                //                    dis += oq[dim]*ab[dim];
//                //                }
//                //                //开始标记处理方法
//                //                double k = Math.pow(queryToPivotDistance.length, 0.5);
//                //                if (dis < -k*radius) actions[j] = NodeSearchAction.RESULTNONE;
//                //                if (dis > k*radius) actions[i] = NodeSearchAction.RESULTNONE;
//
//                //方案三、判断超立方体所有的顶点与划分边界是否有交点，没有交点则可以排除对面的划分块
//                //用于存储查询超立方体的顶点
//                List<double[]> allVertex = new ArrayList<>();
//                //生成超立方体所有的顶点
//               generateVertexHelp(queryToPivotDistance, radius, 0, new double[queryToPivotDistance.length], allVertex);
//
//                //计算单位向量ab
//                double[] a    = aNode.getCentroidOf(i);
//                double[] b    = aNode.getCentroidOf(j);
//                double[] ab   = new double[queryToPivotDistance.length];
//                double   l_ab = LMetric.EuclideanDistanceMetric.getDistance(a, b);
//                for (int dim = 0; dim < ab.length; dim++)
//                {
//                    ab[dim] = (b[dim] - a[dim]) / l_ab;
//                }
//                //判断顶点位置
//                //默认顶点都在i的一侧
//                boolean allAtI = true;
//                for (var vertex : allVertex)
//                {
//                    double[] ov = new double[vertex.length];
//
//                    //计算向量ov
//                    for (int dim = 0; dim < ov.length; dim++)
//                    {
//                        ov[dim] = vertex[dim] - (a[dim] + b[dim]) / 2;
//                    }
//
//                    //计算投影dis
//                    double dis = 0;
//                    for (int dim = 0; dim < ov.length; dim++)
//                    {
//                        dis += ov[dim] * ab[dim];
//                    }
//                    //投影大于0，证明这个顶点在j的一侧，其他顶点不用判断了，j侧无法排除
//                    if (dis > 0)
//                    {
//                        allAtI = false;
//                        break;
//                    }
//                }
//                //如果所有顶点都在i侧，则j可以排除
//                if (allAtI)
//                    actions[j] = NodeSearchAction.RESULTNONE;
//
//            }
//        }
//        return actions;
//    }


    @Override
    public NodeSearchAction[] willTheSubTreeFurtherSearch(Node node, Metric metric, IndexObject query, double radius, double[] queryToPivotDistance)
    {
        //划分时使用的是L2距离，则搜索的时候也要使用L2距离，所以传进来的metric被忽略，直接使用L2距离即可。
        if (!(node instanceof PCTInternalNode))
            throw new RuntimeException("KMP范围搜索传入的必须是KMPInternalNode");
        PCTInternalNode aNode = (PCTInternalNode) node;

//        double[][]      centroids = aNode.getCentroids();
//        for(int i=0; i<centroids.length; i++)
//            System.out.println(Arrays.toString(centroids[i]));

//        if(aNode.getNumPivots()==2){
//            //二维使用碰撞检测的方式搜索
//            return searchOn2PivotSpace(aNode, radius, queryToPivotDistance);
//        }
        NodeSearchAction[] search = search(aNode, radius, queryToPivotDistance);
        return search;
    }

    /**
     * 可以解决2维支撑点空间的搜索问题
     * @param node
     * @param radius
     * @param queryToPivotDistance
     * @return
     */
    private NodeSearchAction[] searchOn2PivotSpace(PCTInternalNode node, double radius, double[] queryToPivotDistance){
        //1.初始化相关的值
        Metric l2metric = LMetric.EuclideanDistanceMetric;
        int childrenNum = node.getNumChildren();
        int pivotsNum = node.getNumPivots();
        NodeSearchAction[] actions = new NodeSearchAction[childrenNum];
        //假设所有孩子都不需要搜索
        for (int i = 0; i < childrenNum; i++)
        {
            actions[i] = NodeSearchAction.RESULTNONE;
        }
        double[][] centroids = node.getCentroids();
        double[][] maxCoordinateSingleDim = node.getMaxCoordinateSingleDim();
        double[][] minCoordinateSingleDim = node.getMinCoordinateSingleDim();
        double[] radii = node.getRadii();
        double[][] vertexes = generateVertex(queryToPivotDistance, radius);
        //2. 判断整个划分块在查询超立方体中和划分块和超立方体完全无交集的情景
        for (int i = 0; i < childrenNum; i++)
        {
            if(isVRInQB(maxCoordinateSingleDim[i], minCoordinateSingleDim[i], vertexes)){
                actions[i] = NodeSearchAction.RESULTNEEDLINERSCAN;
            }else if(isVRNoneQB(maxCoordinateSingleDim[i], minCoordinateSingleDim[i], vertexes)){
                actions[i] = NodeSearchAction.RESULTNONE;
            }
            if(isVRInQB(centroids[i], radii[i], queryToPivotDistance, radius)){
                actions[i] = NodeSearchAction.RESULTNEEDLINERSCAN;
            }else if(isVRNoneQB(centroids[i], radii[i], queryToPivotDistance, radius)){
                actions[i] = NodeSearchAction.RESULTNONE;
            }
        }
        //3. 计算查询超立方体顶点位于的划分块，该块直接设置为待搜索状态
        Set<Integer> vLocationSet = new HashSet<>();
        for (int i = 0; i < vertexes.length; i++)
        {
            double[] v = vertexes[i];
            int l = locateV(centroids, v);  //获取顶点v所在的孩子的下标
            vLocationSet.add(l);
            actions[l] = NodeSearchAction.RESULTUNKNOWN;  //标记该块需搜索
        }
        //4. 判断超立方体是否完全位于一个划分块中，是的话直接返回了
        if(vLocationSet.size()==1){
            return actions;
        }
        //5. 获取所有的维诺边，开始判断
        Voronoi aVoronoi = Voronoi.getAVoronoi(centroids);
        List<VoronoiEdge> voronoiEdge = aVoronoi.getVoronoiEdge();
        VoronoiEdge edge = null;
        for (int i = 0; i < voronoiEdge.size(); i++)
        {
            edge = voronoiEdge.get(i);
            double[][] edge1 = edge.getEdge();
            boolean    isInf = edge.getIsInf();
            int[]      pointsIndex = edge.getPointsIndex();
            //6. 碰撞检测算法
            if (isCollided(edge1, isInf, vertexes)){
                actions[pointsIndex[0]] =
                        actions[pointsIndex[0]]!=NodeSearchAction.RESULTNEEDLINERSCAN?
                                NodeSearchAction.RESULTUNKNOWN
                                        :actions[pointsIndex[0]];
                actions[pointsIndex[1]] = actions[pointsIndex[1]]!=NodeSearchAction.RESULTNEEDLINERSCAN?
                        NodeSearchAction.RESULTUNKNOWN
                        :actions[pointsIndex[1]];
            }
        }
        return actions;
    }

    /**
     * 碰撞检测算法，判断edge1代表的维诺边，与vertexes代表的正方形是否碰撞
     * @param edge1 维诺边的两个端点
     * @param isInf edge1边是否是无限延伸的
     * @param vertexes 查询正方形的顶点
     * @return
     */
    private boolean isCollided(double[][] edge1, boolean isInf, double[][] vertexes)
    {
        //1. 先判断维诺边的方向向量方向
        //求方向向量的单位向量
        double[] a = initVector(edge1[0], edge1[1]);
        double mode_a = Math.sqrt(a[0]*a[0] + a[1]*a[1]);
        a[0] /= mode_a;
        a[1] /= mode_a;
        //求得了方向单位向量a
        //开始求方向向量上的边界[aa,bb]
        double aa = 0;
        double bb = Double.MAX_VALUE;
        if(!isInf){
            //求bb
            double[] ab = initVector(edge1[0], edge1[1]);
            bb = a[0]*ab[0] + a[1]*ab[1];
        }
        //求得边界之后将查询正方形4个顶点都映射到单位法向量a上，判断是否在[aa,bb]内
        int vote = 0;  //点的位置<aa -1分，>bb +1分
        for (int i = 0; i < vertexes.length; i++)
        {
            double[] av = initVector(edge1[0], vertexes[i]);
            double l = a[0]*av[0] + a[1]*av[1];
            if(l<aa){
                vote--;
            }else if(l>bb){
                vote++;
            }
        }
        if (Math.abs(vote)==4) return false;
        //2. 判断维诺边的法向量方向
        //求维诺边的法向量
        double[] _a = new double[]{-a[1], a[0]};
        a = _a;
        //求得了方向单位向量a;
        //开始求方向向量上的边界[aa,bb]
        aa = 0;
        //求得边界之后将查询正方形4个顶点都映射到单位法向量a上，判断是否在[aa,bb]内
        vote = 0;  //点的位置<aa -1分，>bb +1分
        for (int i = 0; i < vertexes.length; i++)
        {
            double[] av = initVector(edge1[0], vertexes[i]);
            double l = a[0]*av[0] + a[1]*av[1];
            if(l<aa){
                vote--;
            }else if(l>aa){
                vote++;
            }
        }
        if (Math.abs(vote)==4) return false;
        //3. 判断x坐标方向
        aa = edge1[0][0];
        bb = edge1[1][0];
        if(isInf){
            if(bb>aa) bb = Double.MAX_VALUE;
            else if(bb<aa) bb = Double.MIN_VALUE;
            else bb = aa;
        }
        if(bb<aa){
            double t = bb;
            bb = aa;
            aa = t;
        }
        //保证边界是[aa,bb]
        //求得边界之后将查询正方形4个顶点都映射到x上，判断是否在[aa,bb]内
        vote = 0;  //点的位置<aa -1分，>bb +1分
        for (int i = 0; i < vertexes.length; i++)
        {
            double l = vertexes[i][0];
            if(l<aa){
                vote--;
            }else if(l>bb){
                vote++;
            }
        }
        if (Math.abs(vote)==4) return false;
        //4. 判断y坐标方向
        aa = edge1[0][1];
        bb = edge1[1][1];
        if(isInf){
            if(bb>aa) bb = Double.MAX_VALUE;
            else if(bb<aa) bb = Double.MIN_VALUE;
            else bb = aa;
        }
        if(bb<aa){
            double t = bb;
            bb = aa;
            aa = t;
        }
        //保证边界是[aa,bb]
        //求得边界之后将查询正方形4个顶点都映射到x上，判断是否在[aa,bb]内
        vote = 0;  //点的位置<aa -1分，>bb +1分
        for (int i = 0; i < vertexes.length; i++)
        {
            double l = vertexes[i][1];
            if(l<aa){
                vote--;
            }else if(l>bb){
                vote++;
            }
        }
        if (Math.abs(vote)==4) return false;
        //上述1~4均发生碰撞
        return true;
    }

    /**
     * 可以解决任意维度的支撑点空间的搜索问题
     * @param node
     * @param radius
     * @param queryToPivotDistance
     * @return
     */
    private NodeSearchAction[] search(PCTInternalNode node, double radius, double[] queryToPivotDistance){
        //1.初始化相关的值
        Metric l2metric = LMetric.EuclideanDistanceMetric;
        int childrenNum = node.getNumChildren();
        int pivotsNum = node.getNumPivots();
        NodeSearchAction[] actions = new NodeSearchAction[childrenNum];
        double[][] centroids = node.getCentroids();
        double[][] maxCoordinateSingleDim = node.getMaxCoordinateSingleDim();
        double[][] minCoordinateSingleDim = node.getMinCoordinateSingleDim();
        double[] radii = node.getRadii();
        double[][] vertexes = generateVertex(queryToPivotDistance, radius);
        Set<Integer> actionUnknownsSet = new HashSet<>();  // 用来记录需要进入搜索的孩子的下标
        // 初始化actionUnknowns，假设所有孩子都要进入搜索
        for (int i = 0; i < childrenNum; i++)
        {
            actionUnknownsSet.add(i);
        }
        //2. 判断整个划分块在查询超立方体中和划分块和超立方体完全无交集的情景
        for (int i = 0; i < childrenNum; i++)
        {
            //mbr
            if(isVRInQB(maxCoordinateSingleDim[i], minCoordinateSingleDim[i], vertexes)){
                actions[i] = NodeSearchAction.RESULTNEEDLINERSCAN;
                actionUnknownsSet.remove(i);
            }else if(isVRNoneQB(maxCoordinateSingleDim[i], minCoordinateSingleDim[i], vertexes)){
                actions[i] = NodeSearchAction.RESULTNONE;
                actionUnknownsSet.remove(i);
            }

        }
        if (actionUnknownsSet.isEmpty()) return actions;
        //3. 计算查询超立方体顶点位于的划分块，该块直接设置为待搜索状态
        Set<Integer> vLocationSet = new HashSet<>();
        for (int i = 0; i < vertexes.length; i++)
        {
            double[] v = vertexes[i];
            int l = locateV(centroids, v);  //获取顶点v所在的孩子的下标
            if (actionUnknownsSet.contains(l)){
                vLocationSet.add(l);
                actionUnknownsSet.remove(l);
                actions[l] = NodeSearchAction.RESULTUNKNOWN;  //标记该块需搜索
            }
        }
        //4. 判断超立方体是否完全位于一个划分块中，是的话直接返回了
        if(vLocationSet.size()==1){
            Iterator<Integer> iterator = actionUnknownsSet.iterator();
            while (iterator.hasNext()){
                actions[iterator.next()] = NodeSearchAction.RESULTNONE;
            }
            return actions;
        }
        //5. 开始进入两层循环搜索算法
        //5.1 先将顶点所在的划分块的站点作为依据点，判断其他所有的点
        Iterator<Integer> iterator = vLocationSet.iterator();
        while (iterator.hasNext()){
            Integer vl = iterator.next();
            Iterator<Integer> iterator1 = actionUnknownsSet.iterator();
            while (iterator1.hasNext()){
                Integer pj = iterator1.next();

                //方法三、r-邻域法
//                if(locateQBByNR(centroids, vl, pj, queryToPivotDistance, radius)==1){
//                    //如果pj能被排除掉
//                    iterator1.remove();
//                    actions[pj] = NodeSearchAction.RESULTNONE;
//                }

                //方法二、向量法
                if(locateQBbyVertex(centroids, vl, pj, queryToPivotDistance, vertexes)==1){
                    //如果pj能被排除掉
                    iterator1.remove();
                    actions[pj] = NodeSearchAction.RESULTNONE;
                }

                //方法一、2R的方式判断查询立方体的位置
//                if(locateQBby2R(centroids, vl, pj, queryToPivotDistance, radius)==1){
//                    //如果pj能被排除掉
//                    iterator1.remove();
//                    actions[pj] = NodeSearchAction.RESULTNONE;
//                }
            }
        }
        //5.2 如果actionUnknownsSet已经为空了，则直接返回就好了
        if(actionUnknownsSet.isEmpty()){
            return actions;
        }
        //5.3 进入最后一层判断，拿actionUnknownsSet内的块互相排除
        List<Integer> list = new LinkedList<>(actionUnknownsSet);
        for (int i = 0; i < list.size(); i++)
        {
            for (int j = i+1; j < list.size(); j++)
            {
//                int l = locateQBByNR(centroids, i, j, queryToPivotDistance, radius);
                int l = locateQBbyVertex(centroids, i, j, queryToPivotDistance, vertexes);
//                int l = locateQBby2R(centroids, i, j, queryToPivotDistance, radius);
                if(l==1){
                    actions[j] = NodeSearchAction.RESULTNONE;
                    actionUnknownsSet.remove(j);
                }else if(l==-1){
                    actions[i] = NodeSearchAction.RESULTNONE;
                    actionUnknownsSet.remove(i);
                }
            }
        }
        //5.4 将actionUnknownsSet内所有的点标记为继续搜
        if (!actionUnknownsSet.isEmpty()){
            Iterator<Integer> iterator1 = actionUnknownsSet.iterator();
            while (iterator1.hasNext()){
                Integer next = iterator1.next();
                actions[next] = NodeSearchAction.RESULTUNKNOWN;
            }
        }
        return actions;
    }

    /**
     * 判断查询超立方体的位置(法向量法）
     * @param centroids 所有聚类中心
     * @param pi pi在centroids中的下标
     * @param pj pj在centroids中的下标
     * @param q
     * @param radius
     * @return 1 查询超立方体完全位于pi侧
     *          0 查询超立方体与划分超平面相交
     *          -1 查询超立方体完全位于pj侧
     */
    private int locateQBByNR(double[][] centroids, int pi, int pj, double[] q, double radius)
    {
        double piqDis = LMetric.EuclideanDistanceMetric.getDistance(centroids[pi], q);
        double pjqDis = LMetric.EuclideanDistanceMetric.getDistance(centroids[pj], q);
        double[] pipj = initVector(centroids[pi], centroids[pj]);
        double nr = calRNeighbour(pipj, radius);
        double dis = calDisFromPlane(q, centroids[pi], centroids[pi], pipj);
        if(dis > (nr/2)){
            if (piqDis > pjqDis) return 1;
            else return -1;
        }
        return 0;
    }

    /**
     * 判断查询超立方体的位置（2r法）
     * @param centroids 所有聚类中心
     * @param pi pi在centroids中的下标
     * @param pj pj在centroids中的下标
     * @param q
     * @param radius
     * @return 1 查询超立方体完全位于pi侧
     *          0 查询超立方体与划分超平面相交
     *          -1 查询超立方体完全位于pj侧
     */
    private int locateQBby2R(double[][] centroids, int pi, int pj, double[] q, double radius)
    {
        int n = q.length;
        double piqDis = LMetric.EuclideanDistanceMetric.getDistance(centroids[pi], q);
        double pjqDis = LMetric.EuclideanDistanceMetric.getDistance(centroids[pj], q);
        double t = 2*Math.pow(n, 0.5)*radius;
        if(piqDis - pjqDis > t) return -1;
        else if(pjqDis - piqDis > t) return 1;
        return 0;
    }

    /**
     * 判断查询超立方体的位置（判断顶点是否位于同一侧）
     * @param centroids 所有聚类中心
     * @param pi pi在centroids中的下标
     * @param pj pj在centroids中的下标
     * @param q
     * @param vertex 顶点集合
     * @return >0 查询超立方体完全位于pi侧
     *          0 查询超立方体与划分超平面相交
     *          <0 查询超立方体完全位于pj侧
     */
    private int locateQBbyVertex(double[][] centroids, int pi, int pj, double[] q, double[][] vertex)
    {
        int n = q.length;
        double[] m = calMidpoint(centroids[pi], centroids[pj]);
        double[] pipj = initVector(centroids[pi], centroids[pj]);
        int vote = 0;
        for (int i = 0; i < vertex.length; i++)
        {
            double[] v = vertex[i];
            double[] mv = initVector(m, v);
            double r = vectorCdot(mv, pipj);
            if (r < 0) vote++;
            else if(r>0) vote--;
        }
        if(Math.abs(vote)!=Math.pow(2, n)) return 0;
        return vote;
    }

    /**
     * 计算向量点积
     * @param mv
     * @param pipj
     * @return
     */
    private double vectorCdot(double[] mv, double[] pipj)
    {
        double r = 0.0f;
        for (int i = 0; i < mv.length; i++)
        {
            r += mv[i]*pipj[i];
        }
        return r;
    }


    /**
     * 计算查询点q到划分超平面的距离
     * 假设m点为pi，pj的中点，则要计算的距离为：
     *          dis = cdot(vector(m,q),vector(pi,pj)/(|vector(pi,pj)|
     * @param q
     * @param pi
     * @param pj
     * @param pipj
     * @return
     */
    private double calDisFromPlane(double[] q, double[] pi, double[] pj, double[] pipj)
    {
        //只能在q位于j侧的时候调用
//        if (LMetric.EuclideanDistanceMetric.getDistance(q,pi) <
//        LMetric.EuclideanDistanceMetric.getDistance(q,pj)){
//            return calDisFromPlane(q,pj,pi,initVector(pj, pi));
//        }
        int n = q.length;
        double[] m = calMidpoint(pi, pj);
        double[] mq = initVector(m, q);
        double t1 = 0.0f, t2 = 0.0f;
        for (int i = 0; i < n; i++)
        {
            t1 += mq[i]*pipj[i];
            t2 += Math.pow(pipj[i], 2);
        }
        double dis = t1/(Math.sqrt(t2));
        return dis;
    }

    /**
     * 计算pi和pj的中点
     * @param pi
     * @param pj
     * @return
     */
    private double[] calMidpoint(double[] pi, double[] pj)
    {
        int n = pi.length;
        double[] m = new double[n];
        for (int i = 0; i < n; i++)
        {
            m[i] = (pi[i] + pj[i])/2;
        }
        return m;
    }

    /**
     * 计算r邻域
     * @param pipj 划分平面的法向量
     * @param radius 查询半径
     * @return
     */
    private double calRNeighbour(double[] pipj, double radius)
    {
        int n = pipj.length;
        double t1 = 0.0f, t2 = 0.0f;
        for (int i = 0; i < n; i++)
        {
            t1 += Math.abs(pipj[i]);
            t2 += Math.pow(pipj[i], 2);
        }
        return 2*radius*(t1/Math.sqrt(t2));
    }

    /**
     * 计算向量v1v2
     * @param v1
     * @param v2
     * @return
     */
    private double[] initVector(double[] v1, double[] v2)
    {
        double[] v3 = new double[v1.length];
        for (int i = 0; i < v3.length; i++)
        {
            v3[i] = v2[i] - v1[i];
        }
        return v3;
    }

    /**
     * 获取v所在的划分块的下标
     * @param centroids 划分块的聚类中心
     * @param v 顶点的坐标
     * @return
     */
    private int locateV(double[][] centroids, double[] v)
    {
        //其实就是求v离哪个聚类中心更近
        double minDis = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < centroids.length; i++)
        {
            double dis = LMetric.EuclideanDistanceMetric.getDistance(centroids[i], v);
            if(dis<minDis){
                minDis = dis;
                minIndex = i;
            }
        }
        return minIndex;
    }

    /**
     * 判断划分块是否与查询超立方体没有交集
     * @param maxCoordinateSingleDim VR内的点的坐标，在每个维度的最大值
     * @param minCoordinateSingleDim VR内的点的坐标，在每个维度的最小值
     * @param vertexes 查询超立方体的顶点集合
     * @return true 划分块与查询超立方体没有交集
     *          false 划分块与查询超立方体有交集
     */
    private boolean isVRNoneQB(double[] maxCoordinateSingleDim, double[] minCoordinateSingleDim, double[][] vertexes)
    {
        double[] max = new double[maxCoordinateSingleDim.length];
        double[] min = new double[minCoordinateSingleDim.length];
        Arrays.fill(max, Double.MIN_VALUE);
        Arrays.fill(min, Double.MAX_VALUE);
        for (int i = 0; i < vertexes.length; i++)
        {
            for (int j = 0; j < max.length; j++)
            {
                max[j] = Math.max(max[j], vertexes[i][j]);
                min[j] = Math.min(min[j], vertexes[i][j]);
            }
        }
        //max和min是顶点在每个维度的最大值和最小值，只需要某个维度的顶点的最大值和最小值代表的线段和这个维度的数据点的投影不相交那么就可以排除
        for (int i = 0; i < max.length; i++)
        {
            if (min[i] > maxCoordinateSingleDim[i] || max[i] < minCoordinateSingleDim[i]){
                return true;
            }
        }
        return false;
    }


    /**
     * 判断划分块是否位于查询超立方体中
     * @param maxCoordinateSingleDim VR内的点的坐标，在每个维度的最大值
     * @param minCoordinateSingleDim VR内的点的坐标，在每个维度的最小值
     * @param vertexes 查询超立方体的顶点集合
     * @return true 划分块完全位于查询超立方体中
     *          false 划分块没有完全位于查询超立方体中
     */
    private boolean isVRInQB(double[] maxCoordinateSingleDim, double[] minCoordinateSingleDim, double[][] vertexes)
    {
        //计算顶点在每个维度的最大值和最小值
        double[] max = new double[maxCoordinateSingleDim.length];
        double[] min = new double[minCoordinateSingleDim.length];
        Arrays.fill(max, Double.MIN_VALUE);
        Arrays.fill(min, Double.MAX_VALUE);
        for (int i = 0; i < vertexes.length; i++)
        {
            for (int j = 0; j < max.length; j++)
            {
                max[j] = Math.max(max[j], vertexes[i][j]);
                min[j] = Math.min(min[j], vertexes[i][j]);
            }
        }
        //max和min是顶点在每个维度的最大值和最小值
        for (int i = 0; i < max.length; i++)
        {
            if (min[i] > minCoordinateSingleDim[i] || max[i] < maxCoordinateSingleDim[i]){
                return false;
            }
        }
        return true;
    }

    /**
     * 判断划分块是否与查询超立方体没有交集(利用MBS判断）
     * @param centroid 划分块的聚类中心
     * @param childrenRadius 划分块的半径
     * @param q 查询超立方体的中心
     * @param radius
     * @return true 划分块与查询超立方体没有交集
     *          false 划分块与查询超立方体有交集
     */
    private boolean isVRNoneQB(double[] centroid, double childrenRadius, double[] q, double radius)
    {
        double qc = LMetric.EuclideanDistanceMetric.getDistance(q, centroid);
        int n = q.length;
        double coefficient = Math.sqrt(n);
        if(qc > (childrenRadius + coefficient*radius)){
            return true;
        }
        return false;
    }

    /**
     * 判断划分块是否位于查询超立方体中（利用MBS判断）
     * @param centroid 划分块的聚类中心
     * @param childrenRadius 划分块的半径
     * @param q 查询超立方体的中心
     * @return true 划分块完全位于查询超立方体中
     *          false 划分块没有完全位于查询超立方体中
     */
    private boolean isVRInQB(double[] centroid, double childrenRadius, double[] q, double radius)
    {
        double qc = LMetric.EuclideanDistanceMetric.getDistance(q, centroid);
        if(qc + childrenRadius <= radius){
            return true;
        }
        return false;
    }

    /**
     * 生成查询超立方体QB
     * @param q 查询对象在支撑点空间的坐标
     * @param radius 范围查询半径
     * @return
     */
    private double[][] generateVertex(double[] q, double radius)
    {
        List<double[]> list = generateVertexHelp(q, radius, 0, new double[q.length], new ArrayList<>());
        double[][] vertexes = new double[list.size()][q.length];
        for (int i = 0; i < list.size(); i++)
        {
            vertexes[i] = list.get(i);
        }
        return vertexes;
    }

    //辅助搜索用的辅助方法，用于生成查询超立方体的所有的顶点
    private List<double[]> generateVertexHelp(double[] q, double radius, int cur, double[] vertex, List<double[]> allVertex)
    {
        //递归输出,cur指向本次递归要操作的维度
        if (cur == vertex.length)
        {
            allVertex.add(vertex.clone());
            return allVertex;
        }
        vertex[cur] = q[cur] + radius;
        generateVertexHelp(q, radius, cur + 1, vertex, allVertex);
        vertex[cur] = q[cur] - radius;
        generateVertexHelp(q, radius, cur + 1, vertex, allVertex);
        return allVertex;
    }
}
