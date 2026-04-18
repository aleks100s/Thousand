package com.alextos.thousand.di

import com.alextos.thousand.data.ThousandDatabase
import com.alextos.thousand.data.dao.DiceRollDao
import com.alextos.thousand.data.dao.DieDao
import com.alextos.thousand.data.dao.GameDao
import com.alextos.thousand.data.dao.PlayerDao
import com.alextos.thousand.data.dao.TurnDao
import com.alextos.thousand.data.dao.TurnEffectDao
import com.alextos.thousand.data.dao.TurnResultDao
import com.alextos.thousand.data.dao.UserDao
import com.alextos.thousand.data.repository.GameRepositoryImpl
import com.alextos.thousand.data.seed.DatabaseSeeder
import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.usecase.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.usecase.CreateGameUseCase
import com.alextos.thousand.domain.usecase.FindCurrentPlayerUseCase
import com.alextos.thousand.domain.usecase.GetAllGamesUseCase
import com.alextos.thousand.domain.usecase.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.LoadGameUseCase
import com.alextos.thousand.domain.usecase.LoadGameTurnsUseCase
import com.alextos.thousand.domain.usecase.RollTheDiceUseCase
import com.alextos.thousand.domain.usecase.SaveTurnUseCase
import com.alextos.thousand.domain.usecase.SaveUserUseCase
import com.alextos.thousand.presentation.game.create_game.CreateGameViewModel
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
    single<TurnResultDao> { get<ThousandDatabase>().turnResultDao() }
    single<GameRepository> { GameRepositoryImpl(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { GetAllGamesUseCase(get()) }
    factory { GetAllUsersUseCase(get()) }
    factory { LoadGameUseCase(get()) }
    factory { LoadGameTurnsUseCase(get()) }
    factory { SaveUserUseCase(get()) }
    viewModelOf(::CreateGameViewModel)
    viewModelOf(::GamesListViewModel)
    viewModelOf(::PlayGameViewModel)
    viewModelOf(::GameScoreViewModel)
    single { DatabaseSeeder(get()) }
    factory { CalculateDiceRollScoreUseCase() }
    factory { CreateGameUseCase(get()) }
    factory { FindCurrentPlayerUseCase() }
    factory { RollTheDiceUseCase() }
    factory { SaveTurnUseCase(get()) }
}
