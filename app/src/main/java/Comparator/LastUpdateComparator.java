package Comparator;

import java.util.Comparator;

import Model.CoronaData;
import Tools.DateFormatTool;

public class LastUpdateComparator extends CoronaDataComparator implements Comparator<CoronaData>
{
    /** Sort by last_update
     * First object will be the oldest, last the newest.
     * Use comparator.reversed() for the reversed order.
     */
    @Override
    public int compare(CoronaData a, CoronaData b)
    {
        if(a == null || b == null)
        {
            return super.compare(a, b);
        }

        String dateA = DateFormatTool.germanToSort(a.getLast_update());
        String dateB = DateFormatTool.germanToSort(b.getLast_update());

        return dateA.compareTo(dateB);
    }
}
