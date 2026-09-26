package org.bxkr.octodiary.screens.navsections.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.RankingMemberCard
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.ui.theme.LocalCompactLayout

@Composable
fun RankingList() {
    val context = LocalContext.current
    val compact = LocalCompactLayout.current
    var rosterLoading by remember { mutableStateOf(false) }
    var rosterLoaded by remember { mutableStateOf(false) }
    var rosterError by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    fun resolveMemberName(personId: String): String? {
        val child = DataService.profile.children.getOrNull(DataService.currentProfile)
        if (child?.contingentGuid == personId) {
            return listOfNotNull(child.lastName, child.firstName, child.middleName)
                .filter { it.isNotBlank() }
                .fastJoinToString(" ")
                .takeIf { it.isNotBlank() }
        }
        return DataService.classMembers.firstOrNull { it.personId == personId }
            ?.fio?.takeIf { it.isNotBlank() }
    }

    fun loadRoster() {
        rosterLoading = true
        rosterError = false
        DataService.loadClassMembersForRanking(
            onError = {
                rosterLoading = false
                rosterError = true
            },
            onUpdated = {
                rosterLoading = false
                rosterLoaded = true
                rosterError = DataService.classMembers.isEmpty()
            }
        )
    }

    LaunchedEffect(context.isDemo, DataService.currentProfile) {
        if (!context.isDemo) loadRoster()
        else rosterLoaded = true
    }

    LazyColumn(
        Modifier.padding(if (compact) 4.dp else 8.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp)
    ) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                label = { Text(stringResource(R.string.ranking_search)) },
                singleLine = true
            )
        }
        if (rosterLoading || rosterError || !rosterLoaded) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when {
                        rosterLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(4.dp),
                                strokeWidth = 2.dp
                            )
                            Text(stringResource(R.string.ranking_names_loading), style = MaterialTheme.typography.bodySmall)
                        }
                        rosterError -> {
                            Text(
                                stringResource(R.string.ranking_names_unavailable),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = ::loadRoster) {
                                Text(stringResource(R.string.ranking_names_retry))
                            }
                        }
                    }
                }
            }
        }

        items(DataService.ranking.filter { rankingMember ->
            search.isBlank() || rankingMember.personId.contains(search, ignoreCase = true) ||
                resolveMemberName(rankingMember.personId)?.contains(search, ignoreCase = true) == true
        }) { rankingMember ->
            val child = DataService.profile.children.getOrNull(DataService.currentProfile)
            val isSelf = child != null && rankingMember.personId == child.contingentGuid
            val memberName = resolveMemberName(rankingMember.personId)
            val id = rankingMember.personId
            RankingMemberCard(
                rankPlace = rankingMember.rank.rankPlace,
                average = rankingMember.rank.averageMarkFive,
                memberName = memberName ?: id,
                highlighted = isSelf,
                isAnonymized = memberName == null
            )
        }
    }
}
