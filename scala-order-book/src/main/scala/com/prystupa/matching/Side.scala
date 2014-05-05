package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/25/12
 * Time: 3:13 PM
 */


sealed trait Side {
  def value: String

  override def toString: String = value
}

object Buy extends Side {
  override def value: String = "buy"
}

object Sell extends Side {
  override def value: String = "sell"
}
