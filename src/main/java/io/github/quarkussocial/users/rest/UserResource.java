package io.github.quarkussocial.users.rest;

import io.github.quarkussocial.domain.repository.UserRepository;
import io.github.quarkussocial.users.rest.dto.CreateUserRequest;
import io.github.quarkussocial.users.rest.dto.ResponseError;
import io.github.quarkussocial.domain.model.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

import static io.github.quarkussocial.users.rest.dto.ResponseError.UNPROCESSABLE_ENTITY_STATUS;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private final  UserRepository userRepository;
    private final Validator validator;

    @Inject
    public UserResource(UserRepository userRepository, Validator validator) {
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @POST
    @Transactional
    public Response createUser(CreateUserRequest userRequest){

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);

        if(!violations.isEmpty()) {
            ResponseError responseError = ResponseError.createFromValidation(violations);
            return responseError.withStatusCode(UNPROCESSABLE_ENTITY_STATUS);
        }

        User user = new User();
        user.setAge(userRequest.getAge());
        user.setName(userRequest.getName());

        userRepository.persist(user);

        return Response
                .status(Response.Status.CREATED)
                .entity(user)
                .build();
    }

    @GET
    public Response listAllUsers(){
        PanacheQuery<User> query = userRepository.findAll();
        return Response.ok(query.list()).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id){
        User user = userRepository.findById(id);

        if(user != null){
            userRepository.delete(user);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, CreateUserRequest userData){
        User user = userRepository.findById(id);

        if(user != null) {
            user.setName(user.getName());
            user.setAge(user.getAge());

            userRepository.persist(user);

            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }


}
