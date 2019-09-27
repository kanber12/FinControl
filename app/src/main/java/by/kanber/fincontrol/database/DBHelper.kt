package by.kanber.fincontrol.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DB_VERSION = 1
private const val DB_NAME = "finControlDB"
const val TRANSACTION_TABLE = "transactions"
const val METHOD_TABLE = "methods"
const val PLACE_TABLE = "places"
const val CURRENCY_TABLE = "currencies"
const val DEFAULT_PROPERTIES_TABLE = "default_properties"
const val CATEGORY_TABLE = "categories"
const val ID = "_id"
const val SERVER_ID = "server_id"
const val MET_NAME = "method_name"
const val PLACE_NAME = "place_name"
const val CURRENCY_NAME = "currency_name"
const val CATEGORY_NAME = "category_name"
const val TRANS_CATEGORY = "category_id"
const val TRANS_ARCHIVED = "is_archived"
const val TRANS_TYPE = "type"
const val TRANS_SUM = "sum"
const val TRANS_CURRENCY = "currency"
const val TRANS_DATE = "date"
const val TRANS_ARCHIVE_DATE = "archive_date"
const val TRANS_DESCRIPTION = "description"
const val TRANS_METHOD = "method_id"
const val TRANS_PLACE = "place"
const val MET_ACTIVE = "active_status"
const val ID_DEFAULT = "id_default"

const val DEFAULT_METHOD = 1
const val DEFAULT_CATEGORY = 2

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DB_NAME, factory, DB_VERSION) {
    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        db?.execSQL("PRAGMA foreign_keys = ON;")
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table $TRANSACTION_TABLE ($ID integer primary key autoincrement, $TRANS_ARCHIVED integer default 0, $TRANS_TYPE integer, $TRANS_SUM real, $TRANS_CURRENCY text, $TRANS_DATE integer, $TRANS_ARCHIVE_DATE integer default 0, $TRANS_DESCRIPTION text, $TRANS_CATEGORY integer default 1 references $CATEGORY_TABLE($ID) on delete set default, $TRANS_METHOD integer references $METHOD_TABLE($ID), $TRANS_PLACE text, $SERVER_ID integer default 1)")
        db?.execSQL("create table $METHOD_TABLE ($ID integer primary key autoincrement, $MET_NAME text, $MET_ACTIVE integer default 1, $SERVER_ID integer default 1)")
        db?.execSQL("create table $PLACE_TABLE ($ID integer primary key autoincrement, $PLACE_NAME text, $SERVER_ID integer default 1)")

        db?.execSQL("create table $CATEGORY_TABLE ($ID integer primary key autoincrement, $CATEGORY_NAME text, $SERVER_ID integer default 1)")
        db?.execSQL("insert into $CATEGORY_TABLE($ID, $CATEGORY_NAME) values (1, 'None')")

        db?.execSQL("create table $CURRENCY_TABLE ($ID integer primary key autoincrement, $CURRENCY_NAME text, $SERVER_ID integer default 1)")
        db?.execSQL("insert into $CURRENCY_TABLE($ID, $CURRENCY_NAME) values (1, 'BYN')")
        db?.execSQL("insert into $CURRENCY_TABLE($ID, $CURRENCY_NAME) values (2, 'USD')")
        db?.execSQL("insert into $CURRENCY_TABLE($ID, $CURRENCY_NAME) values (3, 'EUR')")
        db?.execSQL("insert into $CURRENCY_TABLE($ID, $CURRENCY_NAME) values (4, 'RUB')")

        db?.execSQL("create table $DEFAULT_PROPERTIES_TABLE ($ID integer primary key, $ID_DEFAULT integer)")
        db?.execSQL("insert into $DEFAULT_PROPERTIES_TABLE($ID, $ID_DEFAULT) values ($DEFAULT_METHOD, -1)")
        db?.execSQL("insert into $DEFAULT_PROPERTIES_TABLE($ID, $ID_DEFAULT) values ($DEFAULT_CATEGORY, 0)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("drop table if exists $TRANSACTION_TABLE")
        db?.execSQL("drop table if exists $METHOD_TABLE")
        db?.execSQL("drop table if exists $PLACE_TABLE")
        db?.execSQL("drop table if exists $CURRENCY_TABLE")
        db?.execSQL("drop table if exists $CATEGORY_TABLE")
        db?.execSQL("drop table if exists $DEFAULT_PROPERTIES_TABLE")
        onCreate(db)
    }
}