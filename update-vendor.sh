#!/usr/bin/env bash
# update-vendor.sh
#
# Downloads (or refreshes) locally-mirrored vendor assets so the application
# can run on a LAN with no internet access.
#
# Assets downloaded:
#   vendor/tailwind.cdn.js      – Tailwind CSS CDN runtime (used by chair.html)
#   vendor/google-fonts.css     – Google Fonts stylesheet with all URLs rewritten to local
#   vendor/fonts/*.woff2        – Manrope, Inter, Material Symbols Outlined font files
#
# Usage:  ./update-vendor.sh
# Requires: curl, sed, grep, awk (all standard on Linux/macOS/WSL/Git Bash)

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
STATIC_DIR="$REPO_ROOT/src/main/resources/static"
VENDOR_DIR="$STATIC_DIR/vendor"
FONT_DIR="$VENDOR_DIR/fonts"

mkdir -p "$FONT_DIR"

UA="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

# ── 1. Tailwind CDN runtime ────────────────────────────────────────────────
echo "Downloading Tailwind CDN runtime..."
curl -fsSL "https://cdn.tailwindcss.com" -o "$VENDOR_DIR/tailwind.cdn.js"
echo "  OK  tailwind.cdn.js  ($(du -k "$VENDOR_DIR/tailwind.cdn.js" | cut -f1) KB)"

# ── 2. Google Fonts CSS ────────────────────────────────────────────────────
# Single stylesheet covers all pages:
#   chair.html       – Manrope, Inter, Material Symbols Outlined
#   participant.html – Manrope, Inter
echo "Downloading Google Fonts CSS..."
FONT_CSS_URL="https://fonts.googleapis.com/css2?family=Manrope:wght@500;700;800&family=Inter:wght@400;500;600&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap"
curl -fsSL -A "$UA" "$FONT_CSS_URL" -o "$VENDOR_DIR/google-fonts.css.tmp"

# ── 3. Download font files and rewrite URLs ────────────────────────────────
echo "Downloading font files..."
rm -f "$FONT_DIR"/*

# Extract unique gstatic URLs
mapfile -t FONT_URLS < <(grep -oP 'https://fonts\.gstatic\.com/[^)\s]+' "$VENDOR_DIR/google-fonts.css.tmp" | sort -u)

cp "$VENDOR_DIR/google-fonts.css.tmp" "$VENDOR_DIR/google-fonts.css"

declare -A SEEN
for URL in "${FONT_URLS[@]}"; do
    BASENAME=$(basename "$URL")
    # Handle duplicate base names by appending a counter
    if [[ -n "${SEEN[$BASENAME]+_}" ]]; then
        EXT="${BASENAME##*.}"
        STEM="${BASENAME%.*}"
        BASENAME="${STEM}_${SEEN[$BASENAME]}.${EXT}"
    fi
    SEEN[$BASENAME]=$(( ${SEEN[$BASENAME]:-0} + 1 ))

    curl -fsSL "$URL" -o "$FONT_DIR/$BASENAME"
    # Rewrite URL in CSS (escape / and . for sed)
    ESCAPED_URL=$(printf '%s\n' "$URL" | sed 's/[\/\.\&]/\\&/g')
    sed -i "s|$URL|/vendor/fonts/$BASENAME|g" "$VENDOR_DIR/google-fonts.css"
done

rm -f "$VENDOR_DIR/google-fonts.css.tmp"

# Sanity check: no external gstatic URLs should remain
if grep -q "fonts\.gstatic\.com" "$VENDOR_DIR/google-fonts.css"; then
    echo "WARNING: some gstatic URLs were not rewritten – check $VENDOR_DIR/google-fonts.css" >&2
else
    echo "  OK  all font URLs rewritten to local paths"
fi

# ── 4. Summary ─────────────────────────────────────────────────────────────
FONT_COUNT=$(ls "$FONT_DIR" | wc -l | tr -d ' ')
FONT_TOTAL=$(du -sk "$FONT_DIR" | cut -f1)
CSS_SIZE=$(du -k "$VENDOR_DIR/google-fonts.css" | cut -f1)

echo ""
echo "Vendor assets updated:"
echo "  vendor/tailwind.cdn.js    $(du -k "$VENDOR_DIR/tailwind.cdn.js" | cut -f1) KB"
echo "  vendor/google-fonts.css   $CSS_SIZE KB"
echo "  vendor/fonts/             $FONT_COUNT files, $FONT_TOTAL KB total"
