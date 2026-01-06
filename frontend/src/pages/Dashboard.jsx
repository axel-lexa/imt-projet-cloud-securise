import React, { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import PipelineCard from "../components/PipelineCard";
import { ActivityChart } from "../components/ActivityChart"; // Vérifie bien le chemin
import { Play, CheckCircle2, AlertCircle } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

const mockPipelines = [
    { id: 1, name: "Pipeline A", trigger: "Manual", status: "SUCCESS" },
    { id: 2, name: "Pipeline B", trigger: "GitHub Push", status: "RUNNING" },
    { id: 3, name: "Pipeline C", trigger: "Manual", status: "FAILED" },
];

export default function Dashboard() {
    const [pipelines, setPipelines] = useState(mockPipelines);

    useEffect(() => {
        const interval = setInterval(() => {
            setPipelines((prev) =>
                prev.map((p) =>
                    p.status === "RUNNING"
                        ? { ...p, status: Math.random() > 0.5 ? "SUCCESS" : "FAILED" }
                        : p
                )
            );
        }, 5000);
        return () => clearInterval(interval);
    }, []);

    // Calcul dynamique pour les StatCards
    const successCount = pipelines.filter(p => p.status === "SUCCESS").length;
    const failedCount = pipelines.filter(p => p.status === "FAILED").length;

    return (
        <div className="flex min-h-screen bg-gray-50/50">
            <Sidebar />
            <div className="flex-1">
                <Topbar title="Aperçu des Pipelines" />
                <main className="p-6 space-y-6">

                    {/* 1. Stats */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <StatCard title="Total" value={pipelines.length} icon={<Play className="text-blue-500" />} />
                        <StatCard title="Succès" value={successCount} icon={<CheckCircle2 className="text-green-500" />} />
                        <StatCard title="Échecs" value={failedCount} icon={<AlertCircle className="text-red-500" />} />
                    </div>

                    {/* 2. Le Graphique (C'était cette partie qui manquait !) */}
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                        <ActivityChart />
                        <Card className="p-6">
                            <h3 className="font-bold mb-2">Santé du Système</h3>
                            <p className="text-sm text-muted-foreground">Tous les services sont opérationnels.</p>
                            <div className="mt-4 h-2 w-full bg-green-100 rounded-full overflow-hidden">
                                <div className="h-full bg-green-500 w-[98%]" />
                            </div>
                        </Card>
                    </div>

                    {/* 3. Liste */}
                    <h2 className="text-xl font-semibold tracking-tight">Pipelines Récents</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                        {pipelines.map(p => <PipelineCard key={p.id} pipeline={p} />)}
                    </div>
                </main>
            </div>
        </div>
    );
}

function StatCard({ title, value, icon }) {
    return (
        <Card>
            <CardContent className="flex items-center justify-between p-6">
                <div>
                    <p className="text-sm font-medium text-muted-foreground">{title}</p>
                    <h3 className="text-2xl font-bold">{value}</h3>
                </div>
                {icon}
            </CardContent>
        </Card>
    );
}