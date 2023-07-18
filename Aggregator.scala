package DU

import scala.concurrent.Channel

class Aggregator(private val S: Int, private val srch: Channel[Lote]) {
  private val lote = new Lote(S)

  def recibir(datos: Array[Double]): Unit = {
    if(lote.estaCompleto) {
      srch.write(lote.copiar())
      lote.clean()
    }
    lote.agregar(datos)
  }
}
