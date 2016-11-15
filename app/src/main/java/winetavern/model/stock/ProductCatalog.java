package winetavern.model.stock;

import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * @author Louis
 */
public interface ProductCatalog extends Catalog<Product>{
    @Query(value = "select * from Product where PRODUCT_ID=?1", nativeQuery = true)
    Product findOne(String id);
}
