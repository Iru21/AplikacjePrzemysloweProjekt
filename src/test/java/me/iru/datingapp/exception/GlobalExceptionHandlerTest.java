package me.iru.datingapp.exception;

import me.iru.datingapp.dto.ErrorResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("User not found");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void testHandleResourceNotFoundException_WithResourceField() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Match", "id", 123L);

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        assert response.getBody() != null;
        assertThat(response.getBody().getMessage()).contains("Match");
        assertThat(response.getBody().getMessage()).contains("id");
        assertThat(response.getBody().getMessage()).contains("123");
    }

    @Test
    void testHandleUserAlreadyExistsException() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleUserAlreadyExistsException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("test@example.com");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    void testHandleUserAlreadyExistsException_DifferentEmails() {
        String[] emails = {"user1@example.com", "user2@test.org", "admin@company.com"};

        for (String email : emails) {
            UserAlreadyExistsException exception = new UserAlreadyExistsException(email);
            ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleUserAlreadyExistsException(exception, webRequest);

            assert response.getBody() != null;
            assertThat(response.getBody().getMessage()).contains(email);
        }
    }

    @Test
    void testHandleInvalidCredentialsException() {
        InvalidCredentialsException exception = new InvalidCredentialsException();

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleInvalidCredentialsException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    void testHandleInvalidCredentialsException_CustomMessage() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Account locked");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleInvalidCredentialsException(exception, webRequest);

        assert response.getBody() != null;
        assertThat(response.getBody().getMessage()).isEqualTo("Account locked");
    }

    @Test
    void testHandleMatchNotActiveException() {
        MatchNotActiveException exception = new MatchNotActiveException(123L);

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleMatchNotActiveException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("123");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
    }

    @Test
    void testHandleMatchNotActiveException_NoArgs() {
        MatchNotActiveException exception = new MatchNotActiveException();

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleMatchNotActiveException(exception, webRequest);

        assert response.getBody() != null;
        assertThat(response.getBody().getMessage()).isEqualTo("Cannot perform this operation on an inactive match");
    }


    @Test
    void testHandleFileStorageException() {
        FileStorageException exception = new FileStorageException("Failed to store file");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleFileStorageException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Failed to store file");
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    }

    @Test
    void testHandleFileStorageException_WithCause() {
        FileStorageException exception = new FileStorageException("Cannot write file", new IllegalStateException("Disk full"));

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleFileStorageException(exception, webRequest);

        assert response.getBody() != null;
        assertThat(response.getBody().getMessage()).isEqualTo("Cannot write file");
    }


    @Test
    void testHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(1024L);

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleMaxUploadSizeExceededException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("File size exceeds maximum allowed limit");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }


    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("user", "email", "Email is required");
        FieldError fieldError2 = new FieldError("user", "age", "Age must be at least 18");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleMethodArgumentNotValidException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed for one or more fields");
        assertThat(response.getBody().getValidationErrors()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).hasSize(2);
        assertThat(response.getBody().getValidationErrors().get("email")).isEqualTo("Email is required");
        assertThat(response.getBody().getValidationErrors().get("age")).isEqualTo("Age must be at least 18");
    }

    @Test
    void testHandleMethodArgumentNotValidException_SingleError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("user", "password", "Password is too short");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleMethodArgumentNotValidException(exception, webRequest);

        assert response.getBody() != null;
        assertThat(response.getBody().getValidationErrors()).hasSize(1);
        assertThat(response.getBody().getValidationErrors().get("password")).isEqualTo("Password is too short");
    }


    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid age range");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleIllegalArgumentException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid age range");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void testHandleIllegalArgumentException_DifferentMessages() {
        String[] messages = {
                "Min age cannot be greater than max age",
                "User already has this interest",
                "Invalid gender value"
        };

        for (String message : messages) {
            IllegalArgumentException exception = new IllegalArgumentException(message);
            ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleIllegalArgumentException(exception, webRequest);

            assert response.getBody() != null;
            assertThat(response.getBody().getMessage()).isEqualTo(message);
        }
    }


    @Test
    void testHandleGlobalException() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleGlobalException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    }

    @Test
    void testHandleGlobalException_NullPointerException() {
        NullPointerException exception = new NullPointerException("Null value encountered");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleGlobalException(exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assert response.getBody() != null;
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
    }

    @Test
    void testHandleGlobalException_RuntimeException() {
        RuntimeException exception = new RuntimeException("Some runtime error");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleGlobalException(exception, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Test
    void testPathExtraction_Standard() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users/123");
        ResourceNotFoundException exception = new ResourceNotFoundException("Not found");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        assert response.getBody() != null;
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/123");
    }

    @Test
    void testPathExtraction_NonUriFormat() {
        when(webRequest.getDescription(false)).thenReturn("some-other-format");
        ResourceNotFoundException exception = new ResourceNotFoundException("Not found");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        assert response.getBody() != null;
        assertThat(response.getBody().getPath()).isEqualTo("some-other-format");
    }

    @Test
    void testPathExtraction_EmptyUri() {
        when(webRequest.getDescription(false)).thenReturn("uri=");
        ResourceNotFoundException exception = new ResourceNotFoundException("Not found");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        assert response.getBody() != null;
        assertThat(response.getBody().getPath()).isEmpty();
    }


    @Test
    void testResponseContainsTimestamp() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Not found");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        assert response.getBody() != null;
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
    }


    @Test
    void testAllHandlersReturnConsistentStructure() {

        ResponseEntity<ErrorResponseDto> response1 = exceptionHandler.handleResourceNotFoundException(
                new ResourceNotFoundException("test"), webRequest);
        assertThat(response1.getBody()).isNotNull();
        assertThat(response1.getBody().getMessage()).isNotNull();
        assertThat(response1.getBody().getStatus()).isNotNull();
        assertThat(response1.getBody().getError()).isNotNull();
        assertThat(response1.getBody().getPath()).isNotNull();
        assertThat(response1.getBody().getTimestamp()).isNotNull();

        ResponseEntity<ErrorResponseDto> response2 = exceptionHandler.handleUserAlreadyExistsException(
                new UserAlreadyExistsException("test@example.com"), webRequest);
        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody().getMessage()).isNotNull();
        assertThat(response2.getBody().getStatus()).isNotNull();

        ResponseEntity<ErrorResponseDto> response3 = exceptionHandler.handleFileStorageException(
                new FileStorageException("test"), webRequest);
        assertThat(response3.getBody()).isNotNull();
        assertThat(response3.getBody().getMessage()).isNotNull();
    }

    @Test
    void testStatusCodesAreCorrect() {

        assertThat(exceptionHandler.handleResourceNotFoundException(
                new ResourceNotFoundException("test"), webRequest).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(exceptionHandler.handleUserAlreadyExistsException(
                new UserAlreadyExistsException("test@example.com"), webRequest).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(exceptionHandler.handleInvalidCredentialsException(
                new InvalidCredentialsException(), webRequest).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(exceptionHandler.handleMatchNotActiveException(
                new MatchNotActiveException(), webRequest).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(exceptionHandler.handleFileStorageException(
                new FileStorageException("test"), webRequest).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(exceptionHandler.handleIllegalArgumentException(
                new IllegalArgumentException("test"), webRequest).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(exceptionHandler.handleGlobalException(
                new Exception("test"), webRequest).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

