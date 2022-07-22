/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.specs

import org.revcloud.vader.specs.factory.SpecFactory
import org.revcloud.vader.specs.specs.BaseSpec

fun interface Spec<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>

fun interface Specs<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, Collection<BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>>
