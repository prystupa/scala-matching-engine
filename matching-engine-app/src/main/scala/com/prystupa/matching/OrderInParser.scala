package com.prystupa.matching

import scala.util.Try

trait OrdersInParser {
  def parse(message: String): Try[Order]
}

object OrderInParser extends OrdersInParser {

  private val core = """(\w+):\s+(\w+)\s+((?i)buy|sell)\s+(\d+)"""
  private val double = """@(\d+(?:\.\d+)?)"""
  private val Market = core.r
  private val Limit = (core + double).r
  private val Peg = (core + """\s+(?:(?i)peg)""" + "(?:" + double + ")?").r

  def parseSide(side: String): Side = side.toLowerCase match {
    case "buy" => Buy
    case "sell" => Sell
  }

  override def parse(message: String): Try[Order] = {
    Try {
      message match {
        case Market(broker, ticker, side, qty) => MarketOrder(broker, parseSide(side), qty.toDouble)
        case Limit(broker, ticker, side, qty, limit) => LimitOrder(broker, parseSide(side), qty.toDouble, limit.toDouble)
        case Peg(broker, ticker, side, qty, limit) => PegOrder(broker, parseSide(side), qty.toDouble, Option(limit).map(_.toDouble))
      }
    }
  }
}