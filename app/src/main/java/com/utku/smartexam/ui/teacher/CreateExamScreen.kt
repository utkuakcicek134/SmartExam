package com.utku.smartexam.ui.teacher

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.utku.smartexam.ui.components.ErrorMessage
import com.utku.smartexam.ui.components.SmartExamButton
import com.utku.smartexam.ui.components.SmartExamTextField
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScreen(
    teacherId: Long,
    examId: Long? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel()
) {
    val state by viewModel.createExamState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(teacherId, examId) {
        viewModel.setTeacherForExam(teacherId)
        if (examId != null && examId > 0) {
            viewModel.loadExamForEdit(examId)
        }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onSaveSuccess()
            viewModel.resetCreateExamState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Exam" else "Create Exam") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetCreateExamState()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                state.error?.let { error ->
                    ErrorMessage(message = error)
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Exam Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        SmartExamTextField(
                            value = state.examName,
                            onValueChange = viewModel::updateExamName,
                            label = "Exam Name"
                        )

                        SmartExamTextField(
                            value = state.subject,
                            onValueChange = viewModel::updateSubject,
                            label = "Subject"
                        )

                        SmartExamTextField(
                            value = state.gradeLevel,
                            onValueChange = viewModel::updateGradeLevel,
                            label = "Grade Level"
                        )
                    }
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedCard(
                            onClick = {
                                val calendar = Calendar.getInstance().apply {
                                    timeInMillis = state.examDate
                                }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day)
                                        viewModel.updateExamDate(calendar.timeInMillis)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Exam Date",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = viewModel.formatDate(state.examDate),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedCard(
                                onClick = {
                                    val parts = state.startTime.split(":")
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            viewModel.updateStartTime(
                                                String.format("%02d:%02d", hour, minute)
                                            )
                                        },
                                        parts.getOrNull(0)?.toIntOrNull() ?: 9,
                                        parts.getOrNull(1)?.toIntOrNull() ?: 0,
                                        true
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Start Time",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = state.startTime,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            OutlinedCard(
                                onClick = {
                                    val parts = state.endTime.split(":")
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            viewModel.updateEndTime(
                                                String.format("%02d:%02d", hour, minute)
                                            )
                                        },
                                        parts.getOrNull(0)?.toIntOrNull() ?: 12,
                                        parts.getOrNull(1)?.toIntOrNull() ?: 0,
                                        true
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "End Time",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = state.endTime,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = state.durationMinutes.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { duration ->
                                    viewModel.updateDuration(duration)
                                }
                            },
                            label = { Text("Duration (minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = state.passingScore.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { score ->
                                    viewModel.updatePassingScore(score)
                                }
                            },
                            label = { Text("Passing Score (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Show Results Immediately",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (state.showResultsImmediately) 
                                        "Students see results right after submission" 
                                    else 
                                        "Results visible after exam time ends",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Switch(
                                checked = state.showResultsImmediately,
                                onCheckedChange = viewModel::updateShowResultsImmediately
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Questions (${state.questions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FilledTonalButton(onClick = viewModel::addQuestion) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Question")
                    }
                }
            }

            itemsIndexed(state.questions) { index, question ->
                QuestionCard(
                    index = index,
                    question = question,
                    onQuestionChange = { viewModel.updateQuestion(index, it) },
                    onRemove = { viewModel.removeQuestion(index) },
                    canRemove = state.questions.size > 1
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SmartExamButton(
                    text = if (state.isEditMode) "Update Exam" else "Create Exam",
                    onClick = viewModel::saveExam,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun QuestionCard(
    index: Int,
    question: QuestionState,
    onQuestionChange: (QuestionState) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove question",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            OutlinedTextField(
                value = question.questionText,
                onValueChange = { onQuestionChange(question.copy(questionText = it)) },
                label = { Text("Question Text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Text(
                text = "Answer Options",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            OptionRow(
                label = "A",
                value = question.optionA,
                isCorrect = question.correctAnswer == "A",
                onValueChange = { onQuestionChange(question.copy(optionA = it)) },
                onSelectCorrect = { onQuestionChange(question.copy(correctAnswer = "A")) }
            )

            OptionRow(
                label = "B",
                value = question.optionB,
                isCorrect = question.correctAnswer == "B",
                onValueChange = { onQuestionChange(question.copy(optionB = it)) },
                onSelectCorrect = { onQuestionChange(question.copy(correctAnswer = "B")) }
            )

            OptionRow(
                label = "C",
                value = question.optionC,
                isCorrect = question.correctAnswer == "C",
                onValueChange = { onQuestionChange(question.copy(optionC = it)) },
                onSelectCorrect = { onQuestionChange(question.copy(correctAnswer = "C")) }
            )

            OptionRow(
                label = "D",
                value = question.optionD,
                isCorrect = question.correctAnswer == "D",
                onValueChange = { onQuestionChange(question.copy(optionD = it)) },
                onSelectCorrect = { onQuestionChange(question.copy(correctAnswer = "D")) }
            )

            OptionRow(
                label = "E",
                value = question.optionE,
                isCorrect = question.correctAnswer == "E",
                onValueChange = { onQuestionChange(question.copy(optionE = it)) },
                onSelectCorrect = { onQuestionChange(question.copy(correctAnswer = "E")) },
                isOptional = true
            )
        }
    }
}

@Composable
fun OptionRow(
    label: String,
    value: String,
    isCorrect: Boolean,
    onValueChange: (String) -> Unit,
    onSelectCorrect: () -> Unit,
    isOptional: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = isCorrect,
            onClick = onSelectCorrect
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Option $label${if (isOptional) " (Optional)" else ""}") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = if (isCorrect) {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            } else {
                OutlinedTextFieldDefaults.colors()
            }
        )
    }
}
