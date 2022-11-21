package io.falu.identity.camera

import io.falu.identity.ai.DetectionOutput

/**
 *
 */
internal interface AnalyzerBuilder<State, Output, Analyzer> {
    fun instance(result: ((Output) -> Unit)): Analyzer
}

/** Helper type alias used for analysis use case callbacks */
internal typealias AnalyzerOutputListener = (result: DetectionOutput) -> Unit