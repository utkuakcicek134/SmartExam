package com.utku.smartexam.ui.student

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.utku.smartexam.data.model.Question
import com.utku.smartexam.ui.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeExamScreen(
    studentId: Long,
    examId: Long,
    onExamComplete: (Long, Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val state by viewModel.takeExamState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(studentId, examId) {
        viewModel.startExam(studentId, examId)
    }

    LaunchedEffect(state.submittedResult) {
        state.submittedResult?.let { result ->
            onExamComplete(examId, result.id)
            viewModel.resetTakeExamState()
        }
    }

    LaunchedEffect(state.alreadyCompleted) {
        if (state.alreadyCompleted && state.submittedResult != null) {
            onExamComplete(examId, state.submittedResult!!.id)
            viewModel.resetTakeExamState()
        }
    }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Exit Exam?") },
            text = { 
                Text("Are you sure you want to exit? Your progress will be saved, but the timer will continue running.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.resetTakeExamState()
                        onNavigateBack()
                    }
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continue Exam")
                }
            }
        )
    }

    if (showSubmitDialog) {
        val unansweredCount = state.questions.size - state.answers.size
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            icon = { Icon(Icons.Default.Send, contentDescription = null) },
            title = { Text("Submit Exam?") },
            text = { 
                Column {
                    Text("Are you sure you want to submit your exam?")
                    if (unansweredCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Warning: You have $unansweredCount unanswered question(s).",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitExam()
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.isLoading) {
        LoadingScreen()
    } else if (state.isSubmitting) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Submitting your exam...")
            }
        }
    } else if (state.error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error ?: "An error occurred",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                text = state.exam?.name ?: "Exam",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Question ${state.currentQuestionIndex + 1} of ${state.questions.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showExitDialog = true }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit")
                        }
                    },
                    actions = {
                        Surface(
                            color = if (state.remainingTimeSeconds < 300) 
                                MaterialTheme.colorScheme.errorContainer 
                            else 
                                MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = viewModel.formatTime(state.remainingTimeSeconds),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(state.questions) { index, question ->
                                QuestionIndicator(
                                    index = index,
                                    isAnswered = state.answers.containsKey(question.id),
                                    isCurrent = index == state.currentQuestionIndex,
                                    onClick = { viewModel.goToQuestion(index) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = viewModel::previousQuestion,
                                enabled = state.currentQuestionIndex > 0
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous")
                            }

                            if (state.currentQuestionIndex == state.questions.size - 1) {
                                Button(
                                    onClick = { showSubmitDialog = true }
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Submit")
                                }
                            } else {
                                Button(
                                    onClick = viewModel::nextQuestion
                                ) {
                                    Text("Next")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            if (state.questions.isNotEmpty()) {
                QuestionContent(
                    question = state.questions[state.currentQuestionIndex],
                    selectedAnswer = state.answers[state.questions[state.currentQuestionIndex].id],
                    onAnswerSelect = { answer ->
                        viewModel.selectAnswer(
                            state.questions[state.currentQuestionIndex].id,
                            answer
                        )
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun QuestionIndicator(
    index: Int,
    isAnswered: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isAnswered -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimary
        isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (isCurrent) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${index + 1}",
            color = textColor,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun QuestionContent(
    question: Question,
    selectedAnswer: String?,
    onAnswerSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        Text(
            text = "Select your answer:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        AnswerOption(
            label = "A",
            text = question.optionA,
            isSelected = selectedAnswer == "A",
            onClick = { onAnswerSelect("A") }
        )

        AnswerOption(
            label = "B",
            text = question.optionB,
            isSelected = selectedAnswer == "B",
            onClick = { onAnswerSelect("B") }
        )

        AnswerOption(
            label = "C",
            text = question.optionC,
            isSelected = selectedAnswer == "C",
            onClick = { onAnswerSelect("C") }
        )

        AnswerOption(
            label = "D",
            text = question.optionD,
            isSelected = selectedAnswer == "D",
            onClick = { onAnswerSelect("D") }
        )

        if (!question.optionE.isNullOrBlank()) {
            AnswerOption(
                label = "E",
                text = question.optionE,
                isSelected = selectedAnswer == "E",
                onClick = { onAnswerSelect("E") }
            )
        }
    }
}

@Composable
fun AnswerOption(
    label: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(width = 2.dp)
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
