package me.dzikimlecz.timetables.managers

class ServerAccessException(msg: String = "", val code: Int, val reason: String): RuntimeException(msg) {
    override val message: String
        get() = "${super.message}, code = $code, reason = $reason"
}
