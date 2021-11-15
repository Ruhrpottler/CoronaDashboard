package Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import Tools.FormatTool;

public class CityDataModel {

    //Allgemein
    private int objectId;
    private String bez;
    private String gen;
    private int ewz;
    private int bl_id;
    private String bl;
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
     * Standardkonstruktor. Notwendig für Firebase, um Java-Objekte zu speichern
     * siehe https://firebase.google.com/docs/database/android/read-and-write
     */
    public CityDataModel()
    {

    }

    /**
     *
     * @param objectId cityId
     * @param bez "Landkreis", "Kreisfreie Stadt" oder "Bezirk" (Berliner Stadtteile)
     * @param gen cityName
     * @param ewz Einwohnerzahl
     * @param bl_id Id des Bundeslandes
     * @param bl Name des Bundeslandes
     * @param last_update Stand (Datum und Uhrzeit)
     * @param death_rate Sterberate
     * @param cases Bestätigte Infektionen (gesamt)
     * @param deaths Todesfälle (gesamt)
     * @param cases_per_100k Fälle pro 100k Einwohner
     * @param cases_per_population Betroffenenrate %
     * @param cases7_per_100k 7-Tage-Inzidenzwert pro 100k Einwohner
     * @param cases7_lk Bestätigte Fälle in den letzten 7 Tagen
     * @param death7_lk Todesfälle in den letzten 7 Tagen
     * @param cases7_bl_per_100k
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
     * Es darf keinen (leeren) Standardkonstruktor geben, damit der Entwickler gezwungen ist alle Werte zu setzen.
     *
     */
    public CityDataModel(int objectId, String bez, String gen, int ewz, int bl_id, String bl, String last_update,
                         double death_rate, int cases, int deaths, double cases_per_100k, double cases_per_population,
                         double cases7_per_100k, int cases7_lk, int death7_lk,
                         double cases7_bl_per_100k, int cases7_bl, int death7_bl)
    {
        setObjectId(objectId);
        setBez(bez);
        setGen(gen);
        setEwz(ewz);
        setBl_id(bl_id);
        setBl(bl);
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

    @Override
    public String toString() {
        return String.format("Daten für %s,\n" +
                        "Stand %s:\n" +
                "Bundesland: %s\n" +
                "7-Tage-Inzidenz: %s\n" +
                "Einwohnerzahl: %s\n" +
                "Bestätigte Fälle: %s\n" +
                "Bestätigte Fälle/100k EW: %s\n" +
                "Betroffenenrate: %.2f%%\n" +
                "Todesfälle: %s\n" +
                "Sterberate: %.2f%%",
                getCityName(), getLast_update(), getBl(), getCases7_per_100k_txt(), FormatTool.intToString(ewz), FormatTool.intToString(cases),
                FormatTool.doubleToString(FormatTool.roundDouble(getCases_per_100k(), 2), 2), getCases_per_population() , FormatTool.intToString(deaths), getDeath_rate()
        );
    }

    /*
     * Achtung: Alle setter müssen final sein (!), damit sie nicht überschrieben werden können!
     */

    //TODO Bezirk und Stadtkreis ?!
    /** BEZ + GEN
     *  z.B. "Kreisfreie Stadt Dortmund", "Landkreis Recklinghausen", "Oberbergischer Kreis"...
     * @return
     */
    public String getCityName()
    {
        if(getGen().toLowerCase().contains("kreis"))
            return getGen();

        return getBez() + " " + getGen();
    }

    public int getObjectId() {
        return objectId;
    }

    public final void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public String getBez() {
        return bez;
    }

    public final void setBez(String bez) {
        this.bez = bez;
    }

    public String getGen() {
        return gen;
    }

    public final void setGen(String gen) {
        this.gen = gen;
    }

    public int getEwz() {
        return ewz;
    }

    public final void setEwz(int ewz) {
        this.ewz = ewz;
    }

    public int getBl_id() {
        return bl_id;
    }

    public final void setBl_id(int bl_id) {
        this.bl_id = bl_id;
    }

    public String getBl() {
        return bl;
    }

    public final void setBl(String bl) {
        this.bl = bl;
    }

    public String getLast_update() {
        String stand = last_update;
        if(stand.contains(", 00:00 Uhr"))
            return stand.replace(", 00:00 Uhr", "").trim();
        return stand;
    }

    public final void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public double getDeath_rate() {
        return death_rate;
    }

    public final void setDeath_rate(double death_rate) {
        this.death_rate = FormatTool.roundDouble(death_rate, 2);
    }

    public int getCases() {
        return cases;
    }

    public final void setCases(int cases) {
        this.cases = cases;
    }

    public int getDeaths() {
        return deaths;
    }

    public final void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public double getCases_per_100k() {
        return cases_per_100k;
    }

    public final void setCases_per_100k(double cases_per_100k) {
        this.cases_per_100k = FormatTool.roundDouble(cases_per_100k, 2);
    }

    public double getCases_per_population() {
        return cases_per_population;
    }

    public final void setCases_per_population(double cases_per_population) {
        this.cases_per_population = FormatTool.roundDouble(cases_per_population, 2);
    }

    public double getCases7_per_100k() {
        return cases7_per_100k;
    }

    public String getCases7_per_100k_txt()
    {
        return FormatTool.doubleToString(getCases7_per_100k(), 1);
    }

    public final void setCases7_per_100k(double cases7_per_100k) {
        this.cases7_per_100k = FormatTool.roundDouble(cases7_per_100k, 1);
    }

    public int getCases7_lk() {
        return cases7_lk;
    }

    public final void setCases7_lk(int cases7_lk) {
        this.cases7_lk = cases7_lk;
    }

    public int getDeath7_lk() {
        return death7_lk;
    }

    public final void setDeath7_lk(int death7_lk) {
        this.death7_lk = death7_lk;
    }

    public double getCases7_bl_per_100k() {
        return cases7_bl_per_100k;
    }

    public final void setCases7_bl_per_100k(double cases7_bl_per_100k) {
        this.cases7_bl_per_100k = FormatTool.roundDouble(cases7_bl_per_100k, 1);
    }

    public int getCases7_bl() {
        return cases7_bl;
    }

    public final void setCases7_bl(int cases7_bl) {
        this.cases7_bl = cases7_bl;
    }

    public int getDeath7_bl() {
        return death7_bl;
    }

    public final void setDeath7_bl(int death7_bl) {
        this.death7_bl = death7_bl;
    }
}
