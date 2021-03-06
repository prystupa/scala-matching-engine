package com.prystupa.matching.step_definitions

import scala.collection.JavaConversions._

import cucumber.api.java.en.{Given, When, Then}
import cucumber.api.DataTable
import com.prystupa.matching._
import org.scalatest.Matchers
import scala.collection.mutable

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/24/12
 * Time: 8:46 PM
 */

class MatchingEngineSteps extends OrderStepUtils with Matchers {

  val buyBook: OrderBook = new OrderBook(Buy)
  val sellBook: OrderBook = new OrderBook(Sell)
  val matchingEngine = new MatchingEngine(buyBook, sellBook)

  var actualTrades = Vector.empty[Trade]
  var actualRejected = Vector.empty[Order]
  var actualCancelled = Vector.empty[Order]

  subscribe[Trade](matchingEngine, t => actualTrades = actualTrades :+ t)
  List(buyBook, sellBook) foreach {
    book =>
      subscribe[Order](book.rejected, o => actualRejected = actualRejected :+ o)
      subscribe[Order](book.cancelled, o => actualCancelled = actualCancelled :+ o)
  }

  @When("^the following orders are submitted in this order:$")
  def the_following_orders_are_submitted_in_this_order(orders: java.util.List[OrderRow]) {

    orders.toList.foreach(r => matchingEngine.acceptOrder(parseOrder(r.broker, r.side, r.qty, r.price)))
  }

  @Then("^market order book looks like:$")
  def market_order_book_looks_like(book: DataTable) {

    val (buyOrders, sellOrders) = parseExpectedBooks(book)

    buyBook.orders().map(o => BookRow(Buy, o.broker, o.qty, bookDisplay(o))) should equal(buyOrders)
    sellBook.orders().map(o => BookRow(Sell, o.broker, o.qty, bookDisplay(o))) should equal(sellOrders)
  }

  @Then("^the following trades are generated:$")
  def the_following_trades_are_generated(trades: java.util.List[TradeRow]) {

    actualTrades.map(t => TradeRow(t.buying.broker, t.selling.broker, t.price, t.qty)) should equal(trades.toVector)
    actualTrades = Vector.empty
  }

  @Then("^no trades are generated$")
  def no_trades_are_generated() {

    actualTrades should equal(Vector.empty)
  }

  @Given("^the reference price is set to \"([^\"]*)\"$")
  def the_reference_price_is_set_to(price: Double) {

    matchingEngine.referencePrice = price
  }

  @Then("^the reference price is reported as \"([^\"]*)\"$")
  def the_reference_price_is_reported_as(price: Double) {

    matchingEngine.referencePrice should equal(price)
  }

  @Then("^the following orders are rejected:$")
  def the_following_orders_are_rejected(orders: java.util.List[OrderRow]) {

    val expected = orders.map(r => parseOrder(r.broker, r.side, r.qty, r.price))

    actualRejected should equal(expected)
    actualRejected = Vector.empty
  }


  private def subscribe[T](publisher: mutable.Publisher[T], handler: T => Unit): Unit = {

    publisher.subscribe(new publisher.Sub {
      def notify(pub: publisher.Pub, event: T) {
        handler(event)
      }
    })
  }

  private def parseExpectedBooks(book: DataTable): (List[BookRow], List[BookRow]) = {
    def buildOrders(orders: List[List[String]], side: Side) = {
      orders.filterNot(_.forall(_.isEmpty)).map(order => {
        val (broker :: qty :: price :: Nil) = order
        BookRow(side, broker, qty.toDouble, price)
      })
    }

    val orders = book.raw().toList.drop(1).map(_.toList)
    val buy = orders.map(_.take(3))
    val sell = orders.map(_.drop(3).reverse)
    (buildOrders(buy, Buy), buildOrders(sell, Sell))
  }

  private case class OrderRow(broker: String, side: String, qty: Double, price: String)

  private case class BookRow(side: Side, broker: String, qty: Double, price: String)

  private case class TradeRow(buyingBroker: String, sellingBroker: String, price: Double, qty: Double)

}
