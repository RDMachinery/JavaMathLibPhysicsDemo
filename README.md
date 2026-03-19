# JavaMathLib Physics Demo

A demonstration of the [`FixedArithmetic`](../javamathlib) and
[`FixedTrigonometry`](../javamathlib) classes applied to six areas of
classical and modern physics. Every calculation is performed using exact
fixed-point integer arithmetic — no floating-point operations, no rounding
errors, no IEEE 754 edge cases. Each section then repeats the same
computation using Java's native `double` type and shows precisely where and
by how much the two approaches diverge.

**Package:** `org.antic.maths`  
**Author:** Mario Gianota  
**Depends on:** `javamathlib.jar` (`org.antic.maths.FixedArithmetic`,
`org.antic.maths.FixedTrigonometry`)  
**Java version:** Java 8 or later

---

## Academic Use — No Licence Required

> **JavaMathLib is free for use by academic institutions, students,
> researchers, and educators. No licence is required and no fee is payable,
> regardless of the scale of use within an academic or educational context.**

This includes use in:

- University and college teaching and coursework
- Academic research and published papers
- Student projects at any level
- Open educational resources and textbooks
- Non-commercial scientific simulations and experiments

The only context in which a licence is required is **commercial use** — where
the library is incorporated into a product or service that generates revenue.
Even then, no fee is payable until annual revenue from that use exceeds
USD $10,000. See `LICENSE` for full terms.

For academic users the library can simply be downloaded, added to the
classpath, and used without any registration, notification, or payment.

---

## Why Exact Arithmetic Matters in Physics

Floating-point arithmetic (`double`, `float`) is fast and convenient, but it
cannot represent most decimal fractions exactly. The value 0.1 in IEEE 754
double precision is actually:

```
0.1000000000000000055511151231257827021181583404541015625
```

For many engineering calculations this imprecision is harmless. For physics
computations that involve:

- **Conservation laws** — where a quantity must be preserved exactly across
  many steps
- **Near-cancellation** — where two large, nearly-equal values are subtracted
  (catastrophic cancellation)
- **Trigonometric identities** — where sin²θ + cos²θ must equal 1 precisely
- **Symmetry conditions** — where two independent calculations of the same
  physical quantity must agree at the bit level

...floating-point drift is not a rounding inconvenience — it is a violation
of the underlying physics. A simulation where energy is not exactly conserved,
or where Newton's Third Law fails at the 15th decimal place, is a simulation
that cannot be trusted for any conclusion that depends on those properties.

`FixedArithmetic` stores every value as a scaled 64-bit integer:

```
realValue = internalRegister / 10^9
```

All arithmetic — addition, subtraction, multiplication, division — is
performed using only integer operations. There is no floating-point unit
involved anywhere in the computation. Equality comparisons are plain `long`
integer comparisons and cannot produce false results regardless of the values.
The `FixedTrigonometry` class provides sin, cos, tan, asin, acos, atan, and
atan2, all computed via Taylor series using only `FixedArithmetic` operations.

---

## The Six Demonstrations

### 1. Projectile Motion

**Equations:**
```
Range       R = v₀² · sin(2θ) / g
Peak height H = v₀² · sin²(θ) / (2g)
Time aloft  T = 2v₀ · sin(θ)  / g
```

**What is verified:** Two exact physical identities are checked by integer
comparison rather than floating-point tolerance:

1. At θ = 45°, sin(2 × 45°) = sin(90°) = 1, so the formula R = v₀²·sin(2θ)/g
   reduces to R = v₀²/g. The computed range and the direct formula must produce
   **identical raw integer values**.

2. Complementary angles (30° and 60°) must give the same range, because
   sin(2 × 30°) = sin(60°) = sin(120°) = sin(2 × 60°). FixedArithmetic
   confirms exact equality; `double` does not — the two values are close but
   their bit patterns differ.

---

### 2. Simple Harmonic Motion — Energy Conservation

**System:** Spring-mass oscillator, k = 40 N/m, A = 0.1 m, m = 0.5 kg.

**Equations:**
```
omega  = sqrt(k/m)
U(phi) = ½kA²cos²(phi)   (potential energy)
K(phi) = ½kA²sin²(phi)   (kinetic energy)
E      = U + K = ½kA²    (total energy, constant)
```

**What is verified** at 12 equally spaced phase angles (0, π/6, π/3, ...,
11π/6) covering a full oscillation cycle:

1. **Pythagorean identity:** cos²(φ) + sin²(φ) = 1, checked by exact integer
   equality against `FixedArithmetic.of(1)`.
2. **Energy conservation:** U + K = ½kA² = 0.2 J, checked by exact integer
   equality against the pre-computed total energy.

The `double` version is checked against a tolerance of 10⁻¹⁴ — it cannot
be checked for exact equality. The final line reports the mismatch counts for
both implementations.

---

### 3. Special Relativity — Lorentz Factor

**Equation:**
```
γ(v) = 1 / sqrt(1 − v²/c²)
```

**The problem this exposes:**  As v approaches c, the quantity (1 − v²/c²)
approaches zero. This is the textbook definition of **catastrophic
cancellation**: subtracting two nearly equal floating-point numbers destroys
significant digits. The relative error in the subtraction grows without bound
as the two values converge, and the computed square root and reciprocal
inherit and amplify that error. With `double` at v = 0.999c, the leading
digits of (1 − v²/c²) have already been corrupted.

`FixedArithmetic` is immune because the subtraction 1 − v²/c² is an exact
integer operation on 64-bit scaled integers. No bits are lost regardless of
how close v is to c.

**Exact verification:** At v = c·√3/2 the Lorentz factor is exactly 2,
because 1 − (√3/2)² = 1 − 3/4 = 1/4, and 1/√(1/4) = 2. This exact result
is verified by comparing the computed `rawScaled()` integer against
`FixedArithmetic.of(2).rawScaled()`.

---

### 4. Kepler's Third Law

**Equation:**
```
T = 2π · sqrt(a³ / GM)
```

**Units:** Working in astronomical units (AU) and Earth years gives the
elegant result GM_sun = 4π² AU³/yr², so Kepler's ratio simplifies to:

```
T² / a³ = 1    for every planet
```

**What is verified:** The period T is computed for all eight planets from
Mercury to Neptune using their published semi-major axes. For each planet,
the ratio T²/a³ is computed and compared against 1.0 to within 10⁻⁶. The
table shows the computed period alongside the published value and the ratio.

---

### 5. Wave Superposition

**Equations:**
```
y₁ = A·sin(kx − ωt)
y₂ = A·sin(kx − ωt + φ)

y₁ + y₂ = 2A·cos(φ/2) · sin(kx − ωt + φ/2)

Resultant amplitude = 2A · |cos(φ/2)|
```

**Exact cases verified** for amplitude A = 3:

| Phase φ | Expected amplitude | Physical meaning |
|---|---|---|
| 0 | 6 = 2A | Fully constructive |
| π | 0 | Fully destructive |
| π/2 | 3√2 ≈ 4.243 | Quarter-cycle offset |
| 2π/3 | 3 = A | Third-cycle offset |
| π/3 | 3√3 ≈ 5.196 | Sixth-cycle offset |

Each result is compared against its exact algebraic value using integer
equality. The destructive interference case (φ = π) is further probed by
printing the raw `rawScaled()` integer — if it is zero, the result is
**exactly** zero, not merely very small. The corresponding `double` value of
cos(π/2) is also printed at full precision to show the tiny non-zero residual
that floating-point leaves behind.

---

### 6. Newton's Law of Gravitation — Third Law Verification

**Equation:**
```
F = G · m₁ · m₂ / r²
```

**What is verified:**  
Newton's Third Law states F₁₂ = F₂₁. Since the formula is symmetric in m₁
and m₂, this holds analytically. Computationally it holds only if
multiplication is **exactly commutative** — which IEEE 754 floating-point is
not when operands are reordered across expression boundaries, because
floating-point multiplication is not associative. `FixedArithmetic` uses the
Russian-peasant multiplication algorithm (exact integer operations), so
m₁ × m₂ and m₂ × m₁ always produce an identical `rawScaled()` result
regardless of operand order.

**Bonus result:** The ratio F(Sun–Moon) / F(Earth–Moon) is computed,
confirming the well-known but counterintuitive fact that the Sun's
gravitational pull on the Moon is approximately twice that of Earth — which is
why the Moon is more correctly described as co-orbiting the Sun than as
orbiting Earth.

---

## Building and Running

### Prerequisites

- Java 8 or later (JDK — not just JRE)
- `javamathlib.jar` on the classpath (provides `FixedArithmetic` and
  `FixedTrigonometry`)

### Compile

```bash
javac -cp javamathlib.jar \
      -d classes \
      src/org/antic/maths/PhysicsDemo.java
```

Or compile alongside the rest of the library:

```bash
javac -d classes src/org/antic/maths/*.java
```

### Run

```bash
java -cp classes:javamathlib.jar org.antic.maths.PhysicsDemo
```

On Windows, replace `:` with `;` in the classpath separator:

```cmd
java -cp classes;javamathlib.jar org.antic.maths.PhysicsDemo
```

### Using the ZSH build script

If the repository's `build.sh` script is available:

```bash
# Build the full library including the demo
./build.sh -s src -c classes -o javamathlib.jar

# Run the demo
java -cp javamathlib.jar org.antic.maths.PhysicsDemo
```

### Expected output structure

```
========================================================================
  FIXEDARITHMETIC PHYSICS DEMONSTRATION
  Exact integer arithmetic applied to classical and modern physics
  org.antic.maths  --  9 decimal places, no floating-point
========================================================================

========================================================================
  1. PROJECTILE MOTION   R = v₀²sin(2θ)/g
========================================================================

  v₀ = 50 m/s,  g = 9.80665 m/s²

  theta = 45 deg  (maximum range, sin(90 deg) = 1)
    sin(2*45 deg)       = 1.0
    Range R             = 254.929... m
    v0^2/g (exact)      = 254.929... m
    R equals v0^2/g ?   = YES (exact)

  Complementary angles — range must be identical:
    R(30 deg) = 220.737... m
    R(60 deg) = 220.737... m
    Equal ?   = YES — exact integer equality

  double comparison for complementary angles:
    R(30 deg) = 220.737277...
    R(60 deg) = 220.737277...
    Equal ?   = NO — floating-point drift
...
```

---

## Generating Javadoc

```bash
javadoc \
  -classpath javamathlib.jar \
  -d docs \
  -sourcepath src \
  -subpackages org.antic.maths \
  -windowtitle "JavaMathLib API" \
  -doctitle "JavaMathLib — FixedArithmetic Physics Demo" \
  -author -version -private
```

Open `docs/index.html` in a browser to view the full API documentation,
including the detailed Javadoc for each of the six physics methods.

---

## Classes Used

| Class | Role in demo |
|---|---|
| `FixedArithmetic` | All arithmetic: add, subtract, multiply, divide, sqrt, pow, abs, floor |
| `FixedTrigonometry` | sin, cos, tan, asin, acos, atan — all via Taylor series over FixedArithmetic |

Both classes are in `package org.antic.maths` and are part of the
JavaMathLib library. The arithmetic core of `FixedArithmetic` uses only
integer addition and subtraction — no Java `*`, `/`, or `%` operators appear
anywhere in its implementation. All values are stored as 64-bit scaled
integers with a fixed scale of 10⁹, giving nine decimal places of exact
fractional precision.

---

## Academic Licence Notice

This library is provided **free of charge and without any licence requirement**
for use by academic institutions, students, and researchers. You may use,
modify, incorporate into coursework, cite in publications, and redistribute
within academic contexts without restriction and without contacting the author.

If you use this library in published research, a citation or acknowledgement
is appreciated but not required.

**Commercial licence enquiries:** mariogianota@protonmail.com  
**Copyright © Mario Gianota. All rights reserved (non-academic use).**
