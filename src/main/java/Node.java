public class Node {
    Vertex self;
    Graph network;

    public Node(String id, String name) {
        self = new Vertex(id,name);
    }
}
