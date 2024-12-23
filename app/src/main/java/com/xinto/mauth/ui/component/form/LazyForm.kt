package com.xinto.mauth.ui.component.form

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope

interface LazyGridForm {

    operator fun LazyGridScope.invoke()

}

fun LazyGridScope.form(lazyGridForm: LazyGridForm) {
    with(lazyGridForm) {
        this@form.invoke()
    }
}

inline fun <reified T: FormField<*>> LazyGridScope.formfield(field: T) {
    item(
        contentType = { field },
        key = field.id
    ) {
        field()
    }
}
inline fun <reified T: FormField<*>> LazyGridScope.singleFormfield(field: T) {
    item(
        contentType = field,
        key = field.id,
        span = { GridItemSpan(maxCurrentLineSpan) }
    ) {
        field()
    }
}