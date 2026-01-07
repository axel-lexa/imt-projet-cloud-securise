import React, { useEffect, useMemo, useState } from "react";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import PipelineCard from "../components/PipelineCard";
import { getPipelines } from "../api/cicdApi";

export default function History() {
    const [pipelines, setPipelines] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [search, setSearch] = useState("");
    const [statusFilters, setStatusFilters] = useState(new Set(["SUCCESS", "RUNNING", "FAILED", "PENDING"]));
    const [dateFrom, setDateFrom] = useState("");
    const [dateTo, setDateTo] = useState("");
    const [sortKey, setSortKey] = useState("date_desc");

    const loadPipelines = async () => {
        try {
            const data = await getPipelines();
            const sorted = Array.isArray(data)
                ? [...data].sort((a, b) => new Date(b.startTime || 0) - new Date(a.startTime || 0))
                : [];
            setPipelines(sorted);
            setError(null);
        } catch (e) {
            console.error("Erreur historique:", e);
            setError("Impossible de charger l'historique");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadPipelines();
        const interval = setInterval(loadPipelines, 5000);
        return () => clearInterval(interval);
    }, []);

    const toggleStatus = (status) => {
        setStatusFilters((prev) => {
            const next = new Set(prev);
            next.has(status) ? next.delete(status) : next.add(status);
            return next;
        });
    };

    const resetFilters = () => {
        setSearch("");
        setStatusFilters(new Set(["SUCCESS", "RUNNING", "FAILED", "PENDING"]));
        setDateFrom("");
        setDateTo("");
        setSortKey("date_desc");
    };

    const getDurationMs = (p) => {
        if (!p?.startTime) return Number.POSITIVE_INFINITY;
        const start = new Date(p.startTime);
        const end = p.endTime ? new Date(p.endTime) : new Date();
        return end - start;
    };

    const filtered = useMemo(() => {
        return pipelines
            .filter((p) => {
                // statut
                if (statusFilters.size > 0 && !statusFilters.has(p.status || "PENDING")) return false;

                // recherche sur repo/name/id
                const term = search.trim().toLowerCase();
                if (term) {
                    const haystack = `${p.name || ""} ${p.repoUrl || ""} ${p.id || ""}`.toLowerCase();
                    if (!haystack.includes(term)) return false;
                }

                // date range
                if (dateFrom) {
                    const start = p.startTime ? new Date(p.startTime) : null;
                    if (!start || start < new Date(dateFrom)) return false;
                }
                if (dateTo) {
                    const start = p.startTime ? new Date(p.startTime) : null;
                    if (!start || start > new Date(`${dateTo}T23:59:59`)) return false;
                }

                return true;
            })
            .sort((a, b) => {
                if (sortKey === "date_asc") return new Date(a.startTime || 0) - new Date(b.startTime || 0);
                if (sortKey === "date_desc") return new Date(b.startTime || 0) - new Date(a.startTime || 0);
                if (sortKey === "dur_asc") return getDurationMs(a) - getDurationMs(b);
                if (sortKey === "dur_desc") return getDurationMs(b) - getDurationMs(a);
                return 0;
            });
    }, [pipelines, search, statusFilters, dateFrom, dateTo, sortKey]);

    return (
        <div className="flex min-h-screen bg-gray-50/50">
            <Sidebar />
            <div className="flex-1">
                <Topbar title="Historique des Pipelines" />
                <main className="p-6 space-y-4">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-semibold tracking-tight">Toutes les exécutions</h2>
                        <span className="text-sm text-muted-foreground">{filtered.length} élément(s)</span>
                    </div>

                    {/* Filtres */}
                    <div className="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 bg-white border rounded-lg p-4 shadow-sm">
                        <div className="flex flex-col gap-2">
                            <label className="text-sm text-muted-foreground">Recherche (nom / repo / id)</label>
                            <input
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className="border rounded-md px-3 py-2 text-sm"
                                placeholder="ex: api-service"
                            />
                        </div>

                        <div className="flex flex-col gap-2 sm:col-span-2 lg:col-span-1">
                            <label className="text-sm text-muted-foreground">Statuts</label>
                            <div className="flex flex-wrap gap-2">
                                {["SUCCESS", "RUNNING", "FAILED", "PENDING"].map((s) => (
                                    <button
                                        key={s}
                                        type="button"
                                        onClick={() => toggleStatus(s)}
                                        className={`px-3 py-1 rounded-full text-xs border transition ${
                                            statusFilters.has(s)
                                                ? "bg-primary text-primary-foreground border-primary"
                                                : "bg-muted text-muted-foreground border-border"
                                        }`}
                                    >
                                        {s}
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div className="flex flex-col gap-2 sm:col-span-2 lg:col-span-1 xl:col-span-2">
                            <label className="text-sm text-muted-foreground">Date début (du / au)</label>
                            <div className="flex flex-col gap-2">
                                <input
                                    type="date"
                                    value={dateFrom}
                                    onChange={(e) => setDateFrom(e.target.value)}
                                    className="border rounded-md px-3 py-2 text-sm w-full"
                                />
                                <input
                                    type="date"
                                    value={dateTo}
                                    onChange={(e) => setDateTo(e.target.value)}
                                    className="border rounded-md px-3 py-2 text-sm w-full"
                                />
                            </div>
                        </div>

                        <div className="flex flex-col gap-2 sm:col-span-2 lg:col-span-1">
                            <label className="text-sm text-muted-foreground">Tri</label>
                            <select
                                value={sortKey}
                                onChange={(e) => setSortKey(e.target.value)}
                                className="border rounded-md px-3 py-2 text-sm"
                            >
                                <option value="date_desc">Date (récent → ancien)</option>
                                <option value="date_asc">Date (ancien → récent)</option>
                                <option value="dur_desc">Durée (longue → courte)</option>
                                <option value="dur_asc">Durée (courte → longue)</option>
                            </select>
                            <button
                                type="button"
                                onClick={resetFilters}
                                className="text-xs text-muted-foreground underline text-left"
                            >
                                Réinitialiser
                            </button>
                        </div>
                    </div>

                    {error && (
                        <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-md">
                            {error}
                        </div>
                    )}

                    {loading ? (
                        <div className="text-center text-muted-foreground py-10">Chargement de l'historique...</div>
                    ) : filtered.length === 0 ? (
                        <div className="text-center text-muted-foreground py-10">Aucun pipeline enregistré.</div>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            {filtered.map((p) => (
                                <PipelineCard key={p.id} pipeline={p} />
                            ))}
                        </div>
                    )}
                </main>
            </div>
        </div>
    );
}
