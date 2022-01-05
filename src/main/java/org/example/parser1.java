package org.example;

import org.jgrapht.alg.cycle.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.readAllLines;

/**
 * @author Zhang Yang
 * @description 解析依赖文本文件
 * @date 2021/11/14 - 16:54
 */
public class parser1 {
    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        SzwarcfiterLauerSimpleCycles szw = new SzwarcfiterLauerSimpleCycles();
//        TarjanSimpleCycles tjs = new TarjanSimpleCycles();
//        TiernanSimpleCycles tis = new TiernanSimpleCycles();
        HawickJamesSimpleCycles hws = new HawickJamesSimpleCycles();
        DirectedMultigraph<String, DefaultEdge> dg = new DirectedMultigraph(DefaultEdge.class);
        DependParser parser = new DependParser(dg, "D:/学习/实验室/构建依赖/依赖解析/freecs/freecs2.txt");
        parser.parseClass();  //建立依赖图

        Set<String> vs1 = dg.vertexSet();  //顶点集
        Set<DefaultEdge> ds1 = dg.edgeSet();  //边集
        System.out.println(vs1.size());
        System.out.println(ds1.size());

        Class clazz = DefaultEdge.class;
        Method m = clazz.getDeclaredMethod("getTarget");
        m.setAccessible(true);
        for (String v : vs1) {   //输出依赖信息
            Set<DefaultEdge> es1 = dg.outgoingEdgesOf(v);
            if (es1.size() > 0) {
                System.out.print(v + " depends on: ");
                Iterator<DefaultEdge> it = es1.iterator();
                while (it.hasNext()) {
                    Object o = m.invoke(it.next());
                    System.out.print(o.toString());
                    if (it.hasNext()) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }
        }

        hws.setGraph(dg);
        long startTime;
        long endTime;
        hws.setPathLimit(8);  //设置循环最大边数
        startTime = System.currentTimeMillis();
        List<List<String>> l2 = hws.findSimpleCycles(); //寻找图中的循环
        endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
        System.out.println(l2.size());
//        Dependencyfilter.printDependency(l2, 500);

//        cyclicFinder cf = new cyclicFinder();
//        cf.find(dg, "freecs", "freecs");
//        System.out.println(cf.getNum());

        List<List<String>> l3 = Dependencyfilter.filterByDuplicate(l2); //只保留最大环
        System.out.println(l3.size());
        Dependencyfilter.printDependency(l3, 50); //输出循环信息

//        HotspotFinder hf = new HotspotFinder(dg);  //筛选热点顶点
//        Set<String> result = hf.find(70);
//        System.out.println(result.size());
//        System.out.println(result);
//
//        Dependencyfilter ddf = new Dependencyfilter(l2, result);  //过滤循环数据
//        List<List<String>> l3 = ddf.filterByHotspot();
//        System.out.println(l3.size());

    }
}

class GraphNode {  //暂时不用
    String Nodename;
    LinkedList<String> NodeList = new LinkedList<>();

    public GraphNode(String Node) {
        Nodename = Node;
    }

    public void addNode(String nodename) {
        NodeList.add(nodename);
    }

    public void showPackage() {
        System.out.print(Nodename + " depends on(Package Level): ");
        for (String s : NodeList) {
            System.out.print(s + " ");
        }
        System.out.print('\n');
    }

    public void showPackageOnClass() {
        System.out.print(Nodename + " depends on(Package on Class Level): ");
        for (String s : NodeList) {
            System.out.print(s + " ");
        }
        System.out.print('\n');
    }

    public void showClassOnClass() {
        System.out.print(Nodename + " depends on(Class on Class Level): ");
        for (String s : NodeList) {
            System.out.print(s + " ");
        }
        System.out.print('\n');
    }
}


class DependParser {
    String path; //文件路径
    DirectedMultigraph<String, DefaultEdge> dg ;

    public DependParser(DirectedMultigraph<String, DefaultEdge> dg1, String path1) {
        dg = dg1;
        path = path1;
    }

    public void parsePackage() throws IOException {  //解析包级依赖
        Path fpath = Paths.get(path);
        java.util.List<String> stringList = readAllLines(fpath);
        Iterator<String> stringIterator = stringList.listIterator();
        String currentNode = null;
        while (stringIterator.hasNext()) {
            String s = stringIterator.next();
            if (s.charAt(0) == ' ') { //如果解析到依赖包
                s = s.trim();
                if (s.length() > 0) {
                    int i = s.indexOf("*");
                    if (i != -1) {
                        continue;
//                        s = s.substring(4, i - 1);
                    } else
                        s = s.substring(4);
                    if (!dg.containsVertex(s))
                        dg.addVertex(s);
                    dg.addEdge(currentNode, s);
                }
            } else {  //解析到目标包
                s = s.trim();
                dg.addVertex(s);
                currentNode = s;
            }

        }
        System.out.println("Parse successful!");
    }

    public void parseClass() throws IOException {  //解析类级依赖
        Path fpath = Paths.get(path);
        java.util.List<String> stringList = readAllLines(fpath);
        Iterator<String> stringIterator = stringList.listIterator();
        String currentPackage = null;
        String currentNode = null;
        String s;
        while (stringIterator.hasNext()) {
            s = stringIterator.next();
            int i = s.indexOf("*");
            if (i != -1) {
//                s = s.substring(0, i - 1);
                continue;
            }
            if (s.charAt(0) != ' ') { //如果解析到包
                s = s.trim();
                currentPackage = s;
            } else {
                s = s.trim();
                if (s.length() == 0)  //最后一行
                    break;
                if (s.charAt(0) != '-') {  //包里面的类
                    i = s.indexOf("$");
                    if (i != -1)
                        s = s.substring(0, i);
                    s = currentPackage + "." + s;
                    if (!dg.containsVertex(s)) ;
                    dg.addVertex(s);
                    currentNode = s;
                } else {      //依赖的类
                    i = s.indexOf("$");
                    if (i != -1)
                        s = s.substring(4, i);
                    else
                        s = s.substring(4);
                    if (s.equals(currentNode)) //忽略自我依赖
                        continue;
                    if (!dg.containsVertex(s))
                        dg.addVertex(s);
                    if (!dg.containsEdge(currentNode, s))
                        dg.addEdge(currentNode, s);
                }
            }
        }


    }

}

class HotspotFinder { //热点寻找
    DirectedMultigraph<String, DefaultEdge> dg;

    public HotspotFinder(DirectedMultigraph<String, DefaultEdge> dg) {
        this.dg = dg;
    }

    public Set<String> find(int threshold) {
        Set<String> vs1 = dg.vertexSet();
        Set<String> vs2 = new HashSet<>();
        for (String s : vs1) {
            if (dg.inDegreeOf(s) >= threshold) {
                vs2.add(s);
            }

        }
        return vs2;

    }

}

class Dependencyfilter {
    List<List<String>> l1;
    Set<String> s1;

    public Dependencyfilter(List<List<String>> l1, Set<String> s1) {
        this.l1 = l1;
        this.s1 = s1;
    }

    public List<List<String>> filterByHotspot() { //只保留包含热点的循环
        Iterator<List<String>> iterator = l1.iterator();
        while (iterator.hasNext()) {
            boolean flag = false;
            List<String> next = iterator.next();
            for (String s : s1) {
                if (next.contains(s)) {
                    flag = true;
                    break;
                }
            }
            if (!flag)
                iterator.remove();

        }
        return l1;

    }

    public static List<List<String>> filterByDuplicate(List<List<String>> ll) {  //只保留最大环
        List<List<String>> result = new ArrayList<>();
        List<String> old;
        old = ll.get(0);
        for (List<String> l2 : ll) {
            if (l2.containsAll(old)) {
                old = l2;
            } else {
                result.add(old);
                old = l2;
            }

        }

        return result;
    }

    public static void printDependency(List<List<String>> l, int len) { //输出循环
        int i = 0;
        for (List<String> l1 : l) {
            System.out.println(l1);
            if (i++ > len)
                break;
        }

    }

}

class cyclicFinder {
    HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
    int num = 0;
    Class clazz = DefaultEdge.class;
    Method m = clazz.getDeclaredMethod("getTarget");
    LinkedList<String> l = new LinkedList<>();

    cyclicFinder() throws NoSuchMethodException {
    }


    public int getNum() {
        return num;
    }

    public void find(DirectedMultigraph<String, DefaultEdge> dg, String node, String start) throws InvocationTargetException, IllegalAccessException { //DFS算法寻找循环
        m.setAccessible(true);
        if (visited.get(node) != null && visited.get(node) == true) {
            if (l.contains(node)) {
//                System.out.println(l);
                num++;
            }
            return;
        }
        visited.put(node, true);
        l.add(node);
        Set<DefaultEdge> defaultEdges = dg.outgoingEdgesOf(node);
        for (DefaultEdge edge : defaultEdges) {
            find(dg, (String) m.invoke(edge), start);
        }
        visited.put(node, false);
        l.remove(node);

    }
}
