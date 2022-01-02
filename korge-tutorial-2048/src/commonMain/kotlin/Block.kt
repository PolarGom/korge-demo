import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors

fun Container.block(number: ENumber) = Block(number).addTo(this)

class Block(val number: ENumber): Container() {
    init {
        roundRect(cellSize, cellSize, 5.0, fill = number.color)
        val textColor = when(number) {
            ENumber.ZERO, ENumber.ONE -> Colors.BLACK
            else -> Colors.WHITE
        }
        text(number.value.toString(), textSizeFor(number), textColor, font) {
            centerBetween(0.0, 0.0, cellSize, cellSize)
        }
    }

    private fun textSizeFor(number: ENumber) = when (number) {
        ENumber.ZERO, ENumber.ONE, ENumber.TWO, ENumber.THREE, ENumber.FOUR, ENumber.FIVE -> cellSize / 2
        ENumber.SIX, ENumber.SEVEN, ENumber.EIGHT -> cellSize * 4 / 9
        ENumber.NINE, ENumber.TEN, ENumber.ELEVEN, ENumber.TWELVE -> cellSize * 2 / 5
        ENumber.THIRTEEN, ENumber.FOURTEEN, ENumber.FIFTEEN -> cellSize * 7 / 20
        ENumber.SIXTEEN -> cellSize * 3 / 10
        else -> 0.0
    }
}