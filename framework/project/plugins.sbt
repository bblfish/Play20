logLevel := Level.Warn

resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.sbtscalariform" % "sbtscalariform" % "0.3.1")

//resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
//resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

addSbtPlugin("com.github.mpeltonen" %% "sbt-idea" % "1.1.0-SNAPSHOT")

