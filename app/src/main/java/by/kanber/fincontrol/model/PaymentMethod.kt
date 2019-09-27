package by.kanber.fincontrol.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import by.kanber.fincontrol.base.PaymentMethodListItem
import by.kanber.fincontrol.base.SpinnerItem
import by.kanber.fincontrol.database.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaymentMethod(
    val id: Int,
    override var name: String,
    var isDefault: Boolean,
    var serverId: Int
) : SpinnerItem(name), PaymentMethodListItem, Parcelable {

    constructor(
        name: String,
        isDefault: Boolean
    ) : this(0, name, isDefault, 1)

    constructor(id: Int, name: String) : this(id, name, false, 1)



    companion object {
        fun getMethodSize(helper: DBHelper): Int {
            val cursor = helper.readableDatabase.rawQuery(
                "select * from $METHOD_TABLE where $MET_ACTIVE = 1",
                null
            )

            val count = cursor.count
            cursor.close()

            return count
        }

        fun getAllMethods(helper: DBHelper): MutableList<PaymentMethod> {
            val methodList = mutableListOf<PaymentMethod>()
            val idDefault = getDefaultMethodId(helper)
            var id: Int
            var serverId: Int
            var name: String
            val database = helper.readableDatabase
            val cursor =
                database.rawQuery("select * from $METHOD_TABLE where $MET_ACTIVE = 1", null)

            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ID)
                val serverIdIndex = cursor.getColumnIndex(SERVER_ID)
                val nameIndex = cursor.getColumnIndex(MET_NAME)

                do {
                    id = cursor.getInt(idIndex)
                    serverId = cursor.getInt(serverIdIndex)
                    name = cursor.getString(nameIndex)
                    val isDefault = id == idDefault

                    methodList.add(PaymentMethod(id, name, isDefault, serverId))
                } while (cursor.moveToNext())

                cursor.close()
            }

            methodList.reverse()

            return methodList
        }

        private fun getLastMethod(helper: DBHelper, isDefault: Boolean): PaymentMethod {
            val database = helper.readableDatabase
            val cursor = database.rawQuery("select * from $METHOD_TABLE", null)

            cursor.moveToLast()

            val id = cursor.getInt(cursor.getColumnIndex(ID))
            val serverId = cursor.getInt(cursor.getColumnIndex(SERVER_ID))
            val name = cursor.getString(cursor.getColumnIndex(MET_NAME))

            cursor.close()

            return PaymentMethod(id, name, isDefault, serverId)
        }

        fun insertMethod(helper: DBHelper, method: PaymentMethod): PaymentMethod {
            insertOrUpdateMethod(helper, method)

            return getLastMethod(helper, method.isDefault)
        }

        fun updateMethod(helper: DBHelper, method: PaymentMethod) {
            insertOrUpdateMethod(helper, method)
        }

        private fun insertOrUpdateMethod(helper: DBHelper, method: PaymentMethod) {
            val cv = ContentValues()
            val database = helper.readableDatabase

            cv.put(MET_NAME, method.name)

            val count = database.update(METHOD_TABLE, cv, "$ID = ${method.id}", null)

            if (count == 0) {
                database.insertWithOnConflict(
                    METHOD_TABLE,
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
        }

        fun deleteMethod(helper: DBHelper, methodId: Int) {
            if (methodIsUsed(helper, methodId)) {
                val cv = ContentValues()
                cv.put(MET_ACTIVE, 0)

                helper.readableDatabase.update(METHOD_TABLE, cv, "$ID = $methodId", null)
            } else {
                helper.readableDatabase.delete(METHOD_TABLE, "$ID = $methodId", null)
            }
        }

        private fun methodIsUsed(helper: DBHelper, id: Int): Boolean {
            val cursor = helper.readableDatabase.rawQuery(
                "select $ID from $TRANSACTION_TABLE where $TRANS_METHOD = $id",
                null
            )
            val count = cursor.count
            cursor.close()

            return count != 0
        }

        fun getDefaultMethodId(helper: DBHelper): Int {
            val database = helper.readableDatabase
            val cursor = database.rawQuery(
                "select id_default from $DEFAULT_PROPERTIES_TABLE where $ID = $DEFAULT_METHOD",
                null
            )
            cursor.moveToFirst()
            val id = cursor.getInt(cursor.getColumnIndex(ID_DEFAULT))
            cursor.close()

            return id
        }

        fun updateDefaultMethod(helper: DBHelper, newId: Int) {
            val database = helper.readableDatabase
            val cv = ContentValues()

            cv.put(ID_DEFAULT, newId)

            database.update(DEFAULT_PROPERTIES_TABLE, cv, "$ID = $DEFAULT_METHOD", null)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PaymentMethod

        if (id != other.id) return false
        if (name != other.name) return false
        if (isDefault != other.isDefault) return false
        if (serverId != other.serverId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + isDefault.hashCode()
        result = 31 * result + serverId
        return result
    }
}