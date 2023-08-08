import java.util.Scanner;

public class tes2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a binary number: ");
        String binary = sc.nextLine();

        int decimal = 0;
        int base = 1;
        int len = binary.length();
        for (int i = len - 1; i >= 0; i--) {
            if (binary.charAt(i) == '1') {
                decimal += base;
            }
            base *= 2;
        }

        System.out.println("Decimal number: " + decimal);
    }
}