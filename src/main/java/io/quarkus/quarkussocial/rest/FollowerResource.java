package io.quarkus.quarkussocial.rest;

import java.util.stream.Collectors;

import io.quarkus.quarkussocial.domain.model.Follower;
import io.quarkus.quarkussocial.domain.model.User;
import io.quarkus.quarkussocial.domain.repository.FollowerRepository;
import io.quarkus.quarkussocial.domain.repository.UserRespository;
import io.quarkus.quarkussocial.dto.FollowerPerUserResponse;
import io.quarkus.quarkussocial.dto.FollowerRequest;
import io.quarkus.quarkussocial.dto.FollowerResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {
    
    private FollowerRepository repository;
    private UserRespository userRespository;

    @Inject
    public FollowerResource(FollowerRepository repository, UserRespository userRespository) {
        this.repository = repository;
        this.userRespository = userRespository;
    }

    @PUT
    @Transactional
    public Response followUser(@PathParam("userId") Long userId, FollowerRequest request) {

        if(userId.equals(request.getFollowerId())) {
            return Response.status(Response.Status.CONFLICT).entity("Não pode seguir a si mesmo").build();
        }

        var user = userRespository.findById(userId);
        var follow = userRespository.findById(request.getFollowerId());
        if(user == null || follow == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var follower = userRespository.findById(request.getFollowerId());

        boolean follows = repository.followes(follower, user);

        if(!follows) {
            var entity = new Follower();
            entity.setFollower(follower);
            entity.setUser(user);
    
            repository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    public Response getFollwer(@PathParam("userId") Long userId) {
        User user = userRespository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var list = repository.findByUser(userId);
        FollowerPerUserResponse responseObject = new FollowerPerUserResponse();
        responseObject.setFollowersCount(list.size());

        var followerList = list.stream().map(FollowerResponse::new).collect(Collectors.toList());

        responseObject.setContent(followerList);

        return Response.ok(responseObject).build();
    }

    @DELETE
    @Transactional
    public Response unfollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId) {
        User user = userRespository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        repository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
