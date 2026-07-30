package com.bangersoul.aivance.core.domain.usecase

/**
 * Base class for all Use Cases in the domain layer.
 *
 * @param <Input> The input type for the use case.
 * @param <Output> The output type for the use case.
 */
abstract class UseCase<in Input, Output> {
    /**
     * Executes the use case with the given input.
     *
     * @param input The input parameters.
     * @return The output result.
     */
    abstract suspend operator fun invoke(input: Input): Output
}

/**
 * A use case with no input parameters.
 */
abstract class NoInputUseCase<Output> {
    abstract suspend operator fun invoke(): Output
}

/**
 * A streaming use case that returns a [kotlinx.coroutines.flow.Flow].
 */
abstract class FlowUseCase<in Input, Output> {
    abstract operator fun invoke(input: Input): kotlinx.coroutines.flow.Flow<Output>
}

/**
 * A streaming use case with no input parameters.
 */
abstract class NoInputFlowUseCase<Output> {
    abstract operator fun invoke(): kotlinx.coroutines.flow.Flow<Output>
}
