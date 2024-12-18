// src/features/ChatBot/ChatMessages.js
import React, { useRef, useEffect } from 'react';
import MessageBubble from './MessageBubble';
import './ChatBotStylesConfig.css';

const ChatMessages = ({ messages }) => {
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div className="chat-messages-container">
      {(messages || []).map((message, index) => (
        <MessageBubble
          key={index}
          sender={message.sender}
          text={message.text}
          imageUrl={message.imageUrl}
        />
      ))}
      <div ref={messagesEndRef} />
    </div>
  );  
};

export default ChatMessages;
