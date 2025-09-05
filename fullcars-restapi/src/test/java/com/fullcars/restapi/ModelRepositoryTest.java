package com.fullcars.restapi;
import static org.assertj.core.api.Assertions.assertThat;

import java.rmi.ServerException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.fullcars.restapi.model.Brand;
import com.fullcars.restapi.model.Model;
import com.fullcars.restapi.service.BrandService;
import com.fullcars.restapi.service.ModelService;

@DataJpaTest
public class ModelRepositoryTest {

    @Autowired
    private ModelService modelRepository;

    @Autowired
    private BrandService brandRepository;

    @Test
    void testPersistAndFindModel() throws ServerException {
        Brand brand = new Brand();
        brand.setName("Honda");
        brand = brandRepository.save(brand);

        Model model = new Model();
        model.setName("Civic");
        model.setBrand(brand);
        model = modelRepository.save(model);

        Model found = modelRepository.findByIdOrThrow(model.getId());
        assertThat(found.getName()).isEqualTo("Civic");
        assertThat(found.getBrand().getName()).isEqualTo("Honda");
    }
}

