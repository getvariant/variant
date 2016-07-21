package com.variant.server.dispatch

import com.variant.core.net.PayloadWriter
import com.variant.core.net.Payload
import com.variant.server.ServerBoot
import com.variant.server.ServerPropertyKeys

class ServerPayloadWriter(body:String) extends PayloadWriter(body) {
  
    setProperty(Payload.Property.SRV_REL, ServerBoot.getCore.getComptime.getComponentVersion)
    setProperty(Payload.Property.SSN_TIMEOUT, ServerBoot.getCore.getProperties.get(ServerPropertyKeys.SESSION_TIMEOUT_SECS))

    def this(bodyAsBytes:Array[Byte]) = this(new String(bodyAsBytes)) 
}