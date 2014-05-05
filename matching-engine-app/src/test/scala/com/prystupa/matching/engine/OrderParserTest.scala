package com.prystupa.matching.engine

import org.scalatest.{Matchers, path}
import com.prystupa.matching._
import com.prystupa.matching.MarketOrder
import com.prystupa.matching.LimitOrder
import scala.util.Success
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OrderParserTest extends path.FunSpec with Matchers {

  val parser = OrderParser

  it("parses Buy market order") {
    parser.parse("A: IBM buy 100") should equal(Success(MarketOrder("A", Buy, 100)))
  }

  it("parses Sell market order") {
    parser.parse("A: IBM sell 100") should equal(Success(MarketOrder("A", Sell, 100)))
  }

  it("parses Buy limit order") {
    parser.parse("A: IBM buy 100@10.2") should equal(Success(LimitOrder("A", Buy, 100, 10.2)))
  }

  it("parses Sell limit order") {
    parser.parse("A: IBM sell 100@10.2") should equal(Success(LimitOrder("A", Sell, 100, 10.2)))
  }

  it("parses Buy pegged order") {
    parser.parse("A: IBM buy 100 peg") should equal(Success(PegOrder("A", Buy, 100, None)))
  }

  it("parses Sell pegged order") {
    parser.parse("A: IBM sell 100 peg") should equal(Success(PegOrder("A", Sell, 100, None)))
  }

  it("parses Buy pegged order with limit") {
    parser.parse("A: IBM buy 100 peg@10.2") should equal(Success(PegOrder("A", Buy, 100, Some(10.2))))
  }

  it("parses Sell pegged order with limit") {
    parser.parse("A: IBM sell 100 peg@10.2") should equal(Success(PegOrder("A", Sell, 100, Some(10.2))))
  }
}
