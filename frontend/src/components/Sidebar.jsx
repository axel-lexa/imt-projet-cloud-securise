// src/components/Sidebar.jsx
import React, { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom"; // Import de Link et du hook de localisation
import { LayoutDashboard, History, Users, LogOut } from "lucide-react";
import { Separator } from "./ui/separator.jsx";
import { getCurrentUser, logout } from "../api/cicdApi.js";

export default function Sidebar() {
    const location = useLocation(); // Récupère le chemin actuel (ex: "/" ou "/users")
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchUser = async () => {
            const userData = await getCurrentUser();
            setUser(userData);
            setLoading(false);
        };
        fetchUser();
    }, []);

    const navItems = [
        { label: "Dashboard", icon: <LayoutDashboard className="w-4 h-4" />, path: "/" },
        { label: "Historique", icon: <History className="w-4 h-4" />, path: "/history" },
        { label: "Utilisateurs", icon: <Users className="w-4 h-4" />, path: "/users" },
    ];

    return (
        <aside className="w-64 border-r bg-card flex flex-col h-screen sticky top-0">
            <div className="p-6">
                <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
                    <img
                        src="/bzh-flag.svg"
                        alt="Gwenn ha Du"
                        className="w-6 h-6 rounded-sm shadow-sm"
                        loading="lazy"
                    />
                    CI/CD Cloud
                </h2>
            </div>

            <nav className="flex-1 px-4 space-y-1">
                {navItems.map((item) => {
                    // Vérifie si l'item est actif en comparant le chemin
                    const isActive = location.pathname === item.path;

                    return (
                        <Link
                            key={item.label}
                            to={item.path}
                            className={`flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-all ${
                                isActive
                                    ? "bg-primary text-primary-foreground shadow-sm"
                                    : "text-muted-foreground hover:bg-secondary hover:text-foreground"
                            }`}
                        >
                            {item.icon}
                            {item.label}
                        </Link>
                    );
                })}
            </nav>

            <div className="p-4 mt-auto">
                <Separator className="mb-4" />
                {loading ? (
                    <div className="flex items-center gap-3 px-3 py-2 text-sm text-muted-foreground">
                        Chargement...
                    </div>
                ) : user && user.authenticated ? (
                    <div className="flex items-center gap-3 px-3 py-2 text-sm">
                        {user.avatarUrl && (
                            <img
                                src={user.avatarUrl}
                                alt={user.name}
                                className="w-8 h-8 rounded-full border border-muted-foreground"
                                loading="lazy"
                            />
                        )}
                        <div className="flex-1 min-w-0">
                            <p className="font-medium text-foreground truncate">{user.name}</p>
                            <p className="text-xs text-muted-foreground truncate">@{user.login}</p>
                        </div>
                    </div>
                ) : (
                    <Link
                        to="/login"
                        className="flex items-center gap-3 px-3 py-2 text-sm text-muted-foreground hover:text-foreground w-full transition-colors"
                    >
                        Se connecter
                    </Link>
                )}
                {user && user.authenticated && (
                    <button
                        onClick={logout}
                        className="flex items-center gap-3 px-3 py-2 text-sm text-muted-foreground hover:text-foreground w-full transition-colors mt-3 hover:bg-secondary rounded-md"
                    >
                        <LogOut className="w-4 h-4" />
                        Déconnexion
                    </button>
                )}
            </div>
        </aside>
    );
}