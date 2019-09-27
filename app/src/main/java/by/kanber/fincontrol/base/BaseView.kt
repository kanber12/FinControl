package by.kanber.fincontrol.base

import retrofit2.HttpException

interface BaseView<I> {
    fun onDeleteSuccess(code: Int, id: Int) {
    }

    fun onSuccess(resp: I) {
    }

    fun onSuccessList(list: List<I>) {
    }

    fun onError(error: HttpException)
}
