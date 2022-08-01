/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@file:JvmName("Vader")

package org.revcloud.vader.runner

import org.revcloud.vader.lift.liftAllToEtr
import org.revcloud.vader.runner.config.ContainerValidationConfig
import org.revcloud.vader.runner.config.ExecutionStrategy.FAIL_FAST
import org.revcloud.vader.runner.config.ValidationConfig
import org.revcloud.vader.types.Validator
import org.revcloud.vader.types.ValidatorEtr
import java.util.Optional

class Vader {
  companion object {
    /** <--- CONTAINER --- */
    @JvmStatic
    @JvmOverloads
    fun <FailureT : Any, ContainerValidatableT> validateAndFailFastForContainer(
      container: ContainerValidatableT,
      containerValidationConfig: ContainerValidationConfig<ContainerValidatableT, FailureT?>,
      throwableMapper: (Throwable) -> FailureT? = { throw it }
    ): Optional<FailureT> = failFastForContainer(containerValidationConfig, throwableMapper)(container)

    @JvmStatic
    @JvmOverloads
    fun <FailureT : Any, ContainerValidatableT, NestedContainerValidatableT> validateAndFailFastForContainer(
      container: ContainerValidatableT,
      containerValidationConfigWith2Levels: ContainerValidationConfigWith2Levels<ContainerValidatableT, NestedContainerValidatableT, FailureT?>,
      throwableMapper: (Throwable) -> FailureT? = { throw it }
    ): Optional<FailureT> = failFastForContainer(containerValidationConfigWith2Levels, throwableMapper)(container)

    /** --- CONTAINER ---> */

    @JvmStatic
    @JvmOverloads
    fun <FailureT : Any, ValidatableT> validate(
      validatable: ValidatableT,
      validationConfig: ValidationConfig<ValidatableT, FailureT?>,
      throwableMapper: (Throwable) -> FailureT? = { throw it }
    ): Optional<FailureT> = when (validationConfig.executionStrategy) {
      FAIL_FAST -> failFast(validationConfig, throwableMapper)(validatable)
      else -> throw IllegalArgumentException("Execution strategy must be specified or call specific strategy methods")
    }

    @JvmStatic
    @JvmOverloads
    fun <FailureT : Any, ValidatableT> validateAndFailFast(
      validatable: ValidatableT,
      validationConfig: ValidationConfig<ValidatableT, FailureT?>,
      throwableMapper: (Throwable) -> FailureT? = { throw it }
    ): Optional<FailureT> = failFast(validationConfig, throwableMapper)(validatable)

// --- ERROR ACCUMULATION ---
    /**
     * Applies the Simple validators on a Single validatable in error-accumulation mode.
     *
     * @param validatable
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param none               Value to be returned in case of no failures.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return List of Validation failures.
     </ValidatableT></FailureT> */
    @JvmStatic
    @JvmOverloads
    fun <FailureT, ValidatableT> validateAndAccumulateErrors(
      validatable: ValidatableT,
      validators: Collection<Validator<ValidatableT?, FailureT?>>,
      none: FailureT,
      throwableMapper: (Throwable) -> FailureT? = { throw it }
    ): List<FailureT?> =
      validateAndAccumulateErrors(validatable, liftAllToEtr(validators, none), none, throwableMapper)

    /**
     * Applies the validators on a Single validatable in error-accumulation mode. The Accumulated
     *
     * @param validatable
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param none               Value to be returned in case of no failures.
     * @param <FailureT>
     * @param <ValidatableT>
     * @param throwableMapper   Function to map throwable to Failure in case of exception
     * @return List of Validation failures. EmptyList if all the validations pass.
     </ValidatableT></FailureT> */
    @JvmStatic
    @JvmOverloads
    fun <FailureT, ValidatableT> validateAndAccumulateErrors(
      validatable: ValidatableT,
      validators: List<ValidatorEtr<ValidatableT?, FailureT?>>,
      none: FailureT,
      throwableMapper: (Throwable) -> FailureT? = { throw it }
    ): List<FailureT?> {
      val results = accumulationStrategy(validators, throwableMapper)(validatable)
        .map { result -> result.fold({ it }, { none }) }
      return if (results.all { it == none }) emptyList() else results
    }
  }
}
