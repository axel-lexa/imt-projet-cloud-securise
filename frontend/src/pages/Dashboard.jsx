import React, {useState, useEffect} from "react";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import PipelineCard from "../components/PipelineCard";
import ActivityChart from "../components/ActivityChart";
import SuccessRateChart from "../components/SuccessRateChart";
import {Play, CheckCircle2, AlertCircle} from "lucide-react";
import {Card, CardContent} from "../components/ui/card.jsx";
import {getPipelines} from "../api/cicdApi";
import { Link } from "react-router-dom";

export default function Dashboard() {
    const [pipelines, setPipelines] = useState([]);
    const [chartData, setChartData] = useState([]);

    // Fonction pour transformer la liste brute en données pour le graphique
    const processChartData = (data) => {
        const stats = {};

        data.forEach(p => {
            if (!p.startTime) return;
            // On extrait la date (YYYY-MM-DD)
            const date = new Date(p.startTime).toISOString().split('T')[0];

            if (!stats[date]) {
                stats[date] = {date, success: 0, failed: 0};
            }

            if (p.status === 'SUCCESS') stats[date].success += 1;
            if (p.status === 'FAILED') stats[date].failed += 1;
        });

        // On transforme l'objet en tableau trié par date et on prend les 7 derniers jours
        return Object.values(stats)
            .sort((a, b) => new Date(a.date) - new Date(b.date))
            .slice(-7);
    };

    const loadData = async () => {
        try {
            const data = await getPipelines();
            if (Array.isArray(data)) {
                setPipelines(data);
                setChartData(processChartData(data));
            }
        } catch (error) {
            console.error("Erreur dashboard:", error);
        }
    };

    useEffect(() => {
        loadData();
        const interval = setInterval(loadData, 3000);
        return () => clearInterval(interval);
    }, []);

    const successCount = pipelines.filter(p => p.status === "SUCCESS").length;
    const failedCount = pipelines.filter(p => p.status === "FAILED").length;
    const total = pipelines.length;

    return (
        <div className="flex min-h-screen bg-gray-50/50">
            <Sidebar/>
            <div className="flex-1">
                <Topbar title="Aperçu des Pipelines"/>
                <main className="p-6 space-y-6">
                    {/* Stats Cards */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <StatCard title="Total Lancés" value={total}
                                  icon={<Play className="text-blue-500"/>}/>
                        <StatCard title="Succès" value={successCount}
                                  icon={<CheckCircle2 className="text-green-500"/>}/>
                        <StatCard title="Échecs" value={failedCount} icon={<AlertCircle className="text-red-500"/>}/>
                    </div>

                    {/* Graphique avec les VRAIES données */}
                    <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
                        <div className="lg:col-span-2">
                            <ActivityChart data={chartData}/>
                        </div>
                        <div className="lg:col-span-2">
                            <SuccessRateChart data={chartData}/>
                        </div>

                        {/*<Card className="p-6">
                            <h3 className="font-bold mb-2">Santé du Système</h3>
                            <p className="text-sm text-muted-foreground">Backend opérationnel.</p>
                            <div className="mt-4 h-2 w-full bg-green-100 rounded-full overflow-hidden">
                                <div className="h-full bg-green-500 w-full"/>
                            </div>
                        </Card>*/}
                    </div>

                    {/* Liste des Pipelines */}
                    <h2 className="text-xl font-semibold tracking-tight">Historique Récent</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                        {pipelines.length === 0 ? (
                            <p className="text-gray-500 col-span-3 text-center py-10">Aucun pipeline.</p>
                        ) : (
                            [...pipelines]
                                .sort((a, b) => {
                                    const as = a.startTime ? new Date(a.startTime).getTime() : 0;
                                    const bs = b.startTime ? new Date(b.startTime).getTime() : 0;
                                    if (bs !== as) return bs - as;
                                    return (b.id || 0) - (a.id || 0);
                                })
                                .slice(0, 3)
                                .map(p => <PipelineCard key={p.id} pipeline={p} />)
                        )}
                    </div>
                    <div className="mt-2">
                        <Link to="/history" className="text-sm text-primary hover:underline">Voir tout l'historique</Link>
                    </div>
                </main>
            </div>
        </div>
    );
}

function StatCard({title, value, icon}) {
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