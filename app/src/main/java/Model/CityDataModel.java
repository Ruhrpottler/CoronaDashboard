package Model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CityDataModel {

    //Allgemein
    int objectId;
    String bez;
    String county;
    int ewz;
    int bl_id;
    String bl;
    String last_update; //Stand

    //Corona-Daten City
    double death_rate;
    int cases;
    int deaths;
    double cases_per_100k;
    double cases_per_population;
    double cases7_per_100k;
    String cases7_per_100k_txt;
    int cases7_lk;
    int death7_lk;

    //Corona-Daten Bundesland
    double cases7_bl_per_100k;
    int cases7_bl;
    int death7_bl;

    /*Wenn weitere hinzugefügt werden:
     * 1. Attribut hinzufügen
     * 2. Setter- (final!!!) und Getter hinzufügen
     * 3. Zum Konstruktor hinzufügen
     * 4. zur fillMethod im DataService hinzufügen (nicht vergessen!!)
     */

    /**
     *
     * @param objectId cityId
     * @param bez
     * @param county "LK Recklinghausen"
     * @param ewz Einwohnerzahl
     * @param bl_id Id des Bundeslandes
     * @param bl Name des Bundeslandes
     * @param last_update Stand (Datum und Uhrzeit)
     * @param death_rate Sterberate
     * @param cases
     * @param deaths
     * @param cases_per_100k
     * @param cases_per_population
     * @param cases7_per_100k 7-Tage-Inzidenzwert pro 100k Einwohner
     * @param cases7_per_100k_txt
     * @param cases7_lk
     * @param death7_lk
     * @param cases7_bl_per_100k
     * @param cases7_bl
     * @param death7_bl
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
    public CityDataModel(int objectId, String bez, String county, int ewz, int bl_id, String bl, String last_update,
                         double death_rate, int cases, int deaths, double cases_per_100k, double cases_per_population,
                         double cases7_per_100k, String cases7_per_100k_txt, int cases7_lk, int death7_lk,
                         double cases7_bl_per_100k, int cases7_bl, int death7_bl)
    {
        setObjectId(objectId);
        setBez(bez);
        setCounty(county);
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
        setCases7_per_100k_txt(cases7_per_100k_txt);
        setCases7_lk(cases7_lk);
        setDeath7_lk(death7_lk);
        setCases7_bl_per_100k(cases7_bl_per_100k);
        setCases7_bl(cases7_bl);
        setDeath7_bl(death7_bl);
    }

    @Override
    public String toString() {
        return "CityDataModel{" +
                ", county='" + county + '\'' +
                ", 7-Tage-Inzidenzwert=" + cases7_per_100k +
                '}';
    }

    /** Rundet "normal (ab 5 auf, vorher ab)
     * BigDecimal Round Modus Doku: https://docs.oracle.com/javase/7/docs/api/java/math/RoundingMode.html
     * @param value
     * @param stelle Diese Zahl gibt an, wie viele Zahlen (abgesehen von 0) hinter dem Komma stehen bleiben.
     *               Stelle 2 heißt also, es wird auf die 3. Stelle geschaut und anhand dessen entschieden.
     *               Bsp: 3.4537 wird zu 3.45
     */
    public double roundDouble(double value, int stelle) //TODO static oder nicht? In Eclipse eig schon
    {
        if (stelle < 0) throw new IllegalArgumentException(String.format("Runden auf die Stelle '%d' ist nicht möglich.", stelle));

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(stelle, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /*
     * Alle setter müssen final sein, damit sie nicht überschrieben werden können!
     */

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

    public String getCounty() {
        return county;
    }

    public final void setCounty(String county) {
        this.county = county;
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
        return last_update;
    }

    public final void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public double getDeath_rate() {
        return death_rate;
    }

    public final void setDeath_rate(double death_rate) {
        this.death_rate = roundDouble(death_rate, 2);
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
        this.cases_per_100k = roundDouble(cases_per_100k, 2);
    }

    public double getCases_per_population() {
        return cases_per_population;
    }

    public final void setCases_per_population(double cases_per_population) {
        this.cases_per_population = roundDouble(cases_per_population, 2);
    }

    public double getCases7_per_100k() {
        return cases7_per_100k;
    }

    public final void setCases7_per_100k(double cases7_per_100k) {
        this.cases7_per_100k = roundDouble(cases7_per_100k, 1);
    }

    public String getCases7_per_100k_txt() {
        return cases7_per_100k_txt;
    }

    public final void setCases7_per_100k_txt(String cases7_per_100k_txt) {
        this.cases7_per_100k_txt = cases7_per_100k_txt;
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
        this.cases7_bl_per_100k = roundDouble(cases7_bl_per_100k, 1);
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
