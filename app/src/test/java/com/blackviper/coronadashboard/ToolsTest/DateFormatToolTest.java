package com.blackviper.coronadashboard.ToolsTest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import Tools.DateFormatTool;

public class DateFormatToolTest
{
    private static final String FORMAT_GER = DateFormatTool.getFormatGerman();
    private static final String FORMAT_SORT = DateFormatTool.getFormatSort();

    //Tests for FORMAT_GER -> FORMAT_SORT

    @Test
    public void germanToSortTest()
    {
        String date = "19.10.2022";
        String expectedResult = "221019";
        assertEquals(expectedResult, DateFormatTool.germanToSort(date));

        date = "01.01.2018";
        expectedResult = "180101";
        assertEquals(expectedResult, DateFormatTool.germanToSort(date));

        date = "31.12.2021";
        expectedResult = "211231";
        assertEquals(expectedResult, DateFormatTool.germanToSort(date));
    }

    @Test(expected = IllegalArgumentException.class)
    public void germanToSortYearExceptionTest()
    {
        String date = "19.10.22";
        DateFormatTool.germanToSort(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void germanToSortMonthExceptionTest()
    {
        String date = "19.7.2022";
        DateFormatTool.germanToSort(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void germanToSortDayExceptionTest()
    {
        String date = "5.07.2022";
        DateFormatTool.germanToSort(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void germanToSortLettersExceptionTest()
    {
        String date = "05.07.202a";
        DateFormatTool.germanToSort(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void germanToSortEmptyExceptionTest()
    {
        String date = "";
        DateFormatTool.germanToSort(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void germanToSortWhitespaceExceptionTest()
    {
        String date = " ";
        DateFormatTool.germanToSort(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void germanToSortNullExceptionTest()
    {
        DateFormatTool.germanToSort(null);
    }

    //Tests for FORMAT_SORT -> FORMAT_GER

    @Test
    public void sortToGermanTest()
    {
        String date = "221019";
        String expectedResult = "19.10.2022";
        assertEquals(expectedResult, DateFormatTool.sortToGerman(date));

        date = "180101";
        expectedResult = "01.01.2018";

        assertEquals(expectedResult, DateFormatTool.sortToGerman(date));

        date = "211231";
        expectedResult = "31.12.2021";

        assertEquals(expectedResult, DateFormatTool.sortToGerman(date));
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortToGermanYearExceptionTest()
    {
        String date = "20221019";
        DateFormatTool.sortToGerman(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortToGermanMonthExceptionTest()
    {
        String date = "22719";
        DateFormatTool.sortToGerman(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortToGermanDayExceptionTest()
    {
        String date = "22075";
        DateFormatTool.sortToGerman(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortToGermanLettersExceptionTest()
    {
        String date = "2a0705";
        DateFormatTool.sortToGerman(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortToGermanEmptyExceptionTest()
    {
        String date = "";
        DateFormatTool.sortToGerman(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortToGermanWhitespaceExceptionTest()
    {
        String date = " ";
        DateFormatTool.sortToGerman(date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortToGermanNullExceptionTest()
    {
        DateFormatTool.sortToGerman(null);
    }
}
