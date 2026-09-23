"""
pixel_canvas.py
16-bit SNES Pixel Art Rendering Engine for PixelFit Quest
Provides color palettes, badge frame generation, drawing primitives,
and export to 64x64 and 128x128 PNG.
"""
from PIL import Image
import numpy as np

# -------------------------------------------------------------------------
# PALETTES (Curated 16-bit SNES RPG Palettes per Tier)
# -------------------------------------------------------------------------
# Transparent
T = (0, 0, 0, 0)

# Bronze Tier: copper, rust, leather
PAL_BRONZE = {
    'outline': (30, 14, 8, 255),
    'edge_hi': (248, 176, 128, 255),
    'metal_hi': (224, 136, 76, 255),
    'metal_mid': (184, 90, 40, 255),
    'metal_dark': (128, 50, 18, 255),
    'rust': (110, 45, 18, 255),
    'rust_dark': (68, 26, 10, 255),
    'recess_bg': (42, 22, 12, 255),
    'recess_dark': (24, 12, 6, 255),
    'rivet_hi': (255, 210, 160, 255),
    'rivet_dark': (50, 20, 10, 255),
}

# Silver Tier: steel, moon, chainmail
PAL_SILVER = {
    'outline': (16, 20, 28, 255),
    'edge_hi': (250, 252, 255, 255),
    'metal_hi': (216, 228, 240, 255),
    'metal_mid': (140, 160, 180, 255),
    'metal_dark': (80, 96, 115, 255),
    'steel_shadow': (48, 60, 75, 255),
    'recess_bg': (28, 36, 48, 255),
    'recess_dark': (16, 22, 30, 255),
    'rivet_hi': (255, 255, 255, 255),
    'rivet_dark': (32, 40, 52, 255),
}

# Gold Tier: fire orange, gold trim, torch
PAL_GOLD = {
    'outline': (32, 14, 2, 255),
    'edge_hi': (255, 244, 170, 255),
    'metal_hi': (255, 204, 40, 255),
    'metal_mid': (220, 148, 16, 255),
    'metal_dark': (160, 88, 8, 255),
    'fire_hi': (255, 120, 20, 255),
    'fire_mid': (230, 60, 10, 255),
    'recess_bg': (44, 18, 6, 255),
    'recess_dark': (24, 8, 2, 255),
    'rivet_hi': (255, 252, 210, 255),
    'rivet_dark': (70, 32, 4, 255),
}

# Platinum Tier: white-gold, crystal, night sky
PAL_PLATINUM = {
    'outline': (12, 10, 24, 255),
    'edge_hi': (255, 255, 255, 255),
    'metal_hi': (242, 246, 235, 255),
    'metal_mid': (180, 210, 230, 255),
    'crystal_hi': (160, 240, 255, 255),
    'crystal_mid': (70, 170, 220, 255),
    'crystal_dark': (30, 90, 150, 255),
    'recess_bg': (20, 16, 38, 255),
    'recess_dark': (10, 8, 20, 255),
    'rivet_hi': (220, 250, 255, 255),
    'rivet_dark': (28, 24, 52, 255),
}

# Locked Stone Tier: weathered grey stone, cracked, iron chain
PAL_LOCKED = {
    'outline': (20, 22, 26, 255),
    'stone_hi': (176, 182, 192, 255),
    'stone_mid': (116, 122, 134, 255),
    'stone_dark': (70, 75, 84, 255),
    'fissure': (24, 26, 32, 255),
    'recess_bg': (52, 56, 64, 255),
    'recess_dark': (32, 35, 42, 255),
    'chain_hi': (205, 215, 225, 255),
    'chain_mid': (135, 145, 160, 255),
    'chain_dark': (65, 72, 85, 255),
    'chain_edge': (25, 28, 35, 255),
}

# Common General Sprite Colors
C_BLACK = (20, 16, 16, 255)
C_WHITE = (255, 255, 255, 255)
C_RED = (210, 36, 32, 255)
C_DARK_RED = (130, 18, 16, 255)
C_WOOD_HI = (195, 136, 75, 255)
C_WOOD_MID = (145, 88, 42, 255)
C_WOOD_DARK = (92, 50, 22, 255)
C_WOOD_SHADOW = (52, 26, 10, 255)
C_LEATHER_HI = (170, 95, 45, 255)
C_LEATHER_MID = (120, 60, 25, 255)
C_LEATHER_DARK = (65, 30, 12, 255)
C_EARTH_HI = (140, 110, 70, 255)
C_EARTH_MID = (95, 70, 42, 255)
C_EARTH_DARK = (55, 38, 22, 255)
C_STEEL_HI = (235, 245, 255, 255)
C_STEEL_MID = (155, 175, 195, 255)
C_STEEL_DARK = (75, 95, 115, 255)
C_STEEL_SHADOW = (40, 52, 65, 255)
C_GOLD_HI = (255, 240, 120, 255)
C_GOLD_MID = (245, 190, 20, 255)
C_GOLD_DARK = (180, 115, 10, 255)
C_GOLD_SHADOW = (100, 55, 5, 255)
C_FIRE_YELLOW = (255, 245, 80, 255)
C_FIRE_ORANGE = (255, 125, 15, 255)
C_FIRE_RED = (215, 35, 15, 255)
C_GREEN_HI = (110, 235, 75, 255)
C_GREEN_MID = (45, 170, 35, 255)
C_GREEN_DARK = (18, 95, 22, 255)
C_CYAN_HI = (175, 245, 255, 255)
C_CYAN_MID = (65, 185, 235, 255)
C_CYAN_DARK = (20, 100, 155, 255)


class PixelCanvas:
    def __init__(self, size=64):
        self.size = size
        self.grid = np.zeros((size, size, 4), dtype=np.uint8)

    def pset(self, x, y, color):
        """Set a single pixel if inside canvas."""
        if 0 <= x < self.size and 0 <= y < self.size:
            if len(color) == 3:
                color = (color[0], color[1], color[2], 255)
            self.grid[y, x] = color

    def fill_rect(self, x1, y1, x2, y2, color):
        """Fill rectangle inclusive of bounds."""
        min_x, max_x = max(0, min(x1, x2)), min(self.size - 1, max(x1, x2))
        min_y, max_y = max(0, min(y1, y2)), min(self.size - 1, max(y1, y2))
        if len(color) == 3:
            color = (color[0], color[1], color[2], 255)
        self.grid[min_y:max_y + 1, min_x:max_x + 1] = color

    def rect(self, x1, y1, x2, y2, color):
        """Draw rectangle border."""
        min_x, max_x = min(x1, x2), max(x1, x2)
        min_y, max_y = min(y1, y2), max(y1, y2)
        self.line(min_x, min_y, max_x, min_y, color)
        self.line(min_x, max_y, max_x, max_y, color)
        self.line(min_x, min_y, min_x, max_y, color)
        self.line(max_x, min_y, max_x, max_y, color)

    def line(self, x0, y0, x1, y1, color):
        """Bresenham's line algorithm."""
        dx = abs(x1 - x0)
        dy = -abs(y1 - y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx + dy
        while True:
            self.pset(x0, y0, color)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def fill_circle(self, cx, cy, r, color):
        """Fill circle centered at (cx, cy)."""
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r ** 2:
                    self.pset(x, y, color)

    def circle(self, cx, cy, r, color):
        """Draw circle outline."""
        x = r
        y = 0
        err = 0
        while x >= y:
            self.pset(cx + x, cy + y, color)
            self.pset(cx + y, cy + x, color)
            self.pset(cx - y, cy + x, color)
            self.pset(cx - x, cy + y, color)
            self.pset(cx - x, cy - y, color)
            self.pset(cx - y, cy - x, color)
            self.pset(cx + y, cy - x, color)
            self.pset(cx + x, cy - y, color)
            y += 1
            err += 1 + 2 * y
            if 2 * (err - x) + 1 > 0:
                x -= 1
                err += 1 - 2 * x

    def draw_badge_frame(self, tier):
        """
        Draws the 16-bit SNES square relic badge frame with chamfered corners,
        metallic/stone bevel, corner rivets, and deep textured recess.
        """
        if tier == 'bronze':
            pal = PAL_BRONZE
        elif tier == 'silver':
            pal = PAL_SILVER
        elif tier == 'gold':
            pal = PAL_GOLD
        elif tier == 'platinum':
            pal = PAL_PLATINUM
        elif tier == 'locked':
            pal = PAL_LOCKED
        else:
            raise ValueError(f"Unknown tier: {tier}")

        def cut_corner(x, y, min_sum):
            return (
                (x - 5) + (y - 5) < min_sum
                or (58 - x) + (y - 5) < min_sum
                or (x - 5) + (58 - y) < min_sum
                or (58 - x) + (58 - y) < min_sum
            )

        def frame_color(x, y):
            highlight = y <= 9 or x <= 9
            shadow = y >= 54 or x >= 54
            if tier == 'locked':
                if highlight:
                    return pal['stone_hi']
                if shadow:
                    return pal['stone_dark']
                return pal['stone_mid']
            if highlight:
                return pal['edge_hi']
            if not shadow:
                return pal['metal_mid']
            if tier == 'platinum':
                return pal['crystal_dark']
            if tier == 'silver':
                return pal['steel_shadow']
            if tier == 'bronze':
                return pal['rust_dark']
            return pal['metal_dark']

        # Badge is (5, 5)–(58, 58). Corners are cut on a 5px chamfer.
        for y in range(5, 59):
            for x in range(5, 59):
                if cut_corner(x, y, 5):
                    continue
                self.pset(x, y, pal['outline'])

        for y in range(6, 58):
            for x in range(6, 58):
                # <= 5 on the chamfer sum, so the outline ring stays put.
                if cut_corner(x, y, 6):
                    continue
                if x < 12 or x > 51 or y < 12 or y > 51:
                    self.pset(x, y, frame_color(x, y))

        # 3. Inner Recess Bevel and Fill (12, 12 to 51, 51)
        # Recess outline (11, 11 to 52, 52)
        for y in range(11, 53):
            for x in range(11, 53):
                if (x == 11 and (y == 11 or y == 52)) or (x == 52 and (y == 11 or y == 52)):
                    continue
                if x == 11 or x == 52 or y == 11 or y == 52:
                    # Shadow on top/left of recess, highlight on bottom/right
                    if x == 11 or y == 11:
                        self.pset(x, y, pal['recess_dark'])
                    else:
                        if tier == 'locked':
                            self.pset(x, y, pal['stone_dark'])
                        elif tier == 'platinum':
                            self.pset(x, y, pal['crystal_dark'])
                        elif tier == 'gold':
                            self.pset(x, y, pal['metal_dark'])
                        elif tier == 'silver':
                            self.pset(x, y, pal['metal_dark'])
                        else:
                            self.pset(x, y, pal['rust'])
                else:
                    # Inside the recess
                    self.pset(x, y, pal['recess_bg'])

        # 4. Subtle Texture in the recess
        if tier == 'bronze':
            # Leather grain texture
            for y in range(13, 51, 2):
                for x in range(13, 51, 2):
                    self.pset(x, y, (48, 26, 14, 255))
        elif tier == 'silver':
            # Chainmail / brushed steel mesh
            for y in range(13, 51, 3):
                for x in range(13, 51, 3):
                    self.pset(x, y, (35, 45, 60, 255))
        elif tier == 'gold':
            # Warm embers
            for y in range(13, 51, 4):
                for x in range(13, 51, 4):
                    self.pset(x, y, (56, 24, 8, 255))
        elif tier == 'platinum':
            # Celestial stars
            stars = [(16, 18), (46, 16), (22, 44), (45, 42), (18, 32), (44, 28), (32, 16)]
            for sx, sy in stars:
                self.pset(sx, sy, (120, 160, 220, 255))
                self.pset(sx + 1, sy, (230, 245, 255, 255))
        elif tier == 'locked':
            # Stone grain
            for y in range(13, 51):
                for x in range(13, 51):
                    if (x * 7 + y * 13) % 11 == 0:
                        self.pset(x, y, pal['stone_dark'])

        # 5. Corner Rivets / Studs on the metallic frame
        rivet_locs = [(10, 10), (53, 10), (10, 53), (53, 53)]
        for rx, ry in rivet_locs:
            if tier != 'locked':
                self.pset(rx, ry, pal['rivet_hi'])
                self.pset(rx + 1, ry, pal['rivet_hi'])
                self.pset(rx, ry + 1, pal['rivet_dark'])
                self.pset(rx + 1, ry + 1, pal['rivet_dark'])
            else:
                self.pset(rx, ry, pal['stone_hi'])
                self.pset(rx + 1, ry, pal['stone_dark'])
                self.pset(rx, ry + 1, pal['stone_dark'])
                self.pset(rx + 1, ry + 1, pal['fissure'])

    def to_image(self):
        """Return PIL RGBA Image at native size (64x64)."""
        return Image.fromarray(self.grid, 'RGBA')

    def to_128(self):
        """Return 2x nearest-neighbor integer scaled 128x128 image."""
        im = self.to_image()
        return im.resize((128, 128), resample=Image.Resampling.NEAREST)
