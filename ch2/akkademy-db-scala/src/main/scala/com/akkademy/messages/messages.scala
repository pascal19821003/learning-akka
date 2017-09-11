package com.akkademy.messages

case class SetRequest(key: String, value: Object)
case class GetRequest(key: String)

case class KeyNotFoundException(key: String) extends Exception

case class Heartbeat(conf:String)

case class ConfUpdate(conf:String)
case class ConfUpdateRes(message:String)