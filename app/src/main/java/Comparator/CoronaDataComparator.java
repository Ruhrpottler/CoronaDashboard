package Comparator;

import java.util.Comparator;

import Model.CoronaData;

public class CoronaDataComparator implements Comparator<CoronaData>
{
    @Override
    public int compare(CoronaData a, CoronaData b)
    {
        if(a == null && b == null)
        {
            return 0;
        }
        if(a == null && b != null)
        {
            return 1;
        }
        if(a != null && b == null)
        {
            return -1;
        }

        return Integer.compare(a.getObjectId(), b.getObjectId());
    }
}
