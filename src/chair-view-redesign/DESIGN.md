# Design System Specification: The Orchestrator

## 1. Overview & Creative North Star
This design system is built to transform the functional "Chair View" of a meeting manager into a high-end, editorial-inspired command center. Our Creative North Star is **"The Orchestrator."** 

Unlike standard dashboards that rely on rigid grids and clinical borders, "The Orchestrator" uses intentional asymmetry, deep tonal layering, and high-contrast typography to create an environment of calm authority. We move beyond the "template" look by treating the interface as a physical workspace of stacked, frosted surfaces where the most critical meeting data feels "curated" rather than just displayed.

---

## 2. Colors & Surface Philosophy
The palette is rooted in a deep, midnight foundation (`background: #0b1326`) to reduce eye strain during long sessions, contrasted with vibrant, glowing accents that guide the Chair’s focus.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to section off primary areas of the UI. Structure must be defined solely through background shifts. 
- Use `surface_container_low` for the main canvas.
- Use `surface_container` or `surface_container_high` to define active zones.
- Use `surface_bright` only for floating utility panels.

### Surface Hierarchy & Nesting
Depth is achieved through "Tonal Stacking." Imagine the UI as sheets of fine paper or glass:
- **Level 0 (Base):** `surface` (`#0b1326`)
- **Level 1 (Sectioning):** `surface_container` (`#171f33`)
- **Level 2 (Active Cards):** `surface_container_high` (`#222a3d`)
- **Level 3 (Pop-overs/Modals):** `surface_container_highest` (`#2d3449`)

### The "Glass & Gradient" Rule
To elevate CTAs, use subtle linear gradients (e.g., `primary_container` to `primary`) instead of flat hex codes. For floating elements like "Meeting Controls," apply **Glassmorphism**: use `surface_variant` at 60% opacity with a `24px` backdrop-blur to allow the content beneath to softly bleed through.

---

## 3. Typography: Editorial Authority
We use a dual-font strategy to balance legibility with a high-end aesthetic.

*   **Display & Headlines (Manrope):** Chosen for its modern, geometric structure. Use `display-sm` for meeting titles to establish a strong visual anchor.
*   **Body & Labels (Inter):** The workhorse font. Use `body-md` for all data entries and `label-sm` (all caps, 0.05em letter spacing) for meta-information like "Speaker Time Remaining."

**Hierarchy Tip:** Contrast `headline-sm` in `on_surface` with `label-md` in `on_surface_variant` to create immediate visual "scannability" without needing icons for everything.

---

## 4. Elevation & Depth
Traditional drop shadows are too "heavy" for this system. We use ambient light and tonal layering.

*   **The Layering Principle:** Place a `surface_container_highest` card on top of a `surface_container_low` background. The slight shift in luminosity creates a natural lift.
*   **Ambient Shadows:** For floating elements (like the "Open Popout" button), use an extra-diffused shadow: `box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4)`. The shadow must never be pure black; it should be a deep tint of the `background`.
*   **The "Ghost Border" Fallback:** If high-contrast accessibility is required, use a "Ghost Border": the `outline_variant` token at **15% opacity**. Never use a 100% opaque border.

---

## 5. Components

### Buttons & Controls
- **Primary Action (e.g., "Next Speaker"):** Use `primary_container` (`#3d5afe`) with `on_primary_container` text. Apply `DEFAULT` (8px) roundedness. Use a subtle inner-glow (1px, top-only) to give it a tactile, premium feel.
- **Semantic Start:** `tertiary` (`#00e475`) for "Start Meeting." This color represents momentum.
- **Semantic Stop/Destroy:** `error` (`#ffb4ab`) with a `error_container` background for high-stakes actions.

### Cards & Lists
- **Forbid dividers.** Use `spacing-6` (1.5rem) of vertical white space to separate speakers or agenda items.
- Use `surface_container_low` for the card body and `surface_container_highest` for the active "Current Speaker" card to denote status.

### Status Indicators
- Use the `tertiary_fixed` (`#62ff96`) token for "Connected" pulses.
- Indicators should be small, 8px circular "pips" with a 4px soft glow (outer glow) in the same color.

### Input Fields
- Avoid boxes. Use a bottom-weighted `surface_variant` background with a `2px` focus underline in `primary`. This maintains the editorial feel while providing clear interaction states.

---

## 6. Do’s and Don’ts

### Do:
- **Use Asymmetry:** Place the "Meeting Time" in a larger, high-contrast `display-md` font while keeping auxiliary controls smaller and tucked to the right.
- **Embrace Breathing Room:** Use the `spacing-8` (2rem) scale between major layout blocks. 
- **Use Tinted Layers:** Ensure all "neutral" surfaces have a slight blue/indigo undertone to stay within the cool, professional spectrum of the system.

### Don’t:
- **Don’t use 100% White:** All "on-surface" text should be `on_surface` (`#dae2fd`) to prevent pixel-bleeding on dark backgrounds.
- **Don’t use "Default" Shadows:** Avoid CSS `box-shadow` defaults. Always use large blurs and low opacities.
- **Don’t use Grid Lines:** Never use horizontal or vertical lines to separate data. Let the typography and color shifts do the work.