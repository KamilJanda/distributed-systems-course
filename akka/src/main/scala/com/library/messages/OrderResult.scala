package com.library.messages

sealed trait OrderResult

case object OrderSuccess extends OrderResult

case object OrderFail extends OrderResult
