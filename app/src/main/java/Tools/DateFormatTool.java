package Tools;

import android.util.Log;

public class DateFormatTool //extends FormatTool
{

    /**
     *
     * @param input format dd.MM.yyyy
     * @return format yyMMdd
     */
    public static String convertDateToString(String input) //TODO UnitTests schreiben
    {
        if(input == null || input.isEmpty())
        {
            throw new IllegalArgumentException("String date is null or emtpy.");
        }
        input = input.trim();
        if(input.length() != 10)
        {
            throw new IllegalArgumentException("String date has an incorrect format, should be 'DD.MM.YYYY'.");
        }

        int index = input.indexOf(".");
        if(index != 2)
        {
            throw new IllegalArgumentException("Format incorrect");
        }
        String day = input.substring(0, index);
        input = input.substring(++index);
        String month = input.substring(0, --index);
        input = input.substring(++index);
        String year = input.substring(2);
        String result = year+month+day;
        if(result.length() != 6)
        {
            throw new IllegalArgumentException("String date has an incorrect format. Format should be 'DD.MM.YYYY'.");
        }
        Log.d(DateFormatTool.class.toString(), String.format("Converted date '%s' to format yyMMDD. Result: '%s'", input, result));
        return result;
    }
}
