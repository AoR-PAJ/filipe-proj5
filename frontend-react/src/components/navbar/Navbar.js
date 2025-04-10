import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import "./Navbar.css";
import StoreLogo from "../../images/icon.png";
import { userStore } from "../../stores/UserStore";
import { IoAddCircleOutline } from "react-icons/io5";
import { FaUser, FaCogs, FaSignOutAlt, FaSignInAlt } from "react-icons/fa"; // Importa os ícones FontAwesome
import ProductModal from "../product/ProductModal";
import api from "../../services/apiService";
import { toast } from "react-toastify";

const Navbar = () => {
  const username = userStore((state) => state.username);
  const imagem = userStore((state) => state.imagem);
  const isAdmin = userStore((state) => state.isAdmin);
  const clearUser = userStore((state) => state.clearUser);

  const [isModalOpen, setIsModalOpen] = useState(false); // Estado para controlar o modal

  // Exibe uma toast de boas-vindas após o login
  useEffect(() => {
    if (username) {
      toast.success(`Login efetuado com sucesso! Bem-vindo, ${username}!`, {
        position: "top-right",
        autoClose: 3000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
      });
    }
  }, [username]);

  const handleLogout = async () => {
    try {
      await api.post("/users/logout");
      clearUser();
      toast.success("Logout realizado com sucesso!");
    } catch (error) {
      console.error("Erro no logout:", error.response?.data || error.message);
      toast.error("Erro ao realizar logout. Tente novamente.");
    }
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container-fluid">
        <Link className="navbar-logo" to="/">
          <img
            src={StoreLogo}
            alt="Store Logo"
            className="d-inline-block align-text-top"
          />
        </Link>
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav ms-auto">
            {username ? (
              <>
                <li className="nav-item">
                  <IoAddCircleOutline
                    className="add-product-icon"
                    onClick={() => setIsModalOpen(true)}
                  />
                </li>
                <li className="nav-item dropdown">
                  <img
                    src={imagem}
                    alt="User"
                    className="nav-user-image dropdown-toggle"
                    id="navbarDropdown"
                    role="button"
                    data-bs-toggle="dropdown"
                    aria-expanded="false"
                  />
                  <ul
                    className="dropdown-menu dropdown-menu-end"
                    aria-labelledby="navbarDropdown"
                  >
                    <li>
                      <span className="dropdown-item-text">
                        Bem-vindo, {username}!
                      </span>
                    </li>
                    <li>
                      <Link to="/profile" className="dropdown-item">
                        O meu perfil
                      </Link>
                    </li>
                    {isAdmin && (
                      <li>
                        <Link to="/admin" className="dropdown-item">
                          Administrar
                        </Link>
                      </li>
                    )}
                    <li>
                      <button className="dropdown-item" onClick={handleLogout}>
                        Logout
                      </button>
                    </li>
                  </ul>
                </li>
              </>
            ) : (
              <li className="nav-item">
                <Link to="/login" className="nav-link">
                  <FaSignInAlt className="nav-icon" />
                </Link>
              </li>
            )}
          </ul>
        </div>
      </div>

      {/* Modal de criação de produto */}
      <ProductModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      />
    </nav>
  );
};

export default Navbar;
