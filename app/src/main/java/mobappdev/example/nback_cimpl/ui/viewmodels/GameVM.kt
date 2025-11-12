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
    val nrOfTurns: StateFlow<Int>
    val timeInterval: StateFlow<Int>
    val nBack: StateFlow<Int>
    val gridSize: StateFlow<Int>
    val nrOfLetters: StateFlow<Int>

    fun setGameType(gameType: GameType)
    fun increaseNrOfTurns()
    fun decreaseNrOfTurns()
    fun increaseTimeInterval()
    fun decreaseTimeInterval()
    fun increaseNBack()
    fun decreaseNBack()
    fun increaseGridSize()
    fun decreaseGridSize()
    fun increaseNrOfLetters()
    fun decreaseNrOfLetters()

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

    private val _nrOfTurns = MutableStateFlow(10)
    override val nrOfTurns: StateFlow<Int>
        get() = _nrOfTurns

    private val _timeInterval = MutableStateFlow(2)
    override val timeInterval: StateFlow<Int>
        get() = _timeInterval

    private val _nBack = MutableStateFlow(2)
    override val nBack: StateFlow<Int>
        get() = _nBack

    private val _gridSize = MutableStateFlow(3)
    override val gridSize: StateFlow<Int>
        get() = _gridSize

    private val _nrOfLetters = MutableStateFlow(9)
    override val nrOfLetters: StateFlow<Int>
        get() = _nrOfLetters

    private var job: Job? = null  // coroutine job for the game event
    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var visualEvents = emptyArray<Int>()  // Array with all visual events
    private var audioEvents = emptyArray<Int>()  // Array with all audio events



    override fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    //To limit the users input on the settings, simple + and - functions are used
    //Looks a bit messy :D
    override fun increaseNrOfTurns() {
        viewModelScope.launch{ userPreferencesRepository.saveNrOfTurns(_nrOfTurns.value + 1) }
    }
    override fun decreaseNrOfTurns() {
        if(_nrOfTurns.value <= 1) return
        viewModelScope.launch{ userPreferencesRepository.saveNrOfTurns(_nrOfTurns.value - 1) }
    }
    override fun increaseTimeInterval() {
        viewModelScope.launch{ userPreferencesRepository.saveTimeInterval(_timeInterval.value + 1) }
    }
    override fun decreaseTimeInterval() {
        if(_timeInterval.value <= 1) return
        viewModelScope.launch{ userPreferencesRepository.saveTimeInterval(_timeInterval.value - 1) }
    }
    override fun increaseNBack() {
        viewModelScope.launch{ userPreferencesRepository.saveNBack(_nBack.value + 1) }
    }
    override fun decreaseNBack() {
        if(_nBack.value <= 1) return
        viewModelScope.launch{ userPreferencesRepository.saveNBack(_nBack.value - 1) }
    }
    override fun increaseGridSize() {
        if(_gridSize.value >= 10) return
        viewModelScope.launch{ userPreferencesRepository.saveGridSize(_gridSize.value + 1) }
    }
    override fun decreaseGridSize() {
        if(_gridSize.value <= 2) return
        viewModelScope.launch{ userPreferencesRepository.saveGridSize(_gridSize.value - 1) }
    }
    override fun increaseNrOfLetters() {
        if(_gridSize.value >= 26) return
        viewModelScope.launch{ userPreferencesRepository.saveNrOfLetters(_nrOfLetters.value + 1) }
    }
    override fun decreaseNrOfLetters() {
        if(_nrOfLetters.value <= 3) return
        viewModelScope.launch{ userPreferencesRepository.saveNrOfLetters(_nrOfLetters.value - 1) }
    }


    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop

        _gameState.value = _gameState.value.copy( //Resetting gamevalues since they stay
            visualEventValue = -1,
            audioEventValue = ' ',
            turnCount = 0,
            visualGuessStatus = GuessStatus.NotGuessed,
            audioGuessStatus = GuessStatus.NotGuessed
            )
        _score.value = 0

        visualEvents = nBackHelper.generateNBackString( //Fetching from C-Model
            size = _nrOfTurns.value,
            combinations = _gridSize.value * _gridSize.value,
            percentMatch = 30,
            nBack= _nBack.value
        ).toList().toTypedArray()

        audioEvents = nBackHelper.generateNBackString(  //Fetching from C-Model
            size = _nrOfTurns.value,
            combinations = _nrOfLetters.value,
            percentMatch = 30,
            nBack= _nBack.value
        ).toList().toTypedArray().reversedArray() // Reversing to make sure its not the exact same as above

        Log.d("GameVM", "The following visual sequence was generated: ${visualEvents.contentToString()}")
        Log.d("GameVM", "The following audio sequence was generated: ${audioEvents.contentToString()}")

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

    override fun checkVisualMatch() {
        val currentEventValue = _gameState.value.visualEventValue
        val currentTurn = _gameState.value.turnCount
        val guessStatus = _gameState.value.visualGuessStatus

        if(gameState.value.gameType == GameType.Audio) return
        if(currentTurn - _nBack.value - 1 < 0 || guessStatus != GuessStatus.NotGuessed) return

        if(currentEventValue == visualEvents[currentTurn-_nBack.value-1]){
            _score.value++
            _gameState.value = _gameState.value.copy(visualGuessStatus = GuessStatus.Correct)
        }
        else _gameState.value = _gameState.value.copy(visualGuessStatus = GuessStatus.Incorrect)
    }

    override fun checkAudioMatch() {
        val currentEventValue = _gameState.value.audioEventValue
        val currentTurn = _gameState.value.turnCount
        val guessStatus = _gameState.value.audioGuessStatus

        if(gameState.value.gameType == GameType.Visual) return
        if(currentTurn - _nBack.value - 1 < 0 || guessStatus != GuessStatus.NotGuessed) return

        if((currentEventValue - 'A' + 1) == audioEvents[currentTurn-_nBack.value-1]){
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
            delay( (_timeInterval.value*1000).toLong() )
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
            delay( (_timeInterval.value*1000).toLong() )
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
            delay( (_timeInterval.value*1000).toLong() )
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
        viewModelScope.launch {
            userPreferencesRepository.nrofturns.collect {
                _nrOfTurns.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.timeinterval.collect {
                _timeInterval.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.nback.collect {
                _nBack.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.gridsize.collect {
                _gridSize.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.nrofletters.collect {
                _nrOfLetters.value = it
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

enum class GuessStatus{ //Used for locking buttons and showing feedback in the ui
    NotGuessed,
    Correct,
    Incorrect
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,
    val visualEventValue: Int = -1,
    val audioEventValue: Char = ' ',
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
    override val nrOfTurns: StateFlow<Int>
        get() = MutableStateFlow(10).asStateFlow()
    override val timeInterval: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val gridSize: StateFlow<Int>
        get() = MutableStateFlow(3).asStateFlow()
    override val nBack: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val nrOfLetters: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()

    override fun setGameType(gameType: GameType) {}
    override fun increaseNrOfTurns() {}
    override fun decreaseNrOfTurns() {}
    override fun increaseTimeInterval() {}
    override fun decreaseTimeInterval() {}
    override fun increaseNBack() {}
    override fun decreaseNBack() {}
    override fun increaseGridSize() {}
    override fun decreaseGridSize() {}
    override fun increaseNrOfLetters() {}
    override fun decreaseNrOfLetters() {}
    override fun startGame() {}
    override fun checkVisualMatch() {}
    override fun checkAudioMatch() {}
}