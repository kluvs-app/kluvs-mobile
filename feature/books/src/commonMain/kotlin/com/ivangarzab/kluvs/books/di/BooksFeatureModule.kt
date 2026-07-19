package com.ivangarzab.kluvs.books.di

import com.ivangarzab.kluvs.books.domain.AssignShelfUseCase
import com.ivangarzab.kluvs.books.domain.GetShelfUseCase
import com.ivangarzab.kluvs.books.domain.RemoveFromShelfUseCase
import com.ivangarzab.kluvs.books.domain.SearchBooksUseCase
import com.ivangarzab.kluvs.books.domain.ToggleLikeUseCase
import com.ivangarzab.kluvs.books.presentation.BooksViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val booksFeatureModule = module {
    // Use Cases
    factoryOf(::GetShelfUseCase)
    factoryOf(::AssignShelfUseCase)
    factoryOf(::RemoveFromShelfUseCase)
    factoryOf(::ToggleLikeUseCase)
    factoryOf(::SearchBooksUseCase)

    // ViewModels
    factoryOf(::BooksViewModel)
}
