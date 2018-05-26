package io.nuls.core.tools.cmd;

import java.util.Scanner;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class CommandHelper {

    public static boolean checkArgsIsNull(String... args) {
        for (String arg : args) {
            if (arg == null || arg.trim().length() == 0) {
                return false;
            }
        }
        return true;
    }

    public static void confirmPwd(String newPwd) {
        System.out.print("Please enter your password again:");
        Scanner scanner = new Scanner(System.in);
        String confirmed = scanner.nextLine();
        while (!newPwd.equals(confirmed)) {
            System.out.print("The password you entered did not match.\nPlease confirm the new password:");
            confirmed = scanner.nextLine();
        }
    }

    public static String getPwd(){
        System.out.print("Please enter the password, if the account has no password directly return.\npassword:");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}
