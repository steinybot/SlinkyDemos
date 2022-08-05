import scala.sys.process.Process

lazy val `storybook-react` = project
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    externalNpm := {
      if (scala.util.Properties.isWin) Process("yarn", baseDirectory.value).run()
      else Process("bash -ci 'yarn'", baseDirectory.value).run()

      baseDirectory.value
    }
  )
