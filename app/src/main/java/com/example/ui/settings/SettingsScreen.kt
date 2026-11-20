package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.ui.common.PageHeader
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    user: AppUser?,
    flocksCount: Int,
    productionCount: Int,
    salesCount: Int,
    expensesCount: Int,
    inventoryCount: Int,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAdmin = user?.role == "ADMIN"
    val roleLabel = if (isAdmin) "Farm owner" else "Farm staff"

    var inviteEmail by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf("USER") }
    var isSendingInvite by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PageHeader(
            eyebrow = "Your account",
            title = "Profile & team",
            description = "Your sign-in details, saved farm records, and the teammates helping on the farm."
        )

        // Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.fullName?.ifBlank { "Farm Member" } ?: "Farm Member",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = user?.email?.ifBlank { "Signed in" } ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedText
                        )
                    }

                    // Role Chip
                    Surface(
                        color = if (isAdmin) GoldenYellow else SurfaceButter,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = roleLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAdmin) DeepEspresso else WarmAmber,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onSignOutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceButter),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = BrickRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign out", color = BrickRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Farm Records Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "SAVED FARM RECORDS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RecordMetric("Flocks", "$flocksCount")
                    RecordMetric("Logs", "$productionCount")
                    RecordMetric("Sales", "$salesCount")
                    RecordMetric("Costs", "$expensesCount")
                    RecordMetric("Store", "$inventoryCount")
                }
            }
        }

        // Team Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "TEAM ACCESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isAdmin) {
                    Text(
                        text = "Staff members can log daily eggs, sales, and expenses. Owners and admins can also edit or remove records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        label = { Text("Teammate Email") },
                        placeholder = { Text("colleague@example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = inviteRole == "USER",
                            onClick = { inviteRole = "USER" },
                            label = { Text("Farm staff") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldenYellowSoft,
                                selectedLabelColor = DeepEspresso
                            )
                        )
                        FilterChip(
                            selected = inviteRole == "ADMIN",
                            onClick = { inviteRole = "ADMIN" },
                            label = { Text("Farm owner / Admin") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldenYellowSoft,
                                selectedLabelColor = DeepEspresso
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (inviteEmail.isNotBlank()) {
                                isSendingInvite = true
                                Toast.makeText(context, "Invite sent to $inviteEmail", Toast.LENGTH_SHORT).show()
                                inviteEmail = ""
                                isSendingInvite = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow,
                            contentColor = DeepEspresso
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = inviteEmail.isNotBlank() && !isSendingInvite
                    ) {
                        Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send invite", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "You're signed in as farm staff. Ask the farm owner (florenceabeesiga@gmail.com) if you need help updating team access.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordMetric(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ),
            color = WarmAmber
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MutedText
        )
    }
}
