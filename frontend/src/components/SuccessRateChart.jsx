import React, {useMemo} from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid} from "recharts";

export default function SuccessRateChart({data}) {
    const series = useMemo(() => {
        return (data || []).map((item) => {
            const total = (item.success || 0) + (item.failed || 0);
            const rate = total > 0 ? Math.round((item.success / total) * 100) : 0;
            return {...item, rate};
        });
    }, [data]);

    return (
        <Card>
            <CardHeader>
                <CardTitle>Taux de succès par jour</CardTitle>
            </CardHeader>
            <CardContent className="pl-2">
                <div className="h-[220px] w-full">
                    {(!series || series.length === 0) ? (
                        <div className="h-full flex items-center justify-center text-gray-400 text-sm">
                            Pas assez de données pour afficher le graphique
                        </div>
                    ) : (
                        <ResponsiveContainer width="100%" height="100%">
                            <AreaChart data={series} margin={{left: 0, right: 12}}>
                                <defs>
                                    <linearGradient id="successRate" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="0%" stopColor="#22c55e" stopOpacity={0.4}/>
                                        <stop offset="100%" stopColor="#22c55e" stopOpacity={0.05}/>
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                                <XAxis
                                    dataKey="date"
                                    stroke="#888888"
                                    fontSize={12}
                                    tickLine={false}
                                    axisLine={false}
                                    tickFormatter={(value) => new Date(value).toLocaleDateString(undefined, {
                                        weekday: 'short',
                                        day: 'numeric'
                                    })}
                                />
                                <YAxis
                                    stroke="#888888"
                                    fontSize={12}
                                    tickLine={false}
                                    axisLine={false}
                                    allowDecimals={false}
                                    domain={[0, 100]}
                                    tickFormatter={(v) => `${v}%`}
                                />
                                <Tooltip
                                    formatter={(value) => [`${value}%`, "Succès"]}
                                    contentStyle={{
                                        background: '#0f172a',
                                        border: '1px solid #1e293b',
                                        color: '#fff',
                                        borderRadius: '6px'
                                    }}
                                    labelStyle={{color: '#cbd5e1'}}
                                />
                                <Area
                                    type="monotone"
                                    dataKey="rate"
                                    name="Succès"
                                    stroke="#22c55e"
                                    fill="url(#successRate)"
                                    strokeWidth={2}
                                    dot={{r: 3}}
                                />
                            </AreaChart>
                        </ResponsiveContainer>
                    )}
                </div>
            </CardContent>
        </Card>
    );
}
