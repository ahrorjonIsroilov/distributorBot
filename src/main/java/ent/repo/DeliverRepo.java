package ent.repo;

import ent.entity.Deliver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliverRepo extends JpaRepository<Deliver, Long> {

    List<Deliver> getAllByDeleted(boolean deleted);
}
