import React, { useState } from "react";
import { Button, CircularProgress } from "@mui/material";
import { triggerPipeline } from "../api/cicdApi";

export default function DeployButton() {
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");

    const handleDeploy = async () => {
        setLoading(true);
        const res = await triggerPipeline();
        setMessage(res.message);
        setLoading(false);
    };

    return (
        <div style={{ marginBottom: "20px" }}>
            <Button variant="contained" color="primary" size="large" onClick={handleDeploy} disabled={loading}>
                {loading ? <CircularProgress size={24} color="inherit"/> : "DÉPLOYER"}
            </Button>
            {message && <p>{message}</p>}
        </div>
    );
}
