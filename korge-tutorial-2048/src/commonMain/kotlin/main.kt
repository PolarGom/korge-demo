import com.soywiz.klock.seconds
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.animate.Animator
import com.soywiz.korge.animate.animateSequence
import com.soywiz.korge.html.*
import com.soywiz.korge.input.*
import com.soywiz.korge.ui.uiText
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.font.*
import com.soywiz.korim.format.*
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.dynamic.KDynamic.Companion.keys
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korma.interpolation.Easing
import com.soywiz.korui.layout.Position
import kotlin.Number
import kotlin.properties.*
import kotlin.random.*
import com.soywiz.korge.tween.*

var cellSize: Double = 0.0
var fieldSize: Double = 0.0
var leftIndent: Double = 0.0
var topIndent: Double = 0.0
var font: BitmapFont by Delegates.notNull()
val blocks = mutableMapOf<Int, Block>()
var map = PositionMap()
var freeId = 0

var isAnimationRunning = false
var isGameOver = false

fun columnX(number: Int) = leftIndent + 10 + (cellSize + 10) * number
fun rowY(number: Int) = topIndent + 10 + (cellSize + 10) * number
fun numberFor(blockId: Int) = blocks[blockId]!!.number
fun deleteBlock(blockId: Int) = blocks.remove(blockId)!!.removeFromParent()

suspend fun main() = Korge(width = 480, height = 640, title = "2048", bgcolor = RGBA(253, 247, 240)) {

	font = resourcesVfs["clear_sans.fnt"].readBitmapFont()
	cellSize = views.virtualWidth / 5.0
	fieldSize = 50 + 4 * cellSize
	leftIndent = (views.virtualWidth - fieldSize) / 2
	topIndent = 150.0
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

	val font = resourcesVfs["clear_sans.fnt"].readBitmapFont()
	text("2048", cellSize * 0.5, Colors.WHITE, font).centerOn(bgLogo)

	text("BEST", 30.0, RGBA(239, 226, 210), font) {
		centerXOn(bgBest)
		alignTopToTopOf(bgBest, 5.0)
	}
	text("0", cellSize * 0.5, Colors.WHITE, font) {
		setTextBounds(Rectangle(0.0, 0.0, bgBest.width, cellSize - 24.0))
		alignment = TextAlignment.MIDDLE_CENTER
		alignTopToTopOf(bgBest, 12.0)
		centerXOn(bgBest)
	}

	text("SCORE", 30.0, RGBA(239, 226, 210), font) {
		centerXOn(bgScore)
		alignTopToTopOf(bgScore, 5.0)
	}
	text("0", cellSize * 0.5, Colors.WHITE, font) {
		setTextBounds(Rectangle(0.0, 0.0, bgScore.width, cellSize - 24.0))
		alignment = TextAlignment.MIDDLE_CENTER
		alignTopToTopOf(bgScore, 12.0)
		centerXOn(bgScore)
	}

	val restartImg = resourcesVfs["restart.png"].readBitmap()
	val undoImg = resourcesVfs["undo.png"].readBitmap()
	val btnSize = cellSize * 0.3
	val restartBlock = container {
		val background = roundRect(btnSize, btnSize, 5.0, fill = RGBA(185, 174, 160))
		image(restartImg) {
			size(btnSize * 0.8, btnSize * 0.8)
			centerOn(background)
		}
		alignTopToBottomOf(bgBest, 5)
		alignRightToRightOf(bgField)

		onClick {
			this@Korge.restart()
		}
	}
	val undoBlock = container {
		val background = roundRect(btnSize, btnSize, 5.0, fill = RGBA(185, 174, 160))
		image(undoImg) {
			size(btnSize * 0.6, btnSize * 0.6)
			centerOn(background)
		}
		alignTopToTopOf(restartBlock)
		alignRightToLeftOf(restartBlock, 5.0)
	}

	generateBlock()

	showGameOver {  }

	keys {
		down {
			when(it.key) {
				Key.LEFT -> moveBlocksTo(Direction.LEFT)
				Key.RIGHT  -> moveBlocksTo(Direction.RIGHT)
				Key.UP  -> moveBlocksTo(Direction.TOP)
				Key.DOWN  -> moveBlocksTo(Direction.BOTTOM)
				else -> Unit
			}
		}
	}

	onSwipe(20.0) {
		when (it.direction) {
			SwipeDirection.LEFT -> moveBlocksTo(Direction.LEFT)
			SwipeDirection.RIGHT -> moveBlocksTo(Direction.RIGHT)
			SwipeDirection.TOP -> moveBlocksTo(Direction.TOP)
			SwipeDirection.BOTTOM -> moveBlocksTo(Direction.BOTTOM)
		}
	}
}

fun Stage.moveBlocksTo(direction: Direction) {
	if ( isAnimationRunning ) return

	if ( !map.hasAvailableMoves() ) {
		if ( !isGameOver ) {
			isGameOver = true
			showGameOver {
				isGameOver = false
				restart()
			}
		}

		return
	}

	val moves = mutableListOf<Pair<Int, CustomPosition>>()
	val merges = mutableListOf<Triple<Int, Int, CustomPosition>>()

	val newMap = calculateNewMap(map.copy(), direction, moves, merges)

	if ( map != newMap ) {
		isAnimationRunning = true
		showAnimation(moves, merges) {
			map = newMap
			generateBlock()
			isAnimationRunning = false
		}
	}
}

fun Container.showGameOver(onRestart: () -> Unit) = container {
//	val format = TextFormat(color = RGBA(0, 0, 0), size = 40, font = font)
//	val skin = TextSkin(normal = format, over = format.copy(color = RGBA(90, 90, 90), down = format.copy(color = RGBA(120, 120, 120))))

	fun restart() {
		this@container.removeFromParent()
		onRestart()
	}

	position(leftIndent, topIndent)

	roundRect(fieldSize, fieldSize, 5.0, fill = Colors["FFFFFF33"])

	text("Game Over", 60.0, Colors.BLACK, font) {
		centerBetween(0.0, 0.0, fieldSize, fieldSize)
		y -= 60
	}

	text("Try again", 30.0, Colors.BLACK) {
		centerBetween(0.0, 0.0, fieldSize, fieldSize)
		y += 20
		onClick {
			restart()
		}
	}

	keys {
		down {
			when( it.key ) {
				Key.ENTER, Key.SPACE -> restart()
				else -> Unit
			}
		}
	}
}

fun Container.restart() {
	map = PositionMap()
	blocks.values.forEach { it.removeFromParent() }
	blocks.clear()
	generateBlock()
}

fun Container.generateBlock() {
	val position = map.getRandomFreePosition() ?: return
	val number = if ( Random.nextDouble() < 0.9 ) ENumber.ZERO else ENumber.ONE
	val newId = createNewBlock(number, position)
	map[position.x, position.y] = newId
}

fun Container.createNewBlock(number: ENumber, position: CustomPosition): Int {
	val id = freeId++
	createNewBlockWithId(id, number, position)
	return id
}

fun Container.createNewBlockWithId(id: Int, number: ENumber, position: CustomPosition) {
	blocks[id] = block(number).position(columnX(position.x), rowY(position.y))
}

fun Stage.showAnimation(
	moves: List<Pair<Int, CustomPosition>>,
	merges: List<Triple<Int, Int, CustomPosition>>,
	onEnd: () -> Unit
) = launchImmediately {
	animateSequence {
		parallel {
			moves.forEach { (id, pos) ->
				blocks[id]!!.moveTo(columnX(pos.x), rowY(pos.y), 0.15.seconds, Easing.LINEAR)
			}
			merges.forEach { (id1, id2, pos) ->
				sequence {
					parallel {
						blocks[id1]!!.moveTo(columnX(pos.x), rowY(pos.y), 0.15.seconds, Easing.LINEAR)
						blocks[id2]!!.moveTo(columnX(pos.x), rowY(pos.y), 0.15.seconds, Easing.LINEAR)
					}

					block {
						val nextNumber = numberFor(id1).next()
						deleteBlock(id1)
						deleteBlock(id2)
						createNewBlockWithId(id1, nextNumber, pos)
					}

					sequenceLazy {
						animateScale(blocks[id1]!!)
					}
				}
			}
		}

		block {
			onEnd()
		}
	}
}

fun Animator.animateScale(block: Block) {
	val x = block.x
	val y = block.y
	val scale = block.scale

	tween(
		block::x[x - 4],
		block::y[y - 4],
		block::scale[scale + 0.1],
		time = 0.1.seconds,
		easing = Easing.LINEAR
	)
	tween(
		block::x[x],
		block::y[y],
		block::scale[scale],
		time = 0.1.seconds,
		easing = Easing.LINEAR
	)
}

fun calculateNewMap(
	map: PositionMap,
	direction: Direction,
	moves: MutableList<Pair<Int, CustomPosition>>,
	merges: MutableList<Triple<Int, Int, CustomPosition>>
): PositionMap {
	val newMap = PositionMap()
	val startIndex = when (direction) {
		Direction.LEFT, Direction.TOP -> 0
		Direction.RIGHT, Direction.BOTTOM -> 3
	}

	var columnRow = startIndex

	fun newPosition(line: Int) = when (direction) {
		Direction.LEFT -> CustomPosition(columnRow++, line)
		Direction.RIGHT -> CustomPosition(columnRow--, line)
		Direction.TOP -> CustomPosition(line, columnRow++)
		Direction.BOTTOM -> CustomPosition(line, columnRow--)
	}

	for ( line in 0..3 ) {
		var curPos = map.getNotEmptyPositionFrom(direction, line)
		columnRow = startIndex
		while (curPos != null) {
			val newPos = newPosition(line)
			val curId = map[curPos.x, curPos.y]
			map[curPos.x, curPos.y] = -1

			val nextPos = map.getNotEmptyPositionFrom(direction, line)
			val nextId = nextPos?.let { map[it.x, it.y] }

			if ( nextId != null && numberFor(curId) == numberFor(nextId) ) {
				map[nextPos.x, nextPos.y] = -1
				newMap[newPos.x, newPos.y] = curId
				merges += Triple(curId, nextId, newPos)
			} else {
				newMap[newPos.x, newPos.y] = curId
				moves += Pair(curId, newPos)
			}

			curPos = map.getNotEmptyPositionFrom(direction, line)
		}
	}

	return newMap
}