package Tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatTool {

    public static String intToString(int value)
    {
        return NumberFormat.getNumberInstance(Locale.GERMANY).format(value);
    }

    public static String doubleToString(double value, int anzahlStellen)
    {
        //TODO anzStellen automatisch herausfinden, wenn möglich
        String str = String.format("%." + anzahlStellen + "f", value).replace(".", ",");

        //Nachkommaanteil 0 soll wegfallen (150,00 --> 150).
        while(str.endsWith("0"))
        {
            str = cutLastString(str);
        }
        if(str.endsWith(","))
            str = cutLastString(str);

        return str;
    }

    public static String cutLastString(String str)
    {
        return str.substring(0, str.length() - 1);
    }

    /** Rundet "normal (ab 5 auf, vorher ab)
     * BigDecimal Round Modus Doku: https://docs.oracle.com/javase/7/docs/api/java/math/RoundingMode.html
     * @param value
     * @param stelle Diese Zahl gibt an, wie viele Zahlen (abgesehen von 0) hinter dem Komma stehen bleiben.
     *               Stelle 2 heißt also, es wird auf die 3. Stelle geschaut und anhand dessen entschieden.
     *               Bsp: 3.4537 wird zu 3.45
     */
    public static double roundDouble(double value, int stelle)
    {
        if (stelle < 0) throw new IllegalArgumentException(String.format("Runden auf die Stelle '%d' ist nicht möglich.", stelle));

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(stelle, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
