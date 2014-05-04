package com.prystupa.matching

import com.prystupa.matching.OrderBook.Modifier
import collection.mutable


/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

object OrderBook {

  sealed trait Modifier {
    def decreaseTopBy(qty: Double)
  }

}

class OrderBook(side: Side) {

  private val marketBook = FastList[Order]()
  private val limitBook = FastList[OrdersAtLimit]()
  private val priceOrdering = if (side == Sell) Ordering.ordered[Double] else Ordering.ordered[Double].reverse
  private val modifier = new BookModifier
  private val rejectedPub = new OrderPublisher
  private val cancelledPub = new OrderPublisher

  val rejected: mutable.Publisher[Order] = rejectedPub
  val cancelled: mutable.Publisher[Order] = cancelledPub

  def add(order: Order) {

    order match {
      case _: MarketOrder => marketBook.append(order)
      case LimitOrder(_, _, _, limit) => addLimit(limit, order)
      case peg: PegOrder => addPeg(peg, isNewOrder = true)
    }
  }

  def valueOf(order: Order): PriceLevel = order match {

    case _: MarketOrder => MarketPrice
    case LimitOrder(_, _, _, limit) => LimitPrice(limit)
    case PegOrder(_, _, _, limit) => bestLimit.map(bl => {
      LimitPrice(limit.fold(bl)(l => priceOrdering.max(l, bl)))
    }).getOrElse(UndefinedPrice)
  }

  def top: Option[Order] = marketBook.headOption orElse limitBook.headOption.map(_.orders.head)

  def bestLimit: Option[Double] = limitBook.headOption.map(_.limit)

  def modify(worker: Modifier => Unit) {

    val oldBestLimit = bestLimit

    worker(modifier)

    val newBestLimit = searchBestLimit()
    if (newBestLimit != oldBestLimit) updatePegsOnBestLimitWorsened(newBestLimit)
  }

  def orders(): List[Order] = {
    marketBook.toList ++ limitBook.flatMap(_.orders)
  }


  private def addLimit(level: Double, order: Order) {

    val oldBestLimit = bestLimit
    val oldTopLevel = limitBook.headOption
    insertLimit(level, order)

    oldTopLevel.foreach(level => {
      if (oldBestLimit != bestLimit) updatePegsOnBestLimitImproved(level)
    })
  }

  private def insertLimit(level: Double, order: Order): OrderRef = {

    val entry = limitBook.getOrInsertAt(
      levelOrders => priceOrdering.compare(level, levelOrders.limit),
      OrdersAtLimit(level, FastList(), FastList()))
    val OrdersAtLimit(_, orders, _) = entry.value

    new OrderRef(entry, orders.append(order))
  }

  private def searchBestLimit(): Option[Double] = {
    def search(book: Iterable[OrdersAtLimit]): Option[Double] = {
      if (book.isEmpty) None
      else book.head.orders.collectFirst({
        case LimitOrder(_, _, _, limit) => limit
      }) orElse search(book.tail)
    }

    search(limitBook)
  }

  private def addPeg(order: PegOrder, isNewOrder: Boolean) {
    bestLimit match {
      case Some(bl) =>
        val level = order.limit.fold(bl)(priceOrdering.max(_, bl))
        val ref = insertLimit(level, order)
        ref.addToPegs()
      case None => if (isNewOrder) rejectedPub.publish(order) else cancelledPub.publish(order)
    }
  }

  private def updatePegsOnBestLimitImproved(level: OrdersAtLimit) {

    val pegsToResubmit = FastList[OrderRef]()
    level.pegs.removeInto(pegsToResubmit, l => l.order match {
      case PegOrder(_, _, _, limit) => limit != Some(level.limit)
      case _ => false
    })

    resubmitPegs(pegsToResubmit)
  }

  private def updatePegsOnBestLimitWorsened(lastLimit: Option[Double]) {
    def find(levels: Iterable[OrdersAtLimit], results: FastList[OrderRef]): Any = {
      levels.headOption.foreach(level => {
        if (lastLimit.fold(true)(ll => priceOrdering.gt(ll, level.limit))) {
          level.pegs.removeInto(results, l => l.order match {
            case PegOrder(_, _, _, limit) => true
            case _ => false
          })
          find(levels.tail, results)
        }
      })
    }

    val pegsToResubmit = FastList[OrderRef]()
    find(limitBook, pegsToResubmit)

    resubmitPegs(pegsToResubmit)
  }

  private def resubmitPegs(pegs: FastList[OrderRef]) {
    pegs.foreach(_.remove())
    pegs.foreach(ref => addPeg(ref.toPegOrder, isNewOrder = false))
  }

  private class BookModifier extends Modifier {

    def decreaseTopBy(qty: Double) {
      def decrease(list: FastList[Order]) {
        val top = list.head
        if (qty == top.qty) list.removeTop()
        else list.updateTop(top.withQty(top.qty - qty))
      }

      if (marketBook.headOption.isDefined) decrease(marketBook)
      else {
        limitBook.headOption match {
          case Some(OrdersAtLimit(_, orders, _)) =>
            decrease(orders)
            if (orders.isEmpty) limitBook.removeTop()
          case None => throw new IllegalStateException("No top order in the book")
        }
      }
    }
  }

  private case class OrdersAtLimit(limit: Double, orders: FastList[Order], pegs: FastList[OrderRef])

  private class OrderRef(list: FastList.Entry[OrdersAtLimit], entry: FastList.Entry[Order]) {

    def remove() {
      val orders = list.value.orders
      entry.remove()
      if (orders.isEmpty) list.remove()
    }

    def order = entry.value

    def toPegOrder: PegOrder = entry.value match {
      case peg: PegOrder => peg
      case _ => throw new IllegalStateException()
    }

    def addToPegs() {
      list.value.pegs.append(this)
    }
  }

  private class OrderPublisher extends mutable.Publisher[Order] {
    override def publish(order: Order): Unit = super.publish(order)
  }

}
