package com.prystupa.matching.engine

import akka.actor.{ActorLogging, ActorRef, Actor}
import scala.collection.mutable
import com.prystupa.matching._
import scala.util.Success

trait OrderProcessorComponent {
  def orderBookEventGateway: ActorRef

  class OrderProcessor extends Actor with ActorLogging {
    private val buy = new OrderBook(Buy)
    private val sell = new OrderBook(Sell)
    private val matchingEngine = new MatchingEngine(buy, sell)

    List(buy, sell, matchingEngine).foreach(publisher => subscribe(publisher))

    override def receive: Receive = {
      case Success(order: Order) =>
        log.debug("Received order {}", order)
        matchingEngine.acceptOrder(order)
    }

  }

  private def subscribe(publisher: mutable.Publisher[OrderBookEvent]): Unit = publisher.subscribe(new publisher.Sub {
    override def notify(pub: publisher.Pub, event: OrderBookEvent): Unit = {
      orderBookEventGateway ! event
    }
  })
}
