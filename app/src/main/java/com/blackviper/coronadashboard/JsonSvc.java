package com.blackviper.coronadashboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Model.BaseData;
import Model.City;
import Model.CoronaData;
import Model.Data;

public class JsonSvc
{

    protected static JSONArray getJSONFeaturesFromResponse(JSONObject response) throws JSONException
    {
        return response.getJSONArray("features");
    }

    protected static JSONObject getJSONAttributesFromFeatures(JSONArray features, int index) throws JSONException
    {
        return features.getJSONObject(index).getJSONObject("attributes");
    }

    protected static City createAndFillCity(JSONObject attributes) throws JSONException
    {
        BaseData baseData = BaseData.createDataFromJSONAttributes(attributes);
        CoronaData coronaData = CoronaData.createDataFromJSONAttributes(attributes);
        return new City(baseData, coronaData);
    }

    protected static List<BaseData> getBaseDataListFromResponse(JSONObject response) throws JSONException
    {
        return getListFromResponse(response, new BaseData());
    }

    protected static List<CoronaData> getCoronaDataListFromResponse(JSONObject response) throws JSONException
    {
        return getListFromResponse(response, new CoronaData());
    }

    protected static List<City> getCityListFromResponse(JSONObject response) throws JSONException
    {
        List<City> list = new ArrayList<>();
        JSONArray features = getJSONFeaturesFromResponse(response);
        JSONObject attributes;
        City city;
        for(int i = 0; i < features.length(); i++)
        {
            attributes = getJSONAttributesFromFeatures(features, i);
            city = createAndFillCity(attributes);
            if(city != null)
            {
                list.add(city);
            }
        }
        return list;
    }

    private static <T extends Data> List<T> getListFromResponse(JSONObject response, T t) throws JSONException
    {
        JSONArray features = getJSONFeaturesFromResponse(response);
        JSONObject attributes;
        List<T> list = new ArrayList<>();

        for (int i = 0; i < features.length(); i++)
        {
            attributes = getJSONAttributesFromFeatures(features, i);
            list.add((T) t.createDataFromJSONAttributesGeneric(attributes));
        }
        return list;
    }
}
