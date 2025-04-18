package aor.paj.bean;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import aor.paj.dao.ProductDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.UserDto;
import aor.paj.entity.ProductEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

@Stateless
public class UserBean implements Serializable {

    @EJB //injeção de dependência- neste caso significa que a variável abaixo vai ser injetada automaticamente no container
    UserDao userDao;

    @EJB
    ProductDao productDao;

    private static final Logger logger = LogManager.getLogger(UserBean.class);

    public String loginUser(UserDto user) {
        logger.info("Login attempt for user: {}", user.getUsername());
        UserEntity userEntity = userDao.findUserByUsername(user.getUsername());
        if (userEntity != null) {
            // Verifica se a password inserida corresponde ao hash armazenado
            if (userEntity.checkPassword(user.getPassword())) {
                String token = generateNewToken();
                userEntity.setToken(token);
                userDao.merge(userEntity);
                logger.info("Login successful for user: {}", user.getUsername());
                return token;
            } else {
                logger.warn("Invalid password for user: {}", user.getUsername());
            }
        } else {
            logger.warn("User not found: {}", user.getUsername());

        }
        return null;
    }

public boolean registerUser(UserDto user) {
    UserEntity u_temp = userDao.findUserByUsername(user.getUsername());
    if (u_temp == null) {
        UserEntity userEntity = convertUserDtotoUserEntity(user);
        userEntity.setIsVerified(false); // Conta não verificada
        userEntity.setVerificationToken(generateNewToken()); // Gera o token de verificação
        try {
            userDao.persist(userEntity);
            // Exibe o link de verificação na consola
            String verificationLink = "http://localhost:8080/filipe-proj5/rest/users/verify?token=" + userEntity.getVerificationToken();
            System.out.println("Link de verificação: " + verificationLink);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
    } else {
        throw new RuntimeException("Já existe um utilizador com esse username");
    }
}

    public List<UserDto> getAllUsers() {
        List<UserEntity> users = userDao.findAll();
        return convertUserEntityListToUserDtoList(users);
    }


    UserEntity convertUserDtotoUserEntity(UserDto user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(hashPassword(user.getPassword()));
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setEmail(user.getEmail());
        userEntity.setPhone(user.getPhone());
        userEntity.setImagem(user.getImagem());
        userEntity.setIsAdmin(user.getIsAdmin());
        userEntity.setDeleted(user.getIsDeleted());
        userEntity.setIsVerified(user.isVerified()); // Adicionado
        userEntity.setVerificationToken(user.getVerificationToken()); // Adicionado
        userEntity.setPasswordRecoveryToken(user.getPasswordRecoveryToken()); // Adicionado
        return userEntity;
    }

    public UserDto convertUserEntityToUserDto(UserEntity user) {
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setPhone(user.getPhone());
        userDto.setImagem(user.getImagem());
        userDto.setAdmin(user.isAdmin());
        userDto.setId(user.getId());
        userDto.setDeleted(user.isDeleted());
        userDto.setIsVerified(user.isVerified()); // Adicionado
        userDto.setVerificationToken(user.getVerificationToken()); // Adicionado
        userDto.setPasswordRecoveryToken(user.getPasswordRecoveryToken()); // Adicionado
        return userDto;
    }

    public String generateNewToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public boolean tokenExist(String token) {
        if (userDao.findByToken(token) != null)
            return true;
        return false;

    }

    public boolean updateUser(UserDto userDto) {
        UserEntity userEntity = userDao.findUserByUsername(userDto.getUsername());
        if (userEntity != null) {
            userEntity.setPassword(userDto.getPassword());
            userEntity.setFirstName(userDto.getFirstName());
            userEntity.setLastName(userDto.getLastName());
            userEntity.setEmail(userDto.getEmail());
            userEntity.setPhone(userDto.getPhone());
            userEntity.setImagem(userDto.getImagem());
            userEntity.setIsAdmin(userDto.getIsAdmin());
            userEntity.setDeleted(userDto.getIsDeleted());
            userDao.merge(userEntity);
            return true;
        }
        return false;
    }

    public UserEntity getUserByToken(String token) {
        return userDao.findByToken(token);
    }

    public boolean logoutUser(String token) {
        UserEntity userEntity = userDao.findByToken(token);
        if (userEntity != null) {
            userEntity.setToken(null);
            userDao.merge(userEntity);
            return true;
        }
        return false;
    }

    public boolean deleteUser(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            // Apagar produtos do utilizador
            List<ProductEntity> products = userEntity.getProducts();
            for (ProductEntity product : products) {
                productDao.remove(product);
            }
            // Apagar utilizador
            userDao.remove(userEntity);
            return true;
        }
        return false;
    }

    public boolean softDeleteUser(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        if (userEntity != null) {
            userEntity.setDeleted(true); // Supondo que você tem um campo isDeleted para soft delete
            userDao.merge(userEntity);
            return true;
        }
        return false;
    }

    public UserEntity getUserByUsername(String username) {
        return userDao.findUserByUsername(username);
    }

    public List<UserDto> getDeletedUsers() {
        List<UserEntity> users = userDao.findDeletedUsers();
        return convertUserEntityListToUserDtoList(users);
    }

    private List<UserDto> convertUserEntityListToUserDtoList(List<UserEntity> userEntities) {
        List<UserDto> userDtos = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            userDtos.add(convertUserEntityToUserDto(userEntity));
        }
        return userDtos;
    }

    public String hashPassword(String password) {
//        logger.info("Hashing password.");
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }


}
