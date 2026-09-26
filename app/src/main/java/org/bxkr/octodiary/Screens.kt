package org.bxkr.octodiary

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.bxkr.octodiary.screens.navsections.dashboard.DashboardScreen
import org.bxkr.octodiary.screens.navsections.daybook.ScheduleScreen
import org.bxkr.octodiary.screens.navsections.homeworks.PlannerScreen
import org.bxkr.octodiary.screens.navsections.marks.MarksScreen
import org.bxkr.octodiary.screens.navsections.profile.ProfileScreen2

enum class Screen {
    Login,
    Callback,
    MainNav
}

enum class NavSection(
    val icon: ImageVector,
    @StringRes val title: Int,
    val route: String,
    val composable: @Composable () -> Unit
) {
    Dashboard(Icons.Rounded.Dashboard, R.string.dashboard, "dashboard", { DashboardScreen() }),
    Daybook(Icons.AutoMirrored.Rounded.MenuBook, R.string.diary, "daybook", { ScheduleScreen() }),
    Homeworks(Icons.Rounded.HomeWork, R.string.homeworks, "homeworks", { PlannerScreen() }),
    Marks(Icons.AutoMirrored.Rounded.TrendingUp, R.string.marks, "marks", { MarksScreen() }),
    Profile(Icons.Rounded.Person, R.string.profile, "profile", { ProfileScreen2() })
}
