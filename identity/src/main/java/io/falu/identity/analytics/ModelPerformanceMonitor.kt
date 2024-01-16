package io.falu.identity.analytics

import io.falu.core.AnalyticsApiClient
import io.falu.identity.camera.Monitor
import io.falu.identity.camera.MonitorImpl
import io.falu.identity.camera.Statistic
import org.joda.time.Duration

/**
 * Monitor the performance of the model
 */
internal class ModelPerformanceMonitor(
    private val builder: IdentityAnalyticsRequestBuilder,
    private val apiClient: AnalyticsApiClient
) {
    private val preprocessingStats = mutableListOf<Statistic>()
    private val inferenceStats = mutableListOf<Statistic>()

    fun monitorPreProcessing(): Monitor =
        MonitorImpl { start, stats ->
            preprocessingStats.add(Statistic(start, Duration(start.toDateTime(), null), stats))
        }

    fun monitorInference(): Monitor =
        MonitorImpl { start, _ ->
            inferenceStats.add(Statistic(start, Duration(start.toDateTime(), null)))
        }

    /**
     * Calculate the average duration from a list of statistics
     */
    private fun List<Statistic>.duration() =
        (this.fold(Duration.ZERO) { duration, next -> duration + next.duration }.millis / size)

    fun reportModelPerformance(model: String) {
        apiClient.reportTelemetry(
            builder.modelPerformance(
                model,
                inference = inferenceStats.duration(),
                preprocessing = preprocessingStats.duration(),
                imageInfo = preprocessingStats.lastOrNull()?.result,
                frames = preprocessingStats.size
            ), IdentityAnalyticsRequestBuilder.ORIGIN
        )

        preprocessingStats.clear()
        inferenceStats.clear()
    }
}