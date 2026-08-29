package com.alextos.thousand.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alextos.thousand.domain.models.Die
import com.alextos.thousand.domain.models.DieValue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SpriteDiceView(
    die: Die,
    animate: Boolean,
    animationIndex: Int = 0,
    animationKey: Int = 0,
    onAnimationFinished: () -> Unit = {},
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    val rotationX = remember { Animatable(0f) }
    val rotationY = remember { Animatable(0f) }
    val rotationZ = remember { Animatable(0f) }

    LaunchedEffect(die, animate, animationIndex, animationKey) {
        rotationX.snapTo(0f)
        rotationY.snapTo(0f)
        rotationZ.snapTo(0f)
        if (!animate) {
            onAnimationFinished()
            return@LaunchedEffect
        }

        val duration = 700 + ((animationIndex * 173 + die.value.value * 97) % 550)
        val profile = rollProfile(animationIndex, die.value)
        coroutineScope {
            launch {
                rotationX.animateTo(profile.xTurns * 360f, tween(duration, easing = FastOutSlowInEasing))
            }
            launch {
                rotationY.animateTo(profile.yTurns * 360f, tween(duration, easing = FastOutSlowInEasing))
            }
            launch {
                rotationZ.animateTo(profile.zTurns * 360f, tween(duration, easing = FastOutSlowInEasing))
            }
        }
        rotationX.snapTo(0f)
        rotationY.snapTo(0f)
        rotationZ.snapTo(0f)
        onAnimationFinished()
    }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Кубик, значение ${die.value.value}" },
    ) {
        drawCube(die.value, rotationX.value, rotationY.value, rotationZ.value)
    }
}

private data class RollProfile(
    val xTurns: Int,
    val yTurns: Int,
    val zTurns: Int,
)

private fun rollProfile(index: Int, value: DieValue): RollProfile {
    val seed = index * 7 + value.value
    val direction = if (seed % 2 == 0) 1 else -1
    return RollProfile(
        xTurns = direction * (2 + (seed % 3)),
        yTurns = -direction * (2 + ((seed + 1) % 3)),
        zTurns = direction * (1 + ((seed + 2) % 3)),
    )
}

private fun DrawScope.drawCube(value: DieValue, rotationX: Float, rotationY: Float, rotationZ: Float) {
    val rotated = cubeVertices.map { it.rotate(rotationX, rotationY, rotationZ) }
    val cubeScale = min(size.width, size.height) / 2.55f
    val center = Offset(size.width / 2f, size.height / 2f - cubeScale * 0.02f)
    val projected = rotated.map { point ->
        val perspective = 1f / (1f + point.z * 0.1f)
        Offset(center.x + point.x * cubeScale * perspective, center.y - point.y * cubeScale * perspective)
    }

    drawOval(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(center.x - cubeScale * 0.62f, center.y + cubeScale * 0.66f),
        size = Size(cubeScale * 1.25f, cubeScale * 0.22f),
    )

    faces(value)
        .map { face -> face.withDepth(rotated, projected) }
        .filter { face -> face.normal(rotated).z > 0.08f }
        .sortedBy { it.depth }
        .forEach { face ->
            val path = Path().apply {
                moveTo(face.points[0].x, face.points[0].y)
                face.points.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }
            drawPath(path, colorFor(face.normal(rotated).z))
            drawPath(path, Color.Black, style = Stroke(width = 2f))
            drawPips(face)
        }
}

private fun DrawScope.drawPips(face: ProjectedFace) {
    val radius = max(1.5f, min(size.width, size.height) * 0.065f)
    val pipInset = 0.56f
    pipPositions(face.value).forEach { (x, y) ->
        val u = (x * pipInset + 1f) / 2f
        val v = (y * pipInset + 1f) / 2f
        val top = lerp(face.points[0], face.points[1], u)
        val bottom = lerp(face.points[3], face.points[2], u)
        val point = lerp(top, bottom, v)
        drawCircle(Color.Black, radius, point)
    }
}

private fun lerp(a: Offset, b: Offset, amount: Float) = Offset(
    x = a.x + (b.x - a.x) * amount,
    y = a.y + (b.y - a.y) * amount,
)

private fun colorFor(normalZ: Float) = when {
    normalZ > 0.7f -> Color(0xFFFFFFFF)
    normalZ > 0.25f -> Color(0xFFE8E8E8)
    else -> Color(0xFFCECECE)
}

private fun pipPositions(value: DieValue): List<Pair<Float, Float>> = when (value) {
    DieValue.ONE -> listOf(0f to 0f)
    DieValue.TWO -> listOf(-1f to -1f, 1f to 1f)
    DieValue.THREE -> listOf(-1f to -1f, 0f to 0f, 1f to 1f)
    DieValue.FOUR -> listOf(-1f to -1f, 1f to -1f, -1f to 1f, 1f to 1f)
    DieValue.FIVE -> listOf(-1f to -1f, 1f to -1f, 0f to 0f, -1f to 1f, 1f to 1f)
    DieValue.SIX -> listOf(-1f to -1f, 1f to -1f, -1f to 0f, 1f to 0f, -1f to 1f, 1f to 1f)
}

private data class ProjectedFace(
    val indices: List<Int>,
    val value: DieValue,
    val depth: Float = 0f,
    val points: List<Offset> = emptyList(),
) {
    fun withDepth(vertices: List<Point3>, projected: List<Offset>) = copy(
        depth = indices.map { vertices[it].z }.average().toFloat(),
        points = indices.map { projected[it] },
    )

    fun normal(vertices: List<Point3>): Point3 {
        val a = vertices[indices[0]]
        val b = vertices[indices[1]]
        val c = vertices[indices[2]]
        return (b - a).cross(c - a).normalized()
    }
}

private fun faces(front: DieValue) = listOf(
    ProjectedFace(listOf(4, 5, 6, 7), front),
    ProjectedFace(listOf(1, 0, 3, 2), opposite(front)),
    ProjectedFace(listOf(0, 1, 5, 4), DieValue.TWO),
    ProjectedFace(listOf(3, 7, 6, 2), DieValue.FIVE),
    ProjectedFace(listOf(1, 2, 6, 5), DieValue.THREE),
    ProjectedFace(listOf(0, 4, 7, 3), DieValue.FOUR),
)

private val cubeVertices = listOf(
    Point3(-1f, -1f, -1f), Point3(1f, -1f, -1f), Point3(1f, 1f, -1f), Point3(-1f, 1f, -1f),
    Point3(-1f, -1f, 1f), Point3(1f, -1f, 1f), Point3(1f, 1f, 1f), Point3(-1f, 1f, 1f),
)

private fun opposite(value: DieValue) = when (value) {
    DieValue.ONE -> DieValue.SIX
    DieValue.TWO -> DieValue.FIVE
    DieValue.THREE -> DieValue.FOUR
    DieValue.FOUR -> DieValue.THREE
    DieValue.FIVE -> DieValue.TWO
    DieValue.SIX -> DieValue.ONE
}

private data class Point3(val x: Float, val y: Float, val z: Float)

private operator fun Point3.plus(other: Point3) = Point3(x + other.x, y + other.y, z + other.z)
private operator fun Point3.minus(other: Point3) = Point3(x - other.x, y - other.y, z - other.z)
private operator fun Point3.div(value: Float) = Point3(x / value, y / value, z / value)

private fun Point3.cross(other: Point3) = Point3(
    y * other.z - z * other.y,
    z * other.x - x * other.z,
    x * other.y - y * other.x,
)

private fun Point3.normalized(): Point3 {
    val length = max(0.001f, sqrt(x * x + y * y + z * z))
    return this / length
}

private fun Point3.rotate(xDegrees: Float, yDegrees: Float, zDegrees: Float): Point3 {
    val xAngle = xDegrees * PI.toFloat() / 180f
    val yAngle = yDegrees * PI.toFloat() / 180f
    val zAngle = zDegrees * PI.toFloat() / 180f
    val cosX = cos(xAngle)
    val sinX = sin(xAngle)
    val cosY = cos(yAngle)
    val sinY = sin(yAngle)
    val cosZ = cos(zAngle)
    val sinZ = sin(zAngle)
    val rotatedY = y * cosX - z * sinX
    val rotatedZ = y * sinX + z * cosX
    val rotatedX = x * cosY + rotatedZ * sinY
    val finalZ = -x * sinY + rotatedZ * cosY
    return Point3(
        rotatedX * cosZ - rotatedY * sinZ,
        rotatedX * sinZ + rotatedY * cosZ,
        finalZ,
    )
}
