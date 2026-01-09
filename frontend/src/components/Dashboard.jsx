import React, { useState, useEffect } from "react";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";
import PipelineCard from "./PipelineCard";

// mock pipelines
const mockPipelines = [
    { id: 1, name: "Pipeline A", trigger: "Manual", status: "SUCCESS" },
    { id: 2, name: "Pipeline B", trigger: "GitHub Push", status: "RUNNING" },
    { id: 3, name: "Pipeline C", trigger: "Manual", status: "FAILED" },
];

export default function Dashboard() {
    const [pipelines, setPipelines] = useState(mockPipelines);

    // simulate progress
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

    return (
        <div className="flex min-h-screen bg-gray-50/50">
            <Sidebar />
            <div className="flex-1">
                <Topbar title="Aperçu des Pipelines" />
                <main className="p-6 space-y-6">
                    {/* Stats Cards */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        {/* Tes StatCards ici... */}
                    </div>

                    {/* Section Graphique */}
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                        <ActivityChart />
                        {/* On peut ajouter une autre petite carte à côté ici */}
                        <Card className="p-6">
                            <h3 className="font-bold mb-2">Santé du Système</h3>
                            <p className="text-sm text-muted-foreground">Tous les services sont opérationnels.</p>
                            <div className="mt-4 h-2 w-full bg-green-100 rounded-full overflow-hidden">
                                <div className="h-full bg-green-500 w-[98%]" />
                            </div>
                        </Card>
                    </div>

                    <h2 className="text-xl font-semibold tracking-tight">Pipelines Récents</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                        {pipelines.map(p => <PipelineCard key={p.id} pipeline={p} />)}
                    </div>
                </main>
            </div>
        </div>
    );
}
