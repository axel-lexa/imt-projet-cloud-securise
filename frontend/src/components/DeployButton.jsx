import React, {useState} from "react";
import {Button} from "./ui/button.jsx";
import {Loader2, Rocket} from "lucide-react";
import {triggerPipeline} from "../api/cicdApi";

export default function DeployButton() {
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");

    // URL par défaut de votre projet
    const DEFAULT_REPO = "https://github.com/Deeffault/IMT-Architecture-Logiciel.git";

    const handleDeploy = async () => {
        setLoading(true);
        setMessage(""); // Reset du message
        try {
            // CORRECTION ICI : On passe l'URL à la fonction
            const res = await triggerPipeline(DEFAULT_REPO);

            setMessage("Pipeline lancé avec succès ! 🚀");

            // On efface le message après 3 secondes
            setTimeout(() => setMessage(""), 3000);
        } catch (error) {
            console.error(error);
            // On affiche un message d'erreur plus précis si possible
            setMessage("Erreur : Impossible de lancer le déploiement.");
        } finally {
            setLoading(false);
        }
    };

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