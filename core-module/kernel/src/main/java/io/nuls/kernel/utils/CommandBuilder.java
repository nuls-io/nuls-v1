
package io.nuls.kernel.utils;

import io.nuls.core.tools.str.StringUtils;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class CommandBuilder {
    private StringBuilder builder = new StringBuilder();
    private static final String LINE_SEPARATOR = "line.separator";
    private int i = 0;

    public CommandBuilder newLine(String content) {
        if(StringUtils.isBlank(content))
            return this.newLine();
        builder.append(content).append(System.getProperty(LINE_SEPARATOR));
        if(i++ == 0)
            this.newLine("\tOPTIONS:");
        return this;
    }

    public CommandBuilder newLine() {
        builder.append(System.getProperty(LINE_SEPARATOR));
        return this;
    }

    public String toString() {
        if(i == 2)
            this.newLine("\tnone");
        return builder.toString();
    }
}
