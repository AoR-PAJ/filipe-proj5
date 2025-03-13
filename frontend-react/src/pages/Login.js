import React, { useState } from "react";
import "../pages/Login.css";
import { useNavigate } from "react-router-dom";
import { userStore } from "../stores/UserStore";

function Login() {
  const navigate = useNavigate();
  const [inputs, setInputs] = useState({});
  const [error, setError] = useState(null);
  const updateName = userStore((state) => state.updateName);
  const updateToken = userStore((state) => state.updateToken);

  const handleChange = (event) => {
    const name = event.target.name;
    const value = event.target.value;
    setInputs((values) => ({ ...values, [name]: value }));
  };

  // Função para fazer o pedido REST de login
  const loginUser = async (username, password) => {
    try {
      const response = await fetch(
        "http://localhost:8080/filipe-proj4/rest/users/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ username, password }),
        }
      );

      if (!response.ok) {
        throw new Error("Login failed");
      }

      // Trata a resposta como texto
      const token = await response.text();
      return token;
    } catch (error) {
      throw error;
    }
  };

  // Função para lidar com o envio do formulário
  const handleSubmit = async (event) => {
    event.preventDefault(); // Previne o comportamento padrão do formulário
    try {
      console.log("Enviando dados:", inputs);
      const token = await loginUser(inputs.username, inputs.password); // Chama a função de login
      updateName(inputs.username); // Atualiza o estado do username na store
      updateToken(token); // Atualiza o estado do token na store
      console.log("Token recebido:", token);
      alert(
        `Login bem-sucedido!\nEnviado: ${JSON.stringify(
          inputs
        )}\nToken recebido: ${token}`
      ); // Alerta para o utilizador
      navigate("/home", { replace: true }); // Navega para a página inicial
    } catch (error) {
      console.error("Erro no login:", error);
      setError("Login failed. Please check your username and password.");
      alert(
        `Login falhado!\nEnviado: ${JSON.stringify(inputs)}\nErro: ${
          error.message
        }`
      ); // Alerta para o utilizador
    }
  };


  return (
    <div className="login-container">
      <div className="Login">
        <h1>Login</h1>
        {error && <p className="error">{error}</p>}{" "}
        {/* Exibe a mensagem de erro, se houver */}
        <form onSubmit={handleSubmit}>
          <label>
            Enter your username:
            <input
              type="text"
              name="username"
              defaultValue={inputs.username || ""}
              onChange={handleChange}
            />
          </label>
          <label>
            Enter your password:
            <input
              type="password"
              name="password"
              defaultValue={inputs.password || ""}
              onChange={handleChange}
            />
          </label>
          <input type="submit" value="Login" />
        </form>
        <p>
          Don't have an account? <a href="/register">Register here</a>
        </p>
      </div>
    </div>
  );
}

export default Login;
