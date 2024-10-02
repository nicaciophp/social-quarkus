package io.quarkus.quarkussocial.dto;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.ws.rs.core.Response;
import lombok.Data;

@Data
public class ResponseError {
    
    public static final int UNPROCESSABLE_ENTITY_STATUS = 422;

    private String message;
    private Collection<FieldError> errors;

    

    public ResponseError(String message, Collection<FieldError> errors) {
        this.message = message;
        this.errors = errors;
    }

    public static <T> ResponseError createFromValidation(Set<ConstraintViolation<T>> violations) {

        List<FieldError> errors = violations
            .stream()
            .map( cv -> new FieldError(cv.getPropertyPath().toString(), cv.getMessage())).collect(Collectors.toList());

        String message = "Validation Error";
        var responseError = new ResponseError(message, errors);

        return responseError;
    }

    public Response withStatusCode(int code) {
        return Response.status(code).entity(this).build();
    }
    
}
