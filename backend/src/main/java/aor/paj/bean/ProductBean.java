package aor.paj.bean;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import aor.paj.dao.CategoryDao;
import aor.paj.dao.ProductDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.EstadosDoProduto;
import aor.paj.dto.ProductDto;
import aor.paj.entity.CategoryEntity;
import aor.paj.entity.ProductEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class ProductBean implements Serializable {
    private static final long serialVersionUID = 1L;
    // private static final Logger logger= LogManager.getLogger(ProductBean.class);
    @EJB
    ProductDao productDao;
    @EJB
    UserDao userDao;
    @EJB
    CategoryDao categoryDao;
    @EJB
    private CategoryBean categoryBean;


    @Inject
    public ProductBean(final ProductDao productDao, final UserDao userDao, final CategoryDao categoryDao) {
        this.productDao = productDao;
        this.userDao = userDao;
        this.categoryDao = categoryDao;
    }

    public ProductBean() {
    }

    public boolean addProduct(String token, ProductDto p) {
        UserEntity userEntity = userDao.findByToken(token);

        if (userEntity != null) {
            ProductEntity productEntity = convertProductDtoToProductEntity(p);
            productEntity.setUserAutor(userEntity);
            productDao.persist(productEntity);
            return true;
        }
        return false;
    }

    public List<ProductDto> getAllProducts() {
        List<ProductEntity> products = productDao.findAll();
        return convertProductEntityListtoProductDtoList(products);
    }

    public ProductDto getProductById(int id) {
        ProductEntity p = productDao.findById(id);
        if (p != null) {
            return convertProductEntityToProductDto(p);
        }
        return null;
    }

    public List<ProductDto> getProductsByUser(UserEntity userEntity) {
        List<ProductEntity> products = productDao.findProductByUser(userEntity);
        return convertProductEntityListtoProductDtoList(products);
    }

    public boolean updateProduct(int id, ProductDto productDto) {
        ProductEntity productEntity = productDao.findById(id);
        if (productEntity != null) {
            productEntity.setTitle(productDto.getTitle());
            CategoryEntity category = categoryDao.findById(productDto.getCategoryId());
            productEntity.setCategory(category);
            productEntity.setPrice(productDto.getPrice());
            productEntity.setImagem(productDto.getImagem());
            productEntity.setLocal(productDto.getLocal());
            productEntity.setDescription(productDto.getDescription());
            productEntity.setState(productDto.getState());
            productDao.merge(productEntity);
            return true;
        }
        return false;
    }

    public boolean deleteProduct(int id) {
        ProductEntity productEntity = productDao.findById(id);
        if (productEntity != null) {
            productDao.remove(productEntity);
            return true;
        }
        return false;
    }

    public boolean softDeleteProduct(int id) {
        ProductEntity productEntity = productDao.findById(id);
        if (productEntity != null) {
            productEntity.setState(EstadosDoProduto.APAGADO);
            productDao.merge(productEntity);
            return true;
        }
        return false;
    }

    public boolean updateProductState(int id, EstadosDoProduto state) {
        ProductEntity productEntity = productDao.findById(id);
        if (productEntity != null) {
            // Permitir apenas a alteração para o estado "COMPRADO"
            if (state == EstadosDoProduto.COMPRADO) {
                productEntity.setState(state);
                productDao.merge(productEntity);
                return true;
            }
        }
        return false;
    }

    public List<ProductDto> getProductsByCategory(int categoryId) {
        List<ProductEntity> products = productDao.findProductByCategory(categoryId);
        return convertProductEntityListtoProductDtoList(products);
    }

    public List<ProductDto> getDeletedProducts() {
        List<ProductEntity> products = productDao.findDeletedProducts();
        return convertProductEntityListtoProductDtoList(products);
    }


    ProductEntity convertProductDtoToProductEntity(ProductDto a) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setTitle(a.getTitle());
        productEntity.setDescription(a.getDescription());
        productEntity.setPrice(a.getPrice());
        productEntity.setImagem(a.getImagem());
        productEntity.setLocal(a.getLocal());
        productEntity.setState(a.getState());
        productEntity.setCategory(categoryBean.convertCategoryDtoToCategoryEntity(a.getCategory()));

        return productEntity;
    }


    private List<ProductDto> convertProductEntityListtoProductDtoList(List<ProductEntity> productEntity) {
        List<ProductDto> productDtos = new ArrayList<>();
        for (ProductEntity a : productEntity) {
            productDtos.add(convertProductEntityToProductDto(a));
        }
        return productDtos;
    }

    ProductDto convertProductEntityToProductDto(ProductEntity productEntity) {
        ProductDto productDto = new ProductDto();
        productDto.setTitle(productEntity.getTitle());
        productDto.setDescription(productEntity.getDescription());
        productDto.setId(productEntity.getId());
        productDto.setUserAutor(productEntity.getUserAutor().getUsername());
        productDto.setPrice(productEntity.getPrice());
        productDto.setImagem(productEntity.getImagem());
        productDto.setLocal(productEntity.getLocal());
        productDto.setState(productEntity.getState());
        productDto.setCategory(categoryBean.convertCategoryEntityToCategoryDto(productEntity.getCategory()));
        productDto.setCategoryId(productEntity.getCategory().getId());

        // Resolve o userId usando o UserDao
        UserEntity userEntity = userDao.findUserByUsername(productEntity.getUserAutor().getUsername());
        productDto.setUserId(userEntity != null ? userEntity.getId() : 0);

        return productDto;
    }

    public List<ProductDto> getAvailableProducts() {
        // Obtém os produtos disponíveis do DAO
        List<ProductEntity> availableProducts = productDao.findProductsByState(EstadosDoProduto.DISPONIVEL);
        return convertProductEntityListtoProductDtoList(availableProducts);
    }

    public List<ProductDto> getPurchasedProducts() {
        // Obtém os produtos comprados do DAO
        List<ProductEntity> purchasedProducts = productDao.findPurchasedProducts();
        return convertProductEntityListtoProductDtoList(purchasedProducts);
    }

    public List<ProductDto> getProductsByState(EstadosDoProduto state) {
        List<ProductEntity> products = productDao.findProductsByState(state);
        return convertProductEntityListtoProductDtoList(products);
    }
}


