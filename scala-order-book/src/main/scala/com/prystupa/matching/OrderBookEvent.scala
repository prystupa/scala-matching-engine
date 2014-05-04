package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 11:03 PM
 */

object OrderBookEvent {
  def apply(trade: Trade): OrderBookEvent = OrderBookEvent(Some(trade), None, None)

  def apply(rejected: RejectedOrder): OrderBookEvent = OrderBookEvent(None, Some(rejected), None)

  def apply(canceled: CancelledOrder): OrderBookEvent = OrderBookEvent(None, None, Some(canceled))
}


case class OrderBookEvent private(trade: Option[Trade], rejected: Option[RejectedOrder], canceled: Option[CancelledOrder]) {
  def forEach(fTrade: (Trade) => Unit, fRejected: (RejectedOrder) => Unit, fCanceled: (CancelledOrder) => Unit): Unit = {
    trade.foreach(fTrade)
    rejected.foreach(fRejected)
    canceled.foreach(fCanceled)
  }
}

case class Trade(buying: Order, selling: Order, price: Double, qty: Double)

case class RejectedOrder(order: Order)

case class CancelledOrder(order: Order)