package com.fullcars.restapi.service.excel;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fullcars.restapi.model.ProviderMapping;
import com.fullcars.restapi.repository.IProviderPartRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class ProviderExcelProcessor {

    private IProviderPartRepository providerPartsRepo;
    private FileMapper fileMapper;
    @Autowired
    private EntityManager em;
        
    public ProviderExcelProcessor(IProviderPartRepository providerPartsRepo, FileMapper fileMapper) {
		this.providerPartsRepo = providerPartsRepo;
		this.fileMapper = fileMapper;
	}

    //@Async
    @Transactional
    public void processExcel(File tempFile, ProviderMapping mapping) throws Exception {
    	providerPartsRepo.deleteByProviderMappingNative(mapping);
        providerPartsRepo.flush();

        AtomicInteger cant = new AtomicInteger(0);
        fileMapper.mapFile(tempFile, mapping, batch -> {
            providerPartsRepo.saveAll(batch);
            providerPartsRepo.flush(); // flush por batch
            em.clear();
            cant.addAndGet(batch.size());
        });
        System.gc();

        System.out.println("Procesamiento completo: " + cant + " partes insertadas.");
    }
    
}

