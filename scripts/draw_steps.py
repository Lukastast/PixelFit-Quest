"""
draw_steps.py
Draws the 4 step achievements:
1. steps_5000 (First Miles): Bronze muddy boots, first path
2. steps_10000 (Walker): Bronze walking staff, dirt road
3. steps_50000 (Roadster): Silver horse on a stone road
4. steps_100000 (Marathoner): Gold winged sandals over a long road
"""
from pixel_canvas import (
    PixelCanvas, PAL_BRONZE, PAL_SILVER, PAL_GOLD, PAL_PLATINUM,
    C_WOOD_HI, C_WOOD_MID, C_WOOD_DARK, C_WOOD_SHADOW,
    C_LEATHER_HI, C_LEATHER_MID, C_LEATHER_DARK,
    C_EARTH_HI, C_EARTH_MID, C_EARTH_DARK,
    C_STEEL_HI, C_STEEL_MID, C_STEEL_DARK, C_STEEL_SHADOW,
    C_GOLD_HI, C_GOLD_MID, C_GOLD_DARK, C_GOLD_SHADOW,
    C_FIRE_YELLOW, C_FIRE_ORANGE,
    C_WHITE, C_BLACK
)


def draw_steps_5000():
    """steps_5000: Bronze muddy boots, first path."""
    c = PixelCanvas(64)
    c.draw_badge_frame('bronze')

    # Winding earthen path at bottom (x: 14 to 50, y: 41 to 49)
    for y in range(41, 50):
        for x in range(14, 50):
            # Curved dirt trail
            c.pset(x, y, C_EARTH_DARK)
    for x in range(16, 48):
        c.pset(x, 43, C_EARTH_MID)
        c.pset(x, 44, C_EARTH_HI)

    # Mud splatters / pebbles on trail
    pebbles = [(18, 47), (24, 45), (42, 46), (46, 43)]
    for px, py in pebbles:
        c.pset(px, py, (180, 150, 100, 255))
        c.pset(px, py + 1, C_EARTH_DARK)

    # Pair of Sturdy Leather Boots
    # Left Boot (in front: x: 20 to 32, y: 22 to 44)
    # Boot Shaft (x: 23 to 30, y: 22 to 34)
    c.fill_rect(23, 22, 30, 34, PAL_BRONZE['outline'])
    c.fill_rect(24, 23, 29, 33, C_LEATHER_MID)
    c.line(24, 23, 25, 33, C_LEATHER_HI)
    c.line(29, 23, 29, 33, C_LEATHER_DARK)
    # Folded cuff at top
    c.fill_rect(22, 21, 31, 23, C_LEATHER_HI)
    c.rect(21, 20, 32, 24, PAL_BRONZE['outline'])
    # Laces
    for ly in [25, 28, 31]:
        c.pset(26, ly, (210, 170, 120, 255))
        c.pset(27, ly, (210, 170, 120, 255))

    # Left Boot Foot & Toe (x: 20 to 33, y: 34 to 43)
    c.fill_rect(20, 34, 33, 41, C_LEATHER_MID)
    c.fill_rect(20, 34, 24, 41, C_LEATHER_HI)
    c.fill_rect(20, 38, 33, 42, C_LEATHER_DARK)
    # Sole
    c.fill_rect(19, 42, 33, 44, PAL_BRONZE['outline'])
    # Mud coating on left boot sole and heel
    for mx, my in [(19, 41), (20, 42), (21, 43), (22, 41), (28, 42), (31, 41), (32, 42)]:
        c.pset(mx, my, C_EARTH_DARK)

    # Right Boot (slightly behind: x: 33 to 44, y: 20 to 42)
    c.fill_rect(35, 20, 42, 32, PAL_BRONZE['outline'])
    c.fill_rect(36, 21, 41, 31, C_LEATHER_MID)
    c.line(36, 21, 37, 31, C_LEATHER_HI)
    c.fill_rect(34, 19, 43, 21, C_LEATHER_HI)
    # Right Boot Foot & Toe (pointing right)
    c.fill_rect(34, 32, 45, 39, C_LEATHER_MID)
    c.fill_rect(37, 32, 45, 39, C_LEATHER_DARK)
    c.fill_rect(34, 39, 46, 42, PAL_BRONZE['outline'])
    c.fill_rect(35, 40, 45, 41, (40, 25, 15, 255))
    # Mud on right boot
    for mx, my in [(35, 39), (36, 40), (43, 39), (44, 40), (45, 39)]:
        c.pset(mx, my, C_EARTH_DARK)

    return c


def draw_steps_10000():
    """steps_10000: Bronze walking staff, dirt road."""
    c = PixelCanvas(64)
    c.draw_badge_frame('bronze')

    # Rolling Hills & Horizon in distance (y: 28 to 36)
    c.fill_rect(14, 32, 49, 49, C_EARTH_DARK)
    # Distant hills
    c.line(14, 32, 28, 28, (70, 50, 30, 255))
    c.line(28, 28, 50, 34, (70, 50, 30, 255))

    # Winding Dirt Road receding into the horizon (narrow at top 28, 30, wide at 16 to 48 at bottom)
    for y in range(30, 50):
        t = (y - 30) / 19.0
        left_x = int(28 - t * 14)
        right_x = int(32 + t * 15)
        for x in range(left_x, right_x):
            c.pset(x, y, C_EARTH_MID if y % 2 == 0 else C_EARTH_HI)
            if x == left_x or x == right_x - 1:
                c.pset(x, y, C_EARTH_DARK)

    # Bronze Walking Staff standing prominent on the trail
    # Staff runs vertically at x: 26 to 28, from y: 15 down to 47
    # Wooden Staff Shaft
    c.line(26, 17, 26, 46, C_WOOD_HI)
    c.line(27, 17, 27, 46, C_WOOD_MID)
    c.line(28, 17, 28, 46, C_WOOD_DARK)
    c.line(29, 17, 29, 46, PAL_BRONZE['outline'])

    # Wrapped Leather Grip (y: 27 to 34)
    for gy in range(27, 35):
        c.pset(26, gy, C_LEATHER_HI)
        c.pset(27, gy, C_LEATHER_MID if gy % 2 == 0 else C_LEATHER_DARK)
        c.pset(28, gy, C_LEATHER_DARK)

    # Bronze Carved Staff Head / Pommel at Top (x: 23 to 31, y: 14 to 20)
    c.fill_circle(27, 17, 4, PAL_BRONZE['outline'])
    c.fill_circle(27, 17, 3, PAL_BRONZE['metal_dark'])
    c.fill_circle(26, 16, 2, PAL_BRONZE['metal_hi'])
    c.pset(25, 15, PAL_BRONZE['edge_hi'])
    # Embedded Amber Gem in Pommel
    c.pset(27, 17, C_FIRE_YELLOW)
    c.pset(28, 18, C_FIRE_ORANGE)

    # Bronze Ferrule Spike at bottom planted in the dirt (y: 45 to 48)
    c.fill_rect(26, 45, 28, 48, PAL_BRONZE['metal_mid'])
    c.line(26, 45, 26, 48, PAL_BRONZE['metal_hi'])
    c.pset(27, 48, PAL_BRONZE['outline'])

    return c


def draw_steps_50000():
    """steps_50000: Silver horse on a stone road."""
    c = PixelCanvas(64)
    c.draw_badge_frame('silver')

    # Stone Paved Road at Bottom (y: 42 to 49)
    c.fill_rect(14, 42, 50, 49, C_STEEL_SHADOW)
    # Cobblestone grid
    for cy in [43, 46, 49]:
        c.line(14, cy, 50, cy, PAL_SILVER['outline'])
    for cx in range(16, 50, 6):
        c.line(cx, 42, cx, 45, C_STEEL_DARK)
        c.line(cx + 3, 45, cx + 3, 49, C_STEEL_DARK)

    # Galloping Silver Warhorse! (charging rightwards, x: 18 to 48, y: 20 to 44)
    # Horse Body (torso: x: 24 to 40, y: 28 to 36)
    c.fill_rect(25, 29, 39, 36, C_STEEL_MID)
    c.fill_rect(26, 28, 38, 34, C_STEEL_HI)
    c.line(26, 27, 37, 27, C_WHITE)  # Back ridge highlight
    c.fill_rect(26, 35, 38, 37, C_STEEL_SHADOW)

    # Horse Powerful Rump & Hindquarters (x: 18 to 26, y: 26 to 36)
    c.fill_circle(23, 31, 5, C_STEEL_MID)
    c.fill_circle(23, 30, 4, C_STEEL_HI)
    c.line(19, 29, 22, 27, C_WHITE)

    # Hind Legs (in galloping stride)
    # Left hind leg trailing back: (20, 34) -> (16, 38) -> (14, 42)
    c.line(20, 34, 16, 38, C_STEEL_MID)
    c.line(16, 38, 14, 42, C_STEEL_SHADOW)
    c.pset(14, 42, PAL_SILVER['outline'])  # Hoof
    # Right hind leg bent forward: (24, 34) -> (22, 39) -> (25, 43)
    c.line(24, 34, 22, 39, C_STEEL_HI)
    c.line(22, 39, 25, 43, C_STEEL_MID)
    c.pset(25, 43, PAL_SILVER['outline'])

    # Horse Chest & Forequarters (x: 36 to 43, y: 26 to 34)
    c.fill_circle(38, 30, 5, C_STEEL_MID)
    c.fill_circle(38, 29, 4, C_STEEL_HI)
    c.pset(39, 28, C_WHITE)

    # Forelegs (reaching forward in gallop)
    # Left foreleg reaching forward: (39, 33) -> (43, 37) -> (47, 41)
    c.line(39, 33, 43, 37, C_STEEL_HI)
    c.line(43, 37, 47, 41, C_STEEL_MID)
    c.pset(47, 41, PAL_SILVER['outline'])  # Hoof
    # Right foreleg tucked: (37, 33) -> (39, 38) -> (38, 42)
    c.line(37, 33, 39, 38, C_STEEL_SHADOW)
    c.line(39, 38, 38, 42, C_STEEL_SHADOW)
    c.pset(38, 42, PAL_SILVER['outline'])

    # Horse Neck & Head (arching up and forward: neck x: 38 to 44, head x: 42 to 49, y: 18 to 28)
    c.line(38, 28, 43, 21, C_STEEL_HI)
    c.line(39, 29, 44, 22, C_STEEL_HI)
    c.line(40, 30, 45, 23, C_STEEL_MID)
    # Head & Muzzle
    c.fill_rect(43, 19, 47, 24, C_STEEL_HI)
    c.fill_rect(47, 22, 49, 25, C_STEEL_MID)  # Muzzle
    c.pset(48, 25, C_STEEL_SHADOW)  # Nostril
    # Ears
    c.pset(43, 17, C_STEEL_HI)
    c.pset(44, 18, C_STEEL_HI)
    # Eye
    c.pset(45, 20, PAL_SILVER['outline'])

    # Flowing Silver Mane & Tail
    # Mane blowing back across neck
    for mx, my in [(38, 23), (39, 21), (36, 25), (37, 27)]:
        c.pset(mx, my, C_WHITE)
    # Tail streaming back
    c.line(19, 29, 14, 30, C_WHITE)
    c.line(18, 30, 13, 32, C_STEEL_HI)
    c.line(17, 31, 14, 34, C_STEEL_MID)

    return c


def draw_steps_100000():
    """steps_100000: Gold winged sandals over a long road."""
    c = PixelCanvas(64)
    c.draw_badge_frame('gold')

    # Grand Highway in perspective receding to golden horizon
    # Horizon at y: 22
    c.fill_circle(32, 22, 4, C_FIRE_YELLOW)  # Distant rising sun
    c.pset(32, 22, C_WHITE)

    # Road perspective from (30, 24) spreading down to (14, 49) and (50, 49)
    for y in range(24, 50):
        t = (y - 24) / 25.0
        x_left = int(31 - t * 17)
        x_right = int(33 + t * 17)
        for x in range(x_left, x_right + 1):
            if x == x_left or x == x_right:
                c.pset(x, y, C_GOLD_DARK)
            elif (x + y) % 4 == 0:
                c.pset(x, y, (70, 35, 15, 255))
            else:
                c.pset(x, y, (50, 22, 8, 255))
        # Center dashed road line
        if y % 3 == 0:
            c.pset(32, y, C_GOLD_HI)

    # Golden Winged Sandal (Talaria of Hermes) hovering in mid-air
    # Sandal Sole at y: 36, x: 23 to 41
    c.fill_rect(23, 36, 41, 38, PAL_GOLD['outline'])
    c.fill_rect(24, 36, 40, 37, C_GOLD_MID)
    c.line(24, 36, 40, 36, C_GOLD_HI)
    c.line(24, 37, 40, 37, C_GOLD_SHADOW)
    # Straps wrapping ankle & foot
    straps = [(26, 34), (27, 33), (29, 34), (32, 32), (35, 34), (38, 35)]
    for sx, sy in straps:
        c.pset(sx, sy, C_GOLD_HI)
        c.pset(sx, sy + 1, C_GOLD_DARK)

    # Majestic Feathered Wings unfurled on the heel!
    # Left Wing (x: 15 to 27, y: 20 to 35)
    left_wing_pts = [
        (26, 34), (22, 30), (18, 25), (15, 20),
        (17, 24), (20, 28), (23, 32), (25, 35)
    ]
    for i in range(len(left_wing_pts) - 1):
        c.line(left_wing_pts[i][0], left_wing_pts[i][1], left_wing_pts[i + 1][0], left_wing_pts[i + 1][1], C_GOLD_HI)
    # Fill left wing feathers with bright white-gold
    for y in range(21, 34):
        for x in range(16, 27):
            if (x - 15) * 1.2 + 20 <= y <= 35 - (x - 15) * 0.3:
                c.pset(x, y, C_WHITE if (x + y) % 2 == 0 else C_GOLD_HI)

    # Right Wing (x: 37 to 49, y: 20 to 35)
    right_wing_pts = [
        (38, 34), (42, 30), (46, 25), (49, 20),
        (47, 24), (44, 28), (41, 32), (39, 35)
    ]
    for i in range(len(right_wing_pts) - 1):
        c.line(right_wing_pts[i][0], right_wing_pts[i][1], right_wing_pts[i + 1][0], right_wing_pts[i + 1][1], C_GOLD_HI)
    # Fill right wing feathers
    for y in range(21, 34):
        for x in range(37, 49):
            if (49 - x) * 1.2 + 20 <= y <= 35 - (49 - x) * 0.3:
                c.pset(x, y, C_WHITE if (x + y) % 2 == 0 else C_GOLD_HI)

    # Radiant golden sparkle halo
    for hx, hy in [(20, 18), (44, 18), (32, 28), (32, 42)]:
        c.pset(hx, hy, C_WHITE)

    return c
