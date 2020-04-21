/*
 * Copyright 2020 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */

package sample.consumer.config;


import sample.consumer.BaseService;
import sample.consumer.Service;
import sample.consumer.representation.ParentInputRepresentation;
import sample.consumer.failure.ValidationFailure;
import io.vavr.collection.List;
import org.qtc.delphinus.types.validators.Validator;


/**
 * gakshintala created on 4/13/20.
 */
public class ServiceConfig {
    BaseService<ParentInputRepresentation> getService(List<Validator<ParentInputRepresentation, ValidationFailure>> requestValidators) {
        final Service service = new Service();
        service.setRequestValidators(requestValidators);
        return service;
    }
}