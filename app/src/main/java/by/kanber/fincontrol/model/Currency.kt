package by.kanber.fincontrol.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import by.kanber.fincontrol.database.CURRENCY_NAME
import by.kanber.fincontrol.database.CURRENCY_TABLE
import by.kanber.fincontrol.database.DBHelper

data class Currency(val id: Int, var name: String) {
    companion object {
        fun getAllCurrencies(helper: DBHelper): MutableList<String> {
            val currencyList = mutableListOf<String>()
            var name: String
            val database = helper.readableDatabase
            val cursor = database.rawQuery("select * from $CURRENCY_TABLE", null)

            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(CURRENCY_NAME)

                do {
                    name = cursor.getString(nameIndex)

                    currencyList.add(name)
                } while (cursor.moveToNext())

                cursor.close()
            }

            return currencyList
        }

        fun insertCurrency(helper: DBHelper, currency: String) {
            val cv = ContentValues()
            val database = helper.readableDatabase

            cv.put(CURRENCY_NAME, currency)

            database.insertWithOnConflict(CURRENCY_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }

        fun updateCurrency(helper: DBHelper, oldName: String, newName: String) {
            val cv = ContentValues()

            cv.put(CURRENCY_NAME, newName)

            helper.readableDatabase.update(CURRENCY_TABLE, cv, "$CURRENCY_NAME = '$oldName'", null)
        }

        fun deleteCurrency(helper: DBHelper, currency: String) {
            helper.readableDatabase.delete(CURRENCY_TABLE, "$CURRENCY_NAME = '$currency'", null)
        }
    }
}