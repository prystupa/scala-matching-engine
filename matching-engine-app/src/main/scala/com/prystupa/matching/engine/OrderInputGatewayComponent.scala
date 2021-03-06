package com.prystupa.matching.engine

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.camel.{CamelMessage, Consumer}

trait OrderInputGatewayComponent {

  def orderInputEndpointUri: String

  def orderParser: OrderParser

  def orderProcessor: ActorRef

  class OrderInputGateway extends Actor with Consumer with ActorLogging {

    override val endpointUri: String = orderInputEndpointUri

    override def receive: Receive = {
      case m: CamelMessage => orderProcessor ! orderParser.parse(m.bodyAs[String])
    }
  }

}
