package com.app.waleent.geodo;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by wale on 10/30/17.
 */

class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp){
        return timestamp == null ? null : new Date(timestamp);
    }
    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
