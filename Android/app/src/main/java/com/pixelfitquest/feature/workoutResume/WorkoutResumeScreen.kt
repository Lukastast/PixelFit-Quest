package com.pixelfitquest.feature.workoutResume

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.healthbonuses.ui.SessionBonusesCard
import com.pixelfitquest.feature.streak.WeeklyStreakBonusBanner
import com.pixelfitquest.feature.streak.WeeklyStreakViewModel
import com.pixelfitquest.feature.streak.model.WeeklyStreakSnapshot
import com.pixelfitquest.feature.workout.analysis.BAR_EVEN_DEG
import com.pixelfitquest.feature.workout.analysis.BAR_METER_SPAN_DEG
import com.pixelfitquest.feature.workout.model.WorkoutSet
import com.pixelfitquest.feature.workout.model.enums.displayName
import com.pixelfitquest.feature.workoutResume.model.CoachingVisualInfo
import com.pixelfitquest.feature.workoutResume.model.CoachingVisuals
import com.pixelfitquest.feature.workoutResume.ui.FormClipVisual
import com.pixelfitquest.ui.theme.HeartRuby
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.SlateSurface
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutResumeScreen(
    openScreen: (String) -> Unit,
    onWorkoutDeleted: () -> Unit,
    viewModel: WorkoutResumeViewModel,
    weeklyStreakViewModel: WeeklyStreakViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val summary by viewModel.summary.collectAsState()
    val exercisesWithSets by viewModel.exercisesWithSets.collectAsState()
    val bonusUi by viewModel.bonusUi.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedSetIndex by viewModel.selectedSetIndex.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val characterData by viewModel.characterData.collectAsState()
    val weeklyStreak by weeklyStreakViewModel.snapshot.collectAsState()

    val spacing = MaterialTheme.spacing
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape || spacing.widthClass != PixelFitWidthClass.Compact
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isFemale = remember(characterData.gender) {
        characterData.gender.lowercase() in listOf("female", "woman", "character_woman_idle", "fitness_character_woman_idle")
    }

    val allSets = remember(exercisesWithSets) {
        exercisesWithSets.flatMap { it.sets }
    }

    val currentSet: WorkoutSet? = remember(allSets, selectedSetIndex) {
        if (allSets.isNotEmpty()) {
            allSets.getOrNull(selectedSetIndex) ?: allSets.first()
        } else null
    }

    val visualInfo: CoachingVisualInfo = remember(currentSet) {
        currentSet?.let { CoachingVisuals.resolveVisualForSet(it) }
            ?: CoachingVisuals.infoFor(CoachingVisuals.TAG_FORM_EVEN)
    }

    LaunchedEffect(Unit) {
        viewModel.deleted.collect { onWorkoutDeleted() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.resume_screen_title),
                        fontFamily = determination,
                        fontSize = if (useTwoPane) 18.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { openScreen("home") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_desc),
                            tint = Color.White,
                        )
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = spacing.sm),
                    ) {
                        // Level Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(spacing.cornerXs))
                                .background(SlateGroove)
                                .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                                .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                        ) {
                            Text(
                                text = "Lvl ${userData?.level ?: 1}",
                                color = SilverSteel,
                                fontFamily = determination,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                            )
                        }

                        // XP Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(spacing.cornerXs))
                                .background(SlateGroove)
                                .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                                .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                        ) {
                            Text(
                                text = "+${summary.totalXp} XP",
                                color = VitalGreen,
                                fontFamily = determination,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                            )
                        }

                        // Coins Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(spacing.cornerXs))
                                .background(SlateGroove)
                                .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                                .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(modifier = Modifier.width(spacing.xxs))
                                Text(
                                    text = "+${summary.totalCoins}",
                                    color = ImperialGold,
                                    fontFamily = determination,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDeep,
                ),
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.logsigninbackground),
                    contentScale = ContentScale.Crop,
                )
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.md, vertical = spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // --- 3 Equal Tabs (Customization Screen Pattern) ---
                ResumeTabBar(
                    selected = selectedTab,
                    onSelect = viewModel::selectTab,
                    isCompact = useTwoPane,
                )

                Spacer(Modifier.height(spacing.sm))

                // --- Main Content: Adaptive Two-Pane vs Single Column ---
                if (useTwoPane) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        // Left Pane: HeroStage + Set Selector + Primary CTA
                        Column(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            HeroStageBox(
                                visualInfo = visualInfo,
                                setNumber = currentSet?.setNumber ?: 1,
                                formScore = currentSet?.formScore?.toInt() ?: summary.avgScore.toInt(),
                                levelDeg = currentSet?.levelDeg ?: 0f,
                                twistDeg = currentSet?.twistDeg ?: 0f,
                                romScore = currentSet?.romScore?.toInt() ?: 100,
                                isFemale = isFemale,
                                isCompact = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            if (allSets.size > 1) {
                                SetSelectorGroupedByExercise(
                                    exercisesWithSets = exercisesWithSets,
                                    selectedSetIndex = selectedSetIndex,
                                    onSelect = viewModel::selectSetIndex,
                                )
                            }

                            Spacer(Modifier.weight(1f))

                            PixelArtButton(
                                onClick = { openScreen("home") },
                                imageRes = R.drawable.button_unclicked,
                                pressedRes = R.drawable.button_clicked,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(spacing.scale(48)),
                            ) {
                                Text(
                                    text = stringResource(R.string.continue_to_quest),
                                    color = Color.White,
                                    fontFamily = determination,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                )
                            }
                        }

                        // Right Pane: Tab Content
                        Box(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxHeight(),
                        ) {
                            when (selectedTab) {
                                ResumeTab.Form -> FormTabContent(
                                    visualInfo = visualInfo,
                                    currentSet = currentSet,
                                    allSets = allSets,
                                    exercisesWithSets = exercisesWithSets,
                                    summary = summary,
                                    selectedSetIndex = selectedSetIndex,
                                    onSelectSet = viewModel::selectSetIndex,
                                    onContinue = { openScreen("home") },
                                    isFemale = isFemale,
                                    showHeroStage = false, // already on left pane
                                )
                                ResumeTab.Sets -> SetsTabContent(
                                    exercisesWithSets = exercisesWithSets,
                                    allSets = allSets,
                                    summary = summary,
                                    weeklyStreak = weeklyStreak,
                                    bonusUi = bonusUi,
                                    selectedSetIndex = selectedSetIndex,
                                    isDeleting = isDeleting,
                                    onSelectSet = { index ->
                                        viewModel.selectSetIndex(index)
                                        viewModel.selectTab(ResumeTab.Form)
                                    },
                                    onDeleteRequest = { showDeleteDialog = true },
                                )
                            }
                        }
                    }
                } else {
                    // Portrait: Single Column Content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        when (selectedTab) {
                            ResumeTab.Form -> FormTabContent(
                                visualInfo = visualInfo,
                                currentSet = currentSet,
                                allSets = allSets,
                                exercisesWithSets = exercisesWithSets,
                                summary = summary,
                                selectedSetIndex = selectedSetIndex,
                                onSelectSet = viewModel::selectSetIndex,
                                onContinue = { openScreen("home") },
                                isFemale = isFemale,
                                showHeroStage = true,
                            )
                            ResumeTab.Sets -> SetsTabContent(
                                exercisesWithSets = exercisesWithSets,
                                allSets = allSets,
                                summary = summary,
                                weeklyStreak = weeklyStreak,
                                bonusUi = bonusUi,
                                selectedSetIndex = selectedSetIndex,
                                isDeleting = isDeleting,
                                onSelectSet = { index ->
                                    viewModel.selectSetIndex(index)
                                    viewModel.selectTab(ResumeTab.Form)
                                },
                                onDeleteRequest = { showDeleteDialog = true },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteWorkoutDialog(
            onDismiss = { if (!isDeleting) showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteWorkout()
            },
        )
    }
}

// ==========================================
// TAB BAR (CustomizationTabBar Pattern)
// ==========================================

@Composable
private fun ResumeTabBar(
    selected: ResumeTab,
    onSelect: (ResumeTab) -> Unit,
    isCompact: Boolean = false,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) spacing.xxs else spacing.xs),
    ) {
        ResumeTab.entries.forEach { tab ->
            val isSelected = selected == tab
            val title = when (tab) {
                ResumeTab.Form -> stringResource(R.string.resume_tab_form)
                ResumeTab.Sets -> stringResource(R.string.resume_tab_sets)
            }

            PixelArtButton(
                onClick = { onSelect(tab) },
                imageRes = if (isSelected) R.drawable.button_clicked else R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier
                    .weight(1f)
                    .height(if (isCompact) spacing.scale(36) else spacing.scale(42)),
            ) {
                Text(
                    text = title,
                    color = if (isSelected) ImperialGold else Color.White,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompact) 11.sp else 13.sp,
                )
            }
        }
    }
}

// ==========================================
// HERO STAGE BOX (Customization HeroStage Pattern)
// ==========================================

@Composable
private fun HeroStageBox(
    visualInfo: CoachingVisualInfo,
    setNumber: Int,
    formScore: Int,
    levelDeg: Float,
    twistDeg: Float,
    romScore: Int,
    isCompact: Boolean = false,
    isFemale: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.cornerMd))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(2.dp, SlateBorder, RoundedCornerShape(spacing.cornerMd))
            .padding(spacing.sm),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Badge: Set Number + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove)
                        .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                        .padding(horizontal = spacing.sm, vertical = spacing.xxs),
                ) {
                    Text(
                        text = stringResource(R.string.set_label, setNumber),
                        color = SilverSteel,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(visualInfo.accentColor.copy(alpha = 0.20f))
                        .border(1.dp, visualInfo.accentColor, RoundedCornerShape(spacing.cornerXs))
                        .padding(horizontal = spacing.sm, vertical = spacing.xxs),
                ) {
                    Text(
                        text = stringResource(visualInfo.badgeTextRes),
                        color = visualInfo.accentColor,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }

            Spacer(Modifier.height(spacing.xs))

            // 16-bit SNES Animated Form Clip Visual with lifter avatar
            FormClipVisual(
                tag = visualInfo.tag,
                isFemale = isFemale,
                size = if (isCompact) spacing.scale(105) else spacing.scale(125),
            )

            Spacer(Modifier.height(spacing.xs))

            // Bottom Info Overlay: Title + Actionable Coaching Cue
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.cornerSm))
                    .background(SlateGroove.copy(alpha = 0.92f))
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerSm))
                    .padding(spacing.sm),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(visualInfo.titleRes),
                        color = visualInfo.accentColor,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(spacing.xxs))

                    Text(
                        text = stringResource(visualInfo.cueRes),
                        color = SilverSteel,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(spacing.xxs))

                    // Metrics Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Text(
                            text = "Form: $formScore/100",
                            color = gradeColor(formScore.toFloat()),
                            fontFamily = determination,
                            fontSize = 11.sp,
                        )
                        Text(
                            text = "ROM: $romScore%",
                            color = SilverSlate,
                            fontFamily = determination,
                            fontSize = 11.sp,
                        )
                        Text(
                            text = "Tilt: ${levelCaption(levelDeg)}",
                            color = if (kotlin.math.abs(levelDeg) > BAR_EVEN_DEG) HeartRuby else VitalGreen,
                            fontFamily = determination,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SET SELECTOR (GROUPED BY EXERCISE)
// ==========================================

@Composable
private fun SetSelectorGroupedByExercise(
    exercisesWithSets: List<com.pixelfitquest.feature.workout.model.ExerciseWithSets>,
    selectedSetIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    var globalCounter = 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        exercisesWithSets.forEach { item ->
            val exercise = item.exercise
            val sets = item.sets
            if (sets.isNotEmpty()) {
                val exerciseStartIndex = globalCounter
                globalCounter += sets.size

                Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                    Text(
                        text = exercise.type.displayName(),
                        color = ImperialGold,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        sets.forEachIndexed { setIndex, set ->
                            val currentGlobalIndex = exerciseStartIndex + setIndex
                            val isSelected = currentGlobalIndex == selectedSetIndex
                            val setVisual = remember(set) { CoachingVisuals.resolveVisualForSet(set) }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(spacing.cornerSm))
                                    .background(if (isSelected) SlateSurface else SlateDeep.copy(alpha = 0.88f))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ImperialGold else SlateBorder,
                                        shape = RoundedCornerShape(spacing.cornerSm),
                                    )
                                    .clickable { onSelect(currentGlobalIndex) }
                                    .padding(horizontal = spacing.sm, vertical = spacing.xs),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(setVisual.accentColor),
                                    )
                                    Spacer(Modifier.width(spacing.xs))
                                    Text(
                                        text = stringResource(R.string.set_chip_label, set.setNumber),
                                        color = if (isSelected) ImperialGold else Color.White,
                                        fontFamily = determination,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// FORM TAB CONTENT
// ==========================================

@Composable
private fun FormTabContent(
    visualInfo: CoachingVisualInfo,
    currentSet: WorkoutSet?,
    allSets: List<WorkoutSet>,
    exercisesWithSets: List<com.pixelfitquest.feature.workout.model.ExerciseWithSets>,
    summary: com.pixelfitquest.feature.workoutResume.model.WorkoutSummary,
    selectedSetIndex: Int,
    onSelectSet: (Int) -> Unit,
    onContinue: () -> Unit,
    isFemale: Boolean = false,
    showHeroStage: Boolean,
) {
    val spacing = LocalSpacing.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        contentPadding = PaddingValues(bottom = spacing.lg),
    ) {
        if (showHeroStage) {
            item {
                HeroStageBox(
                    visualInfo = visualInfo,
                    setNumber = currentSet?.setNumber ?: 1,
                    formScore = currentSet?.formScore?.toInt() ?: summary.avgScore.toInt(),
                    levelDeg = currentSet?.levelDeg ?: 0f,
                    twistDeg = currentSet?.twistDeg ?: 0f,
                    romScore = currentSet?.romScore?.toInt() ?: 100,
                    isFemale = isFemale,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (allSets.size > 1) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                    SetSelectorGroupedByExercise(
                        exercisesWithSets = exercisesWithSets,
                        selectedSetIndex = selectedSetIndex,
                        onSelect = onSelectSet,
                    )
                }
            }
        }

        // Detailed Balance & Twist Meters for the Selected Set
        if (currentSet != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(spacing.cornerSm))
                        .background(SlateSurface.copy(alpha = 0.94f))
                        .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerSm))
                        .padding(spacing.sm),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Text(
                            text = "IMU Balance Analysis · Set ${currentSet.setNumber}",
                            color = ImperialGold,
                            fontFamily = determination,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        ImbalanceMeterWithClips(
                            title = stringResource(R.string.z_tilt_score_title) + " (Z-Tilt)",
                            degrees = currentSet.levelDeg,
                            caption = levelCaption(currentSet.levelDeg),
                            leftTag = CoachingVisuals.TAG_TILT_LEFT,
                            leftLabel = "Left Tilt",
                            rightTag = CoachingVisuals.TAG_TILT_RIGHT,
                            rightLabel = "Right Tilt",
                            accentColor = HeartRuby,
                            isFemale = isFemale,
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(SlateBorder.copy(alpha = 0.6f)),
                        )

                        ImbalanceMeterWithClips(
                            title = stringResource(R.string.x_tilt_score_title) + " (X-Tilt)",
                            degrees = currentSet.twistDeg,
                            caption = twistCaption(currentSet.twistDeg),
                            leftTag = CoachingVisuals.TAG_TWIST_HIP,
                            leftLabel = "Hip Twist",
                            rightTag = CoachingVisuals.TAG_TWIST_HEAD,
                            rightLabel = "Head Twist",
                            accentColor = TorchAmber,
                            isFemale = isFemale,
                        )
                    }
                }
            }
        }

        if (showHeroStage) {
            item {
                PixelArtButton(
                    onClick = onContinue,
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.scale(50)),
                ) {
                    Text(
                        text = stringResource(R.string.continue_to_quest),
                        color = Color.White,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

// ==========================================
// SETS & REPS TAB CONTENT (WITH REWARDS & DELETE AT BOTTOM)
// ==========================================

@Composable
private fun SetsTabContent(
    exercisesWithSets: List<com.pixelfitquest.feature.workout.model.ExerciseWithSets>,
    allSets: List<WorkoutSet>,
    summary: com.pixelfitquest.feature.workoutResume.model.WorkoutSummary,
    weeklyStreak: WeeklyStreakSnapshot,
    bonusUi: com.pixelfitquest.feature.healthbonuses.model.SessionBonusUiState,
    selectedSetIndex: Int,
    isDeleting: Boolean,
    onSelectSet: (Int) -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val spacing = LocalSpacing.current
    if (exercisesWithSets.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.no_exercises_completed),
                color = SilverSlate,
                fontFamily = determination,
                fontSize = 14.sp,
            )
        }
        return
    }

    var globalIndexCounter = 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        contentPadding = PaddingValues(bottom = spacing.lg),
    ) {
        // --- 1. Workout Average Form (Moved to Top) ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.cornerSm))
                    .background(SlateSurface.copy(alpha = 0.94f))
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerSm))
                    .padding(spacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Workout Average Form",
                            color = SilverSteel,
                            fontFamily = determination,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${allSets.size} sets analyzed",
                            color = SilverSlate,
                            fontSize = 11.sp,
                        )
                    }

                    Text(
                        text = stringResource(R.string.score_out_of_100, summary.avgScore.toInt()),
                        color = gradeColor(summary.avgScore),
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }
            }
        }

        // --- 2. Completed Exercises & Sets (Grouped by Exercise) ---
        exercisesWithSets.forEach { item ->
            val exercise = item.exercise
            val sets = item.sets

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(spacing.cornerMd))
                        .background(SlateDeep.copy(alpha = 0.94f))
                        .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerMd))
                        .padding(spacing.sm),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = exercise.type.displayName(),
                                color = ImperialGold,
                                fontFamily = determination,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                            Text(
                                text = stringResource(R.string.sets_completed_count, sets.size),
                                color = SilverSlate,
                                fontSize = 12.sp,
                            )
                        }

                        sets.forEach { set ->
                            val currentIndex = globalIndexCounter++
                            val isSelected = currentIndex == selectedSetIndex
                            val visual = CoachingVisuals.resolveVisualForSet(set)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(spacing.cornerSm))
                                    .background(if (isSelected) SlateSurface else SlateGroove)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ImperialGold else SlateBorder,
                                        shape = RoundedCornerShape(spacing.cornerSm),
                                    )
                                    .clickable { onSelectSet(currentIndex) }
                                    .padding(spacing.sm),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = stringResource(R.string.set_label, set.setNumber),
                                            color = Color.White,
                                            fontFamily = determination,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "${set.reps} reps · ${set.weight.toInt()} kg",
                                                color = SilverSteel,
                                                fontSize = 12.sp,
                                            )
                                            Text(
                                                text = stringResource(R.string.score_out_of_100, set.formScore.toInt()),
                                                color = gradeColor(set.formScore),
                                                fontFamily = determination,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                            )
                                        }
                                    }

                                    // Mistake tag line
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(visual.accentColor),
                                        )
                                        Spacer(Modifier.width(spacing.xxs))
                                        Text(
                                            text = stringResource(visual.titleRes),
                                            color = visual.accentColor,
                                            fontSize = 11.sp,
                                        )
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            text = "Tap to inspect clip 🔍",
                                            color = SilverSlate,
                                            fontSize = 10.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Rewards at Bottom of Sets & Reps ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.cornerMd))
                    .background(SlateDeep.copy(alpha = 0.94f))
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerMd))
                    .padding(spacing.md),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = "Session Rewards",
                        color = ImperialGold,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total XP", color = SilverSlate, fontSize = 12.sp)
                            Text(
                                text = "+${summary.totalXp}",
                                color = VitalGreen,
                                fontFamily = determination,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Coins Earned", color = SilverSlate, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(spacing.xxs))
                                Text(
                                    text = "+${summary.totalCoins}",
                                    color = ImperialGold,
                                    fontFamily = determination,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Weekly Streak Bonus Banner
        item {
            WeeklyStreakBonusBanner(snapshot = weeklyStreak)
        }

        // Health Connect Extras
        if (bonusUi.loaded) {
            item {
                SessionBonusesCard(
                    bonuses = bonusUi.bonuses,
                    snapshot = bonusUi.snapshot,
                )
            }
        }

        // --- 4. Delete Workout Button (Only CTA at Bottom) ---
        item {
            PixelArtButton(
                onClick = { if (!isDeleting) onDeleteRequest() },
                imageRes = R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.scale(46)),
            ) {
                Text(
                    text = stringResource(
                        if (isDeleting) R.string.deleting_workout else R.string.delete_workout,
                    ),
                    color = HeartRuby,
                    fontFamily = determination,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ==========================================
// METERS & CAPTIONS
// ==========================================

@Composable
private fun ImbalanceMeterWithClips(
    title: String,
    degrees: Float,
    caption: String,
    leftTag: String,
    leftLabel: String,
    rightTag: String,
    rightLabel: String,
    accentColor: Color,
    isFemale: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val isLeftActive = degrees < -BAR_EVEN_DEG
    val isRightActive = degrees > BAR_EVEN_DEG
    val isEven = !isLeftActive && !isRightActive

    val activeCue = when {
        isLeftActive -> stringResource(CoachingVisuals.infoFor(leftTag).cueRes)
        isRightActive -> stringResource(CoachingVisuals.infoFor(rightTag).cueRes)
        else -> null
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.xxs),
    ) {
        // Section Title & Reading Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontFamily = determination,
                fontWeight = FontWeight.Bold,
                color = SilverSteel,
            )

            Text(
                text = caption,
                fontSize = 11.sp,
                fontFamily = determination,
                fontWeight = FontWeight.Bold,
                color = when {
                    isLeftActive || isRightActive -> accentColor
                    else -> VitalGreen
                },
            )
        }

        Spacer(Modifier.height(spacing.scale(2)))

        // Clips + Meter Bar Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left Clip
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(spacing.scale(56)),
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.scale(50))
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove)
                        .border(
                            width = if (isLeftActive) 1.5.dp else 1.dp,
                            color = if (isLeftActive) accentColor else SlateBorder,
                            shape = RoundedCornerShape(spacing.cornerXs),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    FormClipVisual(
                        tag = leftTag,
                        isFemale = isFemale,
                        size = spacing.scale(46),
                    )
                }
                Spacer(Modifier.height(spacing.scale(2)))
                Text(
                    text = leftLabel,
                    color = if (isLeftActive) accentColor else SilverSlate,
                    fontFamily = determination,
                    fontWeight = if (isLeftActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }

            // Center: Tilt Bar with direction cues
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TiltScoreBar(
                    degrees = degrees,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.scale(10)),
                )
                Spacer(modifier = Modifier.height(spacing.scale(3)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "◀ ${leftLabel.substringBefore(" ")}",
                        fontSize = 9.sp,
                        color = if (isLeftActive) accentColor else SilverSlate.copy(alpha = 0.6f),
                    )
                    Text(
                        text = stringResource(R.string.imbalance_even),
                        fontSize = 9.sp,
                        color = if (isEven) VitalGreen else SilverSlate.copy(alpha = 0.5f),
                    )
                    Text(
                        text = "${rightLabel.substringBefore(" ")} ▶",
                        fontSize = 9.sp,
                        color = if (isRightActive) accentColor else SilverSlate.copy(alpha = 0.6f),
                    )
                }
            }

            // Right Clip
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(spacing.scale(56)),
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.scale(50))
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove)
                        .border(
                            width = if (isRightActive) 1.5.dp else 1.dp,
                            color = if (isRightActive) accentColor else SlateBorder,
                            shape = RoundedCornerShape(spacing.cornerXs),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    FormClipVisual(
                        tag = rightTag,
                        isFemale = isFemale,
                        size = spacing.scale(46),
                    )
                }
                Spacer(Modifier.height(spacing.scale(2)))
                Text(
                    text = rightLabel,
                    color = if (isRightActive) accentColor else SilverSlate,
                    fontFamily = determination,
                    fontWeight = if (isRightActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }

        // Active coaching cue (if mistake detected)
        if (activeCue != null) {
            Spacer(Modifier.height(spacing.scale(2)))
            Text(
                text = activeCue,
                color = accentColor.copy(alpha = 0.9f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun levelCaption(deg: Float): String {
    val mag = kotlin.math.abs(deg).toInt()
    return when {
        deg > BAR_EVEN_DEG -> stringResource(R.string.imbalance_right, mag)
        deg < -BAR_EVEN_DEG -> stringResource(R.string.imbalance_left, mag)
        else -> stringResource(R.string.imbalance_even)
    }
}

@Composable
private fun twistCaption(deg: Float): String {
    val mag = kotlin.math.abs(deg).toInt()
    return when {
        deg > BAR_EVEN_DEG -> stringResource(R.string.imbalance_head, mag)
        deg < -BAR_EVEN_DEG -> stringResource(R.string.imbalance_hip, mag)
        else -> stringResource(R.string.imbalance_even)
    }
}

@Composable
fun TiltScoreBar(
    degrees: Float,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(LocalSpacing.current.scale(12)),
) {
    val spacing = LocalSpacing.current
    val span = BAR_METER_SPAN_DEG
    val clamped = degrees.coerceIn(-span, span)
    val positionFraction = (clamped + span) / (2f * span)

    BoxWithConstraints(
        modifier = modifier.clip(RoundedCornerShape(spacing.scale(6))),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            HeartRuby,
                            VitalGreen,
                            HeartRuby,
                        ),
                    ),
                ),
        )

        val markerSize = spacing.sm
        val markerSizePx = with(LocalDensity.current) { markerSize.toPx() }
        val maxOffset = (constraints.maxWidth - markerSizePx).coerceAtLeast(0f)
        val markerOffset = (maxOffset * positionFraction).toInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(markerOffset, 0) }
                .size(markerSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(spacing.xxxs, SlateDeep, CircleShape),
        )
    }
}

private fun gradeColor(score: Float): Color = when {
    score >= 90 -> VitalGreen
    score >= 70 -> ImperialGold
    score >= 50 -> TorchAmber
    else -> HeartRuby
}

// ==========================================
// DELETE WORKOUT DIALOG
// ==========================================

@Composable
private fun DeleteWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.questloginboard),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(spacing.dialogHeight),
                contentScale = ContentScale.FillBounds,
            )
            Column(
                modifier = Modifier
                    .padding(spacing.xl)
                    .fillMaxWidth(0.95f)
                    .heightIn(max = spacing.scale(300)),
            ) {
                Text(
                    text = stringResource(R.string.delete_workout_title),
                    color = Color.White,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text = stringResource(R.string.delete_workout_description),
                    color = SilverSteel,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    PixelArtButton(
                        onClick = onDismiss,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .height(spacing.scale(46))
                            .width(spacing.buttonWidthSm),
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = Color.White,
                            fontFamily = determination,
                        )
                    }
                    PixelArtButton(
                        onClick = onConfirm,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .height(spacing.scale(46))
                            .width(spacing.buttonWidthSm),
                    ) {
                        Text(
                            text = stringResource(R.string.delete_workout_confirm),
                            color = HeartRuby,
                            fontFamily = determination,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
