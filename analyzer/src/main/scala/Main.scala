package myapp

import scala.compat.Platform.EOL
import scala.meta._

//If you skip the readme, the gist is you need the compiler plugin and -Yrangepos to enable the semanticDb
//addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % ScalametaVersion cross CrossVersion.full),
//scalacOptions += "-Yrangepos"

object Main {
  //Extract a pretty name out of the symbol syntax string
  def prettyName(name: ResolvedName): String =
    name.symbol //use symbol which will be dealiased in case of package renaming on import
    .syntax //syntax is not what we want, but it's close
      .dropRight(1) //syntax prints a trailing . that we don't want
      .takeWhile(_ != '(') //Only keep the portion of syntax before the (additional information I don't want to print)
      .replace('#', '.') //change # in syntax to . to complete beautifying the string

  def main(args: Array[String]): Unit = {
    //Load the semanticDb
    val database = Database.load(Classpath(BuildInfo.classpath), Sourcepath(BuildInfo.sourcepath))

    //The semanticDb contains original source text, we want to parse it to get abstract syntax trees
    val asts: Seq[Source] = database.documents.map(_.input.text.parse[Source].get)

    //Use a partial function to collect only the imports from source files
    val imports = asts.flatMap(_.collect {
      //ignore nodes that are not Imports
      case Import(importers) => importers.flatMap(i => i.importees)
    }.flatten)

    val names: Seq[ResolvedName] = imports
      .map(_.pos) //just keep the position of the import
      .flatMap(p => database.names.find(rn => rn.position.start == p.start)) //lookup by start position

    val result = names.map(prettyName).mkString(EOL, EOL, EOL)
    /*
      _root_.scala.collection.JavaConverters
      _root_.scala.collection.convert.AsJavaConverters.asJavaCollection
      _root_.scala.collection.convert.AsJavaConverters.asJavaEnumeration
     */

    println(result)
    //Check out the other useful information in the semanticDb
    println(database)
  }
}
