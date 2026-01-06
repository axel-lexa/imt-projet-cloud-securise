import React, {useEffect, useState, useRef} from "react";
import {useParams, Link} from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {
    CheckCircle2, XCircle, Loader2, Clock,
    ArrowLeft, Terminal, AlertTriangle
} from "lucide-react";
import {getPipelineById} from "../api/cicdApi";

// Définition des étapes basées sur vos logs Backend
const STEPS_DEFINITION = [
    {id: 1, label: "Clonage du Dépôt (Git)", marker: "--- ÉTAPE 1"},
    {id: 2, label: "Build & Test (Maven)", marker: "--- ÉTAPE 2"},
    {id: 3, label: "Construction Image Docker", marker: "--- ÉTAPE 3"},
    {id: 4, label: "Transfert vers la VM", marker: "--- TRANSFERT"},
    {id: 5, label: "Déploiement (SSH)", marker: "--- ÉTAPE 4"}
];

export default function PipelineDetail() {
    const {id} = useParams();
    const [pipeline, setPipeline] = useState(null);
    const logsEndRef = useRef(null);

    // Fonction pour analyser les logs et déduire l'état de chaque étape
    const getStepsStatus = (logs, globalStatus) => {
        if (!logs) logs = "";

        let foundCurrent = false;

        return STEPS_DEFINITION.map((step, index) => {
            const isStarted = logs.includes(step.marker);
            const nextStep = STEPS_DEFINITION[index + 1];
            const isNextStarted = nextStep ? logs.includes(nextStep.marker) : false;
            const isCompleted = isStarted && (isNextStarted || (globalStatus === "SUCCESS" && index === STEPS_DEFINITION.length - 1));

            let status = "PENDING";
            if (isCompleted) {
                status = "SUCCESS";
            } else if (isStarted) {
                // Si l'étape a commencé mais que la suivante non...
                if (globalStatus === "FAILED") {
                    status = "FAILED"; // C'est ici que ça a planté
                } else if (globalStatus === "SUCCESS") {
                    status = "SUCCESS"; // Cas rare dernière étape
                } else {
                    status = "RUNNING"; // En cours
                    foundCurrent = true;
                }
            }

            return {...step, status};
        });
    };

    const fetchData = async () => {
        const data = await getPipelineById(id);
        if (data) setPipeline(data);
    };

    useEffect(() => {
        fetchData();
        const interval = setInterval(fetchData, 1000); // Mise à jour temps réel (1s)
        return () => clearInterval(interval);
    }, [id]);

    // Scroll automatique vers le bas des logs
    useEffect(() => {
        if (pipeline?.status === "RUNNING") {
            logsEndRef.current?.scrollIntoView({behavior: "smooth"});
        }
    }, [pipeline?.logs]);

    if (!pipeline) return (
        <div className="flex items-center justify-center h-screen bg-gray-50">
            <Loader2 className="w-10 h-10 animate-spin text-blue-600"/>
        </div>
    );

    const steps = getStepsStatus(pipeline.logs, pipeline.status);

    return (
        <div className="flex min-h-screen bg-gray-50/50">
            <Sidebar/>
            <div className="flex-1 flex flex-col">
                <Topbar title={`Pipeline #${pipeline.id}`}/>

                <main className="p-6 space-y-6 max-w-6xl mx-auto w-full">
                    {/* Header avec Bouton Retour et Statut Global */}
                    <div className="flex items-center justify-between">
                        <Link to="/">
                            <Button variant="ghost" className="gap-2">
                                <ArrowLeft className="w-4 h-4"/> Retour
                            </Button>
                        </Link>
                        <GlobalStatusBadge status={pipeline.status}/>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

                        {/* COLONNE GAUCHE : Étapes (Alignement Colonnes) */}
                        <Card className="lg:col-span-1 h-fit">
                            <CardHeader>
                                <CardTitle>Étapes du Processus</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-0">
                                {/* Entête du "Tableau" */}
                                <div className="grid grid-cols-12 text-xs font-semibold text-gray-400 mb-4 px-2">
                                    <div className="col-span-2 text-center">ÉTAT</div>
                                    <div className="col-span-10">NOM DE L'ÉTAPE</div>
                                </div>

                                {/* Liste des étapes */}
                                <div className="space-y-4">
                                    {steps.map((step, idx) => (
                                        <div key={step.id} className="relative group">
                                            {/* Ligne verticale de connexion */}
                                            {idx !== steps.length - 1 && (
                                                <div
                                                    className={`absolute left-[1.1rem] top-8 w-[2px] h-6 ${step.status === 'SUCCESS' ? 'bg-green-200' : 'bg-gray-100'}`}/>
                                            )}

                                            <div
                                                className="grid grid-cols-12 items-center gap-3 p-2 rounded-lg transition-colors hover:bg-gray-50">

                                                {/* Colonne 1 : Icône (Centrée) */}
                                                <div className="col-span-2 flex justify-center">
                                                    <StepIcon status={step.status}/>
                                                </div>

                                                {/* Colonne 2 : Label et Statut */}
                                                <div className="col-span-10 flex flex-col">
                                                    <span className={`text-sm font-medium ${
                                                        step.status === 'RUNNING' ? 'text-blue-700' :
                                                            step.status === 'FAILED' ? 'text-red-700' : 'text-gray-700'
                                                    }`}>
                                                        {step.label}
                                                    </span>
                                                    <span className="text-xs text-gray-400 font-mono">
                                                        {step.status}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>

                        {/* COLONNE DROITE : Logs */}
                        <Card className="lg:col-span-2 flex flex-col h-[600px]">
                            <CardHeader className="border-b bg-gray-50/50 py-3">
                                <div className="flex items-center gap-2">
                                    <Terminal className="w-4 h-4 text-gray-500"/>
                                    <CardTitle className="text-sm font-mono">Console Output</CardTitle>
                                </div>
                            </CardHeader>
                            <CardContent className="flex-1 p-0 overflow-hidden bg-[#1e1e1e]">
                                <div className="h-full overflow-y-auto p-4 font-mono text-xs text-green-400 space-y-1">
                                    {pipeline.logs ? (
                                        pipeline.logs.split('\n').map((line, i) => (
                                            <div key={i}
                                                 className={`${line.includes("ERREUR") || line.includes("ERROR") ? "text-red-400 font-bold" : ""} break-all`}>
                                                {line}
                                            </div>
                                        ))
                                    ) : (
                                        <span className="text-gray-500 italic">En attente de logs...</span>
                                    )}
                                    <div ref={logsEndRef}/>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </main>
            </div>
        </div>
    );
}

// --- SOUS-COMPOSANTS ---

function StepIcon({status}) {
    switch (status) {
        case "SUCCESS":
            return <CheckCircle2 className="w-6 h-6 text-green-500"/>;
        case "FAILED":
            return <XCircle className="w-6 h-6 text-red-500"/>;
        case "RUNNING":
            return <Loader2 className="w-6 h-6 text-blue-500 animate-spin"/>;
        default:
            return <div className="w-5 h-5 rounded-full border-2 border-gray-200"/>;
    }
}

function GlobalStatusBadge({status}) {
    const config = {
        SUCCESS: {color: "bg-green-100 text-green-800", icon: CheckCircle2, text: "Succès"},
        FAILED: {color: "bg-red-100 text-red-800", icon: XCircle, text: "Échec"},
        RUNNING: {color: "bg-blue-100 text-blue-800", icon: Loader2, text: "En cours...", spin: true},
        PENDING: {color: "bg-gray-100 text-gray-800", icon: Clock, text: "En attente"},
    };

    const current = config[status] || config.PENDING;
    const Icon = current.icon;

    return (
        <Badge className={`${current.color} px-3 py-1 flex items-center gap-2 text-sm border-0`}>
            <Icon className={`w-4 h-4 ${current.spin ? 'animate-spin' : ''}`}/>
            {current.text}
        </Badge>
    );
}