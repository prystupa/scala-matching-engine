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

    subscribe[Trade](matchingEngine, "trade")
    List(buy, sell) foreach {
      book =>
        subscribe(book.rejected, "rejected")
        subscribe(book.cancelled, "cancelled")
    }

    override def receive: Receive = {
      case Success(order: Order) =>
        log.debug("Received order {}", order)
        matchingEngine.acceptOrder(order)
    }

  }

  private def subscribe[T](publisher: mutable.Publisher[T], discriminator: String): Unit = publisher.subscribe(new publisher.Sub {
    override def notify(pub: publisher.Pub, event: T): Unit = {
      val gatewayEvent = Map[String, T](discriminator -> event)
      orderBookEventGateway ! gatewayEvent
    }
  })
}
