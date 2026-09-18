package com.southboundstudios.tripcheq.di

import com.southboundstudios.tripcheq.presentation.TripCalculatorViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        TripCalculatorViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
}