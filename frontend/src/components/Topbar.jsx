import React from "react";
import DeployButton from "./DeployButton";

export default function Topbar() {
    return (
        <div className="flex justify-between items-center p-4 bg-white shadow">
            <h1 className="text-2xl font-bold">Dashboard CI/CD</h1>
            <DeployButton />
        </div>
    );
}
