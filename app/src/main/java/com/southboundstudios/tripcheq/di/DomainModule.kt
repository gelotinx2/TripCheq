package com.southboundstudios.tripcheq.di

import com.southboundstudios.tripcheq.domain.usecase.CalculateFuelCostUseCase
import com.southboundstudios.tripcheq.domain.usecase.CalculateTollCostUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { CalculateFuelCostUseCase() }
    factory { CalculateTollCostUseCase(get()) }
}