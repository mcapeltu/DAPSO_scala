package DU

import org.apache.spark.broadcast.Broadcast
import scala.util.Random

class FuncionesAuxiliares {
  //Genera un uniform entre -a y a
  def Uniform(a: Double, rand: Random): Double = {
    val num = rand.nextDouble() * 2 * a // genera un número aleatorio entre 0.0 y 2a
    val ret = num - a
    ret
  }

  def MSE(y: Array[Double], pred: Array[Double]): Double = {
    val n = y.length
    if (n != pred.length) {
      println("error: datos y predicción de distintos tamaños")
      return -1
    }
    var resultado = 0.0
    for (i <- 0 until n) {
      resultado += math.pow(y(i) - pred(i), 2)
    }
    resultado /= n
    resultado
  }

  def InitParticles(N: Int, M: Int, objetivo: Array[Double], bgf: Double, mpg: Array[Double], rand: Random): (Double, Array[Double], Array[Array[Double]]) = {
    var parts_ = Array.empty[Array[Double]]
    var best_global_fitness = bgf
    var mejor_pos_global = mpg


    for (j <- 0 until M) {
      val posicion = Array.fill(N)(Uniform(100, rand))
      val velocidad = Array.fill(N)(Uniform(100, rand))
      val fit = MSE(posicion, objetivo)
      val part_ = posicion ++ velocidad ++ posicion ++ Array(fit)

      //best_local_fitness_arr = best_local_fitness_arr :+ fit
      if (fit < best_global_fitness) {
        best_global_fitness = fit
        //accum.setValue(fit)
        mejor_pos_global = posicion
      }
      parts_ = parts_ :+ part_
    }
    (best_global_fitness, mejor_pos_global, parts_)
  }

}
