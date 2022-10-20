package ent.repo.auth;

import ent.entity.auth.AuthUser;
import ent.enums.Role;
import ent.repo.BaseRepo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface AuthRepo extends BaseRepo, JpaRepository<AuthUser, Long> {

    void deleteByChatId(Long chatId);

    Optional<AuthUser> findByChatId(Long chatId);

    AuthUser findByChatIdAndRegisteredTrue(Long chatId);

    @Transactional
    @Modifying
    @Query("update AuthUser set blocked=:status where id=:id")
    void setUserBlockedStatus(
            @Param("id") Long id,
            @Param("status") Boolean status);


    Boolean existsByUsernameAndRegisteredFalse(String username);

    Boolean existsByUsername(String username);

    AuthUser findByUsername(String username);

    Boolean existsByChatIdAndRegisteredTrue(Long chatId);

    Boolean existsByChatIdAndRegisteredFalse(Long chatId);

    List<AuthUser> findAllByRole(Role role, Pageable pageable);

    List<AuthUser> findAllByRole(Role role);
}
