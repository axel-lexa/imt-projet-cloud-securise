// src/pages/PipelineDetail.jsx
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import JobBox from "../components/JobBox";

export default function PipelineDetail() {
    const { id } = useParams();
    const navigate = useNavigate();

    // On simule un état qui pourrait venir d'une API ou de ton mockPipelines
    const [currentPipeline, setCurrentPipeline] = useState(null);

    useEffect(() => {
        // Ici, on simule la récupération de la pipeline spécifique
        // Dans un vrai projet, tu ferais : fetch(`/api/pipelines/${id}`)
        const mockDetailedData = {
            id: id,
            name: `Pipeline ${id === "1" ? "Frontend" : "Backend"}`,
            // Tu peux changer ces status ici pour tester le slider !
            stages: [
                {
                    name: "Build",
                    jobs: [
                        { name: "Compile Source", status: "SUCCESS", duration: "1m 20s" },
                        { name: "Docker Build", status: "SUCCESS", duration: "45s" }
                    ]
                },
                {
                    name: "Test",
                    jobs: [
                        { name: "Unit Tests", status: "RUNNING", duration: "10s" },
                        { name: "Linting", status: "SUCCESS", duration: "5s" },
                        { name: "Security Scan", status: "PENDING" }
                    ]
                },
                {
                    name: "Deploy",
                    jobs: [
                        { name: "Push to Registry", status: "PENDING" },
                        { name: "Deploy to K8s", status: "PENDING" }
                    ]
                }
            ]
        };
        setCurrentPipeline(mockDetailedData);
    }, [id]);

    if (!currentPipeline) return <div className="p-10">Chargement...</div>;

    // --- LOGIQUE DE CALCUL AUTOMATIQUE DES ÉTAPES ---
    const stagesWithStatus = currentPipeline.stages.map(stage => {
        const allSuccess = stage.jobs.every(j => j.status === "SUCCESS");
        const hasRunning = stage.jobs.some(j => j.status === "RUNNING");
        const hasFailed = stage.jobs.some(j => j.status === "FAILED");

        let status = "PENDING";
        if (allSuccess) status = "SUCCESS";
        else if (hasFailed) status = "FAILED";
        else if (hasRunning) status = "RUNNING";

        return { ...stage, status };
    });

    // Calcul de la barre de progression
    const getProgressWidth = () => {
        const successCount = stagesWithStatus.filter(s => s.status === "SUCCESS").length;
        const runningIndex = stagesWithStatus.findIndex(s => s.status === "RUNNING");

        if (successCount === 3) return "100%";
        if (runningIndex !== -1) {
            // Si on est en train de build (index 0), on est à 25%
            // Si on est en train de test (index 1), on est à 50%
            // Si on est en train de deploy (index 2), on est à 75%
            return `${(runningIndex * 33) + 15}%`;
        }
        return `${successCount * 33}%`;
    };

    return (
        <div className="flex min-h-screen bg-gray-50/50">
            <Sidebar />
            <div className="flex-1">
                <Topbar title={`${currentPipeline.name} (#${id})`} />
                <main className="p-6">
                    <Button variant="ghost" onClick={() => navigate("/")} className="mb-8 gap-2">
                        <ArrowLeft className="w-4 h-4" /> Dashboard
                    </Button>

                    {/* SLIDER DYNAMIQUE */}
                    <div className="relative mb-12 px-10">
                        <div className="absolute top-5 left-10 right-10 h-0.5 bg-gray-200 -z-10" />
                        <div
                            className="absolute top-5 left-10 h-0.5 bg-primary transition-all duration-1000 ease-in-out -z-10"
                            style={{ width: `calc(${getProgressWidth()} - 40px)` }}
                        />

                        <div className="flex justify-between items-start">
                            {stagesWithStatus.map((stage, idx) => (
                                <div key={idx} className="flex flex-col items-center">
                                    <div className={`w-10 h-10 rounded-full border-4 flex items-center justify-center bg-white transition-all duration-500 ${
                                        stage.status === "SUCCESS" ? "border-primary bg-primary text-white" :
                                            stage.status === "RUNNING" ? "border-primary text-primary animate-pulse shadow-[0_0_15px_rgba(59,130,246,0.5)]" :
                                                stage.status === "FAILED" ? "border-red-500 text-red-500" :
                                                    "border-gray-200 text-gray-400"
                                    }`}>
                                        {stage.status === "SUCCESS" ? <Check className="w-5 h-5" /> : (idx + 1)}
                                    </div>
                                    <span className={`mt-2 text-xs font-bold uppercase ${stage.status !== "PENDING" ? "text-primary" : "text-gray-400"}`}>
                                        {stage.name}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* JOBS PAR COLONNES */}
                    <div className="flex flex-col md:flex-row gap-8 overflow-x-auto pb-4">
                        {stagesWithStatus.map((stage, idx) => (
                            <div key={idx} className="flex flex-col gap-4 min-w-[280px]">
                                <div className="space-y-3 p-4 bg-gray-100/50 rounded-xl border border-dashed border-gray-200">
                                    {stage.jobs.map((job, jIdx) => (
                                        <JobBox key={jIdx} job={job} />
                                    ))}
                                </div>
                            </div>
                        ))}
                    </div>
                </main>
            </div>
        </div>
    );
}