package com.xinto.mauth.di

import com.xinto.mauth.otp.generator.OtpGenerator
import com.xinto.mauth.otp.generator.OtpGeneratorImpl
import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.otp.parser.OtpUriParserImpl
import com.xinto.mauth.otp.transformer.KeyTransformer
import com.xinto.mauth.otp.transformer.KeyTransformerImpl
import org.apache.commons.codec.binary.Base32
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val otpModule = module {

    fun provideOtpGenerator(): OtpGenerator {
        return OtpGeneratorImpl()
    }

    fun provideKeyTransformer(): KeyTransformer {
        return KeyTransformerImpl(Base32())
    }

    fun provideUriParser(): OtpUriParser {
        return OtpUriParserImpl()
    }

    singleOf(::provideOtpGenerator)
    singleOf(::provideKeyTransformer)
    singleOf(::provideUriParser)
}