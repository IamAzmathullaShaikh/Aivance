package com.bangersoul.aivance.core.common.security

/**
 * Real certificate pins for every provider host the app talks to.
 *
 * Pins are SHA-256 hashes of the SubjectPublicKeyInfo (SPKI), the same
 * format OkHttp's native `CertificatePinner` and our interceptor use.
 * Each host carries its **live leaf pin plus the issuing/intermediate and
 * root CA pins** so that ordinary leaf-certificate rotation (e.g. Google
 * Trust Services reissuance) still matches via the stable CA pin. A CA
 * rotation requires an app update — this is the intended fail-closed
 * behavior of pinning.
 *
 * VERIFICATION (evidence, fetched live from each host's TLS chain on
 * 2026-08-04 — see extract_pins.py / security_scan.py in the repo root):
 *
 *   api.groq.com                     leaf aa5cf8666f4f6cfa...
 *   api.openai.com                   leaf 244339974d38f7dd...
 *   openrouter.ai                    leaf 455476f15affcffc...
 *   api.anthropic.com                leaf cb37cd6f56d170d1...
 *   generativelanguage.googleapis.com leaf 1f981a301a267222...
 *   remoteok.com                     leaf 91d9db38e20b4fdd...
 *   remotive.com                     leaf e9d4ef2aaaad56ec...
 *   api.apify.com                    leaf 42a6b51960194444...
 *   api.hunter.io                    leaf d4f37a0eedec2ba3...
 *
 * CA pins shared across hosts:
 *   Google Trust Services WE1       kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=
 *   Google Trust Services WE2       vh78KSg1Ry4NaqGDV10w/cTb9VH3BQUZoCWNa93W/EY=
 *   GTS Root R4                     mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=
 *   Amazon RSA 2048 M04             G9LNNAql897egYsabashkzUCTEJkWBzgoEtk8X/678c=
 *   Amazon Root CA 1                ++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=
 */
object CertificatePins {

    /**
     * OkHttp-native pins in `sha256/<base64>` form, grouped per host.
     * A host matches when ANY of its pins is present in the presented chain.
     */
    val OKHTTP_PINS: Map<String, List<String>> = mapOf(
        "api.groq.com" to listOf(
            "sha256/qlz4Zm9PbPpv2CxOaUFVnoHR1biLWAAFQhCCt8gSogo=", // leaf
            "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // WE1
            "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="  // GTS Root R4
        ),
        "api.openai.com" to listOf(
            "sha256/JEM5l00499277DC2tHGecIWfIl199sH4CqM/0w0bHsA=", // leaf
            "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // WE1
            "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="  // GTS Root R4
        ),
        "openrouter.ai" to listOf(
            "sha256/RVR28Vr/z/x/oAsg9++hGSb5DnUljt5WjbKMNv0xpN4=", // leaf
            "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // WE1
            "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="  // GTS Root R4
        ),
        "api.anthropic.com" to listOf(
            "sha256/yzfNb1bRcNF+H1Fts441Vj0MIuuxepdWKmqKJ/bVV6U=", // leaf
            "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // WE1
            "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="  // GTS Root R4
        ),
        "generativelanguage.googleapis.com" to listOf(
            "sha256/H5gaMBomciJwleO778pZ4grLUMhP8FX4JYe7pnaSbAs=", // leaf (shared Google edge cert)
            "sha256/vh78KSg1Ry4NaqGDV10w/cTb9VH3BQUZoCWNa93W/EY=", // WE2
            "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="  // GTS Root R4
        ),
        "remoteok.com" to listOf(
            "sha256/kdnbOOILT91kqq6doJavJSAYk+KTEtu4Wl4UFHEXKcw=", // leaf
            "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // WE1
            "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="  // GTS Root R4
        ),
        "remotive.com" to listOf(
            "sha256/6dTvKqqtVuwRE2o79r3e9n/xckVt9ejmu929W8zirbA=", // leaf
            "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // WE1
            "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="  // GTS Root R4
        ),
        "api.apify.com" to listOf(
            "sha256/Qqa1GWAZRERvWBQycjC1oZ/KbrPhNTdRRV+0nK6lDo8=", // leaf (*.apify.com)
            "sha256/G9LNNAql897egYsabashkzUCTEJkWBzgoEtk8X/678c=", // Amazon RSA 2048 M04
            "sha256/++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI="  // Amazon Root CA 1
        ),
        "api.hunter.io" to listOf(
            "sha256/1PN6Du3sK6O9BeLeU7cPJZw3GA0YQxi40Tg5kiqcdLY=", // leaf
            "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=", // WE1
            "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c="  // GTS Root R4
        )
    )

    /**
     * Hex-encoded SPKI SHA-256 pins (lowercase), keyed by host, for the
     * [CertificatePinningInterceptor] (defense-in-depth second layer).
     */
    val HEX_PINS: Map<String, List<String>> = mapOf(
        "api.groq.com" to listOf(
            "aa5cf8666f4f6cfa6fd82c4e6941559e81d1d5b88b580005421082b7c812a20a", // leaf
            "908769e8d34477cc2cba0632c88605b22d7294c0840f78596d247c645b1afc0e", // WE1
            "9847e5653e5e9e847516e5cb818606aa7544a19be67fd7366d506988e8d84347"  // GTS Root R4
        ),
        "api.openai.com" to listOf(
            "244339974d38f7ddbbec30b6b4719e70859f225d7df6c1f80aa33fd30d1b1ec0", // leaf
            "908769e8d34477cc2cba0632c88605b22d7294c0840f78596d247c645b1afc0e", // WE1
            "9847e5653e5e9e847516e5cb818606aa7544a19be67fd7366d506988e8d84347"  // GTS Root R4
        ),
        "openrouter.ai" to listOf(
            "455476f15affcffc7fa00b20f7efa11926f90e75258ede568db28c36fd31a4de", // leaf
            "908769e8d34477cc2cba0632c88605b22d7294c0840f78596d247c645b1afc0e", // WE1
            "9847e5653e5e9e847516e5cb818606aa7544a19be67fd7366d506988e8d84347"  // GTS Root R4
        ),
        "api.anthropic.com" to listOf(
            "cb37cd6f56d170d17e1f516db38e35563d0c22ebb17a97562a6a8a27f6d557a5", // leaf
            "908769e8d34477cc2cba0632c88605b22d7294c0840f78596d247c645b1afc0e", // WE1
            "9847e5653e5e9e847516e5cb818606aa7544a19be67fd7366d506988e8d84347"  // GTS Root R4
        ),
        "generativelanguage.googleapis.com" to listOf(
            "1f981a301a2672227095e3bbefca59e20acb50c84ff055f82587bba676926c0b", // leaf
            "be1efc292835472e0d6aa183575d30fdc4dbf551f7050519a0258d6bddd6fc46", // WE2
            "9847e5653e5e9e847516e5cb818606aa7544a19be67fd7366d506988e8d84347"  // GTS Root R4
        ),
        "remoteok.com" to listOf(
            "91d9db38e20b4fdd64aaae9da096af25201893e29312dbb85a5e1414711729cc", // leaf
            "908769e8d34477cc2cba0632c88605b22d7294c0840f78596d247c645b1afc0e", // WE1
            "9847e5653e5e9e847516e5cb818606aa7544a19be67fd7366d506988e8d84347"  // GTS Root R4
        ),
        "remotive.com" to listOf(
            "e9d4ef2aaaad56ec11136a3bf6bddef67ff172456df5e8e6bbddbd5bcce2adb0", // leaf
            "908769e8d34477cc2cba0632c88605b22d7294c0840f78596d247c645b1afc0e", // WE1
            "9847e5653e5e9e847516e5cb818606aa7544a19be67fd7366d506988e8d84347"  // GTS Root R4
        ),
        "api.apify.com" to listOf(
            "42a6b519601944446f5814327230b5a19fca6eb3e1353751455fb49caea50e8f", // leaf
            "1bd2cd340aa5f3dede818b1a6dab219335024c4264581ce0a04b64f17ffaefc7", // Amazon RSA 2048 M04
            "fbe3018031f9586bcbf41727e417b7d1c45c2f47f93be372a17b96b50757d5a2"  // Amazon Root CA 1
        ),
        "api.hunter.io" to listOf(
            "d4f37a0eedec2ba3bd05e2de53b70f259c37180d184318b8d13839922a9c74b6", // leaf
            "908769e8d34477cc2cba0632c88605b22d7294c0840f78596d247c645b1afc0e", // WE1
            "9847e5653e5e9e847516e5cb818606aa7544a19be67fd7366d506988e8d84347"  // GTS Root R4
        )
    )

    /** True when [pin] is an OkHttp-native `sha256/` base64 pin. */
    fun isOkHttpPin(pin: String): Boolean =
        pin.startsWith("sha256/") && pin.length == "sha256/".length + 44

    /** True when [hex] is a 64-char lowercase hex SPKI digest. */
    fun isHexPin(hex: String): Boolean =
        hex.length == 64 && hex.all { it in "0123456789abcdef" }
}
