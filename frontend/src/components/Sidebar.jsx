import React from "react";
import {Link, useLocation, useNavigate} from "react-router-dom";
import {LayoutDashboard, History, Users, Settings, LogOut} from "lucide-react";
import {Separator} from "@/components/ui/separator";

export default function Sidebar() {
    const location = useLocation();
    const navigate = useNavigate();

    const navItems = [
        {label: "Dashboard", icon: <LayoutDashboard className="w-4 h-4"/>, path: "/"},
        {label: "Historique", icon: <History className="w-4 h-4"/>, path: "/history"},
        {label: "Utilisateurs", icon: <Users className="w-4 h-4"/>, path: "/users"},
    ];

    const handleLogout = async () => {
        try {
            await fetch("http://localhost:8081/logout", {method: "POST"});
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

            <div className="p-4 mt-auto space-y-2">
                <Separator className="mb-4"/>

                <Link
                    to="/settings"
                    className="flex items-center gap-3 px-3 py-2 text-sm text-muted-foreground hover:text-foreground w-full transition-colors"
                >
                    <Settings className="w-4 h-4"/>
                    Paramètres
                </Link>

                <button
                    onClick={handleLogout}
                    className="flex items-center gap-3 px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50 hover:text-red-700 w-full transition-colors rounded-md text-left"
                >
                    <LogOut className="w-4 h-4"/>
                    Se déconnecter
                </button>
            </div>
        </aside>
    );
}