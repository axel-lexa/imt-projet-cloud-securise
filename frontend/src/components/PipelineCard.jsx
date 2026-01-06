// src/components/PipelineCard.jsx
import React from "react";
import { useNavigate } from "react-router-dom"; // Import nécessaire pour le clic
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { PlayCircle, CheckCircle2, XCircle, Loader2 } from "lucide-react";

export default function PipelineCard({ pipeline }) {
    const navigate = useNavigate();

    // Configuration des styles selon le statut
    const statusConfig = {
        SUCCESS: { color: "bg-green-500/10 text-green-600 border-green-200", icon: <CheckCircle2 className="w-4 h-4" /> },
        RUNNING: { color: "bg-blue-500/10 text-blue-600 border-blue-200", icon: <Loader2 className="w-4 h-4 animate-spin" /> },
        FAILED: { color: "bg-red-500/10 text-red-600 border-red-200", icon: <XCircle className="w-4 h-4" /> },
        PENDING: { color: "bg-gray-500/10 text-gray-600 border-gray-200", icon: <PlayCircle className="w-4 h-4" /> },
    };

    const config = statusConfig[pipeline.status] || statusConfig.PENDING;

    return (
        <Card
            className="rounded-xl border bg-card text-card-foreground shadow hover:shadow-md transition-shadow cursor-pointer"
            onClick={() => navigate(`/pipeline/${pipeline.id}`)}
        >
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                <CardTitle className="text-sm font-semibold">{pipeline.name}</CardTitle>
                {config.icon}
            </CardHeader>
            <CardContent>
                <div className="text-xs text-muted-foreground mb-3 font-mono">
                    ID: {pipeline.id} • {pipeline.trigger}
                </div>

                {/* Affichage des mini-étapes (Stages) */}
                {pipeline.steps && (
                    <div className="flex gap-1 mb-4">
                        {pipeline.steps.map((step, i) => (
                            <div
                                key={i}
                                title={step.name}
                                className={`h-1.5 flex-1 rounded-full ${
                                    step.status === "SUCCESS" ? "bg-green-500" :
                                        step.status === "RUNNING" ? "bg-blue-500 animate-pulse" :
                                            step.status === "FAILED" ? "bg-red-500" : "bg-muted"
                                }`}
                            />
                        ))}
                    </div>
                )}

                <div className="flex items-center justify-between">
                    <Badge variant="outline" className={`${config.color} font-medium`}>
                        {pipeline.status}
                    </Badge>

                    {/* Petit texte indicatif pour l'UX */}
                    <span className="text-[10px] text-muted-foreground italic">Cliquez pour voir les jobs</span>
                </div>
            </CardContent>
        </Card>
    );
}