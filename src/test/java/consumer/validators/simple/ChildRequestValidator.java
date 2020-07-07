/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.simple;


import consumer.failure.ValidationFailure;
import consumer.representation.Child;
import org.qtc.delphinus.types.validators.simple.SimpleThrowableValidator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;
import consumer.failure.ApiErrorCodes;
import consumer.failure.ValidationFailureMessage;

public class ChildRequestValidator {

    static final String ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID
            = "PaymentStandardFields.PaymentAuthorizationId.getName()";
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final SimpleValidator<Child, ValidationFailure> validation1 =
            child -> {
                if (child._isSetPaymentAuthorizationId()) {
                    return null;
                } else {
                    return new ValidationFailure(ApiErrorCodes.REQUIRED_FIELD_MISSING, ValidationFailureMessage.FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID);
                }
            };

    static final SimpleValidator<Child, ValidationFailure> validation2 =
            child -> {
                if (child._isSetPaymentAuthorizationId()) {
                    return null;
                } else {
                    return new ValidationFailure(ApiErrorCodes.REQUIRED_FIELD_MISSING, ValidationFailureMessage.FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID);
                }
            };

    static final SimpleThrowableValidator<Child, ValidationFailure> validationThrowable1 =
            child -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

    static final SimpleThrowableValidator<Child, ValidationFailure> validationThrowable2 =
            child -> {
                throw new IllegalArgumentException("2 - you did something illegal again");
            };

}