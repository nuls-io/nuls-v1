package io.nuls.client;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Niels on 2017/10/26.
 * nuls.io
 */
public class CommandHandle {
    public static void main(String[] args) throws IOException {
        System.out.print("nuls>>> ");
        Scanner scan = new Scanner(System.in);
        while (true) {
            String read = scan.nextLine();
            doCommand(read);
            System.out.print("nuls>>> ");
        }
    }

    private static void doCommand(String read) {
        switch (read.toUpperCase()){
            case "EXIT":
                doStop();
            case "":
                break;
        }
    }

    private static void doStop() {
        System.out.println("stoping...");
        System.exit(0);
    }
}
