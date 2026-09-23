"""
draw_milestones.py
Draws the 5 milestone achievements:
1. level_5 (Apprentice): Bronze apprentice cap + wooden sword
2. level_10 (Adventurer): Silver map and compass, mid-level hero
3. level_20 (Champion): Gold crown on a champion's helm
4. unique_3 (Variety Pack): Bronze three-rune puzzle (press, squat, pull)
5. unique_8 (Full Roster): Silver party of 8 tiny weapon icons in a ring
"""
import math
from pixel_canvas import (
    PixelCanvas, PAL_BRONZE, PAL_SILVER, PAL_GOLD, PAL_PLATINUM,
    C_WOOD_HI, C_WOOD_MID, C_WOOD_DARK, C_WOOD_SHADOW,
    C_LEATHER_HI, C_LEATHER_MID, C_LEATHER_DARK,
    C_STEEL_HI, C_STEEL_MID, C_STEEL_DARK, C_STEEL_SHADOW,
    C_GOLD_HI, C_GOLD_MID, C_GOLD_DARK, C_GOLD_SHADOW,
    C_GREEN_HI, C_GREEN_MID, C_GREEN_DARK,
    C_FIRE_YELLOW, C_FIRE_ORANGE, C_FIRE_RED,
    C_CYAN_HI, C_CYAN_MID, C_CYAN_DARK,
    C_RED, C_DARK_RED, C_WHITE, C_BLACK
)


def draw_level_5():
    """level_5: Bronze apprentice cap + wooden sword."""
    c = PixelCanvas(64)
    c.draw_badge_frame('bronze')

    # Wooden Training Sword (crossing diagonally behind cap from 15, 47 to 49, 15)
    # Blade (from 24, 38 to 49, 15)
    c.line(24, 38, 49, 15, C_WOOD_SHADOW)
    c.line(23, 37, 48, 14, C_WOOD_MID)
    c.line(22, 36, 47, 13, C_WOOD_HI)
    c.pset(48, 14, C_WOOD_HI)  # Tip

    # Crossguard at (23, 37)
    c.line(20, 34, 26, 40, PAL_BRONZE['outline'])
    c.line(20, 33, 25, 39, C_WOOD_HI)

    # Grip & Pommel (19, 41) to (15, 45)
    c.line(19, 41, 15, 45, C_LEATHER_MID)
    c.fill_circle(15, 45, 1, C_WOOD_HI)
    c.pset(15, 45, PAL_BRONZE['outline'])

    # Apprentice Cap (Novice green cloth cap with folded brim and red feather)
    # Cap Body (x: 20 to 44, y: 26 to 43)
    c.fill_rect(21, 28, 43, 40, C_GREEN_MID)
    c.fill_rect(23, 26, 39, 36, C_GREEN_HI)
    c.fill_rect(33, 32, 43, 40, C_GREEN_DARK)
    c.rect(20, 26, 44, 41, PAL_BRONZE['outline'])

    # Folded Cap Brim / Band (x: 20 to 44, y: 38 to 44)
    c.fill_rect(19, 38, 45, 44, PAL_BRONZE['outline'])
    c.fill_rect(20, 39, 44, 43, C_LEATHER_MID)
    c.line(20, 39, 44, 39, C_LEATHER_HI)
    c.line(20, 43, 44, 43, C_LEATHER_DARK)

    # Red/Orange Feather Quill in Cap (x: 35 to 44, y: 15 to 29)
    feather_spine = [(36, 28), (38, 24), (40, 20), (42, 16)]
    for fx, fy in feather_spine:
        c.pset(fx, fy, C_FIRE_YELLOW)
        c.pset(fx - 1, fy, C_FIRE_RED)
        c.pset(fx + 1, fy, C_FIRE_ORANGE)
        c.pset(fx, fy - 1, C_FIRE_ORANGE)
    c.pset(43, 15, C_FIRE_YELLOW)
    c.pset(42, 16, C_WHITE)

    return c


def draw_level_10():
    """level_10: Silver map and compass, mid-level hero."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # Unfurled Adventure Map (x: 18 to 48, y: 20 to 48)
    c.fill_rect(17, 19, 49, 49, PAL_SILVER['outline'])
    c.fill_rect(18, 20, 48, 48, (230, 215, 180, 255))
    c.line(18, 20, 48, 20, (250, 240, 210, 255))
    c.line(18, 48, 48, 48, (170, 145, 110, 255))

    # Map details: Coastline contours & Red Treasure 'X'
    c.line(32, 28, 38, 30, (140, 115, 80, 255))
    c.line(38, 30, 44, 27, (140, 115, 80, 255))
    c.line(34, 40, 46, 42, (140, 115, 80, 255))
    # Red X mark
    c.line(40, 34, 44, 38, C_RED)
    c.line(40, 38, 44, 34, C_RED)

    # Silver Navigational Compass placed over the map (Center: 27, 28, Radius: 9)
    c.fill_circle(27, 28, 10, PAL_SILVER['outline'])
    c.fill_circle(27, 28, 9, C_STEEL_DARK)
    c.fill_circle(26, 27, 8, C_STEEL_MID)
    c.fill_circle(26, 27, 7, C_STEEL_HI)
    c.fill_circle(27, 28, 6, (20, 28, 40, 255))  # Glass dial

    # Compass Cardinal Points (N, S, E, W marks)
    c.pset(27, 23, C_STEEL_HI)  # North
    c.pset(27, 33, C_STEEL_HI)  # South
    c.pset(22, 28, C_STEEL_HI)  # West
    c.pset(32, 28, C_STEEL_HI)  # East

    # Compass Magnetic Needle (Two-tone: Red pointer pointing North, Silver South)
    # North Needle (pointing up to 27, 24)
    c.line(27, 28, 27, 24, C_RED)
    c.line(26, 27, 27, 24, C_DARK_RED)
    # South Needle (pointing down to 27, 32)
    c.line(27, 28, 27, 32, C_WHITE)
    c.line(28, 29, 27, 32, C_STEEL_MID)
    # Brass Center Pivot
    c.fill_circle(27, 28, 1, C_GOLD_HI)

    # Glass glare highlight across compass
    c.line(23, 24, 25, 22, C_WHITE)

    return c


def draw_level_20():
    """level_20: Gold crown on a champion's helm."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Champion's Knight Helmet (x: 21 to 43, y: 25 to 48)
    # Helm Dome
    c.fill_circle(32, 35, 10, PAL_GOLD['outline'])
    c.fill_circle(32, 35, 9, C_STEEL_SHADOW)
    c.fill_circle(31, 34, 8, C_STEEL_MID)
    c.fill_circle(30, 33, 6, C_STEEL_HI)

    # Cheekplates & Aventail
    c.fill_rect(23, 38, 41, 48, PAL_GOLD['outline'])
    c.fill_rect(24, 39, 40, 47, C_STEEL_MID)
    c.line(25, 40, 39, 40, C_STEEL_HI)
    c.line(24, 47, 40, 47, C_STEEL_SHADOW)

    # Golden T-Visor & Eye Slits
    c.fill_rect(24, 33, 40, 36, PAL_GOLD['outline'])
    c.fill_rect(25, 34, 39, 35, (10, 8, 16, 255))
    c.line(32, 34, 32, 44, (10, 8, 16, 255))  # Center nasal bar

    # Gold Trim on Helmet
    c.line(23, 32, 41, 32, C_GOLD_HI)
    c.line(24, 44, 40, 44, C_GOLD_MID)

    # Royal Golden Crown seated atop the helm (x: 20 to 44, y: 15 to 27)
    # Crown Base Band (x: 22 to 42, y: 23 to 27)
    c.fill_rect(21, 23, 43, 27, PAL_GOLD['outline'])
    c.fill_rect(22, 24, 42, 26, C_GOLD_MID)
    c.line(22, 24, 42, 24, C_GOLD_HI)
    c.line(22, 26, 42, 26, C_GOLD_SHADOW)

    # Crown Jewels in Band (Ruby center, Sapphires sides)
    c.pset(32, 25, C_RED)
    c.pset(26, 25, C_CYAN_HI)
    c.pset(38, 25, C_CYAN_HI)

    # Crown 5 Spikes / Peaks (y: 15 to 23)
    # Peak 1 (Leftmost): at x: 23, y: 19
    c.fill_rect(22, 19, 24, 23, C_GOLD_MID)
    c.pset(23, 18, C_GOLD_HI)
    # Peak 2: at x: 27, y: 17
    c.fill_rect(26, 17, 28, 23, C_GOLD_MID)
    c.pset(27, 16, C_GOLD_HI)
    # Peak 3 (Center, tallest): at x: 32, y: 14
    c.fill_rect(31, 15, 33, 23, C_GOLD_MID)
    c.pset(32, 14, C_WHITE)
    c.pset(32, 15, C_RED)  # Center jewel on peak
    # Peak 4: at x: 37, y: 17
    c.fill_rect(36, 17, 38, 23, C_GOLD_MID)
    c.pset(37, 16, C_GOLD_HI)
    # Peak 5 (Rightmost): at x: 41, y: 19
    c.fill_rect(40, 19, 42, 23, C_GOLD_MID)
    c.pset(41, 18, C_GOLD_HI)

    # Crown golden outline
    c.line(22, 19, 25, 23, PAL_GOLD['outline'])
    c.line(25, 23, 27, 16, PAL_GOLD['outline'])
    c.line(27, 16, 30, 23, PAL_GOLD['outline'])
    c.line(30, 23, 32, 14, PAL_GOLD['outline'])
    c.line(32, 14, 34, 23, PAL_GOLD['outline'])
    c.line(34, 23, 37, 16, PAL_GOLD['outline'])
    c.line(37, 16, 39, 23, PAL_GOLD['outline'])
    c.line(39, 23, 42, 19, PAL_GOLD['outline'])

    # Royal Glint Sparkle
    c.pset(32, 13, C_WHITE)

    return c


def draw_unique_3():
    """unique_3: Bronze three-rune puzzle (press, squat, pull)."""
    c = PixelCanvas(64)
    c.draw_badge_frame('bronze')

    # Triangular Bronze Relic / Triskele Puzzle (Vertices: Top (32, 17), Bottom-Left (18, 45), Bottom-Right (46, 45))
    # Outer Triangle Border
    c.line(32, 16, 17, 45, PAL_BRONZE['outline'])
    c.line(32, 16, 47, 45, PAL_BRONZE['outline'])
    c.line(17, 45, 47, 45, PAL_BRONZE['outline'])

    c.line(32, 17, 18, 44, PAL_BRONZE['edge_hi'])
    c.line(32, 17, 46, 44, PAL_BRONZE['metal_dark'])
    c.line(18, 44, 46, 44, PAL_BRONZE['rust_dark'])

    # Inner Triangular Recesses for the 3 Runes
    # 1. Top Rune Disc (Center: 32, 25, Radius: 6) - Press Rune (Golden/Yellow Upward Arrow + Bar)
    c.fill_circle(32, 25, 7, PAL_BRONZE['outline'])
    c.fill_circle(32, 25, 6, (38, 18, 10, 255))
    c.circle(32, 25, 6, PAL_BRONZE['metal_mid'])
    # Rune: Upward arrow over horizontal bar
    c.line(28, 28, 36, 28, C_FIRE_YELLOW)  # Barbell bar
    c.line(32, 22, 32, 27, C_FIRE_YELLOW)  # Arrow shaft
    c.pset(32, 21, C_WHITE)
    c.pset(31, 22, C_FIRE_YELLOW)
    c.pset(33, 22, C_FIRE_YELLOW)

    # 2. Bottom-Left Rune Disc (Center: 24, 38, Radius: 6) - Squat Rune (Cyan Deep Knee Angle)
    c.fill_circle(24, 38, 7, PAL_BRONZE['outline'])
    c.fill_circle(24, 38, 6, (38, 18, 10, 255))
    c.circle(24, 38, 6, PAL_BRONZE['metal_mid'])
    # Rune: Deep angle glyph (V / deep hip-knee angle)
    c.line(20, 36, 24, 41, C_CYAN_HI)
    c.line(24, 41, 28, 36, C_CYAN_HI)
    c.pset(24, 41, C_WHITE)
    c.pset(24, 37, C_CYAN_MID)  # Center depth point

    # 3. Bottom-Right Rune Disc (Center: 40, 38, Radius: 6) - Pull Rune (Red/Gold Dual Hook / Deadlift)
    c.fill_circle(40, 38, 7, PAL_BRONZE['outline'])
    c.fill_circle(40, 38, 6, (38, 18, 10, 255))
    c.circle(40, 38, 6, PAL_BRONZE['metal_mid'])
    # Rune: Upward dual hook glyph
    c.line(40, 35, 40, 41, C_FIRE_ORANGE)
    c.line(37, 39, 40, 41, C_FIRE_ORANGE)
    c.line(43, 39, 40, 41, C_FIRE_ORANGE)
    c.pset(40, 35, C_WHITE)

    # Interlocking puzzle channels connecting the 3 runes
    c.line(32, 25, 24, 38, PAL_BRONZE['metal_hi'])
    c.line(32, 25, 40, 38, PAL_BRONZE['metal_hi'])
    c.line(24, 38, 40, 38, PAL_BRONZE['metal_hi'])

    return c


def draw_unique_8():
    """unique_8: Silver party of 8 tiny weapon icons in a ring."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # Central Silver Medallion Disc (Center: 32, 32, Radius: 18)
    c.fill_circle(32, 32, 19, PAL_SILVER['outline'])
    c.fill_circle(32, 32, 18, C_STEEL_SHADOW)
    c.fill_circle(31, 31, 17, C_STEEL_MID)
    c.fill_circle(32, 32, 15, (20, 26, 36, 255))  # Inner dark field

    # Ring of 8 Tiny RPG Weapon Icons arranged radially at 45-degree intervals!
    # Radius ~ 11 from center (32, 32)
    # 1. 12:00 (x: 32, y: 21) - Sword
    c.line(32, 19, 32, 24, C_STEEL_HI)
    c.line(31, 23, 33, 23, C_GOLD_HI)  # Guard
    c.pset(32, 18, C_WHITE)

    # 2. 1:30 (x: 40, y: 24) - Battleaxe
    c.line(38, 26, 42, 22, C_WOOD_MID)
    c.pset(42, 21, C_STEEL_HI)
    c.pset(43, 22, C_STEEL_HI)
    c.pset(41, 23, C_WHITE)

    # 3. 3:00 (x: 43, y: 32) - Bow
    c.line(43, 29, 45, 32, C_WOOD_HI)
    c.line(45, 32, 43, 35, C_WOOD_HI)
    c.line(43, 29, 43, 35, C_WHITE)  # Bowstring

    # 4. 4:30 (x: 40, y: 40) - Dagger
    c.line(39, 39, 42, 42, C_STEEL_HI)
    c.pset(42, 42, C_WHITE)
    c.pset(38, 38, C_GOLD_HI)

    # 5. 6:00 (x: 32, y: 43) - Barbell / Hammer
    c.line(29, 43, 35, 43, C_STEEL_HI)
    c.fill_rect(28, 42, 29, 44, C_STEEL_HI)
    c.fill_rect(35, 42, 36, 44, C_STEEL_HI)

    # 6. 7:30 (x: 24, y: 40) - Shield
    c.fill_rect(23, 39, 25, 42, C_STEEL_MID)
    c.line(23, 39, 25, 39, C_STEEL_HI)
    c.pset(24, 43, C_STEEL_DARK)

    # 7. 9:00 (x: 21, y: 32) - Wizard Staff
    c.line(21, 29, 21, 35, C_WOOD_MID)
    c.pset(21, 28, C_CYAN_HI)  # Staff crystal

    # 8. 10:30 (x: 24, y: 24) - Flanged Mace
    c.line(22, 26, 25, 23, C_STEEL_DARK)
    c.fill_circle(25, 22, 1, C_GOLD_HI)

    # Center Hero Star Crest (Center: 32, 32)
    c.fill_circle(32, 32, 3, C_STEEL_HI)
    c.pset(32, 32, C_WHITE)
    c.pset(32, 30, C_WHITE)
    c.pset(32, 34, C_WHITE)
    c.pset(30, 32, C_WHITE)
    c.pset(34, 32, C_WHITE)

    return c
