import com.soywiz.korge.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*

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
		RoundRect(10.0, 10.0, cellSize, cellSize, 5.0)
		/*fill(Colors["#cec0b2"]) {
			val field = RoundRect
		}*/
	}
}