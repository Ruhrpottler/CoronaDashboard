package Model;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import Tools.Constants;

public class BaseData implements Data
{
    private int objectId;
    private int bl_id;
    private String bl;
    private String bez; //TODO Könnte man theoretisch auch als Ids übersetzen und speichern, damit es schneller geht
    private String gen;
    private int ewz;

    private static final String STR_KREIS = "kreis";

    public BaseData() { } //TODO nicht unbedingt notwendig, aber vmtl. will Firebase den auch hier haben

    public BaseData(int objectId, int bl_id, String bl, String bez, String gen, int ewz)
    {
        this.objectId = objectId;
        this.bl_id = bl_id;
        this.bl = bl;
        this.bez = bez;
        this.gen = gen;
        this.ewz = ewz;
    }

    @NonNull
    @Override
    public String toString()
    {
        return "objectId: " + getObjectId()
                + "Bundesland-ID: " + getBl_id()
                + "Bundesland: " + getBl()
                + "Stadt: " + getCityName()
                + "Einwohnerzahl: " + getEwz();
    }

    /** @return BEZ + GEN (z.B. "Kreisfreie Stadt Dortmund", "Landkreis Recklinghausen"...)
     *          Wenn "kreis" in GEN enthalten, nur GEN zurückgeben
     *          (z.B. "Kreis Oberbergischer Kreis" -> "Oberbergischer Kreis")
     */
    public String getCityName()
    {
        if(getGen().toLowerCase().contains(STR_KREIS)) {
            return getGen();
        }
        return getBez() + " " + getGen();
    }

    public int getObjectId()
    {
        return objectId;
    }

    public final void setObjectId(int objectId)
    {
        this.objectId = objectId;
    }

    public int getBl_id()
    {
        return bl_id;
    }

    public final void setBl_id(int bl_id)
    {
        this.bl_id = bl_id;
    }

    public String getBl() {
        return bl;
    }

    public final void setBl(String bl) {
        this.bl = bl;
    }

    public String getBez()
    {
        return bez;
    }

    public final void setBez(String bez)
    {
        this.bez = bez;
    }

    public String getGen()
    {
        return gen;
    }

    public final void setGen(String gen)
    {
        this.gen = gen;
    }

    public int getEwz()
    {
        return ewz;
    }

    public final void setEwz(int ewz)
    {
        this.ewz = ewz;
    }

    @Override
    public Data createDataFromJSONAttributesGeneric(JSONObject attributes) throws JSONException
    {
        return createDataFromJSONAttributes(attributes);

    }

    public static BaseData createDataFromJSONAttributes(JSONObject attributes) throws JSONException
    {
        return new BaseData(
                attributes.getInt(Constants.STR_OBJECT_ID),
                attributes.getInt(Constants.STR_BL_ID),
                attributes.getString(Constants.STR_BL),
                attributes.getString(Constants.STR_BEZ),
                attributes.getString(Constants.STR_GEN),
                attributes.getInt(Constants.STR_EWZ)
        );
    }

}
