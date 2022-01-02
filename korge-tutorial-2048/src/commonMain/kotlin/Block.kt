import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors

class Block(val number: Number): Container() {
    init {
        roundRect(cellSize, cellSize, 5.0, fill = number.color)
        val textColor = when(number) {
            Number.ZERO, Number.ONE -> Colors.BLACK
            else -> Colors.WHITE
        }
        text(number.value.toString(), textSizeFor(number), textColor, font) {
            centerBetween(0.0, 0.0, cellSize, cellSize)
        }
    }

    private fun textSizeFor(number: Number) = when (number) {
        Number.ZERO, Number.ONE, Number.TWO, Number.THREE, Number.FOUR, Number.FIVE -> cellSize / 2
        Number.SIX, Number.SEVEN, Number.EIGHT -> cellSize * 4 / 9
        Number.NINE, Number.TEN, Number.ELEVEN, Number.TWELVE -> cellSize * 2 / 5
        Number.THIRTEEN, Number.FOURTEEN, Number.FIFTEEN -> cellSize * 7 / 20
        Number.SIXTEEN -> cellSize * 3 / 10
        else -> 0.0
    }

    fun Container.block(number: Number) = Block(number).addTo(this)
}