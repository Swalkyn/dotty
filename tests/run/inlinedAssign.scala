object Test {

  @`inline`
  def swap[T](x: T, x_= : T => Unit, y: T, y_= : T => Unit) = {
    val t = x
    x_=(y)
    y_=(t)
  }

  def main(args: Array[String]) = {
    var x = 1
    var y = 2
    @`inline` def setX(z: Int) = x = z
    @`inline` def setY(z: Int) = y = z
    swap[Int](x, setX, y, setY)
    assert(x == 2 && y == 1)
  }
}
