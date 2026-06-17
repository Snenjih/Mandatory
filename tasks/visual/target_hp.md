# Target HP

**ID:** `target_hp`  
**Category:** VISUAL  
**Status:** [ ] TODO  
**Class:** `modules/impl/TargetHpModule.java`

## Description

Zeigt die Lebenspunkte (HP) des aktuell anvisierten Mobs oder Spielers als HUD-Element an — wahlweise als Zahl, als Balken oder beides. Gibt im Kampf sofortiges Feedback über den Zustand des Gegners und hilft dabei, den richtigen Moment für den finalen Schlag einzuschätzen.

## Settings

| Setting ID | Type | Default | Range / Options | Label | Beschreibung |
|---|---|---|---|---|---|
| `x_pos` | Int | `10` | `0–1920` | "X Position" | Horizontale Position des HUD-Elements |
| `y_pos` | Int | `10` | `0–1080` | "Y Position" | Vertikale Position |
| `show_bar` | Boolean | `true` | — | "Show Bar" | HP-Balken anzeigen |
| `show_numbers` | Boolean | `true` | — | "Show Numbers" | Absolute HP-Zahl anzeigen |
| `show_name` | Boolean | `true` | — | "Show Name" | Name des Ziels anzeigen |
| `bar_width` | Int | `100` | `40–300` | "Bar Width" | Breite des HP-Balkens in Pixeln |
| `color_high` | Int | `0xFF55FF55` | ARGB Hex | "Color High" | Balkenfarbe bei hoher HP |
| `color_mid` | Int | `0xFFFFAA00` | ARGB Hex | "Color Mid" | Balkenfarbe bei mittlerer HP |
| `color_low` | Int | `0xFFFF5555` | ARGB Hex | "Color Low" | Balkenfarbe bei niedriger HP |
| `background` | Boolean | `true` | — | "Background" | Halbtransparenten Hintergrund zeichnen |

## Implementation

### Event Hooks

- `onRenderHud(DrawContext ctx, float tickDelta)` — `mc.targetedEntity` auslesen und HP rendern.

### Required Mixins

Kein Mixin erforderlich.

### Core Algorithm

```
onRenderHud(DrawContext ctx, float tickDelta):
    if (mc.player == null) return

    Entity targeted = mc.targetedEntity
    if (targeted == null) return
    if (!(targeted instanceof LivingEntity living)) return   // Nur LivingEntity hat HP

    float health    = living.getHealth()
    float maxHealth = living.getMaxHealth()
    float fraction  = health / maxHealth

    // Farbe (Ampel)
    int color
    if (fraction > 0.6f)      color = colorHigh.get()
    else if (fraction > 0.3f) color = colorMid.get()
    else                       color = colorLow.get()

    int x = xPos.get()
    int y = yPos.get()
    int currentY = y

    // Optionaler Hintergrund
    if (background.get()):
        int bgH = (showName.get() ? 10 : 0) + (showBar.get() ? 8 : 0) + (showNumbers.get() ? 10 : 0) + 4
        int bgW = Math.max(barWidth.get(), 100) + 4
        ctx.fill(x - 2, y - 2, x + bgW, y + bgH, 0x88000000)

    // Name des Ziels
    if (showName.get()):
        String name = living.getName().getString()
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal(name), x, currentY, 0xFFFFFFFF)
        currentY += 10

    // HP-Balken
    if (showBar.get()):
        ctx.fill(x, currentY, x + barWidth.get(), currentY + 6, 0xFF222222)
        int fill = Math.round(fraction * barWidth.get())
        ctx.fill(x, currentY, x + fill, currentY + 6, color)
        currentY += 9

    // HP-Zahlen
    if (showNumbers.get()):
        String hpText = String.format("%.1f / %.1f", health, maxHealth)
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal(hpText), x, currentY, color)
```

### Edge Cases

- `mc.targetedEntity == null`: Nichts rendern (kein Ziel).
- `mc.targetedEntity` ist kein `LivingEntity` (z. B. ein Boot, Item-Frame): `instanceof`-Check schlägt fehl → `return`.
- Spieler als Ziel auf Multiplayer: `ClientPlayerEntity` extends `LivingEntity` → funktioniert; HP über `getHealth()`.
- Ziel stirbt zwischen Frames: `health` wird 0, Balken leer.
- `maxHealth` änderbar (Attribute-Modifizierung): Immer `getMaxHealth()` aufrufen, nicht cachen.
- Sehr hohe HP (Raid-Boss, Wither): Zahlenwert kann breit werden — Balkenbreite und Textlänge nicht festlegend koppeln.
- Singleplayer vs. Multiplayer: `mc.targetedEntity` funktioniert in beiden Kontexten identisch.
- `targetedEntity` auf Clients manchmal leicht hinter dem Server: Flackern bei sehr schnellem Zielwechsel ist tolerierbar.

## Translation Keys

```json
"mandatory.target_hp.name": "Target HP",
"mandatory.target_hp.description": "Displays the health of the entity you are currently targeting.",
"mandatory.target_hp.x_pos": "X Position",
"mandatory.target_hp.y_pos": "Y Position",
"mandatory.target_hp.show_bar": "Show Bar",
"mandatory.target_hp.show_numbers": "Show Numbers",
"mandatory.target_hp.show_name": "Show Name",
"mandatory.target_hp.bar_width": "Bar Width",
"mandatory.target_hp.color_high": "Color High",
"mandatory.target_hp.color_mid": "Color Mid",
"mandatory.target_hp.color_low": "Color Low",
"mandatory.target_hp.background": "Background"
```

## Icon

**Pfad:** `src/main/resources/assets/mandatory/textures/gui/sprites/modules/target_hp.png`  
**Größe:** 32×32 PNG  
**Vorschlag:** Herz-Symbol (Minecraft-Style) mit einem Fadenkreuz oder Pfeil darauf; roter Balken darunter. Erinnert an den Vanilla-HP-Balken, aber mit Ziel-Overlay.
