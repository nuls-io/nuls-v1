package io.nuls.client.helper;

import io.nuls.core.utils.str.StringUtils;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class CommandBulider {
    private StringBuilder builder = new StringBuilder();
    private static final String LINE_SEPARATOR = "line.separator";
    private int i = 0;

    public CommandBulider newLine(String content) {
        if(StringUtils.isBlank(content))
            return this.newLine();
        builder.append(content).append(System.getProperty(LINE_SEPARATOR));
        if(i++ == 0)
            this.newLine("\tOPTIONS:");
        return this;
    }

    public CommandBulider newLine() {
        builder.append(System.getProperty(LINE_SEPARATOR));
        return this;
    }

    public String toString() {
        if(i == 2)
            this.newLine("\tnone");
        return builder.toString();
    }
}
