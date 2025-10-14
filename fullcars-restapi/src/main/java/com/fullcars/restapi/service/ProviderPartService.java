package com.fullcars.restapi.service;

import java.rmi.ServerException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.dto.ProviderPartDTO;
import com.fullcars.restapi.model.Brand;
import com.fullcars.restapi.model.CarPart;
import com.fullcars.restapi.model.Category;
import com.fullcars.restapi.model.ProviderPart;

@Service
public class ProviderPartService {

	private final ProviderService providerService;
	private final CarPartService carPartService;
	private final BrandService brandService;
	private final CategoryService categoryService;

	public ProviderPartService(CarPartService carPartService, ProviderService providerService, BrandService brandService, CategoryService categoryService) {
		this.providerService = providerService;
		this.carPartService = carPartService;
		this.brandService = brandService;
		this.categoryService = categoryService;
	}
	@Transactional
	public CarPart findOrCreateFromProvider(ProviderPartDTO providerPart) throws ServerException {
        // Search for an existing CarPart linked by providerCode and providerId
        Optional<CarPart> existing = carPartService.findByProviderCodeAndProviderId(
                providerPart.provCod(),
                providerPart.providerId(),
                providerPart.nombre()
        );
        
        if (existing.isPresent()) 
            return existing.get();
        
        // Otherwise, create a new CarPart initialized from provider data
        Brand brand = brandService.getBrandByNameOrCreate(providerPart.marca());
        Category cat = categoryService.getCategoryByNameOrCreate(providerPart.category());
        
        CarPart newCarPart = new CarPart();
        newCarPart.setName(providerPart.nombre());
        newCarPart.setBrand(brand);
        newCarPart.setCategory(cat);
        newCarPart.setProviderSku(providerPart.provCod());
        newCarPart.setProvider(providerService.findByIdOrThrow(providerPart.providerId()));
        newCarPart.setBasePrice(providerPart.precio());
        newCarPart.setQuality(providerPart.quality());
        newCarPart.setStock(0L); // new parts start with no stock

        return carPartService.save(newCarPart);
    }

}
