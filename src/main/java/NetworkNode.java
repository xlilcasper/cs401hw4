import org.graphstream.graph.implementations.SingleGraph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

/**
 * Created by Brian on 11/18/2017.
 */
public class NetworkNode {
    private int maxNodes=0;
    private Integer id=0;
    private Vertex self;
    private InputStream inputFile;
    private Graph graph;
    private int basePort;
    private InetAddress broadcastAddress;
    private List<String> neighbor=new ArrayList<>();
    private List<String> heardFrom=new ArrayList<>();
    private boolean debug=false;

    public NetworkNode(Integer id, int basePort) {
        if (id==0)
            debug=true;
        try {
            broadcastAddress = InetAddress.getByName("255.255.255.255") ;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.id=id;
        this.basePort=basePort;
        self = new Vertex(id,id);
        ClassLoader classLoader = getClass().getClassLoader();
        this.inputFile = classLoader.getResourceAsStream("graph.txt");
        try {
            this.graph = findNeighbors(inputFile);
            graph.showGraph(self.getId());
            //Listen for connections
            new Thread(new Runnable(){
                //Normally we would just listen on baseport but we are running
                //multiple programs on the same machine
                private DatagramSocket socket = new DatagramSocket(basePort+id, InetAddress.getByName("0.0.0.0"));
                private DatagramPacket packet;
                @Override
                public void run() {
                    System.out.println("Node "+id+" is online");
                    byte[] buffer = new byte[2048];
                    packet=new DatagramPacket(buffer,buffer.length);
                    String msg="";
                    try {
                        while (!msg.equals("exit")) {
                            socket.receive(packet);
                            msg = new String(buffer, 0, packet.getLength());
                            if (!msg.equals("exit")) {
                                Scanner in = new Scanner(msg);
                                String from = in.next();

                                Graph newGraph = processEdges(in,self,false);
                                if (graph.combine(newGraph)) {
                                    debugPrintln("Changed!");
                                    sendLinkPacket();
                                    if (graph.getVertexes().size()==maxNodes && debug) {
                                        int dest=5;
                                        Dijkstra dijkstra = new Dijkstra(graph,true);
                                        dijkstra.run(self);
                                        debugPrintln("Shortest Path "+self.getId()+ " to "+dest);
                                        LinkedList<Vertex> path = dijkstra.getPath(new Vertex(dest, dest));
                                        debugPrintln(path);
                                    }
                                } else if (!heardFrom.contains(from)) {
                                    heardFrom.add(from);
                                    //graph.highlighNode(from);
                                    sendLinkPacket();
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            sendLinkPacket();
            System.out.println("Sent linkpacket");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendLinkPacket() {
        try {
            broadcast(self.getId()+" " + graph.getEdgeList(),broadcastAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Graph findNeighbors(InputStream file) throws FileNotFoundException {
        /*
        File format is
        [nodes in network]
        [source] [destination] [weight]
        [source] [destination] [weight]
        [source] [destination] [weight]
        [source] [destination] [weight]
         */
        Scanner in = new Scanner(file);
        maxNodes = in.nextInt();

        //We can only know about our neighbors when we start
        return processEdges(in,self,true);
    }

    private Graph processEdges(Scanner in,Vertex self,boolean skipNonNeighbors) {
        HashMap<Integer,Vertex> vertexes=new HashMap<>();
        List<Edge> edges = new ArrayList<>();
        vertexes.put(Integer.parseInt(self.getId()),self);
        while (in.hasNext()) {
            Integer sourceId=in.nextInt();
            Integer destinationId=in.nextInt();
            int weight = in.nextInt();
            //If I am not the source or destination and it is our initial load then I don't know about this connection
            if (destinationId!= id && sourceId != id && skipNonNeighbors) {
                neighbor.add(((sourceId==id) ? sourceId : destinationId).toString());
                continue;
            }

            //If my id is the destination, "swap" it so we have the right destination
            //if (destinationId==id)
            //    destinationId=sourceId;

            //grabs it if it exists else it adds it and returns it
            Vertex source = vertexes.computeIfAbsent(sourceId,(id) -> new Vertex(id,id));
            Vertex destination = vertexes.computeIfAbsent(destinationId,(id) -> new Vertex(id,id));
            String id = "Edge_"+source.getName()+"-"+destination.getName()+"("+weight+")";
            edges.add(new Edge(id,source,destination,weight));
        }
        //Our graph needs two lists, convert our hash to a list.
        return new Graph(new ArrayList<>(vertexes.values()),edges);
    }

    public  void broadcast( String data,InetAddress address) throws IOException {
        //Broadcast should just send it out on a single port
        //however we are running them on the same machine.
        for (int i=0;i<maxNodes;i++) {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] buffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, basePort+i);
            socket.send(packet);
            socket.close();
        }
    }

    private Graph randomGraph(int nodes) {
        List<Vertex> vertexes=new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        //build a set of nodes
        for (int i=0;i<nodes;i++) {
            Vertex vertex = new Vertex("Node_"+i,"Node_"+i);
            vertexes.add(vertex);
        }

        //Build a random set of links
        //Loop over each vertex, add 1 to 3 links
        int max = vertexes.size()-1;
        if (max>3)
            max=3;

        Random rand=new Random();
        for (Vertex vertex : vertexes) {
            int num = rand.nextInt(max)+1;
            for (int i=0;i<num;i++) {
                Vertex destination=vertex;
                //make sure they are not the same.
                while (destination.equals(vertex)) {
                    //Make sure they are not already connected
                    destination = vertexes.get(rand.nextInt(vertexes.size()));
                }
                int weight = rand.nextInt(9)+1;
                String id = "Edge_"+vertex.getName()+"_"+destination.getName()+"_"+weight;
                edges.add(new Edge(id,vertex,destination,weight));
            }
        }

        //Make our graph
        return new Graph(vertexes,edges);
    }

    public Graph getGraph() {
        return graph;
    }

    public Integer getId() {
        return id;
    }

    public void debugPrint(Object msg) {
        debugPrint(msg,false);
    }

    public void debugPrint(Object msg,boolean newline) {
        if (debug) {
            if (msg == null) {
                System.out.print("Node " + self.getId() + ": null");
                if (newline)
                    System.out.println();
                return;
            }
            System.out.print("Node " + self.getId() + ": " + msg.toString());
            if (newline)
                System.out.println();
        }
    }

    public void debugPrintln(Object msg) {
        debugPrint(msg,true);
    }
}


