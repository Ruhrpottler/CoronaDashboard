package Tools;

import android.content.Context;

import com.blackviper.coronadashboard.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatTool {

    private final Context context;

    public FormatTool(Context context)
    {
        this.context = context;
    }

    public static String intToString(int value) //TODO rename. Keine Aussagekraft //TODO "use Locale" Meldung im CityDataModel
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
        {
            str = cutLastString(str);
        }

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
        if (stelle < 0)
        {
            throw new IllegalArgumentException(String.format("Runden auf die Stelle '%d' ist nicht möglich.", stelle));
        }

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(stelle, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String[] seperateBezAndGen(String cityName)
    {
        cityName = cityName.toLowerCase();
        String[] cityNameArray;// = new String[2];
        if (cityName.startsWith(context.getString(R.string.STR_KREISFREIE_STADT).toLowerCase()))
        {
            cityNameArray = seperateString(context.getString(R.string.STR_KREISFREIE_STADT).toLowerCase(), cityName);
        }
        else if (cityName.startsWith(context.getString(R.string.STR_LANDKREIS).toLowerCase()))
        {
            cityNameArray = seperateString(context.getString(R.string.STR_LANDKREIS).toLowerCase(), cityName);
        }
        else if (cityName.startsWith(context.getString(R.string.STR_STADTKREIS).toLowerCase()))
        {
            cityNameArray = seperateString(context.getString(R.string.STR_STADTKREIS).toLowerCase(), cityName);
        }
        else if (cityName.startsWith(context.getString(R.string.STR_KREIS).toLowerCase()))
        {
            cityNameArray = seperateString(context.getString(R.string.STR_KREIS).toLowerCase(), cityName);
        }
        else if (cityName.startsWith(context.getString(R.string.STR_BEZIRK).toLowerCase()))
        {
            cityNameArray = seperateString(context.getString(R.string.STR_BEZIRK).toLowerCase(), cityName);
        }
        else
        {
            cityNameArray = new String[]{"", cityName};
        }

        return cityNameArray;
    }

    private String[] seperateString(String firstPart, String fullString)
    {
        return new String[]{firstPart, fullString.replace(firstPart, "").trim()};
    }
}
