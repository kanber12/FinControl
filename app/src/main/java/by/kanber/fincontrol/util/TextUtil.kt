package by.kanber.fincontrol.util

import by.kanber.fincontrol.model.Currency
import java.math.BigDecimal
import java.math.RoundingMode

class TextUtil {
    companion object {
        fun isEmpty(text: String): Boolean =
                text == "" || text.trim() == "" || text.split("\n").isEmpty()

        fun getViewableSum(sum: Double): String =
            BigDecimal(sum).setScale(2, RoundingMode.HALF_UP).toString()

        fun getFromMethodName(name: String): String = "From $name"

        fun getToMethodName(name: String): String = "To $name"

        fun getViewableBalance(balance: Double, currency: Currency): String {
            return "${getViewableSum(balance)} ${currency.name}"
        }
    }
}