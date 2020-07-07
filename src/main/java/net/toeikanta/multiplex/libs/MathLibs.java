package net.toeikanta.multiplex.libs;

public class MathLibs {
    // แปลง Double เป็น String ที่เป็นจำนวนเต็มเท่านั้น
    public static String parseDouble(Double num){
        return String.valueOf(Math.round(num));
    }
}
