package com.alextos.thousand.di

import com.alextos.thousand.data.ThousandDao
import com.alextos.thousand.data.ThousandDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
    includes(platformModule)

    single<ThousandDao> { get<ThousandDatabase>().thousandDao() }
}
