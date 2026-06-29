package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JobPosting
import com.example.data.Part
import com.example.ui.RepairViewModel
import com.example.ui.theme.*

@Composable
fun AdminDashboardView(
    viewModel: RepairViewModel,
    modifier: Modifier = Modifier
) {
    val requests by viewModel.allRepairRequests.collectAsState()
    val listings by viewModel.allListings.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val parts by viewModel.allParts.collectAsState()
    val jobPostings by viewModel.allJobPostings.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var activeTab by viewModel.adminSelectedTab

    // Hardcoded list of platform user listings for testing role assignment
    val mockUsers = remember {
        mutableStateListOf(
            Triple("Alok Rathi", "+91 98224 01234", "CUSTOMER"),
            Triple("Devendra Chaudhari", "+91 94221 45678", "TECHNICIAN"),
            Triple("Rahul Deshmukh", "+91 98230 98765", "TECHNICIAN"),
            Triple("Pranay Pathak", "+91 97632 24681", "REPAIRMASTER"),
            Triple("Baburao Dharaskar", "+91 91580 13579", "COORDINATOR")
        )
    }

    // Default global surcharge setting
    var baseServiceChargePct by remember { mutableStateOf("15") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "ADMINISTRATIVE CONSOLE", color = TealPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "Superadmin Control Deck", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            RoleBadge(role = "ADMIN")
        }

        // Admin Tabs Row
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = NavySurface,
            contentColor = TealPrimary,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = TealPrimary
                )
            }
        ) {
            val tabs = listOf("Overview", "Applications", "Users", "Parts Editor", "Settings", "Careers Portal")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == index) TealPrimary else GrayText
                        )
                    }
                )
            }
        }

        // Tab Content Router
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                0 -> AdminOverviewPanel(
                    userCount = mockUsers.size + 1, // include self
                    repairCount = requests.size,
                    activeCount = requests.count { it.statusStep in 2..10 },
                    completedCount = requests.count { it.statusStep == 11 },
                    partsCount = parts.size,
                    listingsCount = listings.size,
                    ordersCount = orders.size,
                    pendingApplicationsCount = if (userProfile.technicianStatus == "Pending" || userProfile.repairMasterStatus == "Pending") 1 else 0
                )
                1 -> AdminApplicationsPanel(
                    profile = userProfile,
                    onApprove = { type, name -> viewModel.approveApplication(type, name) },
                    onReject = { type -> viewModel.rejectApplication(type) }
                )
                2 -> AdminUsersPanel(
                    usersList = mockUsers,
                    onRoleChanged = { index, newRole -> mockUsers[index] = mockUsers[index].copy(third = newRole) }
                )
                3 -> AdminPartsPanel(
                    parts = parts,
                    onAddPart = { name, price, cat -> viewModel.addPart(name, price, cat) },
                    onDeletePart = { viewModel.deletePart(it) }
                )
                4 -> AdminSettingsPanel(
                    serviceChargePct = baseServiceChargePct,
                    onUpdateCharge = { baseServiceChargePct = it }
                )
                5 -> AdminCareersPanel(
                    postings = jobPostings,
                    onCreatePosting = { t, r, l, d -> viewModel.createJobPosting(t, r, l, d) },
                    onClosePosting = { viewModel.closeJobPosting(it) },
                    onDeletePosting = { viewModel.deleteJobPosting(it) }
                )
            }
        }
    }
}

@Composable
fun AdminOverviewPanel(
    userCount: Int,
    repairCount: Int,
    activeCount: Int,
    completedCount: Int,
    partsCount: Int,
    listingsCount: Int,
    ordersCount: Int,
    pendingApplicationsCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner alerts for pending items
        if (pendingApplicationsCount > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmberAccent.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, AmberAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = AmberAccent)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("PENDING PARTNER REGISTRATIONS", color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("You have partner applicant forms waiting for background credentials verification.", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        Text("Platform Metrics Summary", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        // Metrics Grid (8 tiles)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(title = "Registered Users", value = "$userCount", icon = Icons.Filled.Person, iconColor = TealPrimary, modifier = Modifier.weight(1f))
                StatCard(title = "Total Repair Tickets", value = "$repairCount", icon = Icons.Filled.List, iconColor = Color.White, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(title = "Active Repairs", value = "$activeCount", icon = Icons.Filled.Build, iconColor = AmberAccent, modifier = Modifier.weight(1f))
                StatCard(title = "Completed Handbacks", value = "$completedCount", icon = Icons.Filled.CheckCircle, iconColor = AccentGreen, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(title = "Parts Catalog", value = "$partsCount items", icon = Icons.Filled.Settings, iconColor = TealPrimary, modifier = Modifier.weight(1f))
                StatCard(title = "Marketplace Listings", value = "$listingsCount Units", icon = Icons.Filled.ShoppingCart, iconColor = Color.White, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(title = "Sold Devices", value = "$ordersCount Sales", icon = Icons.Filled.Check, iconColor = AccentGreen, modifier = Modifier.weight(1f))
                StatCard(title = "Pending Applications", value = "$pendingApplicationsCount Pending", icon = Icons.Filled.Warning, iconColor = AmberAccent, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AdminApplicationsPanel(
    profile: com.example.data.UserProfile,
    onApprove: (String, String) -> Unit,
    onReject: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Partner Applicant Backlog", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        }

        val hasTechnicianPending = profile.technicianStatus == "Pending"
        val hasMasterPending = profile.repairMasterStatus == "Pending"

        if (!hasTechnicianPending && !hasMasterPending) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No partner registrations currently pending verification.", color = GrayText, fontSize = 13.sp)
                    }
                }
            }
        }

        if (hasTechnicianPending) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = BorderStroke(1.dp, AmberAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = profile.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Appling for: Certified Technician Node", color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Contact: ${profile.phone}", color = GrayText, fontSize = 11.sp)
                            }
                            Surface(color = AmberAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text("PENDING REVIEW", color = AmberAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApprove("Technician", profile.name) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_approve_tech")
                            ) {
                                Text("Verify & Approve", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onReject("Technician") },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_reject_tech")
                            ) {
                                Text("Reject Form", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        if (hasMasterPending) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = BorderStroke(1.dp, AmberAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = profile.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Appling for: Repair Master Affiliate Shop Owner", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Contact: ${profile.phone}", color = GrayText, fontSize = 11.sp)
                            }
                            Surface(color = AmberAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text("PENDING REVIEW", color = AmberAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApprove("Repair Master", profile.name) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_approve_master")
                            ) {
                                Text("Verify & Approve", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onReject("Repair Master") },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_reject_master")
                            ) {
                                Text("Reject Shop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Show past decisions
        if (profile.technicianStatus == "Approved" || profile.repairMasterStatus == "Approved") {
            item {
                Text(text = "Approved Partnerships Log", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = NavySurface), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (profile.technicianStatus == "Approved") {
                            Text(text = "✔ ${profile.name} - Approved as Certified Technician Specialist", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (profile.repairMasterStatus == "Approved") {
                            Text(text = "✔ ${profile.name} - Approved as Retail Repair Master Lab Partner", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUsersPanel(
    usersList: List<Triple<String, String, String>>,
    onRoleChanged: (Int, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = usersList.filter { it.first.contains(searchQuery, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Registered Users List", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search system clients & contractors...", color = Color.Gray, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = GrayBorder
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filteredUsers.size) { index ->
            val user = filteredUsers[index]
            var showRoleDropdown by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, GrayBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = user.first, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = user.second, color = GrayText, fontSize = 11.sp)
                    }

                    // Role Clickable selector
                    Box {
                        Surface(
                            modifier = Modifier
                                .clickable { showRoleDropdown = true }
                                .testTag("change_role_dropdown_${index}"),
                            color = TealPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = user.third, color = TealPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleDropdown,
                            onDismissRequest = { showRoleDropdown = false },
                            modifier = Modifier.background(NavyLightSurface)
                        ) {
                            listOf("CUSTOMER", "TECHNICIAN", "REPAIRMASTER", "COORDINATOR", "ADMIN", "MARKETPLACE_BUYER").forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        onRoleChanged(index, r)
                                        showRoleDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPartsPanel(
    parts: List<Part>,
    onAddPart: (String, Double, String) -> Unit,
    onDeletePart: (Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Display") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("Display", "Battery", "Camera", "Port", "Chassis", "Audio", "Accessory")
    val filteredParts = parts.filter { it.name.contains(searchQuery, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Form to Add Part
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Spare Parts to Inventory", color = TealPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("e.g. OEM OLED Panel (iPhone 14)", color = Color.Gray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = GrayBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_part_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            placeholder = { Text("Price (e.g. 119.00)", color = Color.Gray, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = GrayBorder
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_part_price_input")
                        )

                        // Category drop selection
                        var expandedCat by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clickable { expandedCat = true },
                                color = NavyDark,
                                border = BorderStroke(1.dp, GrayBorder),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = category, color = Color.White, fontSize = 13.sp)
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = GrayText)
                                }
                            }
                            DropdownMenu(
                                expanded = expandedCat,
                                onDismissRequest = { expandedCat = false },
                                modifier = Modifier.background(NavyLightSurface)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = Color.White, fontSize = 12.sp) },
                                        onClick = {
                                            category = cat
                                            expandedCat = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val doublePrice = price.toDoubleOrNull()
                            if (name.isNotBlank() && doublePrice != null) {
                                onAddPart(name, doublePrice, category)
                                name = ""
                                price = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("admin_submit_part_button")
                    ) {
                        Text("Add to Spare parts catalogue", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Search and lists
        item {
            Text(text = "Parts Catalog Listing", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search parts catalog...", color = Color.Gray, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = GrayBorder
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (filteredParts.isEmpty()) {
            item {
                Text("No parts matched your query.", color = GrayText, fontSize = 12.sp)
            }
        } else {
            items(filteredParts) { part ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = BorderStroke(1.dp, GrayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = part.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${part.category} • ₹${String.format("%.2f", part.price)}", color = TealPrimary, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = { onDeletePart(part.id) },
                            modifier = Modifier
                                .background(AccentRed.copy(alpha = 0.12f), CircleShape)
                                .testTag("admin_delete_part_${part.id}")
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AccentRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsPanel(
    serviceChargePct: String,
    onUpdateCharge: (String) -> Unit
) {
    var feeVal by remember { mutableStateOf(serviceChargePct) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Global Surcharge Configurations", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = BorderStroke(1.dp, GrayBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Platform Administrative Surcharge Fee (%)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This percentage fee is automatically appended to technician diagnostics invoices to fund administrative escrow accounts.",
                    color = GrayText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = feeVal,
                        onValueChange = { feeVal = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = GrayBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .width(80.dp)
                            .testTag("admin_service_charge_input")
                    )

                    Button(
                        onClick = { onUpdateCharge(feeVal) },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCareersPanel(
    postings: List<JobPosting>,
    onCreatePosting: (String, String, String, String) -> Unit,
    onClosePosting: (Long) -> Unit,
    onDeletePosting: (Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var roleType by remember { mutableStateOf("Technician") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val rolesList = listOf("Technician", "Repair Master")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = BorderStroke(1.dp, TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Publish Partner Job Opening", color = TealPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Job Title", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Master Microsoldering Tech", color = Color.Gray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = GrayBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_job_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text("Partner Role Type", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row {
                                rolesList.forEach { r ->
                                    val isSelected = roleType == r
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) TealPrimary else NavyLightSurface)
                                            .clickable { roleType = r }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = r, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1.0f)) {
                            Text("Location", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                placeholder = { Text("Nagpur, MH", color = Color.Gray, fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = TealPrimary,
                                    unfocusedBorderColor = GrayBorder
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_job_location_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Job Description", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Outline credentials, schedules, motherboard tools...", color = Color.Gray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = GrayBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("admin_job_desc_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && location.isNotBlank() && description.isNotBlank()) {
                                onCreatePosting(title, roleType, location, description)
                                title = ""
                                location = ""
                                description = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("admin_submit_job_button")
                    ) {
                        Text("Publish Opportunity Listing", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text(text = "Active Job Openings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        if (postings.isEmpty()) {
            item {
                Text("No career openings published.", color = GrayText, fontSize = 12.sp)
            }
        } else {
            items(postings) { posting ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = BorderStroke(1.dp, if (posting.isClosed) GrayBorder else TealPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = posting.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${posting.roleType} • ${posting.location}", color = TealPrimary, fontSize = 11.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!posting.isClosed) {
                                    IconButton(
                                        onClick = { onClosePosting(posting.id) },
                                        modifier = Modifier.background(AmberAccent.copy(alpha = 0.12f), CircleShape)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = AmberAccent, modifier = Modifier.size(16.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { onDeletePosting(posting.id) },
                                    modifier = Modifier.background(AccentRed.copy(alpha = 0.12f), CircleShape)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = posting.description, color = GrayText, fontSize = 12.sp, maxLines = 2)
                    }
                }
            }
        }
    }
}
