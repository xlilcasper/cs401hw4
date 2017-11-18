import org.graphstream.graph.*;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class Graph {
    private final List<Vertex> vertexes;
    private final List<Edge> edges;
    private final org.graphstream.graph.Graph displayGraph = new SingleGraph("Graph");
    private Viewer viewer=null;

    public Graph(List<Vertex> vertexes, List<Edge> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
        vertexes.forEach((v)->displayGraph.addNode(v.getId()).addAttribute("ui.label", v.getId()));
        edges.forEach((e)-> displayGraph.addEdge(e.getId(),e.getSource().getId(),e.getDestination().getId()));
    }

    public void addVertex(Vertex vertex) {
        vertexes.add(vertex);
        displayGraph.addNode(vertex.getId()).addAttribute("ui.label", vertex.getId());
    }

    public void addEdge(Edge e) {
        if (!edges.contains(e)) {
            edges.add(e);
            displayGraph.addEdge(e.getId(),e.getSource().getId(),e.getDestination().getId());
        }
    }

    public boolean combine(Graph graph) {
        pauseGraph();
        boolean changed=false;
        for (Vertex v : graph.getVertexes()) {
            if (!getVertexes().contains(v)) {
                changed = true;
                addVertex(v);
            }
        }
        for (Edge e : graph.getEdges()) {
            if (!getEdges().contains(e)) {
                changed=true;
                addEdge(e);
            }
        }
        resumeGraph();
        return changed;
    }



    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public String getEdgeList() {
        String edgeList="";
        for (Edge e : getEdges()) {
            edgeList+=e.getSource()+" "+e.getDestination()+" "+e.getWeight()+System.lineSeparator();
        }
        return edgeList;
    }

    public void highlighNode(String nodeId) {
        Node node = displayGraph.getNode(nodeId);
        if (node == null)
            return;
        node.addAttribute("ui.style", "fill-color: rgb(0,255,0);");
    }

    public void showGraph(String highlight) {
        int xSize=300;
        int ySize=300;

        //size of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int windowsPerX=screenSize.width/xSize;
        int windowsPerY=screenSize.height/ySize;

        displayGraph.getNode(highlight).addAttribute("ui.style","fill-color: rgb(255,0,0);");

        Iterator<Node> ittr = displayGraph.getNode(highlight).getNeighborNodeIterator();
        while (ittr.hasNext()) {
            ittr.next().addAttribute("ui.style","fill-color: rgb(0,255,0);");
        }

        viewer = displayGraph.display();
        JFrame frame = ((JFrame) (viewer.getDefaultView().getParent().getParent().getParent().getParent()));
        frame.setTitle("Node "+highlight+" graph");
        int id=Integer.parseInt(highlight);
        viewer.getDefaultView().resizeFrame(xSize,ySize);
        frame.setLocation(id%windowsPerX*xSize,(id/windowsPerX)%(windowsPerY)*ySize);

    }

    public void pauseGraph() {
        if (viewer==null)
            return;
        PrintStream err = System.err;
        try {
            System.setErr(new PrintStream(new FileOutputStream("NUL:")));
            viewer.disableAutoLayout();
            System.setErr(err);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void resumeGraph() {
        if (viewer==null)
            return;
        PrintStream err = System.err;
        try {
            System.setErr(new PrintStream(new FileOutputStream("NUL:")));
            viewer.enableAutoLayout();
            System.setErr(err);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Graph{" +
                "vertexes=" + vertexes + "\n" +
                ", edges=" + edges + "\n" +
                '}';
    }
}
