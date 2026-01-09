import React, {useState, useEffect} from "react";
import {Button} from "./ui/button.jsx";
import {Loader2, Rocket, Lock} from "lucide-react";
import {triggerPipeline, getCurrentUser} from "../api/cicdApi";

export default function DeployButton() {
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");
    const [userRole, setUserRole] = useState(null);

    // URL par défaut de votre projet
    const DEFAULT_REPO = "https://github.com/Deeffault/IMT-Architecture-Logiciel.git";

    useEffect(() => {
        const fetchUser = async () => {
            const user = await getCurrentUser();
            if (user) {
                setUserRole(user.role);
            }
        };
        fetchUser();
    }, []);

    const handleDeploy = async () => {
        setLoading(true);
        setMessage(""); // Reset du message
        try {
            const res = await triggerPipeline(DEFAULT_REPO);
            setMessage("Pipeline lancé avec succès ! 🚀");
            setTimeout(() => setMessage(""), 3000);
        } catch (error) {
            console.error(error);
            setMessage("Erreur : Impossible de lancer le déploiement.");
        } finally {
            setLoading(false);
        }
    };

    // Si le rôle n'est pas encore chargé, on affiche un bouton désactivé ou un loader
    if (!userRole) {
        return <Button disabled size="lg" variant="outline"><Loader2 className="w-4 h-4 animate-spin"/></Button>;
    }

    // Vérification des droits : Seuls ADMIN et DEVOPS peuvent déployer
    const canDeploy = userRole === "ADMIN" || userRole === "DEVOPS";

    if (!canDeploy) {
        return (
            <div className="flex flex-col items-end gap-2">
                <Button disabled size="lg" className="bg-gray-400 cursor-not-allowed opacity-70">
                    <Lock className="mr-2 h-4 w-4"/>
                    DÉPLOIEMENT (Restreint)
                </Button>
                <p className="text-xs text-gray-500">Réservé aux Admins & DevOps</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col items-end gap-2">
            <Button
                onClick={handleDeploy}
                disabled={loading}
                size="lg"
                className="font-bold shadow-sm bg-blue-600 hover:bg-blue-700 text-white"
            >
                {loading ? (
                    <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin"/>
                        DÉPLOIEMENT...
                    </>
                ) : (
                    <>
                        <Rocket className="mr-2 h-4 w-4"/>
                        DÉPLOYER
                    </>
                )}
            </Button>

            {message && (
                <p className={`text-xs font-medium animate-in fade-in slide-in-from-top-1 ${message.includes("Erreur") ? "text-red-600" : "text-green-600"}`}>
                    {message}
                </p>
            )}
        </div>
    );
}
