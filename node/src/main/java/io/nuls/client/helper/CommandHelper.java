package io.nuls.client.helper;

import io.nuls.core.chain.entity.Na;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.AccountService;

import java.util.List;
import java.util.Scanner;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class CommandHelper {

    public static boolean checkArgsIsNull(String... args) {
        for(String arg : args) {
            if(arg == null || arg.trim().length() == 0)
                return false;
        }
        return true;
    }



    public static void confirmPwd(String newPwd) {
        System.out.print("Please confirm password:");
        Scanner scanner = new Scanner(System.in);
        String confirmed = scanner.nextLine();
        while (!newPwd.equals(confirmed)) {
            System.out.print("The password you entered did not match.\nPlease confirm password:");
            confirmed = scanner.nextLine();
        }
    }

    public static void checkAndConfirmPwd(String pwd) {
        RpcClientResult result = AccountService.ACCOUNT_SERVICE.getAccountList();
        List list = (List)result.getData();
        if(list == null || list.size() == 0) {
            System.out.print("Please confirm password:");
            Scanner scanner = new Scanner(System.in);
            String confirmed = scanner.nextLine();
            while (!pwd.equals(confirmed)) {
                System.out.print("The password you entered did not match.\nPlease confirm password:");
                confirmed = scanner.nextLine();
            }
        }
    }

    public static Long getLongAmount(String arg) {
        Na na = null;
        try {
            na = Na.parseNuls(arg);
            return na.getValue();
        } catch (Exception e) {
            return null;
        }
    }
}
