package org.skull.king.bot.domain

sealed interface BotActionResult<out T> {

    companion object {
        fun <T> success(result: T) = Success(result)
        fun <T> failure(failure: String) = Failure<T>(failure)
    }

    fun onFailure(block: (failure: String) -> Unit): BotActionResult<T> {
        if (this is Failure) {
            block(this.failure)
        }
        return this
    }

    fun onSuccess(block: (success: T) -> Unit): BotActionResult<T> {
        if (this is Success) {
            block(this.result)
        }
        return this
    }

    data class Success<T>(val result: T) : BotActionResult<T>
    data class Failure<T>(val failure: String) : BotActionResult<T>

}