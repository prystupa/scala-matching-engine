package com.prystupa.matching.engine

import scala.util.Try
import com.prystupa.matching._
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper

trait OrderParser {
  def parse(message: String): Try[Order]

  def serialize(trade: Trade): String

  def serializeReject(order: Order): String

  def serializeCancel(order: Order): String
}

object OrderParser extends OrderParser {

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

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

  override def serialize(trade: Trade): String = {
    mapper.writeValueAsString(Map("trade" -> trade))
  }

  override def serializeReject(order: Order): String = {
    mapper.writeValueAsString(Map("rejected" -> order))
  }

  override def serializeCancel(order: Order): String = {
    mapper.writeValueAsString(Map("cancel" -> order))
  }
}
