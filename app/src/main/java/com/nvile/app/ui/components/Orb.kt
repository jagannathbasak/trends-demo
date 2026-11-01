package com.nvile.app.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import com.nvile.app.ui.theme.Nv
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Colour moods for the orb. */
enum class OrbMood(val a: Color, val b: Color, val c: Color) {
    Prediction(Color(0xFF0A3D35), Nv.Mint, Nv.Indigo),
    Thinking(Color(0xFF14104A), Nv.Cyan, Nv.Violet),
    Caution(Color(0xFF3A1A08), Nv.Amber, Nv.Coral),
}

private val isRobolectric = Build.FINGERPRINT.contains("robolectric", ignoreCase = true)

/**
 * NVILE's signature: a living sphere of light. On Android 13+ it is a fluid-noise shader
 * running on the GPU; elsewhere a layered-gradient approximation. [energy] (0–1) speeds the
 * flow and brightens the glow while NVILE is thinking.
 */
@Composable
fun Orb(modifier: Modifier = Modifier, mood: OrbMood = OrbMood.Prediction, energy: Float = 0f) {
    val e by animateFloatAsState(energy, tween(900), label = "energy")
    val time by produceState(0f) {
        val start = withInfiniteAnimationFrameMillis { it }
        while (true) withInfiniteAnimationFrameMillis { value = (it - start) / 1000f }
    }
    if (Build.VERSION.SDK_INT >= 33 && !isRobolectric) {
        ShaderOrb(modifier, mood, e, time)
    } else {
        GradientOrb(modifier, mood, e, time)
    }
}

@RequiresApi(33)
@Composable
private fun ShaderOrb(modifier: Modifier, mood: OrbMood, energy: Float, time: Float) {
    val shader = remember { RuntimeShader(ORB_AGSL) }
    val brush = remember(shader) { ShaderBrush(shader) }
    Canvas(modifier) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        shader.setFloatUniform("iEnergy", energy)
        shader.setFloatUniform("cA", mood.a.red, mood.a.green, mood.a.blue, 1f)
        shader.setFloatUniform("cB", mood.b.red, mood.b.green, mood.b.blue, 1f)
        shader.setFloatUniform("cC", mood.c.red, mood.c.green, mood.c.blue, 1f)
        drawRect(brush)
    }
}

@Composable
private fun GradientOrb(modifier: Modifier, mood: OrbMood, energy: Float, time: Float) {
    Canvas(modifier) {
        val c = center
        val r = min(size.width, size.height) * 0.39f
        val t = time * (0.35f + energy)
        // outer glow
        drawCircle(
            Brush.radialGradient(listOf(mood.b.copy(alpha = 0.38f + 0.2f * energy), mood.c.copy(alpha = 0.12f), Color.Transparent), c, r * 1.6f),
            r * 1.6f, c,
        )
        val sphere = Path().apply { addOval(androidx.compose.ui.geometry.Rect(c, r)) }
        clipPath(sphere) {
            drawCircle(Brush.radialGradient(listOf(lerp(mood.a, mood.b, 0.35f), mood.a, Color(0xFF020504)), c + Offset(-r * 0.3f, -r * 0.35f), r * 1.5f), r, c)
            // drifting light masses
            val blobs = listOf(
                Triple(mood.b, 0.55f, 0f), Triple(mood.c, 0.6f, 2.1f), Triple(lerp(mood.b, mood.c, 0.5f), 0.45f, 4.2f),
            )
            blobs.forEachIndexed { i, (col, size, phase) ->
                val a = t * (0.7f + i * 0.25f) + phase
                val p = c + Offset(cos(a) * r * 0.42f, sin(a * 1.3f) * r * 0.38f)
                drawCircle(Brush.radialGradient(listOf(col.copy(alpha = 0.85f), col.copy(alpha = 0f)), p, r * size), r * size, p)
            }
            // depth: darken the limb
            drawCircle(Brush.radialGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.55f)), c, r), r, c)
            // specular
            val s = c + Offset(-r * 0.32f, -r * 0.38f)
            drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.55f), Color.Transparent), s, r * 0.42f), r * 0.42f, s)
        }
        // rim light
        drawCircle(
            Brush.radialGradient(listOf(Color.Transparent, mood.b.copy(alpha = 0.0f), mood.b.copy(alpha = 0.45f), Color.Transparent), c, r * 1.03f),
            r * 1.03f, c,
        )
    }
}

private const val ORB_AGSL = """
uniform float2 iResolution;
uniform float iTime;
uniform float iEnergy;
uniform float4 cA;
uniform float4 cB;
uniform float4 cC;

float hash(float2 p) { return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453); }
float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + float2(1.0, 0.0)), u.x),
               mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
}
float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) { v += a * noise(p); p *= 2.03; a *= 0.5; }
    return v;
}

half4 main(float2 fragCoord) {
    float2 uv = (fragCoord - 0.5 * iResolution) / (0.5 * min(iResolution.x, iResolution.y));
    float r = length(uv);
    float t = iTime * (0.22 + iEnergy * 0.9);
    float R = 0.74 + 0.015 * sin(iTime * 1.1) + 0.03 * iEnergy * sin(iTime * 6.0);
    float inside = smoothstep(R, R - 0.012, r);
    float z = sqrt(max(0.0, R * R - r * r)) / R;

    float2 p = uv * (1.7 - 0.7 * z) * 1.6;
    float2 q = float2(fbm(p + float2(0.0, t * 0.7)), fbm(p + float2(5.2, 1.3) - t * 0.5));
    float n = fbm(p + 2.4 * q + float2(t * 0.25, -t * 0.15));

    float3 col = mix(cA.rgb, cB.rgb, smoothstep(0.25, 0.85, n));
    col = mix(col, cC.rgb, smoothstep(0.4, 0.95, q.y) * 0.85);
    col += cB.rgb * pow(smoothstep(0.55, 1.0, n), 3.0) * 0.8;
    col *= 0.28 + 0.9 * z;

    float rim = smoothstep(R - 0.22, R, r) * inside;
    col += mix(cB.rgb, cC.rgb, 0.5 + 0.5 * uv.y) * rim * 0.75;

    float spec = pow(max(0.0, 1.0 - length(uv - float2(-0.27, -0.33)) / 0.42), 3.0);
    col += float3(spec * 0.5);

    float glow = exp(-5.5 * max(0.0, r - R)) * (1.0 - inside);
    float3 g = mix(cB.rgb, cC.rgb, 0.4) * glow * (0.45 + 0.35 * iEnergy);
    float a = max(inside, glow * (0.45 + 0.35 * iEnergy));
    return half4(half3(col * inside + g), half(a));
}
"""
