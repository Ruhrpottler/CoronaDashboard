package Model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import Comparator.LastUpdateComparator;

import Tools.FormatTool;

public class City {

    private int objectId; //TODO Wirklich in allen 3 Model-Klassen notwendig?
    private BaseData baseData;
    private List<CoronaData> coronaData;

    public City() { }

    public City(BaseData baseData, CoronaData coronaData)
    {
        setBaseData(baseData);
        setObjectId(baseData.getObjectId());
        List<CoronaData> list = new ArrayList<>();
        if(coronaData != null)
        {
            list.add(coronaData);
        }
        setCoronaDataList(list);
    }

    public City(BaseData baseData, List<CoronaData> coronaData)
    {
        setBaseData(baseData);
        setObjectId(baseData.getObjectId());
        setCoronaDataList(coronaData);
    }

    @NonNull
    @Override
    public String toString()
    {
        CoronaData newestData = getNewestCoronaData();
        if(newestData == null)
        {
            return "Data empty.";
        }
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
                getBaseData().getCityName(), newestData.getLast_update(), getBaseData().getBl(), newestData.getCases7_per_100k_txt(),
                FormatTool.intToString(getBaseData().getEwz()), FormatTool.intToString(newestData.getCases()),
                FormatTool.doubleToString(FormatTool.roundDouble(newestData.getCases_per_100k(), 2), 2),
                newestData.getCases_per_population() , FormatTool.intToString(newestData.getDeaths()), newestData.getDeath_rate()
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

    public List<CoronaData> getCoronaDataList()
    {
        if(coronaData == null)
        {
            return new ArrayList<CoronaData>();
        }
        return coronaData;
    }

    /**
     * List will be sorted. Newest will be the first element.
     * @param coronaList
     */
    public void setCoronaDataList(List<CoronaData> coronaList)
    {
        if(coronaList == null || coronaList.isEmpty())
        {
            return;
        }
        Comparator<CoronaData> cmp = new LastUpdateComparator().reversed();
        coronaList.sort(cmp);
        this.coronaData = coronaList;
    }

    public CoronaData getNewestCoronaData()
    {
        List<CoronaData> list = getCoronaDataList();
        if(list == null)
        {
            return null;
        }
        return list.get(0);
    }
}
