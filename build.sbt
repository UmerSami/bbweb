import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

name := "bbweb"

organization in ThisBuild := "org.biobank"

version := "0.1-SNAPSHOT"

def excludeSpecs2(module: ModuleID): ModuleID =
  module.excludeAll(ExclusionRule(organization = "org.specs2", name = "specs2"))
    .exclude("com.novocode", "junit-interface")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(libraryDependencies ~= (_.map(excludeSpecs2)))

scalaVersion := Option(System.getProperty("scala.version")).getOrElse("2.11.7")

scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "deprecation",        // warning and location for usages of deprecated APIs
  "-feature",           // warning and location for usages of features that should be imported explicitly
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-unchecked",          // additional warnings where generated code depends on assumptions
  "-Xlint",
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused
)

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")

//javaOptions ++= Seq("-Xmx1024M", "-XX:MaxPermSize=512m")

javaOptions in Test ++=  Seq(
  "-Dconfig.file=conf/test.conf",
  "-Dlogger.resource=logback-test.xml"
)

javacOptions in ThisBuild  ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint"
)

testOptions in Test := Nil

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/report")

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")

resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS"        at "https://oss.sonatype.org/content/repositories/releases",
  "Akka Snapshots"      at "http://repo.akka.io/snapshots/",
  "dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"
)

libraryDependencies ++= Seq(
  cache,
  filters,
  ( "com.typesafe.akka"         %% "akka-persistence"                   % "2.4.1" % "compile" ).excludeAll(ExclusionRule(organization="com.google.protobuf")),
  "com.typesafe.akka"         %% "akka-persistence-query-experimental" % "2.4.1",
  "org.mongodb"               %% "casbah"                              % "2.8.2"             % "compile",
  "com.github.scullxbones"    %% "akka-persistence-mongo-casbah"       % "1.1.9"             % "compile",
  "com.typesafe.akka"         %% "akka-remote"                         % "2.4.1"             % "compile",
  "com.typesafe.akka"         %% "akka-slf4j"                          % "2.4.1"             % "compile",
  "org.scala-stm"             %% "scala-stm"                           % "0.7"               % "compile",
  "org.scalaz"                %% "scalaz-core"                         % "7.2.0"             % "compile",
  "org.iq80.leveldb"          % "leveldb"                              % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all"                       % "1.8",
  "com.github.t3hnar"         %% "scala-bcrypt"                        % "2.5",
  "com.typesafe.play"         %% "play-mailer"                         % "3.0.1",
  // WebJars infrastructure
  ( "org.webjars"               %% "webjars-play"                       % "2.4.0-1").exclude("org.webjars", "requirejs"),
  // WebJars dependencies
  "org.webjars"               %  "requirejs"                           % "2.1.22",
  "org.webjars"               %  "underscorejs"                        % "1.8.3",
  "org.webjars"               %  "jquery"                              % "2.2.0",
  ( "org.webjars"             %  "bootstrap"                           % "3.3.6"  ).excludeAll(ExclusionRule(organization="org.webjars")),
  ( "org.webjars"             %  "angularjs"                           % "1.4.9"  ).exclude("org.webjars", "jquery"),
  ( "org.webjars"             %  "angular-ui-bootstrap"                % "0.14.3" ).exclude("org.webjars", "angularjs"),
  ( "org.webjars"             %  "angular-ui-router"                   % "0.2.17" ).exclude("org.webjars", "angularjs"),
  "org.webjars"               % "smart-table"                          % "2.1.3-1",
  ( "org.webjars"             %  "toastr"                              % "2.1.1"  ).exclude("org.webjars", "jquery"),
  ( "org.webjars"             %  "angular-sanitize"                    % "1.3.11" ).exclude("org.webjars", "angularjs"),
  "org.webjars"               %  "momentjs"                            % "2.11.1",
  "org.webjars.bower"         % "angular-utils-ui-breadcrumbs"         % "0.2.1",
  // Testing
  "com.github.dnvriend"       %% "akka-persistence-inmemory"           % "1.2.0"              % "test",
  "com.typesafe.akka"         %% "akka-testkit"                        % "2.4.1"              % "test",
  "com.github.nscala-time"    %% "nscala-time"                         % "2.6.0"              % "test",
  "org.scalatestplus"         %% "play"                                % "1.4.0-M4"           % "test",
  "org.pegdown"               % "pegdown"                              % "1.6.0"              % "test"
)

routesGenerator := InjectedRoutesGenerator

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

//net.virtualvoid.sbt.graph.Plugin.graphSettings

//MochaKeys.requires += "./setup.js"

// Configure the steps of the asset pipeline (used in stage and dist tasks)
// rjs = RequireJS, uglifies, shrinks to one file, replaces WebJars with CDN
// digest = Adds hash to filename
// gzip = Zips all assets, Asset controller serves them automatically when client accepts them
pipelineStages := Seq(rjs, digest, gzip)

// To completely override the optimization process, use this config option:
//requireNativePath := Some("node r.js -o name=main out=javascript-min/main.min.js")

PB.protobufSettings

PB.runProtoc in PB.protobufConfig := (args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v261" +: args.toArray))

// setting for play-auto-refresh plugin so that it does not open a new browser window when
// the application is run
com.jamesward.play.BrowserNotifierKeys.shouldOpenBrowser := false

coverageExcludedPackages := "<empty>;Reverse.*"
