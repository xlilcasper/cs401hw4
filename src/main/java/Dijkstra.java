import java.util.*;

public class Dijkstra {
    private final List<Vertex> nodes;
    private final List<Edge> edges;
    private Set<Vertex> visited;
    private Set<Vertex> unvisited;
    private Map<Vertex,Vertex> parents;
    private Map<Vertex,Integer> distance;

    public Dijkstra(Graph graph) {
        this.nodes = new ArrayList<Vertex>(graph.getVertexes());
        this.edges = new ArrayList<Edge>(graph.getEdges());
    }

    public void run(Vertex source) {
        visited = new HashSet<Vertex>();
        unvisited = new HashSet<Vertex>();
        distance = new HashMap<Vertex, Integer>();
        parents = new HashMap<Vertex, Vertex>();
        distance.put(source,0);
        unvisited.add(source);

        //While we still have nodes to
        while (unvisited.size()>0) {
            //Greedy
            Vertex node = getMin(unvisited);
            visited.add(node);
            unvisited.remove(node);
            minDistances(node);
        }

    }

    private boolean isVisted(Vertex vertex) {
        return visited.contains(vertex);
    }

    private Vertex getMin(Set<Vertex> vertexes) {
        Vertex min = null;
        for (Vertex vertex : vertexes) {
            if (min==null) {
                min=vertex;
                continue;
            }
            if (shortestDistance(vertex)<shortestDistance(min))
                min=vertex;
        }
        return min;
    }

    private void minDistances(Vertex node) {
        List<Vertex> neighbors = getUnvisitedNeighbors(node);
        for (Vertex target : neighbors) {
            //See if we found a shorter path
            int dist = shortestDistance(node) + getDistance(node, target);
            if (dist < shortestDistance(target)) {
                distance.put(target,dist);
                parents.put(target,node);
                //We found a new node, lets visit it as well.
                unvisited.add(target);
            }
        }
    }

    private int getDistance(Vertex node, Vertex target) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(node) && edge.getDestination().equals(target))
                return edge.getWeight();
        }
        throw new RuntimeException("Nodes not connected");
    }
    private List<Vertex> getUnvisitedNeighbors(Vertex node) {
        List<Vertex> ret = new ArrayList<Vertex>();
        //Loop over all our edges and return any
        //that next to node that are not visited
        for (Edge edge : edges) {
            if (edge.getSource().equals(node) && !isVisted(edge.getDestination())) {
                ret.add(edge.getDestination());
            }
        }
        return ret;
    }

    private int shortestDistance(Vertex dest) {
        //grab our shortest found distance to the destination node
        Integer d = distance.get(dest);
        if (d==null)
            return Integer.MAX_VALUE;
        return d;
    }

    public LinkedList<Vertex> getPath(Vertex target) {
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        Vertex step = target;
        // check if a path exists
        if (parents.get(step) == null) {
            return null;
        }
        path.add(step);
        while (parents.get(step) != null) {
            step = parents.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

}

