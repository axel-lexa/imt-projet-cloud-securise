// src/components/DeployButton.jsx
import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Loader2, Rocket } from "lucide-react";
import { triggerPipeline } from "../api/cicdApi";

export default function DeployButton() {
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");

    const handleDeploy = async () => {
        setLoading(true);
        try {
            const res = await triggerPipeline();
            setMessage(res.message);
            // On efface le message après 3 secondes
            setTimeout(() => setMessage(""), 3000);
        } catch (error) {
            setMessage("Erreur lors du déploiement");
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
                className="font-bold shadow-sm"
            >
                {loading ? (
                    <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        DÉPLOIEMENT...
                    </>
                ) : (
                    <>
                        <Rocket className="mr-2 h-4 w-4" />
                        DÉPLOYER
                    </>
                )}
            </Button>

            {message && (
                <p className="text-xs font-medium text-green-600 animate-in fade-in slide-in-from-top-1">
                    {message}
                </p>
            )}
        </div>
    );
}