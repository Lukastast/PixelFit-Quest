"""
generate_all.py
Driver script to generate all 31 16-bit SNES RPG achievement icons:
- 25 current achievements
- 1 shared locked achievement
- 5 future form achievements
Outputs both 64x64 and 128x128 PNGs to:
- res/achievements/64x64/
- res/achievements/128x128/
- Android/app/src/main/res/drawable/
"""
import os

from draw_workouts import (
    draw_bronze_workout_1, draw_iron_workout_5,
    draw_silver_workout_10, draw_steel_workout_25,
    draw_gold_workout_50, draw_platinum_workout_100
)
from draw_streaks import (
    draw_streak_3, draw_streak_7, draw_streak_14, draw_streak_30
)
from draw_steps import (
    draw_steps_5000, draw_steps_10000, draw_steps_50000, draw_steps_100000
)
from draw_volume_sets import (
    draw_volume_1000, draw_volume_10000, draw_volume_50000,
    draw_sets_10, draw_sets_50, draw_sets_200
)
from draw_milestones import (
    draw_level_5, draw_level_10, draw_level_20,
    draw_unique_3, draw_unique_8
)
from draw_locked_form import (
    draw_locked_achievement, draw_form_even_bar, draw_form_square,
    draw_form_no_dump, draw_clip_true, draw_rom_honest
)
from draw_heart import draw_resting_heart_rate

ACHIEVEMENT_GENERATORS = {
    # 25 Shipped Achievements
    "bronze_workout_1": draw_bronze_workout_1,
    "iron_workout_5": draw_iron_workout_5,
    "silver_workout_10": draw_silver_workout_10,
    "steel_workout_25": draw_steel_workout_25,
    "gold_workout_50": draw_gold_workout_50,
    "platinum_workout_100": draw_platinum_workout_100,
    "streak_3": draw_streak_3,
    "streak_7": draw_streak_7,
    "streak_14": draw_streak_14,
    "streak_30": draw_streak_30,
    "steps_5000": draw_steps_5000,
    "steps_10000": draw_steps_10000,
    "steps_50000": draw_steps_50000,
    "steps_100000": draw_steps_100000,
    "volume_1000": draw_volume_1000,
    "volume_10000": draw_volume_10000,
    "volume_50000": draw_volume_50000,
    "sets_10": draw_sets_10,
    "sets_50": draw_sets_50,
    "sets_200": draw_sets_200,
    "level_5": draw_level_5,
    "level_10": draw_level_10,
    "level_20": draw_level_20,
    "unique_3": draw_unique_3,
    "unique_8": draw_unique_8,
    "resting_heart_rate": draw_resting_heart_rate,

    # 1 Shared Locked Achievement
    "locked_achievement": draw_locked_achievement,

    # Drawn for the gallery. Not copied into the app until the catalog has these ids.
    "form_even_bar": draw_form_even_bar,
    "form_square": draw_form_square,
    "form_no_dump": draw_form_no_dump,
    "clip_true": draw_clip_true,
    "rom_honest": draw_rom_honest,
}

# Icons with no AchievementCatalog entry. Kept out of the APK.
GALLERY_ONLY = {
    "form_even_bar",
    "form_square",
    "form_no_dump",
    "clip_true",
    "rom_honest",
}


def main():
    dir_64 = os.path.join("res", "achievements", "64x64")
    dir_128 = os.path.join("res", "achievements", "128x128")
    dir_drawable = os.path.join("Android", "app", "src", "main", "res", "drawable")

    os.makedirs(dir_64, exist_ok=True)
    os.makedirs(dir_128, exist_ok=True)
    os.makedirs(dir_drawable, exist_ok=True)

    print(f"Generating {len(ACHIEVEMENT_GENERATORS)} icons...")

    for ach_id, generator_fn in ACHIEVEMENT_GENERATORS.items():
        canvas = generator_fn()
        im64 = canvas.to_image()
        im128 = canvas.to_128()

        im64.save(os.path.join(dir_64, f"{ach_id}.png"), format="PNG")
        im128.save(os.path.join(dir_128, f"{ach_id}.png"), format="PNG")
        if ach_id not in GALLERY_ONLY:
            # 128px nearest-neighbor scale stays sharp on xxhdpi and xxxhdpi.
            im128.save(os.path.join(dir_drawable, f"{ach_id}.png"), format="PNG")

        print(f"  [DONE] {ach_id} -> 64x64 and 128x128")

    print(f"\nAll {len(ACHIEVEMENT_GENERATORS)} icons successfully generated and saved!")


if __name__ == "__main__":
    main()
