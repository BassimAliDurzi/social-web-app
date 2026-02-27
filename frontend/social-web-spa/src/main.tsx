import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { bootstrapAuth, logout } from "./auth/authStore";
import "./index.css";

bootstrapAuth();

// Global logout event hook (used by API layer on 401)
window.addEventListener("auth:logout", () => {
  logout();
});

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);