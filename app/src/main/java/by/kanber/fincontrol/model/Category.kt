package by.kanber.fincontrol.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import by.kanber.fincontrol.base.SpinnerItem
import by.kanber.fincontrol.database.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(val id: Int, override var name: String) : SpinnerItem(name), Parcelable {

    constructor(name: String) : this(0, name)



    companion object {
        fun getAllCategories(helper: DBHelper, whereStatement: String): MutableList<Category> {
            val categoryList = mutableListOf<Category>()
            var id: Int
            var name: String
            val database = helper.readableDatabase
            val cursor = database.rawQuery("select * from $CATEGORY_TABLE $whereStatement", null)

            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ID)
                val nameIndex = cursor.getColumnIndex(CATEGORY_NAME)

                do {
                    id = cursor.getInt(idIndex)
                    name = cursor.getString(nameIndex)

                    categoryList.add(Category(id, name))
                } while (cursor.moveToNext())

                cursor.close()
            }

            return categoryList
        }

        private fun getLastCategory(helper: DBHelper): Category {
            val database = helper.readableDatabase
            val cursor = database.rawQuery("select * from $CATEGORY_TABLE", null)

            cursor.moveToLast()

            val id = cursor.getInt(cursor.getColumnIndex(ID))
            val name = cursor.getString(cursor.getColumnIndex(CATEGORY_NAME))

            cursor.close()

            return Category(id, name)
        }

        fun insertCategory(helper: DBHelper, category: Category): Category {
            insertOrUpdateCategory(helper, category)

            return getLastCategory(helper)
        }

        fun updateCategory(helper: DBHelper, category: Category) {
            insertOrUpdateCategory(helper, category)
        }

        private fun insertOrUpdateCategory(helper: DBHelper, category: Category) {
            val cv = ContentValues()
            val database = helper.readableDatabase

            cv.put(CATEGORY_NAME, category.name)

            val count = database.update(CATEGORY_TABLE, cv, "$ID = ${category.id}", null)

            if (count == 0) {
                database.insertWithOnConflict(
                    CATEGORY_TABLE,
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
        }

        fun deleteCategory(helper: DBHelper, id: Int) {
            helper.readableDatabase.delete(CATEGORY_TABLE, "$ID = $id", null)
        }

        fun getDefaultCategoryId(helper: DBHelper): Int {
            val database = helper.readableDatabase
            val cursor = database.rawQuery(
                "select id_default from $DEFAULT_PROPERTIES_TABLE where $ID = $DEFAULT_CATEGORY",
                null
            )
            cursor.moveToFirst()
            val id = cursor.getInt(cursor.getColumnIndex(ID_DEFAULT))
            cursor.close()

            return id
        }

        fun updateDefaultCategory(helper: DBHelper, newId: Int) {
            val database = helper.readableDatabase
            val cv = ContentValues()

            cv.put(ID_DEFAULT, newId)

            database.update(DEFAULT_PROPERTIES_TABLE, cv, "$ID = $DEFAULT_CATEGORY", null)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Category

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        return result
    }
}