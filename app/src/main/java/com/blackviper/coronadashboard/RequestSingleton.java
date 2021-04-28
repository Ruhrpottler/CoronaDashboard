package com.blackviper.coronadashboard;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Documentation: https://developer.android.com/training/volley/requestqueue#singleton
 * Aufruf wie folgt: RequestSingleton.getInstance(MainActivity.this).addToRequestQueue(request); request ist ein JSONObjectRequest
 */
public class RequestSingleton
{
    private static RequestSingleton instance; //Typ ist die Instanz der Klasse
    private RequestQueue requestQueue;
    private static Context ctx;

    private RequestSingleton(Context context)
    {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    /**
     * @param applicationContext Sollte der ApplicationContext sein, damit die Instanz nicht recreated wird,
     *                wenn die Activity neu erstellt wird (z.B. wenn Smartphone gedreht wird).
     * @return Wenn die Instanz existiert, wird diese zurückgegeben. Wenn nicht, wird sie erstellt.
     */
    public static synchronized RequestSingleton getInstance(Context applicationContext) {
        if (instance == null)
        {
            instance = new RequestSingleton(applicationContext);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null)
        {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req)
    {
        getRequestQueue().add(req);
    }


}
