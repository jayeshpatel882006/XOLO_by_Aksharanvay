#!/usr/bin/env python3
"""Generate the legacy PNG launcher icons for XOLO.

The adaptive icon (API 26+) is a vector. These PNGs cover API 24-25 and
launchers that still prefer bitmaps. The artwork is a placeholder and can be
replaced without touching app code.
"""
import math
import os
import struct
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")

PAPER = (245, 239, 228, 255)
INK = (25, 24, 39, 255)
SOLAR = (255, 107, 74, 255)
CLEAR = (0, 0, 0, 0)

SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def blend(dst, src):
    a = src[3] / 255.0
    return (
        int(src[0] * a + dst[0] * (1 - a)),
        int(src[1] * a + dst[1] * (1 - a)),
        int(src[2] * a + dst[2] * (1 - a)),
        255,
    )


def draw_icon(size, round_icon):
    ss = 4
    n = size * ss
    px = [[(0, 0, 0, 0) for _ in range(n)] for _ in range(n)]

    cx = cy = n / 2.0
    radius = n * 0.47

    def inside_round_rect(x, y):
        r = n * 0.22
        if x < r and y < r:
            return (x - r) ** 2 + (y - r) ** 2 <= r * r
        if x > n - r and y < r:
            return (x - (n - r)) ** 2 + (y - r) ** 2 <= r * r
        if x < r and y > n - r:
            return (x - r) ** 2 + (y - (n - r)) ** 2 <= r * r
        if x > n - r and y > n - r:
            return (x - (n - r)) ** 2 + (y - (n - r)) ** 2 <= r * r
        return 0 <= x < n and 0 <= y < n

    def inside_circle(x, y, r):
        return (x - cx) ** 2 + (y - cy) ** 2 <= r * r

    for y in range(n):
        for x in range(n):
            xf, yf = x + 0.5, y + 0.5
            if round_icon:
                if inside_circle(xf, yf, radius):
                    px[y][x] = INK
            else:
                if inside_round_rect(xf, yf):
                    px[y][x] = INK

    # Cream badge.
    badge_r = n * 0.335
    for y in range(n):
        for x in range(n):
            if inside_circle(x + 0.5, y + 0.5, badge_r):
                px[y][x] = PAPER

    # X (two rounded strokes), left of centre.
    def stroke(x1, y1, x2, y2, width, color):
        half = width / 2.0
        minx = max(0, int(min(x1, x2) - half - 2))
        maxx = min(n - 1, int(max(x1, x2) + half + 2))
        miny = max(0, int(min(y1, y2) - half - 2))
        maxy = min(n - 1, int(max(y1, y2) + half + 2))
        dx, dy = x2 - x1, y2 - y1
        length2 = dx * dx + dy * dy
        for yy in range(miny, maxy + 1):
            for xx in range(minx, maxx + 1):
                t = ((xx + 0.5 - x1) * dx + (yy + 0.5 - y1) * dy) / length2
                t = max(0.0, min(1.0, t))
                px0 = x1 + t * dx
                py0 = y1 + t * dy
                if (xx + 0.5 - px0) ** 2 + (yy + 0.5 - py0) ** 2 <= half * half:
                    px[yy][xx] = color

    x_cx, x_cy = n * 0.44, n * 0.50
    x_r = n * 0.145
    stroke(x_cx - x_r, x_cy - x_r, x_cx + x_r, x_cy + x_r, n * 0.062, INK)
    stroke(x_cx + x_r, x_cy - x_r, x_cx - x_r, x_cy + x_r, n * 0.062, INK)

    # O ring, right of centre.
    o_cx, o_cy = n * 0.635, n * 0.50
    o_r = n * 0.125
    o_w = n * 0.055
    for y in range(n):
        for x in range(n):
            d = math.hypot(x + 0.5 - o_cx, y + 0.5 - o_cy)
            if o_r - o_w / 2 <= d <= o_r + o_w / 2:
                px[y][x] = SOLAR

    # Downsample.
    out = []
    for y in range(size):
        row = []
        for x in range(size):
            r = g = b = a = 0
            for sy in range(ss):
                for sx in range(ss):
                    p = px[y * ss + sy][x * ss + sx]
                    r += p[0]
                    g += p[1]
                    b += p[2]
                    a += p[3]
            count = ss * ss
            row.append((r // count, g // count, b // count, a // count))
        out.append(row)
    return out


def write_png(path, rows):
    height = len(rows)
    width = len(rows[0])
    raw = bytearray()
    for row in rows:
        raw.append(0)
        for r, g, b, a in row:
            raw.extend((r, g, b, a))
    def chunk(tag, data):
        return (
            struct.pack(">I", len(data))
            + tag
            + data
            + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
        )
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(png)


for density, size in SIZES.items():
    folder = os.path.join(RES, "mipmap-" + density)
    os.makedirs(folder, exist_ok=True)
    write_png(os.path.join(folder, "ic_launcher.png"), draw_icon(size, False))
    write_png(os.path.join(folder, "ic_launcher_round.png"), draw_icon(size, True))
    print("wrote", folder)
