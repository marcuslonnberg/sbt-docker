{
  sys.props.get("plugin.version") match {
    case Some(v) =>
      addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % v)
    case None =>
      sys.error("The system property 'plugin.version' is not defined. " +
        "Specify this property using the scriptedLaunchOpts -D.")
  }
}
