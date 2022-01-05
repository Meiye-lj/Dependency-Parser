package org.example;

import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Zhang Yang
 * @description
 * @date 2021/11/28 - 17:30
 */
public class cycletest {
    public static void main(String[] args) {
        SzwarcfiterLauerSimpleCycles szw = new SzwarcfiterLauerSimpleCycles();
        DirectedMultigraph<String, DefaultEdge> dg = new DirectedMultigraph(DefaultEdge.class);
        dg.addVertex("a");
        dg.addVertex("b");
        dg.addVertex("c");
        dg.addVertex("d");
        dg.addEdge("a", "b");
        dg.addEdge("b", "c");
        dg.addEdge("d", "a");
        dg.addEdge("c", "d");
        dg.addEdge("a", "d");
        dg.addEdge("d", "b");
        System.out.println(dg.toString());
        szw.setGraph(dg);
        List<List<String>> l1 = szw.findSimpleCycles();
        System.out.println(l1.toString());
        System.out.println(l1.size());
//        System.out.println((l1.get(0)).get(1));


    }
}

class vertex implements Supplier<String> {
    String node = null;
    LinkedList<edge> NodeList = new LinkedList<>();

    public vertex(String n) {
        node = n;
    }

    @Override
    public String get() {
        return node;
    }
}

class edge implements Supplier<String> {
    String toNode = null;

    public edge(String toNode) {
        this.toNode = toNode;
    }

    @Override
    public String get() {
        return toNode;
    }

    class test {
        String string = "a";
        int i = 5;

        public test(String string, int i) {
            this.string = string;
            this.i = i;
        }
    }
}