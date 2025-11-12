package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int

    fun setGameType(gameType: GameType)
    fun startGame()

    fun checkVisualMatch()
    fun checkAudioMatch()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    override val nBack: Int = 2  // TODO: nBack is currently hardcoded
    private var job: Job? = null  // coroutine job for the game event
    private val eventInterval: Long = 2000L  // 2000 ms (2s)
    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var visualEvents = emptyArray<Int>()  // Array with all visual events
    private var audioEvents = emptyArray<Int>()  // Array with all audio events

    override fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop

        _gameState.value = _gameState.value.copy(
            visualEventValue = -1,
            audioEventValue = '0',
            turnCount = 0,
            visualGuessStatus = GuessStatus.NotGuessed,
            audioGuessStatus = GuessStatus.NotGuessed
            )
        _score.value = 0

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        visualEvents = nBackHelper.generateNBackString(10, 9, 30, nBack).toList().toTypedArray() // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        audioEvents = visualEvents.reversedArray()

        Log.d("GameVM", "The following sequence was generated: ${visualEvents.contentToString()}")
        Log.d("GameVM", "The following sequence was generated: ${audioEvents.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame(audioEvents)
                GameType.AudioVisual -> runAudioVisualGame(audioEvents, visualEvents)
                GameType.Visual -> runVisualGame(visualEvents)
            }
            if(_score.value > _highscore.value) {
                userPreferencesRepository.saveHighScore(_score.value)
            }
        }
    }

    /**
     * Todo: This function should check if there is a match when the user presses a match button
     * Make sure the user can only register a match once for each event.
     */
    override fun checkVisualMatch() {
        val currentEventValue = _gameState.value.visualEventValue
        val currentTurn = _gameState.value.turnCount
        val guessStatus = _gameState.value.visualGuessStatus

        if(currentTurn-nBack-1 < 0 || guessStatus != GuessStatus.NotGuessed) return

        if(currentEventValue == visualEvents[currentTurn-nBack-1]){
            _score.value++
            _gameState.value = _gameState.value.copy(visualGuessStatus = GuessStatus.Correct)
        }
        else _gameState.value = _gameState.value.copy(visualGuessStatus = GuessStatus.Incorrect)
    }

    override fun checkAudioMatch() {
        val currentEventValue = _gameState.value.audioEventValue
        val currentTurn = _gameState.value.turnCount
        val guessStatus = _gameState.value.audioGuessStatus

        if(currentTurn-nBack-1 < 0 || guessStatus != GuessStatus.NotGuessed) return
        if((currentEventValue - 'A' + 1) == audioEvents[currentTurn-nBack-1]){
            _score.value++
            _gameState.value = _gameState.value.copy(audioGuessStatus = GuessStatus.Correct)
        }
        else _gameState.value = _gameState.value.copy(audioGuessStatus = GuessStatus.Incorrect)
    }

    private suspend fun runAudioGame(events: Array<Int>) {
        delay(2000)

        for (value in events) {
            _gameState.value = _gameState.value.copy(
                audioEventValue = ('A' + value - 1),          //Convert to letter
                turnCount = _gameState.value.turnCount + 1,
                audioGuessStatus = GuessStatus.NotGuessed
            )
            delay(eventInterval)
        }
    }

    private suspend fun runVisualGame(events: Array<Int>){
        delay(2000)

        for (value in events) {
            _gameState.value = _gameState.value.copy(
                visualEventValue = value,
                turnCount = _gameState.value.turnCount + 1,
                visualGuessStatus = GuessStatus.NotGuessed
            )
            delay(eventInterval)
        }
    }

    private suspend fun runAudioVisualGame(audioEvents: Array<Int>, visualEvents: Array<Int>){
        delay(2000)

        for (i in 0 until visualEvents.size) {
            _gameState.value = _gameState.value.copy(
                audioEventValue = ('A' + audioEvents[i] - 1),
                visualEventValue = visualEvents[i],
                turnCount = _gameState.value.turnCount + 1,
                visualGuessStatus = GuessStatus.NotGuessed,
                audioGuessStatus = GuessStatus.NotGuessed
            )
            delay(eventInterval)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

enum class GuessStatus{ //Used for locking and showing feedback in the ui
    NotGuessed,
    Correct,
    Incorrect
}


data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val visualEventValue: Int = -1,  // The value of the array string
    val audioEventValue: Char = '0',
    val turnCount: Int = 0,
    val visualGuessStatus: GuessStatus = GuessStatus.NotGuessed,
    val audioGuessStatus: GuessStatus = GuessStatus.NotGuessed
)

class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: Int
        get() = 2

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkVisualMatch() {
    }

    override fun checkAudioMatch() {
    }
}