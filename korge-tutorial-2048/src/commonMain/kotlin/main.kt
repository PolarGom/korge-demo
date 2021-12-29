import com.soywiz.korge.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.vector.rect
import com.soywiz.korma.geom.vector.roundRect

suspend fun main() = Korge(width = 480, height = 640, title = "2048", bgcolor = RGBA(253, 247, 240)) {

	val cellSize = views.virtualWidth / 5.0
	val fieldSize = 50 + 4 * cellSize
	val leftIndent = (views.virtualWidth - fieldSize) / 2
	val topIndent = 150.0
	val bgField = RoundRect(fieldSize, fieldSize, 5.0, fill = Colors["#b9aea0"])
	bgField.x = leftIndent
	bgField.y = topIndent

	addChild(bgField)

	graphics {
		position(leftIndent, topIndent)
		fill(Colors["#cec0b2"]) {
			for ( y in 0..3 ) {
				for ( x in 0..3 ) {
					roundRect(10 + (10 + cellSize) * y, 10 + (10 + cellSize) * x, cellSize, cellSize, 5.0)
				}
			}
		}
	}

	val bgLogo = roundRect(cellSize, cellSize, 5.0, fill = Colors["#edc403"]) {
		position(leftIndent, 30.0)
	}

	val bgBest = roundRect(cellSize * 1.5, cellSize * 0.8, 5.0, fill = Colors["#bbae9e"]) {
		alignRightToRightOf(bgField)
		alignTopToTopOf(bgLogo)
	}

	val bgScore = roundRect(cellSize * 1.5, cellSize * 0.8, 5.0, fill = Colors["#bbae9e"]) {
		alignRightToLeftOf(bgBest, 24)
		alignTopToTopOf(bgBest)
	}
}