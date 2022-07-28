package com.blackviper.coronadashboard;

import Tools.FormatTool;

public class UrlManager //TODO implement
{
//    private static final String domain = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/"
//            + "RKI_Landkreisdaten/FeatureServer/0/query?where=";
//    private static final String outfields = "outFields=OBJECTID,BL_ID, BL, GEN,BEZ,EWZ";


    public static String getUrlSearchObjectIdByCityName(String cityName)
    {
        String[] cityNameArray = FormatTool.seperateBezAndGen(cityName);
        String bez = cityNameArray[0];
        String gen = cityNameArray[1];

        String whereCondition;
        if (bez.isEmpty())
        {
            whereCondition = "GEN%20%3D%20'" + gen + "'";
        }
        else
        {
            whereCondition = "BEZ%20%3D%20'" + bez + "'%20AND%20GEN%20%3D%20'" + gen + "'";
        }

        String url = "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/"
                + "RKI_Landkreisdaten/FeatureServer/0/query?where="
                + whereCondition
                + "&outFields=OBJECTID&returnGeometry=false&returnIdsOnly=true&outSR=&f=json";
        return url;
    }
}
