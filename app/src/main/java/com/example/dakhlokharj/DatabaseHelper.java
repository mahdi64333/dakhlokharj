package com.example.dakhlokharj;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int ORDER_MODE_TIME_DESC = 0;
    public static final int ORDER_MODE_TIME_ASC = 1;
    public static final int ORDER_MODE_PRICE_DESC = 2;
    public static final int ORDER_MODE_PRICE_ASC = 3;
    private static final String DB_NAME = "dakhlokharj.db";
    private static final String TEST_DB_NAME = "test.db";
    private static final int VERSION = 1;
    private static final String RESIDENTS_TABLE_NAME = "residents";
    private static final String RESIDENTS_COLUMN_RESIDENT_ID = "resident_id";
    private static final String RESIDENTS_COLUMN_NAME = "name";
    private static final String RESIDENTS_COLUMN_ACTIVE = "active";
    private static final String RESIDENTS_CREATE_QUERY = "CREATE TABLE '" + RESIDENTS_TABLE_NAME + "' (\n" +
            "'" + RESIDENTS_COLUMN_RESIDENT_ID + "'INTEGER NOT NULL UNIQUE,\n" +
            "'" + RESIDENTS_COLUMN_NAME + "'TEXT UNIQUE,\n" +
            "'" + RESIDENTS_COLUMN_ACTIVE + "'INTEGER DEFAULT 1,\n" +
            "PRIMARY KEY('" + RESIDENTS_COLUMN_RESIDENT_ID + "')\n" +
            ")";
    private static final String ORDERS_TABLE_NAME = "orders";
    private static final String ORDERS_COLUMN_ORDER_ID = "order_id";
    private static final String ORDERS_COLUMN_PRODUCT = "product";
    private static final String ORDERS_COLUMN_PRICE = "price";
    private static final String ORDERS_COLUMN_BUYER_ID = "buyer_id";
    private static final String ORDERS_COLUMN_YEAR = "year";
    private static final String ORDERS_COLUMN_MONTH = "month";
    private static final String ORDERS_COLUMN_DAY = "day";
    private static final String ORDERS_COLUMN_HOUR = "hour";
    private static final String ORDERS_COLUMN_MINUTE = "minute";
    private static final String ORDERS_COLUMN_SECOND = "second";
    private static final String ORDERS_CREATE_QUERY = "CREATE TABLE '" + ORDERS_TABLE_NAME + "' (\n" +
            "'" + ORDERS_COLUMN_ORDER_ID + "'INTEGER,\n" +
            "'" + ORDERS_COLUMN_PRODUCT + "'TEXT,\n" +
            "'" + ORDERS_COLUMN_PRICE + "'INTEGER,\n" +
            "'" + ORDERS_COLUMN_BUYER_ID + "'INTEGER,\n" +
            "'" + ORDERS_COLUMN_YEAR + "'INTEGER,\n" +
            "'" + ORDERS_COLUMN_MONTH + "'INTEGER,\n" +
            "'" + ORDERS_COLUMN_DAY + "'INTEGER,\n" +
            "'" + ORDERS_COLUMN_HOUR + "'INTEGER,\n" +
            "'" + ORDERS_COLUMN_MINUTE + "'INTEGER,\n" +
            "'" + ORDERS_COLUMN_SECOND + "'INTEGER,\n" +
            "FOREIGN KEY('" + ORDERS_COLUMN_BUYER_ID + "') REFERENCES '" + RESIDENTS_TABLE_NAME +
            "'('" + RESIDENTS_COLUMN_RESIDENT_ID + "') ON DELETE CASCADE ON UPDATE CASCADE,\n" +
            "PRIMARY KEY('" + ORDERS_COLUMN_ORDER_ID + "')\n" +
            ")";
    private static final String CONSUMERS_TABLE_NAME = "consumers";
    private static final String CONSUMERS_COLUMN_ORDER_ID = "for_order_id";
    private static final String CONSUMERS_COLUMN_CONSUMER_ID = "consumer_id";
    private static final String CONSUMERS_CREATE_QUERY = "CREATE TABLE '" + CONSUMERS_TABLE_NAME + "' (\n" +
            "'" + CONSUMERS_COLUMN_ORDER_ID + "'INTEGER,\n" +
            "'" + CONSUMERS_COLUMN_CONSUMER_ID + "'INTEGER,\n" +
            "FOREIGN KEY('" + CONSUMERS_COLUMN_ORDER_ID + "') REFERENCES '" +
            ORDERS_TABLE_NAME + "'('" + ORDERS_COLUMN_ORDER_ID + "') ON DELETE CASCADE ON UPDATE CASCADE,\n" +
            "FOREIGN KEY('" + CONSUMERS_COLUMN_CONSUMER_ID + "') REFERENCES '" +
            RESIDENTS_TABLE_NAME + "'('" + RESIDENTS_COLUMN_RESIDENT_ID + "') ON DELETE CASCADE ON UPDATE CASCADE,\n" +
            "PRIMARY KEY('" + CONSUMERS_COLUMN_ORDER_ID + "','" + CONSUMERS_COLUMN_CONSUMER_ID + "')\n" +
            ")";
    private static final String orderByTimeDesc = " ORDER BY " +
            ORDERS_COLUMN_YEAR + " DESC, " +
            ORDERS_COLUMN_MONTH + " DESC, " +
            ORDERS_COLUMN_DAY + " DESC, " +
            ORDERS_COLUMN_HOUR + " DESC, " +
            ORDERS_COLUMN_MINUTE + " DESC, " +
            ORDERS_COLUMN_SECOND + " DESC";
    private static final String orderByPriceDesc = " ORDER BY " +
            ORDERS_COLUMN_PRICE + " DESC";
    private static final String orderByTimeAsc = " ORDER BY " +
            ORDERS_COLUMN_YEAR + " ASC, " +
            ORDERS_COLUMN_MONTH + " ASC, " +
            ORDERS_COLUMN_DAY + " ASC, " +
            ORDERS_COLUMN_HOUR + " ASC, " +
            ORDERS_COLUMN_MINUTE + " ASC, " +
            ORDERS_COLUMN_SECOND + " ASC";
    private static final String orderByPriceAsc = " ORDER BY " +
            ORDERS_COLUMN_PRICE + " ASC";
    private final Context context;
    private String orderString;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.context = context;
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(this.context.getString(R.string.settings_shared_preferences), Context.MODE_PRIVATE);
        switch (sharedPreferences.getInt(this.context.getString(R.string.settings_default_order), DatabaseHelper.ORDER_MODE_TIME_DESC)) {
            case DatabaseHelper.ORDER_MODE_TIME_DESC:
                setToOrderByTimeDesc();
                break;
            case DatabaseHelper.ORDER_MODE_TIME_ASC:
                setToOrderByTimeAsc();
                break;
            case DatabaseHelper.ORDER_MODE_PRICE_DESC:
                setToOrderByPriceDesc();
                break;
            case DatabaseHelper.ORDER_MODE_PRICE_ASC:
                setToOrderByPriceAsc();
                break;
        }
    }

    public DatabaseHelper(Context context, String TEST_DB_NAME) {
        super(context, TEST_DB_NAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RESIDENTS_CREATE_QUERY);
        db.execSQL(ORDERS_CREATE_QUERY);
        db.execSQL(CONSUMERS_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + CONSUMERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ORDERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RESIDENTS_TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    public void setToOrderByTimeDesc() {
        orderString = orderByTimeDesc;
    }

    public void setToOrderByPriceDesc() {
        orderString = orderByPriceDesc;
    }

    public void setToOrderByTimeAsc() {
        orderString = orderByTimeAsc;
    }

    public void setToOrderByPriceAsc() {
        orderString = orderByPriceAsc;
    }

    public String addResident(String residentName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RESIDENTS_COLUMN_NAME, residentName);

        try {
            db.insertOrThrow(RESIDENTS_TABLE_NAME, null, cv);
        } catch (SQLException e) {
            return e.getMessage();
        } finally {
            db.close();
        }
        return null;
    }

    public String addOrder(Order order, ArrayList<String> consumers) {
        int buyerId = getResidentIdByName(order.getBuyer());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ORDERS_COLUMN_PRODUCT, order.getOrderName());
        cv.put(ORDERS_COLUMN_PRICE, order.getPrice());
        cv.put(ORDERS_COLUMN_BUYER_ID, buyerId);
        cv.put(ORDERS_COLUMN_YEAR, order.getYear());
        cv.put(ORDERS_COLUMN_MONTH, order.getMonth());
        cv.put(ORDERS_COLUMN_DAY, order.getDay());
        cv.put(ORDERS_COLUMN_HOUR, order.getHour());
        cv.put(ORDERS_COLUMN_MINUTE, order.getMinute());
        cv.put(ORDERS_COLUMN_SECOND, order.getSecond());

        try {
            db.insertOrThrow(ORDERS_TABLE_NAME, null, cv);
        } catch (SQLException e) {
            db.close();
            return e.getMessage();
        }
        Cursor cursor = db.rawQuery("SELECT " + ORDERS_COLUMN_ORDER_ID +
                " FROM " + ORDERS_TABLE_NAME +
                " ORDER BY " + ORDERS_COLUMN_ORDER_ID + " DESC LIMIT 1", null);
        cursor.moveToFirst();
        int orderId = cursor.getInt(0);
        cursor.close();
        for (int i = 0; i < consumers.size(); i++) {
            db.close();
            cv = new ContentValues();
            cv.put(CONSUMERS_COLUMN_ORDER_ID, orderId);
            cv.put(CONSUMERS_COLUMN_CONSUMER_ID, getResidentIdByName(consumers.get(i)));
            db = this.getWritableDatabase();
            try {
                db.insertOrThrow(CONSUMERS_TABLE_NAME, null, cv);
            } catch (SQLException e) {
                db.close();
                return e.getMessage();
            }
        }
        db.close();
        return String.valueOf(orderId);
    }

    public ArrayList<Resident> getAllResidents(boolean onlyActives) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ArrayList<Resident> residents = new ArrayList<>();
        String query;
        if (onlyActives) {
            query = "SELECT * FROM " + RESIDENTS_TABLE_NAME +
                    " WHERE " + RESIDENTS_COLUMN_ACTIVE + "=1 ORDER BY " + RESIDENTS_COLUMN_NAME;
        } else {
            query = "SELECT * FROM " + RESIDENTS_TABLE_NAME + " ORDER BY " + RESIDENTS_COLUMN_NAME;
        }
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                residents.add(new Resident(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2) == 1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return residents;
    }

    public ArrayList<ResidentSummery> getAllResidentsSummery() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ArrayList<ResidentSummery> residentsSummaries = new ArrayList<>();
        String query;
        query = "SELECT * FROM " + RESIDENTS_TABLE_NAME +
                " WHERE " + RESIDENTS_COLUMN_ACTIVE + "=1 ORDER BY " + RESIDENTS_COLUMN_NAME;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                residentsSummaries.add(new ResidentSummery(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2) == 1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        query = "SELECT " + RESIDENTS_COLUMN_RESIDENT_ID + ", sum(" + ORDERS_COLUMN_PRICE +
                ") FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + ORDERS_COLUMN_BUYER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                " WHERE " + RESIDENTS_COLUMN_ACTIVE + "=1" +
                " GROUP BY " + RESIDENTS_COLUMN_RESIDENT_ID +
                " ORDER BY " + RESIDENTS_COLUMN_NAME;
        cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int i = 0;
            do {
                while (residentsSummaries.get(i).getId() != cursor.getInt(0)) {
                    i++;
                }
                residentsSummaries.get(i).setCredit(Math.round(cursor.getInt(1) / 100f) * 100);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        query = "SELECT " + RESIDENTS_COLUMN_RESIDENT_ID +
                ", sum(" + ORDERS_COLUMN_PRICE + "/(SELECT count(*) FROM " + CONSUMERS_TABLE_NAME +
                " AS innerTabel WHERE innerTabel." + CONSUMERS_COLUMN_ORDER_ID + "=outerTable." + CONSUMERS_COLUMN_ORDER_ID + "))" +
                " FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + CONSUMERS_TABLE_NAME + " AS outerTable" +
                " ON " + ORDERS_COLUMN_ORDER_ID + "=" + CONSUMERS_COLUMN_ORDER_ID +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + CONSUMERS_COLUMN_CONSUMER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                " WHERE " + RESIDENTS_COLUMN_ACTIVE + "=1" +
                " GROUP BY " + RESIDENTS_COLUMN_RESIDENT_ID +
                " ORDER BY " + RESIDENTS_COLUMN_NAME;
        cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int i = 0;
            do {
                while (residentsSummaries.get(i).getId() != cursor.getInt(0)) {
                    i++;
                }
                residentsSummaries.get(i).setDebt(Math.round(cursor.getInt(1) / 100f) * 100);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return residentsSummaries;
    }

    public ArrayList<ResidentSummery> getAllResidentsSummeryWithDateBetween(int fromYear, int toYear,
                                                                            int fromMonth, int toMonth,
                                                                            int fromDay, int toDay) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ArrayList<ResidentSummery> residentsSummaries = new ArrayList<>();
        String query;
        query = "SELECT * FROM " + RESIDENTS_TABLE_NAME +
                " WHERE " + RESIDENTS_COLUMN_ACTIVE + "=1 ORDER BY " + RESIDENTS_COLUMN_NAME;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                residentsSummaries.add(new ResidentSummery(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2) == 1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        query = "SELECT " + RESIDENTS_COLUMN_RESIDENT_ID + ", sum(" + ORDERS_COLUMN_PRICE +
                ") FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + ORDERS_COLUMN_BUYER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                " WHERE " + RESIDENTS_COLUMN_ACTIVE + "=1" +

                " AND ((" + ORDERS_COLUMN_YEAR + ">" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "<" + toYear +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "<" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + ">" + fromMonth +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_YEAR + ">" + fromYear +
                " AND " + ORDERS_COLUMN_MONTH + "<" + toMonth +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "<" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + fromMonth +
                " AND " + ORDERS_COLUMN_DAY + ">=" + fromDay +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_YEAR + ">" + fromYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + toMonth +
                " AND " + ORDERS_COLUMN_DAY + "<=" + toDay +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + fromMonth +
                " AND " + ORDERS_COLUMN_MONTH + "<" + toMonth +
                " AND " + ORDERS_COLUMN_DAY + ">=" + fromDay +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + toMonth +
                " AND " + ORDERS_COLUMN_MONTH + ">" + fromMonth +
                " AND " + ORDERS_COLUMN_DAY + "<=" + toDay +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + toMonth +
                " AND " + ORDERS_COLUMN_MONTH + "=" + fromMonth +
                " AND " + ORDERS_COLUMN_DAY + ">=" + fromDay +
                " AND " + ORDERS_COLUMN_DAY + "<=" + toDay + "))" +

                " GROUP BY " + RESIDENTS_COLUMN_RESIDENT_ID +
                " ORDER BY " + RESIDENTS_COLUMN_NAME;
        cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int i = 0;
            do {
                while (residentsSummaries.get(i).getId() != cursor.getInt(0)) {
                    i++;
                }
                residentsSummaries.get(i).setCredit(Math.round(cursor.getInt(1) / 100f) * 100);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        query = "SELECT " + RESIDENTS_COLUMN_RESIDENT_ID +
                ", sum(" + ORDERS_COLUMN_PRICE + "/(SELECT count(*) FROM " + CONSUMERS_TABLE_NAME +
                " AS innerTabel WHERE innerTabel." + CONSUMERS_COLUMN_ORDER_ID + "=outerTable." + CONSUMERS_COLUMN_ORDER_ID + "))" +
                " FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + CONSUMERS_TABLE_NAME + " AS outerTable" +
                " ON " + ORDERS_COLUMN_ORDER_ID + "=" + CONSUMERS_COLUMN_ORDER_ID +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + CONSUMERS_COLUMN_CONSUMER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                " WHERE " + RESIDENTS_COLUMN_ACTIVE + "=1" +
                " AND ((" + ORDERS_COLUMN_YEAR + ">" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "<" + toYear +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "<" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + ">" + fromMonth +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_YEAR + ">" + fromYear +
                " AND " + ORDERS_COLUMN_MONTH + "<" + toMonth +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "<" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + fromMonth +
                " AND " + ORDERS_COLUMN_DAY + ">=" + fromDay +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_YEAR + ">" + fromYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + toMonth +
                " AND " + ORDERS_COLUMN_DAY + "<=" + toDay +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + fromMonth +
                " AND " + ORDERS_COLUMN_MONTH + "<" + toMonth +
                " AND " + ORDERS_COLUMN_DAY + ">=" + fromDay +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + toMonth +
                " AND " + ORDERS_COLUMN_MONTH + ">" + fromMonth +
                " AND " + ORDERS_COLUMN_DAY + "<=" + toDay +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + toMonth +
                " AND " + ORDERS_COLUMN_MONTH + "=" + fromMonth +
                " AND " + ORDERS_COLUMN_DAY + ">=" + fromDay +
                " AND " + ORDERS_COLUMN_DAY + "<=" + toDay + "))" +
                " GROUP BY " + RESIDENTS_COLUMN_RESIDENT_ID +
                " ORDER BY " + RESIDENTS_COLUMN_NAME;
        cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int i = 0;
            do {
                while (residentsSummaries.get(i).getId() != cursor.getInt(0)) {
                    i++;
                }
                residentsSummaries.get(i).setDebt(Math.round(cursor.getInt(1) / 100f) * 100);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return residentsSummaries;
    }

    public ArrayList<Order> getAllOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ArrayList<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + ORDERS_COLUMN_BUYER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                orderString;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Order newOrder = new Order(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(11),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getInt(9));
                orders.add(newOrder);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public ArrayList<Order> getAllOrdersWithOrderName(String orderName) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ArrayList<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + ORDERS_COLUMN_BUYER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                " WHERE " + ORDERS_COLUMN_PRODUCT + " LIKE '%" + orderName + "%'" +
                orderString;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Order newOrder = new Order(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(11),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getInt(9));
                orders.add(newOrder);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public ArrayList<Order> getAllOrdersWithPriceBetween(int FROMPrice, int toPrice) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ArrayList<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + ORDERS_COLUMN_BUYER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                " WHERE " + ORDERS_COLUMN_PRICE + " BETWEEN " +
                FROMPrice + " AND " + toPrice +
                orderString;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Order newOrder = new Order(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(11),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getInt(9));
                orders.add(newOrder);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public ArrayList<Order> getAllOrdersWithBuyer(String Buyer) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ArrayList<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + ORDERS_COLUMN_BUYER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                " WHERE " + RESIDENTS_COLUMN_NAME + "='" + Buyer + "'" +
                orderString;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Order newOrder = new Order(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(11),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getInt(9));
                orders.add(newOrder);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public ArrayList<Order> getAllOrdersWithDateBetween(int fromYear, int toYear,
                                                        int fromMonth, int toMonth,
                                                        int fromDay, int toDay) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ArrayList<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + ORDERS_COLUMN_BUYER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                " WHERE (" + ORDERS_COLUMN_YEAR + ">" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "<" + toYear +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "<" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + ">" + fromMonth +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_YEAR + ">" + fromYear +
                " AND " + ORDERS_COLUMN_MONTH + "<" + toMonth +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "<" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + fromMonth +
                " AND " + ORDERS_COLUMN_DAY + ">=" + fromDay +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_YEAR + ">" + fromYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + toMonth +
                " AND " + ORDERS_COLUMN_DAY + "<=" + toDay +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + fromMonth +
                " AND " + ORDERS_COLUMN_MONTH + "<" + toMonth +
                " AND " + ORDERS_COLUMN_DAY + ">=" + fromDay +

                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + toMonth +
                " AND " + ORDERS_COLUMN_MONTH + ">" + fromMonth +
                " AND " + ORDERS_COLUMN_DAY + "<=" + toDay +


                ") OR (" + ORDERS_COLUMN_YEAR + "=" + fromYear +
                " AND " + ORDERS_COLUMN_YEAR + "=" + toYear +
                " AND " + ORDERS_COLUMN_MONTH + "=" + toMonth +
                " AND " + ORDERS_COLUMN_MONTH + "=" + fromMonth +
                " AND " + ORDERS_COLUMN_DAY + ">=" + fromDay +
                " AND " + ORDERS_COLUMN_DAY + "<=" + toDay + ")" +
                orderString;


        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Order newOrder = new Order(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(11),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getInt(9));
                orders.add(newOrder);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public ArrayList<Order> getAllOrdersWithConsumer(String Buyer) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        ArrayList<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM " + ORDERS_TABLE_NAME +
                " INNER JOIN " + CONSUMERS_TABLE_NAME +
                " ON " + ORDERS_COLUMN_ORDER_ID + "=" + CONSUMERS_COLUMN_ORDER_ID +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + CONSUMERS_COLUMN_CONSUMER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                " WHERE " + RESIDENTS_COLUMN_NAME + "='" + Buyer + "'" +
                orderString;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Order newOrder = new Order(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(11),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getInt(9));
                orders.add(newOrder);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    public String getAllConsumersName(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        StringBuilder consumersString = new StringBuilder("\n\n");
        String query;
        query = "SELECT " + RESIDENTS_COLUMN_NAME + " FROM " + CONSUMERS_TABLE_NAME +
                " INNER JOIN " + RESIDENTS_TABLE_NAME +
                " ON " + CONSUMERS_COLUMN_CONSUMER_ID + "=" + RESIDENTS_COLUMN_RESIDENT_ID +
                "  WHERE " + CONSUMERS_COLUMN_ORDER_ID + "=" + orderId;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                consumersString.append(cursor.getString(0)).append("\n");
            } while (cursor.moveToNext());
        }
        cursor.close();
        return consumersString.toString();
    }

    public int getResidentIdByName(String residentName) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return -1;
        }
        Cursor cursor = db.rawQuery("SELECT " + RESIDENTS_COLUMN_RESIDENT_ID +
                " FROM " + RESIDENTS_TABLE_NAME +
                " WHERE " + RESIDENTS_COLUMN_NAME + "='" + residentName + "'", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }

    public void updateResidentActiveById(int residentId, boolean checked) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RESIDENTS_COLUMN_ACTIVE, checked ? 1 : 0);

        try {
            db.update(RESIDENTS_TABLE_NAME, cv,
                    RESIDENTS_COLUMN_RESIDENT_ID + "=" + residentId, null);
        } catch (SQLException e) {
            Toast.makeText(context, R.string.an_error_has_occurred, Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    public void updateResidentNameById(int residentId, String residentName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RESIDENTS_COLUMN_NAME, residentName);

        try {
            db.update(RESIDENTS_TABLE_NAME, cv,
                    RESIDENTS_COLUMN_RESIDENT_ID + "=" + residentId, null);
        } catch (SQLException e) {
            Toast.makeText(context, R.string.an_error_has_occurred, Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    public void deleteResidentById(int residentId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(RESIDENTS_TABLE_NAME,
                    RESIDENTS_COLUMN_RESIDENT_ID + "=" + residentId, null);
        } catch (SQLException e) {
            Toast.makeText(context, R.string.an_error_has_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteOrderById(int orderId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(ORDERS_TABLE_NAME,
                    ORDERS_COLUMN_ORDER_ID + "=" + orderId, null);
        } catch (SQLException e) {
            Toast.makeText(context, R.string.an_error_has_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkIfResidentExistsByName(String residentName) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT " + RESIDENTS_COLUMN_NAME +
                " FROM " + RESIDENTS_TABLE_NAME +
                " WHERE " + RESIDENTS_COLUMN_NAME + "='" + residentName + "'", null);
        boolean b = cursor.getCount() > 0;
        cursor.close();
        return b;
    }

    public static boolean importDB(Context context, Uri importUri) {
        FileInputStream fis;
        try {
            fis = (FileInputStream) context.getContentResolver().openInputStream(importUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        String outFileName = context.getDatabasePath(TEST_DB_NAME).getPath();

        OutputStream output;
        try {
            output = new FileOutputStream(outFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        try {
            int length;
            byte[] charBuffer = new byte[16];
            length = fis.read(charBuffer, 0, 16);
            if (length <= 0) {
                return false;
            }
            StringBuilder header = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                header.append((char) charBuffer[i]);
            }
            if (!header.toString().contains("SQLite format 3")) {
                Toast.makeText(context, R.string.select_correct_file_type, Toast.LENGTH_SHORT).show();
                return false;
            }
            output.write(charBuffer, 0, length);


            byte[] buffer = new byte[1024];
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            DatabaseHelper testDbHelper = new DatabaseHelper(context, TEST_DB_NAME);
            SQLiteDatabase testDb = testDbHelper.getReadableDatabase();
            Cursor cursor = testDb.rawQuery("SELECT sql FROM sqlite_master" +
                            " WHERE name='" + CONSUMERS_TABLE_NAME +
                            "' OR name='" + ORDERS_TABLE_NAME + "' OR name='"
                            + RESIDENTS_TABLE_NAME + "'" +
                            "ORDER BY name",
                    null);
            if (cursor.getCount() != 3) {
                return false;
            }
            cursor.moveToFirst();
            if (!cursor.getString(0).equals(CONSUMERS_CREATE_QUERY)) {
                return false;
            }
            cursor.moveToNext();
            if (!cursor.getString(0).equals(ORDERS_CREATE_QUERY)) {
                return false;
            }
            cursor.moveToNext();
            if (!cursor.getString(0).equals(RESIDENTS_CREATE_QUERY)) {
                return false;
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        File testFile = new File(context.getDatabasePath(TEST_DB_NAME).getPath());
        File dbFile = new File(context.getDatabasePath(DB_NAME).getPath());

        return testFile.renameTo(dbFile);
    }

    public static boolean exportDB(Context context) {
        final String inFileName = context.getDatabasePath(DB_NAME).getPath();
        File dbFile = new File(inFileName);
        FileInputStream fis;
        try {
            fis = new FileInputStream(dbFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        String outFileName = Environment.getExternalStorageDirectory() + "/dakhlokharj.db";

        OutputStream output;
        try {
            output = new FileOutputStream(outFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {

                output.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
