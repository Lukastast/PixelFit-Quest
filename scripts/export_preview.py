"""Write res/achievements/preview.html from the generated 128x128 icons."""
import os

def main():
    src_128 = os.path.join("res", "achievements", "128x128")
    files = [f for f in os.listdir(src_128) if f.endswith(".png")]

    tier_map = {
        'bronze_workout_1': ('Bronze', 'bronze', 'Rookie'),
        'iron_workout_5': ('Bronze', 'bronze', 'Regular'),
        'silver_workout_10': ('Silver', 'silver', 'Veteran'),
        'steel_workout_25': ('Silver', 'silver', 'Grinder'),
        'gold_workout_50': ('Gold', 'gold', 'Legend'),
        'platinum_workout_100': ('Platinum', 'platinum', 'Immortal'),
        'streak_3': ('Bronze', 'bronze', 'On Fire'),
        'streak_7': ('Silver', 'silver', 'Week Warrior'),
        'streak_14': ('Gold', 'gold', 'Unstoppable'),
        'streak_30': ('Platinum', 'platinum', 'Habit Hero'),
        'steps_5000': ('Bronze', 'bronze', 'First Miles'),
        'steps_10000': ('Bronze', 'bronze', 'Walker'),
        'steps_50000': ('Silver', 'silver', 'Roadster'),
        'steps_100000': ('Gold', 'gold', 'Marathoner'),
        'volume_1000': ('Bronze', 'bronze', 'Iron Novice'),
        'volume_10000': ('Silver', 'silver', 'Iron Veteran'),
        'volume_50000': ('Gold', 'gold', 'Iron Titan'),
        'sets_10': ('Bronze', 'bronze', 'Set Starter'),
        'sets_50': ('Silver', 'silver', 'Set Grinder'),
        'sets_200': ('Gold', 'gold', 'Set Machine'),
        'level_5': ('Bronze', 'bronze', 'Apprentice'),
        'level_10': ('Silver', 'silver', 'Adventurer'),
        'level_20': ('Gold', 'gold', 'Champion'),
        'unique_3': ('Bronze', 'bronze', 'Variety Pack'),
        'unique_8': ('Silver', 'silver', 'Full Roster'),
        'locked_achievement': ('Locked', 'locked', 'Locked (Shared)'),
        'form_even_bar': ('Gold', 'gold', 'True Bar'),
        'form_square': ('Gold', 'gold', 'Square Press'),
        'form_no_dump': ('Gold', 'gold', 'Slow Lower'),
        'clip_true': ('Silver', 'silver', 'Right Sleeve'),
        'rom_honest': ('Gold', 'gold', 'Full Stroke'),
    }

    html = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>PixelFit Quest — 16-Bit SNES Achievement Icons</title>
<style>
  body {
    background: #14121d;
    color: #edeaf5;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    margin: 0;
    padding: 30px;
  }
  h1 { color: #ffc01e; margin-bottom: 5px; }
  p.sub { color: #9c94ad; margin-top: 0; margin-bottom: 25px; }
  .grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 16px;
  }
  .card {
    background: #201c2e;
    border: 2px solid #332d47;
    border-radius: 8px;
    padding: 16px 12px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    box-shadow: 0 4px 12px rgba(0,0,0,0.3);
  }
  .preview-box {
    width: 128px;
    height: 128px;
    background-image: 
      linear-gradient(45deg, #181524 25%, transparent 25%), 
      linear-gradient(-45deg, #181524 25%, transparent 25%), 
      linear-gradient(45deg, transparent 75%, #181524 75%), 
      linear-gradient(-45deg, transparent 75%, #181524 75%);
    background-size: 16px 16px;
    background-position: 0 0, 0 8px, 8px -8px, -8px 0px;
    background-color: #242034;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 12px;
    border: 1px solid #3d3554;
  }
  .card img {
    image-rendering: pixelated;
    width: 112px;
    height: 112px;
  }
  .name {
    font-weight: bold;
    font-size: 14px;
    color: #ffffff;
    margin-bottom: 4px;
  }
  .filename {
    font-family: monospace;
    font-size: 11px;
    color: #ffb834;
    word-break: break-all;
    margin-bottom: 4px;
  }
  .badge-tag {
    font-size: 10px;
    padding: 2px 8px;
    border-radius: 4px;
    font-weight: 600;
    text-transform: uppercase;
  }
  .bronze { background: #5a2c14; color: #ffaf82; }
  .silver { background: #2f3e50; color: #d0e4f5; }
  .gold { background: #613e0c; color: #ffe482; }
  .platinum { background: #1f3747; color: #94f0ff; }
  .locked { background: #353840; color: #b8bec9; }
</style>
</head>
<body>
  <h1>PixelFit Quest — 16-Bit SNES Achievement Icons</h1>
  <p class="sub">31 custom square badge / relic icons (64×64 native & 128×128 pixel-scaled) with transparent backgrounds, 1–2px dark outlines, and curated tier palettes.</p>
  <div class="grid">
"""

    for f in sorted(files):
        ach_id = f.replace(".png", "")
        tier_label, tier_cls, name = tier_map.get(ach_id, ("Gold", "gold", ach_id))
        html += f"""
    <div class="card">
      <div class="preview-box">
        <img src="128x128/{f}" alt="{name}">
      </div>
      <div class="name">{name}</div>
      <div class="filename">{f}</div>
      <span class="badge-tag {tier_cls}">{tier_label}</span>
    </div>"""

    html += """
  </div>
</body>
</html>
"""
    preview_path = os.path.join("res", "achievements", "preview.html")
    with open(preview_path, "w", encoding="utf-8") as pf:
        pf.write(html)
    print(f"Generated {preview_path}")

if __name__ == "__main__":
    main()
