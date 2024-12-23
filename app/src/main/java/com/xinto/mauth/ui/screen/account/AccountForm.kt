package com.xinto.mauth.ui.screen.account

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Stable
import com.xinto.mauth.R
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.ui.component.form.ComboBoxFormField
import com.xinto.mauth.ui.component.form.Form
import com.xinto.mauth.ui.component.form.IntFormField
import com.xinto.mauth.ui.component.form.LazyGridForm
import com.xinto.mauth.ui.component.form.PasswordFormField
import com.xinto.mauth.ui.component.form.TextFormField
import com.xinto.mauth.ui.component.form.formfield
import com.xinto.mauth.ui.component.form.singleFormfield

@Stable
class AccountForm(private val initial: DomainAccountInfo) : Form<DomainAccountInfo>(), LazyGridForm {

    val icon = IconFormField(initial = initial.icon)
    val label = TextFormField(
        initial = initial.label,
        label = R.string.account_data_label,
        icon = R.drawable.ic_label,
        required = true
    )
    val issuer = TextFormField(
        initial = initial.issuer,
        label = R.string.account_data_issuer,
        icon = R.drawable.ic_apartment
    )
    val secret = PasswordFormField(
        initial = initial.secret,
        label = R.string.account_data_secret,
        icon = R.drawable.ic_key,
        required = true
    )
    val algorithm = ComboBoxFormField(
        initial = initial.algorithm,
        label = R.string.account_data_algorithm
    )
    val type = ComboBoxFormField(
        initial = initial.type,
        label = R.string.account_data_type
    )
    val digits = IntFormField(
        initial = initial.digits,
        label = R.string.account_data_digits,
        min = 1,
        max = 10
    )
    val counter = IntFormField(
        initial = initial.counter,
        label = R.string.account_data_counter,
        min = 0
    )
    val period = IntFormField(
        initial = initial.period,
        label = R.string.account_data_period,
        min = 1
    )

    fun isSame(): Boolean {
        val typeSimilar = when (type.value) {
            OtpType.TOTP -> initial.period == period.value
            OtpType.HOTP -> initial.counter == counter.value
        }

        return initial.icon == icon.value &&
                initial.label == label.value &&
                initial.issuer == issuer.value &&
                initial.secret == secret.value &&
                initial.type == type.value &&
                initial.digits == digits.value &&
                typeSimilar
    }

    override fun validate(): DomainAccountInfo? {
        if (!icon.validate()) return null
        if (!label.validate()) return null
        if (!issuer.validate()) return null
        if (!secret.validate()) return null
        if (!algorithm.validate()) return null
        if (!type.validate()) return null
        if (!digits.validate()) return null

        when (type.value) {
            OtpType.TOTP -> if (!period.validate()) return null
            OtpType.HOTP -> if (!counter.validate()) return null
        }

        return initial.copy(
            icon = icon.value,
            label = label.value,
            issuer = issuer.value,
            secret = secret.value,
            algorithm = algorithm.value,
            type = type.value,
            digits = digits.value,
            counter = if (type.value == OtpType.HOTP) counter.value else initial.counter,
            period = if (type.value == OtpType.TOTP) period.value else initial.period
        )
    }

    override operator fun LazyGridScope.invoke() {
        singleFormfield(icon)
        singleFormfield(label)
        singleFormfield(issuer)
        singleFormfield(secret)
        formfield(type)
        formfield(algorithm)
        formfield(digits)

        item(
            key = "Type",
            contentType = "Type"
        ) {
            AnimatedContent(
                targetState = type.value,
                transitionSpec = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() togetherWith
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeOut()
                },
                label = "HOTP/TOTP"
            ) {
                when (it) {
                    OtpType.TOTP -> period()
                    OtpType.HOTP -> counter()
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountForm

        return initial == other.initial
    }

    override fun hashCode(): Int {
        return initial.hashCode()
    }

}