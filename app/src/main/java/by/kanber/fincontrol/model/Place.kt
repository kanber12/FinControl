package by.kanber.fincontrol.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import by.kanber.fincontrol.database.DBHelper
import by.kanber.fincontrol.database.PLACE_NAME
import by.kanber.fincontrol.database.PLACE_TABLE

data class Place(var name: String) {
    companion object {
        fun getAllPlaces(helper: DBHelper): MutableList<String> {
            val placeList = mutableListOf<String>()
            var name: String
            val database = helper.readableDatabase
            val cursor = database.rawQuery("select * from $PLACE_TABLE", null)

            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(PLACE_NAME)

                do {
                    name = cursor.getString(nameIndex)

                    placeList.add(name)
                } while (cursor.moveToNext())

                cursor.close()
            }

            return placeList
        }

        fun insertPlace(helper: DBHelper, place: String) {
            val cv = ContentValues()
            val database = helper.readableDatabase

            cv.put(PLACE_NAME, place)

            database.insertWithOnConflict(PLACE_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }

        fun updatePlace(helper: DBHelper, oldName: String, newName: String) {
            val cv = ContentValues()

            cv.put(PLACE_NAME, newName)

            helper.readableDatabase.update(PLACE_TABLE, cv, "$PLACE_NAME = '$oldName'", null)
        }

        fun deletePlace(helper: DBHelper, place: String) {
            helper.readableDatabase.delete(PLACE_TABLE, "$PLACE_NAME = '$place'", null)
        }
    }
}