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
import com.alextos.thousand.data.service.StorageServiceImpl
import com.alextos.thousand.domain.repository.GameRepository
import com.alextos.thousand.domain.repository.StatisticsRepository
import com.alextos.thousand.domain.service.ShakeDeviceObserver
import com.alextos.thousand.domain.service.StorageService
import com.alextos.thousand.domain.usecase.game.ApplyDiceRollRestrictionsUseCase
import com.alextos.thousand.domain.usecase.game.CalculateDiceRollScoreUseCase
import com.alextos.thousand.domain.usecase.game.DetermineAvailableButtonsUseCase
import com.alextos.thousand.domain.usecase.game.crud.CreateGameUseCase
import com.alextos.thousand.domain.usecase.game.crud.CreateRematchUseCase
import com.alextos.thousand.domain.usecase.game.crud.DeleteGameUseCase
import com.alextos.thousand.domain.usecase.user.DeleteUserUseCase
import com.alextos.thousand.domain.usecase.game.FindCurrentPlayerUseCase
import com.alextos.thousand.domain.usecase.game.FormatTurnEffectUseCase
import com.alextos.thousand.domain.usecase.game.MakeBotReplyUseCase
import com.alextos.thousand.domain.usecase.game.MakeBotDecisionUseCase
import com.alextos.thousand.domain.usecase.game.server.DefaultGameServer
import com.alextos.thousand.domain.usecase.game.server.TutorialGameServer
import com.alextos.thousand.domain.usecase.user.GenerateBotNameUseCase
import com.alextos.thousand.domain.usecase.game.crud.GetAllGamesUseCase
import com.alextos.thousand.domain.usecase.user.GetAllUsersUseCase
import com.alextos.thousand.domain.usecase.game.LoadGameUseCase
import com.alextos.thousand.domain.usecase.game.LoadGameTurnsUseCase
import com.alextos.thousand.domain.usecase.statistics.DiceStatisticsUseCase
import com.alextos.thousand.domain.usecase.statistics.GamesStatisticsUseCase
import com.alextos.thousand.domain.usecase.statistics.RollsStatisticsUseCase
import com.alextos.thousand.domain.usecase.statistics.TurnsStatisticsUseCase
import com.alextos.thousand.domain.usecase.game.RollTheDiceUseCase
import com.alextos.thousand.domain.usecase.game.SaveTurnUseCase
import com.alextos.thousand.domain.usecase.game.TutorialRollUseCase
import com.alextos.thousand.domain.usecase.user.SaveUserUseCase
import com.alextos.thousand.domain.usecase.game.crud.UpdateGameUseCase
import com.alextos.thousand.presentation.game.create_game.CreateGameViewModel
import com.alextos.thousand.presentation.game.game_list.GamesListViewModel
import com.alextos.thousand.presentation.other.game_rules.GameRulesViewModel
import com.alextos.thousand.presentation.game.game_results.GameResultsViewModel
import com.alextos.thousand.presentation.game.game_score.GameScoreViewModel
import com.alextos.thousand.presentation.game.play_game.PlayGameViewModel
import com.alextos.thousand.presentation.other.tutorial_game.TutorialGameViewModel
import com.alextos.thousand.presentation.onboarding.FirstUserViewModel
import com.alextos.thousand.presentation.other.users.UsersViewModel
import com.alextos.thousand.presentation.other.statistics.dice_statistics.DiceStatisticsViewModel
import com.alextos.thousand.presentation.other.statistics.games_statistics.GamesStatisticsViewModel
import com.alextos.thousand.presentation.other.statistics.roll_statistics.RollsStatisticsViewModel
import com.alextos.thousand.presentation.other.statistics.turn_statistics.TurnsStatisticsViewModel
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
    viewModelOf(::FirstUserViewModel)
    viewModelOf(::GamesListViewModel)
    viewModelOf(::GameRulesViewModel)
    viewModelOf(::TutorialGameViewModel)
    viewModelOf(::PlayGameViewModel)
    viewModelOf(::GameScoreViewModel)
    viewModelOf(::GameResultsViewModel)
    viewModelOf(::UsersViewModel)
    viewModelOf(::DiceStatisticsViewModel)
    viewModelOf(::GamesStatisticsViewModel)
    viewModelOf(::RollsStatisticsViewModel)
    viewModelOf(::TurnsStatisticsViewModel)
    factory { CalculateDiceRollScoreUseCase() }
    factory { ApplyDiceRollRestrictionsUseCase() }
    factory { CreateGameUseCase(get()) }
    factory { CreateRematchUseCase(get()) }
    factory { GenerateBotNameUseCase() }
    factory { FindCurrentPlayerUseCase() }
    factory { FormatTurnEffectUseCase() }
    factory { RollTheDiceUseCase() }
    factory { TutorialRollUseCase() }
    factory { SaveTurnUseCase(get()) }
    factory { UpdateGameUseCase(get()) }
    factory { DeleteGameUseCase(get()) }
    factory { DeleteUserUseCase(get()) }
    single { shakeDeviceObserver }
    factory { DefaultGameServer(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { TutorialGameServer(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { MakeBotDecisionUseCase() }
    factory { MakeBotReplyUseCase() }
    factory { DetermineAvailableButtonsUseCase() }
}
