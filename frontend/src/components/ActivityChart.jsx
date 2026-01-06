import React from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend, CartesianGrid} from "recharts";

export default function ActivityChart({data}) {
    return (
        <Card className="col-span-2">
            <CardHeader>
                <CardTitle>Activité des 7 derniers jours</CardTitle>
            </CardHeader>
            <CardContent className="pl-2">
                <div className="h-[220px] w-full">
                    {(!data || data.length === 0) ? (
                        <div className="h-full flex items-center justify-center text-gray-400 text-sm">
                            Pas assez de données pour afficher le graphique
                        </div>
                    ) : (
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={data} barSize={18}>
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
                                />
                                <Tooltip
                                    contentStyle={{
                                        background: '#0f172a',
                                        border: '1px solid #1e293b',
                                        color: '#fff',
                                        borderRadius: '6px'
                                    }}
                                    labelStyle={{color: '#cbd5e1'}}
                                />
                                <Legend />
                                <Bar dataKey="success" name="Succès" stackId="a" fill="#22c55e" radius={[3,3,0,0]} />
                                <Bar dataKey="failed" name="Échecs" stackId="a" fill="#ef4444" radius={[3,3,0,0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    )}
                </div>
            </CardContent>
        </Card>
    );
}