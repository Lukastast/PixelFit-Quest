"""
draw_locked_form.py
Draws the shared locked achievement and the 5 future IMU/form achievements:
1. locked_achievement: Same badge shape, grey stone, cracked, chain across it
2. form_even_bar (True Bar): Perfectly level gold barbell, spirit-level bubble centered
3. form_square (Square Press): Two arms as matching pillars under a square bar
4. form_no_dump (Slow Lower): Barbell descending on a golden chain, not falling
5. clip_true (Right Sleeve): Phone clipped to the right bar sleeve, glowing pose
6. rom_honest (Full Stroke): Hero at the bottom of a squat/press, full depth, no cheat
"""
from pixel_canvas import (
    PixelCanvas, PAL_BRONZE, PAL_SILVER, PAL_GOLD, PAL_PLATINUM, PAL_LOCKED,
    C_WOOD_HI, C_WOOD_MID, C_WOOD_DARK, C_WOOD_SHADOW,
    C_LEATHER_HI, C_LEATHER_MID, C_LEATHER_DARK,
    C_STEEL_HI, C_STEEL_MID, C_STEEL_DARK, C_STEEL_SHADOW,
    C_GOLD_HI, C_GOLD_MID, C_GOLD_DARK, C_GOLD_SHADOW,
    C_GREEN_HI, C_GREEN_MID, C_GREEN_DARK,
    C_FIRE_YELLOW, C_FIRE_ORANGE, C_FIRE_RED,
    C_CYAN_HI, C_CYAN_MID, C_CYAN_DARK,
    C_RED, C_DARK_RED, C_WHITE, C_BLACK
)


def draw_locked_achievement():
    """locked_achievement: Same badge shape, grey stone, cracked, chain across it."""
    c = PixelCanvas(64)
    c.draw_badge_frame('locked')

    # Chiseled Fissures / Deep Cracks across the stone face
    # Main crack 1: From (30, 12) down through (26, 24), (35, 34), to (42, 51)
    c.line(30, 12, 26, 24, PAL_LOCKED['fissure'])
    c.line(26, 24, 35, 34, PAL_LOCKED['fissure'])
    c.line(35, 34, 42, 51, PAL_LOCKED['fissure'])
    # Crack highlight rim
    c.line(31, 12, 27, 24, PAL_LOCKED['stone_hi'])
    c.line(27, 24, 36, 34, PAL_LOCKED['stone_hi'])

    # Secondary branch crack: From (26, 24) down-left to (15, 36)
    c.line(26, 24, 15, 36, PAL_LOCKED['fissure'])
    c.line(26, 25, 15, 37, PAL_LOCKED['stone_hi'])

    # Tertiary crack: From (35, 34) right to (48, 28)
    c.line(35, 34, 48, 28, PAL_LOCKED['fissure'])

    # Heavy Forged Iron Chains crossing diagonally across the stone badge!
    # Chain 1: Top-left (14, 14) to Bottom-right (50, 50)
    # Draw individual interlocking 3D iron links
    pts1 = [
        (15, 15), (19, 19), (23, 23), (27, 27),
        (37, 37), (41, 41), (45, 45), (49, 49)
    ]
    for lx, ly in pts1:
        # Link outer ring (oval)
        c.fill_rect(lx - 2, ly - 2, lx + 2, ly + 2, PAL_LOCKED['chain_edge'])
        c.fill_rect(lx - 1, ly - 1, lx + 1, ly + 1, PAL_LOCKED['chain_mid'])
        c.line(lx - 1, ly - 1, lx + 1, ly - 1, PAL_LOCKED['chain_hi'])
        c.pset(lx, ly, PAL_LOCKED['chain_dark'])  # Hollow center

    # Chain 2: Top-right (50, 14) to Bottom-left (14, 50)
    pts2 = [
        (49, 15), (45, 19), (41, 23), (37, 27),
        (27, 37), (23, 41), (19, 45), (15, 49)
    ]
    for lx, ly in pts2:
        c.fill_rect(lx - 2, ly - 2, lx + 2, ly + 2, PAL_LOCKED['chain_edge'])
        c.fill_rect(lx - 1, ly - 1, lx + 1, ly + 1, PAL_LOCKED['chain_mid'])
        c.line(lx - 1, ly - 1, lx + 1, ly - 1, PAL_LOCKED['chain_hi'])
        c.pset(lx, ly, PAL_LOCKED['chain_dark'])

    # Massive Center Iron Padlock Boss binding the crossed chains together!
    # Padlock Shackle / Arch (x: 27 to 37, y: 22 to 30)
    c.fill_circle(32, 27, 5, PAL_LOCKED['chain_edge'])
    c.fill_circle(32, 27, 4, PAL_LOCKED['chain_mid'])
    c.fill_circle(32, 27, 2, PAL_LOCKED['recess_dark'])
    c.line(29, 23, 35, 23, PAL_LOCKED['chain_hi'])

    # Padlock Heavy Body (x: 26 to 38, y: 29 to 41)
    c.fill_rect(25, 28, 39, 41, PAL_LOCKED['chain_edge'])
    c.fill_rect(26, 29, 38, 40, PAL_LOCKED['chain_mid'])
    c.line(26, 29, 38, 29, PAL_LOCKED['chain_hi'])
    c.line(26, 30, 26, 39, PAL_LOCKED['chain_hi'])
    c.line(26, 40, 38, 40, PAL_LOCKED['chain_dark'])
    c.line(38, 30, 38, 40, PAL_LOCKED['chain_dark'])

    # Keyhole in Center of Lock
    c.fill_circle(32, 34, 2, PAL_LOCKED['chain_edge'])
    c.fill_rect(31, 34, 33, 37, PAL_LOCKED['chain_edge'])
    c.pset(32, 34, C_BLACK)
    c.pset(32, 35, C_BLACK)
    c.pset(32, 36, C_BLACK)

    # Rivets on lock corners
    c.pset(27, 30, PAL_LOCKED['chain_hi'])
    c.pset(37, 30, PAL_LOCKED['chain_hi'])
    c.pset(27, 39, PAL_LOCKED['chain_dark'])
    c.pset(37, 39, PAL_LOCKED['chain_dark'])

    return c


def draw_form_even_bar():
    """form_even_bar (True Bar): Perfectly level gold barbell, spirit-level bubble centered."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Perfectly Horizontal Barbell Shaft at y: 32 (x: 14 to 50)
    c.line(14, 32, 50, 32, PAL_GOLD['outline'])
    c.line(14, 31, 50, 31, C_WHITE)
    c.line(14, 33, 50, 33, C_GOLD_DARK)

    # Left Weight Plates (x: 14 to 20, y: 22 to 42)
    c.fill_rect(14, 22, 20, 42, PAL_GOLD['outline'])
    c.fill_rect(15, 23, 19, 41, C_GOLD_MID)
    c.line(15, 23, 19, 23, C_GOLD_HI)
    c.line(15, 41, 19, 41, C_GOLD_SHADOW)
    # Plate collar
    c.fill_rect(21, 28, 23, 36, C_GOLD_HI)
    c.rect(20, 27, 24, 37, PAL_GOLD['outline'])

    # Right Weight Plates (x: 44 to 50, y: 22 to 42)
    c.fill_rect(44, 22, 50, 42, PAL_GOLD['outline'])
    c.fill_rect(45, 23, 49, 41, C_GOLD_MID)
    c.line(45, 23, 49, 23, C_GOLD_HI)
    c.line(45, 41, 49, 41, C_GOLD_SHADOW)
    # Plate collar
    c.fill_rect(41, 28, 43, 36, C_GOLD_HI)
    c.rect(40, 27, 44, 37, PAL_GOLD['outline'])

    # Spirit-Level Tubular Vial centered on the bar! (x: 25 to 39, y: 28 to 36)
    c.fill_rect(24, 27, 40, 37, PAL_GOLD['outline'])
    # Brass Mounting Caps on ends
    c.fill_rect(24, 28, 26, 36, C_GOLD_HI)
    c.fill_rect(38, 28, 40, 36, C_GOLD_HI)

    # Glass Vial Chamber (x: 27 to 37, y: 28 to 36)
    c.fill_rect(27, 28, 37, 36, (20, 60, 25, 255))
    # Glowing Neon/Lime Green Fluid
    c.fill_rect(27, 29, 37, 35, C_GREEN_MID)
    c.fill_rect(27, 30, 37, 34, C_GREEN_HI)

    # Two Black Indicator Target Lines (marking exact balance zone)
    c.line(30, 29, 30, 35, C_BLACK)
    c.line(34, 29, 34, 35, C_BLACK)

    # Perfectly Centered Bright White Air Bubble (x: 31 to 33, y: 30 to 34)
    c.fill_circle(32, 32, 2, C_WHITE)
    c.pset(32, 32, (230, 255, 220, 255))

    # Glass highlight reflection streak
    c.line(27, 29, 37, 29, C_WHITE)

    # Balance check sparkles / level arrows
    c.pset(32, 23, C_WHITE)
    c.pset(31, 24, C_FIRE_YELLOW)
    c.pset(33, 24, C_FIRE_YELLOW)
    c.pset(32, 41, C_WHITE)

    return c


def draw_form_square():
    """form_square (Square Press): Two arms as matching pillars under a square bar."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Rigid Square-Aligned Horizontal Bar at y: 22 (x: 15 to 49)
    c.fill_rect(15, 20, 49, 24, PAL_GOLD['outline'])
    c.fill_rect(16, 21, 48, 23, C_STEEL_MID)
    c.line(16, 21, 48, 21, C_WHITE)

    # Plates on the bar ends
    c.fill_rect(14, 16, 19, 28, PAL_GOLD['outline'])
    c.fill_rect(15, 17, 18, 27, C_STEEL_HI)
    c.fill_rect(45, 16, 50, 28, PAL_GOLD['outline'])
    c.fill_rect(46, 17, 49, 27, C_STEEL_HI)

    # Two Arms as Matching Classical Pillars / Architectural Columns
    # Left Arm Pillar (x: 23 to 29, y: 24 to 48)
    # Capital / Fist clasping bar at top
    c.fill_rect(22, 24, 30, 27, PAL_GOLD['outline'])
    c.fill_rect(23, 25, 29, 26, C_GOLD_HI)
    # Pillar Shaft (muscular armored vertical column)
    c.fill_rect(23, 27, 29, 44, PAL_GOLD['outline'])
    c.fill_rect(24, 28, 28, 43, C_GOLD_MID)
    c.line(24, 28, 25, 43, C_GOLD_HI)
    c.line(28, 28, 28, 43, C_GOLD_SHADOW)
    # Pillar Base
    c.fill_rect(22, 44, 30, 48, PAL_GOLD['outline'])
    c.fill_rect(23, 45, 29, 47, C_GOLD_MID)
    c.line(23, 45, 29, 45, C_GOLD_HI)

    # Right Arm Pillar (x: 35 to 41, y: 24 to 48) - Perfectly Symmetrical!
    # Capital / Fist clasping bar
    c.fill_rect(34, 24, 42, 27, PAL_GOLD['outline'])
    c.fill_rect(35, 25, 41, 26, C_GOLD_HI)
    # Pillar Shaft
    c.fill_rect(35, 27, 41, 44, PAL_GOLD['outline'])
    c.fill_rect(36, 28, 40, 43, C_GOLD_MID)
    c.line(36, 28, 37, 43, C_GOLD_HI)
    c.line(40, 28, 40, 43, C_GOLD_SHADOW)
    # Pillar Base
    c.fill_rect(34, 44, 42, 48, PAL_GOLD['outline'])
    c.fill_rect(35, 45, 41, 47, C_GOLD_MID)
    c.line(35, 45, 41, 45, C_GOLD_HI)

    # Symmetrical Square Grid Alignment Lines (showing 0 twist / perfectly orthogonal)
    c.line(32, 16, 32, 48, (70, 35, 15, 255))  # Center plumb line
    c.pset(32, 22, C_FIRE_YELLOW)
    c.pset(32, 32, C_FIRE_YELLOW)

    return c


def draw_form_no_dump():
    """form_no_dump (Slow Lower): Barbell descending on a golden chain, not falling."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Top Anchor Beam / Pulleys at y: 14 to 17 (x: 20 to 44)
    c.fill_rect(20, 14, 44, 17, PAL_GOLD['outline'])
    c.fill_rect(21, 15, 43, 16, C_GOLD_MID)
    c.line(21, 15, 43, 15, C_GOLD_HI)

    # Twin Golden Hoist Chains extending down from anchors!
    # Left Chain (at x: 25, from y: 17 to 35)
    for cy in range(17, 36, 3):
        c.fill_rect(24, cy, 26, cy + 2, PAL_GOLD['outline'])
        c.pset(25, cy + 1, C_GOLD_HI)
        c.pset(25, cy, C_WHITE)

    # Right Chain (at x: 39, from y: 17 to 35)
    for cy in range(17, 36, 3):
        c.fill_rect(38, cy, 40, cy + 2, PAL_GOLD['outline'])
        c.pset(39, cy + 1, C_GOLD_HI)
        c.pset(39, cy, C_WHITE)

    # Loaded Barbell smoothly descending at y: 36 (x: 14 to 50)
    c.line(14, 36, 50, 36, PAL_GOLD['outline'])
    c.line(14, 35, 50, 35, C_WHITE)
    c.line(14, 37, 50, 37, C_GOLD_DARK)

    # Weight Plates on Left (x: 14 to 20, y: 28 to 44)
    c.fill_rect(14, 28, 20, 44, PAL_GOLD['outline'])
    c.fill_rect(15, 29, 19, 43, C_GOLD_MID)
    c.line(15, 29, 19, 29, C_GOLD_HI)
    c.line(15, 43, 19, 43, C_GOLD_SHADOW)

    # Weight Plates on Right (x: 44 to 50, y: 28 to 44)
    c.fill_rect(44, 28, 50, 44, PAL_GOLD['outline'])
    c.fill_rect(45, 29, 49, 43, C_GOLD_MID)
    c.line(45, 29, 49, 29, C_GOLD_HI)
    c.line(45, 43, 49, 43, C_GOLD_SHADOW)

    # Controlled Descent Tempo Indicators (Smooth speed control particles)
    for my in [28, 31, 34]:
        c.pset(17, my, (160, 100, 20, 255))
        c.pset(47, my, (160, 100, 20, 255))
        c.pset(32, my, (140, 80, 15, 255))

    # Cushion / Solid ground beneath (no crash, perfect safety)
    c.fill_rect(22, 46, 42, 48, (60, 28, 10, 255))
    c.line(22, 46, 42, 46, (120, 60, 20, 255))

    return c


def draw_clip_true():
    """clip_true (Right Sleeve): Phone clipped to the right bar sleeve, glowing pose."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # Barbell Right Sleeve & Collar (Zoomed perspective on right barbell end)
    # Shaft entering from left: y: 31 to 35, x: 14 to 24
    c.fill_rect(14, 30, 24, 36, PAL_SILVER['outline'])
    c.fill_rect(14, 31, 24, 35, C_STEEL_MID)
    c.line(14, 31, 24, 31, C_WHITE)

    # Large Olympic Weight Plate at x: 23 to 29, y: 16 to 50
    c.fill_rect(22, 15, 30, 51, PAL_SILVER['outline'])
    c.fill_rect(23, 16, 29, 50, (60, 75, 90, 255))
    c.line(23, 16, 29, 16, C_STEEL_HI)
    c.line(23, 50, 29, 50, C_STEEL_SHADOW)

    # Sleeve Collar (x: 29 to 33, y: 26 to 40)
    c.fill_rect(29, 25, 33, 41, PAL_SILVER['outline'])
    c.fill_rect(30, 26, 32, 40, C_STEEL_HI)

    # Rotating Barbell Sleeve Cylinder (x: 33 to 50, y: 29 to 37)
    c.fill_rect(33, 28, 51, 38, PAL_SILVER['outline'])
    c.fill_rect(33, 29, 50, 37, C_STEEL_MID)
    c.line(33, 29, 50, 29, C_WHITE)
    c.line(33, 37, 50, 37, C_STEEL_SHADOW)

    # Phone / Sensor Device Clipped to the Sleeve (x: 36 to 46, y: 20 to 36)
    # Heavy-Duty Clamp / Clip around the sleeve
    c.fill_rect(39, 34, 43, 40, (30, 35, 45, 255))
    c.line(39, 34, 43, 34, C_STEEL_HI)

    # Smartphone Body (x: 36 to 46, y: 18 to 34)
    c.fill_rect(35, 17, 47, 35, (15, 18, 25, 255))
    c.fill_rect(36, 18, 46, 34, (25, 30, 42, 255))
    c.rect(35, 17, 47, 35, C_STEEL_DARK)

    # Glowing Cyan Screen (x: 37 to 45, y: 19 to 33)
    c.fill_rect(37, 19, 45, 33, (10, 45, 75, 255))
    # Active IMU Sensor Pose Grid (Crosshair + Orientation axes)
    c.line(41, 20, 41, 32, C_CYAN_HI)  # Y-axis
    c.line(38, 26, 44, 26, C_CYAN_HI)  # X-axis
    # Target center reticle & pose node
    c.fill_circle(41, 26, 1, C_WHITE)

    # Radiating cyan sensor pose wave / aura
    c.pset(34, 16, C_CYAN_HI)
    c.pset(48, 16, C_CYAN_HI)
    c.pset(41, 14, C_WHITE)

    return c


def draw_rom_honest():
    """rom_honest (Full Stroke): Hero at the bottom of a squat/press, full depth, no cheat."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Parallel & Below-Parallel Depth Check Line at y: 39 across badge
    c.line(16, 39, 48, 39, (60, 25, 10, 255))

    # 16-Bit Pixel Hero in Full Depth Olympic Squat!
    # Ground platform (y: 47 to 49, x: 18 to 46)
    c.fill_rect(18, 47, 46, 49, (45, 20, 8, 255))
    c.line(18, 47, 46, 47, (90, 45, 15, 255))

    # Hero Feet & Weightlifting Shoes planted flat at y: 45-46
    # Left foot (x: 21 to 25)
    c.fill_rect(20, 45, 25, 47, PAL_GOLD['outline'])
    c.fill_rect(21, 45, 24, 46, C_WHITE)
    # Right foot (x: 39 to 43)
    c.fill_rect(38, 45, 43, 47, PAL_GOLD['outline'])
    c.fill_rect(39, 45, 42, 46, C_WHITE)

    # Shin angles: Ankle to Knees (Knees forward over toes at y: 36)
    # Left Shin: (23, 45) -> (24, 36)
    c.line(23, 45, 24, 36, C_GOLD_DARK)
    # Right Shin: (41, 45) -> (40, 36)
    c.line(41, 45, 40, 36, C_GOLD_DARK)

    # Deep Thighs (Knee y: 36 down to Hip y: 41 - hips BELOW knees! Full ROM!)
    # Left Thigh: Knee (24, 36) -> Hip (30, 41)
    c.line(24, 36, 30, 41, C_GOLD_HI)
    c.line(24, 37, 30, 42, C_GOLD_MID)
    # Right Thigh: Knee (40, 36) -> Hip (34, 41)
    c.line(40, 36, 34, 41, C_GOLD_HI)
    c.line(40, 37, 34, 42, C_GOLD_MID)

    # Hip / Pelvis bottomed out at y: 41
    c.fill_rect(29, 39, 35, 42, C_GOLD_MID)

    # Braced Upright Torso (x: 29 to 35, y: 28 to 39)
    c.fill_rect(28, 28, 36, 38, PAL_GOLD['outline'])
    c.fill_rect(29, 29, 35, 37, C_GOLD_MID)
    c.line(29, 29, 30, 37, C_GOLD_HI)
    c.line(35, 29, 35, 37, C_GOLD_DARK)

    # Hero Head & Determined Eyes (y: 22 to 28, x: 30 to 34)
    c.fill_circle(32, 25, 3, C_GOLD_HI)
    c.pset(31, 25, (30, 14, 2, 255))
    c.pset(33, 25, (30, 14, 2, 255))

    # Loaded Olympic Barbell balanced securely across rear delts / shoulders at y: 27
    c.line(14, 27, 50, 27, PAL_GOLD['outline'])
    c.line(14, 26, 50, 26, C_WHITE)
    c.line(14, 28, 50, 28, C_GOLD_DARK)

    # Hands gripping bar at (27, 27) and (37, 27)
    c.fill_circle(27, 27, 1, C_GOLD_HI)
    c.fill_circle(37, 27, 1, C_GOLD_HI)

    # Big Gold Olympic Plates on Barbell Ends (x: 14 to 19, y: 19 to 35)
    c.fill_rect(14, 19, 19, 35, PAL_GOLD['outline'])
    c.fill_rect(15, 20, 18, 34, C_GOLD_MID)
    c.line(15, 20, 18, 20, C_GOLD_HI)
    c.line(15, 34, 18, 34, C_GOLD_SHADOW)

    # Right Plates (x: 45 to 50, y: 19 to 35)
    c.fill_rect(45, 19, 50, 35, PAL_GOLD['outline'])
    c.fill_rect(46, 20, 49, 34, C_GOLD_MID)
    c.line(46, 20, 49, 20, C_GOLD_HI)
    c.line(46, 34, 49, 34, C_GOLD_SHADOW)

    # Full ROM Depth Success Sparkles (Green check & golden glints)
    c.pset(32, 44, C_GREEN_HI)
    c.pset(31, 45, C_GREEN_HI)
    c.pset(33, 43, C_GREEN_HI)
    c.pset(18, 16, C_FIRE_YELLOW)
    c.pset(46, 16, C_FIRE_YELLOW)

    return c
