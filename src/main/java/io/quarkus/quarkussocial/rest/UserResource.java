package io.quarkus.quarkussocial.rest;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.quarkussocial.domain.model.User;
import io.quarkus.quarkussocial.domain.repository.UserRespository;
import io.quarkus.quarkussocial.dto.CreateUserRequest;
import io.quarkus.quarkussocial.dto.ResponseError;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private UserRespository repository;
    private Validator validator;

    @Inject
    public UserResource(UserRespository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;}

    @POST
    @Transactional
    public Response createUser(CreateUserRequest userRequest) {
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);

        if(!violations.isEmpty()) {
            return ResponseError
                .createFromValidation(violations)
                .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }

        User user = new User();

        user.setAge(userRequest.getAge());
        user.setName(userRequest.getName());

        repository.persist(user);
        
        return Response
            .status(Response.Status.CREATED.getStatusCode())
            .entity(user)
            .build();
    }

    @GET
    public Response getAllUsers() {
        PanacheQuery<User> query = repository.findAll();
        return Response.ok(query.list()).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id) {
        User user = repository.findById(id);

        if(user != null) {
            repository.delete(user);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, CreateUserRequest userData) {
        User user = repository.findById(id);
        if(user != null) {
            user.setName(userData.getName());
            user.setAge(userData.getAge());
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}