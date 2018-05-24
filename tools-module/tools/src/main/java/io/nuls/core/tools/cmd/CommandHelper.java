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
            if (arg == null || arg.trim().length() == 0)
                return false;
        }
        return true;
    }


    public static void confirmPwd(String newPwd) {
        System.out.print("Please confirm the new password:");
        Scanner scanner = new Scanner(System.in);
        String confirmed = scanner.nextLine();
        while (!newPwd.equals(confirmed)) {
            System.out.print("The password you entered did not match.\nPlease confirm the new password:");
            confirmed = scanner.nextLine();
        }
    }

//    public static void checkAndConfirmPwd(String pwd) {
//        Result result = null;
//        //AccountService.ACCOUNT_SERVICE.getAccountList();
//        List list = (List) result.getData();
//        if (list == null || list.size() == 0) {
//            System.out.print("Please confirm the new password:");
//            Scanner scanner = new Scanner(System.in);
//            String confirmed = scanner.nextLine();
//            while (!pwd.equals(confirmed)) {
//                System.out.print("The password you entered did not match.\nPlease confirm the new password:");
//                confirmed = scanner.nextLine();
//            }
//        }
//    }
//
//    public static Long getLongAmount(String arg) {
//        Na na = null;
//        try {
//            na = Na.parseNuls(arg);
//            return na.getValue();
//        } catch (Exception e) {
//            return null;
//        }
//    }

}
