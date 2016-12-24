organization := "org.tensorflow"

name := "tfrecords-hadoop"

version := "0.12"

scalaVersion := "2.11.8"

libraryDependencies ++=
  "com.google.guava" % "guava" % "20.0" ::
    "com.google.protobuf" % "protobuf-java" % "3.1.0" ::
    "org.apache.hadoop" % "hadoop-client" % "2.7.3" % Provided ::
    Nil

assemblyShadeRules in assembly ++=
  // rename the protobuf3 to avoid conflicting with Hadoop's protobuf2 dependency
  ShadeRule.rename("com.google.protobuf.**" -> "protobuf3.@1").inAll ::
    // we need a newer guava for crc32 hashing
    ShadeRule.rename("com.google.common.**" -> "guava20.@1").inAll ::
    Nil

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)