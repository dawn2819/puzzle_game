---
name: Modern Heritage Puzzles
colors:
  surface: '#fff9ea'
  surface-dim: '#dfdacb'
  surface-bright: '#fff9ea'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f9f3e4'
  surface-container: '#f3edde'
  surface-container-high: '#eee8d9'
  surface-container-highest: '#e8e2d3'
  on-surface: '#1d1c13'
  on-surface-variant: '#5d403b'
  inverse-surface: '#333027'
  inverse-on-surface: '#f6f0e1'
  outline: '#916f6a'
  outline-variant: '#e6bdb7'
  surface-tint: '#bf080b'
  primary: '#b40006'
  on-primary: '#ffffff'
  primary-container: '#da251d'
  on-primary-container: '#fff3f1'
  inverse-primary: '#ffb4a9'
  secondary: '#626200'
  on-secondary: '#ffffff'
  secondary-container: '#e7e700'
  on-secondary-container: '#666600'
  tertiary: '#3e6137'
  on-tertiary: '#ffffff'
  tertiary-container: '#557a4e'
  on-tertiary-container: '#e1ffd5'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad5'
  primary-fixed-dim: '#ffb4a9'
  on-primary-fixed: '#410001'
  on-primary-fixed-variant: '#930004'
  secondary-fixed: '#eaea00'
  secondary-fixed-dim: '#cdcd00'
  on-secondary-fixed: '#1d1d00'
  on-secondary-fixed-variant: '#494900'
  tertiary-fixed: '#c4eeb8'
  tertiary-fixed-dim: '#a9d19e'
  on-tertiary-fixed: '#012202'
  on-tertiary-fixed-variant: '#2c4f27'
  background: '#fff9ea'
  on-background: '#1d1c13'
  surface-variant: '#e8e2d3'
typography:
  display-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 48px
    fontWeight: '800'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Be Vietnam Pro
    fontSize: 32px
    fontWeight: '800'
    lineHeight: '1.2'
  headline-md:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '700'
    lineHeight: '1.3'
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  game-logic:
    fontFamily: JetBrains Mono
    fontSize: 16px
    fontWeight: '500'
    lineHeight: '1.0'
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  gutter: 16px
  margin-mobile: 20px
  margin-desktop: 40px
  container-max-width: 1200px
---

## Brand & Style
This design system celebrates Vietnamese cultural identity through a high-fidelity, contemporary lens. The brand personality is vibrant, intellectual, and festive, targeting a global audience seeking both mental stimulation and cultural immersion.

The design style is a hybrid of **Modern-Tactile** and **Graphic Minimalism**. It uses clean, geometric layouts to ensure game logic is clear, while layering "Modern Vietnam" motifs—specifically the geometry of the *nón lá* (conical hat) and the *sao vàng* (gold star)—as functional UI elements rather than mere decoration. The aesthetic balances the energy of a bustling Hanoi street with the serene, structured beauty of traditional architecture.

Key principles:
- **Cultural Intent:** Every icon and pattern is derived from Vietnamese iconography but rendered with modern gradients and precise geometry.
- **Vibrancy:** Use of high-saturation primary colors grounded by earthy, organic secondary tones.
- **Tactile Feedback:** Buttons and interactive elements should feel physical, using subtle inner shadows and "squishy" animations to mimic real puzzle pieces.

## Colors
The palette is rooted in the national colors of Vietnam, elevated for high-fidelity digital interfaces.

- **Primary (Flag Red):** Used for critical actions, branding, and hero elements. It signifies energy and luck.
- **Secondary (Star Gold):** Reserved for achievements, highlights, and primary icons.
- **Bamboo & Banana Leaf Greens:** Used for success states, environment-related puzzles, and secondary buttons to provide visual relief from the intense red.
- **Earthy Brown:** Provides grounding for borders, shadows, and text to avoid the harshness of pure black.
- **Backgrounds:** 
  - **Light Mode:** Uses a warm parchment/cream (`#F5EFE0`) to reduce eye strain and evoke traditional paper.
  - **Dark Mode:** Employs a deep charcoal with subtle red-tinted shadows to maintain the brand's warmth in low light.

## Typography
The system uses **Be Vietnam Pro** for its linguistic optimization and friendly, modern character. Its geometric construction complements the "Modern Vietnam" aesthetic perfectly.

- **Headlines:** Use Extra Bold (800) weights to command attention. Title case is preferred for a professional look.
- **Game Logic:** **JetBrains Mono** is introduced for numerical data, puzzle coordinates, and timers. Its monospaced nature ensures that shifting numbers do not cause layout jitters during gameplay.
- **Accessibility:** Maintain a minimum contrast ratio of 4.5:1 for all body text against the parchment background.

## Layout & Spacing
The layout follows a **Fluid Grid** model based on an 8px base unit. 

- **Grid:** Use a 12-column grid for desktop and a 4-column grid for mobile.
- **Game Board:** Centralized with dynamic padding that ensures the board is always the focal point.
- **Margins:** Generous outer margins (20px on mobile) prevent the vibrant colors from feeling overwhelming or "cramped."
- **Patterns:** The background should feature a subtle, low-opacity "Banana Leaf" (*cây chuối*) pattern. In light mode, this is a slightly darker cream; in dark mode, it is a faint deep-green tint.

## Elevation & Depth
This system avoids flat design in favor of **Tonal Layers** and **Tactile Depth**.

- **Surfaces:** Use high-quality parchment textures for game containers.
- **Shadows:** Instead of neutral grays, shadows are tinted with **Earthy Brown** or **Deep Red** to maintain color harmony. 
- **The "Nón Lá" Effect:** Floating action buttons (FABs) use a subtle conical gradient to mimic the shape of a traditional hat, giving them a distinct 3D presence without being overtly skeuomorphic.
- **Active States:** Interactive elements should "press" into the surface (inset shadow) when clicked, reinforcing the puzzle-piece metaphor.

## Shapes
The shape language is primarily **Rounded**, reflecting the approachable nature of a casual game collection.

- **Component Corners:** Standard buttons and cards use a 0.5rem (8px) radius.
- **Specialty Shapes:** The *nón lá* geometry is used for category icons and top-level navigation highlights—specifically an equilateral triangle with slightly softened points.
- **The Star:** The five-pointed star (*sao vàng*) is used strictly for progression, achievement badges, and rating systems.

## Components
Consistent application of the "Modern Vietnam" theme across key components:

- **Buttons:** Primary buttons are **Flag Red** with a slight bottom-edge "lip" (2px) in a darker shade to provide a tactile, pressable look. Text is white, bold, and centered.
- **Chips / Tags:** Use the **Banana Leaf Green** for difficulty levels (Easy/Medium) and **Earth Brown** for game categories.
- **Cards:** Puzzle selection cards feature a subtle gradient border transition from Red to Gold. The background is a clean white/parchment.
- **Input Fields:** Thick, Earthy Brown borders (2px) that turn Flag Red on focus. Labels use the Game Logic font for a technical feel.
- **Icons:** All icons are custom-stroked (2px) with slightly rounded terminals. Achievement icons always feature the **Star Gold** as a fill color.
- **Progress Bars:** Represented as a growing bamboo stalk, where segments fill with green as the user nears puzzle completion.