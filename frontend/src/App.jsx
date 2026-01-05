import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import Deployments from "./pages/Deployments";

export default function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/pipeline/:id" element={<Deployments />} />
            </Routes>
        </Router>
    );
}
