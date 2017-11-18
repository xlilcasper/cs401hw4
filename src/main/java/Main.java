import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Main {
    private static final int basePort=12345;
    private static int maxNodes;

    public static void main(String[] args) {
                if (args.length!=0) {
            int nodeID = Integer.parseInt(args[0]);
            //bring our nodes up slowly
            try {
                Thread.sleep(nodeID*3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            NetworkNode node = new NetworkNode(nodeID, basePort);
            return;
        }
        else {
            ClassLoader classLoader = Main.class.getClassLoader();
            InputStream inputFile = classLoader.getResourceAsStream("graph.txt");
            Scanner in = new Scanner(inputFile);
            maxNodes = in.nextInt();
            for (int i=0;i<maxNodes;i++) {
                //Spawn more of ourself.
                String[] parms = new String[1];
                parms[0] = Integer.toString(i);
                new Thread(() -> {
                    new Main().main(parms);
                }).start();
            }
        }

        System.out.println("Press enter to exit");
        try {
            while(System.in.available()==0) {}
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            InetAddress inetAddress = InetAddress.getByName("255.255.255.255");
            broadcast("exit",inetAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void broadcast( String data,InetAddress address) throws IOException {
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

}
