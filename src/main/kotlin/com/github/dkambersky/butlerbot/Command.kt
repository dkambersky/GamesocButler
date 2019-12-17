package com.github.dkambersky.butlerbot

import discord4j.core.event.domain.Event
import reactor.core.publisher.Mono

interface Command {
   fun process(e: Event) : Mono<Void>
}