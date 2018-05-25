package io.nuls.client.cmd;

import io.nuls.client.constant.CommandConstant;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Cmd;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.processor.CommandProcessor;

import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class CommandHandler {

    public static final Map<String, CommandProcessor> PROCESSOR_MAP = new TreeMap<>();

    private CommandHandler() {

    }

    private static CommandHandler instance = new CommandHandler();

    public static CommandHandler getInstance() {
        return instance;
    }
    /**
     * 初始化加载所有命令行实现
     */
    public void init() {
        Collection<Object> list = SpringLiteContext.getAllBeanList();
        for (Object object : list) {
            if (object.getClass().getAnnotation(Cmd.class) != null) {
                Log.debug("register Cmd processor:{}", object.getClass());
                CommandProcessor processor = (CommandProcessor) object;
                PROCESSOR_MAP.put(processor.getCommand(), processor);
            }
        }
    }


    public static void main(String[] args) {
        System.out.print(CommandConstant.COMMAND_PS1);
        Scanner scan = new Scanner(System.in);
        while (scan.hasNextLine()) {
            String read = scan.nextLine().trim();
            if (StringUtils.isBlank(read)) {
                System.out.print(CommandConstant.COMMAND_PS1);
                continue;
            }
            System.out.print(instance.processCommand(read.split("\\s+")) + "\n" + CommandConstant.COMMAND_PS1);
        }
    }


    private String processCommand(String[] args) {
        int length = args.length;
        if (length == 0) {
            return CommandConstant.COMMAND_ERROR;
        }
        String command = args[0];
        CommandProcessor processor = PROCESSOR_MAP.get(command);
        if (processor == null) {
            return command + " not a nuls command!";
        }
        if(length == 2 && CommandConstant.NEED_HELP.equals(args[1])) {
            return processor.getHelp();
        }
        try {
            boolean result = processor.argsValidate(args);
            if (!result) {
                return "args incorrect:\n" + processor.getHelp();
            }
            return processor.execute(args).toString();
        } catch (Exception e) {
            return CommandConstant.EXCEPTION + ": " + e.getMessage();
        }
    }
}
