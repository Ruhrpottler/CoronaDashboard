package Tools;

import android.util.Log;

import androidx.annotation.NonNull;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public class DateFormatTool
{
    protected static final String FORMAT_GER = "dd.MM.yyyy";
    private static final String FORMAT_SORT = "yyMMdd";

    public static String getFormatGerman()
    {
        return FORMAT_GER;
    }

    public static String getFormatSort()
    {
        return FORMAT_SORT;
    }

    /**
     * Validates the input-format and converts it.
     * @param input format dd.MM.yyyy
     * @return format yyMMdd
     */
    public static String germanToSort(@NonNull String input)
    {
        return validateAndConvert(input, FORMAT_GER, FORMAT_SORT);
    }

    /**
     * Validates the input-format and converts it. No validation if the date is really valid (e.g. 31.02.2022)
     * @param input format yyMMdd
     * @return format dd.MM.yyyy
     */
    public static String sortToGerman(@NonNull String input)
    {
        return validateAndConvert(input, FORMAT_SORT, FORMAT_GER);
    }

    private static String validateAndConvert(String input, String formatFrom, String formatTo)
    {
        if(input == null || input.trim().isEmpty())
        {
            throw new IllegalArgumentException("String input is null or empty.");
        }
        input = input.trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatFrom, Locale.GERMAN)
                .withResolverStyle(ResolverStyle.STRICT);
        String result;
        try
        {
            TemporalAccessor ta = formatter.parse(input);
            formatter = DateTimeFormatter.ofPattern(formatTo, Locale.GERMAN)
                    .withResolverStyle(ResolverStyle.STRICT);
            result = formatter.format(ta);
        }
        catch(DateTimeParseException e)
        {
            String msg = String.format("The date '%s' has an invalid date format, should be '%s'.", input, formatFrom);
            Log.e(DateFormatTool.class.toString(), msg + "\n" + e.getStackTrace().toString());
            throw new IllegalArgumentException(msg);
        }

        Log.d(DateFormatTool.class.toString(),
                String.format("Converted date '%s' to format '%s'. Result: '%s'", input, formatTo, result));
        return result;
    }
}
