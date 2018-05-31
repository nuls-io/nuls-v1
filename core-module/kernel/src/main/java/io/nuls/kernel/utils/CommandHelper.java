package io.nuls.kernel.utils;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.Na;
import jline.console.ConsoleReader;

import java.io.Console;
import java.io.IOException;
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

/*  public static void confirmPwd(String newPwd) {
        System.out.print("Please enter your password again:");
        Scanner scanner = new Scanner(System.in);
        String confirmed = scanner.nextLine();
        while (!newPwd.equals(confirmed)) {
            System.out.print("The password you entered did not match.\nEnter your new password: ");
            confirmed = scanner.nextLine();
        }
    }

   public static String getPwd(){
        System.out.print("Please enter the password, if the account has no password directly return.\npassword:");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }*/

    public static void confirmPwd(String newPwd) {
        System.out.print("Please enter your new password again:");
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String confirmed = null;
            do {
                confirmed = reader.readLine('*');
                if(!newPwd.equals(confirmed)){
                    System.out.print("The password you entered did not match.\nEnter your new password: ");
                }
            } while (!newPwd.equals(confirmed));
        } catch (IOException e) {

        }finally {
            try {
                if(!reader.delete()){
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getPwd(){
        System.out.print("Please enter the password, if the account has no password directly return.\nEnter your password:");
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String pwd = reader.readLine('*');
            return pwd;
        } catch (IOException e) {
            return null;
        }finally {
            try {
                if(!reader.delete()){
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
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

    public static String naToNuls(Object object){
        if(null == object){
            return null;
        }
        Long na = null;
        if(object instanceof Long){
            na = (Long)object;
        }else if(object instanceof Integer){
            na = ((Integer)object).longValue();
        }else{
            return null;
        }
        return (Na.valueOf(na)).toText();
    }

    public static String txTypeExplain(Integer type){
        if(null == type){
            return null;
        }
        switch (type){
            case 1:
                return "coinbase";
            case 2:
                return "transfer";
            case 51:
                return "account_alias";
            case 90:
                return "register_agent";
            case 91:
                return "join_consensus";
            case 92:
                return "cancel_deposit";
            case 93:
                return "yellow_punish";
            case 94:
                return "red_punish";
            case 95:
                return "stop_agent";
            default:
                return null;
        }
    }

    public static String consensusExplain(Integer status){
        if(null == status){
            return null;
        }
        switch (status){
            case 0:
                return "unconsensus";
            case 1:
                return "consensus";

            default:
                return null;
        }
    }

    public static String statusConfirmExplain(Integer status){
        if(null == status){
            return null;
        }
        switch (status){
            case 0:
                return "confirm";
            case 1:
                return "unConfirm";

            default:
                return null;
        }
    }
}
