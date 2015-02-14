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
  /**
   * Parse a [[sbtdocker.ImageName]] from a string.
   */
  def apply(name: String): ImageName = {
    val (registry, rest) = name.split("/", 3).toList match {
      case host :: x :: xs if host.contains(".") || host.contains(":") || host == "localhost" =>
        (Some(host), x :: xs)
      case xs =>
        (None, xs)
    }

    val (namespace, repoAndTag) = rest match {
      case n :: r :: Nil =>
        (Some(n), r)
      case r :: Nil =>
        (None, r)
      case _ =>
        throw new IllegalArgumentException(s"Invalid image name: '$name'")
    }

    val (repo, tag) = repoAndTag.split(":", 2) match {
      case Array(r, t) =>
        (r, Some(t))
      case Array(r) =>
        (r, None)
    }

    ImageName(registry, namespace, repo, tag)
  }
}

/**
 * Name of a Docker image.
 * Format: [registry/][namespace/]repository[:tag]
 * Examples: `docker-registry.example.com/scala:2.11` or `example/scala:2.11`
 * @param repository Name of the repository.
 * @param registry Host and optionally port of the registry, example `docker-registry.example.com:5000`.
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

  @deprecated("Use toString instead.", "0.4.0")
  def name = toString
}
