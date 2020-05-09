package com.timotiusoktorio.inventoryapp.data;

import android.provider.BaseColumns;

public class ProductContract {

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String NOT_NULL = " NOT NULL";
    private static final String COMMA_SEP = ", ";

    public ProductContract() {
    }

    static abstract class ProductEntry implements BaseColumns {

        static final String TABLE_NAME = "product";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_CODE = "code";
        static final String COLUMN_SUPPLIER = "supplier";
        static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";
        static final String COLUMN_PHOTO_PATH = "photo_path";
        static final String COLUMN_PRICE = "price";
        static final String COLUMN_QUANTITY = "quantity";

        static final String SQL_CREATE_ENTRY =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                        COLUMN_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_CODE + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_SUPPLIER + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_SUPPLIER_EMAIL + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_PHOTO_PATH + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_PRICE + REAL_TYPE + NOT_NULL + COMMA_SEP +
                        COLUMN_QUANTITY + INTEGER_TYPE + NOT_NULL + ")";

        static final String SQL_DELETE_ENTRY =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

    }
}