package controllers;

import com.google.common.base.Function;
import devote.blockchain.api.BlockchainException;
import exceptions.BusinessLogicViolationException;
import exceptions.ForbiddenException;
import exceptions.NotFoundException;
import play.Logger;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Result;

import static play.mvc.Results.*;


public class DefaultExceptionMapper implements Function<Throwable, Result> {
    private Logger.ALogger logger;

    public DefaultExceptionMapper(Logger.ALogger logger) {
        this.logger = logger;
    }

    @Override
    public Result apply(Throwable input) {
        logger.info("apply(): input class = {}", input.getClass().getCanonicalName());
        if (input instanceof BusinessLogicViolationException) {
            logger.warn("Bad Request due to business logic violation!", input);
            return badRequest(((BusinessLogicViolationException) input).errorContent);
        }

        if (input instanceof IllegalArgumentException) {
            logger.warn("Bad Request!", input);
            ValidationError ve = new ValidationError("", input.getMessage());
            return badRequest(Json.toJson(ve.messages()));
        }

        if (input instanceof NotFoundException) {
            logger.warn("Not Found!", input);
            return notFound();
        }

        if (input instanceof ForbiddenException) {
            logger.warn("Forbidden!", input);
            ValidationError ve = new ValidationError("", input.getMessage());
            return forbidden(Json.toJson(ve.messages()));
        }

        if(input instanceof BlockchainException) {
            logger.warn("Blockchain exception!", input);
            return internalServerError(input.getMessage());
        }

        logger.error("Internal Error!", input);
        return internalServerError();
    }
}
