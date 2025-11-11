package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    val gameState by vm.gameState.collectAsState()

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.Start){
                    Text("Type: ${gameState.gameType}")
                    Text("Turns: 10")  //TODO: currently hardcoded as there is no option to change these
                    Text("Interval: 2s")
                    Text("N: 2")
                }


                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { vm.setGameType(GameType.Visual) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(gameState.gameType == GameType.Visual) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.inverseOnSurface
                        )
                    ) {
                        Text(
                            text = "Visual",
                            color =
                                if(gameState.gameType == GameType.Visual) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = { vm.setGameType(GameType.Audio) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(gameState.gameType == GameType.Audio) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.inverseOnSurface
                        )
                    ) {
                        Text(
                            text = "Audio",
                            color =
                                if(gameState.gameType == GameType.Audio) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = { vm.setGameType(GameType.AudioVisual) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(gameState.gameType == GameType.AudioVisual) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.inverseOnSurface
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




            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button( onClick = onStartGame ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Start Game".uppercase(),
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
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