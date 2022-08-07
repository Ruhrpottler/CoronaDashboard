package Tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.blackviper.coronadashboard.MainActivity;

public class UiUtility
{
    private final Context context;

    public UiUtility(Context context)
    {
        this.context = context;
    }

    public void showToastTextShort(String str)
    {
        showToastText(str, Toast.LENGTH_SHORT);
    }

    public void showToastTextLong(String str)
    {
        showToastText(str, Toast.LENGTH_LONG);
    }

    private void showToastText(String str, int length)
    {
        if(length != Toast.LENGTH_SHORT && length != Toast.LENGTH_LONG)
        {
            length = Toast.LENGTH_LONG;
            Log.i("IllegalArgument", String.format("'%d' is an illegal length for the toast display time.", length));
        }
        Toast.makeText(context, str, length).show();
    }
}
