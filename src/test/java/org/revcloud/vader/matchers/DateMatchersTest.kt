/*
 * Copyright 2021 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */

package org.revcloud.vader.matchers

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.revcloud.vader.matchers.DateMatchers.isEqualToDayOfDate
import java.util.*

class DateMatchersTest : StringSpec({
    "is Date's day matching" {
        forAll(
            row(1, GregorianCalendar(2014, Calendar.FEBRUARY, 1).time, true),
            row(2, GregorianCalendar(2014, Calendar.FEBRUARY, 1).time, false),
            row(null, GregorianCalendar(2014, Calendar.FEBRUARY, 1).time, false),
            row(null, null, false),
        ) { day, date, result ->
            isEqualToDayOfDate().apply(day, date) shouldBe result
        }
    }
})