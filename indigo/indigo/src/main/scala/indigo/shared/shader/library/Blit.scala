package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object Blit:

  case class IndigoBitmapData(FILLTYPE: highp[Float])

  object fragment:
    inline def shader =
      Shader[IndigoFragmentEnv & IndigoBitmapData, vec4] { env =>
        import TileAndStretch.*

        ubo[IndigoBitmapData]

        env.CHANNEL_0 = tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_0,
          env.SRC_CHANNEL,
          env.CHANNEL_0_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )
        env.CHANNEL_1 = tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_1,
          env.SRC_CHANNEL,
          env.CHANNEL_1_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )
        env.CHANNEL_2 = tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_2,
          env.SRC_CHANNEL,
          env.CHANNEL_2_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )
        env.CHANNEL_3 = tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_3,
          env.SRC_CHANNEL,
          env.CHANNEL_3_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )

        env.CHANNEL_0;
      }

    inline def asFragment =
      Shader[IndigoFragmentEnv & IndigoBitmapData] { env =>
        def fragment: vec4 =
          shader.run(env)
      }

    val output = asFragment.toGLSL[Indigo]