package sbtdocker

object BuildOptions {

  object Remove {

    sealed trait Option

    case object Always extends Option

    case object OnSuccess extends Option

    case object Never extends Option

  }


  object Pull {

    sealed trait Option

    case object Always extends Option

    case object IfMissing extends Option

  }

}

/**
 * Options for when building a Docker image.
 * @param cache Use cache when building the image.
 * @param removeIntermediateContainers Remove intermediate containers after a build.
 * @param pullBaseImage Always attempts to pull a newer version of the base image.
 */
case class BuildOptions(
  cache: Boolean = true,
  removeIntermediateContainers: BuildOptions.Remove.Option = BuildOptions.Remove.OnSuccess,
  pullBaseImage: BuildOptions.Pull.Option = BuildOptions.Pull.IfMissing
)

class CreateOptions
{
  import scala.language.implicitConversions

  private [sbtdocker] var imageId: Option[String] = None
  private [sbtdocker] var exposes: Seq[Port] = Seq.empty
  private [sbtdocker] var ports: Seq[PortMap] = Seq.empty
  private [sbtdocker] var env: Seq[(String, String)] = Seq.empty


  def image(id: String): Unit = imageId = Some(id)

  def expose(port: Port): Unit = exposes = exposes :+ port

  def port(portMap: PortMap): Unit = ports = ports :+ portMap
  def port(ip: IPAddress, hp: Port, cp: Port): Unit =
    port(PortMap(hostIp = Some(ip), hostPort = Some(hp), containerPort = cp))
  def port(ip: IPAddress, cp: Port): Unit =
    port(PortMap(hostIp = Some(ip), hostPort = None, containerPort = cp))
  def port(hp: Port, cp: Port): Unit =
    port(PortMap(hostPort = Some(hp), containerPort = cp))

  def env(pairs: (String, String)*): Unit = env = env ++ pairs

  implicit def intToPort(i: Int): Port = Port(i)
  implicit def stringToIpAddress(s: String): IPAddress = s match {
    case IPAddress(ip) => ip
    case _ => sys.error(s"Could not parse '$s' as an ip address")
  }
  implicit def stringToPortMap(s: String): PortMap = s match {
    case PortMap(pm) => pm
    case _ => sys.error(s"Could not parse '$s' as a port mapping")
  }

  override def toString = s"CreateOptions(imageId=$imageId, exposes=$exposes, ports=$ports, env=$env)"
}

class StartOptions
{
  private [sbtdocker] var containers: Seq[ContainerId] = Seq.empty

  def container(cid: ContainerId*): Unit = containers = containers ++ cid

  override def toString = s"StartOptions(containers=$containers)"
}

case class PortMap(
  hostIp: Option[IPAddress] = None,
  hostPort: Option[Port] = None,
  containerPort: Port,
  isUDP: Boolean = false
) {
  override def toString = {
    val udp = if(isUDP) "/udb" else ""
    hostIp match {
      case Some(hip) =>
        hostPort match {
          case Some(hp) =>
            s"$hip:$hp:$containerPort$udp"
          case None =>
            s"$hip::$containerPort$udp"
        }
      case None =>
        hostPort match {
          case Some(hp) =>
            s"$hp:$containerPort$udp"
          case None =>
            s"$containerPort$udp"
        }
    }

  }
}

object PortMap {
  object ProcessMapping {
    def unapply(mapping: String): Option[PortMap] = mapping split ":" match {
      case Array(IPAddress(hostIp), Port(hostPort), Port(containerPort)) =>
        Some(PortMap(hostIp = Some(hostIp), hostPort = Some(hostPort), containerPort = containerPort))
      case Array(IPAddress(hostIp), "", Port(containerPort)) =>
        Some(PortMap(hostIp = Some(hostIp), containerPort = containerPort))
      case Array(Port(hostPort), Port(containerPort)) =>
        Some(PortMap(hostPort = Some(hostPort), containerPort = containerPort))
      case Array(Port(containerPort)) =>
        Some(PortMap(containerPort = containerPort))
      case _ => None
    }
  }



  def unapply(pm: String): Option[PortMap] = {
    pm split "/" match {
      case Array(ProcessMapping(mapping), "udp") =>
        Some(mapping.copy(isUDP = true))
      case Array(ProcessMapping(mapping)) =>
        Some(mapping)
      case _ =>
        None
    }
  }
}

case class IPAddress(
  d1: IPDigit,
  d2: IPDigit,
  d3: IPDigit,
  d4: IPDigit)
{
  override def toString = s"$d1.$d2.$d3.$d4"
}

object IPAddress {
  def unapply(txt: String): Option[IPAddress] = txt split '.' match {
    case Array(IPDigit(d1), IPDigit(d2), IPDigit(d3), IPDigit(d4)) =>
      Some(IPAddress(d1, d2, d3, d4))
    case _ =>
      None
  }
}

case class IPDigit(digit: Short) // 8 bit unsigned
{
  if(digit < 0 || digit > 255)
    throw new IllegalArgumentException(s"IP digit must be in the range 0..255 but was '$digit'")

  override def toString = digit.toString
}

object IPDigit {
  val IPDigitRx = """(\d+)""".r
  def unapply(txt: String): Option[IPDigit] = txt match {
    case IPDigitRx(d) =>
      d.toShort match {
        case di if di < 256 =>
          Some(IPDigit(di))
        case _ => None
      }
    case _ => None
  }
}

case class Port(number: Int) // 16 bit unsigned
{
  if(number < 0 || number > 65535)
    throw new IllegalArgumentException(s"Port must be in the range 0..65535 but was '$number'")

  override def toString = number.toString
}

object Port {
  private val PortRx = """(\d+)""".r

  def unapply(txt: String): Option[Port] = txt match {
    case PortRx(n) =>
      n.toInt match {
        case i if i <= 65535 =>
          Some(Port(i))
        case _ =>
          None
      }
    case _ => None
  }
}

/**
 * Id of an Docker image.
 * @param id Id as a hexadecimal digit string.
 */
case class ImageId(id: String) {
  override def toString = id
}

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
case class ImageName(
  registry: Option[String] = None,
  namespace: Option[String] = None,
  repository: String,
  tag: Option[String] = None
) {
  override def toString = {
    val registryString = registry.fold("")(_ + "/")
    val namespaceString = namespace.fold("")(_ + "/")
    val tagString = tag.fold("")(":" + _)
    registryString + namespaceString + repository + tagString
  }

  @deprecated("Use toString instead.", "0.4.0")
  def name = toString
}

case class ContainerId(id: String) {
  override def toString = id
}

object ContainerId {
  private val ContainerIdRx = """([0-9a-f]+)""".r
  /**
    * Parse a [[sbtdocker.ContainerId]] from a string.
    */
  def unapply(id: String): Option[ContainerId] = id match {
    case ContainerIdRx(i) =>
      Some(ContainerId(i))
    case _ =>
      None
  }
}