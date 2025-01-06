package com.xinto.mauth.ui.component.form

abstract class Form<T : Any> {

    abstract fun validate(): T?

}