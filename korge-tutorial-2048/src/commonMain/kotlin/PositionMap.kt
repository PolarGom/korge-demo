import com.soywiz.kds.*
import kotlin.random.*

class CustomPosition(val x: Int, val y: Int)

enum class Direction {
    LEFT, RIGHT, TOP, BOTTOM
}

class PositionMap(private val array: IntArray2 = IntArray2(4, 4, -1)) {

    private fun getOrNull(x: Int, y: Int) = if (array.get(x, y) != -1) CustomPosition(x, y) else null

    private fun getNumber(x: Int, y: Int) = array.tryGet(x, y)?.let { blocks[it]?.number?.ordinal ?: -1 } ?: -1

    fun getRandomFreePosition(): CustomPosition? {
        val quantity = array.count { it == -1 }
        if (quantity == 0) return null
        val chosen = Random.nextInt(quantity)
        var current = -1
        array.each { x, y, value ->
            if (value == -1) {
                current++
                if (current == chosen) {
                    return CustomPosition(x, y)
                }
            }
        }
        return null
    }

    operator fun get(x: Int, y: Int) = array[x, y]

    operator fun set(x: Int, y: Int, value: Int) {
        array[x, y] = value
    }

    fun forEach(action: (Int) -> Unit) { array.forEach(action) }

    override fun equals(other: Any?): Boolean {
        return (other is PositionMap) && this.array.data.contentEquals(other.array.data)
    }
    override fun hashCode() = array.hashCode()
}