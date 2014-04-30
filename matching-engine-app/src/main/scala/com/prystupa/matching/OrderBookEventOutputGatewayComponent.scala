package com.prystupa.matching

import akka.actor.Actor
import akka.camel.Producer

trait OrderBookEventOutputGatewayComponent {

  def orderBookEventsOutputEndpointUri: String

  class OrderBookEventOutputGateway extends Actor with Producer {

    override val endpointUri: String = orderBookEventsOutputEndpointUri
  }

}
