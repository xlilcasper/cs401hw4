import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final List<Vertex> vertexes;
    private final List<Edge> edges;

    public Graph(List<Vertex> vertexes, List<Edge> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
    }

    public void addVertex(Vertex vertex, List<Edge> edges) {
        this.edges.addAll(edges);
        if (!vertexes.contains(vertex))
            vertexes.add(vertex);
    }

    public void addEdge(Vertex source, Vertex destination, int weight) {
        String id = "Edge_"+source.getName()+"_"+destination.getName()+"_"+weight;
        addEdge(id,source,destination,weight);
    }

    public void addEdge(String id, Vertex source, Vertex destination, int weight) {
        Edge edge = new Edge(id,source,destination,weight);
        if (!edges.contains(edge))
            edges.add(edge);
    }

    public void addEdge(String id, String source, String destination, int weight) {
        Vertex vertexSource = vertexes.stream()
                .filter(x -> x.getName().equals(source))
                .findAny().get();
        Vertex vertexDestination = vertexes.stream()
                .filter(x -> x.getName().equals(destination))
                .findAny().get();
        addEdge(id,vertexSource,vertexDestination,weight);

    }

    public void addEdge(String source, String destination, int weight) {
        Vertex vertexSource = vertexes.stream()
                .filter(x -> x.getName().equals(source))
                .findAny().get();
        Vertex vertexDestination = vertexes.stream()
                .filter(x -> x.getName().equals(destination))
                .findAny().get();

        String id = "Edge_"+vertexSource.getName()+"_"+vertexDestination.getName()+"_"+weight;
        addEdge(id,vertexSource,vertexDestination,weight);
    }


    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "vertexes=" + vertexes + "\n" +
                ", edges=" + edges + "\n" +
                '}';
    }
}
