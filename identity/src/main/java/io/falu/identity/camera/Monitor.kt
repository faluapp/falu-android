package io.falu.identity.camera

import org.joda.time.Duration
import org.joda.time.LocalDateTime

/**
 * Monitor the start time and result of a stat
 */
internal interface Monitor {
    /**
     * The time monitoring started
     */
    val start: LocalDateTime

    /**
     * Monitor the stats
     */
    fun monitor(stats: String? = null)
}

/**
 *
 */
internal class MonitorImpl(private val complete: (LocalDateTime, String?) -> Unit) : Monitor {
    override val start: LocalDateTime = LocalDateTime.now()

    override fun monitor(stats: String?) {
        complete(start, stats)
    }
}

data class Statistic(
    val start: LocalDateTime,
    val duration: Duration,
    val result: String? = null
)