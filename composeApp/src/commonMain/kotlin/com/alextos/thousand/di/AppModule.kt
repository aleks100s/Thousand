package com.alextos.thousand.di

import com.alextos.thousand.data.ThousandDatabase
import com.alextos.thousand.data.dao.DiceRollDao
import com.alextos.thousand.data.dao.DieDao
import com.alextos.thousand.data.dao.GameDao
import com.alextos.thousand.data.dao.PlayerDao
import com.alextos.thousand.data.dao.TurnDao
import com.alextos.thousand.data.dao.TurnEffectDao
import com.alextos.thousand.data.dao.UserDao
import com.alextos.thousand.data.repository.GameRepositoryImpl
import com.alextos.thousand.data.seed.DatabaseSeeder
import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.usecase.GetAllGamesUseCase
import com.alextos.thousand.domain.usecase.LoadGameUseCase
import com.alextos.thousand.presentation.game.game_list.GamesListViewModel
import com.alextos.thousand.presentation.game.game_score.GameScoreViewModel
import com.alextos.thousand.presentation.game.play_game.PlayGameViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
    includes(platformModule)

    single<UserDao> { get<ThousandDatabase>().userDao() }
    single<GameDao> { get<ThousandDatabase>().gameDao() }
    single<PlayerDao> { get<ThousandDatabase>().playerDao() }
    single<TurnDao> { get<ThousandDatabase>().turnDao() }
    single<DiceRollDao> { get<ThousandDatabase>().diceRollDao() }
    single<DieDao> { get<ThousandDatabase>().dieDao() }
    single<TurnEffectDao> { get<ThousandDatabase>().turnEffectDao() }
    single<GameRepository> { GameRepositoryImpl(get(), get(), get(), get(), get(), get()) }
    factory { GetAllGamesUseCase(get()) }
    factory { LoadGameUseCase(get()) }
    viewModelOf(::GamesListViewModel)
    viewModelOf(::PlayGameViewModel)
    viewModelOf(::GameScoreViewModel)
    single { DatabaseSeeder(get()) }
}
