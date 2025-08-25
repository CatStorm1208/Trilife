package de.catstorm.trilife.logic;

//Actually, Java is probably not to blame here. It's probably just my IDE yelling at me. But seriously, why did Math.TAU only get added in Java 19???
public final class FuckTheJavaStandardMathLibrary {
    public static double TAU = Math.PI*2;

    public static class TimeConstants {
        //Seconds
        public static int t5s = 5*20;
        public static int t10s = 10*20;
        public static int t15s = 15*20;
        public static int t30s = 30*20;

        //Minutes
        public static int t5min = 300*20;

        //Hours
        public static int t1h = 3600*20;
    }
}