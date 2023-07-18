package DU
import scala.concurrent._
import akka.actor.{Actor, ActorSystem, Props}
import scala.util.Random
import DU.FuncionesEval

// Definir un actor para evaluar el mejor global y actualizar de forma asíncrona
class GlobalEvalActor(fuch: Channel[Array[Array[Double]]], N: Int, S: Int, I: Int, m: Int, aggr: Aggregator, rand: Random, W: Double, c_1: Double, c_2: Double, V_max: Double) extends Actor {
  val FE = new FuncionesEval
  var best_global_fitness = Double.MaxValue
  var mejor_pos_global: Array[Double] = _

  def receive: Receive = {
    case "start" =>
      val iters = I * m / S
      for (i <- 0 until iters) {
        println("iter " + i + " dentro de globalActor")
        // Esperar un elemento del canal fuch
        var sr = fuch.read

        var pos: Array[Double] = new Array[Double](0)
        var velocidad: Array[Double] = new Array[Double](0)
        var mpl:Array[Double] = new Array[Double](0)
        var fit:Double = 0

        for (par <- sr) {
          pos = par.slice(0, N)
          velocidad = par.slice(N, 2 * N)
          mpl = par.slice(2 * N, 3 * N)
          fit = par(3 * N)

          if (fit < best_global_fitness) {
            best_global_fitness = fit
            mejor_pos_global = pos
          }
          val newPar = FE.posEval(par, mejor_pos_global, N, rand, W, c_1, c_2, V_max)
          aggr.recibir(newPar)
        }
        sr = null
        println("mejor posición actual " + mejor_pos_global.mkString(", "))
      }
      DAPSO.acabado = true

      println(s"mejor_pos_global-> ${mejor_pos_global.mkString("[", ", ", "]")}")
      println(s"mejor fitness global-> $best_global_fitness")

      context.stop(self)
  }
}


