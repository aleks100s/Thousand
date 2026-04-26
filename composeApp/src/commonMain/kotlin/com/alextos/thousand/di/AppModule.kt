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
import com.alextos.thousand.data.local.DataStoreKeyValueStorage
import com.alextos.thousand.data.local.KeyValueStorage
import com.alextos.thousand.data.repository.GameRepositoryImpl
import com.alextos.thousand.data.repository.StatisticsRepositoryImpl
import com.alextos.thousand.data.seed.DatabaseSeeder
import com.alextos.thousand.data.service.StorageServiceImpl
import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.repository.StatisticsRepository
import com.alextos.thousand.domain.service.ShakeDeviceObserver
import com.alextos.thousand.domain.service.StorageService
import com.alextos.thousand.domain.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.usecase.game.CreateGameUseCase
import com.alextos.thousand.domain.usecase.game.CreateRematchUseCase
import com.alextos.thousand.domain.usecase.game.DeleteGameUseCase
import com.alextos.thousand.domain.game.FindCurrentPlayerUseCase
import com.alextos.thousand.domain.game.GameServer
import com.alextos.thousand.domain.usecase.game.GetAllGamesUseCase
import com.alextos.thousand.domain.usecase.game.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.game.LoadGameUseCase
import com.alextos.thousand.domain.usecase.game.LoadGameTurnsUseCase
import com.alextos.thousand.domain.usecase.statistics.DiceStatisticsUseCase
import com.alextos.thousand.domain.usecase.statistics.GamesStatisticsUseCase
import com.alextos.thousand.domain.usecase.statistics.RollsStatisticsUseCase
import com.alextos.thousand.domain.usecase.statistics.TurnsStatisticsUseCase
import com.alextos.thousand.domain.game.RollTheDiceUseCase
import com.alextos.thousand.domain.game.SaveTurnUseCase
import com.alextos.thousand.domain.usecase.game.SaveUserUseCase
import com.alextos.thousand.domain.game.UpdateGameUseCase
import com.alextos.thousand.presentation.game.create_game.CreateGameViewModel
import com.alextos.thousand.presentation.game.game_list.GamesListViewModel
import com.alextos.thousand.presentation.game.game_score.GameScoreViewModel
import com.alextos.thousand.presentation.game.play_game.PlayGameViewModel
import com.alextos.thousand.presentation.statistics.DiceStatisticsViewModel
import com.alextos.thousand.presentation.statistics.GamesStatisticsViewModel
import com.alextos.thousand.presentation.statistics.RollsStatisticsViewModel
import com.alextos.thousand.presentation.statistics.TurnsStatisticsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

fun appModule(shakeDeviceObserver: ShakeDeviceObserver) = module {
    includes(platformModule)

    single<UserDao> { get<ThousandDatabase>().userDao() }
    single<GameDao> { get<ThousandDatabase>().gameDao() }
    single<PlayerDao> { get<ThousandDatabase>().playerDao() }
    single<TurnDao> { get<ThousandDatabase>().turnDao() }
    single<DiceRollDao> { get<ThousandDatabase>().diceRollDao() }
    single<DieDao> { get<ThousandDatabase>().dieDao() }
    single<TurnEffectDao> { get<ThousandDatabase>().turnEffectDao() }
    single<TurnResultDao> { get<ThousandDatabase>().turnResultDao() }
    single<KeyValueStorage> { DataStoreKeyValueStorage(get()) }
    single<StorageService> { StorageServiceImpl(get()) }
    single<GameRepository> { GameRepositoryImpl(get(), get(), get(), get(), get(), get(), get(), get()) }
    single<StatisticsRepository> { StatisticsRepositoryImpl(get(), get()) }
    factory { GetAllGamesUseCase(get()) }
    factory { GetAllUsersUseCase(get()) }
    factory { DiceStatisticsUseCase(get()) }
    factory { GamesStatisticsUseCase(get()) }
    factory { RollsStatisticsUseCase(get()) }
    factory { TurnsStatisticsUseCase(get()) }
    factory { LoadGameUseCase(get()) }
    factory { LoadGameTurnsUseCase(get()) }
    factory { SaveUserUseCase(get()) }
    viewModelOf(::CreateGameViewModel)
    viewModelOf(::GamesListViewModel)
    viewModelOf(::PlayGameViewModel)
    viewModelOf(::GameScoreViewModel)
    viewModelOf(::DiceStatisticsViewModel)
    viewModelOf(::GamesStatisticsViewModel)
    viewModelOf(::RollsStatisticsViewModel)
    viewModelOf(::TurnsStatisticsViewModel)
    single { DatabaseSeeder(get()) }
    factory { CalculateDiceRollScoreUseCase() }
    factory { CreateGameUseCase(get()) }
    factory { CreateRematchUseCase(get()) }
    factory { FindCurrentPlayerUseCase() }
    factory { RollTheDiceUseCase() }
    factory { SaveTurnUseCase(get()) }
    factory { UpdateGameUseCase(get()) }
    factory { DeleteGameUseCase(get()) }
    single { shakeDeviceObserver }
    factory { GameServer(get(), get(), get(), get(), get(), get()) }
}
