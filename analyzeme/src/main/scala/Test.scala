//Add some ugly renamed and nested imports to demonstrate using semantic db to fully qualify imports
import scala.collection.{ JavaConverters => jc }
import jc.{ asJavaCollection => ajc, asJavaEnumeration => aje }

object Test {
  def printList() = {
    val x = List(1, 2, 3).map(_ + 1)
    println(x)
  }
}
