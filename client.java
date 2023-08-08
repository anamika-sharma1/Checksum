import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.LinkedList;

class sc implements Serializable {
    private String cno;
    private String username;
    private String userpassword;
    private String certid;

    public sc(String a, String b, String c, String d) {
        this.cno = a;
        this.username = b;
        this.userpassword = c;
        this.certid = d;
    }

    public String getCno() {
        return cno;
    }

    public String getUsername() {
        return username;
    }

    public String getUserpassword() {
        return userpassword;
    }

    public String getCertid() {
        return certid;
    }
}

class packet implements Serializable {
    private int packet_id;
    private String client_id;
    private String source_ip;
    private String destination_ip;
    private String sport;
    private String dport;
    private String payload;
    private String message_name;
    private sc security_certificate;
    private int ack;

    public packet(int pid, String cid, String sip, String dip, String spo, String dpo, String pl, String msg, sc ob) {
        this.packet_id = pid;
        this.client_id = cid;
        this.source_ip = sip;
        this.destination_ip = dip;
        this.sport = spo;
        this.dport = dpo;
        this.payload = pl;
        this.message_name = msg;
        this.security_certificate = ob;
    }

    public void setPayload(String pl) {
        this.payload = pl;
    }

    public void setAck(int ac) {
        this.ack = ac;
    }

    public int getAck() {
        return ack;
    }

    public int getPid() {
        return packet_id;
    }

    public String getCid() {
        return client_id;
    }

    public String getSip() {
        return source_ip;
    }

    public String getDip() {
        return destination_ip;
    }

    public String getSport() {
        return sport;
    }

    public String getDport() {
        return dport;
    }

    public String getPayload() {
        return payload;
    }

    public String getMessageName() {
        return message_name;
    }

    public sc getSecurityCertificate() {
        return security_certificate;
    }
}

class Queue {
    private LinkedList<packet> list = new LinkedList<packet>();

    public void enqueue(packet item) {
        list.addLast(item);
    }

    public Object dequeue() {
        return list.removeFirst();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public Object peek() {
        return list.getFirst();
    }
}

public class client {
    static Queue q = new Queue();

    public static void main(String[] args) throws Exception {
        System.out.println("Want to send(s)/receive(r) data : ");
        Scanner scanner = new Scanner(System.in);
        char c = scanner.next().charAt(0);
        String message = "", temp = "";
        Vector<DatagramPacket> packetsToSend = new Vector<>();
        if (c == 's') {
            System.out.println("Enter Message to be sent :");
            while (true) {
                temp = "";
                temp = scanner.nextLine();
                if (temp.equals("0")) {
                    break;
                } else {
                    message = message + temp + "\n";
                }
            }
            // System.out.println(message);
            scanner.close();
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            StringBuilder bitStringBuilder = new StringBuilder();
            int sum_before = 0;
            for (byte b : messageBytes) {
                // System.out.println(b);
                sum_before = sum_before + b;
                for (int i = 7; i >= 0; i--) {
                    bitStringBuilder.append((b & (1 << i)) != 0 ? "1" : "0");
                }
            }
            String bitString = bitStringBuilder.toString();
            // System.out.println(bitString);
            String checksum = getChecksum(sum_before);
            double no_of_packets = Math.ceil((double) bitString.length() / 8);
            no_of_packets = (int) no_of_packets;
            // System.out.println(no_of_packets);
            int i = 0, k = 0, j = 0, carry = 0, n = 0, z = 0;
            int[] sum = new int[10];
            String s;
            sc obj = new sc("1234", "Anamika", "abcd", "0987");
            for (i = 1; i <= no_of_packets; i++) {
                s = bitString.substring(k, k + 8);
                k += 8;
                packet p = new packet(i - 1, "dell1234", "127.0.0.1", "127.0.0.1", "6000", "8000",
                        s, "Message from client", obj);
                // p.setAck(1);
                q.enqueue(p);
                // System.out.println(s);
            }
            System.out.println("\n*********************************************");
            packet pp = new packet(i - 1, "dell1234", "127.0.0.1", "127.0.0.1", "6000", "8000",
                    checksum, "Checksum", obj);
            q.enqueue(pp);
        }
        DatagramSocket serverSocket = new DatagramSocket(6000);
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
                // System.out.println("running read thread");
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    Object object = new Object();
                    try {
                        object = convertToObject(receivePacket.getData());
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                    packet p = (packet) object;
                    System.out.println("Message from " + p.getDip() + "/" + p.getDport() + " : " + p.getMessageName());

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
                if (q.isEmpty() != true) {
                    // System.out.println("kk");
                    packet p = (packet) q.dequeue();
                    try {
                        ByteArrayOutputStream bb = new ByteArrayOutputStream();
                        ObjectOutputStream oo = new ObjectOutputStream(bb);
                        oo.writeObject(p);
                        byte[] objectData = bb.toByteArray();
                        int port_no = Integer.parseInt(p.getDport());
                        DatagramPacket dp1 = new DatagramPacket(objectData, objectData.length,
                                InetAddress.getLocalHost(),
                                7000);
                        serverSocket.send(dp1);
                        // System.out.println("Sent all packets...");
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

    static String getChecksum(int x) {
        String s1 = "", s2 = "", a = "";
        int[] binary1 = new int[8];
        int[] binary2 = new int[8];
        int index = 0, index2 = 0;
        while (x > 0) {
            if (index > 7) {
                binary2[index2++] = x % 2;
                x /= 2;
            } else {
                binary1[index++] = x % 2;
                x /= 2;
            }
        }
        if (index < 7 && index2 == 0) {
            while (index < 7) {
                binary1[index++] = 0;
            }
        }
        // System.out.println(index2);
        // System.out.println(index);
        // System.out.print("Binary number: ");
        if (index2 != 0) {
            for (int i = index2 - 1; i >= 0; i--) {
                // System.out.println(binary2[i]);
                a = Integer.toString(binary2[i]);
                s1 = s1 + a;
            }
        }
        for (int i = index - 1; i >= 0; i--) {
            // System.out.println(binary1[i]);
            a = Integer.toString(binary1[i]);
            s2 = s2 + a;
        }
        // System.out.print(s1 + " -- " + s2);

        int decimal_1 = 0, decimal_2 = 0;
        int base = 1;
        int len = s1.length();
        if (index2 != 0) {
            for (int i = len - 1; i >= 0; i--) {
                if (s1.charAt(i) == '1') {
                    decimal_1 += base;
                }
                base *= 2;
            }
        }

        base = 1;
        len = s2.length();
        for (int i = len - 1; i >= 0; i--) {
            if (s2.charAt(i) == '1') {
                decimal_2 += base;
            }
            base *= 2;
        }

        // System.out.println("Decimal number: " + decimal_1 + "--" + decimal_2);
        int sum = decimal_1 + decimal_2;
        int[] binary3 = new int[8]; // assuming a 32-bit binary number
        index = 0;
        while (sum > 0) {
            binary3[index++] = sum % 2;
            sum /= 2;
        }
        s1 = "";
        for (int i = index - 1; i >= 0; i--) {
            // System.out.println(binary3[i]);
            a = Integer.toString(binary3[i]);
            s1 = s1 + a;
        }

        System.out.print("Checksum : " + s1);
        return s1;
    }
}
