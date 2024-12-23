package com.xinto.mauth.ui.component.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Stable
abstract class FormField<T>(
    initial: T,
    val id: Int
) {

    var value by mutableStateOf(initial)
        protected set

    var error by mutableStateOf(false)
        private set

    @Composable
    abstract operator fun invoke(modifier: Modifier = Modifier)

    fun validate(): Boolean {
        return isValid().also {
            error = !it
        }
    }

    protected open fun isValid(): Boolean {
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FormField<*>) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }


}