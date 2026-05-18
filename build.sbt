

lazy val appName: String = "overseas-pension-transfer-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.7"

addCommandAlias("runAllChecks", ";clean;compile;scalafmtAll;scalastyle;coverage;test;it/test;coverageReport")

val commonSettings: Seq[String] = Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:noAutoTupling",
  "-Wvalue-discard",
  "-Werror",
  "-Wconf:src=routes/.*:s",
  "-Wunused:unsafe-warn-patvars",
  "-Wconf:msg=Flag.*repeatedly:s"
)

/*
Seq(
      "-feature",
      "-Wconf:cat=deprecation:ws,cat=feature:ws,cat=optimizer:ws,src=target/.*:s"
    )
    
Seq("-unchecked", "-deprecation")
 */

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings) *)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "viewmodels.govuk.all._"
    ),
    PlayKeys.playDefaultPort := 15600,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*components.*;" +
      ".*Routes.*;.*viewmodels.govuk.*;.*views.html.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 78,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= commonSettings,
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    pipelineStages := Seq(digest),
    Assets / pipelineStages := Seq(concat),
    scalafmtOnCompile := true
  )

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")
    .settings(scalacOptions ++= commonSettings)
