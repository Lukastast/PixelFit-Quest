"""
draw_streaks.py
Draws the 4 streak achievements:
1. streak_3 (On Fire): Bronze campfire, 3 small flames
2. streak_7 (Week Warrior): Silver week-sun with 7 rays, warrior helm
3. streak_14 (Unstoppable): Gold comet that does not go out
4. streak_30 (Habit Hero): Platinum calendar-moon, unbroken chain of 30 links
"""
import math
from pixel_canvas import (
    PixelCanvas, PAL_BRONZE, PAL_SILVER, PAL_GOLD, PAL_PLATINUM,
    C_WOOD_HI, C_WOOD_MID, C_WOOD_DARK, C_WOOD_SHADOW,
    C_STEEL_HI, C_STEEL_MID, C_STEEL_DARK, C_STEEL_SHADOW,
    C_GOLD_HI, C_GOLD_MID, C_GOLD_DARK,
    C_FIRE_YELLOW, C_FIRE_ORANGE, C_FIRE_RED,
    C_CYAN_HI, C_CYAN_MID, C_CYAN_DARK,
    C_WHITE, C_BLACK
)


def draw_streak_3():
    """streak_3: Bronze campfire, 3 small flames."""
    c = PixelCanvas(64)
    c.draw_badge_frame('bronze')

    # Stone hearth ring at bottom (x: 18 to 46, y: 44 to 49)
    stones = [(20, 46, 3), (25, 47, 3), (32, 48, 3), (39, 47, 3), (44, 46, 3)]
    for sx, sy, r in stones:
        c.fill_circle(sx, sy, r, PAL_BRONZE['outline'])
        c.fill_circle(sx, sy, r - 1, (90, 80, 75, 255))
        c.pset(sx - 1, sy - 1, (140, 130, 125, 255))

    # Charcoal embers inside hearth
    c.fill_rect(22, 44, 42, 46, (40, 18, 10, 255))
    c.pset(26, 45, C_FIRE_RED)
    c.pset(32, 45, C_FIRE_ORANGE)
    c.pset(37, 45, C_FIRE_RED)

    # Crossed Campfire Logs
    # Log 1: from (20, 45) to (43, 40)
    c.line(20, 45, 43, 40, C_WOOD_SHADOW)
    c.line(20, 44, 43, 39, C_WOOD_DARK)
    c.line(21, 43, 43, 38, C_WOOD_MID)
    # Log 2: from (44, 45) to (21, 40)
    c.line(44, 45, 21, 40, C_WOOD_SHADOW)
    c.line(44, 44, 21, 39, C_WOOD_DARK)
    c.line(43, 43, 21, 38, C_WOOD_HI)

    # 3 Small Distinct Flames!
    # Flame 1: Left Flame (x: 23 to 29, y: 28 to 41)
    c.fill_circle(26, 38, 4, C_FIRE_RED)
    c.fill_circle(26, 36, 3, C_FIRE_ORANGE)
    c.fill_circle(25, 34, 2, C_FIRE_YELLOW)
    c.pset(24, 30, C_FIRE_YELLOW)
    c.pset(24, 29, C_FIRE_ORANGE)
    c.pset(25, 33, C_WHITE)

    # Flame 2: Center Tall Flame (x: 29 to 35, y: 19 to 41)
    c.fill_circle(32, 38, 5, C_FIRE_RED)
    c.fill_circle(32, 34, 4, C_FIRE_ORANGE)
    c.fill_circle(32, 29, 3, C_FIRE_YELLOW)
    c.line(32, 21, 32, 26, C_FIRE_YELLOW)
    c.pset(32, 20, C_FIRE_ORANGE)
    c.pset(32, 19, C_FIRE_YELLOW)
    c.pset(32, 28, C_WHITE)
    c.pset(32, 29, C_WHITE)

    # Flame 3: Right Flame (x: 35 to 41, y: 27 to 41)
    c.fill_circle(38, 38, 4, C_FIRE_RED)
    c.fill_circle(38, 35, 3, C_FIRE_ORANGE)
    c.fill_circle(38, 32, 2, C_FIRE_YELLOW)
    c.pset(39, 29, C_FIRE_YELLOW)
    c.pset(39, 28, C_FIRE_ORANGE)
    c.pset(38, 32, C_WHITE)

    # Rising spark embers
    c.pset(28, 22, C_FIRE_YELLOW)
    c.pset(36, 21, C_FIRE_YELLOW)
    c.pset(31, 16, C_FIRE_ORANGE)

    return c


def draw_streak_7():
    """streak_7: Silver week-sun with 7 rays, warrior helm."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # Knight's Warrior Helm at lower center (x: 22 to 42, y: 28 to 48)
    # Helm Dome & Cheekplates
    c.fill_circle(32, 36, 10, PAL_SILVER['outline'])
    c.fill_circle(32, 36, 9, C_STEEL_SHADOW)
    c.fill_circle(31, 35, 8, C_STEEL_MID)
    c.fill_circle(30, 34, 6, C_STEEL_HI)

    # Neck Guard
    c.fill_rect(24, 42, 40, 48, PAL_SILVER['outline'])
    c.fill_rect(25, 43, 39, 47, C_STEEL_MID)
    c.line(26, 44, 38, 44, C_STEEL_HI)

    # Visor Eye Slit (horizontal slit with dark shadow and rivet highlights)
    c.fill_rect(25, 35, 39, 37, PAL_SILVER['outline'])
    c.fill_rect(26, 36, 38, 36, (10, 14, 20, 255))
    # Brow ridge highlight above visor
    c.line(24, 34, 40, 34, C_STEEL_HI)
    # Nose ridge
    c.line(32, 34, 32, 42, C_STEEL_HI)

    # Week-Sun Disc cresting atop the helm (Center: 32, 22, Radius: 5)
    c.fill_circle(32, 22, 6, PAL_SILVER['outline'])
    c.fill_circle(32, 22, 5, C_GOLD_MID)
    c.fill_circle(31, 21, 4, C_GOLD_HI)
    c.pset(31, 21, C_WHITE)

    # Exactly 7 Distinct Sun Rays radiating outward in an upward arc!
    # Angles: -150, -125, -100, -75, -50, -25, 0 (or symmetric across top: 180 to 0)
    ray_angles = [-140, -115, -90, -65, -40, -15, 10]
    # Center is (32, 22)
    # Distinct rays:
    ray_endpoints = [
        (18, 26),  # Ray 1: Far left
        (20, 18),  # Ray 2: Upper-left
        (25, 14),  # Ray 3: High-left
        (32, 12),  # Ray 4: Top center
        (39, 14),  # Ray 5: High-right
        (44, 18),  # Ray 6: Upper-right
        (46, 26),  # Ray 7: Far right
    ]
    for rx, ry in ray_endpoints:
        # Draw bold tapered ray from near disc (r=6) to endpoint
        c.line(32, 22, rx, ry, PAL_SILVER['outline'])
        # Inset highlight ray
        nx = 32 + int((rx - 32) * 0.4)
        ny = 22 + int((ry - 22) * 0.4)
        c.line(nx, ny, rx, ry, C_STEEL_HI)
        c.pset(rx, ry, C_WHITE)

    return c


def draw_streak_14():
    """streak_14: Gold comet that does not go out."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Comet Core at Upper Right (Center: 43, 21)
    # Blazing Tail sweeping down-left toward (14, 48)
    # Fill layered flame tail
    # Tail outer bounds: triangle from (43, 21) spreading to (14, 42) and (20, 48)
    tail_pts = [
        # (x, y, color)
        # Deep fire red outer plume
        ((14, 46), (16, 44), (20, 40), (25, 35), (32, 29), (38, 24), C_FIRE_RED),
        ((16, 48), (20, 46), (25, 41), (30, 36), (36, 30), (41, 25), C_FIRE_RED),
    ]
    for t_line in tail_pts:
        pts = t_line[:-1]
        col = t_line[-1]
        for i in range(len(pts) - 1):
            c.line(pts[i][0], pts[i][1], pts[i + 1][0], pts[i + 1][1], col)

    # Dense fire body
    for y in range(21, 48):
        # Progress t from 0 (at 21) to 1 (at 47)
        t = (y - 21) / 26.0
        center_x = 43 - int(t * 26)
        width = int(2 + t * 5)
        for x in range(center_x - width, center_x + width + 1):
            dist = abs(x - center_x)
            if dist <= width // 2:
                c.pset(x, y, C_FIRE_YELLOW if t < 0.6 else C_FIRE_ORANGE)
            else:
                c.pset(x, y, C_FIRE_ORANGE if t < 0.7 else C_FIRE_RED)

    # Inner intense white/yellow flame core streak
    c.line(43, 21, 22, 42, C_WHITE)
    c.line(42, 21, 21, 42, C_FIRE_YELLOW)

    # Comet Head / Nucleus (Center: 43, 21, Radius: 6)
    c.fill_circle(43, 21, 7, PAL_GOLD['outline'])
    c.fill_circle(43, 21, 6, C_FIRE_ORANGE)
    c.fill_circle(42, 20, 5, C_FIRE_YELLOW)
    c.fill_circle(41, 19, 3, C_GOLD_HI)
    c.fill_circle(40, 18, 2, C_WHITE)

    # Intense flare star on comet head
    c.line(36, 20, 48, 20, C_WHITE)
    c.line(42, 14, 42, 26, C_WHITE)

    # Persistent trail embers & sparks floating behind
    spark_locs = [(15, 38), (12, 45), (18, 49), (26, 46), (32, 42), (37, 34)]
    for sx, sy in spark_locs:
        c.pset(sx, sy, C_FIRE_YELLOW)
        c.pset(sx + 1, sy, C_FIRE_ORANGE)

    return c


def draw_streak_30():
    """streak_30: Platinum calendar-moon, unbroken chain of 30 links."""
    c = PixelCanvas(64)
    c.draw_badge_frame('platinum')

    # Platinum Crescent Moon in Center
    # Outer circle centered at (32, 32), radius 9
    # Inner cutout centered at (35, 30), radius 8
    for y in range(21, 44):
        for x in range(21, 44):
            d_out = (x - 32) ** 2 + (y - 32) ** 2
            d_in = (x - 36) ** 2 + (y - 30) ** 2
            if d_out <= 81 and d_in > 58:
                c.pset(x, y, PAL_PLATINUM['outline'])

    for y in range(22, 43):
        for x in range(22, 43):
            d_out = (x - 32) ** 2 + (y - 32) ** 2
            d_in = (x - 36) ** 2 + (y - 30) ** 2
            if d_out <= 64 and d_in > 60:
                c.pset(x, y, C_CYAN_MID)
            elif d_out <= 64 and d_in > 52:
                c.pset(x, y, C_WHITE if (x < 31) else C_CYAN_HI)

    # Moon surface craters / glow
    c.pset(27, 30, C_WHITE)
    c.pset(26, 34, C_CYAN_HI)

    # Unbroken Chain of Exactly 30 Links encircling the moon!
    # Radius R = 15 around center (32, 32)
    # 30 links: angle step = 360 / 30 = 12 degrees each
    r_chain = 15.5
    for i in range(30):
        angle = math.radians(i * 12)
        lx = 32 + int(round(r_chain * math.cos(angle)))
        ly = 32 + int(round(r_chain * math.sin(angle)))
        # Alternating chain links (link vs connector)
        if i % 2 == 0:
            c.pset(lx, ly, C_WHITE)
            # Outline
            if i % 4 == 0:
                c.pset(lx - 1, ly, C_CYAN_DARK)
        else:
            c.pset(lx, ly, C_CYAN_HI)

    # 4 Crystal Star Accents around the chain
    for qx, qy in [(16, 16), (48, 16), (16, 48), (48, 48)]:
        c.pset(qx, qy, C_WHITE)
        c.pset(qx - 1, qy, C_CYAN_HI)
        c.pset(qx + 1, qy, C_CYAN_HI)
        c.pset(qx, qy - 1, C_CYAN_HI)
        c.pset(qx, qy + 1, C_CYAN_HI)

    return c
