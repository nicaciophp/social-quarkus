package io.quarkus.quarkussocial.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.quarkussocial.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRespository implements PanacheRepository<User>{
    
}
