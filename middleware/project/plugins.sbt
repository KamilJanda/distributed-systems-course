resolvers += Resolver.bintrayIvyRepo("twittercsl", "sbt-plugins")

addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "19.4.0")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.20")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.8.1"