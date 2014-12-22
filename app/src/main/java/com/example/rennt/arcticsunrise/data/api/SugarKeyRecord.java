package com.example.rennt.arcticsunrise.data.api;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by rennt on 12/21/14.
 */
public class SugarKeyRecord<T> extends SugarRecord<T> {
    private long _relatedKey;

    /**
     * Save the Record with the given key. Key preferably being the Id of a parent model.
     * @param key
     */
    public void saveWithKey(long key){
        _relatedKey = key;
        this.save();
    }

    public static <T extends SugarKeyRecord> List<T> findByKey(Class<T> type, long key){
        return find(type, "_RELATED_KEY = ?", Long.toString(key));
    }
}
