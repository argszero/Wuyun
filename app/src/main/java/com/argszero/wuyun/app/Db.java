package com.argszero.wuyun.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shaoaq on 7/3/15.
 */
public class Db {
    private String dir;
    private static Db instance = new Db();

    public void saveHtml(String dirName, String file, String html) throws IOException {
        File idDir = new File(dir, dirName);
        idDir.mkdirs();
        FileUtils.write(new File(idDir, file + ".org"), html);

        Pattern imgPattern = Pattern.compile("img src=\"(http://static.wooyun.org[^\"]*)\"[^>]*>");
        Matcher matcher = imgPattern.matcher(html);
        while (matcher.find()) {
            File newImgFile = new File(idDir, Double.toString(Math.random()) + ".png");
            String imgurl = matcher.group(1);
            FileUtils.copyURLToFile(new URL(imgurl), newImgFile);
            html = html.replace(matcher.group(0), "img src=\"" + newImgFile.getName() + "\" style=\"width:100%;height:auto;\" />");
            html = html.replace(matcher.group(1), newImgFile.getName());
        }
        html = html.replace("margin:0 0 0 580px;", "margin:0 0 0 0px;");
        html = html.replace("<hr align=\"center\"/>","<br><hr align=\"center\"/>");

        Document doc = Jsoup.parse(html);
        Elements content = doc.select("div.content");
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html>\n" +
                "<html lang=\"zh-cn\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"initial-scale=1, maximum-scale=1,minimum-scale=1\">\n" +
                "    <title></title>\n" +
                "    <style>\n" +
                "        body{\n" +
                "            background-color:black;\n" +
                "            color:#eee;\n" +
                "            font-size:0.9em;\n" +
                "        }\n" +
                "        a{\n" +
                "            color:lightblue;\n" +
                "        }\n" +
                "        h2,h3{\n" +
                "            font-size:0.9em;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n");
        sb.append(content);
        sb.append("</body>\n" +
                "</html>");
        String newhtml = sb.toString().replaceAll("<br>(\\s<br>)+", "<br>");
        FileUtils.write(new File(idDir, file), newhtml);
    }

    public String readHtml(String dirName, String file) throws IOException {
        File idDir = new File(dir, dirName);
        idDir.mkdirs();
        return FileUtils.readFileToString(new File(idDir, file));
    }

    public String getBaseUrl(String dirName) throws MalformedURLException {
        File idDir = new File(dir, dirName);
        return idDir.toURI().toURL().toString();

    }


    public enum Status {_public, _submit, _confirm, _alarm, _unclaim}

    private final static String initSql = "" +
//            "drop table article;" +
            "CREATE TABLE IF NOT EXISTS article(" +
            "   id integer primary key autoincrement," +
            "   name varchar(100)," +
            "   status varchar(100)," +
            "   title varchar(200)," +
            "   status_up_time varchar(100)," +
            "   author varchar(100)," +
            "   rank integer" +
            "   " +
            ");";

    private Db() {
    }

    public synchronized void init(String dir) {
        if (this.dir == null) {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
            db.execSQL(initSql);
            db.close();
        }
        this.dir = dir;
    }

    public static Db get() {
        return instance;
    }


    public String getLastName(Status status) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
        try {
            Cursor cursor = db.rawQuery("select name " +
                    "   from article " +
                    "   where status = ? " +
                    "   order by id desc " +
                    "   limit 1", new String[]{status.toString()});
            if (cursor.getCount() != 0) {
                cursor.moveToNext();
                String name = cursor.getString(0);
                return name;
            }
            cursor.close();
        } finally {
            db.close();
        }

        return null;
    }

    public long save(String name, Status status, String title, String author, String upTime, String rank) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
        try {
            Cursor cursor = db.rawQuery("select id " +
                    "   from article " +
                    "   where status = ? " +
                    "   and name = ? " +
                    "", new String[]{status.toString(), name});
            long id;
            if (cursor.moveToNext()) {
                id = cursor.getInt(-1);
            } else {
                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("status", status.toString());
                values.put("title", title);
                values.put("author", author);
                values.put("status_up_time", upTime);
                values.put("rank", rank);
                id = db.insert("article", null, values);
            }
            return id;
        } finally {
            db.close();
        }

    }

    public JSONArray list(Status status) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
        try {
            JSONArray result = new JSONArray();
            Cursor cursor = db.rawQuery("select * " +
                            "   from article " +
                            "   where status = ? " +
                            "   order by id desc " +
                            "   ",
                    new String[]{status.toString()});
            while (cursor.moveToNext()) {
                try {
                    JSONObject object = getArticle(cursor);
                    result.put(object);
                } catch (JSONException e) {
                    Log.e("abc", "list error", e);
                }
            }
            return result;
        } finally {
            db.close();
        }
    }

    public JSONObject getArticle(int id) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
        try {
            JSONObject result = new JSONObject();
            Cursor cursor = db.rawQuery("select * " +
                            "   from article " +
                            "   where id = ? " +
                            "   ",
                    new String[]{id + ""});
            if (cursor.moveToNext()) {
                try {
                    result = getArticle(cursor);
                } catch (JSONException e) {
                    Log.e("abc", "get error", e);
                }
            }
            return result;
        } finally {
            db.close();
        }
    }


    private JSONObject getArticle(Cursor cursor) throws JSONException {
        int id = cursor.getColumnIndex("id");
        int name = cursor.getColumnIndex("name");
        int title = cursor.getColumnIndex("title");
        int author = cursor.getColumnIndex("author");
        int upTime = cursor.getColumnIndex("status_up_time");
        int rank = cursor.getColumnIndex("rank");

        JSONObject object = new JSONObject();
        object.put("id", cursor.getInt(id));
        object.put("name", cursor.getString(name));
        object.put("title", cursor.getString(title));
        object.put("author", cursor.getString(author));
        object.put("upTime", cursor.getString(upTime));
        object.put("rank", cursor.getString(rank));
        return object;
    }

    public JSONArray listBeforeInclude(Status status, int itemId, int limit) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
        try {
            JSONArray result = new JSONArray();
            Cursor cursor = db.rawQuery("select * " +
                            "   from article " +
                            "   where status = ? and id <= ?" +
                            "   order by id desc " +
                            "   limit ? " +
                            "",
                    new String[]{status.toString(), itemId + "", limit + ""});
            while (cursor.moveToNext()) {
                try {
                    JSONObject object = getArticle(cursor);
                    result.put(object);
                } catch (JSONException e) {
                    Log.e("abc", "list error", e);
                }
            }
            return result;
        } finally {
            db.close();
        }
    }

    public JSONArray listAfter(Status status, int itemId, int limit) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
        try {
            JSONArray result = new JSONArray();
            Cursor cursor = db.rawQuery("select * " +
                            "   from article " +
                            "   where status = ? and id > ?" +
                            "   order by id asc " +
                            "   limit ? " +
                            "   ",
                    new String[]{status.toString(), itemId + "", limit + ""});
            List<JSONObject> list = new ArrayList<JSONObject>();
            while (cursor.moveToNext()) {
                try {
                    JSONObject object = getArticle(cursor);
                    list.add(object);
                } catch (JSONException e) {
                    Log.e("abc", "list error", e);
                }
            }
            Collections.reverse(list);
            for (JSONObject object : list) {
                result.put(object);
            }
            return result;
        } finally {
            db.close();
        }
    }

    public JSONArray listBefore(Status status, int itemId, int limit) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
        try {
            JSONArray result = new JSONArray();
            Cursor cursor = db.rawQuery("select * " +
                            "   from article " +
                            "   where status = ? and id < ?" +
                            "   order by id desc " +
                            "   limit ? " +
                            "   ",
                    new String[]{status.toString(), itemId + "", limit + ""});
            while (cursor.moveToNext()) {
                try {
                    JSONObject object = getArticle(cursor);
                    result.put(object);
                } catch (JSONException e) {
                    Log.e("abc", "list error", e);
                }
            }
            return result;
        } finally {
            db.close();
        }
    }

    public int countArticle() {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dir + "/db.db", null);
        try {
            Cursor cursor = db.rawQuery("select count(1) " +
                            "   from article " +
                            "   ",
                    new String[]{});
            cursor.moveToNext();
            return cursor.getInt(0);
        } finally {
            db.close();
        }
    }

    public String getDir() {
        return dir;
    }
}
