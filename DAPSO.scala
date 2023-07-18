package DU

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.util.CollectionAccumulator
import org.apache.spark.util.DoubleAccumulator

import scala.util.Random
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.async.Async._
import scala.util.{Failure, Success}
import akka.actor.{Actor, ActorSystem, Props}

import DU.FuncionesAuxiliares
import DU.FuncionesEval



object DAPSO {
  val FA = new FuncionesAuxiliares
  val FE = new FuncionesEval

  val rand = new Random()
  val V_max = 10.0
  val W = 1.0
  val c_1 = 0.8
  val c_2 = 0.2
  val objetivo_ = Array[Double](50, 50, 50)
  val objetivo = objetivo_

  // Número dimensiones de los vectores
  val n = 3
  // Número de partículas
  val m = 30
  //número de partículas por lote
  val S = 5
  // Número de iteraciones
  val I = 100000

  var acabado: Boolean = false

  var particulas = Array.empty[Array[Double]]
  var mejor_pos_global_arr = Array.fill(n)(0.0)
  // maximum float
  var best_global_fitness = Double.MaxValue
  var mejor_pos_global = mejor_pos_global_arr

  // Definición de los Channel
  val srch = new Channel[Lote]()
  val fuch = new Channel[Array[Array[Double]]]()

  def main(args : Array[String]): Unit = {

    println( "Hello World!" )

    //Inicialización de las partículas
    var resultado: (Double, Array[Double], Array[Array[Double]]) = FA.InitParticles(n, m, objetivo, best_global_fitness, mejor_pos_global, rand)
    val (double1, array2, arrayDeArrays) = resultado
    best_global_fitness = double1
    mejor_pos_global = array2
    particulas = arrayDeArrays

    //Definimos el aggregator
    val aggr = new Aggregator(S, srch)

    //añadimos las partículas al aggregator
    for (i <- 0 until m) {
      aggr.recibir(particulas(i))
    }


    var tiempo_fitness = 0.0
    var tiempo_poseval = 0.0
    var tiempo_global = 0.0
    var tiempo_collect = 0.0
    var tiempo_foreach = 0.0

    val start = System.nanoTime()

    // Crear el sistema de actores
    val system = ActorSystem("MyActorSystem")

    // Crear los actores
    val globalEvalActor = system.actorOf(Props(new GlobalEvalActor(fuch, n, S, I, m, aggr, rand, W, c_1, c_2, V_max)), "globalEvalActor")

    globalEvalActor ! "start"

    val rows = m
    val columns = n

    var psfuCopy: Array[Array[Double]] = Array.ofDim[Double](rows, columns)

    while (!acabado) {
      // Esperar un elemento del canal srch
      FE.awaitFitness(srch, n, objetivo).onComplete {
        case Success(psfu) =>
          psfuCopy = psfu.map(_.clone())
          fuch.write(psfuCopy)
          println("iter dentro de awaitFitness")
      }
    }

    if (acabado) {
      val end = System.nanoTime()
      val tiempo = (end - start) / 1e9
      println(s"Tiempo de ejecucion(s): $tiempo")
      println(s"Tiempo de ejecucion fitness(s): $tiempo_fitness")
      println(s"Tiempo de ejecucion poseval(s): $tiempo_poseval")
      println(s"Tiempo de ejecucion global fitness(s): $tiempo_global")
      println(s"Tiempo de ejecucion collect(s): $tiempo_collect")
      println(s"Tiempo de ejecucion foreach(s): $tiempo_foreach")

    }

  }


}
