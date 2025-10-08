package com.github.menglanyan.inventory_management.services.impl;

import com.github.menglanyan.inventory_management.dtos.ProductDTO;
import com.github.menglanyan.inventory_management.dtos.Response;
import com.github.menglanyan.inventory_management.entities.Category;
import com.github.menglanyan.inventory_management.entities.Product;
import com.github.menglanyan.inventory_management.exceptions.NotFoundException;
import com.github.menglanyan.inventory_management.repositories.CategoryRepository;
import com.github.menglanyan.inventory_management.repositories.ProductRepository;
import com.github.menglanyan.inventory_management.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final ModelMapper modelMapper;

    private static final String IMAGE_DIRECTORY = System.getProperty("user.dir") + "/product-images/";


    @Override
    public Response saveProduct(ProductDTO productDTO, MultipartFile imageFile) {

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        Product productToSave = Product.builder()
                .name(productDTO.getName())
                .sku(productDTO.getSku())
                .price(productDTO.getPrice())
                .stockQuantity(productDTO.getStockQuantity())
                .description(productDTO.getDescription())
                .category(category)
                .build();

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            productToSave.setImageUrl(imagePath);
        }

        productRepository.save(productToSave);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Product Saved Successfully")
                .build();

    }

    @Override
    public Response updateProduct(ProductDTO productDTO, MultipartFile imageFile) {

        Product existingProduct = productRepository.findById(productDTO.getProductId())
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            existingProduct.setImageUrl(imagePath);
        }

        if (productDTO.getCategoryId() != null && productDTO.getCategoryId() > 0) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category Not Found"));
            existingProduct.setCategory(category);
        }

        if (productDTO.getName() != null && !productDTO.getName().isBlank()) {
            existingProduct.setName(productDTO.getName());
        }

        if (productDTO.getSku() != null && !productDTO.getSku().isBlank()) {
            existingProduct.setSku(productDTO.getSku());
        }

        if (productDTO.getDescription() != null && !productDTO.getDescription().isBlank()) {
            existingProduct.setDescription(productDTO.getDescription());
        }

        if (productDTO.getPrice() != null && productDTO.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
            existingProduct.setPrice(productDTO.getPrice());
        }

        if (productDTO.getStockQuantity() != null && productDTO.getStockQuantity() >= 0) {
            existingProduct.setStockQuantity(productDTO.getStockQuantity());
        }

        productRepository.save(existingProduct);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Product Updated Successfully")
                .build();

    }

    @Override
    public Response getAllProducts() {

        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<ProductDTO> productDTOList = modelMapper.map(products, new TypeToken<List<ProductDTO>>() {}.getType());

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Products Retrieved Successfully")
                .products(productDTOList)
                .build();

    }

    @Override
    public Response getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Product Retrieved Successfully")
                .product(productDTO)
                .build();

    }

    @Override
    public Response deleteProduct(Long id) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        productRepository.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Product Deleted Successfully")
                .build();

    }

    @Override
    public Response searchProduct(String input) {

        List<Product> products = productRepository.findByNameContainingOrDescriptionContaining(input, input);

        if (products.isEmpty()) {
            throw new NotFoundException("Products Not Found");
        }

        List<ProductDTO> productDTOList = modelMapper.map(products, new TypeToken<List<ProductDTO>>() {}.getType());

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Products Retrieved Successfully")
                .products(productDTOList)
                .build();

    }

    private String saveImage(MultipartFile imageFile) {

        // Validate if it is image file and greater than 1GB
        if (!imageFile.getContentType().startsWith("image/") || imageFile.getSize() > 1024L * 1024L * 1024L) {
            throw new IllegalArgumentException("Only image file under 1GB is allowed");
        }

        // Create the directory if it does not exist
        File directory = new File(IMAGE_DIRECTORY);

        if (!directory.exists()) {
            directory.mkdir();
            log.info("Directory is created");
        }

        // Generate unique file name for the image, prevent filename collisions
        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

        // Get the absolute path of the image
        String imagePath = IMAGE_DIRECTORY + uniqueFileName;

        try {
            File destinationFile = new File(imagePath);

            // Write the image to the folder
            imageFile.transferTo(destinationFile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving image" + e.getMessage());
        }

        return imagePath;

    }
}
