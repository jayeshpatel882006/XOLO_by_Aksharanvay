#!/usr/bin/env python3
"""Generate XOLO's original, tiny sound effects.

No samples, no external audio libraries: every sound is synthesised from
sine/triangle partials with a short envelope, then written as mono 16-bit WAV.
"""
import math
import os
import struct
import wave

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "sounds")
os.makedirs(OUT, exist_ok=True)

RATE = 22050


def env(t, duration, attack=0.006, release=0.06):
    if t < attack:
        return t / attack
    if t > duration - release:
        return max(0.0, (duration - t) / release)
    return 1.0


def tone(t, freq, harmonics=(1.0, 0.28, 0.12)):
    value = 0.0
    for i, amp in enumerate(harmonics, start=1):
        value += amp * math.sin(2 * math.pi * freq * i * t)
    return value / sum(harmonics)


def write(name, duration, fn):
    frames = bytearray()
    count = int(RATE * duration)
    for i in range(count):
        t = i / RATE
        sample = max(-1.0, min(1.0, fn(t, duration)))
        frames += struct.pack("<h", int(sample * 32767 * 0.72))
    path = os.path.join(OUT, name)
    with wave.open(path, "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(RATE)
        f.writeframes(bytes(frames))
    print(f"{name}: {os.path.getsize(path)} bytes")


def tap(t, d):
    freq = 1500 - 500 * (t / d)
    return tone(t, freq, (1.0, 0.15)) * env(t, d, 0.001, 0.035) * 0.55


def place_x(t, d):
    p = t / d
    freq = 440 + 260 * p
    return tone(t, freq, (1.0, 0.32, 0.10)) * env(t, d, 0.002, 0.09) * 0.8


def place_o(t, d):
    p = t / d
    freq = 330 - 70 * p
    return tone(t, freq, (1.0, 0.22, 0.06)) * env(t, d, 0.004, 0.11) * 0.75


def win(t, d):
    notes = [523.25, 659.25, 783.99, 1046.5]
    idx = min(len(notes) - 1, int(t / (d / len(notes))))
    local = t - idx * (d / len(notes))
    freq = notes[idx]
    return tone(t, freq, (1.0, 0.24, 0.08)) * env(local, d / len(notes), 0.004, 0.10) * 0.7


def draw(t, d):
    p = t / d
    freq = 392 if p < 0.5 else 293.66
    return tone(t, freq, (1.0, 0.18)) * env(t, d, 0.006, 0.16) * 0.55


def start(t, d):
    p = t / d
    freq = 220 + 440 * p * p
    shimmer = 0.18 * math.sin(2 * math.pi * (freq * 2.01) * t)
    return (tone(t, freq, (1.0, 0.2)) + shimmer) * env(t, d, 0.01, 0.18) * 0.55


write("tap.wav", 0.055, tap)
write("place_x.wav", 0.18, place_x)
write("place_o.wav", 0.20, place_o)
write("win.wav", 0.62, win)
write("draw.wav", 0.46, draw)
write("start.wav", 0.38, start)
