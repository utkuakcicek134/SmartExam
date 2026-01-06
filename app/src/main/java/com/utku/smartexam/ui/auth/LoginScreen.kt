package com.utku.smartexam.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.utku.smartexam.data.model.User
import com.utku.smartexam.data.model.UserRole
import com.utku.smartexam.ui.components.ErrorMessage
import com.utku.smartexam.ui.components.SmartExamButton
import com.utku.smartexam.ui.components.SmartExamPasswordField
import com.utku.smartexam.ui.components.SmartExamTextField

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.loginState.collectAsState()

    LaunchedEffect(state.loginSuccess) {
        state.loginSuccess?.let { user ->
            onLoginSuccess(user)
            viewModel.resetLoginState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SmartExam",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Welcome back! Please login to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Login as:",
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
                onClick = { viewModel.updateLoginRole(UserRole.STUDENT) },
                modifier = Modifier.weight(1f)
            )
            RoleSelectionCard(
                title = "Teacher",
                icon = Icons.Default.School,
                isSelected = state.selectedRole == UserRole.TEACHER,
                onClick = { viewModel.updateLoginRole(UserRole.TEACHER) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        state.error?.let { error ->
            ErrorMessage(message = error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        SmartExamTextField(
            value = state.email,
            onValueChange = viewModel::updateLoginEmail,
            label = "Email",
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SmartExamPasswordField(
            value = state.password,
            onValueChange = viewModel::updateLoginPassword,
            label = "Password"
        )

        Spacer(modifier = Modifier.height(24.dp))

        SmartExamButton(
            text = "Login",
            onClick = viewModel::login,
            isLoading = state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onNavigateToRegister) {
                Text("Register")
            }
        }
    }
}

@Composable
fun RoleSelectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) 
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp
            ) 
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
