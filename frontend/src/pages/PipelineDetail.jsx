import React, {useEffect, useState, useRef} from "react";
import {useParams, Link} from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import {Card, CardContent, CardHeader, CardTitle} from "../components/ui/card.jsx";
import {Badge} from "../components/ui/badge.jsx";
import {Button} from "../components/ui/button.jsx";
import {
    CheckCircle2, XCircle, Loader2, Clock,
    ArrowLeft, Terminal, Check, X
} from "lucide-react";
import {getPipelineById} from "../api/cicdApi";

// Imports pour le WebSocket
import {Client} from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const STEPS_DEFINITION = [
    {id: 1, label: "Clonage du Dépôt (Git)", marker: "--- ÉTAPE 1"},
    {id: 2, label: "Build & Test (Maven)", marker: "--- ÉTAPE 2"},
    {id: 3, label: "Analyse SonarQube", marker: "--- ÉTAPE 2.5: ANALYSE QUALITÉ ---"},
    {id: 4, label: "Construction Image Docker", marker: "--- ÉTAPE 3"},
    {id: 5, label: "Transfert vers la VM", marker: "--- TRANSFERT"},
    {id: 6, label: "Déploiement (SSH)", marker: "--- ÉTAPE 4"}
];

export default function PipelineDetail() {
    const {id} = useParams();
    const [pipeline, setPipeline] = useState(null);
    const logsEndRef = useRef(null);

    // Fonction de parsing des étapes (inchangée)
    const getStepsStatus = (logs, globalStatus) => {
        if (!logs) logs = "";
        return STEPS_DEFINITION.map((step, index) => {
            const isStarted = logs.includes(step.marker);
            const nextStep = STEPS_DEFINITION[index + 1];
            const isNextStarted = nextStep ? logs.includes(nextStep.marker) : false;
            const isCompleted = isStarted && (isNextStarted || (globalStatus === "SUCCESS" && index === STEPS_DEFINITION.length - 1));

            let status = "PENDING";
            if (isCompleted) {
                status = "SUCCESS";
            } else if (isStarted) {
                if (globalStatus === "FAILED") {
                    status = "FAILED";
                } else if (globalStatus === "SUCCESS") {
                    status = "SUCCESS";
                } else {
                    status = "RUNNING";
                }
            }
            return {...step, status};
        });
    };

    // 1. Chargement initial (HTTP classique)
    useEffect(() => {
        const fetchInitialData = async () => {
            const data = await getPipelineById(id);
            if (data) setPipeline(data);
        };
        fetchInitialData();
    }, [id]);

    // 2. Connexion WebSocket (Temps réel)
    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('/ws'),

            // Reconnexion automatique
            reconnectDelay: 2000,

            onConnect: () => {
                console.log("Connecté au WebSocket !");
                // Abonnement au topic spécifique de ce pipeline
                client.subscribe(`/topic/pipeline/${id}`, (message) => {
                    if (message.body) {
                        const updatedPipeline = JSON.parse(message.body);
                        setPipeline(updatedPipeline);
                    }
                });
            },
            onStompError: (frame) => {
                console.error('Erreur Broker: ' + frame.headers['message']);
            },
        });

        client.activate();

        // Nettoyage lors du démontage du composant
        return () => {
            client.deactivate();
        };
    }, [id]);

    // Scroll automatique vers le bas
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
                    {/* Header */}
                    <div className="flex items-center justify-between">
                        <Link to="/">
                            <Button variant="ghost" className="gap-2">
                                <ArrowLeft className="w-4 h-4"/> Retour
                            </Button>
                        </Link>
                        <GlobalStatusBadge status={pipeline.status}/>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        {/* Étapes */}
                        <Card className="lg:col-span-1 h-fit">
                            <CardHeader>
                                <CardTitle>Étapes du Processus</CardTitle>
                            </CardHeader>
                            <CardContent className="pt-2">
                                <div className="relative pl-8 md:pl-10">
                                    <div className="space-y-5">
                                        {steps.map((step, idx) => (
                                            <div key={step.id} className="relative pb-5">
                                                {/* Connecteur: part sous l'icône et va jusqu'au rond suivant */}
                                                <div
                                                    className={`${idx === steps.length - 1 ? 'hidden' : ''} absolute left-4 md:left-5 -translate-x-1/2 top-8 h-[calc(100%+2rem)] w-px ${step.status === 'SUCCESS' ? 'bg-green-300' : 'bg-gray-200'} z-0`}
                                                />

                                                {/* Icône */}
                                                <div className="absolute left-4 md:left-5 -translate-x-1/2 top-1.5 z-10">
                                                    <div className="w-8 h-8 rounded-full bg-white ring-2 ring-gray-200 flex items-center justify-center">
                                                        <StepIcon status={step.status} />
                                                    </div>
                                                </div>

                                                {/* Contenu */}
                                                <div className="ml-6 md:ml-8 p-2 rounded-lg transition-colors hover:bg-gray-50">
                                                    <div
                                                        className={`text-sm font-medium ${step.status === 'RUNNING' ? 'text-blue-700' : step.status === 'FAILED' ? 'text-red-700' : 'text-gray-700'}`}
                                                    >
                                                        {step.label}
                                                    </div>
                                                    <div className="text-xs text-gray-400 font-mono">{step.status}</div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            </CardContent>
                        </Card>

                        {/* Logs */}
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

// Sous-composants inchangés
function StepIcon({status}) {
    switch (status) {
        case "SUCCESS":
            return <Check className="w-4 h-4 text-green-600"/>;
        case "FAILED":
            return <X className="w-4 h-4 text-red-600"/>;
        case "RUNNING":
            return <Loader2 className="w-4 h-4 text-blue-600 animate-spin"/>;
        default:
            return null;
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