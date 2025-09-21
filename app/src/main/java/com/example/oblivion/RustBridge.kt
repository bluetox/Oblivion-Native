package com.example.oblivion

object RustBridge {
    external fun runClient(): String
    external fun init(callback: RustBridgeCallback)
}