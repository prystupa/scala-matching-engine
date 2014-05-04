package broker

import akka.actor.{Props, ActorSystem}

object BrokerApp
  extends App
  with ConfigurationComponent
  with OrderOutGatewayComponent {

  val system = ActorSystem("MatchingEngineApp")

  val gateway = system.actorOf(Props(new OrderOutGateway))

  val orders = Stream.continually {
    Console.readLine("Order> ")
  }

  orders.takeWhile(!_.isEmpty) foreach {
    gateway ! _
  }
}
