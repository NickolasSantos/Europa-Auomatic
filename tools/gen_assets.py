#!/usr/bin/env python3
"""Gera modelos, blockstates, receitas, loot tables, tags, worldgen e texturas do Europa RPG.

Uso: python3 tools/gen_assets.py   (a partir da raiz do repositório)
As texturas são pixel-art procedural original (sem assets da Mojang). Re-executar sobrescreve os arquivos.
"""
import json
import os
import random

from PIL import Image

MOD = "europa"
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", MOD)
DATA = os.path.join(ROOT, "data", MOD)
MCDATA = os.path.join(ROOT, "data", "minecraft")


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3],)


# ----------------------------------------------------------------------------- texturas

def noise_tex(base, var, seed, size=16):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (size, size))
    for y in range(size):
        for x in range(size):
            f = 1 + rnd.uniform(-var, var)
            img.putpixel((x, y), shade(base, f))
    return img


def ore_tex(base, var, ore_light, ore_dark, seed, clusters=6):
    img = noise_tex(base, var, seed)
    rnd = random.Random(seed * 7 + 1)
    for _ in range(clusters):
        cx, cy = rnd.randint(1, 13), rnd.randint(1, 13)
        pts = [(cx, cy), (cx + 1, cy), (cx, cy + 1)] + ([(cx + 1, cy + 1)] if rnd.random() < 0.6 else [])
        for (x, y) in pts:
            img.putpixel((x, y), ore_dark)
        img.putpixel((cx, cy), ore_light)
        if rnd.random() < 0.5:
            img.putpixel((cx + 1, cy), ore_light)
    return img


def block_tex(light, mid, dark, seed):
    img = noise_tex(mid, 0.06, seed)
    for i in range(16):
        img.putpixel((i, 0), light)
        img.putpixel((0, i), light)
        img.putpixel((i, 15), dark)
        img.putpixel((15, i), dark)
    for (x, y) in [(3, 3), (4, 3), (3, 4), (11, 10), (12, 11), (11, 11), (6, 12), (12, 4)]:
        img.putpixel((x, y), light)
    return img


def planks_tex(seed, base="#8a5a2b"):
    rnd = random.Random(seed)
    b = hexc(base)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        row = 1 + rnd.uniform(-0.08, 0.08)
        for x in range(16):
            f = row + rnd.uniform(-0.04, 0.04)
            if y % 4 == 3:
                f = 0.65
            img.putpixel((x, y), shade(b, f))
    return img


def quest_board_front():
    img = planks_tex(11)
    paper, ink, pin = hexc("#efe2c0"), hexc("#5a4630"), hexc("#c0392b")
    for (x0, y0, x1, y1) in [(2, 2, 7, 8), (9, 3, 13, 9), (4, 10, 11, 14)]:
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                img.putpixel((x, y), paper)
        for y in range(y0 + 2, y1, 2):
            for x in range(x0 + 1, x1):
                img.putpixel((x, y), ink)
        img.putpixel(((x0 + x1) // 2, y0), pin)
    return img


def altar_top():
    img = noise_tex(hexc("#2b2b33"), 0.08, 21)
    rune = hexc("#b366ff")
    for i in range(3, 13):
        img.putpixel((i, 3), rune); img.putpixel((i, 12), rune)
        img.putpixel((3, i), rune); img.putpixel((12, i), rune)
    for (x, y) in [(7, 6), (8, 6), (6, 7), (9, 7), (6, 8), (9, 8), (7, 9), (8, 9), (7, 7), (8, 8)]:
        img.putpixel((x, y), hexc("#e0b3ff"))
    return img


def altar_side():
    img = noise_tex(hexc("#26262c"), 0.08, 22)
    for x in range(16):
        img.putpixel((x, 0), hexc("#3d3d47")); img.putpixel((x, 15), hexc("#141418"))
    for x in range(2, 14, 3):
        img.putpixel((x, 7), hexc("#9b4dff")); img.putpixel((x + 1, 8), hexc("#9b4dff"))
    return img


def from_template(rows, palette):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != "." and ch in palette:
                img.putpixel((x, y), palette[ch])
    return img


SWORD = [
    ".............ooo",
    "............obbo",
    "...........obmo.",
    "..........obmo..",
    ".........obmo...",
    "........obmo....",
    ".......obmo.....",
    "..oo..obmo......",
    "..ogoobmo.......",
    "...oggmo........",
    "....ogo.........",
    "...ohogo........",
    "..oho.oo........",
    ".oho............",
    "ojo.............",
    "oo..............",
]
PICKAXE = [
    "....oooooo......",
    "...obbbbbmo.....",
    "....ooommmmo....",
    ".......oohmmo...",
    "......oho.ommo..",
    ".....oho...omo..",
    "....oho.....omo.",
    "...oho......omo.",
    "..oho........oo.",
    ".oho............",
    "oho.............",
    "oo..............",
    "................",
    "................",
    "................",
    "................",
]
AXE = [
    "......ooo.......",
    ".....obbmo......",
    "....obbmmmo.....",
    "....obmmmhoo....",
    ".....ommoho.....",
    "......ooho......",
    ".....oho........",
    "....oho.........",
    "...oho..........",
    "..oho...........",
    ".oho............",
    "oho.............",
    "oo..............",
    "................",
    "................",
    "................",
]
SHOVEL = [
    "...........ooo..",
    "..........obbmo.",
    ".........obbmmo.",
    ".........obmmo..",
    "........ohoo....",
    ".......oho......",
    "......oho.......",
    ".....oho........",
    "....oho.........",
    "...oho..........",
    "..oho...........",
    ".oho............",
    "oho.............",
    "oo..............",
    "................",
    "................",
]
HAMMER = [
    "........oooooo..",
    ".......obbbbbmo.",
    "......obbmmmmmmo",
    "......obmmmmmmmo",
    ".......ommmmhmo.",
    "........oommoo..",
    ".......oho......",
    "......oho.......",
    ".....oho........",
    "....oho.........",
    "...oho..........",
    "..oho...........",
    ".oho............",
    "ojo.............",
    "oo..............",
    "................",
]
STAFF = [
    "...........ooo..",
    "..........obgbo.",
    ".........obgggbo",
    ".........ogbbbgo",
    "..........obgbo.",
    ".........ohooo..",
    "........oho.....",
    ".......oho......",
    "......oho.......",
    ".....oho........",
    "....oho.........",
    "...oho..........",
    "..oho...........",
    ".oho............",
    ".oo.............",
    "................",
]
HELMET = [
    "................",
    "................",
    "...oooooooooo...",
    "..obbbbbbbbbmo..",
    "..obmmmmmmmmmo..",
    "..obmoooooommo..",
    "..obmo....ommo..",
    "..omo......omo..",
    "..ooo......ooo..",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]
CHESTPLATE = [
    "................",
    "..oooo....oooo..",
    ".obbmoo..oobbmo.",
    ".obmmmooooommmo.",
    ".ommmmbbbbmmmmo.",
    "..ooommbbmmooo..",
    "....ommbbmmo....",
    "....ommmmmmo....",
    "....ommmmmmo....",
    "....ommmmmmo....",
    "....ommmmmmo....",
    "....oooooooo....",
    "................",
    "................",
    "................",
    "................",
]
LEGGINGS = [
    "................",
    "...oooooooooo...",
    "...obbbbbbbmo...",
    "...ommmmmmmmo...",
    "...ommmoommmo...",
    "...ommo..ommo...",
    "...ommo..ommo...",
    "...ommo..ommo...",
    "...ommo..ommo...",
    "...ommo..ommo...",
    "...oooo..oooo...",
    "................",
    "................",
    "................",
    "................",
    "................",
]
BOOTS = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "...oooo..oooo...",
    "...obmo..obmo...",
    "...obmo..obmo...",
    "..oobmo..obmoo..",
    ".obbbmo..obmmmo.",
    ".ommmmo..ommmmo.",
    ".oooooo..oooooo.",
    "................",
    "................",
    "................",
    "................",
]
INGOT = [
    "................",
    "................",
    "................",
    "................",
    "......ooooooo...",
    ".....obbbbbbmo..",
    "....obbmmmmmmo..",
    "...obbmmmmmmo...",
    "..obbmmmmmmmo...",
    "..ommmmmmmmo....",
    "..oooooooooo....",
    "................",
    "................",
    "................",
    "................",
    "................",
]
RAW = [
    "................",
    "................",
    "................",
    "......oooo......",
    "....oobbmmoo....",
    "...obbbmmmmmo...",
    "...obmmmmdmmo...",
    "..obmmdmmmmmmo..",
    "..ommmmmmdmmmo..",
    "..ommdmmmmmmo...",
    "...ommmmmmmo....",
    "....oooooooo....",
    "................",
    "................",
    "................",
    "................",
]
SHARD = [
    "................",
    "........o.......",
    ".......obo......",
    ".......obo......",
    "......obbmo.....",
    "......obmmo.....",
    ".....obbmmmo....",
    ".....obmmmmo....",
    "....obbmmmmmo...",
    ".....ommmmmo....",
    "......ommmo.....",
    ".......omo......",
    "........o.......",
    "................",
    "................",
    "................",
]
KEY = [
    "................",
    "................",
    "..oooo..........",
    ".obbbmo.........",
    ".obo.mo.........",
    ".obo.mo.........",
    ".obbbmooooooooo.",
    "..oooommmmmmmmo.",
    "..........omomo.",
    "..........oooo..",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]
BOOK = [
    "................",
    "..oooooooooooo..",
    "..obbbbbbbbbbo..",
    "..obmmmmmmmmpo..",
    "..obmmggggmmpo..",
    "..obmmgmmgmmpo..",
    "..obmmggggmmpo..",
    "..obmmmgmmmmpo..",
    "..obmmmgmmmmpo..",
    "..obmmmmmmmmpo..",
    "..obmmmmmmmmpo..",
    "..obbbbbbbbbpo..",
    "..oppppppppppo..",
    "..oooooooooooo..",
    "................",
    "................",
]

MATERIALS = {
    "mithril": {"o": "#1d3b4a", "b": "#f0fdff", "m": "#8fd3e8", "d": "#5fa8c0"},
    "adamantite": {"o": "#3a0a14", "b": "#ff8a96", "m": "#c0283e", "d": "#8e1a2c"},
}
HANDLE = {"h": "#6b4423", "j": "#4a2e17", "g": "#c9a227"}


def pal(d):
    return {k: hexc(v) for k, v in d.items()}


def armor_layer(base, accent, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (64, 32))
    b, a = hexc(base), hexc(accent)
    for y in range(32):
        for x in range(64):
            c = a if (x % 8 == 0 or y % 8 == 0) else b
            img.putpixel((x, y), shade(c, 1 + rnd.uniform(-0.08, 0.08)))
    return img


def boss_texture():
    """Textura 64x32 no layout do modelo de esqueleto (UVs do modelo bípede)."""
    rnd = random.Random(99)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    bone, dark = hexc("#3b3346"), hexc("#1c1824")

    def fill(x0, y0, w, h, c, var=0.1):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                img.putpixel((x, y), shade(c, 1 + rnd.uniform(-var, var)))

    fill(0, 0, 32, 16, bone)          # cabeça
    fill(16, 16, 24, 16, bone)        # tronco
    fill(40, 16, 16, 16, bone)        # braços
    fill(0, 16, 16, 16, bone)         # pernas
    # rosto (face frontal da cabeça: u 8..15, v 8..15)
    for x in range(8, 16):
        img.putpixel((x, 15), dark)
    for (x, y) in [(9, 11), (10, 11), (13, 11), (14, 11)]:
        img.putpixel((x, y), hexc("#d48cff"))
    for (x, y) in [(9, 12), (10, 12), (13, 12), (14, 12)]:
        img.putpixel((x, y), hexc("#7b2fbe"))
    for x in (10, 12, 14):
        img.putpixel((x, 14), dark)
    # costelas (frente do tronco: u 20..27, v 20..31)
    for y in range(21, 30, 2):
        for x in range(21, 27):
            img.putpixel((x, y), dark)
    # coroa no topo da cabeça (u 8..15, v 0..7) e bordas
    for x in range(8, 16):
        img.putpixel((x, 0), hexc("#c9a227")); img.putpixel((x, 7), hexc("#c9a227"))
    for x in range(0, 32):
        if x % 2 == 0:
            img.putpixel((x, 8), hexc("#c9a227"))
    return img


def shrine_top():
    img = noise_tex(hexc("#9a9a9a"), 0.07, 41)
    gold, gem = hexc("#e8c547"), hexc("#b366ff")
    for i in range(2, 14):
        img.putpixel((i, 2), gold); img.putpixel((i, 13), gold)
        img.putpixel((2, i), gold); img.putpixel((13, i), gold)
    for (x, y) in [(7, 7), (8, 7), (7, 8), (8, 8)]:
        img.putpixel((x, y), gem)
    for (x, y) in [(6, 7), (9, 8), (7, 6), (8, 9)]:
        img.putpixel((x, y), hexc("#e0b3ff"))
    return img


def shrine_side():
    img = noise_tex(hexc("#8a8a8a"), 0.08, 42)
    gold = hexc("#e8c547")
    for x in range(16):
        img.putpixel((x, 0), hexc("#b0b0b0")); img.putpixel((x, 15), hexc("#5a5a5a"))
        img.putpixel((x, 3), gold)
    # chama de vela estilizada
    for (x, y) in [(7, 6), (8, 6), (7, 7), (8, 7), (7, 8), (8, 8), (7, 9), (8, 9), (7, 10), (8, 10)]:
        img.putpixel((x, y), hexc("#efe2c0"))
    for (x, y) in [(7, 4), (8, 5), (7, 5)]:
        img.putpixel((x, y), hexc("#ffb000"))
    return img


def servant_texture():
    """64x64 no layout do modelo de zumbi/jogador: tons de osso pálido azulado com olhos de alma."""
    rnd = random.Random(77)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    base, dark, cloth = hexc("#7d8a99"), hexc("#3a4250"), hexc("#2c2440")

    def fill(x0, y0, w, h, c, var=0.1):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                img.putpixel((x, y), shade(c, 1 + rnd.uniform(-var, var)))

    fill(0, 0, 32, 16, base)          # cabeça
    fill(16, 16, 24, 16, cloth)       # tronco (manto)
    fill(40, 16, 16, 16, base)        # braço direito
    fill(0, 16, 16, 16, cloth)        # perna direita
    fill(16, 48, 16, 16, cloth)       # perna esquerda
    fill(32, 48, 16, 16, base)        # braço esquerdo
    for (x, y) in [(9, 12), (10, 12), (13, 12), (14, 12)]:
        img.putpixel((x, y), hexc("#7ff7ff"))
    for x in range(10, 14):
        img.putpixel((x, 14), dark)
    for y in range(20, 32):
        img.putpixel((23, y), hexc("#8a6bbf")); img.putpixel((24, y), hexc("#8a6bbf"))
    return img


def save(img, *path):
    p = os.path.join(ASSETS, "textures", *path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)


def gen_textures():
    stone, deep, nether = hexc("#7d7d7d"), hexc("#4a4a50"), hexc("#6e2a2a")
    save(ore_tex(stone, 0.12, hexc("#f0fdff"), hexc("#5fa8c0"), 1), "block", "mithril_ore.png")
    save(ore_tex(deep, 0.12, hexc("#f0fdff"), hexc("#5fa8c0"), 2), "block", "deepslate_mithril_ore.png")
    save(ore_tex(deep, 0.14, hexc("#ff8a96"), hexc("#a01d33"), 3, clusters=4), "block", "adamantite_ore.png")
    save(ore_tex(nether, 0.18, hexc("#b8fff4"), hexc("#2fb8a8"), 4), "block", "soulstone_ore.png")
    save(block_tex(hexc("#f0fdff"), hexc("#8fd3e8"), hexc("#3f7f94"), 5), "block", "mithril_block.png")
    save(block_tex(hexc("#ff8a96"), hexc("#c0283e"), hexc("#5a0f1d"), 6), "block", "adamantite_block.png")
    save(quest_board_front(), "block", "quest_board_front.png")
    save(planks_tex(12), "block", "quest_board_side.png")
    save(planks_tex(13, "#7a4f26"), "block", "quest_board_top.png")
    save(altar_top(), "block", "boss_altar_top.png")
    save(altar_side(), "block", "boss_altar_side.png")
    save(noise_tex(hexc("#1f1f25"), 0.08, 23), "block", "boss_altar_bottom.png")

    for mat, colors in MATERIALS.items():
        p = pal({**colors, **HANDLE})
        save(from_template(SWORD, p), "item", f"{mat}_sword.png")
        save(from_template(PICKAXE, p), "item", f"{mat}_pickaxe.png")
        save(from_template(AXE, p), "item", f"{mat}_axe.png")
        save(from_template(SHOVEL, p), "item", f"{mat}_shovel.png")
        save(from_template(HELMET, p), "item", f"{mat}_helmet.png")
        save(from_template(CHESTPLATE, p), "item", f"{mat}_chestplate.png")
        save(from_template(LEGGINGS, p), "item", f"{mat}_leggings.png")
        save(from_template(BOOTS, p), "item", f"{mat}_boots.png")
        save(from_template(INGOT, p), "item", f"{mat}_ingot.png")
        save(from_template(RAW, p), "item", f"raw_{mat}.png")
    save(from_template(SHARD, pal({"o": "#0b2a2a", "b": "#e6fffb", "m": "#2fb8a8"})), "item", "soul_shard.png")

    legend = {
        "flamebrand": (SWORD, {"o": "#4a1500", "b": "#fff1a8", "m": "#ff6a00", "g": "#ffb000", "h": "#3b1f0f", "j": "#2a160a"}),
        "frostfang": (SWORD, {"o": "#0d2a4a", "b": "#ffffff", "m": "#6ec6ff", "g": "#b0e0ff", "h": "#33506b", "j": "#22384d"}),
        "soulreaper": (SWORD, {"o": "#0b2a2a", "b": "#b8fff4", "m": "#2fb8a8", "g": "#7b2fbe", "h": "#241b2e", "j": "#160f1c"}),
        "stormbreaker": (HAMMER, {"o": "#1b1b3a", "b": "#fff59d", "m": "#7f7fff", "h": "#5b4a3a", "j": "#3a2e24"}),
        "arcane_staff": (STAFF, {"o": "#2a0b3a", "b": "#e0b3ff", "g": "#9b4dff", "h": "#5b3a1e"}),
        "catacomb_key": (KEY, {"o": "#2a1f0a", "b": "#fff1a8", "m": "#c9a227"}),
        "rebirth_tome": (BOOK, {"o": "#1a0f24", "b": "#7b2fbe", "m": "#4b1c75", "g": "#ffd27f", "p": "#efe2c0"}),
    }
    for name, (tpl, colors) in legend.items():
        save(from_template(tpl, pal(colors)), "item", f"{name}.png")

    save(armor_layer("#8fd3e8", "#5fa8c0", 31), "models", "armor", "mithril_layer_1.png")
    save(armor_layer("#7cc3d8", "#4f98b0", 32), "models", "armor", "mithril_layer_2.png")
    save(armor_layer("#c0283e", "#8e1a2c", 33), "models", "armor", "adamantite_layer_1.png")
    save(armor_layer("#a82236", "#701526", 34), "models", "armor", "adamantite_layer_2.png")
    save(boss_texture(), "entity", "catacomb_lord.png")
    save(servant_texture(), "entity", "servant.png")
    save(shrine_top(), "block", "shrine_top.png")
    save(shrine_side(), "block", "shrine_side.png")
    save(noise_tex(hexc("#6a6a6a"), 0.07, 43), "block", "shrine_bottom.png")

    icon = from_template(SWORD, pal(legend["soulreaper"][1])).resize((128, 128), Image.NEAREST)
    bg = Image.new("RGBA", (128, 128), hexc("#1c1824"))
    bg.alpha_composite(icon)
    os.makedirs(ASSETS, exist_ok=True)
    bg.save(os.path.join(ASSETS, "icon.png"))


# ----------------------------------------------------------------------------- modelos / blockstates

SIMPLE_BLOCKS = ["mithril_ore", "deepslate_mithril_ore", "adamantite_ore", "soulstone_ore", "mithril_block", "adamantite_block"]
GENERATED_ITEMS = ["raw_mithril", "mithril_ingot", "raw_adamantite", "adamantite_ingot", "soul_shard", "catacomb_key", "rebirth_tome",
                   "mithril_helmet", "mithril_chestplate", "mithril_leggings", "mithril_boots",
                   "adamantite_helmet", "adamantite_chestplate", "adamantite_leggings", "adamantite_boots"]
HANDHELD_ITEMS = ["mithril_sword", "mithril_pickaxe", "mithril_axe", "mithril_shovel",
                  "adamantite_sword", "adamantite_pickaxe", "adamantite_axe", "adamantite_shovel",
                  "flamebrand", "frostfang", "soulreaper", "stormbreaker", "arcane_staff"]


def gen_models():
    bs = os.path.join(ASSETS, "blockstates")
    bm = os.path.join(ASSETS, "models", "block")
    im = os.path.join(ASSETS, "models", "item")
    for b in SIMPLE_BLOCKS:
        write_json(os.path.join(bs, b + ".json"), {"variants": {"": {"model": f"{MOD}:block/{b}"}}})
        write_json(os.path.join(bm, b + ".json"), {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD}:block/{b}"}})
        write_json(os.path.join(im, b + ".json"), {"parent": f"{MOD}:block/{b}"})
    # Quadro de missões (orientável)
    write_json(os.path.join(bm, "quest_board.json"), {"parent": "minecraft:block/orientable", "textures": {
        "front": f"{MOD}:block/quest_board_front", "side": f"{MOD}:block/quest_board_side", "top": f"{MOD}:block/quest_board_top"}})
    write_json(os.path.join(bs, "quest_board.json"), {"variants": {
        "facing=north": {"model": f"{MOD}:block/quest_board"},
        "facing=east": {"model": f"{MOD}:block/quest_board", "y": 90},
        "facing=south": {"model": f"{MOD}:block/quest_board", "y": 180},
        "facing=west": {"model": f"{MOD}:block/quest_board", "y": 270}}})
    write_json(os.path.join(im, "quest_board.json"), {"parent": f"{MOD}:block/quest_board"})
    # Altar
    write_json(os.path.join(bm, "boss_altar.json"), {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "top": f"{MOD}:block/boss_altar_top", "side": f"{MOD}:block/boss_altar_side", "bottom": f"{MOD}:block/boss_altar_bottom"}})
    write_json(os.path.join(bs, "boss_altar.json"), {"variants": {"": {"model": f"{MOD}:block/boss_altar"}}})
    write_json(os.path.join(im, "boss_altar.json"), {"parent": f"{MOD}:block/boss_altar"})
    write_json(os.path.join(bm, "shrine.json"), {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "top": f"{MOD}:block/shrine_top", "side": f"{MOD}:block/shrine_side", "bottom": f"{MOD}:block/shrine_bottom"}})
    write_json(os.path.join(bs, "shrine.json"), {"variants": {"": {"model": f"{MOD}:block/shrine"}}})
    write_json(os.path.join(im, "shrine.json"), {"parent": f"{MOD}:block/shrine"})

    for i in GENERATED_ITEMS:
        write_json(os.path.join(im, i + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{i}"}})
    for i in HANDHELD_ITEMS:
        write_json(os.path.join(im, i + ".json"), {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{MOD}:item/{i}"}})
    write_json(os.path.join(im, "catacomb_lord_spawn_egg.json"), {"parent": "minecraft:item/template_spawn_egg"})


# ----------------------------------------------------------------------------- receitas

def item(i):
    return {"item": i if ":" in i else f"{MOD}:{i}"}


def shaped(name, pattern, key, result, count=1):
    write_json(os.path.join(DATA, "recipe", name + ".json"), {
        "type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern,
        "key": {k: item(v) for k, v in key.items()},
        "result": {"id": result if ":" in result else f"{MOD}:{result}", "count": count}})


def shapeless(name, ingredients, result, count=1):
    write_json(os.path.join(DATA, "recipe", name + ".json"), {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [item(i) for i in ingredients],
        "result": {"id": result if ":" in result else f"{MOD}:{result}", "count": count}})


def cooking(name, kind, ingredient, result, xp, time):
    write_json(os.path.join(DATA, "recipe", name + ".json"), {
        "type": f"minecraft:{kind}", "category": "misc", "ingredient": item(ingredient),
        "result": {"id": f"{MOD}:{result}"}, "experience": xp, "cookingtime": time})


def gen_recipes():
    for mat in ["mithril", "adamantite"]:
        ingot = f"{mat}_ingot"
        xp = 0.8 if mat == "mithril" else 1.2
        cooking(f"{ingot}_from_smelting_raw", "smelting", f"raw_{mat}", ingot, xp, 200)
        cooking(f"{ingot}_from_blasting_raw", "blasting", f"raw_{mat}", ingot, xp, 100)
        cooking(f"{ingot}_from_smelting_ore", "smelting", f"{mat}_ore", ingot, xp, 200)
        shaped(f"{mat}_block", ["###", "###", "###"], {"#": ingot}, f"{mat}_block")
        shapeless(f"{ingot}_from_block", [f"{mat}_block"], ingot, 9)
        tools = {
            "sword": [" # ", " # ", " | "], "pickaxe": ["###", " | ", " | "],
            "axe": ["## ", "#| ", " | "], "shovel": [" # ", " | ", " | "],
            "helmet": ["###", "# #"], "chestplate": ["# #", "###", "###"],
            "leggings": ["###", "# #", "# #"], "boots": ["# #", "# #"],
        }
        for t, pattern in tools.items():
            key = {"#": ingot}
            if any("|" in r for r in pattern):
                key["|"] = "minecraft:stick"
            shaped(f"{mat}_{t}", pattern, key, f"{mat}_{t}")
    cooking("mithril_ingot_from_smelting_deepslate_ore", "smelting", "deepslate_mithril_ore", "mithril_ingot", 0.8, 200)
    cooking("soul_shard_from_smelting_ore", "smelting", "soulstone_ore", "soul_shard", 1.0, 200)
    shaped("quest_board", ["PPP", "PBP", "PPP"], {"P": "minecraft:oak_planks", "B": "minecraft:book"}, "quest_board")
    shaped("catacomb_key", [" S ", "SAS", " G "], {"S": "soul_shard", "A": "adamantite_ingot", "G": "minecraft:gold_ingot"}, "catacomb_key")
    shaped("shrine", ["GAG", "SCS", "SSS"], {"G": "minecraft:gold_ingot", "A": "minecraft:amethyst_shard",
                                              "C": "minecraft:candle", "S": "minecraft:stone_bricks"}, "shrine")
    shaped("rebirth_tome", [" S ", "SBS", " D "], {"S": "soul_shard", "B": "minecraft:book", "D": "minecraft:diamond"}, "rebirth_tome")
    # Quadro de missões aceita qualquer tábua (tag)
    p = os.path.join(DATA, "recipe", "quest_board.json")
    with open(p) as f:
        r = json.load(f)
    r["key"]["P"] = {"tag": "minecraft:planks"}
    r["category"] = "building"
    write_json(p, r)


# ----------------------------------------------------------------------------- loot tables

def ore_loot(block, drop, lo=1, hi=1):
    entry_drop = {"type": "minecraft:item", "name": f"{MOD}:{drop}", "functions": [
        {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
        {"function": "minecraft:explosion_decay"}]}
    if (lo, hi) != (1, 1):
        entry_drop["functions"].insert(0, {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}, "add": False})
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{
        "type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"{MOD}:{block}", "conditions": [{"condition": "minecraft:match_tool", "predicate": {
                "predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
            entry_drop]}]}]}


def self_loot(block):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "conditions": [{"condition": "minecraft:survives_explosion"}],
                                                  "entries": [{"type": "minecraft:item", "name": f"{MOD}:{block}"}]}]}


def entry(name, weight, lo=1, hi=1, enchant=False):
    e = {"type": "minecraft:item", "name": name if ":" in name else f"{MOD}:{name}", "weight": weight}
    fns = []
    if (lo, hi) != (1, 1):
        fns.append({"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
    if enchant:
        fns.append({"function": "minecraft:enchant_with_levels", "levels": {"type": "minecraft:uniform", "min": 15, "max": 30}})
    if fns:
        e["functions"] = fns
    return e


def gen_loot():
    lt = os.path.join(DATA, "loot_table")
    write_json(os.path.join(lt, "blocks", "mithril_ore.json"), ore_loot("mithril_ore", "raw_mithril"))
    write_json(os.path.join(lt, "blocks", "deepslate_mithril_ore.json"), ore_loot("deepslate_mithril_ore", "raw_mithril"))
    write_json(os.path.join(lt, "blocks", "adamantite_ore.json"), ore_loot("adamantite_ore", "raw_adamantite"))
    write_json(os.path.join(lt, "blocks", "soulstone_ore.json"), ore_loot("soulstone_ore", "soul_shard", 1, 2))
    for b in ["mithril_block", "adamantite_block", "quest_board", "shrine"]:
        write_json(os.path.join(lt, "blocks", b + ".json"), self_loot(b))

    room = {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            entry("minecraft:bone", 20, 2, 6), entry("minecraft:rotten_flesh", 15, 2, 5), entry("minecraft:iron_ingot", 12, 1, 4),
            entry("minecraft:gold_ingot", 8, 1, 3), entry("raw_mithril", 12, 1, 4), entry("mithril_ingot", 6, 1, 2),
            entry("soul_shard", 6, 1, 2), entry("minecraft:diamond", 3, 1, 2), entry("minecraft:golden_apple", 3),
            entry("minecraft:experience_bottle", 6, 2, 5), entry("minecraft:book", 5, 1, 1, enchant=True)]},
        {"rolls": 1, "entries": [
            {"type": "minecraft:empty", "weight": 55},
            entry("mithril_sword", 10, enchant=True), entry("mithril_helmet", 6), entry("mithril_chestplate", 5),
            entry("adamantite_ingot", 8, 1, 2), entry("catacomb_key", 6), entry("flamebrand", 2), entry("frostfang", 2),
            entry("rebirth_tome", 3), entry("arcane_staff", 2)]}]}
    write_json(os.path.join(lt, "chests", "catacomb_room.json"), room)

    vault = {"type": "minecraft:chest", "pools": [
        {"rolls": 1, "entries": [entry("catacomb_key", 1)]},
        {"rolls": 1, "entries": [entry("flamebrand", 3), entry("frostfang", 3), entry("stormbreaker", 2), entry("arcane_staff", 3)]},
        {"rolls": {"type": "minecraft:uniform", "min": 3, "max": 5}, "entries": [
            entry("adamantite_ingot", 10, 1, 3), entry("mithril_ingot", 12, 2, 5), entry("soul_shard", 10, 2, 4),
            entry("minecraft:diamond", 6, 1, 3), entry("minecraft:enchanted_golden_apple", 1), entry("minecraft:golden_apple", 5, 1, 2)]}]}
    write_json(os.path.join(lt, "chests", "catacomb_vault.json"), vault)

    boss = {"type": "minecraft:entity", "pools": [
        {"rolls": 1, "entries": [entry("soulreaper", 1)], "conditions": [{"condition": "minecraft:killed_by_player"}]},
        {"rolls": 1, "entries": [entry("stormbreaker", 1), entry("flamebrand", 1), entry("frostfang", 1), entry("arcane_staff", 1)],
         "conditions": [{"condition": "minecraft:random_chance", "chance": 0.5}]},
        {"rolls": 1, "entries": [entry("adamantite_ingot", 1, 3, 6)]},
        {"rolls": 1, "entries": [entry("soul_shard", 1, 4, 8)]},
        {"rolls": 1, "entries": [entry("minecraft:wither_skeleton_skull", 1)]}]}
    write_json(os.path.join(lt, "entities", "catacomb_lord.json"), boss)


# ----------------------------------------------------------------------------- tags

def tag(path, values):
    write_json(path, {"replace": False, "values": values})


def gen_tags():
    m = lambda *xs: [f"{MOD}:{x}" for x in xs]
    tb = os.path.join(MCDATA, "tags", "block")
    tag(os.path.join(tb, "mineable", "pickaxe.json"), m("mithril_ore", "deepslate_mithril_ore", "adamantite_ore", "soulstone_ore", "mithril_block", "adamantite_block", "shrine"))
    tag(os.path.join(tb, "mineable", "axe.json"), m("quest_board"))
    tag(os.path.join(tb, "needs_iron_tool.json"), m("mithril_ore", "deepslate_mithril_ore", "soulstone_ore", "mithril_block"))
    tag(os.path.join(tb, "needs_diamond_tool.json"), m("adamantite_ore", "adamantite_block"))
    tag(os.path.join(tb, "dragon_immune.json"), m("boss_altar"))
    tag(os.path.join(tb, "wither_immune.json"), m("boss_altar"))

    # Tags de convenção (usadas pela profissão Mineração e por outros mods)
    cdata = os.path.join(ROOT, "data", "c", "tags")
    tag(os.path.join(cdata, "block", "ores.json"), m("mithril_ore", "deepslate_mithril_ore", "adamantite_ore", "soulstone_ore"))
    tag(os.path.join(cdata, "item", "ores.json"), m("mithril_ore", "deepslate_mithril_ore", "adamantite_ore", "soulstone_ore"))
    tag(os.path.join(cdata, "item", "ingots.json"), m("mithril_ingot", "adamantite_ingot"))

    ti = os.path.join(MCDATA, "tags", "item")
    tag(os.path.join(ti, "swords.json"), m("mithril_sword", "adamantite_sword", "flamebrand", "frostfang", "soulreaper", "stormbreaker"))
    tag(os.path.join(ti, "pickaxes.json"), m("mithril_pickaxe", "adamantite_pickaxe"))
    tag(os.path.join(ti, "axes.json"), m("mithril_axe", "adamantite_axe"))
    tag(os.path.join(ti, "shovels.json"), m("mithril_shovel", "adamantite_shovel"))
    tag(os.path.join(ti, "head_armor.json"), m("mithril_helmet", "adamantite_helmet"))
    tag(os.path.join(ti, "chest_armor.json"), m("mithril_chestplate", "adamantite_chestplate"))
    tag(os.path.join(ti, "leg_armor.json"), m("mithril_leggings", "adamantite_leggings"))
    tag(os.path.join(ti, "foot_armor.json"), m("mithril_boots", "adamantite_boots"))
    tag(os.path.join(ti, "trimmable_armor.json"), m("mithril_helmet", "mithril_chestplate", "mithril_leggings", "mithril_boots",
                                                     "adamantite_helmet", "adamantite_chestplate", "adamantite_leggings", "adamantite_boots"))
    tag(os.path.join(ti, "enchantable", "durability.json"), m("arcane_staff"))

    tag(os.path.join(DATA, "tags", "worldgen", "biome", "has_catacomb.json"), [
        "#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_mountain", "#minecraft:is_hill", "#minecraft:is_jungle",
        "#minecraft:is_savanna", "#minecraft:is_badlands", "minecraft:plains", "minecraft:sunflower_plains", "minecraft:snowy_plains",
        "minecraft:desert", "minecraft:swamp", "minecraft:meadow", "minecraft:dripstone_caves", "minecraft:lush_caves"])


# ----------------------------------------------------------------------------- worldgen

def gen_worldgen():
    wg = os.path.join(DATA, "worldgen")
    write_json(os.path.join(wg, "configured_feature", "ore_mithril.json"), {"type": "minecraft:ore", "config": {
        "size": 7, "discard_chance_on_air_exposure": 0.0, "targets": [
            {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"},
             "state": {"Name": f"{MOD}:mithril_ore"}},
            {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"},
             "state": {"Name": f"{MOD}:deepslate_mithril_ore"}}]}})
    write_json(os.path.join(wg, "configured_feature", "ore_adamantite.json"), {"type": "minecraft:ore", "config": {
        "size": 4, "discard_chance_on_air_exposure": 0.5, "targets": [
            {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"},
             "state": {"Name": f"{MOD}:adamantite_ore"}}]}})
    write_json(os.path.join(wg, "configured_feature", "ore_soulstone.json"), {"type": "minecraft:ore", "config": {
        "size": 6, "discard_chance_on_air_exposure": 0.0, "targets": [
            {"target": {"predicate_type": "minecraft:block_match", "block": "minecraft:netherrack"},
             "state": {"Name": f"{MOD}:soulstone_ore"}}]}})

    def placed(name, count, height):
        write_json(os.path.join(wg, "placed_feature", name + ".json"), {"feature": f"{MOD}:{name}", "placement": [
            {"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": height}, {"type": "minecraft:biome"}]})

    placed("ore_mithril", 7, {"type": "minecraft:trapezoid", "min_inclusive": {"absolute": -48}, "max_inclusive": {"absolute": 48}})
    placed("ore_adamantite", 3, {"type": "minecraft:trapezoid", "min_inclusive": {"above_bottom": 0}, "max_inclusive": {"absolute": -20}})
    placed("ore_soulstone", 10, {"type": "minecraft:uniform", "min_inclusive": {"above_bottom": 10}, "max_inclusive": {"below_top": 10}})

    write_json(os.path.join(wg, "structure", "catacomb.json"), {
        "type": f"{MOD}:catacomb", "biomes": f"#{MOD}:has_catacomb", "step": "underground_structures",
        "spawn_overrides": {}, "terrain_adaptation": "none"})
    write_json(os.path.join(wg, "structure_set", "catacombs.json"), {
        "structures": [{"structure": f"{MOD}:catacomb", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 36, "separation": 14, "salt": 845120731}})


if __name__ == "__main__":
    gen_textures()
    gen_models()
    gen_recipes()
    gen_loot()
    gen_tags()
    gen_worldgen()
    print("ok")
