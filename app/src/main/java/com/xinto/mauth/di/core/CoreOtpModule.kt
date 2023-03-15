package com.xinto.mauth.di.core

import com.xinto.mauth.core.otp.generator.DefaultOtpGenerator
import com.xinto.mauth.core.otp.generator.OtpGenerator
import com.xinto.mauth.core.otp.parser.DefaultOtpUriParser
import com.xinto.mauth.core.otp.parser.OtpUriParser
import com.xinto.mauth.core.otp.transformer.DefaultKeyTransformer
import com.xinto.mauth.core.otp.transformer.KeyTransformer
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val CoreOtpModule = module {
    singleOf(::DefaultOtpGenerator) bind OtpGenerator::class
    singleOf(::DefaultOtpUriParser) bind OtpUriParser::class
    singleOf(::DefaultKeyTransformer) bind KeyTransformer::class
}