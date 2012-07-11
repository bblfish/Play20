logLevel := Level.Warn

resolvers += Classpaths.typesafeResolver

resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbtscalariform" % "sbtscalariform" % "0.3.1")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.3")

//resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

//addSbtPlugin("com.github.mpeltonen" %% "sbt-idea" % "1.1.0-SNAPSHOT")
addSbtPlugin("com.github.mpeltonen" %% "sbt-idea" % "1.0.0")

