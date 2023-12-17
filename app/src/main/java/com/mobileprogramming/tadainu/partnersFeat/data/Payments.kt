package com.mobileprogramming.tadainu.partnersFeat.data

import com.google.firebase.Timestamp

data class Payments(
    var payments_id: String = "",
    var payments_created_at: Timestamp = Timestamp.now(),
    var payments_user_id: String = "",
    var payments_partner_id: String = "",
    var payments_pet_id: String = "",
    var payments_amount: Long = 0,
)
