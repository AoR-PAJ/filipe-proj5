package aor.paj.dao;

import aor.paj.entity.CategoryEntity;
import aor.paj.dto.CategoryDto;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.List;

@Stateless
public class CategoryDao extends AbstratDao<CategoryEntity> {
    private static final long serialVersionUID = 1L;

    public CategoryDao() {
        super(CategoryEntity.class);
    }

    public CategoryEntity createCategory(String name) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        em.persist(category);
        return category;
    }

    // Adiciona o mét0do para buscar uma categoria pelo ID
    public CategoryEntity findById(int categoryId) {
        return em.find(CategoryEntity.class, categoryId);
    }

    public List<CategoryEntity> getAllCategories() {
        try {
            return (List<CategoryEntity>) em.createNamedQuery("Category.getAllCategories").getResultList();

        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(CategoryEntity category) {
        em.merge(category);
    }

    public void deleteCategory(int categoryId) {
        CategoryEntity category = findById(categoryId);
        if (category != null) {
            em.remove(category);
        }
    }

    public List<CategoryDto> getCategoriesByProductCount() {
        return em.createQuery(
            "SELECT new aor.paj.dto.CategoryDto(c.name, COUNT(p)) " +
            "FROM CategoryEntity c LEFT JOIN c.products p " +
            "GROUP BY c.name ORDER BY COUNT(p) DESC",
            CategoryDto.class
        ).getResultList();
    }
}
