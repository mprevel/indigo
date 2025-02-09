package com.example.sandbox

import com.example.sandbox.scenes.*
import example.TestFont
import indigo.*
import indigo.json.Json
import indigo.scenes.*
import indigo.syntax.*
import indigoextras.effectmaterials.LegacyEffects
import indigoextras.effectmaterials.Refraction
import indigoextras.subsystems.FPSCounter

import scala.scalajs.js.annotation.*

@JSExportTopLevel("IndigoGame")
object SandboxGame extends IndigoGame[SandboxBootData, SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  val magnificationLevel: Int = 2
  val gameWidth: Int          = 228
  val gameHeight: Int         = 128
  val viewportWidth: Int      = gameWidth * magnificationLevel  // 456
  val viewportHeight: Int     = gameHeight * magnificationLevel // 256

  def initialScene(bootData: SandboxBootData): Option[SceneName] =
    Some(OriginalScene.name)

  def scenes(bootData: SandboxBootData): NonEmptyList[Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]] =
    NonEmptyList(
      OriginalScene,
      ShapesScene,
      LightsScene,
      RefractionScene,
      LegacyEffectsScene,
      TextBoxScene,
      BoundsScene,
      CameraScene,
      TextureTileScene,
      ConfettiScene,
      MutantsScene,
      CratesScene,
      ClipScene,
      TextScene,
      BoxesScene,
      ManyEventHandlers,
      TimelineScene,
      UltravioletScene,
      PointersScene,
      BoundingCircleScene,
      LineReflectionScene,
      CameraWithCloneTilesScene,
      PathFindingScene,
      CaptureScreenScene,
      NineSliceScene,
      ComponentUIScene,
      MeshScene
    )

  val eventFilters: EventFilters = EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[SandboxBootData, SandboxGameModel]] = {
    val gameViewport =
      (flags.get("width"), flags.get("height")) match {
        case (Some(w), Some(h)) =>
          GameViewport(w.toInt, h.toInt)

        case _ =>
          GameViewport(viewportWidth, viewportHeight)
      }

    Outcome(
      BootResult(
        GameConfig(
          viewport = gameViewport,
          clearColor = RGBA(0.4, 0.2, 0.5, 1),
          magnification = magnificationLevel
        ),
        SandboxBootData(flags.getOrElse("key", "No entry for 'key'."), gameViewport)
      ).withAssets(
        SandboxAssets.assets ++
          Shaders.assets ++
          Archetype.assets
      ).withFonts(
        Fonts.fontInfo,
        TestFont.fontInfo
      ).withSubSystems(
        FPSCounter[SandboxGameModel](
          Point(5, 165),
          LayerKey("fps counter")
        )
      ).withShaders(
        Shaders.circle,
        Shaders.external,
        Shaders.sea,
        LegacyEffects.entityShader,
        Archetype.shader,
        UVShaders.circle,
        UVShaders.voronoi,
        UVShaders.redBlend
      ).addShaders(Refraction.shaders)
    )
  }

  def setup(
      bootData: SandboxBootData,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[SandboxStartupData]] = {
    println(bootData.message)

    val screenCenter: Point =
      bootData.gameViewport.giveDimensions(magnificationLevel).center

    def makeStartupData(
        aseprite: Aseprite,
        spriteAndAnimations: SpriteAndAnimations,
        clips: Map[CycleLabel, Clip[Material.Bitmap]]
    ): Startup.Success[SandboxStartupData] =
      Startup
        .Success(
          SandboxStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withRef(16, 16)      // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(screenCenter) // Also place him in the middle of the screen initially
                .withMaterial(SandboxAssets.dudeMaterial),
              clips
            ),
            screenCenter,
            bootData.gameViewport
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[SandboxStartupData]] = for {
      json                <- assetCollection.findTextDataByName(AssetName(SandboxAssets.dudeName.toString + "-json"))
      aseprite            <- Json.asepriteFromJson(json)
      spriteAndAnimations <- aseprite.toSpriteAndAnimations(dice, SandboxAssets.dudeName)
      clips               <- aseprite.toClips(SandboxAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations, clips)

    Outcome(res.getOrElse(Startup.Failure("Failed to load the dude")))
  }

  def initialModel(startupData: SandboxStartupData): Outcome[SandboxGameModel] =
    Outcome(SandboxModel.initialModel(startupData))

  def initialViewModel(startupData: SandboxStartupData, model: SandboxGameModel): Outcome[SandboxViewModel] =
    Outcome(
      SandboxViewModel(
        Point.zero,
        true,
        CaptureScreenScene.ViewModel(None, None, Point.zero)
      )
    )

  def updateModel(
      context: Context[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    SandboxModel.updateModel(model)

  def updateViewModel(
      context: Context[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] = {
    case RendererDetails(RenderingTechnology.WebGL1, _, _) =>
      Outcome(viewModel.copy(useLightingLayer = false))

    case FrameTick =>
      val updateOffset: Point =
        context.frame.input.gamepad.dpad match {
          case GamepadDPad(true, _, _, _) =>
            viewModel.offset + Point(0, -1)

          case GamepadDPad(_, true, _, _) =>
            viewModel.offset + Point(0, 1)

          case GamepadDPad(_, _, true, _) =>
            viewModel.offset + Point(-1, 0)

          case GamepadDPad(_, _, _, true) =>
            viewModel.offset + Point(1, 0)

          case _ =>
            viewModel.offset
        }

      Outcome(viewModel.copy(offset = updateOffset))

    case FullScreenEntered =>
      println("Entered full screen mode")
      Outcome(viewModel)

    case FullScreenExited =>
      println("Exited full screen mode")
      Outcome(viewModel)

    case KeyboardEvent.KeyDown(Key.PAGE_UP) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.LoopNext)

    case KeyboardEvent.KeyDown(Key.PAGE_DOWN) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.LoopPrevious)

    case KeyboardEvent.KeyDown(Key.HOME) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.First)

    case KeyboardEvent.KeyDown(Key.END) =>
      Outcome(viewModel)
        .addGlobalEvents(SceneEvent.Last)

    case _ =>
      Outcome(viewModel)
  }

  def present(
      context: Context[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        "bg".toLayerKey   -> Layer.Stack.empty,
        "game".toLayerKey -> Layer.Stack.empty,
        "fps counter".toLayerKey ->
          Layer.empty
            .withCamera(Camera.default)
      )
    )

final case class Dude(
    aseprite: Aseprite,
    sprite: Sprite[Material.ImageEffects],
    clips: Map[CycleLabel, Clip[Material.Bitmap]]
)
final case class SandboxBootData(message: String, gameViewport: GameViewport)
final case class SandboxStartupData(dude: Dude, viewportCenter: Point, gameViewport: GameViewport)
final case class SandboxViewModel(
    offset: Point,
    useLightingLayer: Boolean,
    captureScreenScene: CaptureScreenScene.ViewModel
)

final case class Log(message: String) extends GlobalEvent
