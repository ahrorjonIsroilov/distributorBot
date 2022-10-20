package ent.repo;

import ent.entity.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GroupRepo extends JpaRepository<Group, Long> {

    @Transactional
    @Modifying
    void deleteByGroupId(Long groupId);

    Group findByGroupId(Long groupId);

    boolean existsByGroupId(Long groupId);

    List<Group> findAllBy(Pageable pageable);

    List<Group> getAllByAccepted(Boolean accepted);
}
