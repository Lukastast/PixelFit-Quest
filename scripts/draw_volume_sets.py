"""
draw_volume_sets.py
Draws the 3 volume achievements and 3 sets achievements:
1. volume_1000 (Iron Novice): Bronze anvil, one small bar
2. volume_10000 (Iron Veteran): Silver anvil stacked with plates
3. volume_50000 (Iron Titan): Gold titan lifting a mountain-anvil
4. sets_10 (Set Starter): Bronze checklist with 10 ticks
5. sets_50 (Set Grinder): Silver mill grinding set-stones
6. sets_200 (Set Machine): Gold clockwork golem doing reps
"""
from pixel_canvas import (
    PixelCanvas, PAL_BRONZE, PAL_SILVER, PAL_GOLD, PAL_PLATINUM,
    C_WOOD_HI, C_WOOD_MID, C_WOOD_DARK, C_WOOD_SHADOW,
    C_LEATHER_HI, C_LEATHER_MID, C_LEATHER_DARK,
    C_STEEL_HI, C_STEEL_MID, C_STEEL_DARK, C_STEEL_SHADOW,
    C_GOLD_HI, C_GOLD_MID, C_GOLD_DARK, C_GOLD_SHADOW,
    C_CYAN_HI, C_CYAN_MID, C_CYAN_DARK,
    C_FIRE_YELLOW, C_FIRE_ORANGE, C_FIRE_RED,
    C_RED, C_DARK_RED, C_WHITE, C_BLACK
)


# =========================================================================
# VOLUME ACHIEVEMENTS
# =========================================================================

def draw_volume_1000():
    """volume_1000: Bronze anvil, one small bar."""
    c = PixelCanvas(64)
    c.draw_badge_frame('bronze')

    # Heavy Blacksmith Anvil in Bronze/Cast Metal (x: 16 to 48, y: 31 to 48)
    # Anvil Base Footing (x: 20 to 44, y: 44 to 48)
    c.fill_rect(19, 44, 45, 48, PAL_BRONZE['outline'])
    c.fill_rect(20, 44, 44, 47, PAL_BRONZE['metal_dark'])
    c.line(20, 44, 44, 44, PAL_BRONZE['metal_mid'])

    # Anvil Waist (x: 26 to 38, y: 37 to 43)
    c.fill_rect(25, 37, 39, 43, PAL_BRONZE['outline'])
    c.fill_rect(26, 37, 38, 43, PAL_BRONZE['metal_dark'])
    c.line(26, 37, 29, 43, PAL_BRONZE['metal_mid'])

    # Anvil Horn (tapering left: (17, 32) to (26, 36))
    horn_pts = [(17, 32), (25, 31), (25, 36), (18, 34)]
    c.fill_rect(17, 32, 25, 36, PAL_BRONZE['metal_dark'])
    c.line(17, 32, 25, 31, PAL_BRONZE['metal_hi'])
    c.line(17, 33, 25, 32, PAL_BRONZE['edge_hi'])
    c.line(17, 34, 25, 36, PAL_BRONZE['outline'])

    # Anvil Body & Flat Face (x: 25 to 47, y: 31 to 37)
    c.fill_rect(24, 30, 48, 37, PAL_BRONZE['outline'])
    c.fill_rect(25, 31, 47, 36, PAL_BRONZE['metal_dark'])
    c.line(25, 31, 47, 31, PAL_BRONZE['edge_hi'])  # Top face highlight
    c.line(25, 32, 47, 32, PAL_BRONZE['metal_hi'])
    c.line(25, 36, 47, 36, PAL_BRONZE['rust_dark'])

    # Pritchel / Hardy Hole on right face
    c.pset(43, 31, PAL_BRONZE['outline'])
    c.pset(44, 31, PAL_BRONZE['outline'])

    # One Small Bar resting flat on the anvil face (x: 28 to 39, y: 25 to 29)
    c.fill_rect(27, 24, 40, 30, PAL_BRONZE['outline'])
    c.fill_rect(28, 25, 39, 29, PAL_BRONZE['metal_mid'])
    c.fill_rect(28, 25, 38, 26, PAL_BRONZE['edge_hi'])  # Top bevel
    c.line(28, 29, 39, 29, PAL_BRONZE['rust_dark'])     # Bottom shadow
    # Cast bar stamped center mark
    c.pset(33, 27, PAL_BRONZE['outline'])
    c.pset(34, 27, PAL_BRONZE['outline'])

    return c


def draw_volume_10000():
    """volume_10000: Silver anvil stacked with plates."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # Polished Silver Anvil at bottom (y: 35 to 48)
    # Base (x: 21 to 43, y: 44 to 48)
    c.fill_rect(20, 44, 44, 48, PAL_SILVER['outline'])
    c.fill_rect(21, 45, 43, 47, C_STEEL_DARK)
    c.line(21, 44, 43, 44, C_STEEL_MID)

    # Waist (x: 26 to 38, y: 38 to 43)
    c.fill_rect(26, 38, 38, 43, C_STEEL_SHADOW)
    c.line(26, 38, 28, 43, C_STEEL_MID)

    # Horn & Body
    c.fill_rect(18, 35, 46, 39, C_STEEL_DARK)
    c.line(18, 35, 46, 35, C_STEEL_HI)
    c.rect(17, 34, 47, 40, PAL_SILVER['outline'])

    # Stack of Weight Plates resting high on the anvil face!
    # Plate 1 (Bottom, largest: x: 23 to 41, y: 31 to 34)
    c.fill_rect(22, 30, 42, 34, PAL_SILVER['outline'])
    c.fill_rect(23, 31, 41, 33, C_STEEL_MID)
    c.line(23, 31, 41, 31, C_STEEL_HI)
    c.line(23, 33, 41, 33, C_STEEL_SHADOW)

    # Plate 2 (x: 25 to 39, y: 26 to 29)
    c.fill_rect(24, 25, 40, 29, PAL_SILVER['outline'])
    c.fill_rect(25, 26, 39, 28, C_STEEL_MID)
    c.line(25, 26, 39, 26, C_STEEL_HI)
    c.line(25, 28, 39, 28, C_STEEL_SHADOW)

    # Plate 3 (x: 27 to 37, y: 21 to 24)
    c.fill_rect(26, 20, 38, 24, PAL_SILVER['outline'])
    c.fill_rect(27, 21, 37, 23, C_STEEL_MID)
    c.line(27, 21, 37, 21, C_STEEL_HI)
    c.line(27, 23, 37, 23, C_STEEL_SHADOW)

    # Plate 4 (Top, smallest: x: 29 to 35, y: 16 to 19)
    c.fill_rect(28, 15, 36, 19, PAL_SILVER['outline'])
    c.fill_rect(29, 16, 35, 18, C_STEEL_HI)
    c.line(29, 16, 35, 16, C_WHITE)
    c.line(29, 18, 35, 18, C_STEEL_SHADOW)

    # Weight plate center vertical pin / bar holding the stack
    c.fill_rect(31, 14, 33, 34, PAL_SILVER['outline'])
    c.line(32, 14, 32, 34, C_WHITE)

    # Sparkle of immense weight
    c.pset(22, 28, C_WHITE)
    c.pset(42, 28, C_WHITE)

    return c


def draw_volume_50000():
    """volume_50000: Gold titan lifting a mountain-anvil."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Mythological Titan (Atlas/Hercules silhouette) at bottom (y: 33 to 48)
    # Sturdy legs & pelvis (x: 25 to 39, y: 39 to 48)
    c.fill_rect(26, 40, 30, 48, C_GOLD_DARK)
    c.fill_rect(34, 40, 38, 48, C_GOLD_DARK)
    c.line(26, 40, 27, 47, C_GOLD_MID)
    c.line(37, 40, 38, 47, C_GOLD_SHADOW)

    # Broad Muscular Torso (x: 25 to 39, y: 31 to 39)
    c.fill_rect(26, 31, 38, 39, C_GOLD_MID)
    c.fill_rect(28, 31, 36, 37, C_GOLD_HI)
    c.line(32, 32, 32, 38, C_GOLD_SHADOW)
    # Head bowed in immense effort
    c.fill_circle(32, 29, 3, C_GOLD_MID)
    c.pset(32, 28, C_GOLD_HI)

    # Mighty Arms hoisting upward: Left arm (26, 32) -> (21, 26) -> (22, 21)
    c.line(26, 32, 21, 26, C_GOLD_HI)
    c.line(21, 26, 22, 21, C_GOLD_HI)
    c.fill_circle(21, 22, 2, C_GOLD_MID)
    # Right arm: (38, 32) -> (43, 26) -> (42, 21)
    c.line(38, 32, 43, 26, C_GOLD_DARK)
    c.line(43, 26, 42, 21, C_GOLD_MID)
    c.fill_circle(43, 22, 2, C_GOLD_DARK)

    # Mountain-Anvil (Massive anvil forged like a jagged mountain, hoisted aloft)
    # Top Flat Face of the Mountain-Anvil (y: 15 to 19, x: 15 to 49)
    c.fill_rect(14, 14, 50, 19, PAL_GOLD['outline'])
    c.fill_rect(15, 15, 49, 18, C_GOLD_MID)
    c.line(15, 15, 49, 15, C_WHITE)       # Pure brilliant peak highlight
    c.line(15, 16, 49, 16, C_GOLD_HI)

    # Anvil Left Mountain Ridge (15, 19) down to (24, 28)
    c.line(15, 19, 24, 28, PAL_GOLD['outline'])
    # Anvil Right Mountain Ridge (49, 19) down to (40, 28)
    c.line(49, 19, 40, 28, PAL_GOLD['outline'])

    # Mountain body fill
    for y in range(19, 28):
        t = (y - 19) / 9.0
        lx = int(15 + t * 9)
        rx = int(49 - t * 9)
        for x in range(lx, rx + 1):
            if x < 32:
                c.pset(x, y, C_GOLD_HI if (x + y) % 3 == 0 else C_GOLD_MID)
            else:
                c.pset(x, y, C_GOLD_DARK if (x + y) % 2 == 0 else C_GOLD_SHADOW)

    # Fissures of power in the mountain anvil
    c.line(32, 16, 28, 24, C_FIRE_YELLOW)
    c.line(32, 16, 35, 23, C_FIRE_YELLOW)

    # Aura sparkles
    c.pset(14, 13, C_FIRE_YELLOW)
    c.pset(50, 13, C_FIRE_YELLOW)
    c.pset(32, 13, C_WHITE)

    return c


# =========================================================================
# SETS ACHIEVEMENTS
# =========================================================================

def draw_sets_10():
    """sets_10: Bronze checklist with 10 ticks."""
    c = PixelCanvas(64)
    c.draw_badge_frame('bronze')

    # Ancient Parchment / Quest Scroll (x: 18 to 46, y: 15 to 49)
    c.fill_rect(17, 14, 47, 50, PAL_BRONZE['outline'])
    # Parchment fill (warm aged paper)
    c.fill_rect(18, 15, 46, 49, (230, 210, 175, 255))
    c.line(18, 15, 46, 15, (250, 235, 205, 255))
    c.line(18, 49, 46, 49, (180, 150, 115, 255))

    # Bronze Seal / Pin at top center (x: 30 to 34, y: 14 to 17)
    c.fill_circle(32, 16, 3, PAL_BRONZE['outline'])
    c.fill_circle(32, 16, 2, PAL_BRONZE['metal_hi'])
    c.pset(31, 15, PAL_BRONZE['edge_hi'])

    # 10 Ticks: Two Columns of 5 Checkboxes each with bold Red Checkmarks!
    # Row Y coordinates for the 5 rows: 21, 27, 33, 39, 45
    rows_y = [21, 27, 33, 39, 45]

    # Column 1 (Left): Checkbox at x: 21-25
    for ry in rows_y:
        # Box outline
        c.rect(21, ry, 25, ry + 4, PAL_BRONZE['outline'])
        c.fill_rect(22, ry + 1, 24, ry + 3, C_WHITE)
        # Red Checkmark Tick inside box (v shape)
        c.pset(22, ry + 2, C_RED)
        c.pset(23, ry + 3, C_RED)
        c.pset(24, ry + 1, C_RED)
        # Text placeholder line next to box
        c.line(27, ry + 2, 31, ry + 2, (150, 125, 95, 255))

    # Column 2 (Right): Checkbox at x: 34-38
    for ry in rows_y:
        # Box outline
        c.rect(34, ry, 38, ry + 4, PAL_BRONZE['outline'])
        c.fill_rect(35, ry + 1, 37, ry + 3, C_WHITE)
        # Red Checkmark Tick
        c.pset(35, ry + 2, C_RED)
        c.pset(36, ry + 3, C_RED)
        c.pset(37, ry + 1, C_RED)
        # Text placeholder line next to box
        c.line(40, ry + 2, 44, ry + 2, (150, 125, 95, 255))

    return c


def draw_sets_50():
    """sets_50: Silver mill grinding set-stones."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # Stone slabs / set-stones at the bottom being ground (x: 18 to 46, y: 41 to 48)
    for sx, sy, w, h in [(18, 42, 8, 5), (28, 44, 9, 4), (39, 43, 8, 5)]:
        c.fill_rect(sx - 1, sy - 1, sx + w, sy + h, PAL_SILVER['outline'])
        c.fill_rect(sx, sy, sx + w - 1, sy + h - 1, (100, 110, 120, 255))
        c.line(sx, sy, sx + w - 1, sy, (160, 175, 190, 255))
        # Tally marks etched on stones
        c.line(sx + 2, sy + 1, sx + 2, sy + 3, PAL_SILVER['outline'])
        c.line(sx + 4, sy + 1, sx + 4, sy + 3, PAL_SILVER['outline'])

    # Crushed stone dust / sparks at ground point (28 to 36, y: 41)
    c.pset(30, 41, C_WHITE)
    c.pset(33, 40, C_STEEL_HI)
    c.pset(35, 41, C_WHITE)

    # Heavy Silver Grinding Mill Wheel (Center: 32, 28, Radius: 12)
    c.fill_circle(32, 28, 13, PAL_SILVER['outline'])
    c.fill_circle(32, 28, 12, C_STEEL_SHADOW)
    c.fill_circle(31, 27, 11, C_STEEL_MID)
    c.fill_circle(30, 26, 10, C_STEEL_HI)
    c.fill_circle(32, 28, 8, C_STEEL_MID)

    # Mill Cog Teeth radiating around rim
    cog_offsets = [
        (0, -13), (9, -9), (13, 0), (9, 9),
        (0, 13), (-9, 9), (-13, 0), (-9, -9)
    ]
    for dx, dy in cog_offsets:
        c.fill_circle(32 + dx, 28 + dy, 2, C_STEEL_HI)
        c.pset(32 + dx, 28 + dy, C_WHITE)

    # Center Hub & Axle Pin
    c.fill_circle(32, 28, 4, PAL_SILVER['outline'])
    c.fill_circle(32, 28, 3, C_STEEL_DARK)
    c.fill_circle(31, 27, 2, C_WHITE)

    return c


def draw_sets_200():
    """sets_200: Gold clockwork golem doing reps."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Clockwork Golem Body (Torso: x: 26 to 38, y: 28 to 44)
    c.fill_rect(25, 27, 39, 45, PAL_GOLD['outline'])
    c.fill_rect(26, 28, 38, 44, C_GOLD_MID)
    c.fill_rect(26, 28, 30, 44, C_GOLD_HI)
    c.fill_rect(35, 28, 38, 44, C_GOLD_SHADOW)

    # Exposed Clockwork Gears in Chest (x: 28 to 36, y: 32 to 40)
    c.fill_rect(28, 32, 36, 40, (30, 12, 4, 255))
    # Gear 1 (Upper gear)
    c.fill_circle(31, 35, 3, C_FIRE_YELLOW)
    c.pset(31, 35, (30, 12, 4, 255))  # Axle
    # Gear 2 (Lower interlocking gear)
    c.fill_circle(34, 37, 2, C_GOLD_HI)
    c.pset(34, 37, (30, 12, 4, 255))

    # Golem Head (x: 28 to 36, y: 20 to 27)
    c.fill_rect(27, 19, 37, 27, PAL_GOLD['outline'])
    c.fill_rect(28, 20, 36, 26, C_GOLD_HI)
    c.fill_rect(33, 20, 36, 26, C_GOLD_DARK)
    # Glowing Cyan Optic Sensors (Eyes)
    c.pset(30, 23, C_CYAN_HI)
    c.pset(34, 23, C_CYAN_HI)
    # Brass brow ridge
    c.line(28, 21, 36, 21, C_WHITE)

    # Golem Mechanical Piston Arms lifting Barbell overhead!
    # Left Arm: Shoulder (25, 29) -> Elbow (21, 23) -> Wrist (22, 17)
    c.line(25, 29, 21, 23, C_GOLD_HI)
    c.line(21, 23, 22, 17, C_GOLD_HI)
    c.fill_circle(21, 23, 2, C_GOLD_MID)  # Elbow gear
    c.fill_circle(22, 17, 2, C_GOLD_HI)   # Hand clasp

    # Right Arm: Shoulder (39, 29) -> Elbow (43, 23) -> Wrist (42, 17)
    c.line(39, 29, 43, 23, C_GOLD_DARK)
    c.line(43, 23, 42, 17, C_GOLD_MID)
    c.fill_circle(43, 23, 2, C_GOLD_DARK)
    c.fill_circle(42, 17, 2, C_GOLD_MID)

    # Heavy Gear Barbell Overhead (Horizontal at y: 16, x: 15 to 49)
    c.line(15, 16, 49, 16, PAL_GOLD['outline'])
    c.line(15, 15, 49, 15, C_WHITE)
    c.line(15, 17, 49, 17, C_GOLD_DARK)

    # Left Gear-Plates (x: 14 to 19, y: 11 to 21)
    c.fill_rect(14, 11, 19, 21, PAL_GOLD['outline'])
    c.fill_rect(15, 12, 18, 20, C_GOLD_MID)
    c.line(15, 12, 18, 12, C_GOLD_HI)
    c.line(15, 20, 18, 20, C_GOLD_SHADOW)
    # Gear teeth notches
    c.pset(13, 16, C_GOLD_HI)
    c.pset(16, 10, C_GOLD_HI)
    c.pset(16, 22, C_GOLD_DARK)

    # Right Gear-Plates (x: 45 to 50, y: 11 to 21)
    c.fill_rect(45, 11, 50, 21, PAL_GOLD['outline'])
    c.fill_rect(46, 12, 49, 20, C_GOLD_MID)
    c.line(46, 12, 49, 12, C_GOLD_HI)
    c.line(46, 20, 49, 20, C_GOLD_SHADOW)
    # Gear teeth notches
    c.pset(51, 16, C_GOLD_HI)
    c.pset(48, 10, C_GOLD_HI)
    c.pset(48, 22, C_GOLD_DARK)

    return c
