import sbt.Keys.testOptions

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name          := """Comments""",
    version       := "0.0.1",
    scalaVersion  := "2.13.1",

    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.play"       %% "play-ws"         % "2.8.2",
      "com.typesafe.play"       %% "play-slick"      % "5.0.0",
      "com.typesafe.play"       %% "play-json"       % "2.9.0",
      "org.json4s"              %% "json4s-jackson"  % "3.6.8",
      "org.json4s"              %% "json4s-ext"      % "3.6.8",
      "org.postgresql"          %  "postgresql"      % "42.2.13",
      "org.scalacheck"          %% "scalacheck"      % "1.14.3",
      specs2 % Test,
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings",
      "-language:implicitConversions"
    ),
    testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v", "-s")

  )
