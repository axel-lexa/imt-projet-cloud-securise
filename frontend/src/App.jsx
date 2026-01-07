// src/App.jsx
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import Users from "./pages/Users";
import LoginPage from "./pages/LoginPage";
import PipelineDetail from "./pages/PipelineDetail";
import History from "./pages/History";

export default function App() {
    return (
        <Router>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/" element={<Dashboard />} />
                <Route path="/history" element={<History />} />
                <Route path="/users" element={<Users />} />
                <Route path="/pipeline/:id" element={<PipelineDetail />} />
            </Routes>
        </Router>
    );
}