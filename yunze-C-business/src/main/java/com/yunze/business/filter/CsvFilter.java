package com.yunze.business.filter;

import com.opencsv.bean.CsvToBeanFilter;

public class CsvFilter implements CsvToBeanFilter {
    @Override
    public boolean allowLine(String[] strings) {
        if(strings[0].startsWith("ICCID")){
            return false;
        }
        return true;
    }
}
