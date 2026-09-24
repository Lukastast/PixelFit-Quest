"""
draw_heart.py
Draws the 16-bit SNES Resting Heart Rate achievement badge:
- Silver relic badge frame with metallic bevel and rivets
- Shaded 16-bit Ruby Heart with faceted crystal highlights
- Calm, steady EKG pulse wave across the lower half
- Soft radiant vitality glow
"""
from pixel_canvas import (
    PixelCanvas, PAL_SILVER,
    C_WHITE, C_BLACK
)

# Curated 16-bit Ruby Cardio Palette
C_RUBY_WHITE = (255, 255, 255, 255)
C_RUBY_GLINT = (255, 205, 215, 255)
C_RUBY_HI = (255, 95, 110, 255)
C_RUBY_MID = (225, 40, 55, 255)
C_RUBY_DEEP = (165, 20, 35, 255)
C_RUBY_DARK = (105, 12, 22, 255)
C_RUBY_SHADOW = (55, 8, 14, 255)
C_RUBY_OUTLINE = (28, 6, 10, 255)

# Luminous Pulse Wave Palette (Cyan/Mint Crystal)
C_PULSE_HI = (235, 255, 255, 255)
C_PULSE_MID = (100, 230, 245, 255)
C_PULSE_DARK = (30, 130, 160, 255)
C_PULSE_GLOW = (20, 65, 85, 255)


def draw_resting_heart_rate():
    """resting_heart_rate: Silver relic badge with faceted ruby heart & steady resting pulse."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # 1. Soft Warm Vitality Glow behind the heart (center: 32, 28)
    for dy in range(-12, 13):
        for dx in range(-14, 15):
            dist_sq = dx * dx + dy * dy
            if dist_sq <= 196:
                gx = 32 + dx
                gy = 28 + dy
                if 12 <= gx <= 52 and 12 <= gy <= 52:
                    if dist_sq <= 64:
                        c.pset(gx, gy, (65, 22, 35, 255))
                    elif dist_sq <= 144:
                        c.pset(gx, gy, (48, 20, 36, 255))
                    else:
                        c.pset(gx, gy, (38, 24, 42, 255))

    # 2. Calm Steady EKG Pulse Wave across bottom (y: 44 to 48)
    # Trace continuous cardiogram segments:
    # Segment 1: Left baseline (14, 46) -> (22, 46)
    c.line(14, 46, 22, 46, C_PULSE_DARK)
    c.line(15, 46, 22, 46, C_PULSE_MID)

    # Segment 2: P-wave bump (22, 46) -> (24, 44) -> (26, 44) -> (28, 46)
    c.line(22, 46, 24, 44, C_PULSE_MID)
    c.line(24, 44, 26, 44, C_PULSE_MID)
    c.line(26, 44, 28, 46, C_PULSE_MID)
    c.pset(25, 43, C_PULSE_HI)

    # Segment 3: PR interval (28, 46) -> (29, 46)
    c.line(28, 46, 29, 46, C_PULSE_MID)

    # Segment 4: Q-dip (29, 46) -> (30, 48)
    c.line(29, 46, 30, 48, C_PULSE_MID)

    # Segment 5: R-spike up (30, 48) -> (32, 40)
    c.line(30, 48, 32, 40, C_PULSE_MID)
    c.line(31, 46, 32, 40, C_PULSE_HI)

    # Segment 6: S-dip down (32, 40) -> (34, 49)
    c.line(32, 40, 34, 49, C_PULSE_MID)
    c.line(32, 40, 33, 44, C_PULSE_HI)

    # Segment 7: Recovery to baseline (34, 49) -> (36, 46)
    c.line(34, 49, 36, 46, C_PULSE_MID)

    # Segment 8: ST segment (36, 46) -> (37, 46)
    c.line(36, 46, 37, 46, C_PULSE_MID)

    # Segment 9: T-wave (37, 46) -> (39, 44) -> (41, 44) -> (43, 46)
    c.line(37, 46, 39, 44, C_PULSE_MID)
    c.line(39, 44, 41, 44, C_PULSE_MID)
    c.line(41, 44, 43, 46, C_PULSE_MID)
    c.pset(40, 43, C_PULSE_HI)

    # Segment 10: Right baseline (43, 46) -> (50, 46)
    c.line(43, 46, 50, 46, C_PULSE_MID)
    c.line(49, 46, 50, 46, C_PULSE_DARK)

    # 3. 16-Bit Faceted Ruby Heart (x: 21 to 43, y: 17 to 37)
    # Pixel heart row by row definitions: (y, x_start, x_end)
    # Lobes on top: left lobe (23 to 30), right lobe (34 to 41)
    # y=17: top tips of lobes
    c.fill_rect(24, 17, 29, 17, C_RUBY_OUTLINE)
    c.fill_rect(35, 17, 40, 17, C_RUBY_OUTLINE)

    # y=18: wider lobes
    c.pset(23, 18, C_RUBY_OUTLINE)
    c.fill_rect(24, 18, 29, 18, C_RUBY_HI)
    c.pset(30, 18, C_RUBY_OUTLINE)
    c.pset(34, 18, C_RUBY_OUTLINE)
    c.fill_rect(35, 18, 40, 18, C_RUBY_MID)
    c.pset(41, 18, C_RUBY_OUTLINE)

    # y=19: lobe expansion
    c.pset(22, 19, C_RUBY_OUTLINE)
    c.fill_rect(23, 19, 24, 19, C_RUBY_HI)
    c.fill_rect(25, 19, 28, 19, C_RUBY_GLINT)
    c.fill_rect(29, 19, 30, 19, C_RUBY_HI)
    c.pset(31, 19, C_RUBY_OUTLINE)
    c.pset(33, 19, C_RUBY_OUTLINE)
    c.fill_rect(34, 19, 39, 19, C_RUBY_MID)
    c.pset(40, 19, C_RUBY_DEEP)
    c.pset(41, 19, C_RUBY_OUTLINE)
    c.pset(42, 19, C_RUBY_OUTLINE)

    # y=20: widest lobe tops
    c.pset(21, 20, C_RUBY_OUTLINE)
    c.fill_rect(22, 20, 23, 20, C_RUBY_HI)
    c.fill_rect(24, 20, 27, 20, C_RUBY_WHITE)
    c.fill_rect(28, 20, 30, 20, C_RUBY_HI)
    c.pset(31, 20, C_RUBY_MID)
    c.pset(32, 20, C_RUBY_OUTLINE)
    c.pset(33, 20, C_RUBY_MID)
    c.fill_rect(34, 20, 38, 20, C_RUBY_MID)
    c.fill_rect(39, 20, 41, 20, C_RUBY_DEEP)
    c.pset(42, 20, C_RUBY_DARK)
    c.pset(43, 20, C_RUBY_OUTLINE)

    # y=21: cleft starts
    c.pset(21, 21, C_RUBY_OUTLINE)
    c.fill_rect(22, 21, 23, 21, C_RUBY_HI)
    c.fill_rect(24, 21, 26, 21, C_RUBY_WHITE)
    c.fill_rect(27, 21, 30, 21, C_RUBY_HI)
    c.fill_rect(31, 21, 32, 21, C_RUBY_DEEP)
    c.fill_rect(33, 21, 38, 21, C_RUBY_MID)
    c.fill_rect(39, 21, 41, 21, C_RUBY_DEEP)
    c.pset(42, 21, C_RUBY_DARK)
    c.pset(43, 21, C_RUBY_OUTLINE)

    # y=22: cleft descends
    c.pset(21, 22, C_RUBY_OUTLINE)
    c.fill_rect(22, 22, 23, 22, C_RUBY_HI)
    c.fill_rect(24, 22, 25, 22, C_RUBY_GLINT)
    c.fill_rect(26, 22, 30, 22, C_RUBY_MID)
    c.fill_rect(31, 22, 32, 22, C_RUBY_DARK)
    c.fill_rect(33, 22, 37, 22, C_RUBY_MID)
    c.fill_rect(38, 22, 41, 22, C_RUBY_DEEP)
    c.pset(42, 22, C_RUBY_DARK)
    c.pset(43, 22, C_RUBY_OUTLINE)

    # y=23: body merger
    c.pset(21, 23, C_RUBY_OUTLINE)
    c.fill_rect(22, 23, 23, 23, C_RUBY_HI)
    c.fill_rect(24, 23, 29, 23, C_RUBY_MID)
    c.fill_rect(30, 23, 33, 23, C_RUBY_DEEP)
    c.fill_rect(34, 23, 38, 23, C_RUBY_MID)
    c.fill_rect(39, 23, 41, 23, C_RUBY_DEEP)
    c.pset(42, 23, C_RUBY_DARK)
    c.pset(43, 23, C_RUBY_OUTLINE)

    # y=24: full width heart body
    c.pset(21, 24, C_RUBY_OUTLINE)
    c.fill_rect(22, 24, 24, 24, C_RUBY_HI)
    c.fill_rect(25, 24, 30, 24, C_RUBY_MID)
    c.fill_rect(31, 24, 35, 24, C_RUBY_DEEP)
    c.fill_rect(36, 24, 39, 24, C_RUBY_DEEP)
    c.fill_rect(40, 24, 42, 24, C_RUBY_DARK)
    c.pset(43, 24, C_RUBY_OUTLINE)

    # y=25: body begins tapering
    c.pset(22, 25, C_RUBY_OUTLINE)
    c.fill_rect(23, 25, 25, 25, C_RUBY_HI)
    c.fill_rect(26, 25, 31, 25, C_RUBY_MID)
    c.fill_rect(32, 25, 37, 25, C_RUBY_DEEP)
    c.fill_rect(38, 25, 41, 25, C_RUBY_DARK)
    c.pset(42, 25, C_RUBY_OUTLINE)

    # y=26
    c.pset(22, 26, C_RUBY_OUTLINE)
    c.fill_rect(23, 26, 25, 26, C_RUBY_HI)
    c.fill_rect(26, 26, 31, 26, C_RUBY_MID)
    c.fill_rect(32, 26, 36, 26, C_RUBY_DEEP)
    c.fill_rect(37, 26, 40, 26, C_RUBY_DARK)
    c.pset(41, 26, C_RUBY_OUTLINE)

    # y=27
    c.pset(23, 27, C_RUBY_OUTLINE)
    c.fill_rect(24, 27, 26, 27, C_RUBY_HI)
    c.fill_rect(27, 27, 31, 27, C_RUBY_MID)
    c.fill_rect(32, 27, 36, 27, C_RUBY_DEEP)
    c.fill_rect(37, 27, 39, 27, C_RUBY_DARK)
    c.pset(40, 27, C_RUBY_OUTLINE)

    # y=28: facet reflection across lower curve
    c.pset(23, 28, C_RUBY_OUTLINE)
    c.fill_rect(24, 28, 26, 28, C_RUBY_HI)
    c.fill_rect(27, 28, 30, 28, C_RUBY_MID)
    c.fill_rect(31, 28, 35, 28, C_RUBY_DEEP)
    c.fill_rect(36, 28, 39, 28, C_RUBY_DARK)
    c.pset(40, 28, C_RUBY_OUTLINE)

    # y=29
    c.pset(24, 29, C_RUBY_OUTLINE)
    c.fill_rect(25, 29, 27, 29, C_RUBY_HI)
    c.fill_rect(28, 29, 31, 29, C_RUBY_MID)
    c.fill_rect(32, 29, 35, 29, C_RUBY_DEEP)
    c.fill_rect(36, 29, 38, 29, C_RUBY_DARK)
    c.pset(39, 29, C_RUBY_OUTLINE)

    # y=30
    c.pset(25, 30, C_RUBY_OUTLINE)
    c.fill_rect(26, 30, 28, 30, C_RUBY_HI)
    c.fill_rect(29, 30, 32, 30, C_RUBY_MID)
    c.fill_rect(33, 30, 35, 30, C_RUBY_DEEP)
    c.fill_rect(36, 30, 37, 30, C_RUBY_DARK)
    c.pset(38, 30, C_RUBY_OUTLINE)

    # y=31
    c.pset(26, 31, C_RUBY_OUTLINE)
    c.fill_rect(27, 31, 29, 31, C_RUBY_HI)
    c.fill_rect(30, 31, 33, 31, C_RUBY_MID)
    c.fill_rect(34, 31, 35, 31, C_RUBY_DEEP)
    c.pset(36, 31, C_RUBY_DARK)
    c.pset(37, 31, C_RUBY_OUTLINE)

    # y=32
    c.pset(27, 32, C_RUBY_OUTLINE)
    c.fill_rect(28, 32, 30, 32, C_RUBY_HI)
    c.fill_rect(31, 32, 33, 32, C_RUBY_MID)
    c.fill_rect(34, 32, 35, 32, C_RUBY_DEEP)
    c.pset(36, 32, C_RUBY_OUTLINE)

    # y=33
    c.pset(28, 33, C_RUBY_OUTLINE)
    c.fill_rect(29, 33, 31, 33, C_RUBY_HI)
    c.fill_rect(32, 33, 33, 33, C_RUBY_MID)
    c.pset(34, 33, C_RUBY_DEEP)
    c.pset(35, 33, C_RUBY_OUTLINE)

    # y=34
    c.pset(29, 34, C_RUBY_OUTLINE)
    c.fill_rect(30, 34, 31, 34, C_RUBY_HI)
    c.fill_rect(32, 34, 33, 34, C_RUBY_MID)
    c.pset(34, 34, C_RUBY_OUTLINE)

    # y=35
    c.pset(30, 35, C_RUBY_OUTLINE)
    c.pset(31, 35, C_RUBY_HI)
    c.pset(32, 35, C_RUBY_MID)
    c.pset(33, 35, C_RUBY_OUTLINE)

    # y=36: tip of heart
    c.pset(31, 36, C_RUBY_OUTLINE)
    c.pset(32, 36, C_RUBY_HI)
    c.pset(33, 36, C_RUBY_OUTLINE)

    # y=37: bottom apex point
    c.pset(32, 37, C_RUBY_OUTLINE)

    # 4. Tiny Sparkling Glints around the heart
    # Upper left glint
    c.pset(17, 18, (255, 230, 240, 255))
    c.pset(17, 17, (255, 160, 180, 255))
    c.pset(17, 19, (255, 160, 180, 255))
    c.pset(16, 18, (255, 160, 180, 255))
    c.pset(18, 18, (255, 160, 180, 255))

    # Upper right gentle star glint
    c.pset(47, 18, (255, 230, 240, 255))
    c.pset(47, 17, (255, 160, 180, 255))
    c.pset(47, 19, (255, 160, 180, 255))
    c.pset(46, 18, (255, 160, 180, 255))
    c.pset(48, 18, (255, 160, 180, 255))

    return c
