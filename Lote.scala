package DU

import scala.concurrent.Channel

class Lote(private val S: Int) {
  private var lote: Array[Array[Double]] = Array.ofDim[Array[Double]](S)
  private var index: Int = 0

  def agregar(elemento: Array[Double]): Unit = {
    if (index < S) {
      lote(index) = elemento
      index += 1
    } else {
      throw new IllegalStateException("El lote está lleno")
    }
  }

  def estaCompleto: Boolean = index == S

  def obtenerLote: Array[Array[Double]] = lote.clone()

  def obtenerIndex: Int = index

  def copiar(): Lote = {
    val copiaLote = new Lote(S)
    copiaLote.index = index
    for (i <- 0 until index) {
      copiaLote.lote(i) = lote(i).clone()
    }
    copiaLote
  }

  def clean(): Unit = {
    lote = null
    lote = Array.ofDim[Array[Double]](S)
    index = 0
  }
}
