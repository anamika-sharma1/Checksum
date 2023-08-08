import java.util.Scanner;
import java.util.Vector;

public class test1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a decimal number: ");
        int decimal = sc.nextInt();
        String s1 = "", s2 = "", a = "";
        int[] binary1 = new int[8]; // assuming a 32-bit binary number
        int[] binary2 = new int[8];
        int index = 0, index2 = 0;
        while (decimal > 0) {
            if (index > 7) {
                binary2[index2++] = decimal % 2;
                decimal /= 2;
            } else {
                binary1[index++] = decimal % 2;
                decimal /= 2;
            }
        }
        System.out.println(index2);
        System.out.println(index);
        System.out.print("Binary number: ");
        for (int i = index2 - 1; i >= 0; i--) {
            System.out.println(binary2[i]);
            a = Integer.toString(binary2[i]);
            s1 = s1 + a;
        }
        for (int i = index - 1; i >= 0; i--) {
            System.out.println(binary1[i]);
            a = Integer.toString(binary1[i]);
            s2 = s2 + a;
        }
        System.out.print(s1 + " -- " + s2);

        int decimal_1 = 0, decimal_2 = 0;
        int base = 1;
        int len = s1.length();
        for (int i = len - 1; i >= 0; i--) {
            if (s1.charAt(i) == '1') {
                decimal_1 += base;
            }
            base *= 2;
        }

        base = 1;
        len = s2.length();
        for (int i = len - 1; i >= 0; i--) {
            if (s2.charAt(i) == '1') {
                decimal_2 += base;
            }
            base *= 2;
        }

        System.out.println("Decimal number: " + decimal_1 + "--" + decimal_2);
        int sum = decimal_1 + decimal_2;
        int[] binary3 = new int[8]; // assuming a 32-bit binary number
        index = 0;
        while (sum > 0) {
            binary3[index++] = sum % 2;
            sum /= 2;
        }
        s1 = "";
        for (int i = index - 1; i >= 0; i--) {
            System.out.println(binary3[i]);
            a = Integer.toString(binary3[i]);
            s1 = s1 + a;
        }

        System.out.print(s1);
        // System.out.println(index);
        // Vector<Integer> nn = new Vector<Integer>();
        // if (index > 8) {
        // for (int i = 1; i <= index - 2; i++) {
        // nn.add(binary[index - i]);
        // }
        // }
    }
}