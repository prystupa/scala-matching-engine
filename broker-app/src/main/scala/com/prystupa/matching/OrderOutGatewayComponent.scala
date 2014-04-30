package com.prystupa.matching

import akka.actor.Actor
import akka.camel.Producer

trait OrderOutGatewayComponent {

  def ordersOutEndpointUri: String

  class OrderOutGateway extends Actor with Producer {

    override def endpointUri: String = ordersOutEndpointUri
  }
}
