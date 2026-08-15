
package com.sv.elveloz.ui.shared
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElVelozDateTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (startDateMillis: Long?, endDateMillis: Long?, startTime: String, endTime: String) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    var startTime by remember { mutableStateOf("10 : 30 am") }
    var endTime by remember { mutableStateOf("05 : 30 pm") }
    var isSelectingStartTime by remember { mutableStateOf(true) }

    // Nueva variable que controla si el reloj se abre o se cierra
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Hora",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimeSelectorButton(
                        time = startTime,
                        isSelected = isSelectingStartTime,
                        onClick = {
                            isSelectingStartTime = true
                            showTimePicker = true // <- Al hacer clic, abre el reloj
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TimeSelectorButton(
                        time = endTime,
                        isSelected = !isSelectingStartTime,
                        onClick = {
                            isSelectingStartTime = false
                            showTimePicker = true // <- Al hacer clic, abre el reloj
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier.height(350.dp),
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        selectedDayContainerColor = Color(0xFF222831),
                        dayInSelectionRangeContainerColor = Color(0xFFF0F0F0),
                        dayInSelectionRangeContentColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("Cancelar", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            onConfirm(
                                dateRangePickerState.selectedStartDateMillis,
                                dateRangePickerState.selectedEndDateMillis,
                                startTime,
                                endTime
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222831))
                    ) {
                        Text("Listo", color = Color.White)
                    }
                }
            }
        }
    }

    // --- LÓGICA DEL RELOJ CIRCULAR NATIVO ---
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 10,
            initialMinute = 30,
            is24Hour = false // Falso para que use formato de 12 hrs (AM/PM)
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar", color = Color.Black) }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Matemática sencilla para convertir la hora a tu formato "10 : 30 am"
                    val isPm = timePickerState.hour >= 12
                    val amPm = if (isPm) "pm" else "am"
                    val hour12 = if (timePickerState.hour % 12 == 0) 12 else timePickerState.hour % 12
                    val formattedTime = String.format(Locale.getDefault(), "%02d : %02d %s", hour12, timePickerState.minute, amPm)

                    // Lo guardamos en el botón correcto (Inicio o Fin)
                    if (isSelectingStartTime) {
                        startTime = formattedTime
                    } else {
                        endTime = formattedTime
                    }
                    showTimePicker = false
                }) { Text("Aceptar", color = Color.Black) }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
fun TimeSelectorButton(time: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF222831) else Color.White
    val contentColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isSelected) Color.Transparent else Color.LightGray

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Reloj",
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier
                .height(16.dp)
                .width(1.dp)
                .background(if (isSelected) Color.Gray else Color.LightGray)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text(text = time, color = contentColor, fontWeight = FontWeight.Bold)
        }
    }
}