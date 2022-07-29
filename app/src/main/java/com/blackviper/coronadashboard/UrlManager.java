package com.blackviper.coronadashboard;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import Tools.FormatTool;

public class UrlManager
{
    private static final String URL_START = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis"
            + "/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?where=";
    private static final String OUT_FIELDS_BASE_DATA = "OBJECTID,BL_ID, BL, GEN,BEZ,EWZ";
    private static final String OUT_FIELDS_CORONA_DATA = "last_update,death_rate,cases,deaths,"
            + "cases_per_100k,cases_per_population,cases7_per_100k,cases7_lk,death7_lk,death7_lk,"
            + "cases7_bl_per_100k,cases7_bl,death7_bl";
    private static final String FORMAT_JSON = "json";

    //Public methods

    public static String getUrlGetAllBaseData()
    {
        return getUrlWithWhereConditionAllOutFields(getWhereConditionEmptyEncoded(),
                false, getOutFieldsBaseData());
    }

    public static String getUrlGetAllCityData()
    {
        return getUrlWithWhereConditionAllOutFields(getWhereConditionEmptyEncoded(),
                false, getOutFieldsAll());
    }

    public static String getUrlGetCityByObjectId(int objectId)
    {
        String objectIdStr = Integer.toString(objectId);
        String whereCondition = encodeUrl("OBJECTID=" + objectIdStr);
        return getUrlWithWhereConditionAllOutFields(whereCondition, false, getOutFieldsAll());
    }

    public static String getUrlFindObjectIdByCityName(String cityName)
    {
        String[] cityNameArray = FormatTool.seperateBezAndGen(cityName);
        String bez = cityNameArray[0];
        String gen = cityNameArray[1];

        String whereCondition;
        if (bez.isEmpty())
        {
            whereCondition = getWhereConditionGenEncoded(gen);
        }
        else
        {
            whereCondition = encodeUrl(String.format("BEZ='%s'AND", bez));
            whereCondition = whereCondition + getEncodedWhitespace() + getWhereConditionGenEncoded(gen);
        }
        String outfields = "&outFields=OBJECTID";
        String result = getUrlWithWhereConditionAllOutFields(whereCondition, true, outfields);
        return result;
    }

    //Internal methods

    private static String getEndDefault(boolean idsOnly)
    {
        String result = getReturnGeometry(false);
        if(idsOnly)
        {
            result = result + "&returnIdsOnly=true";
        }
        return result + getFormatOut(FORMAT_JSON);
    }

    private static String getReturnGeometry(boolean returnGeometry)
    {
        return "&returnGeometry=" + returnGeometry;
    }

    private static String getFormatOut(String format)
    {
        return "&f=" + format;
    }

    private static String getUrlWithWhereConditionAllOutFields(String whereCondition, boolean idsOnly, String outfields)
    {
        return URL_START
                + whereCondition
                + outfields
                + getEndDefault(idsOnly);
    }

    private static String getOutFieldsAll()
    {
        return getOutFieldsBaseData() + "," + OUT_FIELDS_CORONA_DATA;
    }

    private static String getOutFieldsBaseData()
    {
        return "&outFields=" + OUT_FIELDS_BASE_DATA;
    }

    private static String getWhereConditionEmptyEncoded()
    {
        return encodeUrl("1=1");
    }

    private static String getWhereConditionGenEncoded(String gen)
    {
        return encodeUrl(String.format("GEN='%s'", gen));
    }

    /**
     * Does NOT support special character like whitespaces.
     * E.g. whitespaces will be encoded to "+" instead of "%20".
     */
    private static String encodeUrl(String url)
    {
        String result = "";
        try
        {
            result = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        }
        catch(UnsupportedEncodingException e)
        {
            Log.e("UrlManager", "URL-Encoding failed: " + e.getMessage());
        }
        return result;
    }

    private static String getEncodedWhitespace()
    {
        return "%20";
    }
}
