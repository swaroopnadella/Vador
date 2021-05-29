@file:JvmName("Utils")

package org.revcloud.vader.runner

import io.vavr.CheckedFunction1.liftTry
import io.vavr.Function1.identity
import io.vavr.control.Either
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

@JvmSynthetic
fun <FailureT, ValidatableT> fireValidators(
  validatable: Either<FailureT?, ValidatableT?>,
  validatorEtrs: List<ValidatorEtr<ValidatableT, FailureT>>,
  throwableMapper: (Throwable) -> FailureT?,
): List<Either<FailureT?, ValidatableT?>> =
  validatorEtrs.map { fireValidator(it, validatable, throwableMapper) }

@JvmSynthetic
fun <FailureT, ValidatableT> fireValidator(
  validatorEtr: ValidatorEtr<ValidatableT, FailureT>,
  validatable: Either<FailureT?, ValidatableT?>,
  throwableMapper: (Throwable) -> FailureT?,
): Either<FailureT?, ValidatableT?> =
  liftTry(validatorEtr).apply(validatable)
    .fold({ left<FailureT?, ValidatableT?>(throwableMapper(it)) }) { it }
    .flatMap { validatable } // Put the original Validatable in the right state

@JvmSynthetic
fun <FailureT> validateBatchSize(
  validatables: Collection<*>,
  headerConfig: HeaderValidationConfig<*, FailureT>
): Optional<FailureT> {
  val minBatchSize = headerConfig.shouldHaveMinBatchSize
  if (minBatchSize != null && validatables.size < minBatchSize._1) {
    return Optional.ofNullable(minBatchSize._2)
  }
  val maxBatchSize = headerConfig.shouldHaveMaxBatchSize
  return if (maxBatchSize != null && validatables.size > maxBatchSize._1) {
    Optional.ofNullable(maxBatchSize._2)
  } else Optional.empty()
}

fun <ValidatableT, FailureT> filterNullValidatablesAndDuplicates(
  validatables: List<ValidatableT?>,
  nullValidatable: FailureT?,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>
): List<Either<FailureT?, ValidatableT?>> {
  if (validatables.isEmpty()) {
    return emptyList()
  } else if (validatables.size == 1) {
    val validatable = validatables[0]
    return if (validatable == null) listOf(left(nullValidatable)) else listOf(right(validatables[0]))
  }
  val duplicateFinder = batchValidationConfig.findAndFilterDuplicatesWith
  val keyMapperForDuplicates = duplicateFinder ?: identity()
  val groups = validatables.withIndex().groupBy { (_, validatable) ->
    if (validatable == null) null else Optional.ofNullable(
      keyMapperForDuplicates.apply(
        validatable
      )
    )
  }
  val invalids: List<Pair<Int, Either<FailureT?, ValidatableT?>>> =
    groups[null]?.map { (index, _) -> index to left(nullValidatable) } ?: emptyList()

  // TODO 11/04/21 gopala.akshintala: add test 
  if (duplicateFinder == null) { // Skip the rest if duplicateFinder is not defined
    val valids: List<Pair<Int, Either<FailureT?, ValidatableT?>>> =
      groups.filterKeys { it != null }.values.flatten()
        .map { (index, validatable) -> index to right(validatable) }
    return (valids + invalids).sortedBy { it.first }.map { it.second }
  }
  val failureForNullKeys = batchValidationConfig.andFailNullKeysWith
  val withNullKeys: List<Pair<Int, Either<FailureT?, ValidatableT?>>> =
    groups[Optional.empty()]?.map { (index, validatable) ->
      index to if (failureForNullKeys == null) right(validatable) else left(failureForNullKeys)
    } ?: emptyList()
  val partition =
    groups.filterKeys { it != null && it.isPresent }.values.partition { it.size == 1 }
  val failureForDuplicate = batchValidationConfig.andFailDuplicatesWith
  val duplicates: List<Pair<Int, Either<FailureT?, ValidatableT?>>> =
    failureForDuplicate?.let {
      partition.second.flatten().map { (index, _) -> index to left(it) }
    } ?: emptyList()

  val nonDuplicates: List<Pair<Int, Either<FailureT?, ValidatableT?>>> =
    partition.first.flatten().map { (index, validatable) -> index to right(validatable) }
  return (nonDuplicates + duplicates + invalids + withNullKeys).sortedBy { it.first }
    .map { it.second }
}

fun <ValidatableT, FailureT> filterNullValidatablesAndDuplicatesForAllOrNone(
  validatables: List<ValidatableT?>,
  nullValidatable: FailureT?,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>
): Optional<FailureT> {
  if (validatables.isEmpty()) {
    return Optional.empty()
  } else if (validatables.size == 1) {
    val validatable = validatables[0]
    return if (validatable == null) Optional.ofNullable(nullValidatable) else Optional.empty()
  }
  val duplicateFinder = batchValidationConfig.findAndFilterDuplicatesWith
  val keyMapperForDuplicates = duplicateFinder ?: identity()
  val groups = validatables
    .groupBy { if (it == null) null else Optional.ofNullable(keyMapperForDuplicates.apply(it)) }
  val invalids = groups[null]
  if (invalids != null && invalids.isNotEmpty()) {
    return Optional.ofNullable(nullValidatable)
  }
  val invalidsWithNullKeys = groups[Optional.empty()]
  if (invalidsWithNullKeys != null && invalidsWithNullKeys.isNotEmpty()) {
    return Optional.ofNullable(batchValidationConfig.andFailNullKeysWith)
  }
  val valids = groups.filterKeys { it != null && it.isPresent }.values
  val failureForDuplicate = batchValidationConfig.andFailDuplicatesWith
  if (duplicateFinder != null && failureForDuplicate != null && valids.any { it.size > 1 }) {
    return Optional.of(failureForDuplicate)
  }
  return Optional.empty()
}

@JvmSynthetic
internal fun <FailureT, ValidatableT> findFirstFailure(
  validatable: Either<FailureT?, ValidatableT?>,
  validationConfig: BaseValidationConfig<ValidatableT, FailureT>,
  throwableMapper: (Throwable) -> FailureT?,
): Either<FailureT?, ValidatableT?> =
  fireValidators(
    validatable,
    toValidators(validationConfig),
    throwableMapper
  ).firstOrNull { it.isLeft }
    ?: validatable
