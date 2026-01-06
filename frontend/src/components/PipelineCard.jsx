import React from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {CheckCircle2, XCircle, Clock, Loader2, GitBranch} from "lucide-react";
import {Link} from "react-router-dom";

export default function PipelineCard({pipeline}) {
    // Calcul de la durée
    const getDuration = () => {
        if (!pipeline.startTime) return "-";

        const start = new Date(pipeline.startTime);
        // Si pas de endTime, on prend l'heure actuelle (pour afficher le temps écoulé en direct)
        const end = pipeline.endTime ? new Date(pipeline.endTime) : new Date();

        const diffMs = end - start;
        const seconds = Math.floor(diffMs / 1000);

        if (seconds < 60) return `${seconds}s`;
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}m ${remainingSeconds}s`;
    };

    // Configuration visuelle selon le statut
    const getStatusConfig = (status) => {
        switch (status) {
            case "SUCCESS":
                return {
                    icon: <CheckCircle2 className="w-5 h-5 text-green-500"/>,
                    bg: "bg-green-50 border-green-200",
                    text: "text-green-700",
                    label: "Succès"
                };
            case "FAILED":
                return {
                    icon: <XCircle className="w-5 h-5 text-red-500"/>,
                    bg: "bg-red-50 border-red-200",
                    text: "text-red-700",
                    label: "Échec"
                };
            case "RUNNING":
                return {
                    // Animation de rotation (spin) native de Lucide
                    icon: <Loader2 className="w-5 h-5 text-blue-500 animate-spin"/>,
                    bg: "bg-blue-50 border-blue-200",
                    text: "text-blue-700",
                    label: "En cours"
                };
            default: // PENDING
                return {
                    icon: <Clock className="w-5 h-5 text-gray-500"/>,
                    bg: "bg-gray-50 border-gray-200",
                    text: "text-gray-700",
                    label: "En attente"
                };
        }
    };

    const config = getStatusConfig(pipeline.status);

    return (
        <Link to={`/pipeline/${pipeline.id}`} className="block transition-transform hover:scale-[1.02]">
            <Card className={`border shadow-sm ${config.bg}`}>
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                    <CardTitle className="text-sm font-medium text-gray-600">
                        #{pipeline.id}
                    </CardTitle>
                    {config.icon}
                </CardHeader>
                <CardContent>
                    <div className="flex flex-col gap-1">
                        <div className="text-2xl font-bold flex items-center gap-2">
                            <span className={config.text}>{config.label}</span>
                        </div>

                        <div className="flex items-center text-xs text-muted-foreground mt-2 gap-4">
                            <div className="flex items-center gap-1">
                                <GitBranch className="w-3 h-3"/>
                                {pipeline.branch || "main"}
                            </div>
                            <div className="flex items-center gap-1">
                                <Clock className="w-3 h-3"/>
                                {getDuration()}
                            </div>
                        </div>

                        <p className="text-xs text-gray-400 mt-1">
                            {new Date(pipeline.startTime).toLocaleString()}
                        </p>
                    </div>
                </CardContent>
            </Card>
        </Link>
    );
}