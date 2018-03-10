/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.core.utils.log;


import ch.qos.logback.classic.Level;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 公共日志输出工具 <br>
 * 该类提供了基本的日志输出。该类不可以继承。
 * 依赖于slf4j
 *
 * @author Niels
 * @date [2017年9月25日]
 */
public final class MsgLog {
    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger("msgLog");

    /**
     * 日志级别
     */
    private static final Map<String, Level> LOG_LEVELS = new HashMap<>();

    /**
     * 存放deviceId等关键信息
     */
    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 不允许实例化该类
     */
    private MsgLog() {
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * 初始化日志等级（只开放DEBUG/INFO/WARN/ERROR/FATAL 5个级别的配置）
     */
    static {
        LOG_LEVELS.put("DEBUG", Level.DEBUG);
        LOG_LEVELS.put("INFO", Level.INFO);
        LOG_LEVELS.put("WARN", Level.WARN);
        LOG_LEVELS.put("ERROR", Level.ERROR);
    }

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void debug(String msg) {
        if (LOG.isDebugEnabled()) {
            String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                    : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
            LOG.debug(logContent);
        }
    }

    public static void debug(String msg, Object... objs) {
        if (LOG.isDebugEnabled()) {
            String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                    : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
            LOG.debug(logContent, objs);
        }
    }

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void debug(String msg, Throwable throwable) {
        if (LOG.isDebugEnabled()) {
            String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                    : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
            if (!(throwable instanceof NulsException) || !(throwable instanceof NulsRuntimeException)) {
                throwable = new NulsException(ErrorCode.FAILED, throwable);
            }
            LOG.debug(logContent, throwable);
        }
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void info(String msg) {
        //String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg): (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        String logContent = isStringBlank(getId()) ? (":" + msg)
                : (":" + msg);
        LOG.info(logContent);
    }

    public static void info(String msg, Object... objs) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        LOG.info(logContent, objs);
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void info(String msg, Throwable throwable) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        if (!(throwable instanceof NulsException) || !(throwable instanceof NulsRuntimeException)) {
            throwable = new NulsException(ErrorCode.FAILED, throwable);
        }
        LOG.info(logContent, throwable);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void warn(String msg) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        LOG.warn(logContent);
    }

    public static void warn(String msg, Object... objs) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        LOG.warn(logContent, objs);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void warn(String msg, Throwable throwable) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        if (!(throwable instanceof NulsException) || !(throwable instanceof NulsRuntimeException)) {
            throwable = new NulsException(ErrorCode.FAILED, throwable);
        }
        LOG.warn(logContent, throwable);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void error(String msg) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        LOG.error(logContent);
    }


    public static void error(String msg, Object... objs) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        LOG.error(logContent, objs);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void error(String msg, Throwable throwable) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        if (!(throwable instanceof NulsException) || !(throwable instanceof NulsRuntimeException)) {
            throwable = new NulsException(ErrorCode.FAILED, throwable);
        }
        LOG.error(logContent, throwable);
    }

    public static void error(Throwable throwable) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":")
                : (getLogTrace() + "[" + getId() + "]" + ":");
        if (!(throwable instanceof NulsException) || !(throwable instanceof NulsRuntimeException)) {
            throwable = new NulsException(ErrorCode.FAILED, throwable);
        }
        LOG.error(logContent, throwable);
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void trace(String msg) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        LOG.trace(logContent);
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void trace(String msg, Throwable throwable) {
        String logContent = isStringBlank(getId()) ? (getLogTrace() + ":" + msg)
                : (getLogTrace() + "[" + getId() + "]" + ":" + msg);
        if (!(throwable instanceof NulsException) || !(throwable instanceof NulsRuntimeException)) {
            throwable = new NulsException(ErrorCode.FAILED, throwable);
        }
        LOG.trace(logContent, throwable);
    }


    private static boolean isStringBlank(String val) {
        return null == val || val.trim().isEmpty();
    }

    /**
     * 获取日志记录点的全路径
     *
     * @return 日志记录点的全路径
     */
    private static String getLogTrace() {
        StringBuilder logTrace = new StringBuilder();
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        if (stack.length > 1) {
            // index为3上一级调用的堆栈信息，index为1和2都为Log类自己调两次（可忽略），index为0为主线程触发（可忽略）
            StackTraceElement ste = stack[3];
            if (ste != null) {
                // 获取类名、方法名、日志的代码行数
                logTrace.append(ste.getClassName());
                logTrace.append('.');
                logTrace.append(ste.getMethodName());
                logTrace.append('(');
                logTrace.append(ste.getFileName());
                logTrace.append(':');
                logTrace.append(ste.getLineNumber());
                logTrace.append(')');
            }
        }

        return logTrace.toString();
    }

//    /**
//     * 设置日志级别
//     *
//     * @param level 日志级别
//     */
//    public static void setLevel(String level) {
//        if (LOG_LEVELS.containsKey(level.toUpperCase())) {
//            LOG.setLevel(LOG_LEVELS.get(level.toUpperCase()));
//        }
//    }

    /**
     * 获取当前线程16位唯一序列号
     *
     * @return 当前线程16位唯一序列号
     */
    private static String getId() {
        return THREAD_LOCAL.get();
    }

    /**
     * 设置日志流水号
     *
     * @param id 流水号
     */
    public static void setId(String id) {
        THREAD_LOCAL.set(id);
    }

    public static void removeId() {
        THREAD_LOCAL.remove();
    }
}
