import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    private static final int basePort=12345;
    private static int maxNodes;

    public static void main(String[] args) {
        if (args.length==0) {
            //We are the spawner. Launch the others
            ClassLoader classLoader = Main.class.getClassLoader();
            InputStream inputFile = classLoader.getResourceAsStream("graph.txt");
            Scanner in = new Scanner(inputFile);
            maxNodes = in.nextInt();
            for (int i=0;i<maxNodes;i++) {
                //Spawn more of ourself.
                String[] parms = new String[1];
                parms[0] = Integer.toString(i);
                new Thread(() -> {
                    try {
                        Path path = (new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())).toPath();
                        String cmd = String.format("cmd /c start cmd.exe /K \"cd %s && java -jar %s %s && exit\"",path.getParent(),path.toAbsolutePath(),parms[0]);
                        Process proc = Runtime.getRuntime().exec(cmd);
                        proc.waitFor();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } else if (Integer.parseInt(args[0])>=0) {
            int nodeID = Integer.parseInt(args[0]);
            NetworkNode node = new NetworkNode(nodeID,basePort);
            System.out.println("I'm Node "+args[0]);
        } else {
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
