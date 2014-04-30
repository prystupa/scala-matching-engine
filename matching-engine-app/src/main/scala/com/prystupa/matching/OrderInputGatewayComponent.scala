package com.prystupa.matching

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.camel.{CamelMessage, Consumer}
import scala.util.{Success, Failure}

trait OrderInputGatewayComponent {

  def orderInputEndpointUri: String

  def orderParser: OrdersInParser

  def orderProcessor: ActorRef

  class OrderInputGateway extends Actor with Consumer with ActorLogging {

    override val endpointUri: String = orderInputEndpointUri

    override def receive: Receive = {
      case m: CamelMessage => orderProcessor ! orderParser.parse(m.bodyAs[String])
    }
  }

}
