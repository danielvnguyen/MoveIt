package com.example.moveit.model.entries;

import java.util.Comparator;

//This class is used to sort the entries list in chronological order
public class EntryComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry o1, Entry o2) {

        long dateTimeValue1 = o1.getDateTime();
        long dateTimeValue2 = o2.getDateTime();
        if (dateTimeValue1 > dateTimeValue2) {
            return -1;
        } else if (dateTimeValue1 < dateTimeValue2) {
            return 1;
        }

        return 0;
    }
}
