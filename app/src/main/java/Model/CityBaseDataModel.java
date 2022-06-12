package Model;

import androidx.annotation.NonNull;

public class CityBaseDataModel
{
    //TODO Reihenfolge anpassen wie (vorher) im CityDataModel
    private int objectId;
    private int bl_id;
    private String bl;
    private String bez; //TODO Könnte man theoretisch auch als Ids übersetzen und speichern, damit es schneller geht
    private String gen;
    private int ewz;

    private static final String STR_KREIS = "kreis";

    public CityBaseDataModel() //TODO nicht unbedingt notwendig, aber vmtl. will Firebase den auch hier haben
    {

    }

    public CityBaseDataModel(int objectId, int bl_id, String bl, String bez, String gen, int ewz)
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
                + "City-Name: " + getCityName()
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

    public void setObjectId(int objectId)
    {
        this.objectId = objectId;
    }

    public int getBl_id()
    {
        return bl_id;
    }

    public void setBl_id(int bl_id)
    {
        this.bl_id = bl_id;
    }

    public String getBl() {
        return bl;
    }

    public void setBl(String bl) {
        this.bl = bl;
    }

    public String getBez()
    {
        return bez;
    }

    public void setBez(String bez)
    {
        this.bez = bez;
    }

    public String getGen()
    {
        return gen;
    }

    public void setGen(String gen)
    {
        this.gen = gen;
    }

    public int getEwz()
    {
        return ewz;
    }

    public void setEwz(int ewz)
    {
        this.ewz = ewz;
    }
}
