import scala.quoted._
import scala.tasty.inspector._

opaque type PhoneNumber = String

case class I8163() {
  val phone: PhoneNumber = "555-555-5555".asInstanceOf[PhoneNumber]
  val other: String = "not a phone"
}

object Test {
  def main(args: Array[String]): Unit = {
    // Artefact of the current test infrastructure
    // TODO improve infrastructure to avoid needing this code on each test
    val classpath = dotty.tools.dotc.util.ClasspathFromClassloader(this.getClass.getClassLoader).split(java.io.File.pathSeparator).find(_.contains("runWithCompiler")).get
    val allTastyFiles = dotty.tools.io.Path(classpath).walkFilter(_.extension == "tasty").map(_.toString).toList
    val tastyFiles = allTastyFiles.filter(_.contains("I8163"))

    new TestInspector().inspectTastyFiles(tastyFiles)
  }
}

class TestInspector() extends TastyInspector:

  protected def processCompilationUnit(using Quotes)(root: quotes.reflect.Tree): Unit =
    inspectClass(root)

  private def inspectClass(using Quotes)(tree: quotes.reflect.Tree): Unit =
    import quotes.reflect._
    tree match {
      case t: PackageClause =>
        t.stats.map( m => inspectClass(m) )
      case t: ClassDef if !t.name.endsWith("$") =>
        val interestingVals = t.body.collect {
          case v: ValDef => v
        }
        val shouldBePhone = interestingVals.find(_.name == "phone").get
        val shouldBePhoneType = shouldBePhone.tpt.tpe match {
          case tr: TypeRef => tr
          case _ => throw new Exception("unexpected")
        }
        assert(shouldBePhoneType.isOpaqueAlias)
        assert(shouldBePhoneType.translucentSuperType.show == "scala.Predef.String")

        val shouldNotBePhone = interestingVals.find(_.name == "other").get
        val shouldNotBePhoneType = shouldNotBePhone.tpt.tpe match {
          case tr: TypeRef => tr
          case _ => throw new Exception("unexpected")
        }
        assert(!shouldNotBePhoneType.isOpaqueAlias)

      case x =>
    }