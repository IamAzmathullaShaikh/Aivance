import sys, re

xml = sys.stdin.read()
# Find all nodes with text or content-desc, capture bounds
nodes = re.findall(
    r'<node[^>]*?text="([^"]*)"[^>]*?content-desc="([^"]*)"[^>]*?'
    r'(?:clickable="(true|false)")?[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"',
    xml,
)
seen = set()
for text, desc, clickable, x1, y1, x2, y2 in nodes:
    label = text.strip() or desc.strip()
    if not label:
        continue
    cx, cy = (int(x1) + int(x2)) // 2, (int(y1) + int(y2)) // 2
    key = (label, cx, cy)
    if key in seen:
        continue
    seen.add(key)
    print(f"{clickable or '-':5s} center=({cx},{cy}) bounds=[{x1},{y1}][{x2},{y2}]  {label[:80]}")
