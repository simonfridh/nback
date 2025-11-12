package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */
@Composable
fun HomeScreen(
    vm: GameViewModel,
    onStartGame: () -> Unit
) {
    val highscore by vm.highscore.collectAsState()
    val nrOfTurns by vm.nrOfTurns.collectAsState()
    val timeInterval by vm.timeInterval.collectAsState()
    val nBack by vm.nBack.collectAsState()
    val gridSize by vm.gridSize.collectAsState()
    val nrOfLetters by vm.nrOfLetters.collectAsState()
    val gameState by vm.gameState.collectAsState()


    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //TOP SECTION (Title)
            Text(
                modifier = Modifier
                    .padding(32.dp, 32.dp, 32.dp, 5.dp),
                text = "N-Back",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp
            )
            Text(
                modifier = Modifier
                    .padding(32.dp, 5.dp, 32.dp, 32.dp),
                text = "High-Score = $highscore",
                style = MaterialTheme.typography.headlineMedium
            )


            //MID SECTION
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Options
                Text(
                    modifier = Modifier
                        .padding(16.dp, 16.dp, 16.dp, 5.dp),
                    text = "Options",
                    style = MaterialTheme.typography.headlineSmall
                )
                settingsBox(
                    text = "Turns: $nrOfTurns",
                    onPlusClick = { vm.increaseNrOfTurns() },
                    onMinusClick = { vm.decreaseNrOfTurns() }
                )
                settingsBox(
                    text = "Interval: $timeInterval s",
                    onPlusClick = { vm.increaseTimeInterval() },
                    onMinusClick = { vm.decreaseTimeInterval() }
                )
                settingsBox(
                    text = "N: $nBack",
                    onPlusClick = { vm.increaseNBack() },
                    onMinusClick = { vm.decreaseNBack() }
                )
                settingsBox(
                    text = "GridSize: $gridSize",
                    onPlusClick = { vm.increaseGridSize() },
                    onMinusClick = { vm.decreaseGridSize() }
                )
                settingsBox(
                    text = "Nr of letters: $nrOfLetters",
                    onPlusClick = { vm.increaseNrOfLetters() },
                    onMinusClick = { vm.decreaseNrOfLetters() }
                )

                //Game mode
                Text(
                    modifier = Modifier
                        .padding(16.dp, 16.dp, 16.dp, 5.dp),
                    text = "Game Mode",
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { vm.setGameType(GameType.Visual) },
                        modifier = Modifier.width(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gameState.gameType == GameType.Visual) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Visual",
                            color =
                                if (gameState.gameType == GameType.Visual) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { vm.setGameType(GameType.Audio) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gameState.gameType == GameType.Audio) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Audio",
                            color =
                                if (gameState.gameType == GameType.Audio) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { vm.setGameType(GameType.AudioVisual) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(gameState.gameType == GameType.AudioVisual) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "AudioVisual",
                            color =
                                if(gameState.gameType == GameType.AudioVisual) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }


            //BOTTOM SECTION (Startbutton)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button( onClick = onStartGame ) {
                    Text(
                        modifier = Modifier
                            .padding(16.dp),
                        text = "START GAME",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun settingsBox( //Total width 272
    text: String,
    onPlusClick: () -> Unit,
    onMinusClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.Center
    ){
        Button(
            onClick = onMinusClick,
            modifier = Modifier
                .fillMaxHeight()
                .width(56.dp),
            shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
        ) {
            Text("-")
        }
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier
                .fillMaxHeight()
                .width(160.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Button(
            onClick = onPlusClick,
            modifier = Modifier
                .fillMaxHeight()
                .width(56.dp),
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
        ) {
            Text("+")
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    Surface {
        HomeScreen(
            FakeVM(),
            onStartGame = {}
        )
    }
}