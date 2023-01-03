package app.codinguyy.coroutineexample.di

import app.codinguyy.coroutineexample.FirstFragmentViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModules = module {

    viewModel { FirstFragmentViewModel() }
}
