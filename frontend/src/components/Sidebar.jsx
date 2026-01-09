// src/components/Sidebar.jsx
import React, { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { LayoutDashboard, History, Users, LogOut, Github, Mail } from "lucide-react";
import { Separator } from "./ui/separator.jsx";
import { Popover, PopoverContent, PopoverTrigger } from "./ui/popover.jsx";
import { getCurrentUser, logout } from "../api/cicdApi.js";

export default function Sidebar() {
    const location = useLocation();
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
        {label: "Dashboard", icon: <LayoutDashboard className="w-4 h-4"/>, path: "/"},
        {label: "Historique", icon: <History className="w-4 h-4"/>, path: "/history"},
        {label: "Utilisateurs", icon: <Users className="w-4 h-4"/>, path: "/users"},
    ];

    const handleLogout = async () => {
        try {
            await fetch("http://localhost:8081/logout", {
                method: "POST",
                credentials: "include"
            });
            navigate("/login", {replace: true});
        } catch (error) {
            console.error("Erreur lors de la déconnexion:", error);
        }
    };

    return (
        <aside className="w-64 border-r bg-card flex flex-col h-screen sticky top-0">
            <div className="p-6">
                <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
                    <img
                        src="/bzh-mount.svg"
                        alt="Gwenn ha Du"
                        className="w-6 h-6 rounded-sm shadow-sm"
                        loading="lazy"
                    />
                    CI/CD Cloud
                </h2>
            </div>

            <nav className="flex-1 px-4 space-y-1">
                {navItems.map((item) => {
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
                    <Popover>
                        <PopoverTrigger asChild>
                            <button className="flex items-center gap-3 px-3 py-2 text-sm w-full hover:bg-secondary rounded-md transition-colors leading-none">
                                {user.avatarUrl && (
                                    <img
                                        src={user.avatarUrl}
                                        alt={user.name}
                                        className="w-8 h-8 rounded-full border border-muted-foreground cursor-pointer shrink-0 block aspect-square object-cover transform-gpu select-none"
                                        loading="lazy"
                                    />
                                )}
                                <div className="flex-1 min-w-0 text-left">
                                    <p className="font-medium text-foreground truncate">{user.name}</p>
                                    <p className="text-xs text-muted-foreground truncate">@{user.login}</p>
                                </div>
                            </button>
                        </PopoverTrigger>
                        <PopoverContent side="top" align="start" className="w-80">
                            <div className="space-y-4">
                                <div className="flex items-center gap-4">
                                    {user.avatarUrl && (
                                        <img
                                            src={user.avatarUrl}
                                            alt={user.name}
                                            className="w-16 h-16 rounded-full border-2 border-primary"
                                            loading="lazy"
                                        />
                                    )}
                                    <div className="flex-1 min-w-0">
                                        <h3 className="font-semibold text-lg truncate">{user.name}</h3>
                                        <p className="text-sm text-muted-foreground truncate">@{user.login}</p>
                                    </div>
                                </div>
                                <Separator />
                                <div className="space-y-2 text-sm">
                                    <div className="flex items-center gap-2">
                                        <Github className="w-4 h-4 text-muted-foreground" />
                                        <a 
                                            href={`https://github.com/${user.login}`}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="text-primary hover:underline truncate"
                                        >
                                            github.com/{user.login}
                                        </a>
                                    </div>
                                    {user.email && (
                                        <div className="flex items-center gap-2">
                                            <Mail className="w-4 h-4 text-muted-foreground" />
                                            <span className="truncate">{user.email}</span>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </PopoverContent>
                    </Popover>
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
                        className="flex items-center gap-3 px-3 py-2 text-sm text-muted-foreground hover:text-foreground w-full transition-colors mt-3 hover:bg-secondary rounded-md leading-none"
                    >
                        <span className="w-4 h-4 shrink-0 grid place-items-center transform-gpu select-none">
                            <LogOut className="w-4 h-4" />
                        </span>
                        Déconnexion
                    </button>
                )}
            </div>
        </aside>
    );
}