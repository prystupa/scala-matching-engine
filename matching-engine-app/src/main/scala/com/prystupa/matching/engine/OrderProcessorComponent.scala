package com.prystupa.matching.engine

import akka.actor.{ActorLogging, ActorRef, Actor}
import scala.collection.mutable
import com.prystupa.matching._
import scala.util.{Failure, Success}

trait OrderProcessorComponent {
  def orderBookEventGateway: ActorRef
  def orderParser: OrderParser

  class OrderProcessor extends Actor with ActorLogging {
    private val buy = new OrderBook(Buy)
    private val sell = new OrderBook(Sell)
    private val matchingEngine = new MatchingEngine(buy, sell)

    subscribe[Trade](matchingEngine, orderParser.serialize)
    List(buy, sell) foreach {
      book =>
        subscribe(book.rejected, orderParser.serializeReject)
        subscribe(book.cancelled, orderParser.serializeCancel)
    }

    override def receive: Receive = {
      case Success(order: Order) =>
        log.debug("Received order {}", order)
        matchingEngine.acceptOrder(order)
      case Failure(e) =>
        log.error(e, e.getMessage)
    }

    private def subscribe[T](publisher: mutable.Publisher[T], serializer: T => String): Unit = publisher.subscribe(new publisher.Sub {
      override def notify(pub: publisher.Pub, event: T): Unit = {
        log.debug("Sending out trading event: {}", event)

        orderBookEventGateway ! serializer(event)
      }
    })
  }

}
