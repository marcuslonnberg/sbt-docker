package sbtdocker

/**
 * Options for the docker build command.
 * @param noCache Do not use cache when building the image.
 * @param rm Remove intermediate containers after a successful build.
 */
case class BuildOptions(noCache: Option[Boolean] = None, rm: Option[Boolean] = None)

/**
 * Id of an Docker image.
 * @param id Id as a hexadecimal digit string.
 */
case class ImageId(id: String)

object ImageName {
  def apply(repository: String) = new ImageName(repository = repository)
}

/**
 * Name of a Docker image.
 * Format: [registry/][namespace/]repository[:tag]
 * Examples: docker-registry.company.com/scala:2.11 or company/scala:2.11
 * @param repository Name of the repository.
 * @param registry Registry domain.
 * @param namespace Namespace name.
 * @param tag Tag, for example a version number.
 */
case class ImageName(registry: Option[String] = None, namespace: Option[String] = None, repository: String, tag: Option[String] = None) {
  override def toString = {
    val registryString = registry.fold("")(_ + "/")
    val namespaceString = namespace.fold("")(_ + "/")
    val tagString = tag.fold("")(":" + _)
    registryString + namespaceString + repository + tagString
  }
}
