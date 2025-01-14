/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.config.base

enum class ExecutionStrategy {
  FAIL_FAST
}

enum class BatchExecutionStrategy {
  FAIL_FAST_FOR_EACH,
  FAIL_FAST_FOR_ANY
}
