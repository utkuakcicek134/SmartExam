package com.utku.smartexam.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.utku.smartexam.data.model.UserRole
import com.utku.smartexam.ui.components.ErrorMessage
import com.utku.smartexam.ui.components.SmartExamButton
import com.utku.smartexam.ui.components.SmartExamPasswordField
import com.utku.smartexam.ui.components.SmartExamTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.registerState.collectAsState()

    LaunchedEffect(state.registerSuccess) {
        if (state.registerSuccess) {
            onRegisterSuccess()
            viewModel.resetRegisterState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Register as:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RoleSelectionCard(
                    title = "Student",
                    icon = Icons.Default.Person,
                    isSelected = state.selectedRole == UserRole.STUDENT,
                    onClick = { viewModel.updateRegisterRole(UserRole.STUDENT) },
                    modifier = Modifier.weight(1f)
                )
                RoleSelectionCard(
                    title = "Teacher",
                    icon = Icons.Default.School,
                    isSelected = state.selectedRole == UserRole.TEACHER,
                    onClick = { viewModel.updateRegisterRole(UserRole.TEACHER) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            state.error?.let { error ->
                ErrorMessage(message = error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            SmartExamTextField(
                value = state.fullName,
                onValueChange = viewModel::updateRegisterFullName,
                label = "Full Name",
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SmartExamTextField(
                value = state.email,
                onValueChange = viewModel::updateRegisterEmail,
                label = "Email",
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SmartExamPasswordField(
                value = state.password,
                onValueChange = viewModel::updateRegisterPassword,
                label = "Password"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SmartExamPasswordField(
                value = state.confirmPassword,
                onValueChange = viewModel::updateRegisterConfirmPassword,
                label = "Confirm Password"
            )

            Spacer(modifier = Modifier.height(24.dp))

            SmartExamButton(
                text = "Register",
                onClick = viewModel::register,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onNavigateBack) {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
