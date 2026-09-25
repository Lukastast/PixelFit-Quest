# PixelFit Quest - UI & Responsive Design Rules

## 1. Pixel Art Buttons & Asset Sizing Parity
- **State Consistency**: Active or "EQUIPPED" buttons must use dedicated pixel-art assets (e.g., `button_green_clicked.png`) rather than flat Compose `Box` background colors.
- **Bounding Box & Margin Parity**: Pixel art buttons (`button_unclicked`, `button_clicked`, `button_green_clicked`) contain transparent border padding (22px horizontal, 19px vertical within a 352×174 canvas). Never replace a pixel art button with a raw colored `Box`, as raw boxes lack transparent padding and appear visibly larger than adjacent buttons.
- **Modular Modifier Parameter**: All action and settings cards must accept `modifier: Modifier = Modifier` so callers can control padding dynamically between portrait (e.g. 32dp horizontal margin) and landscape (edge-to-edge column fill).

## 2. Landscape & Two-Pane Layout Architecture
- **Orientation Detection**: Use `val useTwoPane = spacing.widthClass != PixelFitWidthClass.Compact || LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE`.
- **Partitioning Categories**: In landscape and foldables, do not stretch vertical lists across the entire screen width. Partition related options into balanced side-by-side columns:
  - *Customization*: Left column for preview & stage; right column for items grid.
  - *Settings*: Left column for Gameplay & Preferences; right column for Account & Data.
  - *Workouts*: Left pane for Templates; right pane for History.
- **Plaque & Header Proportions**: Never allow wooden/stone info banners (`info_background`) to stretch 100% width across wide screens. Constrain header plaques to ~40–45% width (or `widthIn(max = 380–420.dp)`) with compact height (`42dp`) and centered alignment.
- **Dialog Constraints**: Constrain modal dialog backgrounds (`questloginboard`) using `widthIn(max = spacing.scale(420))` so modals remain properly proportioned in landscape rather than stretching edge-to-edge.

## 3. Preview Boxes & Clearances (`HeroStage`)
- **Landscape HeroStage Layout**: Title and status badges centered at the top; character sprite on the left; secondary controls (such as Gender toggle) stacked vertically on the right.
- **Portrait Space Preservation**: Combine single-action buttons and calibration cards side-by-side on one line (`46dp` height) to preserve vertical space for the grid below.
- **Locked Banner Clearance**: When an overlay banner (e.g. locked status) is anchored at `Alignment.TopCenter`, apply dedicated top padding/clearance (`spacing.scale(28–30)`) to the inner content column to guarantee the avatar/item sprite never collides or overlaps with the banner.
- **Element Spacing Hierarchy**: Maintain clear separation between sprite and title (`14–16dp`), and tight pairing between title/bonus and controls (`4dp`).

## 4. Texture Alignment & Subpixel Seam Prevention
- **Opaque Texture Rectangles**: In `AppScaffold` and custom navigation bars, avoid drawing semi-transparent edge pixels from sprite borders (e.g. use `srcLeft = 7`, `srcRight = 161`, `srcTop = 7`).
- **1dp Visual Overlap**: Use a 1dp overlap (`.offset(x = (-1).dp).width(totalWidth + 1.dp)` in landscape, or `.offset(y = (-1).dp).height(totalHeight + 1.dp)` in portrait) between navigation bars and home/dwelling backgrounds to eliminate subpixel rounding seams on high-DPI displays.
