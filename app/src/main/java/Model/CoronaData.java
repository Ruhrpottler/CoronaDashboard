package Model;

import Tools.FormatTool;

public class CoronaData
{
    private int objectId; //Primärschlüssel
    //Allgemein
    private String last_update;
    //TODO Datentyp Date nutzen
    //TODO see https://firebase.google.com/docs/reference/android/com/google/firebase/Timestamp
    //TODO see eclipse

    //Corona-Daten City
    private double death_rate;
    private int cases;
    private int deaths;
    private double cases_per_100k;
    private double cases_per_population;
    private double cases7_per_100k;
    private int cases7_lk;
    private int death7_lk;

    //Corona-Daten Bundesland
    private double cases7_bl_per_100k;
    private int cases7_bl;
    private int death7_bl;

    /*Wenn weitere hinzugefügt werden:
     * 1. Attribut hinzufügen
     * 2. Setter- (final!!!) und Getter hinzufügen
     * 3. Zum Konstruktor hinzufügen
     * 4. zur fillMethod im DataService hinzufügen (nicht vergessen!!)
     * 5. Zur URL/Query hinzufügen
     */

    /**
     * Default constructor for firebase to save Java objects
     * see: https://firebase.google.com/docs/database/android/read-and-write
     */
    public CoronaData()
    {

    }

    /**
     * @param objectId Eindeutige ID der Kommune
     * @param last_update Stand (Datum und Uhrzeit)
     * @param death_rate Sterberate
     * @param cases Bestätigte Infektionen (gesamt)
     * @param deaths Todesfälle (gesamt)
     * @param cases_per_100k Fälle pro 100k Einwohner
     * @param cases_per_population Betroffenenrate %
     * @param cases7_per_100k 7-Tage-Inzidenzwert pro 100k Einwohner
     * @param cases7_lk Bestätigte Fälle in den letzten 7 Tagen
     * @param death7_lk Todesfälle in den letzten 7 Tagen
     * @param cases7_bl_per_100k Bestätigte Fälle der letzten 7 Tage im Bundesland pro 100k Einwohner
     * @param cases7_bl Bestätigte Fälle der letzten 7 Tage im Bundesland
     * @param death7_bl Todesfälle in den letzten 7 Tagen im Bundesland
     *
     * Im Konstruktor werden die setter verwendet (statt this.doubleValue=value), damit das,
     * was in den settern implementiert wird (Zahlen entsprechend runden) auch korrekt
     * angewendet wird und ich keinen doppelten/unterschiedlichen Code bekomme.
     * public-Methoden im Konstruktor aufrufen ist aber schlecht, weil man diese in Unterklassen
     * überschreiben kann und diese dann ggf. aufgerufen wird (nur diese).
     * --> Deswegen alle setter auf final setzen, damit können sie nicht überschrieben werden.
     *
     */
    public CoronaData(int objectId, String last_update,
                      double death_rate, int cases, int deaths, double cases_per_100k,
                      double cases_per_population, double cases7_per_100k, int cases7_lk,
                      int death7_lk, double cases7_bl_per_100k, int cases7_bl, int death7_bl)
    {
        setObjectId(objectId);
        setLast_update(last_update);
        setDeath_rate(death_rate);
        setCases(cases);
        setDeaths(deaths);
        setCases_per_100k(cases_per_100k);
        setCases_per_population(cases_per_population);
        setCases7_per_100k(cases7_per_100k);
        setCases7_per_100k(cases7_per_100k);
        setCases7_lk(cases7_lk);
        setDeath7_lk(death7_lk);
        setCases7_bl_per_100k(cases7_bl_per_100k);
        setCases7_bl(cases7_bl);
        setDeath7_bl(death7_bl);
    }



    /*
     * Achtung: Alle setter müssen final sein (!), damit sie nicht überschrieben werden können!
     */

    public int getObjectId()
    {
        return objectId;
    }

    public void setObjectId(int objectId)
    {
        this.objectId = objectId;
    }

    public String getLast_update()
    {
        String stand = last_update;
        if(stand.contains(", 00:00 Uhr"))
            return stand.replace(", 00:00 Uhr", "").trim();
        return stand;
    }

    public final void setLast_update(String last_update)
    {
        this.last_update = last_update;
    }

    public double getDeath_rate()
    {
        return death_rate;
    }

    public final void setDeath_rate(double death_rate)
    {
        this.death_rate = FormatTool.roundDouble(death_rate, 2);
    }

    public int getCases()
    {
        return cases;
    }

    public final void setCases(int cases)
    {
        this.cases = cases;
    }

    public int getDeaths()
    {
        return deaths;
    }

    public final void setDeaths(int deaths)
    {
        this.deaths = deaths;
    }

    public double getCases_per_100k()
    {
        return cases_per_100k;
    }

    public final void setCases_per_100k(double cases_per_100k)
    {
        this.cases_per_100k = FormatTool.roundDouble(cases_per_100k, 2);
    }

    public double getCases_per_population()
    {
        return cases_per_population;
    }

    public final void setCases_per_population(double cases_per_population)
    {
        this.cases_per_population = FormatTool.roundDouble(cases_per_population, 2);
    }

    public double getCases7_per_100k()
    {
        return cases7_per_100k;
    }

    public final void setCases7_per_100k(double cases7_per_100k)
    {
        this.cases7_per_100k = FormatTool.roundDouble(cases7_per_100k, 1);
    }

    public String getCases7_per_100k_txt()
    {
        return FormatTool.doubleToString(getCases7_per_100k(), 1);
    }

    public int getCases7_lk()
    {
        return cases7_lk;
    }

    public final void setCases7_lk(int cases7_lk)
    {
        this.cases7_lk = cases7_lk;
    }

    public int getDeath7_lk()
    {
        return death7_lk;
    }

    public final void setDeath7_lk(int death7_lk)
    {
        this.death7_lk = death7_lk;
    }

    public double getCases7_bl_per_100k()
    {
        return cases7_bl_per_100k;
    }

    public final void setCases7_bl_per_100k(double cases7_bl_per_100k)
    {
        this.cases7_bl_per_100k = FormatTool.roundDouble(cases7_bl_per_100k, 1);
    }

    public int getCases7_bl()
    {
        return cases7_bl;
    }

    public final void setCases7_bl(int cases7_bl)
    {
        this.cases7_bl = cases7_bl;
    }

    public int getDeath7_bl()
    {
        return death7_bl;
    }

    public final void setDeath7_bl(int death7_bl)
    {
        this.death7_bl = death7_bl;
    }
}
