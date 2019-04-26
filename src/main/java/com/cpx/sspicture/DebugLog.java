/**
 * jackball All Rights Reserved.
 */

package com.cpx.sspicture;


import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @describe 创建一个简单并且更易懂的log
 * @author jackball
 * @date: 2014-10-10 下午11:27:25
 */
public class DebugLog {

    /**
     * 类名
     */
    private static String className;

    /**
     * 方法名
     */
    private static String methodName;

    /**
     * 行数
     */
    private static int lineNumber;

    /**
     * Android's max limit for a log entry is ~4076 bytes,
     * so 4000 bytes is used as chunk size since default charset
     * is UTF-8
     */
    private static final int CHUNK_SIZE = 4000;
    public static final int V = 0x1;
    public static final int D = 0x2;
    public static final int I = 0x3;
    public static final int W = 0x4;
    public static final int E = 0x5;
    public static final int A = 0x6;
    private static final int JSON = 0x7;
    /**
     * It is used for json pretty print
     */
    private static final int JSON_INDENT = 2;
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final char HORIZONTAL_DOUBLE_LINE = '║';

    private static boolean isDebuggable = true;

    private DebugLog() {
    }

    public static boolean isDebuggable() {
        return isDebuggable;
    }

    private static String createLog(String log) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(methodName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append("]");
        buffer.append(log);

        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void e(String message) {

        if( ! isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.e(className, createLog(message));
    }

    public static void i(String message) {
        if( ! isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.i(className, createLog(message));
    }

    public static void d(String message) {
        if( ! isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.d(className, createLog(message));
    }

    public static void v(String message) {
        if( ! isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.v(className, createLog(message));
    }

    public static void w(String message) {
        if( ! isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.w(className, createLog(message));
    }

    public static void wtf(String message) {
        if( ! isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.wtf(className, createLog(message));
    }

    public static void e(String message, Throwable tr) {
        if( ! isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.e(className, createLog(message), tr);
    }

    public static void wtf(String message, Throwable tr) {
        if( ! isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.wtf(className, createLog(message), tr);
    }

    /******************** 一般调试 *********************/
    public static void v(String tag, String msg) {
        if( ! isDebuggable() || msg == null)
            return;
        Log.v(tag, msg);

    }

    public static void d(String tag, String msg) {
        if( ! isDebuggable() || msg == null)
            return;
        Log.d(tag, msg);

    }

    public static void i(String tag, String msg) {
        if( ! isDebuggable() || msg == null)
            return;
        Log.i(tag, msg);

    }

    public static void w(String tag, String msg) {
        if( ! isDebuggable() || msg == null)
            return;
        Log.w(tag, msg);

    }

    public static void e(String tag, String msg) {
        if( ! isDebuggable() || msg == null)
            return;
        Log.e(tag, msg);

    }

    public static void e(String tag, String msg, Throwable tr) {
        if( ! isDebuggable() || msg == null)
            return;
        Log.e(tag, msg, tr);
    }

    public static void wtf(String tag, String msg, Throwable tr) {
        if( ! isDebuggable() || msg == null)
            Log.wtf(tag, msg, tr);
    }

    /**
     *
     * @param tag
     * @param jsonFormat
     */
    public static void json(String tag, String jsonFormat) {
        if (!isDebuggable()) {
            return;
        }
        printJson(tag, jsonFormat);
    }


    /******************** end *********************/



    public static void printDefault(int type, String tag, String msg) {
        int index = 0;
        int countOfSub = msg.length() / CHUNK_SIZE;
        if (countOfSub > 0) {
            for (int i = 0; i < countOfSub; i++) {
                String sub = msg.substring(index, index + CHUNK_SIZE);
                printSub(type, tag, sub);
                index += CHUNK_SIZE;
            }
            printSub(type, tag, msg.substring(index, msg.length()));
        } else {
            printSub(type, tag, msg);
        }
    }

    private static void printSub(int type, String tag, String sub) {
        switch (type) {
            case V:
                Log.v(tag, sub);
                break;
            case D:
                Log.d(tag, sub);
                break;
            case I:
                Log.i(tag, sub);
                break;
            case W:
                Log.w(tag, sub);
                break;
            case E:
                Log.e(tag, sub);
                break;
            case A:
                Log.wtf(tag, sub);
                break;
        }
    }


    /**
     * print json 字符串
     * @param tag
     * @param json
     */
    private static void printJson(String tag, String json) {
        if (TextUtils.isEmpty(json)) {
            d(tag, "Empty/Null json Content");
            return;
        }

        try {
            String message;
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                message = jsonObject.toString(JSON_INDENT);
            } else if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                message = jsonArray.toString(JSON_INDENT);
            } else {
                message = "";
            }
            if (TextUtils.isEmpty(message)) {
                e(tag, "Invalid Json");
            } else {
                byte[] bytes = message.getBytes();
                if (bytes.length <= CHUNK_SIZE) {
                    d(tag, message);
                } else {
                    String[] lines = message.split(LINE_SEPARATOR);
                    for (String line : lines) {
                        d(tag, line);
                    }
                }
            }
        } catch (JSONException e) {
            e(tag, "Invalid Json");
        }
    }
}
