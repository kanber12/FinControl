package by.kanber.fincontrol.model

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcelable
import by.kanber.fincontrol.database.*
import by.kanber.fincontrol.util.DateUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Transaction(
    val id: Int,
    var type: Boolean,
    var sum: Double,
    var currency: String,
    var date: Long,
    var description: String,
    var category: Category,
    var method: PaymentMethod,
    var place: String
) : Parcelable {


    companion object {
        fun getAllTransactions(helper: DBHelper, isArchived: Boolean): MutableList<Transaction> {
            val archivedArg = if (isArchived) 1 else 0
            val transList = mutableListOf<Transaction>()
            var id: Int
            var type: Boolean
            var sum: Double
            var currency: String
            var date: Long
            var description: String
            var categoryId: Int
            var categoryName: String
            var methodId: Int
            var methodName: String
            var place: String
            val database = helper.readableDatabase
            val cursor = database.rawQuery(
                "select $TRANSACTION_TABLE.*, $METHOD_TABLE.$MET_NAME, $CATEGORY_TABLE.$CATEGORY_NAME from $TRANSACTION_TABLE join $METHOD_TABLE on $METHOD_TABLE.$ID = $TRANSACTION_TABLE.$TRANS_METHOD join $CATEGORY_TABLE on $CATEGORY_TABLE.$ID = $TRANSACTION_TABLE.$TRANS_CATEGORY where $TRANS_ARCHIVED = $archivedArg",
                null
            )

            if (isArchived) {
                removeExpiredTransactions(helper, cursor)
            }

            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ID)
                val typeIndex = cursor.getColumnIndex(TRANS_TYPE)
                val sumIndex = cursor.getColumnIndex(TRANS_SUM)
                val currencyIndex = cursor.getColumnIndex(TRANS_CURRENCY)
                val dateIndex = cursor.getColumnIndex(TRANS_DATE)
                val descriptionIndex = cursor.getColumnIndex(TRANS_DESCRIPTION)
                val placeIndex = cursor.getColumnIndex(TRANS_PLACE)
                val categoryIdIndex = cursor.getColumnIndex(TRANS_CATEGORY)
                val categoryNameIndex = cursor.getColumnIndex(CATEGORY_NAME)
                val methodIdIndex = cursor.getColumnIndex(TRANS_METHOD)
                val methodNameIndex = cursor.getColumnIndex(MET_NAME)

                do {
                    id = cursor.getInt(idIndex)
                    type = cursor.getInt(typeIndex) == 1
                    sum = cursor.getDouble(sumIndex)
                    currency = cursor.getString(currencyIndex)
                    date = cursor.getLong(dateIndex)
                    description = cursor.getString(descriptionIndex)
                    categoryId = cursor.getInt(categoryIdIndex)
                    categoryName = cursor.getString(categoryNameIndex)
                    methodId = cursor.getInt(methodIdIndex)
                    methodName = cursor.getString(methodNameIndex)
                    place = cursor.getString(placeIndex)

                    transList.add(
                        Transaction(
                            id,
                            type,
                            sum,
                            currency,
                            date,
                            description,
                            Category(categoryId, categoryName),
                            PaymentMethod(methodId, methodName),
                            place
                        )
                    )
                } while (cursor.moveToNext())

                cursor.close()
            }

            return transList
        }

        private fun getLastTransaction(
            helper: DBHelper,
            method: PaymentMethod,
            category: Category
        ): Transaction {
            val database = helper.readableDatabase
            val cursor = database.rawQuery("select * from $TRANSACTION_TABLE", null)

            cursor.moveToLast()

            val id = cursor.getInt(cursor.getColumnIndex(ID))
            val type = cursor.getInt(cursor.getColumnIndex(TRANS_TYPE)) == 1
            val sum = cursor.getDouble(cursor.getColumnIndex(TRANS_SUM))
            val currency = cursor.getString(cursor.getColumnIndex(TRANS_CURRENCY))
            val date = cursor.getLong(cursor.getColumnIndex(TRANS_DATE))
            val description = cursor.getString(cursor.getColumnIndex(TRANS_DESCRIPTION))
            val place = cursor.getString(cursor.getColumnIndex(TRANS_PLACE))

            cursor.close()

            return Transaction(id, type, sum, currency, date, description, category, method, place)
        }

        fun insertTransaction(helper: DBHelper, transaction: Transaction): Transaction {
            insertOrUpdateTransaction(helper, transaction)

            return getLastTransaction(helper, transaction.method, transaction.category)
        }

        fun updateTransaction(helper: DBHelper, transaction: Transaction) {
            insertOrUpdateTransaction(helper, transaction)
        }

        private fun insertOrUpdateTransaction(helper: DBHelper, transaction: Transaction) {
            val cv = ContentValues()
            val database = helper.readableDatabase

            cv.put(TRANS_TYPE, transaction.type)
            cv.put(TRANS_SUM, transaction.sum)
            cv.put(TRANS_CURRENCY, transaction.currency)
            cv.put(TRANS_DATE, transaction.date)
            cv.put(TRANS_DESCRIPTION, transaction.description)
            cv.put(TRANS_CATEGORY, transaction.category.id)
            cv.put(TRANS_METHOD, transaction.method.id)
            cv.put(TRANS_PLACE, transaction.place)

            val count = database.update(TRANSACTION_TABLE, cv, "$ID = ${transaction.id}", null)

            if (count == 0) {
                database.insertWithOnConflict(
                    TRANSACTION_TABLE,
                    null,
                    cv,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
        }

        fun archiveTransaction(helper: DBHelper, transId: Int) {
            changeTransArchiveStatus(helper, transId, true)
        }

        fun unarchiveTransaction(helper: DBHelper, transId: Int) {
            changeTransArchiveStatus(helper, transId, false)
        }

        private fun changeTransArchiveStatus(helper: DBHelper, transId: Int, isArchived: Boolean) {
            val cv = ContentValues()
            val database = helper.readableDatabase

            cv.put(TRANS_ARCHIVED, isArchived)
            cv.put(TRANS_ARCHIVE_DATE, if (isArchived) System.currentTimeMillis() else 0)

            database.update(TRANSACTION_TABLE, cv, "$ID = $transId", null)
        }

        fun deleteTransaction(helper: DBHelper, transId: Int) {
            helper.readableDatabase.delete(TRANSACTION_TABLE, "$ID = $transId", null)
        }

        private fun removeExpiredTransactions(helper: DBHelper, cursor: Cursor) {
            val currentDate = System.currentTimeMillis()
            val archDateIndex = cursor.getColumnIndex(TRANS_ARCHIVE_DATE)

            if (cursor.moveToFirst()) {
                do {
                    val date = cursor.getLong(archDateIndex)

                    if (DateUtil.isTransactionExpired(currentDate, date)) {
                        deleteTransaction(helper, cursor.getInt(cursor.getColumnIndex(ID)))
                    }
                } while (cursor.moveToNext())
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        if (id != other.id) return false
        if (type != other.type) return false
        if (sum != other.sum) return false
        if (currency != other.currency) return false
        if (date != other.date) return false
        if (description != other.description) return false
        if (category.id != other.category.id) return false
        if (method.id != other.method.id) return false
        if (place != other.place) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + type.hashCode()
        result = 31 * result + sum.hashCode()
        result = 31 * result + currency.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + place.hashCode()
        return result
    }
}