import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int nodes=5;

        //Setup default values
        if (args.length>0) {
            nodes=Integer.parseInt(args[0]);
        }
        //if nodes == 1, error

        Graph graph = randomGraph(nodes);

        System.out.println(graph);
    }

    private static Graph randomGraph(int nodes) {
        List<Vertex> vertexs=new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        //build a set of nodes
        for (int i=0;i<nodes;i++) {
            Vertex vertex = new Vertex("Node_"+i,"Node_"+i);
            vertexs.add(vertex);
        }

        //Build a random set of links
        //Loop over each vertex, add 1 to 3 links
        int max = vertexs.size()-1;
        if (max>3)
            max=3;

        Random rand=new Random();
        for (Vertex vertex : vertexs) {
            int num = rand.nextInt(max)+1;
            for (int i=0;i<num;i++) {
                Vertex destination=vertex;
                //make sure they are not the same.
                while (destination.equals(vertex)) {
                    //Make sure they are not already connected
                    destination = vertexs.get(rand.nextInt(vertexs.size()));
                }
                int weight = rand.nextInt(9)+1;
                String id = "Edge_"+vertex.getName()+"_"+destination.getName()+"_"+weight;
                edges.add(new Edge(id,vertex,destination,weight));
            }
        }

        //Make our graph
        return new Graph(vertexs,edges);
    }
}
