package io.quarkus.quarkussocial.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.quarkus.quarkussocial.domain.model.Follower;
import io.quarkus.quarkussocial.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FollowerRepository implements PanacheRepository<Follower> {

    public boolean followes(User follower, User user) {
        Map<String,Object>  params= Parameters.with("follower", follower).and("user", user).map();

        PanacheQuery<Follower> query = find("follower =:follower and user=:user", params);
        Optional<Follower> result = query.firstResultOptional();
        
        return result.isPresent();
    }
    
    public List<Follower> findByUser(Long userId) {
        PanacheQuery<Follower> query = find("user.id", userId);
        return query.list();
    }

    public void deleteByFollowerAndUser(Long followerId, Long userId) {
        var params = Parameters.with("userId", userId).and("followerId", followerId).map();

        delete("follower.id =:followerId and user.id =:userId", params);
    }
}
