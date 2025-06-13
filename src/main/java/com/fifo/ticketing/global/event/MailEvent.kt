package com.fifo.ticketing.global.event

import com.fifo.ticketing.domain.like.dto.NoPayedMailDto
import com.fifo.ticketing.domain.like.dto.ReservationStartMailDto

class MailEvent(val dto: NoPayedMailDto)
class ReservationEvent(val dto: ReservationStartMailDto)