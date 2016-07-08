package com.dev.ops.common.service.exception;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

@RunWith(MockitoJUnitRunner.class)
public class ServiceExceptionControllerAdviceTest {

	private final ServiceExceptionControllerAdvice advice = new ServiceExceptionControllerAdvice();

	@Mock
	private MethodArgumentNotValidException mockedException;

	@Mock
	private BindingResult result;

	@Test
	public void testHandleValidationException() {
		final String exceptionMessage = "Field error in object 'objectName' on field 'field': rejected value [null] with cause: [defaultMessage ]";

		Mockito.doReturn(this.result).when(this.mockedException).getBindingResult();
		Mockito.doReturn(this.getDummyFieldErrors()).when(this.result).getFieldErrors();

		final ResponseEntity<Object> responseEntity = this.advice.handleMethodArgumentNotValid(this.mockedException, Mockito.mock(HttpHeaders.class), HttpStatus.BAD_REQUEST, Mockito.mock(WebRequest.class));
		Assert.assertNotNull(responseEntity);
		Assert.assertEquals("The error should be 400 Bad Request", HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		Assert.assertEquals("The error should be equal", exceptionMessage, responseEntity.getBody());
	}

	private List<FieldError> getDummyFieldErrors() {
		final List<FieldError> fieldErrors = new ArrayList<FieldError>();
		fieldErrors.add(new FieldError("objectName", "field", "defaultMessage"));
		return fieldErrors;
	}

	@Test
	public void testHandlePersistenceException() {
		final String exceptionMessage = "DATABASE_PERSISTENCE_EXCEPTION Problem with accessing the database connection/objects.";
		final Exception exception = new Exception(exceptionMessage);
		final ResponseEntity<Object> responseEntity = this.advice.handlePersistenceException(exception, Mockito.mock(WebRequest.class));
		Assert.assertNotNull(responseEntity);
		Assert.assertEquals("The error should be 500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
		Assert.assertEquals("The error should be equal", exceptionMessage, responseEntity.getBody());
	}

	@Test
	public void testHandleServiceException() {
		final String exceptionMessage = "Test general exception";
		final Exception exception = new Exception(exceptionMessage);
		final ResponseEntity<Object> responseEntity = this.advice.handleServiceException(exception, Mockito.mock(WebRequest.class));
		Assert.assertNotNull(responseEntity);
		Assert.assertEquals("The error should be 500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
		Assert.assertEquals("The error should be equal", exceptionMessage, responseEntity.getBody());
	}
}
