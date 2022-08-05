import scala.sys.process.Process

/**
 * Custom task to start demo with webpack-dev-server, use as `<project>/start`.
 * Just `start` also works, and starts all frontend demos
 *
 * After that, the incantation is this to watch and compile on change:
 * `~<project>/fastOptJS::webpack`
 */
lazy val start = TaskKey[Unit]("start")

/** Say just `dist` or `<project>/dist` to make a production bundle in
 * `docs` for github publishing
 */
lazy val dist = TaskKey[File]("dist")

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
    stFlavour := Flavour.Slinky,
    /** This is not suitable for development, but effective for demo.
      * Run `yarn storybook` commands yourself, and run `~storybook-react/fastOptJS` from sbt
      */
    start := {
      (Compile / fastOptJS).value
      if (scala.util.Properties.isWin) Process("yarn storybook", baseDirectory.value).run()
      else Process("bash -ci 'yarn storybook'", baseDirectory.value).run()
    },
    dist := {
      val distFolder = (ThisBuild / baseDirectory).value / "docs" / moduleName.value
      (Compile / fullOptJS).value
      if (scala.util.Properties.isWin) Process("yarn dist", baseDirectory.value).run()
      else Process("bash -ci 'yarn dist'", baseDirectory.value).run()
      distFolder
    }
  )

/** Note: This can't use scalajs-bundler (at least I don't know how),
  *  so we run yarn ourselves with an external package.json.
  */
lazy val `react-native` = project
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .configure(baseSettings)
  .settings(
    scalaJSLinkerConfig := scalaJSLinkerConfig.value.withModuleKind(ModuleKind.CommonJSModule),
    scalaJSUseMainModuleInitializer := false,
    /* ScalablyTypedConverterExternalNpmPlugin requires that we define how to install node dependencies and where they are */
    externalNpm := {
      if (scala.util.Properties.isWin) Process("yarn", baseDirectory.value).run()
      else Process("bash -ci 'yarn'", baseDirectory.value).run()
      baseDirectory.value
    },
    stFlavour := Flavour.SlinkyNative,
    stStdlib := List("es5"),
    stIgnore := List("url"),
    run := {
      (Compile / fastOptJS).value
      Process("expo start", baseDirectory.value).!
    }
  )
