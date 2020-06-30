package xyz.steamstatistics.misc;

public class Logger {

    public static void log(String log, Class clazz) {
        System.out.println("[Log] >> " + clazz.getSimpleName() + ".java >> " + log);
    }

    public static void error(String error, Class clazz) {
        System.out.println("[Error] >> " + clazz.getSimpleName() + ".java >> " + error);
    }

    public static void warning(String warning, Class clazz) {
        System.out.println("[Warning] >> " + clazz.getSimpleName() + ".java >> " + warning);
    }

}
