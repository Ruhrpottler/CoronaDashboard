package Model;

import androidx.annotation.NonNull;

import java.util.Locale;

import Tools.FormatTool;

public class City {

    private int objectId; //TODO Wirklich in allen 3 Model-Klassen notwendig?
    private BaseData baseData;
    private CoronaData coronaData;

    public City() { }

    public City(BaseData baseData, CoronaData coronaData)
    {
        this.baseData = baseData;
        this.objectId = baseData.getObjectId();
        this.coronaData = coronaData;
    }

    @NonNull
    @Override
    public String toString()
    {
        return String.format(Locale.GERMAN,
                "Daten für %s,\n" +
                        "Stand %s:\n" +
                        "Bundesland: %s\n" +
                        "7-Tage-Inzidenz: %s\n" +
                        "Einwohnerzahl: %s\n" +
                        "Bestätigte Fälle: %s\n" +
                        "Bestätigte Fälle/100k EW: %s\n" +
                        "Betroffenenrate: %.2f%%\n" +
                        "Todesfälle: %s\n" +
                        "Sterberate: %.2f%%",
                getBaseData().getCityName(), getCoronaData().getLast_update(), getBaseData().getBl(), getCoronaData().getCases7_per_100k_txt(),
                FormatTool.intToString(getBaseData().getEwz()), FormatTool.intToString(getCoronaData().getCases()),
                FormatTool.doubleToString(FormatTool.roundDouble(getCoronaData().getCases_per_100k(), 2), 2),
                getCoronaData().getCases_per_population() , FormatTool.intToString(getCoronaData().getDeaths()), getCoronaData().getDeath_rate()
        );
    }

    public int getObjectId()
    {
        return objectId;
    }

    public void setObjectId(int objectId)
    {
        this.objectId = objectId;
    }

    public BaseData getBaseData()
    {
        return baseData;
    }

    public void setBaseData(BaseData baseData)
    {
        this.baseData = baseData;
    }

    public CoronaData getCoronaData()
    {
        return coronaData;
    }

    public void setCoronaData(CoronaData coronaData)
    {
        this.coronaData = coronaData;
    }

}
