package indigo.platform.assets

import indigo.facades.IndigoCanvasRenderingContext2D
import indigo.facades.IndigoTextMetrics
import indigo.shared.datatypes.Font
import indigo.shared.datatypes.FontFamily
import indigo.shared.datatypes.Pixels
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.TextAlign
import indigo.shared.datatypes.TextStyle
import org.scalajs.dom
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.html

import scala.scalajs.js.Dynamic

final class DynamicText:

  private lazy val textContext: IndigoCanvasRenderingContext2D = createTextContext()

  private val toFontStatement: Font => String = f =>
    val style   = f.style.toCSS
    val variant = f.variant.toCSS
    val weight  = f.weight.toCSS
    s"$style $variant $weight ${f.size.toInt}px ${f.family.name}"

  private def setupText(style: TextStyle, width: Int, height: Int): Unit =
    textContext.canvas.width = width
    textContext.canvas.height = height
    textContext.font = toFontStatement(style.font)
    textContext.textAlign = style.alignment.toCSS
    textContext.textBaseline = style.baseLine.toCSS
    textContext.direction = style.direction.toCSS

    textContext.miterLimit = 2
    textContext.lineJoin = "round"
    textContext.strokeStyle = style.stroke.color.toHexString("#")
    textContext.lineWidth = style.stroke.width.toInt
    textContext.fillStyle = style.color.toHexString("#")

    textContext.clearRect(0, 0, width, height)

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def createTextContext(): IndigoCanvasRenderingContext2D =
    dom.document
      .createElement("canvas")
      .asInstanceOf[html.Canvas]
      .getContext("2d", Dynamic.literal())
      .asInstanceOf[IndigoCanvasRenderingContext2D]

  def makeTextImageData(text: String, style: TextStyle, width: Int, height: Int): HTMLCanvasElement =
    setupText(style, width, height)

    val x: Int =
      style.alignment match
        case TextAlign.Left   => 0
        case TextAlign.Right  => width
        case TextAlign.Center => width / 2
        case TextAlign.Start  => 0
        case TextAlign.End    => width
    val y = style.font.size.toInt

    if style.scaleTextToFit then
      if style.stroke.width.toInt > 0 then textContext.strokeText(text, x, y, width)
      textContext.fillText(text, x, y, width)
    else
      if style.stroke.width.toInt > 0 then textContext.strokeText(text, x, y)
      textContext.fillText(text, x, y)

    textContext.canvas

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def measureText(text: String, style: TextStyle, width: Int, height: Int): Rectangle =
    setupText(style, width, height)

    val textMetrics: IndigoTextMetrics =
      textContext.measureText(text).asInstanceOf[IndigoTextMetrics]

    val x: Int =
      style.alignment match
        case TextAlign.Left   => 0
        case TextAlign.Right  => width
        case TextAlign.Center => width / 2
        case TextAlign.Start  => 0
        case TextAlign.End    => width
    val y = style.font.size.toInt

    Rectangle(
      x = x,
      y = y,
      width = (Math.abs(textMetrics.actualBoundingBoxLeft) + Math.abs(textMetrics.actualBoundingBoxRight)).toInt,
      height = (Math.abs(textMetrics.actualBoundingBoxAscent) + Math.abs(textMetrics.actualBoundingBoxAscent)).toInt
    )
