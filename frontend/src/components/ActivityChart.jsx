import React from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend} from "recharts";

export default function ActivityChart({data}) {
    return (
        <Card className="col-span-2">
            <CardHeader>
                <CardTitle>Activité des 7 derniers jours</CardTitle>
            </CardHeader>
            <CardContent className="pl-2">
                <div className="h-[200px] w-full">
                    {(!data || data.length === 0) ? (
                        <div className="h-full flex items-center justify-center text-gray-400 text-sm">
                            Pas assez de données pour afficher le graphique
                        </div>
                    ) : (
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={data}>
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
                                        background: '#333',
                                        border: 'none',
                                        color: '#fff',
                                        borderRadius: '5px'
                                    }}
                                    labelStyle={{color: '#ccc'}}
                                />
                                <Legend/>
                                <Bar dataKey="success" name="Succès" fill="#22c55e" radius={[4, 4, 0, 0]} barSize={30}/>
                                <Bar dataKey="failed" name="Échecs" fill="#ef4444" radius={[4, 4, 0, 0]} barSize={30}/>
                            </BarChart>
                        </ResponsiveContainer>
                    )}
                </div>
            </CardContent>
        </Card>
    );
}