package com.prystupa.matching.engine

trait ConfigurationComponent {

  lazy val orderInputEndpointUri = "rabbitmq://localhost:5672/orders?username=guest&password=guest&queue=orders-to-match&routingKey=ticker"
  lazy val orderBookEventsOutputEndpointUri = "rabbitmq://localhost:5672/order-book-events?username=guest&password=guest&routingKey=broker"
}
