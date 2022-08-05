import scala.sys.process.Process

lazy val baseSettings: Project => Project =
  _.enablePlugins(ScalaJSPlugin)
    .settings(
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.13.2",
      scalacOptions ++= ScalacOptions.flags,
      scalaJSUseMainModuleInitializer := true,
      /* disabled because it somehow triggers many warnings */
      scalaJSLinkerConfig := scalaJSLinkerConfig.value.withSourceMap(false),
      /* for slinky */
      libraryDependencies ++= Seq("me.shadaj" %%% "slinky-hot" % "0.6.5"),
      scalacOptions += "-Ymacro-annotations"
    )

/** Note: This can't use scalajs-bundler (at least I don't know how),
  *  so we run yarn ourselves with an external package.json.
  */
lazy val `storybook-react` = project
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .configure(baseSettings)
  .settings(
    scalaJSLinkerConfig := scalaJSLinkerConfig.value.withModuleKind(ModuleKind.CommonJSModule),
    /* ScalablyTypedConverterExternalNpmPlugin requires that we define how to install node dependencies and where they are */
    externalNpm := {
      if (scala.util.Properties.isWin) Process("yarn", baseDirectory.value).run()
      else Process("bash -ci 'yarn'", baseDirectory.value).run()

      baseDirectory.value
    },
    stFlavour := Flavour.Slinky
  )
