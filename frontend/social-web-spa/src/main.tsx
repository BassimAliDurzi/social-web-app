import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { bootstrapAuth } from "./auth/authStore";
import "./index.css";

bootstrapAuth();

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
