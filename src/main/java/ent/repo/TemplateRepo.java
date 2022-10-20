package ent.repo;

import ent.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TemplateRepo extends JpaRepository<Template, Long> {
    Template findFirstByDate(String date);

    Boolean existsByDateAndEditedTrue(String date);

    Boolean existsByDate(String date);

    @Transactional
    @Modifying
    void deleteByDate(String date);
}
