resolvers += Resolver.bintrayIvyRepo("twittercsl", "sbt-plugins")

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"


addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "19.4.0")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.20")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.8.1"