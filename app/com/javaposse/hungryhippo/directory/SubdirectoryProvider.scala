package com.javaposse.hungryhippo.directory

object SubdirectoryProvider {
  val TITLE = """<title>Index of (.*)</title>""".r

  def findDirectories(document:String): Iterable[String] = {
    // Could be Bintray or Apache directory listing
    val parser:DirectoryParser = TITLE.pattern.matcher(document).find() match {
    case true => ApacheDirectoryParser
    case false => BintrayDirectoryParser
  }
    parser.parse(document)

  }
}
trait DirectoryParser {
  def parse(document:String): Iterable[String]
}

/**
 * Example:
 *
 * <html>
 * <head><title>Index of /maven2/com/netflix/</title></head>
 * <body bgcolor="white">
 * <h1>Index of /maven2/com/netflix/</h1><hr><pre><a href="../">../</a>
 * <a href="archaius/">archaius/</a>                                          18-Jan-2013 18:18                   -
 * <a href="astyanax/">astyanax/</a>                                          20-Sep-2013 21:17                   -
 * </pre><hr></body>
 * </html>
 */
object ApacheDirectoryParser extends DirectoryParser {
  override def parse(document: String): Iterable[String] = {
    val A = """<a href="(.*)/">""".r
    A.findAllIn(document).matchData.map(_.group(1)).toList
  }
}
/**
 *
<html>
    <head>
    </head>
    <body>
      <pre><a onclick="navi('1393439029005com/')" href="#com/" rel="nofollow">com/</a></pre>
    </body>
  </html>
 */
object BintrayDirectoryParser extends DirectoryParser {
  override def parse(document: String): Iterable[String] = {
    val foo = scala.xml.XML.loadString(document)
    val bar = foo \ "body" \ "pre" \ "a"
    bar.map(_.text)
  }
}