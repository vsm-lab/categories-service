package de.hska.vslab.categories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CategoryController {

    @Value("${product-service.base-url}")
    private String productBaseUrl;

    @Autowired
    private CategoryRepository categoryRepository;

    private static final RestTemplate restTemplate = new RestTemplate();

    @PostMapping(path = "/categories") // Map ONLY POST Requests
    public ResponseEntity<String> addNewCategory(@RequestParam(value = "name") String name) {
        try {
            Category category = new Category();
            category.setName(name);
            categoryRepository.save(category);
            return new ResponseEntity<>("Saved", HttpStatus.OK);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping(path = "/categories")
    public ResponseEntity<Iterable<Category>> getAllCategories() {
        try {
            return new ResponseEntity<>(categoryRepository.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping(path = "/categories/{categoryId}")
    public ResponseEntity<Category> getCategoryById(@PathVariable(value = "categoryId") Integer categoryId) {
        try {
            var optionalCategory = categoryRepository.findById(categoryId);
            if (optionalCategory.isPresent()) {
                return new ResponseEntity<>(optionalCategory.get(), HttpStatus.OK);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "categoryId not found");
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @DeleteMapping(path = "categories/{categoryId}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable(value = "categoryId") Integer categoryId) {
        try {
            var response = restTemplate.exchange(productBaseUrl + "/products-by-category/" + categoryId, HttpMethod.DELETE, null, Void.class);
            assert (response.getStatusCode().is2xxSuccessful());
            categoryRepository.deleteById(categoryId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            if (ex instanceof HttpClientErrorException &&
                    ((HttpClientErrorException) ex).getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Products that use this category could not be deleted.");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}