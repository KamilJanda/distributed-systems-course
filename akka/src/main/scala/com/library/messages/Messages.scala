package com.library.messages

sealed trait Message

final case class SearchBookRequest(title: String) extends Message

final case class SearchBookResponse(price: Option[Int]) extends Message

final case class OrderBookRequest(title: String) extends Message

final case class OrderBookResponse(result: OrderResult) extends Message


