// src/components/Sidebar.jsx
import React from "react";
import { Link, useLocation } from "react-router-dom"; // Import de Link et du hook de localisation
import { LayoutDashboard, History, Users, Settings } from "lucide-react";
import { Separator } from "@/components/ui/separator";

export default function Sidebar() {
    const location = useLocation(); // Récupère le chemin actuel (ex: "/" ou "/users")

    const navItems = [
        { label: "Dashboard", icon: <LayoutDashboard className="w-4 h-4" />, path: "/" },
        { label: "Historique", icon: <History className="w-4 h-4" />, path: "/history" },
        { label: "Utilisateurs", icon: <Users className="w-4 h-4" />, path: "/users" },
    ];

    return (
        <aside className="w-64 border-r bg-card flex flex-col h-screen sticky top-0">
            <div className="p-6">
                <h2 className="text-lg font-bold tracking-tight flex items-center gap-2">
                    <div className="w-6 h-6 bg-primary rounded-md shadow-sm" />
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
                <Link
                    to="/settings"
                    className="flex items-center gap-3 px-3 py-2 text-sm text-muted-foreground hover:text-foreground w-full transition-colors"
                >
                    <Settings className="w-4 h-4" />
                    Paramètres
                </Link>
            </div>
        </aside>
    );
}