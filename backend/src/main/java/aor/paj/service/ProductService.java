package aor.paj.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import aor.paj.bean.ProductBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.ProductDto;
//import aor.paj.bean.ProductBean;
import aor.paj.entity.ProductEntity;
import aor.paj.entity.UserEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

//Gere os endpoints do produto
@Path("/products")
public class ProductService {

    @Inject
    ProductBean productBean;
    @Inject
    UserBean userBean;

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts(@HeaderParam("Authorization") String token) {
        UserEntity user= userBean.getUserByToken(token);
        if (user==null) {
            return Response.status(200).entity("Token inválido").build();
        } if(user.isAdmin()) {
            List<ProductDto> products =productBean.getAllProducts();
            return Response.ok(products).build();
        }
        else{
            List<ProductDto>products= productBean.getProductsByUser(user);
            return Response.ok(products).build();
        }
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductById(@PathParam("id") int id) {
        ProductDto productDto = productBean.getProductById(id);
        return productDto == null ? Response.status(200).entity("Produto não encontrado!").build()
                : Response.ok(productDto).build();
    }

    @GET
    @Path("/details")
    public Response getProductDetails(@PathParam("id") String productId) {
        return Response.seeOther(URI.create("/detalhes-produto.html?id=" + productId)).build();
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addProduct(@HeaderParam("Authorization") String token, ProductDto productDto) {
        if (userBean.tokenExist(token)) {
            boolean sucess = productBean.addProduct(token, productDto);
            if (sucess) {
                return Response.status(200).entity("Produto criado com sucesso!").build();
            } else {
                return Response.status(401).entity("Erro ao adicionar produto.").build();
            }
        }
        return Response.status(404).entity("Token inválido").build();
    }
    @DELETE
    @Path("/soft-delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response softDeleteProduct(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!userBean.tokenExist(token)) {
            return Response.status(404).entity("Token inválido").build();
        }
UserEntity user= userBean.getUserByToken(token);
        ProductDto product= productBean.getProductById(id);
       if( product == null || !product.getUserAutor().equals(user.getUsername()) ) {
           return Response.status(404).entity("Não tem permissões para apagar este produto").build();
       }
            boolean success=productBean.softDeleteProduct(id);
       if(success) {
           return Response.noContent().build();
       } else{
           return   Response.status(404).entity("Produto não encontrado").build();
       }

    }
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProduct(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if(!userBean.tokenExist(token)) {
            return Response.status(404).entity("Não tem permissões para essa ação").build();
        }
        UserEntity user = userBean.getUserByToken(token);
        ProductDto product = productBean.getProductById(id);

        if (product == null || !user.isAdmin()) {
            return Response.status(401).entity("Só é permitido a administradores").build();
        }

        boolean success = productBean.deleteProduct(id);
        if (success) {
            return Response.noContent().build();
        } else {
            return Response.status(404).entity("Produto não encontrado!").build();
        }
    }


    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProduct(@HeaderParam("Authorization") String token, @PathParam("id") int id, ProductDto productDto) {
        if (!userBean.tokenExist(token)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Token inválido").build();
        }

        UserEntity user = userBean.getUserByToken(token);
        ProductDto product = productBean.getProductById(id);

        if (product == null || (!user.isAdmin() && product.getUserAutor().equals(user.getUsername()))) {
            return Response.status(404).entity("Não tem permissões para alterar os dados.").build();
        }

        boolean success = productBean.updateProduct(id, productDto);
        if (success) {
            return Response.status(200).entity("Produto atualizado com sucesso!").build();
        } else {
            return Response.status(401).entity("Produto não encontrado!").build();
        }
    }
}


