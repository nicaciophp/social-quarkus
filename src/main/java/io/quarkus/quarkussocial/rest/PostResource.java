package io.quarkus.quarkussocial.rest;

import java.util.stream.Collectors;

import io.quarkus.panache.common.Sort;
import io.quarkus.quarkussocial.domain.model.Post;
import io.quarkus.quarkussocial.domain.model.User;
import io.quarkus.quarkussocial.domain.repository.FollowerRepository;
import io.quarkus.quarkussocial.domain.repository.PostRepository;
import io.quarkus.quarkussocial.domain.repository.UserRespository;
import io.quarkus.quarkussocial.dto.CreatePostRequest;
import io.quarkus.quarkussocial.dto.PostResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

    private UserRespository userRespository;
    private PostRepository postRepository;
    private FollowerRepository followerRepository;

    @Inject
    public PostResource(UserRespository userRepository, PostRepository postRepository, FollowerRepository followerRepository) {
        this.userRespository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }
    
    @POST
    @Transactional
    public Response savePost(@PathParam("userId") Long userId, CreatePostRequest request) {
        User user = userRespository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Post post = new Post();
        post.setText(request.getText());
        post.setUser(user);
        postRepository.persist(post);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    public Response listPost(@PathParam("userId") Long userId, @HeaderParam("followerId") Long followerId) {
        User user = userRespository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if(followerId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Você esqueceu de informar o id do seguidor no cabeçalho").build();
        }

        User follower = userRespository.findById(followerId);

        if(follower == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Follower inexistente").build();
        }

        boolean follows = followerRepository.followes(follower, user);
        
        if(!follows) {
            return Response.status(Response.Status.FORBIDDEN).entity("Você não pode ver esses posts").build();
        }

        var query = postRepository.find("user", Sort.by("dateTime", Sort.Direction.Descending), user);
        var list = query.list();

        var postResponseList = list.stream()
            .map(PostResponse::fromEntity)
            .collect(Collectors.toList());
        return Response.ok(postResponseList).build();
    }

}
