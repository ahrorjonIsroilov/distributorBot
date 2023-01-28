package ent.repo;

import ent.entity.Deliver;
import ent.entity.product.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    List<Product> getAllByDayAndTotalCountGreaterThan(String day, double totalCount);

    List<Product> getAllByDay(String day, Pageable pageable);
    List<Product> getAllByDayAndDeliversIn(String day, List<Deliver> delivers,Pageable pageable);

    boolean existsByEdited(boolean edited);
}
