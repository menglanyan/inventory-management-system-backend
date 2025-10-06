package com.github.menglanyan.inventory_management.services.impl;

import com.github.menglanyan.inventory_management.dtos.Response;
import com.github.menglanyan.inventory_management.dtos.SupplierDTO;
import com.github.menglanyan.inventory_management.entities.Supplier;
import com.github.menglanyan.inventory_management.exceptions.NotFoundException;
import com.github.menglanyan.inventory_management.repositories.SupplierRepository;
import com.github.menglanyan.inventory_management.services.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    private final ModelMapper modelMapper;

    @Override
    public Response addSupplier(SupplierDTO supplierDTO) {

        Supplier supplierToSave = modelMapper.map(supplierDTO, Supplier.class);

        supplierRepository.save(supplierToSave);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Supplier Added Successfully")
                .build();

    }

    @Override
    public Response updateSupplier(Long id, SupplierDTO supplierDTO) {

        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        if (supplierDTO.getName() != null) {
            existingSupplier.setName(supplierDTO.getName());
        }

        if (supplierDTO.getContactInfo() != null) {
            existingSupplier.setContactInfo(supplierDTO.getContactInfo());
        }

        if (supplierDTO.getAddress() != null) {
            existingSupplier.setAddress(supplierDTO.getAddress());
        }

        supplierRepository.save(existingSupplier);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Supplier Updated Successfully")
                .build();
    }

    @Override
    public Response getAllSuppliers() {

        List<Supplier> suppliers = supplierRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<SupplierDTO> supplierDTOList = modelMapper.map(suppliers, new TypeToken<List<SupplierDTO>>() {}.getType());

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Suppliers Retrieved Successfully")
                .suppliers(supplierDTOList)
                .build();

    }

    @Override
    public Response getSupplierById(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        SupplierDTO supplierDTO = modelMapper.map(supplier, SupplierDTO.class);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Supplier Retrieved Successfully")
                .supplier(supplierDTO)
                .build();
    }

    @Override
    public Response deleteSupplier(Long id) {

        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        supplierRepository.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Supplier Deleted Successfully")
                .build();

    }
}
