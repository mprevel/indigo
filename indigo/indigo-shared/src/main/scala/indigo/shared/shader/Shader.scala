package indigo.shared.shader

import indigo.shaders.WebGL2Base

trait Shader {
  val id: ShaderId
  val vertex: String
  val fragment: String
}

object Shader {

  def fromCustomShader(customShader: CustomShader.Source): Shader =
    new Shader {
      val id: ShaderId     = customShader.id
      val vertex: String   = ShaderTemplates.webGL2Vertex(Some(customShader.vertex))
      val fragment: String = ShaderTemplates.webGL2Fragment(Some(customShader.fragment), None)
    }

  object ShaderTemplates {

    def webGL2Vertex(vertexShader: Option[String]): String =
      WebGL2Base.vertexShader(vertexShader)

    def webGL2Fragment(fragmentShader: Option[String], lightShader: Option[String]): String =
      WebGL2Base.fragmentShader(fragmentShader, lightShader)

  }

}