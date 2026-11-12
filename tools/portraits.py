#!/usr/bin/env python3
"""Retratos 32x32 das classes (pixel art procedural) num atlas 256x128 (8 colunas x 4 linhas).

A ordem segue a ordem de registro em ClassCatalog.java (lida do código-fonte), que é a mesma de RpgClass.all().
"""
import os
import re

from PIL import Image

import art

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
CATALOG = os.path.join(ROOT, "src", "main", "java", "com", "europa", "rpg", "content", "ClassCatalog.java")
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "europa", "textures", "gui", "portraits.png")

SKIN = ["#f1c7a5", "#d9a37c", "#a8714f", "#7a4a33", "#e8d3c0", "#9fb7a0", "#b8b0d0"]
CATEGORY_BG = {"MARTIAL": "#5a2a22", "DEXTERITY": "#23492f", "ARCANE": "#253a66", "SPIRITUAL": "#4a3566", "HYBRID": "#5a4a1e"}

# id: (cabeça, cor principal, cor secundária, pele, extras)
SPEC = {
    "warrior": ("helm", "#9aa3ad", "#c9a227", 0, []),
    "barbarian": ("horned", "#7a5c3a", "#d8d0c0", 1, ["beard", "paint"]),
    "paladin": ("helm", "#e0d8b0", "#d9a62a", 4, ["plume"]),
    "samurai": ("topknot", "#b02a2a", "#1a1a1a", 1, ["band"]),
    "monk": ("bald", "#d97a1a", "#f5d76e", 2, ["band", "dots"]),
    "gladiator": ("crest", "#b07a3a", "#c03030", 1, []),
    "rogue": ("hood", "#2f3a2f", "#1a1f1a", 0, ["mask"]),
    "ranger": ("hood", "#3f6a35", "#6b4a2a", 1, []),
    "crossbowman": ("cap", "#5a5a5a", "#a0702a", 2, ["goggles"]),
    "arcane_gunner": ("tricorn", "#2a3a6a", "#d9a62a", 0, []),
    "swordsman": ("hair", "#e8e8ff", "#3a5aa0", 4, ["feather"]),
    "tamer": ("fur", "#8a6a4a", "#d0c0a0", 1, ["paint"]),
    "mage": ("wizard", "#3a5ad0", "#f5d76e", 4, ["beard_white"]),
    "sorcerer": ("wizard", "#8a3ad0", "#ff7ad0", 0, ["glow_pink"]),
    "pyromancer": ("wizard", "#c0301a", "#ffb000", 1, ["flame"]),
    "cryomancer": ("wizard", "#bfe8ff", "#4aa0e0", 4, ["frost"]),
    "electromancer": ("wizard", "#d0b020", "#5ad0ff", 0, ["bolt"]),
    "geomancer": ("wizard", "#7a5a3a", "#5aa05a", 2, []),
    "illusionist": ("hood", "#8a3aa0", "#e0e0ff", 4, ["halfmask"]),
    "graviturge": ("hood", "#2a1a4a", "#b366ff", 6, ["glow_purple", "orb"]),
    "cleric": ("circlet", "#f0f0f0", "#d9a62a", 0, []),
    "druid": ("antlers", "#3a7a3a", "#8a6a3a", 5, ["leaves"]),
    "necromancer": ("hood", "#1f1a2a", "#5a4a7a", 6, ["glow_cyan", "skull"]),
    "warlock": ("hood", "#4a1a2a", "#c03030", 3, ["glow_red", "small_horns"]),
    "shaman": ("feathers", "#a05a2a", "#2aa0a0", 3, ["paint"]),
    "bard": ("hat_feather", "#c03a8a", "#f5d76e", 1, []),
    "dragon_knight": ("dragon_helm", "#7a1a1a", "#d9a62a", 2, []),
    "war_arcanist": ("helm", "#3a4a8a", "#b366ff", 0, ["runes"]),
    "artificer": ("hair", "#6b4a2a", "#d9a62a", 2, ["goggles"]),
    "alchemist": ("hair", "#4a7a4a", "#5ad0a0", 4, ["goggles_green"]),
    "arcane_blade": ("circlet", "#6a3ab0", "#5ad0ff", 1, ["runes"]),
    "summoner": ("hood", "#2a5a6a", "#f5d76e", 5, ["glow_yellow"]),
}


def class_order():
    src = open(CATALOG, encoding="utf-8").read()
    cats = []
    for m in re.finditer(r'current = Category\.(\w+);|cls\("(\w+)"', src):
        if m.group(1):
            cat = m.group(1)
        else:
            cats.append((m.group(2), cat))
    return cats


def px(img, x, y, c):
    if 0 <= x < 32 and 0 <= y < 32:
        img.putpixel((x, y), art.hexc(c) if isinstance(c, str) else c)


def rect(img, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px(img, x, y, c)


def portrait(cid, category):
    head, main, sec, skin_i, extras = SPEC[cid]
    img = Image.new("RGBA", (32, 32))
    bg = art.hexc(CATEGORY_BG[category])
    for y in range(32):
        for x in range(32):
            d = ((x - 16) ** 2 + (y - 14) ** 2) ** 0.5
            img.putpixel((x, y), art.shade(bg, 1.25 - d / 30))
    M, S = art.ramp(main), art.ramp(sec)
    skin = art.ramp(SKIN[skin_i])

    # ombros/torso
    for y in range(24, 32):
        w = 10 + (y - 24)
        for x in range(16 - w, 16 + w):
            px(img, x, y, M["m"] if 13 <= x <= 18 or y > 26 else M["d"])
    rect(img, 14, 24, 17, 31, S["m"])
    rect(img, 13, 21, 18, 24, skin["d"])  # pescoço

    # rosto
    for y in range(8, 23):
        half = 6 if 10 <= y <= 18 else (5 if y in (9, 19) else 4)
        for x in range(16 - half, 16 + half):
            tone = skin["l"] if x < 13 else skin["d"] if x > 19 else skin["m"]
            px(img, x, y, tone)
    eye = art.hexc("#1a1420")
    glow = {"glow_cyan": "#7ff7ff", "glow_red": "#ff4a4a", "glow_purple": "#d07aff", "glow_pink": "#ff9ad8", "glow_yellow": "#ffe14a"}
    eye_c = next((art.hexc(glow[e]) for e in extras if e in glow), None)
    for x in (12, 13, 18, 19):
        px(img, x, 14, eye_c or (art.hexc("#ffffff") if x in (12, 18) else eye))
    if not eye_c:
        px(img, 13, 14, eye); px(img, 19, 14, eye)
    rect(img, 12, 12, 13, 12, skin["o"]); rect(img, 18, 12, 19, 12, skin["o"])  # sobrancelhas
    px(img, 16, 16, skin["d"]); px(img, 16, 17, skin["d"])                        # nariz
    rect(img, 14, 19, 18, 19, art.shade(skin["d"], 0.8))                          # boca

    # cabeça / chapéu
    if head in ("helm", "dragon_helm", "crest"):
        rect(img, 9, 5, 22, 12, M["m"]); rect(img, 9, 5, 22, 5, M["l"])
        rect(img, 9, 12, 9, 20, M["d"]); rect(img, 22, 12, 22, 20, M["d"])
        rect(img, 10, 13, 21, 13, M["o"])
        rect(img, 15, 6, 16, 18, S["m"])
        if head == "dragon_helm":
            for i in range(5):
                px(img, 8 - i // 2, 6 - i, S["l"]); px(img, 23 + i // 2, 6 - i, S["l"])
        if head == "crest":
            rect(img, 13, 1, 18, 4, S["m"]); rect(img, 13, 1, 18, 1, S["l"])
    elif head == "horned":
        rect(img, 9, 6, 22, 10, M["m"]); rect(img, 9, 6, 22, 6, M["l"])
        for i in range(6):
            px(img, 8 - i // 2, 9 - i, S["l"]); px(img, 7 - i // 2, 9 - i, S["m"])
            px(img, 23 + i // 2, 9 - i, S["l"]); px(img, 24 + i // 2, 9 - i, S["m"])
    elif head == "hood":
        for y in range(3, 24):
            w = min(10, 4 + y)
            for x in range(16 - w, 16 + w):
                inside = 10 <= x <= 21 and 9 <= y <= 22
                if not inside or (y <= 10 and 11 <= x <= 20):
                    px(img, x, y, M["d"] if x > 16 else M["m"])
        rect(img, 10, 9, 21, 9, M["o"])
    elif head == "wizard":
        for y in range(0, 10):
            w = max(1, (y + 1) // 2 + 1)
            cx = 17 - (3 - y // 3) if y < 4 else 16
            for x in range(cx - w, cx + w):
                px(img, x, y, M["m"] if x < cx else M["d"])
        rect(img, 7, 9, 24, 10, M["l"]); rect(img, 7, 11, 24, 11, M["d"])
        rect(img, 12, 8, 19, 8, S["m"])
    elif head in ("topknot", "bald"):
        rect(img, 10, 7, 21, 9, art.hexc("#1a1420") if head == "topknot" else skin["l"])
        if head == "topknot":
            rect(img, 14, 3, 17, 6, art.hexc("#1a1420"))
    elif head == "cap":
        rect(img, 9, 6, 22, 10, M["m"]); rect(img, 9, 10, 25, 11, M["d"])
    elif head == "tricorn":
        rect(img, 6, 8, 25, 10, M["d"]); rect(img, 10, 4, 21, 8, M["m"]); rect(img, 6, 8, 25, 8, S["m"])
    elif head == "hair":
        rect(img, 9, 6, 22, 10, M["m"]); rect(img, 9, 6, 22, 6, M["l"]); rect(img, 9, 10, 10, 16, M["d"]); rect(img, 21, 10, 22, 16, M["d"])
    elif head == "fur":
        rect(img, 8, 5, 23, 10, M["m"]); rect(img, 8, 10, 9, 22, M["d"]); rect(img, 22, 10, 23, 22, M["d"])
        for (x, y) in ((9, 3), (10, 4), (21, 4), (22, 3)):
            px(img, x, y, M["l"])
    elif head == "circlet":
        rect(img, 10, 6, 21, 9, art.hexc("#e8d8a8") if main == "#f0f0f0" else M["d"])
        rect(img, 9, 10, 22, 10, S["m"]); px(img, 16, 10, art.hexc("#5ad0ff")); px(img, 15, 10, art.hexc("#5ad0ff"))
    elif head == "antlers":
        rect(img, 10, 6, 21, 9, art.hexc("#5a3a1e"))
        for i in range(6):
            px(img, 10 - i, 6 - i, S["l"]); px(img, 21 + i, 6 - i, S["l"])
            if i in (2, 4):
                px(img, 10 - i + 2, 6 - i - 1, S["l"]); px(img, 21 + i - 2, 6 - i - 1, S["l"])
    elif head == "feathers":
        rect(img, 10, 7, 21, 9, M["m"])
        for i, c in enumerate(("#d03030", "#ffd84d", "#2aa0a0", "#ffffff")):
            rect(img, 11 + i * 3, 1 + i % 2, 12 + i * 3, 7, c)
    elif head == "hat_feather":
        rect(img, 8, 7, 23, 9, M["m"]); rect(img, 11, 4, 20, 7, M["d"])
        for i in range(7):
            px(img, 21 + i // 2, 6 - i, art.hexc("#f5f5f5"))

    for e in extras:
        if e == "beard":
            for y in range(18, 25):
                rect(img, 11 + (y - 18) // 3, y, 20 - (y - 18) // 3, y, art.hexc("#6a3a1a"))
        if e == "beard_white":
            for y in range(18, 27):
                rect(img, 11 + (y - 18) // 3, y, 20 - (y - 18) // 3, y, art.hexc("#e8e8e8"))
        if e == "mask":
            rect(img, 10, 17, 21, 21, art.hexc("#1a1f1a"))
        if e == "halfmask":
            rect(img, 16, 11, 21, 17, art.hexc("#f0f0ff"))
            px(img, 18, 14, art.hexc("#1a1420"))
        if e in ("goggles", "goggles_green"):
            lens = art.hexc("#5ad0a0" if e == "goggles_green" else "#ffd84d")
            rect(img, 9, 7, 22, 7, art.hexc("#3a2a1a"))
            for cx in (12, 19):
                rect(img, cx - 1, 6, cx + 1, 8, art.hexc("#6b4a2a")); px(img, cx, 7, lens)
        if e == "band":
            rect(img, 10, 10, 21, 10, S["m"])
        if e == "paint":
            rect(img, 11, 15, 13, 15, art.hexc("#c03030")); rect(img, 18, 15, 20, 15, art.hexc("#c03030"))
        if e == "dots":
            px(img, 16, 11, art.hexc("#c03030"))
        if e == "plume":
            rect(img, 15, 0, 16, 5, art.hexc("#c03030"))
        if e == "feather":
            for i in range(6):
                px(img, 21 + i // 2, 7 - i, art.hexc("#c03030"))
        if e == "flame":
            for (x, y) in ((14, 0), (15, 1), (16, 0), (17, 2), (13, 2)):
                px(img, x, y, art.hexc("#ffb000"))
        if e == "frost":
            for (x, y) in ((8, 4), (24, 6), (26, 12), (5, 14)):
                px(img, x, y, art.hexc("#ffffff"))
        if e == "bolt":
            for (x, y) in ((24, 2), (23, 3), (24, 4), (23, 5), (22, 6)):
                px(img, x, y, art.hexc("#5ad0ff"))
        if e == "orb":
            rect(img, 24, 24, 27, 27, art.hexc("#b366ff")); px(img, 24, 24, art.hexc("#ffffff"))
        if e == "skull":
            rect(img, 4, 25, 7, 28, art.hexc("#e8e0d0")); px(img, 5, 26, art.hexc("#1a1420")); px(img, 6, 26, art.hexc("#1a1420"))
        if e == "small_horns":
            px(img, 10, 3, art.hexc("#2a1a1a")); px(img, 10, 4, art.hexc("#2a1a1a")); px(img, 21, 3, art.hexc("#2a1a1a")); px(img, 21, 4, art.hexc("#2a1a1a"))
        if e == "runes":
            for x in range(9, 23, 3):
                px(img, x, 28, art.hexc("#5ad0ff"))
        if e == "leaves":
            for (x, y) in ((9, 9), (22, 8), (12, 6), (19, 6)):
                px(img, x, y, art.hexc("#5ad05a"))

    # moldura
    frame = art.hexc("#2a2018")
    for i in range(32):
        for (x, y) in ((i, 0), (i, 31), (0, i), (31, i)):
            img.putpixel((x, y), frame)
    return img


def main():
    order = class_order()
    atlas = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    for i, (cid, cat) in enumerate(order):
        atlas.alpha_composite(portrait(cid, cat), ((i % 8) * 32, (i // 8) * 32))
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    atlas.save(OUT)
    print(len(order), "retratos")


if __name__ == "__main__":
    main()
