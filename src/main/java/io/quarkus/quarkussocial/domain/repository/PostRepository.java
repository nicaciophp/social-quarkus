package io.quarkus.quarkussocial.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.quarkussocial.domain.model.Post;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PostRepository implements PanacheRepository<Post> {
    
}
