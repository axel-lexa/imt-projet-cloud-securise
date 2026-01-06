// src/components/ActivityChart.jsx
import React from "react"
import { Bar, BarChart, CartesianGrid, XAxis, Tooltip } from "recharts"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart"

const chartData = [
    { day: "Lundi", success: 12, failed: 2 },
    { day: "Mardi", success: 18, failed: 1 },
    { day: "Mercredi", success: 15, failed: 5 },
    { day: "Jeudi", success: 25, failed: 3 },
    { day: "Vendredi", success: 20, failed: 0 },
]

const chartConfig = {
    success: { label: "Succès", color: "hsl(var(--chart-1))" },
    failed: { label: "Échecs", color: "hsl(var(--chart-2))" },
}

export function ActivityChart() {
    return (
        <Card className="col-span-1 lg:col-span-2">
            <CardHeader>
                <CardTitle className="text-sm font-medium">Activité Hebdomadaire</CardTitle>
            </CardHeader>
            <CardContent>
                <ChartContainer config={chartConfig} className="h-[200px] w-full">
                    <BarChart data={chartData}>
                        <CartesianGrid vertical={false} strokeDasharray="3 3" />
                        <XAxis
                            dataKey="day"
                            tickLine={false}
                            tickMargin={10}
                            axisLine={false}
                        />
                        <ChartTooltip content={<ChartTooltipContent />} />
                        // Dans src/components/ActivityChart.jsx
                        <Bar dataKey="success" fill="#22c55e" radius={4} /> {/* Vert */}
                        <Bar dataKey="failed" fill="#ef4444" radius={4} />  {/* Rouge */}                    </BarChart>
                </ChartContainer>
            </CardContent>
        </Card>
    )
}