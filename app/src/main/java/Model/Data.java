package Model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * To avoid duplicate code in the {@link com.blackviper.coronadashboard.DataSvc},
 * this interface allows a generalization of {@link BaseData} and {@link CoronaData}
 * and makes the generic method possible.
 */
public interface Data
{
    public <T extends Data> Data createDataFromJSONAttributesGeneric(JSONObject attributes) throws JSONException;
}
