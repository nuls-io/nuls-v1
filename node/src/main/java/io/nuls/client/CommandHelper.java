package io.nuls.client;

import java.util.Scanner;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class CommandHelper {

    public static boolean checkArgsIsNull(String... args) {
        for(String arg : args) {
            if(arg == null)
                return false;
        }
        return true;
    }

    public static void confirmPwd(String newPwd) {
        System.out.print("Please confirm pwd:");
        Scanner scanner = new Scanner(System.in);
        String confirmed = scanner.nextLine();
        while (!newPwd.equals(confirmed)) {
            System.out.print("The password you entered did not match.\nPlease enter confirm pwd:");
            confirmed = scanner.nextLine();
        }
    }
}
