package DU

import DU.FuncionesAuxiliares
import org.apache.spark.broadcast.Broadcast
import scala.util.Random
import scala.concurrent._
import scala.async.Async._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


class FuncionesEval {
  val FA = new FuncionesAuxiliares

  def fitnessEval(part: Array[Double], N: Int, objetivo: Array[Double]): Array[Double] = {

    if (part == null) {
      println("El array es null")
      return Array.empty[Double]
    }

    //println(part.length)
    //println(part.mkString(", "))

    val best_fit_local = part(3 * N)
    val filas = part.slice(0, N)
    val fit = FA.MSE(filas, objetivo)
    if (fit < best_fit_local) {
      part(3 * N) = fit
      for (k <- 0 until N) {
        part(2 * N + k) = filas(k)
      }
    }
    part
  }

  def posEval(part: Array[Double], mpg: Array[Double], N: Int, rand: Random, W: Double, c_1: Double, c_2: Double, V_max: Double): Array[Double] = {
    // global ind (no es necesario en Scala)
    val velocidades = part.slice(N, 2 * N)
    val mpl = part.slice(2 * N, 3 * N)
    val r_1 = rand.nextDouble()
    val r_2 = rand.nextDouble()
    for (k <- 0 until N) {
      velocidades(k) = W * velocidades(k) + c_1 * r_1 * (mpl(k) - part(k)) + c_2 * r_2 * (mpg(k) - part(k))
      if (velocidades(k) > V_max) {
        velocidades(k) = V_max
      } else if (velocidades(k) < -V_max) {
        velocidades(k) = -V_max
      }
      part(k) = part(k) + velocidades(k)
      part(N + k) = velocidades(k)
    }
    part
  }

  def parallelFitness(srch: Channel[Lote], n: Int, objetivo: Array[Double]) = Future {
    var batch = srch.read
  //  println("Tamaño del lote: " + batch.obtenerLote.length)
   // println("Relleno del lote: " + batch.obtenerIndex)
  //  println(batch.obtenerLote.mkString(", "))

    //val RDD = sc.parallelize(batch.obtenerLote)
    //val psfu = RDD.map(x => fitnessEval(x, n)).collect()
    val psfu = batch.obtenerLote.map(x => fitnessEval(x, n, objetivo))
    batch = null

    //RDD.unpersist()
    psfu
  }

  def awaitFitness(srch: Channel[Lote], n: Int, objetivo: Array[Double]) = async {
    await(parallelFitness(srch, n, objetivo))
  }

}
