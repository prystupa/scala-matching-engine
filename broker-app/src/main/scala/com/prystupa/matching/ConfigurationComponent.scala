package com.prystupa.matching

trait ConfigurationComponent {

  lazy val ordersOutEndpointUri = "rabbitmq://localhost:5672/orders?username=guest&password=guest&routingKey=ticker"
}
