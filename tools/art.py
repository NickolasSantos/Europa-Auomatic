#!/usr/bin/env python3
"""Gerador de pixel art do Europa RPG.

Formas são desenhadas como máscaras geométricas; o sombreamento (bisel com luz no canto superior
esquerdo), o contorno e os brilhos são aplicados automaticamente, no estilo das texturas do Minecraft.
Tudo é procedural e original — nenhuma textura da Mojang é usada.
"""
import math
import random

from PIL import Image


# ----------------------------------------------------------------------------- cores

def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3],)


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def ramp(base):
    """Rampa de 5 tons a partir de uma cor base: contorno, escuro, médio, claro, brilho."""
    b = hexc(base) if isinstance(base, str) else base
    return {
        "o": mix(shade(b, 0.28), (20, 16, 30, 255), 0.35),
        "d": shade(b, 0.62),
        "m": b,
        "l": mix(b, (255, 255, 255, 255), 0.35),
        "h": mix(b, (255, 255, 255, 255), 0.72),
    }


WOOD = ramp("#7a4f2a")
GOLD = ramp("#d9a62a")
IRON = ramp("#a7adb5")


# ----------------------------------------------------------------------------- máscaras

class Mask:
    def __init__(self, w=16, h=16):
        self.w, self.h = w, h
        self.cells = {}  # (x,y) -> rampa

    def put(self, x, y, r):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.cells[(x, y)] = r

    def segment(self, x0, y0, x1, y1, thick, r):
        steps = int(max(abs(x1 - x0), abs(y1 - y0)) * 3) + 1
        for i in range(steps + 1):
            t = i / steps
            cx, cy = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
            for dx in range(-2, 3):
                for dy in range(-2, 3):
                    px, py = round(cx + dx * 0.5), round(cy + dy * 0.5)
                    if math.hypot(px - cx, py - cy) <= thick / 2:
                        self.put(px, py, r)

    def poly(self, pts, r):
        xs, ys = [p[0] for p in pts], [p[1] for p in pts]
        for y in range(int(min(ys)), int(max(ys)) + 1):
            for x in range(int(min(xs)), int(max(xs)) + 1):
                if _inside(x + 0.5, y + 0.5, pts):
                    self.put(x, y, r)

    def ellipse(self, cx, cy, rx, ry, r):
        for y in range(int(cy - ry) - 1, int(cy + ry) + 2):
            for x in range(int(cx - rx) - 1, int(cx + rx) + 2):
                if ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2 <= 1:
                    self.put(x, y, r)

    def rows(self, rows, r, ox=0, oy=0):
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch != ".":
                    self.put(ox + x, oy + y, r)

    def render(self, outline=True, img=None):
        img = img or Image.new("RGBA", (self.w, self.h), (0, 0, 0, 0))
        filled = self.cells
        for (x, y), r in filled.items():
            up, left = (x, y - 1) in filled, (x - 1, y) in filled
            down, right = (x, y + 1) in filled, (x + 1, y) in filled
            if not up or not left:
                tone = "l"
            elif not down or not right:
                tone = "d"
            else:
                tone = "m"
            if not up and not left:
                tone = "h"
            img.putpixel((x, y), r[tone])
        if outline:
            for (x, y), r in list(filled.items()):
                for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
                    if (nx, ny) not in filled and 0 <= nx < self.w and 0 <= ny < self.h and img.getpixel((nx, ny))[3] == 0:
                        img.putpixel((nx, ny), r["o"])
        return img


def _inside(x, y, pts):
    inside = False
    j = len(pts) - 1
    for i in range(len(pts)):
        xi, yi = pts[i]
        xj, yj = pts[j]
        if (yi > y) != (yj > y) and x < (xj - xi) * (y - yi) / (yj - yi + 1e-9) + xi:
            inside = not inside
        j = i
    return inside


# ----------------------------------------------------------------------------- ferramentas e armas

def sword(metal, guard=None, grip=None, long=False):
    m = Mask()
    tip = (14.2, 1.8)
    m.segment(4.5, 11.5, *tip, 2.6 if long else 2.2, metal)
    m.segment(12.6, 3.0, 13.6, 2.0, 1.2, metal)
    m.segment(2.3, 9.0, 7.0, 13.7, 1.6, guard or GOLD)
    m.segment(1.4, 14.6, 4.2, 11.8, 1.4, grip or WOOD)
    m.put(1, 15, guard or GOLD)
    img = m.render()
    # fio do gume (linha clara ao longo da lâmina)
    for t in range(4, 12):
        x, y = 5 + t * 0.8, 10.4 - t * 0.8
        if img.getpixel((int(x), int(y)))[3]:
            img.putpixel((int(x), int(y)), metal["h"])
    return img


def pickaxe(metal):
    m = Mask()
    m.segment(2, 14, 10.5, 5.5, 1.5, WOOD)
    m.segment(3.5, 3.2, 8.0, 1.6, 1.8, metal)
    m.segment(8.0, 1.6, 12.6, 3.3, 1.8, metal)
    m.segment(12.6, 3.3, 14.2, 7.6, 1.8, metal)
    m.segment(3.5, 3.2, 2.2, 5.0, 1.4, metal)
    m.segment(14.2, 7.6, 13.0, 9.2, 1.4, metal)
    return m.render()


def axe(metal):
    m = Mask()
    m.segment(2, 14, 11, 5, 1.5, WOOD)
    m.poly([(7.2, 2.0), (11.0, 1.0), (14.6, 4.4), (13.4, 8.8), (10.6, 6.2), (8.6, 4.8)], metal)
    return m.render()


def shovel(metal):
    m = Mask()
    m.segment(2, 14, 9.5, 6.5, 1.5, WOOD)
    m.ellipse(11.8, 4.2, 2.6, 2.8, metal)
    m.poly([(9.4, 4.6), (12.0, 2.0), (12.4, 6.6)], metal)
    return m.render()


def hammer(metal, accent):
    m = Mask()
    m.segment(1.5, 14.5, 9.5, 6.5, 1.6, WOOD)
    m.poly([(6.5, 3.0), (10.5, 0.8), (15.0, 5.3), (12.8, 9.3)], metal)
    m.segment(9.4, 4.2, 11.6, 6.6, 1.2, accent)
    return m.render()


def staff(gem, wood=None):
    m = Mask()
    m.segment(2, 14.5, 11, 5.5, 1.5, wood or ramp("#5b3a1e"))
    m.segment(10.5, 6.5, 12.5, 4.0, 1.2, GOLD)
    m.segment(10.0, 4.0, 13.0, 7.0, 1.0, GOLD)
    m.ellipse(12.6, 3.4, 2.1, 2.1, gem)
    img = m.render()
    img.putpixel((12, 2), gem["h"])
    return img


# ----------------------------------------------------------------------------- armaduras (ícones)

HELMET = ["................", "................", "....########....", "...##########...", "..############..",
          "..############..", "..####....####..", "..###......###..", "..###......###..", "..##........##..", "................"]
CHEST = ["................", "..####....####..", ".######..######.", ".##############.", ".##############.", "..############..",
         "....########....", "....########....", "....########....", "....########....", "....########....", "....########....",
         "................"]
LEGS = ["................", "...##########...", "...##########...", "...##########...", "...####..####...", "...####..####...",
        "...####..####...", "...####..####...", "...####..####...", "...####..####...", "...####..####...", "................"]
BOOTS = ["................"] * 5 + ["...####..####...", "...####..####...", "...####..####...", "..#####..#####..",
                                   ".######..######.", ".######..######.", "................"]


def armor_icon(rows, metal, trim=None):
    m = Mask()
    m.rows(rows, metal)
    img = m.render()
    if trim:
        for (x, y) in list(m.cells):
            if (x, y - 1) not in m.cells and (x, y) in m.cells and y > 0:
                img.putpixel((x, y), trim["l"])
    return img


# ----------------------------------------------------------------------------- materiais e itens simples

def ingot(metal):
    m = Mask()
    m.poly([(2.5, 9.5), (6.5, 5.0), (14.0, 5.0), (10.0, 9.5)], metal)
    m.poly([(2.5, 9.5), (10.0, 9.5), (10.0, 11.5), (2.5, 11.5)], metal)
    m.poly([(10.0, 9.5), (14.0, 5.0), (14.0, 7.0), (10.0, 11.5)], metal)
    img = m.render()
    for x in range(7, 13):
        img.putpixel((x, 6), metal["h"])
    return img


def raw_chunk(metal, seed):
    rnd = random.Random(seed)
    m = Mask()
    m.ellipse(8, 8.5, 5.2, 4.4, metal)
    m.ellipse(5.5, 6.5, 2.6, 2.2, metal)
    m.ellipse(10.5, 11, 2.4, 2.0, metal)
    img = m.render()
    for _ in range(6):
        x, y = rnd.randint(5, 11), rnd.randint(6, 11)
        if img.getpixel((x, y))[3]:
            img.putpixel((x, y), metal["d"])
    return img


def crystal(c):
    m = Mask()
    m.poly([(8, 1.5), (11.5, 6.5), (8, 14.5), (4.5, 6.5)], c)
    img = m.render()
    for y in range(3, 12):
        img.putpixel((7, y), c["h"] if y < 7 else c["l"])
    return img


def star(c):
    m = Mask()
    pts = []
    for i in range(10):
        a = -math.pi / 2 + i * math.pi / 5
        r = 6.6 if i % 2 == 0 else 2.8
        pts.append((8 + math.cos(a) * r, 8.4 + math.sin(a) * r))
    m.poly(pts, c)
    img = m.render()
    img.putpixel((7, 6), c["h"])
    return img


def prism(c):
    m = Mask()
    m.poly([(8, 1), (14, 8), (8, 15), (2, 8)], c)
    img = m.render()
    for i in range(2, 7):
        img.putpixel((8 - i // 2, 2 + i), c["h"])
    return img


def key():
    m = Mask()
    m.ellipse(4.5, 5.0, 3.0, 3.0, GOLD)
    m.segment(6.5, 7.0, 13.5, 14.0, 1.6, GOLD)
    m.segment(11.0, 11.5, 12.5, 10.0, 1.2, GOLD)
    m.segment(12.5, 13.0, 14.0, 11.5, 1.2, GOLD)
    img = m.render()
    img.putpixel((4, 5), (0, 0, 0, 0))
    img.putpixel((5, 5), (0, 0, 0, 0))
    return img


def book(cover, gem):
    m = Mask()
    m.poly([(3, 2), (13, 2), (13, 14), (3, 14)], cover)
    img = m.render()
    for y in range(3, 14):
        img.putpixel((12, y), hexc("#efe2c0"))
    g = Mask()
    g.ellipse(8, 7.5, 2.2, 2.6, gem)
    g.render(outline=False, img=img)
    return img


# ----------------------------------------------------------------------------- variantes de evolução

def stage_variant(img, stage, aura, rune):
    """stage 1 = Desperto (runas e filetes dourados); stage 2 = Ascendido (brilho e aura)."""
    out = img.copy()
    w, h = out.size
    px = out.load()
    filled = {(x, y) for x in range(w) for y in range(h) if px[x, y][3] > 0}
    if stage >= 1:
        for (x, y) in filled:
            r, g, b, a = px[x, y]
            lum = (r + g + b) / 3
            if 80 < lum < 200 and (x + y) % 4 == 0:
                px[x, y] = rune
    if stage >= 2:
        for (x, y) in filled:
            r, g, b, a = px[x, y]
            px[x, y] = mix((r, g, b, a), aura, 0.18)
        rnd = random.Random(w * 31 + len(filled))
        border = [(x, y) for x in range(w) for y in range(h) if px[x, y][3] == 0 and any(
            (x + dx, y + dy) in filled for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
        for (x, y) in rnd.sample(border, min(len(border), 7)):
            px[x, y] = aura[:3] + (200,)
    return out


# ----------------------------------------------------------------------------- blocos

def stone_base(seed, base="#7f7f7f", var=0.10, deep=False):
    rnd = random.Random(seed)
    b = hexc(base)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            n = math.sin(x * 1.7 + seed) * math.cos(y * 1.3 + seed * 0.7)
            f = 1 + rnd.uniform(-var, var) + n * 0.05
            if deep and y % 4 == 0 and rnd.random() < 0.5:
                f *= 0.85
            img.putpixel((x, y), shade(b, f))
    for _ in range(4):
        x, y = rnd.randint(0, 14), rnd.randint(0, 14)
        img.putpixel((x, y), shade(b, 0.72))
        img.putpixel((x + 1, y), shade(b, 0.8))
    return img


def ore(base_img, metal, seed, clusters=5):
    rnd = random.Random(seed * 13)
    img = base_img.copy()
    spots = [(3, 3), (10, 2), (6, 8), (12, 10), (2, 12), (9, 13)]
    rnd.shuffle(spots)
    for (cx, cy) in spots[:clusters]:
        m = Mask()
        m.ellipse(cx + rnd.uniform(-0.5, 0.5), cy + rnd.uniform(-0.5, 0.5), rnd.uniform(1.2, 1.9), rnd.uniform(1.0, 1.6), metal)
        m.render(img=img)
    return img


def storage_block(metal, seed):
    img = Image.new("RGBA", (16, 16))
    rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            inner = x in (1, 14) or y in (1, 14)
            if edge:
                c = metal["o"] if (x == 15 or y == 15) else metal["d"]
            elif inner:
                c = metal["h"] if (x == 1 or y == 1) else metal["d"]
            else:
                c = shade(metal["m"], 1 + rnd.uniform(-0.04, 0.04))
            img.putpixel((x, y), c)
    for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11)):
        img.putpixel((cx, cy), metal["h"])
        img.putpixel((cx + 1, cy + 1), metal["d"])
    for i in range(5, 11):
        img.putpixel((i, 7), metal["l"])
        img.putpixel((i, 8), metal["d"])
    return img


# ----------------------------------------------------------------------------- texturas de modelo (UV)

def face(img, x0, y0, w, h, r, seed=0, plate=True, rivets=False):
    rnd = random.Random(seed + x0 * 7 + y0 * 13)
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            top, bottom = y == y0, y == y0 + h - 1
            left, right = x == x0, x == x0 + w - 1
            if plate and (bottom or right):
                c = r["d"]
            elif plate and (top or left):
                c = r["l"]
            else:
                c = shade(r["m"], 1 + rnd.uniform(-0.06, 0.06))
            img.putpixel((x, y), c)
    if rivets and w >= 4 and h >= 4:
        for (x, y) in ((x0 + 1, y0 + 1), (x0 + w - 2, y0 + 1)):
            img.putpixel((x, y), r["h"])


def box(img, u, v, w, h, d, r, seed=0, rows=None, **kw):
    """Pinta as 6 faces de um cubo no layout UV padrão do Minecraft. rows=(início, fim) limita as linhas laterais."""
    y_side = v + d
    sides = [(u, y_side, d, h), (u + d, y_side, w, h), (u + d + w, y_side, d, h), (u + d + w + d, y_side, w, h)]
    if rows is None:
        face(img, u + d, v, w, d, r, seed, **kw)              # topo
        face(img, u + d + w, v, w, d, r, seed + 1, **kw)      # base
        for i, (x, y, fw, fh) in enumerate(sides):
            face(img, x, y, fw, fh, r, seed + 2 + i, **kw)
    else:
        a, b = rows
        for i, (x, y, fw, fh) in enumerate(sides):
            face(img, x, y + a, fw, b - a, r, seed + 2 + i, **kw)
        if b == h:
            face(img, u + d + w, v, w, d, r, seed + 9, **kw)


def armor_layers(metal, trim, accent):
    """layer_1: elmo (cabeça), peitoral (tronco e braços), botas (pés); layer_2: calças (cintura e pernas)."""
    l1 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    box(l1, 0, 0, 8, 8, 8, metal, 1, rivets=True)                   # elmo
    # visor do elmo: abertura dos olhos na face frontal (u 8..15, v 8..15)
    for x in range(9, 15):
        l1.putpixel((x, 12), (0, 0, 0, 0))
        l1.putpixel((x, 13), metal["o"])
    for y in range(8, 16):
        l1.putpixel((11, y), trim["l"] if y < 12 else trim["m"])
        l1.putpixel((12, y), trim["m"] if y < 12 else trim["d"])
    box(l1, 16, 16, 8, 12, 4, metal, 2, rivets=True)                # peitoral
    for y in range(21, 31):                                        # sulco central
        l1.putpixel((23, y), metal["d"])
        l1.putpixel((24, y), metal["l"])
    for x in range(20, 28):
        l1.putpixel((x, 20), trim["l"])
        l1.putpixel((x, 26), trim["m"])
    l1.putpixel((23, 23), accent["h"]); l1.putpixel((24, 23), accent["l"])
    l1.putpixel((23, 24), accent["l"]); l1.putpixel((24, 24), accent["d"])
    box(l1, 40, 16, 4, 12, 4, metal, 3, rows=(0, 5), rivets=True)    # ombreiras
    for x in range(40, 56):
        l1.putpixel((x, 24), trim["m"])
    box(l1, 0, 16, 4, 12, 4, metal, 4, rows=(8, 12))                 # botas
    for x in range(0, 16):
        l1.putpixel((x, 28), trim["l"])

    l2 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    box(l2, 16, 16, 8, 12, 4, metal, 5, rows=(7, 12))                # cintura
    for x in range(16, 40):
        l2.putpixel((x, 27), trim["m"])
    l2.putpixel((24, 28), accent["l"])
    box(l2, 0, 16, 4, 12, 4, metal, 6, rows=(0, 9))                  # pernas
    for x in range(0, 16):
        l2.putpixel((x, 25), trim["d"])
    return l1, l2


def skeleton_boss():
    """64x32 no layout do esqueleto: rei-lich com coroa, costelas e manto."""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    bone = ramp("#4a4058")
    robe = ramp("#2a1f3d")
    box(img, 0, 0, 8, 8, 8, bone, 11, plate=False)
    # rosto: órbitas brilhantes, nariz, dentes
    for (x, y) in ((9, 11), (10, 11), (13, 11), (14, 11), (9, 12), (14, 12)):
        img.putpixel((x, y), hexc("#160f20"))
    for (x, y) in ((10, 12), (13, 12)):
        img.putpixel((x, y), hexc("#e3a6ff"))
    img.putpixel((11, 13), hexc("#160f20")); img.putpixel((12, 13), hexc("#160f20"))
    for x in range(9, 15):
        img.putpixel((x, 15), hexc("#d8cfe0") if x % 2 else hexc("#160f20"))
    # coroa na camada externa da cabeça (u 32.., hat): faixa nas laterais
    for i, (x, y, w) in enumerate([(32, 8, 8), (40, 8, 8), (48, 8, 8), (56, 8, 8)]):
        for xx in range(x, x + w):
            img.putpixel((xx, y + 2), GOLD["d"])
            img.putpixel((xx, y + 1), GOLD["m"])
            if (xx - x) % 3 == 1:
                img.putpixel((xx, y), GOLD["l"])
        img.putpixel((x + 4, y + 1), hexc("#b366ff"))
    # tronco: manto com costelas expostas no peito
    box(img, 16, 16, 8, 12, 4, robe, 12)
    for y in range(21, 27, 2):
        for x in range(21, 27):
            img.putpixel((x, y), bone["l"])
    for y in range(20, 32):
        img.putpixel((20, y), GOLD["m"]); img.putpixel((27, y), GOLD["d"])
    # braços e pernas finos (2x12x2)
    box(img, 40, 16, 2, 12, 2, bone, 13, plate=False)
    box(img, 0, 16, 2, 12, 2, robe, 14)
    return img


def servant():
    """64x64 no layout do zumbi: morto-vivo encapuzado com olhos de alma."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    skin = ramp("#7d8b92")
    hood = ramp("#3b2d57")
    cloth = ramp("#2c2440")
    box(img, 0, 0, 8, 8, 8, skin, 21, plate=False)
    for (x, y) in ((9, 12), (10, 12), (13, 12), (14, 12)):
        img.putpixel((x, y), hexc("#7ff7ff"))
    for (x, y) in ((9, 13), (14, 13)):
        img.putpixel((x, y), hexc("#2b8a8a"))
    for x in range(10, 14):
        img.putpixel((x, 14), skin["o"])
    # capuz na camada externa (exceto o rosto)
    box(img, 32, 0, 8, 8, 8, hood, 22)
    for y in range(8 + 3, 16):
        for x in range(41, 47):
            img.putpixel((x, y), (0, 0, 0, 0))
    box(img, 16, 16, 8, 12, 4, cloth, 23)
    for x in range(20, 28):
        img.putpixel((x, 26), GOLD["d"])
    for y in range(20, 26):
        img.putpixel((23, y), hood["l"]); img.putpixel((24, y), hood["m"])
    box(img, 40, 16, 4, 12, 4, cloth, 24)
    box(img, 32, 48, 4, 12, 4, cloth, 25)
    for (u, v) in ((40, 16), (32, 48)):  # mãos pálidas
        for x in range(u, u + 16):
            for y in range(v + 4 + 9, v + 16):
                img.putpixel((x, y), skin["m"])
    box(img, 0, 16, 4, 12, 4, cloth, 26)
    box(img, 16, 48, 4, 12, 4, cloth, 27)
    return img
