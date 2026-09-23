"""
draw_workouts.py
Draws the 6 workout achievements:
1. bronze_workout_1 (Rookie): Tiny bronze sword stuck in a wooden dummy
2. iron_workout_5 (Regular): Bronze kettlebell with a first dent
3. silver_workout_10 (Veteran): Silver barbell across a worn shield
4. steel_workout_25 (Grinder): Silver millstone crushing ore, sweat sparkles
5. gold_workout_50 (Legend): Gold hero statue holding a barbell overhead
6. platinum_workout_100 (Immortal): Platinum phoenix bursting from a weight plate
"""
from pixel_canvas import (
    PixelCanvas, PAL_BRONZE, PAL_SILVER, PAL_GOLD, PAL_PLATINUM,
    C_WOOD_HI, C_WOOD_MID, C_WOOD_DARK, C_WOOD_SHADOW,
    C_LEATHER_HI, C_LEATHER_MID, C_LEATHER_DARK,
    C_STEEL_HI, C_STEEL_MID, C_STEEL_DARK, C_STEEL_SHADOW,
    C_GOLD_HI, C_GOLD_MID, C_GOLD_DARK, C_GOLD_SHADOW,
    C_CYAN_HI, C_CYAN_MID, C_CYAN_DARK,
    C_FIRE_YELLOW, C_FIRE_ORANGE, C_FIRE_RED,
    C_WHITE, C_BLACK, C_EARTH_DARK, C_EARTH_MID
)


def draw_bronze_workout_1():
    """bronze_workout_1: Tiny bronze sword stuck in a wooden dummy."""
    c = PixelCanvas(64)
    c.draw_badge_frame('bronze')

    # Central Subject: Wooden training dummy
    # Dummy Stand / Base Post (x: 30-33, y: 44-50)
    c.fill_rect(29, 45, 34, 49, C_WOOD_SHADOW)
    c.fill_rect(30, 44, 33, 48, C_WOOD_DARK)
    c.fill_rect(30, 44, 31, 47, C_WOOD_MID)

    # Dummy Torso: cylindrical wooden body (x: 24 to 39, y: 24 to 44)
    # Outline
    c.rect(23, 23, 40, 44, C_WOOD_SHADOW)
    # Shading across the cylinder: Left highlight, center mid, right dark
    c.fill_rect(24, 24, 27, 43, C_WOOD_HI)
    c.fill_rect(28, 24, 34, 43, C_WOOD_MID)
    c.fill_rect(35, 24, 39, 43, C_WOOD_DARK)
    # Wood grain rings / lines
    c.line(25, 28, 38, 28, C_WOOD_DARK)
    c.line(26, 36, 39, 36, C_WOOD_SHADOW)

    # Cross-arms: Left arm peg (x: 16 to 23, y: 29 to 32)
    c.fill_rect(16, 29, 23, 32, C_WOOD_DARK)
    c.fill_rect(17, 29, 23, 30, C_WOOD_MID)
    c.fill_rect(18, 29, 23, 29, C_WOOD_HI)
    c.rect(15, 28, 23, 33, C_WOOD_SHADOW)

    # Right arm peg (x: 40 to 47, y: 29 to 32)
    c.fill_rect(40, 29, 47, 32, C_WOOD_DARK)
    c.fill_rect(40, 29, 46, 30, C_WOOD_MID)
    c.fill_rect(40, 31, 47, 32, C_WOOD_SHADOW)
    c.rect(39, 28, 48, 33, C_WOOD_SHADOW)

    # Dummy Head (x: 27 to 36, y: 15 to 22)
    c.fill_rect(27, 15, 36, 22, C_WOOD_MID)
    c.fill_rect(28, 15, 31, 21, C_WOOD_HI)
    c.fill_rect(33, 16, 36, 22, C_WOOD_DARK)
    c.rect(26, 14, 37, 23, C_WOOD_SHADOW)
    # Dummy painted target eyes / notches
    c.pset(29, 18, C_WOOD_SHADOW)
    c.pset(34, 18, C_WOOD_SHADOW)
    c.line(30, 20, 33, 20, C_WOOD_SHADOW)

    # Tiny Bronze Sword stuck diagonally in dummy's chest!
    # Entry point at (28, 33) into dummy
    # Blade stuck in wood (splinters)
    c.pset(27, 32, C_WOOD_HI)
    c.pset(29, 34, C_WOOD_HI)

    # Sword Blade sticking out from (22, 27) to (27, 32)
    blade_pts = [(23, 28), (24, 29), (25, 30), (26, 31), (27, 32)]
    for bx, by in blade_pts:
        c.pset(bx, by, PAL_BRONZE['metal_hi'])
        c.pset(bx - 1, by, PAL_BRONZE['outline'])
        c.pset(bx, by + 1, PAL_BRONZE['rust'])

    # Crossguard (perpendicular to blade) at (21, 26)
    c.line(19, 28, 24, 23, PAL_BRONZE['metal_hi'])
    c.line(19, 29, 24, 24, PAL_BRONZE['outline'])

    # Hilt / Grip (18, 23) to (16, 21)
    c.pset(18, 23, C_LEATHER_MID)
    c.pset(17, 22, C_LEATHER_DARK)
    c.pset(16, 21, C_LEATHER_MID)

    # Bronze Pommel at (15, 20)
    c.fill_circle(15, 20, 1, PAL_BRONZE['metal_hi'])
    c.pset(15, 20, PAL_BRONZE['edge_hi'])
    c.pset(16, 21, PAL_BRONZE['outline'])

    return c


def draw_iron_workout_5():
    """iron_workout_5: Bronze kettlebell with a first dent."""
    c = PixelCanvas(64)
    c.draw_badge_frame('bronze')

    # Heavy Bronze Kettlebell
    # Center: (32, 34)
    # 1. Handle (Arch at top from x: 23 to 41, y: 15 to 26)
    # Outer arch
    c.fill_circle(32, 23, 9, PAL_BRONZE['metal_dark'])
    c.fill_circle(32, 23, 8, PAL_BRONZE['metal_mid'])
    c.fill_circle(32, 22, 7, PAL_BRONZE['metal_hi'])
    # Hollow out the inside of the handle
    c.fill_circle(32, 24, 5, PAL_BRONZE['recess_bg'])
    c.circle(32, 24, 5, PAL_BRONZE['outline'])
    # Handle top highlight
    c.line(28, 15, 36, 15, PAL_BRONZE['edge_hi'])

    # 2. Main spherical kettlebell bell (Center: 32, 36, Radius: 12)
    c.fill_circle(32, 36, 13, PAL_BRONZE['outline'])
    c.fill_circle(32, 36, 12, PAL_BRONZE['metal_dark'])
    c.fill_circle(31, 35, 11, PAL_BRONZE['metal_mid'])
    c.fill_circle(29, 33, 8, PAL_BRONZE['metal_hi'])
    c.fill_circle(27, 31, 4, PAL_BRONZE['edge_hi'])
    c.pset(26, 30, C_WHITE)

    # Flat bottom base
    c.fill_rect(24, 46, 40, 48, PAL_BRONZE['outline'])
    c.fill_rect(25, 45, 39, 47, PAL_BRONZE['metal_dark'])
    c.line(26, 47, 38, 47, PAL_BRONZE['rust_dark'])

    # 3. First Battle Dent on the upper-right shoulder! (around x: 38-41, y: 30-33)
    # Carve a dent into the highlight/mid tone with dark crevice and bright rim
    c.fill_circle(39, 32, 2, PAL_BRONZE['outline'])
    c.pset(39, 32, PAL_BRONZE['rust_dark'])
    c.pset(40, 31, PAL_BRONZE['rust_dark'])
    c.pset(38, 33, PAL_BRONZE['outline'])
    # Bright impact scratch highlight on the dent's edge
    c.pset(37, 31, PAL_BRONZE['edge_hi'])
    c.pset(37, 32, C_WHITE)
    c.pset(39, 34, PAL_BRONZE['edge_hi'])

    return c


def draw_silver_workout_10():
    """silver_workout_10: Silver barbell across a worn shield."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # Knight's Worn Shield (Heater Shield)
    # Top edge from (22, 18) to (42, 18), curves down to (32, 47)
    # Shield outline
    shield_pts = [
        (22, 18), (42, 18), (44, 22), (44, 32),
        (38, 41), (32, 47), (26, 41), (20, 32), (20, 22)
    ]
    # Fill shield background (heraldic dark slate-blue)
    for y in range(18, 48):
        for x in range(20, 45):
            # Rough heater shield mask
            if y <= 32:
                if 20 <= x <= 44:
                    c.pset(x, y, (32, 48, 68, 255))
            else:
                dy = y - 32
                if 20 + dy <= x <= 44 - dy:
                    c.pset(x, y, (32, 48, 68, 255))

    # Shield silver beveled rim
    c.line(21, 18, 43, 18, C_STEEL_HI)
    c.line(20, 19, 20, 32, C_STEEL_HI)
    c.line(44, 19, 44, 32, C_STEEL_SHADOW)
    c.line(20, 32, 32, 47, C_STEEL_MID)
    c.line(44, 32, 32, 47, C_STEEL_SHADOW)

    # Shield emblem / cross in worn silver
    c.fill_rect(31, 21, 33, 40, (48, 70, 96, 255))
    c.fill_rect(24, 27, 40, 29, (48, 70, 96, 255))
    # Battle scratch marks on shield
    c.line(23, 22, 26, 26, C_STEEL_SHADOW)
    c.line(24, 22, 27, 26, C_STEEL_HI)
    c.line(36, 34, 40, 37, C_STEEL_SHADOW)

    # Silver Barbell diagonally across shield (from bottom-left 14, 46 to top-right 50, 18)
    # Bar shaft (14, 46) to (50, 18)
    c.line(14, 46, 50, 18, PAL_SILVER['outline'])
    c.line(15, 45, 51, 17, C_STEEL_HI)
    c.line(16, 44, 52, 16, C_STEEL_MID)

    # Weight Plates on Bottom-Left (15, 45)
    c.fill_rect(12, 43, 18, 49, PAL_SILVER['outline'])
    c.fill_rect(13, 44, 17, 48, C_STEEL_MID)
    c.line(13, 44, 17, 44, C_STEEL_HI)
    c.line(13, 48, 17, 48, C_STEEL_SHADOW)

    # Weight Plates on Top-Right (47, 19)
    c.fill_rect(46, 15, 52, 21, PAL_SILVER['outline'])
    c.fill_rect(47, 16, 51, 20, C_STEEL_MID)
    c.line(47, 16, 51, 16, C_STEEL_HI)
    c.line(47, 20, 51, 20, C_STEEL_SHADOW)

    # Inner collars
    c.fill_circle(20, 41, 1, C_STEEL_HI)
    c.fill_circle(44, 23, 1, C_STEEL_HI)

    return c


def draw_steel_workout_25():
    """steel_workout_25: Silver millstone crushing ore, sweat sparkles."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # Rough Iron Ore at bottom (x: 18 to 46, y: 43 to 48)
    for ox, oy, sz in [(22, 45, 3), (30, 46, 4), (40, 45, 3), (35, 44, 2)]:
        c.fill_circle(ox, oy, sz, PAL_SILVER['outline'])
        c.fill_circle(ox, oy, sz - 1, (70, 55, 45, 255))
        c.pset(ox - 1, oy - 1, (130, 110, 95, 255))
        c.pset(ox + 1, oy + 1, (35, 25, 20, 255))

    # Silver Millstone: Large circular wheel (Center: 32, 32, Radius: 13)
    c.fill_circle(32, 32, 14, PAL_SILVER['outline'])
    c.fill_circle(32, 32, 13, C_STEEL_SHADOW)
    c.fill_circle(31, 31, 12, C_STEEL_MID)
    c.fill_circle(30, 30, 11, C_STEEL_HI)
    c.fill_circle(32, 32, 9, C_STEEL_MID)

    # Stone grooves / chisels radiating from hub
    for dx, dy in [(-6, 0), (6, 0), (0, -6), (0, 6), (-4, -4), (4, -4), (-4, 4), (4, 4)]:
        c.line(32 + dx // 2, 32 + dy // 2, 32 + dx, 32 + dy, C_STEEL_SHADOW)

    # Center Iron Axle Hub
    c.fill_circle(32, 32, 4, PAL_SILVER['outline'])
    c.fill_circle(32, 32, 3, C_STEEL_DARK)
    c.fill_circle(31, 31, 2, C_STEEL_HI)
    c.pset(31, 31, C_WHITE)

    # Crushing dust & fragments at point of contact (x: 28 to 36, y: 44 to 46)
    c.pset(28, 44, C_STEEL_HI)
    c.pset(32, 45, C_WHITE)
    c.pset(35, 44, C_STEEL_HI)

    # Sweat Sparkles (brilliant 4-point stars representing hard effort)
    # Sparkle 1: Top-left (20, 18)
    c.pset(20, 18, C_WHITE)
    c.line(18, 18, 22, 18, C_CYAN_HI)
    c.line(20, 16, 20, 20, C_CYAN_HI)
    c.pset(20, 18, C_WHITE)

    # Sparkle 2: Top-right (44, 20)
    c.pset(44, 20, C_WHITE)
    c.line(42, 20, 46, 20, C_CYAN_HI)
    c.line(44, 18, 44, 22, C_CYAN_HI)
    c.pset(44, 20, C_WHITE)

    # Sparkle 3: Lower-left (16, 36)
    c.pset(16, 36, C_CYAN_HI)
    c.pset(16, 35, C_WHITE)

    return c


def draw_gold_workout_50():
    """gold_workout_50: Gold hero statue holding a barbell overhead."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Plinth / Pedestal at bottom (x: 22 to 42, y: 45 to 49)
    c.fill_rect(21, 45, 43, 49, PAL_GOLD['outline'])
    c.fill_rect(22, 45, 42, 46, C_GOLD_HI)
    c.fill_rect(22, 47, 42, 48, C_GOLD_MID)
    c.line(23, 49, 41, 49, C_GOLD_SHADOW)

    # Hero Legs / Base (x: 27 to 37, y: 38 to 44)
    c.fill_rect(28, 38, 31, 44, C_GOLD_MID)
    c.fill_rect(33, 38, 36, 44, C_GOLD_DARK)
    c.line(28, 38, 28, 44, C_GOLD_HI)

    # Hero Muscular Torso & Belt (x: 27 to 37, y: 27 to 37)
    c.fill_rect(28, 28, 36, 37, C_GOLD_MID)
    c.fill_rect(28, 28, 32, 35, C_GOLD_HI)
    c.fill_rect(33, 28, 36, 37, C_GOLD_DARK)
    # Belt buckle
    c.fill_rect(29, 36, 35, 37, PAL_GOLD['outline'])
    c.pset(32, 36, C_GOLD_HI)
    # Chest definition
    c.line(29, 30, 35, 30, C_GOLD_SHADOW)
    c.line(32, 28, 32, 34, C_GOLD_SHADOW)

    # Hero Head (x: 30 to 34, y: 21 to 26)
    c.fill_circle(32, 24, 3, C_GOLD_MID)
    c.fill_circle(31, 23, 2, C_GOLD_HI)
    c.rect(29, 21, 35, 27, PAL_GOLD['outline'])

    # Two Muscular Arms raised high in V-shape overhead!
    # Left arm (28, 28) -> (24, 22) -> (23, 17)
    c.line(28, 28, 24, 22, C_GOLD_HI)
    c.line(27, 28, 23, 22, PAL_GOLD['outline'])
    c.line(24, 22, 23, 17, C_GOLD_HI)
    c.fill_circle(23, 17, 2, C_GOLD_MID)
    c.pset(22, 16, C_GOLD_HI)

    # Right arm (36, 28) -> (40, 22) -> (41, 17)
    c.line(36, 28, 40, 22, C_GOLD_DARK)
    c.line(37, 28, 41, 22, PAL_GOLD['outline'])
    c.line(40, 22, 41, 17, C_GOLD_MID)
    c.fill_circle(41, 17, 2, C_GOLD_DARK)
    c.pset(41, 16, C_GOLD_HI)

    # Barbell Held Overhead! (Horizontal bar at y: 16, x: 14 to 50)
    c.line(14, 16, 50, 16, PAL_GOLD['outline'])
    c.line(14, 15, 50, 15, C_GOLD_HI)
    c.line(14, 17, 50, 17, C_GOLD_DARK)

    # Left Gold Plates (x: 14 to 19, y: 12 to 20)
    c.fill_rect(14, 12, 19, 20, PAL_GOLD['outline'])
    c.fill_rect(15, 13, 18, 19, C_GOLD_MID)
    c.line(15, 13, 18, 13, C_GOLD_HI)
    c.line(15, 19, 18, 19, C_GOLD_SHADOW)

    # Right Gold Plates (x: 45 to 50, y: 12 to 20)
    c.fill_rect(45, 12, 50, 20, PAL_GOLD['outline'])
    c.fill_rect(46, 13, 49, 19, C_GOLD_MID)
    c.line(46, 13, 49, 13, C_GOLD_HI)
    c.line(46, 19, 49, 19, C_GOLD_SHADOW)

    # Halo aura sparkles
    c.pset(32, 13, C_WHITE)
    c.pset(20, 13, C_FIRE_YELLOW)
    c.pset(44, 13, C_FIRE_YELLOW)

    return c


def draw_platinum_workout_100():
    """platinum_workout_100: Platinum phoenix bursting from a weight plate."""
    c = PixelCanvas(64)
    c.draw_badge_frame('platinum')

    # Cracked Olympic Weight Plate at bottom (Center: 32, 46, Radius: 12, top visible)
    c.fill_circle(32, 46, 12, PAL_PLATINUM['outline'])
    c.fill_circle(32, 46, 11, (45, 60, 80, 255))
    c.fill_circle(32, 46, 9, (30, 42, 60, 255))
    # Plate center hole
    c.fill_circle(32, 46, 4, PAL_PLATINUM['outline'])
    c.fill_circle(32, 46, 3, (20, 16, 38, 255))
    # Glowing fracture cracks in plate
    c.line(32, 42, 28, 46, C_CYAN_HI)
    c.line(32, 42, 36, 47, C_CYAN_HI)

    # Platinum Phoenix Soaring Upwards!
    # Phoenix Body (x: 30 to 34, y: 24 to 38)
    c.fill_rect(31, 24, 33, 38, C_WHITE)
    c.line(30, 26, 30, 36, C_CYAN_HI)
    c.line(34, 26, 34, 36, C_CYAN_MID)

    # Phoenix Head & Crown Crest (y: 16 to 23, x: 30 to 34)
    c.fill_circle(32, 21, 2, C_WHITE)
    c.pset(32, 19, C_CYAN_HI)
    c.pset(31, 18, C_WHITE)
    c.pset(33, 18, C_WHITE)
    c.pset(32, 17, C_CYAN_HI)
    # Phoenix Beak
    c.pset(32, 22, C_GOLD_HI)
    # Eye
    c.pset(31, 21, C_CYAN_DARK)

    # Outstretched Majestic Wings!
    # Left Wing (sweeps from (30, 28) up to (15, 18))
    left_wing = [
        (30, 30), (28, 28), (26, 26), (24, 23), (21, 21), (18, 19), (15, 18),
        (16, 22), (19, 25), (22, 28), (25, 32), (28, 35)
    ]
    for i in range(len(left_wing) - 1):
        c.line(left_wing[i][0], left_wing[i][1], left_wing[i + 1][0], left_wing[i + 1][1], C_CYAN_HI)
    # Fill left wing feathers
    for y in range(20, 34):
        for x in range(16, 31):
            if (x - 15) * 0.8 + 18 <= y <= 35 - (x - 15) * 0.3:
                c.pset(x, y, C_WHITE if (x + y) % 3 == 0 else C_CYAN_HI)

    # Right Wing (sweeps from (34, 28) up to (49, 18))
    right_wing = [
        (34, 30), (36, 28), (38, 26), (40, 23), (43, 21), (46, 19), (49, 18),
        (48, 22), (45, 25), (42, 28), (39, 32), (36, 35)
    ]
    for i in range(len(right_wing) - 1):
        c.line(right_wing[i][0], right_wing[i][1], right_wing[i + 1][0], right_wing[i + 1][1], C_CYAN_HI)
    # Fill right wing feathers
    for y in range(20, 34):
        for x in range(33, 49):
            if (49 - x) * 0.8 + 18 <= y <= 35 - (49 - x) * 0.3:
                c.pset(x, y, C_WHITE if (x + y) % 3 == 0 else C_CYAN_MID)

    # Phoenix Tail Plumage spreading into the cracked plate
    c.line(32, 38, 30, 44, C_CYAN_HI)
    c.line(32, 38, 34, 44, C_CYAN_HI)
    c.line(32, 38, 32, 45, C_WHITE)

    # Radiant Stardust Embers
    for ex, ey in [(18, 16), (46, 16), (14, 26), (50, 26), (32, 14)]:
        c.pset(ex, ey, C_WHITE)

    return c
