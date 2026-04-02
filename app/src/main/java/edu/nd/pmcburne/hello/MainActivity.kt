package edu.nd.pmcburne.hello

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import edu.nd.pmcburne.hello.ui.theme.MyApplicationTheme

val UVANavy = Color(0xFF232F5F)
val UVAOrange = Color(0xFFE57200)

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(viewModel, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredLocations = remember(uiState.locations, uiState.selectedTag) {
        uiState.locations.filter { location ->
            location.tags.split(",").map { it.trim() }.contains(uiState.selectedTag)
        }
    }

    val uvaCenter = LatLng(38.0336, -78.5080)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uvaCenter, 14.5f)
    }

    Box(modifier = modifier.fillMaxSize()) {

        // --- Map fills full screen ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            filteredLocations.forEach { location ->
                key(location.id) {
                    val markerState = rememberMarkerState(
                        position = LatLng(location.latitude, location.longitude)
                    )
                    MarkerInfoWindowContent(
                        state = markerState,
                        title = location.name,
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.White)
                                .padding(12.dp)
                                .widthIn(max = 260.dp)
                        ) {
                            Text(
                                text = location.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = UVANavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = location.description,
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }

        // --- Floating overlay: title + dropdown ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Card(
//                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = UVANavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "UVA Grounds Map",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown card
            TagDropdown(
                tags = uiState.allTags,
                selectedTag = uiState.selectedTag,
                onTagSelected = { viewModel.selectTag(it) }
            )


            // Location count badge
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = UVAOrange)
            ) {
                Text(
                    text = "  ${filteredLocations.size} location${if (filteredLocations.size != 1) "s" else ""}  ",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

    }
}

@Composable
fun TagDropdown(
    tags: List<String>,
    selectedTag: String,
    onTagSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.padding(8.dp)) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = UVANavy),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("${selectedTag.uppercase()}", color = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("▼", color = Color.White, fontSize = 10.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            tags.forEach { tag ->
                DropdownMenuItem(
                    text = { Text(tag.uppercase()) },
                    onClick = {
                        onTagSelected(tag)
                        expanded = false
                    }
                )
            }
        }
    }
}