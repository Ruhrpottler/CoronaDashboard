package Tools;

import android.util.Log;

import androidx.annotation.NonNull;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAccessor;

public class DateFormatTool
{
    private static final String FORMAT_GER = "dd.MM.yyyy";
    private static final String FORMAT_SORT = "yyMMdd";
    //private boolean validate

    /**
     * Validates the input-format and converts it.
     * @param input format dd.MM.yyyy
     * @return format yyMMdd
     */
    public static String validateAndConvertDateGermanToSortFormat(@NonNull String input) //TODO UnitTests schreiben
    {
        return validateAndConvert(input, FORMAT_GER, FORMAT_SORT);
    }

    /**
     * Validates the input-format and converts it.
     * @param input format yyMMdd
     * @return format dd.MM.yyyy
     */
    public static String validateAndConvertDateSortFormatToGerman(@NonNull String input)
    {
        return validateAndConvert(input, FORMAT_SORT, FORMAT_GER);
    }

    private static String validateAndConvert(String input, String formatFrom, String formatTo)
    {
        if(input == null && input.isEmpty())
        {
            throw new IllegalArgumentException("String input is null or empty.");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatFrom)
                .withResolverStyle(ResolverStyle.STRICT);
        String result;
        try
        {
            TemporalAccessor ta = formatter.parse(input);
            formatter = DateTimeFormatter.ofPattern(formatFrom)
                    .withResolverStyle(ResolverStyle.STRICT);
            result = formatter.format(ta);
        }
        catch(DateTimeParseException e)
        {
            Log.e(DateFormatTool.class.toString(),
                    String.format("The date '%s' has an invalid date format, should be '%s'.", input, formatFrom));
            return input; //TODO Exception Handling
        }

        Log.d(DateFormatTool.class.toString(),
                String.format("Converted date '%s' to format '%s'. Result: '%s'", input, formatTo, result));
        return result;
    }
}
