import React, { useEffect, useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import webSocketService from "../services/websocketService";
import api from "../services/apiService"; // Serviço para chamadas à API
import "./Chat.css";

//TODO tentar fazer com que um link para um produto crie um card para esse produto dentro do chat

const Chat = ({ loggedInUser }) => {
  const { username } = useParams(); // Obtém o destinatário da URL
  const navigate = useNavigate(); // Hook para navegação
  const [message, setMessage] = useState(""); // Mensagem atual
  const [messages, setMessages] = useState([]); // Histórico de mensagens
  const [recipient, setRecipient] = useState(username || ""); // Destinatário inicial
  const [users, setUsers] = useState([]); // Lista de usuários
  const [loading, setLoading] = useState(true); // Estado de carregamento
  const messagesEndRef = useRef(null); // Referência para o final da lista de mensagens

  // Função para buscar a lista de usuários do backend
  const fetchUsers = async () => {
    try {
      console.log("Buscando lista de usuários...");
      const response = await api.get("/users/all"); // Faz a chamada ao endpoint
      const filteredUsers = response.data.filter(
        (user) => user.username !== loggedInUser // Filtra o usuário logado
      );
      setUsers(filteredUsers); // Armazena os usuários filtrados no estado
      console.log("Usuários carregados:", filteredUsers);
    } catch (err) {
      console.error("Erro ao carregar a lista de usuários:", err);
    } finally {
      setLoading(false); // Indica que o carregamento terminou
    }
  };

  // Função para buscar o histórico de mensagens do backend
  const fetchMessages = async (sender, recipient) => {
    try {
      console.log(
        `A obter histórico de mensagens entre ${sender} e ${recipient}...`
      );
      const response = await api.get(`/messages/${sender}/${recipient}`);
      const formattedMessages = response.data.map((message) => ({
        ...message,
        timestamp: new Date(message.timestamp).toLocaleString("pt-PT"), // Formata o timestamp
      }));
      setMessages(formattedMessages); // Atualiza o estado com o histórico de mensagens formatado
      console.log("Histórico de mensagens carregado:", formattedMessages);
    } catch (err) {
      console.error("Erro ao carregar o histórico de mensagens:", err);
    }
  };

  // Carrega a lista de usuários ao montar o componente
  useEffect(() => {
    fetchUsers();
  }, []);

  // Atualiza o destinatário ao mudar o URL
  useEffect(() => {
    setRecipient(username || "");
    if (username) {
      fetchMessages(loggedInUser, username); // Carrega o histórico de mensagens
    }
  }, [username, loggedInUser]);

  // Conecta ao WebSocket
  useEffect(() => {
    if (recipient) {
      console.log("Conectando ao WebSocket com usuário logado:", loggedInUser);
      webSocketService.connect(loggedInUser); // Conecta ao WebSocket como o usuário logado

      webSocketService.onMessage((data) => {
        console.log("Nova mensagem recebida via WebSocket:", data);

        const dataJSON = JSON.parse(data); // Converte a string JSON em objeto
        const { sender, content } = dataJSON; // Desestrutura o objeto

        setMessages((prevMessages) => [
          ...prevMessages,
          {
            sender: sender || "Desconhecido",
            content,
            timestamp: new Date().toLocaleString("pt-PT"),
          },
        ]);
      });

      return () => {
        console.log("Desconectando do WebSocket...");
        webSocketService.disconnect();
      };
    }
  }, [loggedInUser, recipient]);

  useEffect(() => {
    webSocketService.onMessage((message) => {
      if (message.sender === recipient || message.recipient === recipient) {
        setMessages((prevMessages) => [...prevMessages, message]);
      } else {
        console.warn(
          "Mensagem recebida, mas não pertence ao chat atual:",
          message
        );
      }
    });
  }, [recipient]);

  useEffect(() => {
    webSocketService.setRecipient(recipient);
  }, [recipient]);

  // Faz scroll para o final da lista de mensagens
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  // Marca mensagens como lidas
  useEffect(() => {
    if (messages.length > 0) {
      const unreadMessages = messages.filter((msg) => !msg.isRead);
      if (unreadMessages.length > 0) {
        api.put(`/messages/mark-all-as-read/${recipient}`);
      }
    }
  }, [messages, recipient]);

  useEffect(() => {
    const markMessagesAsRead = async () => {
      try {
        await api.put(`/messages/mark-all-as-read/${recipient}`);
        console.log(
          "Mensagens marcadas como lidas para o destinatário:",
          recipient
        );
      } catch (err) {
        console.error("Erro ao marcar mensagens como lidas:", err);
      }
    };

    if (recipient) {
      markMessagesAsRead();
    }
  }, [recipient]);

  // Atualiza o destinatário e carrega o histórico ao selecionar um usuário
  const handleUserSelection = (user) => {
    console.log("Destinatário selecionado:", user.username);
    setRecipient(user.username); // Define o destinatário
    navigate(`/chat/${user.username}`); // Atualiza o URL
    fetchMessages(loggedInUser, user.username); // Carrega o histórico de mensagens
  };

  // Envia uma mensagem e salva no backend
  const handleSendMessage = async () => {
    if (message.trim() && recipient.trim()) {
      const newMessage = {
        sender: loggedInUser,
        recipient,
        content: message,
      };

      try {
        console.log("Enviando mensagem:", newMessage);

        // Envia a mensagem para o backend via POST
        await api.post("/messages", newMessage); // Salva a mensagem no backend

        // Envia a mensagem via WebSocket
        webSocketService.sendMessage(JSON.stringify(newMessage)); // Envia a mensagem como JSON pelo WebSocket

        // Atualiza o estado local com a mensagem enviada
        setMessages((prevMessages) => [
          ...prevMessages,
          {
            ...newMessage,
            timestamp: new Date().toLocaleString("pt-PT"),
          },
        ]);

        setMessage(""); // Limpa o campo de entrada
      } catch (err) {
        console.error("Erro ao enviar mensagem:", err);
      }
    } else {
      console.warn("Mensagem ou destinatário vazio. Mensagem não enviada.");
    }
  };

  // Envia a mensagem ao pressionar Enter
  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      handleSendMessage();
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-users">
        <h3>Usuários</h3>
        {loading ? (
          <p>Carregando...</p>
        ) : (
          <ul>
            {users.map((user) => (
              <li
                key={user.username}
                onClick={() => handleUserSelection(user)} // Atualiza o URL e o destinatário
              >
                <img
                  src={user.imagem || "https://via.placeholder.com/50"}
                  alt={user.username}
                  className="user-avatar"
                />
                <span>{user.username}</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      {recipient ? (
        <div className="chat-window">
          <h3>Chat com {recipient}</h3>
          <div className="chat-messages">
            {messages.map((msg, index) => (
              <div
                key={index}
                className={`chat-message ${
                  msg.sender === loggedInUser ? "sent" : "received"
                }`}
              >
                <strong>
                  {msg.sender === loggedInUser ? "Você" : msg.sender}:
                </strong>{" "}
                {msg.content}
                <div className="timestamp">{msg.timestamp}</div>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>
          <div className="chat-input">
            <input
              type="text"
              placeholder="Digite sua mensagem..."
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              onKeyDown={handleKeyDown}
            />
            <button onClick={handleSendMessage}>Enviar</button>
          </div>
        </div>
      ) : (
        <div className="chat-placeholder">
          <h3>Selecione um usuário para iniciar o chat</h3>
        </div>
      )}
    </div>
  );
};

export default Chat;
