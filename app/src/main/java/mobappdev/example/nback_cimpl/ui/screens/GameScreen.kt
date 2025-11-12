package mobappdev.example.nback_cimpl.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import mobappdev.example.nback_cimpl.ui.viewmodels.GuessStatus
import java.util.Locale

@Composable
fun GameScreen(
    vm: GameViewModel,
    onBackToHome: () -> Unit
) {
    val score by vm.score.collectAsState()
    val gameState by vm.gameState.collectAsState()
    val gridSize = vm.gridSize.collectAsState()

    //TTS
    val context = LocalContext.current
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    //runs when screen is loaded
    LaunchedEffect(Unit) {
        if (gameState.gameType == GameType.Audio || gameState.gameType == GameType.AudioVisual) {
            if (tts.value == null) {
                tts.value = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts.value?.language = Locale.US
                    }
                }
            }
        }
        vm.startGame()
    }

    //runs when screen is closed
    DisposableEffect(Unit) {
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
        }
    }

    //TTS
    if(gameState.gameType == GameType.Audio || gameState.gameType == GameType.AudioVisual){
        LaunchedEffect(gameState.turnCount) {
            tts.value?.speak("${gameState.audioEventValue}", TextToSpeech.QUEUE_FLUSH, null, "${gameState.turnCount}")
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //TOP SECTION (Back button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button( onClick = onBackToHome ) {
                    Text("BACK")
                }
            }

            //MID SECTION
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                //Game info
                Row(
                    modifier = Modifier.fillMaxWidth(0.95f).padding(24.dp, 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Visual: ${gameState.visualEventValue}",
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "Audio: ${gameState.audioEventValue}",
                        textAlign = TextAlign.Start
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(0.95f).padding(24.dp, 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TURN: ${gameState.turnCount}",
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "SCORE: $score",
                        textAlign = TextAlign.End
                    )
                }

                //GRID
                for (row in 0 until gridSize.value) {
                    Row {
                        for (col in 0 until gridSize.value) {
                            Column(modifier = Modifier.padding((20 / gridSize.value).dp) ){

                                var boxLightOn by remember { mutableStateOf(false) }
                                LaunchedEffect(gameState.turnCount) {   //runs everytime turncount updates
                                    if (gameState.visualEventValue == row * gridSize.value + col + 1) {
                                        boxLightOn = true
                                        delay(1000)
                                        boxLightOn = false
                                    }
                                    else if (boxLightOn) {
                                        boxLightOn = false  //solves issue with boxes staying on after changing screen
                                    }
                                }

                                val boxColor by animateColorAsState(
                                    targetValue = if (boxLightOn) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                )
                                Box(
                                    modifier = Modifier
                                        .size( (336 / gridSize.value).dp)
                                        .background(boxColor)
                                )
                            }
                        }
                    }
                }
            }


            //BOTTOM SECTION (BUTTONS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(gameState.gameType == GameType.Audio || gameState.gameType == GameType.AudioVisual)
                {
                    Button(
                        onClick = { vm.checkAudioMatch() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (gameState.audioGuessStatus) {
                                GuessStatus.Correct -> Color.Green
                                GuessStatus.Incorrect -> Color.Red
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.sound_on),
                            contentDescription = "Sound",
                            modifier = Modifier
                                .height(64.dp)
                                .aspectRatio(3f / 2f)
                        )
                    }
                }

                if(gameState.gameType == GameType.Visual || gameState.gameType == GameType.AudioVisual)
                {
                    Button(
                        onClick = { vm.checkVisualMatch() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (gameState.visualGuessStatus) {
                                GuessStatus.Correct -> Color.Green
                                GuessStatus.Incorrect -> Color.Red
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.visual),
                            contentDescription = "Visual",
                            modifier = Modifier
                                .height(64.dp)
                                .aspectRatio(3f / 2f)
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun GameScreenPreview() {
    Surface {
        GameScreen(
            FakeVM(),
            onBackToHome = {}
        )
    }
}