package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.credentials.CredentialManager
import com.example.data.FirestoreManager
import com.example.data.repo.*
import com.example.ui.auth.AuthScreen
import com.example.ui.shell.AppShell
import com.example.ui.theme.RoyoTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RoyoTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val firestore = remember { FirestoreManager.get(context) }
    val authRepo = remember { AuthRepository(firestore) }
    val flockRepo = remember { FlockRepository(firestore) }
    val productionRepo = remember { ProductionRepository(firestore) }
    val salesRepo = remember { SalesRepository(firestore) }
    val expenseRepo = remember { ExpenseRepository(firestore) }
    val inventoryRepo = remember { InventoryRepository(firestore) }

    var currentUser by remember {
        mutableStateOf(
            try {
                Firebase.auth.currentUser
            } catch (_: Exception) {
                null
            }
        )
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        val fAuth = try { Firebase.auth } catch (_: Exception) { null }
        fAuth?.addAuthStateListener(listener)
        onDispose {
            fAuth?.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            authRepo = authRepo,
            onAuthSuccess = { authedUser ->
                currentUser = authedUser
            }
        )
    } else {
        val appUser by authRepo.observeUserDoc(user.uid).collectAsState(initial = null)

        AppShell(
            user = appUser,
            flockRepo = flockRepo,
            productionRepo = productionRepo,
            salesRepo = salesRepo,
            expenseRepo = expenseRepo,
            inventoryRepo = inventoryRepo,
            onSignOutClick = {
                authRepo.signOut(
                    context = context,
                    credentialManager = credentialManager,
                    scope = coroutineScope,
                    onComplete = {
                        currentUser = null
                    }
                )
            }
        )
    }
}
