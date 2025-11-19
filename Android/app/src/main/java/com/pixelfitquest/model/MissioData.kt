package com.PixelFitQuest.model

// Missions pool (unchanged)
val missionsPool = listOf(
    "Walk 5000 steps",
    "Walk 10000 steps",
    "Complete 1 workout",
    "Complete 2 workouts",
    "Walk 2500 steps",
    "Walk 7500 steps",
)

// Rewards pool: Now each reward is either EXP or Coins (randomly chosen type with a value)
val rewardsPool = listOf(
    "exp:50",   // Format: "TYPE:VALUE"
    "exp:100",
    "exp:75",
    "exp:150",
    "exp:200",
    "coins:10",
    "coins:20",
    "coins:15",
    "coins:30",
    "coins:40"
)