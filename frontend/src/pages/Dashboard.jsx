import React, { useEffect, useState } from "react";
import DeployButton from "../components/DeployButton";
import DeploymentTable from "../components/DeploymentTable";
import { getPipelines } from "../api/cicdApi";

export default function Dashboard() {
    const [pipelines, setPipelines] = useState([]);

    const fetchPipelines = async () => {
        const data = await getPipelines();
        setPipelines(data);
    };

    useEffect(() => {
        fetchPipelines();
        const interval = setInterval(fetchPipelines, 5000); // rafraîchit toutes les 5s
        return () => clearInterval(interval);
    }, []);

    return (
        <div style={{ padding: "20px" }}>
            <h1>CI/CD Dashboard</h1>
            <DeployButton />
            <DeploymentTable pipelines={pipelines} />
        </div>
    );
}
