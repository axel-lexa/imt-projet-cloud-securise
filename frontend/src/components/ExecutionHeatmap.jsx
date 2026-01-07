import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card.jsx";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "./ui/tooltip.jsx";

const days = ["Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"];

export default function ExecutionHeatmap({ matrix }) {
    const flat = Object.values(matrix || {}).map((row) => Object.values(row)).flat();
    const max = flat.length ? Math.max(...flat) : 0;

    const getColor = (val) => {
        if (!val || max === 0) return "bg-slate-100";
        const ratio = val / max;
        if (ratio > 0.66) return "bg-emerald-500";
        if (ratio > 0.33) return "bg-emerald-300";
        return "bg-emerald-200";
    };

    return (
        <Card className="h-full">
            <CardHeader>
                <CardTitle>Heatmap exécutions (heure / jour)</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="text-xs text-muted-foreground mb-2">Plus la case est foncée, plus il y a d'exécutions.</div>
                <div className="grid grid-cols-[40px_repeat(6,1fr)] gap-1 text-xs">
                    <div />
                    {[6,8,10,14,18,22].map((h) => (
                        <div key={h} className="text-center text-muted-foreground">{h}h</div>
                    ))}
                    {days.map((dayLabel, dIdx) => (
                        <React.Fragment key={dayLabel}>
                            <div className="text-muted-foreground flex items-center">{dayLabel}</div>
                            {[6,8,10,14,18,22].map((h) => {
                                const val = matrix?.[dIdx]?.[h] || 0;
                                return (
                                    <TooltipProvider key={`${dIdx}-${h}`}>
                                        <Tooltip>
                                            <TooltipTrigger asChild>
                                                <div className={`h-8 rounded ${getColor(val)}`} />
                                            </TooltipTrigger>
                                            <TooltipContent>
                                                <div className="text-xs">{dayLabel} {h}h — {val} exécution(s)</div>
                                            </TooltipContent>
                                        </Tooltip>
                                    </TooltipProvider>
                                );
                            })}
                        </React.Fragment>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
}
