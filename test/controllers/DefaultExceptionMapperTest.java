package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import devote.blockchain.api.BlockchainException;
import exceptions.BusinessLogicViolationException;
import exceptions.ForbiddenException;
import exceptions.NotFoundException;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;

import static extractors.GenericDataFromResult.jsonOf;
import static extractors.GenericDataFromResult.statusOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static play.mvc.Http.Status.*;

public class DefaultExceptionMapperTest {
    private static final Logger.ALogger logger = Logger.of(DefaultExceptionMapperTest.class);

    private DefaultExceptionMapper defaultExceptionMapper = new DefaultExceptionMapper(logger);

    @Test
    public void testBusinessLogicViolationException() {
        // Given
        ObjectNode errorObject = Json.newObject();
        Exception exception = new BusinessLogicViolationException(errorObject, "someMessage");

        // When
        Result result = defaultExceptionMapper.apply(exception);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
        JsonNode jsonOfResult = jsonOf(result);
        String jsonStrOfResult = jsonOfResult.toString();
        assertThat(jsonStrOfResult, containsString("someMessage"));
    }

    @Test
    public void testIllegalArgumentException() {
        // Given
        Exception exception = new IllegalArgumentException("someMessage");

        // When
        Result result = defaultExceptionMapper.apply(exception);

        // Then
        assertThat(statusOf(result), equalTo(BAD_REQUEST));
        JsonNode jsonOfResult = jsonOf(result);
        String jsonStrOfResult = jsonOfResult.toString();
        assertThat(jsonStrOfResult, containsString("someMessage"));
    }

    @Test
    public void testNotFoundException() {
        // Given
        Exception exception = new NotFoundException("someMessage");

        // When
        Result result = defaultExceptionMapper.apply(exception);

        // Then
        assertThat(statusOf(result), equalTo(NOT_FOUND));
    }

    @Test
    public void testForbiddenException() {
        // Given
        Exception exception = new ForbiddenException("someMessage");

        // When
        Result result = defaultExceptionMapper.apply(exception);

        // Then
        assertThat(statusOf(result), equalTo(FORBIDDEN));
    }

    @Test
    public void testBlockchainException() {
        // Given
        Exception exception = new BlockchainException("someMessage", new RuntimeException());

        // When
        Result result = defaultExceptionMapper.apply(exception);

        // Then
        assertThat(statusOf(result), equalTo(INTERNAL_SERVER_ERROR));
    }

    @Test
    public void testOtherException() {
        // Given
        Exception exception = new RuntimeException();

        // When
        Result result = defaultExceptionMapper.apply(exception);

        // Then
        assertThat(statusOf(result), equalTo(INTERNAL_SERVER_ERROR));
    }
}
