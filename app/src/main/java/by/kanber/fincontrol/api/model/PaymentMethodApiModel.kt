package by.kanber.fincontrol.api.model

import java.util.*

data class PaymentMethodApiModel(var id: Int, var name: String, var balance: Double, var currency: Currency, var isDefault: Boolean)