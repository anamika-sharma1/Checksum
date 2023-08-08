import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.LinkedList;

public class server {
    static Queue q = new Queue();
    static Vector<packet> v = new Vector<packet>();

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
                System.out.println(b);
                sum_before = sum_before + b;
                for (int i = 7; i >= 0; i--) {
                    bitStringBuilder.append((b & (1 << i)) != 0 ? "1" : "0");
                }
            }
            String bitString = bitStringBuilder.toString();
            System.out.println(bitString);
            String checksum = getChecksum(sum_before);
            double no_of_packets = Math.ceil((double) bitString.length() / 8);
            no_of_packets = (int) no_of_packets;
            System.out.println(no_of_packets);
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
                System.out.println(s);
            }
            System.out.println("\n*********************************************");
            packet pp = new packet(i - 1, "dell1234", "127.0.0.1", "127.0.0.1", "6000", "8000",
                    checksum, "Checksum", obj);
            q.enqueue(pp);
        }
        DatagramSocket serverSocket = new DatagramSocket(8000);
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
                    Object object = new Object();
                    try {
                        object = convertToObject(receivePacket.getData());
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                    packet p = (packet) object;
                    if (p.getMessageName().equals("Checksum") == true) {
                        v.add(p);
                        int ans = merge_packets(p.getPayload());
                        v.clear();
                        if (ans == 1) {
                            System.out.println("\nNo error found.");
                        } else {
                            System.out.println("\nError in message received");
                        }
                    } else {
                        v.add(p);
                        System.out.println(
                                "Message from " + p.getDip() + "/" + p.getDport() + " : " + p.getMessageName());
                    }

                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ee) {
                        ee.printStackTrace();
                    }
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
                System.out.println("running send thread");
                if (q.isEmpty() != true) {
                    // System.out.println("anamika");
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
                    // System.out.println("running send thread part 2");
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

        System.out.print("Resulting Sum : " + s1);
        return s1;
    }

    synchronized static int merge_packets(String checksum) {
        Collections.sort(v, new Comparator<packet>() {
            public int compare(packet p1, packet p2) {
                return Integer.compare(p1.getPid(), p2.getPid());
            }
        });
        String received_message = "", msg = "";
        for (packet p : v) {
            // System.out.println(p.getPayload() + "\n");
            if (p.getMessageName() != "Checksum") {
                received_message = received_message + p.getPayload();
            }
            msg = msg + p.getPayload();
        }

        System.out.println("\n\nMessage Received (in bits):-\n" + received_message + "\n");

        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        double size = Math.ceil((double) received_message.length() / 8);
        // System.out.println(size);
        String[] bitArray2 = new String[(int) size];
        String[] bitArray3 = new String[(int) size];
        int i = 0, k = 0;
        while (i < received_message.length()) {
            bitArray2[k++] = received_message.substring(i, i + 8);
            i += 8;
        }

        i = 0;
        k = 0;
        while (i < msg.length()) {
            bitArray3[k++] = msg.substring(i, i + 8);
            i += 8;
        }

        for (String bit : bitArray2) {
            int intValue = Integer.parseInt(bit, 2);
            char charValue = (char) intValue;
            sb.append(charValue);
        }

        for (String bit : bitArray3) {
            int intValue = Integer.parseInt(bit, 2);
            char charValue = (char) intValue;
            sb2.append(charValue);
        }

        String normalString = sb.toString();
        System.out.println("\n\nMessage Received (in char format):-\n" + normalString + "\n");

        String normalString2 = sb2.toString();

        byte[] messageBytes = normalString2.getBytes(StandardCharsets.UTF_8);
        StringBuilder bitStringBuilder = new StringBuilder();
        int sum_before = 0;
        for (byte b : messageBytes) {
            // System.out.println(b);
            sum_before = sum_before + b;
            for (i = 7; i >= 0; i--) {
                bitStringBuilder.append((b & (1 << i)) != 0 ? "1" : "0");
            }
        }

        String result = getChecksum(sum_before);
        for (i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '0') {
                return 0;
            }
        }
        return 1;
    }
}
