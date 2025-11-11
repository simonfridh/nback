package mobappdev.example.nback_cimpl.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

@Composable
fun GameScreen(
    vm: GameViewModel,
    onBackToHome: () -> Unit
) {
    val highscore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val gameState by vm.gameState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gridSize = 3 //TODO: Bara en temporär siffra tills vidare

    LaunchedEffect(Unit) {
        vm.startGame()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
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

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Current eventValue is: ${gameState.eventValue}, TURN: ${gameState.turnCount}",
                    textAlign = TextAlign.Center
                )


                //GRID
                for (row in 0 until gridSize) {
                    Row() {
                        for (col in 0 until gridSize) {
                            Column(modifier = Modifier.padding(5.dp)) {
                                var boxLightOn by remember { mutableStateOf(false) }

                                LaunchedEffect(gameState.turnCount) {   //Needed for the flashing animation
                                    if (gameState.eventValue == row * gridSize + col + 1) {
                                        boxLightOn = true
                                        delay(1000)
                                        boxLightOn = false
                                    }
                                    else if (boxLightOn) {
                                        boxLightOn = false  //Solves issue with boxes staying lit up after changing screen mid game
                                    }
                                }
                                val boxColor by animateColorAsState(
                                    targetValue = if (boxLightOn) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,

                                )
                                Box(
                                    modifier = Modifier
                                        .width(112.dp)
                                        .height(112.dp)
                                        .padding(5.dp)
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
                Button(onClick = {
                    // Todo: change this button behaviour
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "Hey! you clicked the audio button"
                        )
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.sound_on),
                        contentDescription = "Sound",
                        modifier = Modifier
                            .height(64.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
                Button(
                    onClick = {
                        // Todo: change this button behaviour
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                message = "Hey! you clicked the visual button",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
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



@Preview
@Composable
fun GameScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface {
        GameScreen(
            FakeVM(),
            onBackToHome = {}
        )
    }
}