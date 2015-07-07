package com.argszero.wuyun.app;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by shaoaq on 7/3/15.
 */
public class Db {
    private  SQLiteDatabase db;
    public Db(String dir) {
        db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
    }


    public String getLastId() {
        //TODO
        return null;
    }
}
