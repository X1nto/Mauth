package com.xinto.mauth.di

import com.xinto.mauth.otp.generator.HotpGenerator
import com.xinto.mauth.otp.generator.HotpGeneratorImpl
import com.xinto.mauth.otp.generator.TotpGenerator
import com.xinto.mauth.otp.generator.TotpGeneratorImpl
import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.otp.parser.OtpUriParserImpl
import com.xinto.mauth.otp.transformer.KeyTransformer
import com.xinto.mauth.otp.transformer.KeyTransformerImpl
import org.apache.commons.codec.binary.Base32
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val otpModule = module {

    fun provideHotpGenerator(): HotpGenerator {
        return HotpGeneratorImpl()
    }

    fun provideTotpGenerator(hotpGenerator: HotpGenerator): TotpGenerator {
        return TotpGeneratorImpl(hotpGenerator)
    }

    fun provideKeyTransformer(): KeyTransformer {
        return KeyTransformerImpl(Base32())
    }

    fun provideUriParser(): OtpUriParser {
        return OtpUriParserImpl()
    }

    singleOf(::provideHotpGenerator)
    singleOf(::provideTotpGenerator)
    singleOf(::provideKeyTransformer)
    singleOf(::provideUriParser)
}