package com.mobileprogramming.tadainu.partnersFeat.data

import com.google.firebase.Timestamp

data class Reservation(
    var reservation_id: String = "",
    var reservation_check_in: Timestamp? = null,
    var reservation_check_out: Timestamp? = null,
    var reservation_at: Timestamp = Timestamp.now(),
    var reservation_user_id: String = "",
    var reservation_partner_id: String = "",
    var reservation_pet_id: String = "",
)
