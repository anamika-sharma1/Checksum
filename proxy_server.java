import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.LinkedList;

public class proxy_server {
    static Queue q = new Queue();

    public static void main(String[] args) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(7000);
        Thread receiveThread = new Thread(new ReceiveDatagram(serverSocket));
        receiveThread.start();
        Thread sendThread = new Thread(new SendDatagram(serverSocket));
        sendThread.start();
    }

    static class ReceiveDatagram implements Runnable {
        private DatagramSocket serverSocket;

        public ReceiveDatagram(DatagramSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            byte[] receiveData = new byte[1024];

            while (true) {
                System.out.println("running read thread");
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    System.out.println("yes");
                    Object object = new Object();
                    try {
                        object = convertToObject(receivePacket.getData());
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                    packet p = (packet) object;
                    q.enqueue(p);
                    System.out.println("RECEIVED: packet from " + p.getSip() + "/" + p.getSport());
                } catch (Exception e) {
                    System.out.println("Waiting for packet");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class SendDatagram implements Runnable {
        private DatagramSocket serverSocket;

        public SendDatagram(DatagramSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            while (true) {
                System.out.println("running send_thread");
                // Create the datagram to send to the destination
                if (q.isEmpty() != true) {
                    packet p = (packet) q.dequeue();
                    if (p.getMessageName().equals("Checksum") == false) {
                        double rand = Math.random();
                        double x = rand * 10;
                        int y = (int) x % 7;
                        System.out.println(y);
                        if (y % 2 != 0) {
                            String s = p.getPayload();
                            if (s.charAt(y) == '1') {
                                s = s.substring(0, y) + "0" + s.substring(y + 1, 8);
                            } else {
                                s = s.substring(0, y) + "1" + s.substring(y + 1, 8);
                            }
                            p.setPayload(s);
                        }
                    }
                    try {
                        InetAddress dipad = InetAddress.getByName(p.getDip());
                        ByteArrayOutputStream bb = new ByteArrayOutputStream();
                        ObjectOutputStream oo = new ObjectOutputStream(bb);
                        oo.writeObject(p);
                        byte[] objectData = bb.toByteArray();
                        int port_no = Integer.parseInt(p.getDport());
                        DatagramPacket dp1 = new DatagramPacket(objectData, objectData.length,
                                InetAddress.getLocalHost(),
                                port_no);
                        serverSocket.send(dp1);
                        // System.out.println("Press a key to continue...");
                        // Scanner scanner = new Scanner(System.in);
                        // char c = scanner.next().charAt(0);
                    } catch (Exception e) {
                        System.out.println("Cannot send packet");
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static Object convertToObject(byte[] data) throws IOException,
            ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }
}
