import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css"; // <- assure-toi que c'est bien là

ReactDOM.createRoot(document.getElementById("root")).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);
