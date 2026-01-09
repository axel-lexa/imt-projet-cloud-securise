// src/App.jsx
import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import Users from "./pages/Users";
import LoginPage from "./pages/LoginPage";
import PipelineDetail from "./pages/PipelineDetail";
import History from "./pages/History";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
    return (
        <Router>
            <Routes>
                {/* Route publique : Login */}
                <Route path="/login" element={<LoginPage/>}/>

                {/* Routes Protégées : Tout ce qui est ici nécessite d'être loggé */}
                <Route element={<ProtectedRoute/>}>
                    <Route path="/" element={<Navigate to="/dashboard" replace/>}/>
                    <Route path="/dashboard" element={<Dashboard/>}/>
                    <Route path="/pipeline/:id" element={<PipelineDetail/>}/>
                    <Route path="/users" element={<Users/>}/>
                    <Route path="/history" element={<History/>}/>
                    {/* Autres routes protégées... */}
                </Route>
            </Routes>
        </Router>
    );
}

export default App;