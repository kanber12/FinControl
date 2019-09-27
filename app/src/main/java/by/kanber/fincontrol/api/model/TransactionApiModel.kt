package by.kanber.fincontrol.api.model

import by.kanber.fincontrol.model.Currency

data class TransactionApiModel(var id: Int = 0, var sum: Double = 0.0, var currency: Currency, var date: Long,
                               var note: String, var methodId: Int,
                               var place: PlaceApiModel, var from: PlaceApiModel, var fromMethodId: Int,
                               var toMethodId: Int) {

}