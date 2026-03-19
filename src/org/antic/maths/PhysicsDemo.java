package org.antic.maths;
/**
 * A demonstration of {@link FixedArithmetic} and {@link FixedTrigonometry}
 * applied to classical and modern physics problems.
 *
 * <p>Each section solves a well-known physics equation using exact
 * fixed-point arithmetic, then repeats the same computation in Java
 * {@code double} floating-point and shows where and by how much the two
 * diverge. Six domains are covered:</p>
 * <ol>
 *   <li><b>Projectile Motion</b> — range, peak height, time of flight.
 *       Verifies that complementary launch angles produce identical range
 *       using exact integer comparison.</li>
 *   <li><b>Simple Harmonic Motion</b> — energy conservation across a full
 *       cycle. Verifies U + K = E and cos²φ + sin²φ = 1 at twelve phase
 *       angles.</li>
 *   <li><b>Special Relativity</b> — the Lorentz factor γ(v). Demonstrates
 *       catastrophic cancellation in (1 − v²/c²) for double and shows
 *       FixedArithmetic is immune.</li>
 *   <li><b>Kepler's Third Law</b> — orbital period from semi-major axis
 *       for all eight planets. Verifies T²/a³ = 1 for each.</li>
 *   <li><b>Wave Superposition</b> — constructive and destructive
 *       interference amplitudes verified to exact integer equality.</li>
 *   <li><b>Newton's Law of Gravitation</b> — verifies that the force
 *       F₁₂ = F₂₁ holds at the exact integer level regardless of
 *       multiplication order.</li>
 * </ol>
 *
 * @author Mario Gianota
 * @see FixedArithmetic
 * @see FixedTrigonometry
 */
public class PhysicsDemo {

    // ── Shared constants ──────────────────────────────────────────────────────
    static final FixedArithmetic PI  = FixedArithmetic.of("3.141592653");
    static final FixedArithmetic TWO = FixedArithmetic.of(2);
    static final FixedArithmetic ONE = FixedArithmetic.of(1);
    static final FixedArithmetic g   = FixedArithmetic.of("9.80665");

    // ── Entry point ───────────────────────────────────────────────────────────

    /**
     * Runs all six physics demonstrations in sequence.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        banner();
        projectileMotion();
        simpleHarmonicMotion();
        lorentzFactor();
        keplerThirdLaw();
        waveSuperposition();
        newtonGravitation();
        System.out.println("\n" + line('=', 72));
        System.out.println("  All demonstrations complete.");
        System.out.println(line('=', 72));
    }

    // =========================================================================
    // 1. PROJECTILE MOTION
    // =========================================================================

    /**
     * Projectile motion — range, peak height, and time of flight.
     *
     * <p>For a projectile launched at speed v₀ and angle θ above horizontal:</p>
     * <pre>
     *   Range       R = v₀² · sin(2θ) / g
     *   Peak height H = v₀² · sin²(θ)  / (2g)
     *   Time aloft  T = 2v₀ · sin(θ)   / g
     * </pre>
     *
     * <p>Two exact identities are verified:</p>
     * <ol>
     *   <li>At θ = 45°: sin(90°) = 1, so R = v₀²/g exactly.</li>
     *   <li>Complementary angles: R(30°) = R(60°), because
     *       sin(60°) = sin(120°). Verified by exact integer comparison
     *       of the two FixedArithmetic results.</li>
     * </ol>
     */
    static void projectileMotion() {
        section("1. PROJECTILE MOTION   R = v₀²sin(2θ)/g");

        FixedArithmetic v0    = FixedArithmetic.of("50");
        FixedArithmetic v0sq  = v0.multiply(v0);

        // Radians for 45°, 30°, 60°
        FixedArithmetic t45 = FixedArithmetic.of("0.785398163");
        FixedArithmetic t30 = FixedArithmetic.of("0.523598775");
        FixedArithmetic t60 = FixedArithmetic.of("1.047197551");

        System.out.println("  v₀ = 50 m/s,  g = 9.80665 m/s²");
        System.out.println();

        // --- 45° case: R = v₀²/g ---
        FixedArithmetic sin2_45  = FixedTrigonometry.sin(TWO.multiply(t45));
        FixedArithmetic range45  = v0sq.multiply(sin2_45).divide(g);
        FixedArithmetic rangeExact = v0sq.divide(g);

        System.out.println("  theta = 45 deg  (maximum range, sin(90 deg) = 1)");
        System.out.printf("    sin(2*45 deg)       = %s%n",    sin2_45);
        System.out.printf("    Range R             = %s m%n",  range45);
        System.out.printf("    v0^2/g (exact)      = %s m%n",  rangeExact);
        System.out.printf("    R equals v0^2/g ?   = %s%n%n",
            range45.rawScaled() == rangeExact.rawScaled() ? "YES (exact)" : "NO");

        // --- Complementary angles ---
        FixedArithmetic range30 = v0sq.multiply(
            FixedTrigonometry.sin(TWO.multiply(t30))).divide(g);
        FixedArithmetic range60 = v0sq.multiply(
            FixedTrigonometry.sin(TWO.multiply(t60))).divide(g);

        System.out.println("  Complementary angles — range must be identical:");
        System.out.printf("    R(30 deg) = %s m%n", range30);
        System.out.printf("    R(60 deg) = %s m%n", range60);
        System.out.printf("    Equal ?   = %s%n%n",
            range30.rawScaled() == range60.rawScaled()
                ? "YES — exact integer equality"
                : "NO — mismatch");

        // --- Peak height and time of flight at 45° ---
        FixedArithmetic sin45 = FixedTrigonometry.sin(t45);
        FixedArithmetic H     = v0sq.multiply(sin45).multiply(sin45)
                                    .divide(TWO.multiply(g));
        FixedArithmetic T     = TWO.multiply(v0).multiply(sin45).divide(g);
        System.out.printf("  Peak height H(45 deg) = %s m%n", H);
        System.out.printf("  Time of flight T      = %s s%n",  T);

        // --- double comparison ---
        System.out.println();
        System.out.println("  double comparison for complementary angles:");
        double dv0 = 50.0, dg = 9.80665;
        double dr30 = dv0*dv0*Math.sin(2*Math.PI/6)/dg;
        double dr60 = dv0*dv0*Math.sin(2*Math.PI/3)/dg;
        System.out.printf("    R(30 deg) = %.15f%n", dr30);
        System.out.printf("    R(60 deg) = %.15f%n", dr60);
        System.out.printf("    Equal ?   = %s%n",
            dr30 == dr60 ? "YES" : "NO — floating-point drift");
    }

    // =========================================================================
    // 2. SIMPLE HARMONIC MOTION — ENERGY CONSERVATION
    // =========================================================================

    /**
     * Simple Harmonic Motion — energy conservation across a full cycle.
     *
     * <p>For a spring-mass system (spring constant k, amplitude A, mass m):</p>
     * <pre>
     *   omega  = sqrt(k/m)          angular frequency
     *   x(phi) = A * cos(phi)       displacement
     *   v(phi) = -A*omega*sin(phi)  velocity
     *   U(phi) = 1/2 * k * x^2     potential energy  = 1/2 * k * A^2 * cos^2(phi)
     *   K(phi) = 1/2 * m * v^2     kinetic energy    = 1/2 * k * A^2 * sin^2(phi)
     *   E      = U + K              = 1/2 * k * A^2  (constant)
     * </pre>
     *
     * <p>Two identities are verified at 12 equally spaced phase angles:</p>
     * <ol>
     *   <li>Pythagorean: cos²(phi) + sin²(phi) = 1</li>
     *   <li>Energy conservation: U + K = E = 1/2 * k * A² exactly</li>
     * </ol>
     */
    static void simpleHarmonicMotion() {
        section("2. SIMPLE HARMONIC MOTION — ENERGY CONSERVATION");

        FixedArithmetic k    = FixedArithmetic.of("40");   // N/m
        FixedArithmetic A    = FixedArithmetic.of("0.1");  // m
        FixedArithmetic m    = FixedArithmetic.of("0.5");  // kg

        FixedArithmetic halfK  = k.divide(TWO);
        FixedArithmetic Etotal = halfK.multiply(A).multiply(A);  // 1/2 k A^2
        FixedArithmetic omega  = k.divide(m).sqrt();             // sqrt(k/m)

        System.out.printf("  k = %s N/m,  A = %s m,  m = %s kg%n", k, A, m);
        System.out.printf("  omega = sqrt(k/m) = %s rad/s%n", omega);
        System.out.printf("  Total energy E = 1/2*k*A^2 = %s J%n%n", Etotal);

        System.out.printf("  %-8s  %-16s  %-13s  %-13s  %-13s  %-6s%n",
            "Phase", "cos^2+sin^2", "U (J)", "K (J)", "U+K (J)", "OK?");
        System.out.println("  " + line('-', 74));

        String[] labels = { "0","pi/6","pi/3","pi/2","2pi/3","5pi/6",
                            "pi","7pi/6","4pi/3","3pi/2","5pi/3","11pi/6" };
        FixedArithmetic piOver6 = FixedArithmetic.of("0.523598775");

        int energyFail = 0, pythagFail = 0, doubleFail = 0;

        for (int i = 0; i < 12; i++) {
            FixedArithmetic phi = piOver6.multiply(FixedArithmetic.of(i));
            FixedArithmetic c   = FixedTrigonometry.cos(phi);
            FixedArithmetic s   = FixedTrigonometry.sin(phi);

            // Pythagorean identity
            FixedArithmetic pythag = c.multiply(c).add(s.multiply(s));

            // Energies
            FixedArithmetic U = halfK.multiply(A).multiply(A).multiply(c.multiply(c));
            FixedArithmetic K = halfK.multiply(A).multiply(A).multiply(s.multiply(s));
            FixedArithmetic E = U.add(K);

            boolean eOk = E.rawScaled()      == Etotal.rawScaled();
            boolean pOk = pythag.rawScaled() == ONE.rawScaled();
            if (!eOk) energyFail++;
            if (!pOk) pythagFail++;

            System.out.printf("  %-8s  %-16s  %-13s  %-13s  %-13s  %s%n",
                labels[i], pythag, U, K, E, eOk ? "YES" : "NO");

            // double check
            double dphi = i * Math.PI / 6.0;
            double dc = Math.cos(dphi), ds = Math.sin(dphi);
            double dE = 0.5 * 40 * 0.01 * (dc*dc + ds*ds);
            if (Math.abs(dE - 0.2) > 1e-14) doubleFail++;
        }

        System.out.println("  " + line('-', 74));
        System.out.printf("%n  FixedArithmetic energy mismatches    : %d / 12%n", energyFail);
        System.out.printf("  FixedArithmetic Pythagoras mismatches: %d / 12%n", pythagFail);
        System.out.printf("  double energy mismatches (tol 1e-14) : %d / 12%n", doubleFail);
    }

    // =========================================================================
    // 3. SPECIAL RELATIVITY — LORENTZ FACTOR
    // =========================================================================

    /**
     * Special Relativity — the Lorentz factor.
     *
     * <pre>
     *   gamma(v) = 1 / sqrt(1 - v^2/c^2)
     * </pre>
     *
     * <p>As v approaches c, the term (1 - v^2/c^2) approaches zero.
     * This is a textbook case of <em>catastrophic cancellation</em>: when two
     * nearly equal values are subtracted in floating-point arithmetic, the
     * leading significant digits cancel and the result has far fewer correct
     * digits than either operand. With double at v = 0.9999999c, essentially
     * all precision is lost. FixedArithmetic is immune because the subtraction
     * is an exact integer operation — no bits are lost regardless of how
     * close v is to c.</p>
     *
     * <p>Exact verification: at v = c*sqrt(3)/2, gamma = 2.0 exactly,
     * because 1 - 3/4 = 1/4, and 1/sqrt(1/4) = 2.</p>
     */
    static void lorentzFactor() {
        section("3. SPECIAL RELATIVITY — LORENTZ FACTOR  gamma = 1/sqrt(1-v^2/c^2)");

        FixedArithmetic c   = FixedArithmetic.of("299792458");  // m/s
        FixedArithmetic cSq = c.multiply(c);

        System.out.println("  c = 299,792,458 m/s");
        System.out.println();
        System.out.printf("  %-10s  %-13s  %-16s  %-16s%n",
            "v/c (beta)", "1 - beta^2", "gamma (Fixed)", "gamma (double)");
        System.out.println("  " + line('-', 60));

        String[] betas  = { "0", "0.5", "0.8", "0.9", "0.99", "0.999" };
        double[] dbetas = { 0.0,  0.5,  0.8,  0.9,  0.99,  0.999  };

        for (int i = 0; i < betas.length; i++) {
            FixedArithmetic beta  = FixedArithmetic.of(betas[i]);
            FixedArithmetic v     = beta.multiply(c);
            FixedArithmetic inner = ONE.subtract(v.multiply(v).divide(cSq));
            FixedArithmetic gamma = ONE.divide(inner.sqrt());

            double db = dbetas[i];
            double dg = 1.0 / Math.sqrt(1.0 - db*db);

            System.out.printf("  %-10s  %-13s  %-16s  %.9f%n",
                betas[i], inner, gamma, dg);
        }

        // --- Exact case: v = c*sqrt(3)/2 → gamma = 2.0 exactly ---
        System.out.println();
        System.out.println("  Exact case: v = c * sqrt(3)/2  -->  gamma must equal 2.0 exactly");
        System.out.println("  Because: 1 - (sqrt(3)/2)^2 = 1 - 3/4 = 1/4,  1/sqrt(1/4) = 2");
        FixedArithmetic sqrt3over2 = FixedArithmetic.of("3").sqrt()
                                         .divide(TWO);
        FixedArithmetic vEx   = sqrt3over2.multiply(c);
        FixedArithmetic betaEx = vEx.divide(c);
        FixedArithmetic innerEx = ONE.subtract(betaEx.multiply(betaEx));
        FixedArithmetic gammaEx = ONE.divide(innerEx.sqrt());

        FixedArithmetic two = FixedArithmetic.of(2);
        System.out.printf("    beta             = %s%n", betaEx);
        System.out.printf("    1 - beta^2       = %s  (should be 0.25)%n", innerEx);
        System.out.printf("    gamma            = %s%n", gammaEx);
        System.out.printf("    gamma == 2.0 ?   = %s%n",
            gammaEx.rawScaled() == two.rawScaled()
                ? "YES — exact at the integer level"
                : "NO — precision lost");

        // --- Relativistic mass increase demonstration ---
        System.out.println();
        System.out.println("  Relativistic momentum p = gamma*m*v for a 1 kg mass at 0.9c:");
        FixedArithmetic beta9  = FixedArithmetic.of("0.9");
        FixedArithmetic v9     = beta9.multiply(c);
        FixedArithmetic inner9 = ONE.subtract(beta9.multiply(beta9));
        FixedArithmetic gamma9 = ONE.divide(inner9.sqrt());
        // p = gamma*m*v, m=1kg; report gamma*v (momentum per unit mass)
        FixedArithmetic pPerM  = gamma9.multiply(v9);
        System.out.printf("    gamma(0.9c)      = %s%n", gamma9);
        System.out.printf("    p/m = gamma*v    = %s m/s%n", pPerM);
        System.out.printf("    Newtonian v      = %s m/s  (gamma=1 approximation)%n", v9);
    }

    // =========================================================================
    // 4. KEPLER'S THIRD LAW
    // =========================================================================

    /**
     * Kepler's Third Law — orbital period from semi-major axis.
     *
     * <pre>
     *   T = 2*pi * sqrt(a^3 / GM)
     * </pre>
     *
     * <p>Working in astronomical units (AU) and Earth years, GM_sun = 4*pi^2
     * AU^3/yr^2, which gives the elegant dimensionless form T^2/a^3 = 1 for
     * every planet. This is Kepler's ratio and should hold exactly for all
     * eight planets. We verify it for each and report the deviation.</p>
     */
    static void keplerThirdLaw() {
        section("4. KEPLER'S THIRD LAW   T = 2*pi*sqrt(a^3/GM)");

        // GM_sun = 4*pi^2 AU^3/yr^2 — exact in these units
        FixedArithmetic GM = FixedArithmetic.of("4").multiply(PI).multiply(PI);

        System.out.println("  Units: AU and Earth years.");
        System.out.println("  GM_sun = 4*pi^2 AU^3/yr^2  =>  T^2/a^3 = 1 for every planet.");
        System.out.println();
        System.out.printf("  %-10s  %-10s  %-14s  %-12s  %-12s  %s%n",
            "Planet", "a (AU)", "T (yr, calc)", "T (yr, known)", "T^2/a^3", "~1?");
        System.out.println("  " + line('-', 76));

        String[] planets = { "Mercury","Venus","Earth","Mars",
                             "Jupiter","Saturn","Uranus","Neptune" };
        String[] axes    = { "0.387","0.723","1.000","1.524",
                             "5.203","9.537","19.191","30.069" };
        String[] known   = { "0.241","0.615","1.000","1.881",
                             "11.862","29.457","84.011","164.79" };

        int bad = 0;
        for (int i = 0; i < planets.length; i++) {
            FixedArithmetic a    = FixedArithmetic.of(axes[i]);
            FixedArithmetic aCub = a.multiply(a).multiply(a);
            FixedArithmetic T    = TWO.multiply(PI).multiply(aCub.divide(GM).sqrt());
            FixedArithmetic ratio = T.multiply(T).divide(aCub);

            // Accept within 10^-6 of 1.0
            long diff = Math.abs(ratio.rawScaled() - ONE.rawScaled());
            boolean ok = diff < 1000;
            if (!ok) bad++;

            System.out.printf("  %-10s  %-10s  %-14s  %-12s  %-12s  %s%n",
                planets[i], axes[i], T, known[i], ratio, ok ? "YES" : "NO");
        }
        System.out.println("  " + line('-', 76));
        System.out.printf("  Keplers ratio mismatches: %d / 8%n", bad);
    }

    // =========================================================================
    // 5. WAVE SUPERPOSITION
    // =========================================================================

    /**
     * Wave Superposition — constructive and destructive interference.
     *
     * <p>Two waves of equal amplitude A and frequency, with phase offset phi:</p>
     * <pre>
     *   y1 = A*sin(kx - wt)
     *   y2 = A*sin(kx - wt + phi)
     *
     *   y1 + y2 = 2*A*cos(phi/2) * sin(kx - wt + phi/2)
     * </pre>
     *
     * <p>Resultant amplitude = 2*A*|cos(phi/2)|. Exact values:</p>
     * <pre>
     *   phi = 0      --> amplitude = 2A          (fully constructive)
     *   phi = pi     --> amplitude = 0           (fully destructive)
     *   phi = pi/2   --> amplitude = A*sqrt(2)
     *   phi = 2*pi/3 --> amplitude = A
     *   phi = pi/3   --> amplitude = A*sqrt(3)
     * </pre>
     */
    static void waveSuperposition() {
        section("5. WAVE SUPERPOSITION   amplitude = 2A|cos(phi/2)|");

        FixedArithmetic A = FixedArithmetic.of("3");

        System.out.println("  Amplitude A = 3.0");
        System.out.println("  Resultant amplitude = 2*A*|cos(phi/2)|");
        System.out.println();
        System.out.printf("  %-10s  %-18s  %-20s  %-18s  %s%n",
            "phi", "Amplitude (Fixed)", "Expected", "Amplitude (double)", "Exact?");
        System.out.println("  " + line('-', 80));

        // { phi_label, phi_value, expected_label, expected_fa_value }
        String[][] cases = {
            { "0",      "0",           "2A = 6",          "6"           },
            { "pi",     "3.141592653", "0",               "0"           },
            { "pi/2",   "1.570796326", "A*sqrt(2)",       "4.242640687" },
            { "2*pi/3", "2.094395102", "A = 3",           "3"           },
            { "pi/3",   "1.047197551", "A*sqrt(3)",       "5.196152422" },
        };

        for (String[] c : cases) {
            FixedArithmetic phi  = FixedArithmetic.of(c[1]);
            FixedArithmetic amp  = TWO.multiply(A).multiply(
                FixedTrigonometry.cos(phi.divide(TWO))).abs();
            FixedArithmetic exp  = FixedArithmetic.of(c[3]);
            boolean exact = amp.rawScaled() == exp.rawScaled();

            double dphi = Double.parseDouble(c[1]);
            double dAmp = 2 * 3.0 * Math.abs(Math.cos(dphi / 2.0));

            System.out.printf("  %-10s  %-18s  %-20s  %-18.9f  %s%n",
                c[0], amp, c[2] + " = " + c[3], dAmp,
                exact ? "YES" : "approx");
        }

        // --- Destructive: verify the raw integer is exactly zero ---
        System.out.println();
        System.out.println("  Destructive interference (phi=pi): is result exactly zero?");
        FixedArithmetic phiPi  = FixedArithmetic.of("3.141592653");
        FixedArithmetic dest   = TWO.multiply(A).multiply(
            FixedTrigonometry.cos(phiPi.divide(TWO))).abs();
        System.out.printf("    cos(pi/2)          = %s%n",
            FixedTrigonometry.cos(phiPi.divide(TWO)));
        System.out.printf("    Resultant amplitude = %s%n", dest);
        System.out.printf("    Raw scaled integer  = %d  (0 = exactly zero)%n",
            dest.rawScaled());
        System.out.printf("    double cos(pi/2)    = %.20f%n",
            Math.cos(Math.PI / 2.0));
    }

    // =========================================================================
    // 6. NEWTON'S LAW OF GRAVITATION — THIRD LAW VERIFICATION
    // =========================================================================

    /**
     * Newton's Law of Gravitation and the Third Law.
     *
     * <pre>
     *   F = G * m1 * m2 / r^2
     * </pre>
     *
     * <p>Newton's Third Law: F12 = F21. Since the formula is symmetric in
     * m1 and m2, this holds analytically. Computationally it only holds if
     * multiplication is commutative and exact — which floating-point is not
     * when operands are reordered across expression boundaries, because
     * IEEE 754 multiplication is not associative. FixedArithmetic uses
     * exact integer multiplication (Russian-peasant algorithm), so
     * m1 * m2 == m2 * m1 always produces the same bit-pattern.</p>
     *
     * <p>We also compute the ratio F(Sun-Moon) / F(Earth-Moon) to verify
     * that the Sun pulls the Moon approximately twice as hard as Earth does
     * — a well-known result that surprises most students.</p>
     */
    static void newtonGravitation() {
        section("6. NEWTON'S LAW OF GRAVITATION   F = G*m1*m2/r^2");

        // Scale factors: G*10^11, masses in 10^22 kg units, r in 10^8 m units
        // This keeps all values in a range FixedArithmetic handles comfortably.
        FixedArithmetic Gsc = FixedArithmetic.of("6.674");   // G * 10^11
        FixedArithmetic mE  = FixedArithmetic.of("597.2");   // Earth  5.972*10^24 = 597.2 * 10^22
        FixedArithmetic mM  = FixedArithmetic.of("7.342");   // Moon   7.342*10^22
        FixedArithmetic r   = FixedArithmetic.of("3.844");   // 3.844 * 10^8 m

        System.out.println("  Earth-Moon system (scaled units: G*10^11, masses*10^-22 kg, r*10^-8 m)");
        System.out.println();

        FixedArithmetic rSq = r.multiply(r);
        FixedArithmetic F12 = Gsc.multiply(mE).multiply(mM).divide(rSq);
        FixedArithmetic F21 = Gsc.multiply(mM).multiply(mE).divide(rSq);

        System.out.printf("  F12 = G*mEarth*mMoon / r^2 = %s (scaled)%n", F12);
        System.out.printf("  F21 = G*mMoon*mEarth / r^2 = %s (scaled)%n", F21);
        System.out.printf("  F12 == F21 (integer level)? = %s%n%n",
            F12.rawScaled() == F21.rawScaled()
                ? "YES — multiplication order irrelevant in FixedArithmetic"
                : "NO");

        // --- double comparison: does multiplication order matter? ---
        System.out.println("  double comparison:");
        double dG = 6.674e-11, dmE = 5.972e24, dmM = 7.342e22, dr = 3.844e8;
        double dF12 = dG * dmE * dmM / (dr * dr);
        double dF21 = dG * dmM * dmE / (dr * dr);
        System.out.printf("    F12 = G*mE*mM/r^2 = %.15e N%n", dF12);
        System.out.printf("    F21 = G*mM*mE/r^2 = %.15e N%n", dF21);
        System.out.printf("    F12 == F21 ?      = %s%n%n",
            dF12 == dF21 ? "YES" : "NO — operand order changes result");

        // --- Sun pulls Moon harder than Earth does ---
        System.out.println("  Sun vs Earth: which pulls the Moon more strongly?");
        System.out.println("  (ratio F_sun-moon / F_earth-moon, G cancels)");

        // Masses in same unit (10^22 kg): Sun = 1.989*10^30 / 10^22 = 1.989*10^8
        FixedArithmetic mSun  = FixedArithmetic.of("19890000");  // 1.989*10^8 * 10^22
        // Sun-Moon distance approx = Sun-Earth = 1.496*10^11 m = 1496 * 10^8 m
        FixedArithmetic rSM   = FixedArithmetic.of("1496");
        FixedArithmetic rSMSq = rSM.multiply(rSM);
        // Earth-Moon: r = 3.844 * 10^8 m (same unit as above)
        FixedArithmetic rEMSq = rSq;   // already computed

        // Ratio = (mSun / rSM^2) / (mE / rEM^2)
        FixedArithmetic ratio = mSun.divide(rSMSq).divide(
                                mE.divide(rEMSq));

        System.out.printf("    F(Sun-Moon) / F(Earth-Moon) = %s%n", ratio);
        System.out.println("    The Sun's gravitational pull on the Moon is roughly");
        System.out.println("    twice that of Earth — which is why the Moon orbits");
        System.out.println("    the Sun (with Earth) rather than truly orbiting Earth.");
    }

    // ── Private formatting helpers ────────────────────────────────────────────

    private static void banner() {
        System.out.println("=" .repeat(72));
        System.out.println("  FIXEDARITHMETIC PHYSICS DEMONSTRATION");
        System.out.println("  Exact integer arithmetic applied to classical and modern physics");
        System.out.println("  org.antic.maths  --  9 decimal places, no floating-point");
        System.out.println("=".repeat(72));
    }

    private static void section(String title) {
        System.out.println("\n" + line('=', 72));
        System.out.println("  " + title);
        System.out.println(line('=', 72) + "\n");
    }

    private static String line(char ch, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(ch);
        return sb.toString();
    }
}
