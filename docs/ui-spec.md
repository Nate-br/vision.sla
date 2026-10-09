# UI Specification — Vision for a Better Life SLA
## Phase 0 deliverable (plan Section 10)

---

## 1. Design Tokens

### Colour

| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| `primary` | `#1A4B8C` | `#1A4B8C` | Primary actions, key UI elements |
| `onPrimary` | `#FFFFFF` | `#FFFFFF` | Text/icons on primary |
| `primaryContainer` | `#D0E4FF` | `#0F2D5A` | Tinted backgrounds |
| `secondary` | `#C8872A` | `#C8872A` | Accent, gold CTAs |
| `background` | `#F5F8FC` | `#0D1B2A` | Screen background |
| `surface` | `#FFFFFF` | `#152338` | Cards, sheets |
| `onSurface` | `#0D1B2A` | `#CFD8E3` | Body text |
| `success` | `#2E7D32` | `#2E7D32` | Verified, credited |
| `warning` | `#F57C00` | `#F57C00` | Pending, review |
| `error` | `#B3261E` | `#B3261E` | Rejected, errors |

> **Rule:** Semantic colours are NEVER used alone. Always pair with an icon AND text label.
> WCAG AA requires ≥ 4.5:1 contrast ratio for text.

### Typography

| Style | Font | Weight | Size | Line Height |
|-------|------|--------|------|-------------|
| Display Large | Noto Sans Ethiopic | Bold | 57sp | 64sp |
| Headline Large | Noto Sans Ethiopic | SemiBold | 32sp | 40sp |
| Headline Small | Noto Sans Ethiopic | SemiBold | 24sp | 32sp |
| Title Large | Noto Sans | SemiBold | 22sp | 28sp |
| Body Large | Noto Sans | Regular | 16sp | 24sp |
| Body Medium | Noto Sans | Regular | 14sp | 20sp |
| Label Large | Noto Sans | Medium | 14sp | 20sp |

All sizes in `sp` — scales with system font size setting (up to 200%).

### Spacing

4dp base grid. Common values: 4, 8, 12, 16, 24, 32, 48dp.

### Shapes

| Scale | Radius | Used for |
|-------|--------|----------|
| Extra Small | 4dp | Chips, badges |
| Small | 8dp | Input fields |
| Medium | 12dp | Cards |
| Large | 16dp | Bottom sheets, dialogs |
| Extra Large | 24dp | Primary CTA button |

### Elevation

Use `SurfaceTonalElevation` (Material 3) rather than drop shadows on dark theme.

---

## 2. Navigation Structure

```
Bottom Navigation Bar (4 items):
  Home    Pay    History    Profile
   🏠      💳      📋         👤
```

- **Pay** is the primary action — use `FloatingActionButton` or oversized nav item.
- Max 3 taps from any screen to start a payment.
- One primary action per screen.

---

## 3. Component Catalogue
*(Compose `@Preview` gallery — implement in `:core:designsystem`)*

### 3.1 VblButton
```
Variants: Primary | Secondary | Destructive | Ghost
States:   Default | Pressed | Disabled | Loading
Size:     Large (48dp height min), Medium (40dp)
Touch target: always ≥ 48dp
```

### 3.2 VblCard
```
Used for: balance display, payment item, loan summary
Elevation: surface tonal (level 1)
Shape: Medium (12dp)
Padding: 16dp
```

### 3.3 AmountDisplay
```
Format: "ETB 1,500.00" always
Font: Headline Large (Noto Sans Ethiopic for Amharic locale)
Never use raw numbers without currency prefix
```

### 3.4 PaymentStatusChip
```
States: Submitted | Verifying | Credited | Review | Rejected | Expired
Each state: colour background + icon + text label (never colour alone)
  ✓ Credited  → success background + check icon + "Payment confirmed"
  ⏳ Verifying → warning background + clock icon + "Verifying with your bank"
  ✗ Rejected  → error background + X icon + "Could not verify"
```

### 3.5 ProviderSelector
```
Grid of provider logos (with text label for accessibility)
Shows: Telebirr, CBE, CBE Birr, BoA, M-Pesa, Dashen, Awash, Siinqee, Kaafi, Coopay
Zemen: shown but labelled "(manual review)" in muted style
```

### 3.6 ReferenceInputField
```
Smart paste: auto-extracts reference from pasted SMS or receipt link
Auto-removes spaces and hyphens
Shows inline validation as user types (not only on submit)
Correct keyboard: TYPE_CLASS_TEXT, no autocorrect
```

### 3.7 LoadingState / EmptyState / ErrorState / OfflineState
```
Every screen must implement all 4.
ErrorState must show:
  - What happened (plain language)
  - Whether money is safe
  - What to do next (Retry / Contact Support / etc.)
```

### 3.8 AntiScamBanner
```
Shown on the "pay" step displaying org accounts:
"⚠ Pay ONLY to these accounts. We will NEVER ask you to pay to a personal account."
High contrast, can't be dismissed permanently (shown every time)
```

---

## 4. Key Screen Specs

### 4.1 Home Screen
- Savings balance (large, prominent)
- Loan outstanding (if applicable)
- Next due date + amount with countdown colour
- Recent 3 payments (status chips)
- Big **Pay** CTA button
- Last-updated timestamp (cache indicator)

### 4.2 Pay Flow (6 screens, optimise heavily)
1. **Purpose picker** — Contribution / Loan Repayment / Fee (prefilled for instalments)
2. **Amount screen** — fixed or enter amount; clear fee disclosure before confirming
3. **Organisation accounts** — provider cards with large Copy buttons, AntiScamBanner, deep links to bank apps
4. **Provider + reference input** — provider selector → smart reference field or Scan Receipt
5. **Confirmation** — summary: provider, masked reference, amount, purpose. One Confirm button.
6. **Status screen** — live timeline. Works offline from cache. Shows push-notification-aware state.

### 4.3 Status Screen
```
Timeline (vertical stepper):
  ● Submitted          [timestamp]
  ● Verifying…         [animated]
  ○ Credited / Rejected / Review
```
Works offline (cached last known state + "last updated X ago").
Push notification arrives on completion → auto-updates if app is open.

---

## 5. Localisation Test Matrix

For every new screen, test these configurations:
- [ ] English, light theme, default font size
- [ ] Amharic (አማርኛ), dark theme, font size 200%
- [ ] Afaan Oromoo, dark theme, default font size
- [ ] Long strings: check for text overflow, especially amount labels
- [ ] RTL: not required (none of the 3 languages is RTL) but test LTR layout is solid

---

## 6. Accessibility Checklist (per screen)
- [ ] Contrast ≥ 4.5:1 for all text
- [ ] Touch targets ≥ 48dp for all interactive elements
- [ ] All icons have `contentDescription`
- [ ] Logical TalkBack focus order
- [ ] Amount fields announced as "ETB [amount]"
- [ ] Status chips: TalkBack reads both icon and text
- [ ] No information conveyed by colour alone

---

## 7. Motion Guidelines
- Duration: 150–250ms
- Use `AnimatedVisibility`, `AnimatedContent`, `Crossfade`
- Respect `LocalAccessibilityManager.current.isAnimationEnabled` (reduce motion)
- No looping animations unless they can be paused

---

## 8. Usability Testing Plan (Phase 0/1)
Before Phase 1 release:
- [ ] Test with 5 real SLA members (mix of ages, devices, literacy levels)
- [ ] Focus tasks: Sign up, Submit a payment, Check payment status
- [ ] Observe: do they understand the AntiScam warning? do they trust the status screen?
- [ ] Iterate on copy and layout based on feedback

---

*Fonts to download (free, open source):*
- [Noto Sans Ethiopic](https://fonts.google.com/noto/specimen/Noto+Sans+Ethiopic) — Amharic/Ge'ez
- [Noto Sans](https://fonts.google.com/specimen/Noto+Sans) — Latin + Afaan Oromoo
