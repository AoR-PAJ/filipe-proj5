package aor.paj.dao;

import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;

import java.util.List;

@Stateless
public class UserDao extends AbstratDao<UserEntity> {
    private static final long serialVersionUID = 1L;


    public UserDao() {
        super(UserEntity.class);
    }


    public UserEntity findByToken(String token) {
        try {
            return (UserEntity) em.createNamedQuery("Utilizador.findUserByToken").setParameter("token", token)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }


    public UserEntity findUserByUsername(String username) {
        try {
            return (UserEntity) em.createNamedQuery("Utilizador.findUserByUsername").setParameter("username", username)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserEntity> findDeletedUsers() {
        try {
            return em.createNamedQuery("Utilizador.getDeletedUsers", UserEntity.class).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserEntity> findAll() {
        try {
            return em.createNamedQuery("Utilizador.getAllUsers", UserEntity.class).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}
