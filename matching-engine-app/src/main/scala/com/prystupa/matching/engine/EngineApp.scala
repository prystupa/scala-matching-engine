package com.prystupa.matching.engine

import akka.actor.{Props, ActorSystem}

object EngineApp
  extends App
  with ConfigurationComponent
  with OrderInputGatewayComponent
  with OrderProcessorComponent
  with OrderBookEventOutputGatewayComponent {

  val system = ActorSystem("MatchingEngineApp")
  val orderParser = OrderParser
  val orderProcessor = system.actorOf(Props(new OrderProcessor))
  val orderBookEventGateway = system.actorOf(Props(new OrderBookEventOutputGateway))

  system.actorOf(Props(new OrderInputGateway))
}
