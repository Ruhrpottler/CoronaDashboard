package Model;

public class CityStammdatenModel
{
    int objectId;
    int bl_id;
    String bez; //TODO Könnte man theoretisch auch als Ids übersetzen und speichern, damit es schneller geht
    String gen;
    int ewz;

    public CityStammdatenModel(int objectId, int bl_id, String bez, String gen, int ewz)
    {
        this.objectId = objectId;
        this.bl_id = bl_id;
        this.bez = bez;
        this.gen = gen;
        this.ewz = ewz;
    }

    @Override
    public String toString()
    {
        return "objectId: " + getObjectId()
                + "Bundesland-ID: " + getBl_id()
                + "City-Name: " + getCityName()
                + "Einwohnerzahl: " + getEwz();
    }

    /** BEZ + GEN
     *  z.B. "Kreisfreie Stadt Dortmund", "Landkreis Recklinghausen"...
     *  Achtung: "Kreis Oberbergischer Kreis" klingt unschön, muss man rausfiltern.
     * @return
     */
    public String getCityName() //TODO auslagern als Tool, damit der Code nicht doppelt vorhanden ist (CityDataModel)
    {
        if(getGen().toLowerCase().contains("kreis")) {
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
