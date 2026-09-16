package com.southboundstudios.tripcheq.di

import com.southboundstudios.tripcheq.domain.usecase.CalculateFuelCostUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { CalculateFuelCostUseCase() }
}